package grondag.hard_science.simulator.jobs;

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.exotic_matter.simulator.Simulator;
import grondag.exotic_matter.simulator.domain.IDomain;
import grondag.exotic_matter.simulator.domain.IDomainCapability;
import grondag.exotic_matter.simulator.job.RequestPriority;
import grondag.exotic_matter.simulator.job.RequestStatus;
import grondag.exotic_matter.simulator.persistence.AssignedNumber;
import grondag.exotic_matter.simulator.persistence.IIdentified;
import grondag.exotic_matter.varia.structures.SimpleUnorderedArrayList;
import grondag.hard_science.simulator.jobs.tasks.PerpetualTask;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class JobManager implements IDomainCapability
{
    private static final int PROCESS_JOB_ID = IIdentified.FIRST_SYSTEM_ID;
    private static final String NBT_SELF = NBTDictionary.claim("jobMgr");
    private static final String NBT_CHILDREN = NBTDictionary.claim("jobChildren");
    
    /**
     * Lazily created / retrieved.  Task holder for automated resource processing.
     */
    private Job processJob;
    
    /**
     * Should be used for job/task accounting - not for any actual work done by tasks.
     */
    public static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(
        new ThreadFactory()
        {
            private AtomicInteger count = new AtomicInteger(1);
            @Override
            public Thread newThread(@Nullable Runnable r)
            {
                Thread thread = new Thread(r, "Hard Science Job Manager Thread -" + count.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        });
    
    
    protected IDomain domain;
    
    /**
     * Job are containers that hold all of our tasks. <br>
     * Jobs manage task serialization and hold shared state like userName and priority. <br>
     * Jobs do not execute directly.
     */
    private final SimpleUnorderedArrayList<Job> jobs = new SimpleUnorderedArrayList<Job>();
    
    /**
     * Contains jobs with tasks that are ready for execution - will typically have a WAITING status.
     * Tasks are posted to the backlog by their parent job when the task becomes ready to execute.
     * Tasks are pulled from the front of the backlog by machines or processes that consume tasks.
     */
    @SuppressWarnings("unchecked")
    private final LinkedList<Job>[] backlogJobs = new LinkedList[RequestPriority.values().length];
    
    
    /**
     * Retrieves task holder for automated resource processing.
     * Creates job if does not already exist.
     */
    public Job processSystemJob()
    {
        if(this.processJob == null)
        {
            synchronized(this)
            {
                if(this.processJob == null)
                {
                    this.processJob = Job.jobFromId(PROCESS_JOB_ID);
                    if(this.processJob == null)
                    {
                        this.processJob = Job.createSystemJob(RequestPriority.MEDIUM, PROCESS_JOB_ID);
                        this.processJob.addTask(new PerpetualTask(true));
                        this.processJob.onJobAdded(this);
                    }
                }
            }
        }
        return this.processJob;
    }

    /**
     * Removes job from backlog, if it is checked there.
     */
    private void removeJobFromBacklogSynchronously(Job job)
    {
        if(job.effectivePriority() != null)
        {
            LinkedList<Job> list = backlogJobs[job.effectivePriority().ordinal()];
            if(list != null && !list.isEmpty())
            {
                list.remove(job);
            }
        }                
    }
    
    /**
     * Removes job from backlog, if it is checked there,
     * and then adds it to the end of the backlog for the
     * job's current priority.
     * 
     * If job was already in backlog for its current priority
     * its position within the backlog remains unchanged.
     */
    private void addOrReplaceJobInBacklogSynchronously(Job job)
    {
        boolean didRemove = false;
        if(job.effectivePriority() != job.getPriority())
        {
            removeJobFromBacklogSynchronously(job);
            job.updateEffectivePriority();
            didRemove = true;
        }
        
        LinkedList<Job> list = backlogJobs[job.effectivePriority().ordinal()];
        if(list == null)
        {
            list = new LinkedList<Job>();
            list.add(job);
            backlogJobs[job.effectivePriority().ordinal()] = list;
        }
        else if(didRemove || !list.contains(job))
        {
            list.addLast(job);
        }
    }
    
    /** Asynchronously adds job to backlog */
    public void addJob(Job job)
    {
        assert !job.isSystemJob() : "Invalid use of system job";
        
        EXECUTOR.execute(new Runnable()
        {
            @Override
            public void run()
            {
                job.onJobAdded(JobManager.this);
                jobs.addIfNotPresent(job);
                if(job.hasReadyWork()) addOrReplaceJobInBacklogSynchronously(job);
            }
        });
    }
    
    private static final Predicate<AbstractTask> MATCH_ANY_TASK = new Predicate<AbstractTask>()
    {
        @Override
        public boolean test(@Nullable AbstractTask t)
        {
            return true;
        }
    };
    
    /**
     * Searches for the fist ready task of the given type that meets the given predicate.
     * The status of the task is immediately changed to ACTIVE when it is claimed.
     * Future will contain null if no ready task could be checked.
     */
    public Future<AbstractTask> claimReadyWork(TaskType taskType, @Nullable Predicate<AbstractTask> predicate)
    {
        return EXECUTOR.submit(new Callable<AbstractTask>()
        {
            @Override
            public AbstractTask call() throws Exception
            {
                Predicate<AbstractTask> effectivePredicate = predicate == null ? MATCH_ANY_TASK : predicate;
                
                for(LinkedList<Job> list : backlogJobs)
                {
                    if(list != null && !list.isEmpty())
                    {
                        for(Job j : list)
                        {
                            if(j.hasReadyWork())
                            {
                                for(AbstractTask t : j)
                                {
                                    if(t.requestType() == taskType
                                            && t.getStatus() == RequestStatus.READY
                                            && effectivePredicate.test(t))
                                    {
                                        t.claim();
                                        return t;
                                    }
                                }
                            }
                        }
                    }
                }
                return null;
            }
        });
    }
    
    /**
     * Called by a job if it becomes ready or unready to execute.
     * Job manager will assume hasReadyWork is opposite of prior value.
     * (Do not call unless value changes.)
     * Call executes asynchronously.
     */
    public void notifyReadyStatus(Job job)
    {
        assert !job.isSystemJob() : "Invalid use of system job";
        
        EXECUTOR.execute(new Runnable()
        {
            @Override
            public void run()
            {
                if(job.hasReadyWork())
                    addOrReplaceJobInBacklogSynchronously(job);
                else
                    removeJobFromBacklogSynchronously(job);
            }
        });
    }
    
    /**
     * Called by a job when it terminates.
     * Call executes asynchronously.
     */
    public void notifyTerminated(Job job)
    {
        assert !job.isSystemJob() : "Invalid use of system job";
        
        EXECUTOR.execute(new Runnable()
        {
            @Override
            public void run()
            {
                removeJobFromBacklogSynchronously(job);
                jobs.removeIfPresent(job);
                
                // clean up id registry
                for(AbstractTask task : job)
                {
                    Simulator.instance().assignedNumbersAuthority().unregister(task);
                }
                Simulator.instance().assignedNumbersAuthority().unregister(job);
            }
        });
    }
    
    /**
     * Called by a job if it has a change in priority.
     */
    public void notifyPriorityChange(Job job)
    {
        assert !job.isSystemJob() : "Invalid use of system job";
        
        if(!job.isHeld() && job.hasReadyWork()) EXECUTOR.execute(new Runnable()
        {
            @Override
            public void run()
            {
                addOrReplaceJobInBacklogSynchronously(job);
            }
        });
    }
    
    /**
     * Called by job when it is held or released.
     */
    public void notifyHoldChange(Job job)
    {
        assert !job.isSystemJob() : "Invalid use of system job";
        
        if(job.isHeld())
        {
            EXECUTOR.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    removeJobFromBacklogSynchronously(job);
                }
            });
        }
        else if(job.hasReadyWork())
        {
            EXECUTOR.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    addOrReplaceJobInBacklogSynchronously(job);
                }
            });
        }
        
    }

    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag)
    {
        NBTTagList nbtJobs = tag.getTagList(NBT_CHILDREN, 10);
        if( nbtJobs != null && !nbtJobs.hasNoTags())
        {
            for(NBTBase subTag : nbtJobs)
            {
                if(subTag != null)
                {
                    Job j = new Job(this, (NBTTagCompound) subTag);
                    if(!j.getStatus().isTerminated) this.jobs.add(j);
                }
            }   
        }        
    }

    /**
     * Called after all domain deserialization is complete.  
     * Hook for tasks to handle actions that may require other objects to be deserialized start.
     */
    @Override
    public void afterDeserialization()
    {
        if(!this.jobs.isEmpty())
        {
            for(Job j : this.jobs)
            {
                j.afterDeserialization();
            }
        }    
    };
    
    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        if(!this.jobs.isEmpty())
        {
            NBTTagList nbtJobs = new NBTTagList();
            
            for(Job j : this.jobs)
            {
                nbtJobs.appendTag(j.serializeNBT());
            }
            tag.setTag(NBT_CHILDREN, nbtJobs);
        }        
    }
    
    @Override
    public @Nullable IDomain getDomain()
    {
        return this.domain;
    }
    
    @Override
    public void setDirty()
    {
        if(this.domain != null) this.domain.setDirty();
    }

    /**
     * Returns estimated count of tasks of given type in the queue.
     */
    public int getQueueDepth(TaskType blockFabrication)
    {
        // TODO Not a real implementation
        return  Simulator.instance().assignedNumbersAuthority().getIndex(AssignedNumber.TASK).size();
    }

    @Override
    public String tagName()
    {
        return NBT_SELF;
    }

    @Override
    public void setDomain(IDomain domain)
    {
        this.domain = domain;
    }
}
