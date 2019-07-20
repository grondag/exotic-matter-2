package grondag.hard_science.simulator.jobs;

import java.util.Iterator;

import javax.annotation.Nullable;

import grondag.exotic_matter.simulator.Simulator;
import grondag.exotic_matter.simulator.domain.IDomain;
import grondag.exotic_matter.simulator.domain.IDomainMember;
import grondag.exotic_matter.simulator.job.RequestPriority;
import grondag.exotic_matter.simulator.job.RequestStatus;
import grondag.exotic_matter.simulator.persistence.AssignedNumber;
import grondag.exotic_matter.simulator.persistence.IIdentified;
import grondag.fermion.serialization.IReadWriteNBT;
import grondag.fermion.serialization.NBTDictionary;
import grondag.fermion.structures.SimpleUnorderedArrayList;
import grondag.fermion.varia.Useful;
import grondag.xm2.Xm;
import grondag.xm2.block.virtual.ExcavationRenderTracker;
import grondag.xm2.placement.Build;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;

/**
 * Collection of tasks - typically does not do any meaningful by itself.
 * Responsible for serializing tasks contained within it.
 */
public class Job implements Iterable<AbstractTask>, IIdentified, IReadWriteNBT, IDomainMember
{
    private static final String NBT_REQUEST_PRIORITY = NBTDictionary.claim("jobPriority");
    private static final String NBT_REQUEST_USER_NAME = NBTDictionary.claim("jobUser");
    private static final String NBT_REQUEST_STATUS = NBTDictionary.claim("jobStatus");
    private static final String NBT_JOB_HELD_FLAG = NBTDictionary.claim("jobIsHeld");
    private static final String NBT_BUILD_ID = NBTDictionary.claim("jobBuildID");
    private static final String NBT_BUILD_DIMENSION_ID = NBTDictionary.claim("jobDimID");
    private static final String NBT_REQUEST_CHILDREN = NBTDictionary.claim("jobChildren");
    
    public static final String SYSTEM_USER_NAME = "system";
    
    public static Job createSystemJob(RequestPriority priority, int systemJobID)
    {
        return new Job(priority, systemJobID);
    }
    
    protected RequestPriority priority = RequestPriority.MEDIUM;
    
    /**
     * If assigned means this is a construction job.
     */
    private int buildID = IIdentified.UNASSIGNED_ID;
    
    /**
     * Dimension in which work is done, if applies.
     */
    private Identifier dimensionID;
    
    /**
     * Lazily set from dimensionID
     */
    private World world = null;
    
    /**
     * Used by job manager to know prior sort bucket when priority changes.
     */
    private RequestPriority effectivePriority = null;
    
    /**
     * Used by job manager to know prior status when status changes.
     */
    private RequestStatus effectiveStatus = null;
    
    private String userName;
    private JobManager jobManager = NullJobManager.INSTANCE;
    private RequestStatus status;
    
    /**
     * True if child task status may have changed and thus need to 
     * recompute status of this container request.
     */
    protected boolean isTaskStatusDirty = false;
    
    protected int readyWorkCount = 0;
    
    private final SimpleUnorderedArrayList<AbstractTask> tasks =  new SimpleUnorderedArrayList<AbstractTask>();
    
    private int id = IIdentified.UNASSIGNED_ID;
    
    private boolean isHeld;
    
    private final boolean isSystemJob;
    
    /** used by NBT deserialization */
    public Job()
    {
        // system jobs aren't serialized
        this.isSystemJob = false;
    };
    
    public Job(RequestPriority priority, PlayerEntity player)
    {
        this.priority = priority;
        this.userName = player == null ? SYSTEM_USER_NAME : player.getEntityName();
        Simulator.instance().assignedNumbersAuthority().register(this);
        this.isSystemJob = false;
    }
    
    /**
     * System-type job, does not show in backlog, not persisted.
     */
    private Job (RequestPriority priority, int systemJobID)
    {
        assert IIdentified.isSystemID(systemJobID) : "Bad ID for system job";
        this.id = systemJobID;
        this.isSystemJob = true;
        this.priority = priority;
        this.userName = SYSTEM_USER_NAME;
        Simulator.instance().assignedNumbersAuthority().register(this);
    }
    
    /**
     * Called by job manager during deserialization.
     */
    public Job(JobManager manager, CompoundTag tag)
    {
        this.isSystemJob = false;
        this.jobManager = manager;
        this.deserializeNBT(tag);
    }
    
    public boolean isSystemJob()
    {
        return this.isSystemJob;
    }
    
    /**
     * Called when a new job is added to the job manager.
     * Is not called during deserialization.
     */
    public void onJobAdded(JobManager manager)
    {
        this.jobManager = manager;
        this.onLoaded();
        this.setDirty();
    }
    
    /**
     * Called just before a new job is added to the job manager
     * and when a job is deserialized right before tasks are available to be executed.
     * Called only 1X in each case.<p>
     * 
     * Job manager will always be non-null at this point and
     * any tasks that exist before now will be initialized at 
     * this point so that they have access to domain objects.<p>
     * 
     * Does not call {@link #updateReadyWork(int)} if initialized
     * tasks are ready but does increment {@link #readyWorkCount}
     * because job manager will check {@link #hasReadyWork()} right
     * after this runs.  So no need to notify job manager of ready tasks.
     */
    protected void onLoaded()
    {
        if(this.buildID != IIdentified.UNASSIGNED_ID)
        {
            ExcavationRenderTracker.INSTANCE.add(this);
        }
        
        synchronized(this.tasks)
        {
            for(AbstractTask t : tasks)
            {
                if(t.initialize(this)) this.readyWorkCount++;
            }
        }
    }
    
    protected void updateReadyWork(int delta)
    {
        
        // system jobs don't update job manager with ready work
        // because not in the backlog
        if(this.isSystemJob) return;
        
        if(delta > 0)
        {
            if(this.readyWorkCount == 0)
            {
                this.readyWorkCount = delta;
                this.jobManager.notifyReadyStatus(this);
            }
            else
            {
                this.readyWorkCount += delta;
            }
        }
        else if(delta < 0)
        {
            if(this.readyWorkCount > 0)
            {
                this.readyWorkCount += delta;
                if(this.readyWorkCount < 0)
                {
                    this.readyWorkCount = 0;
                    Xm.LOG.warn("Job tried to decrement ready work count below zero. This is probably a bug.");
                }
                if(this.readyWorkCount == 0)
                {
                    this.jobManager.notifyReadyStatus(this);
                }
            }
            else
            {
                Xm.LOG.warn("Job tried to decrement ready work count below zero. This is probably a bug.");
            }
        }
    }
    
    /**
     * Adds task to this job. Task will be initialized 
     * immediately if job is already in the domain job 
     * manager. Otherwise will be initialized right 
     * after this job is added to the domain job manager.
     */
    public void addTask(AbstractTask task)
    {
        synchronized(this.tasks)
        {
            this.isTaskStatusDirty = true;
            tasks.add(task);
            
            // don't initialize tasks until part of the domain
            if(this.jobManager != null && this.jobManager != NullJobManager.INSTANCE)
            {
                if(task.initialize(this)) this.updateReadyWork(1);
            }
        }
        this.setDirty();
    }
    
    public void addTasks(AbstractTask... tasks)
    {
        synchronized(this.tasks)
        {
            if(tasks.length > 0)
            {
                this.isTaskStatusDirty = true;
                int readyCount = 0;
                for(AbstractTask t : tasks)
                {
                    this.tasks.add(t);
                    if(this.jobManager != null)
                    {
                        // don't initialize tasks until we are part of the domain
                        if(t.initialize(this)) readyCount++;
                    }
                }
                if(readyCount > 0) this.updateReadyWork(readyCount);
            }
        }
        this.setDirty();
    }
    
    public RequestPriority getPriority()
    {
        return this.priority;
    }

    public void setPriority(RequestPriority priority)
    {
        if(priority != this.priority)
        {
            this.priority = priority;
            
            // system jobs don't update job manager with 
            // priority because not in the backlog
            if(!this.isSystemJob)
            {
                this.jobManager.notifyPriorityChange(this);
                this.setDirty();
            }
        }
    }
    
    /** 
     * Will be {@link Job#SYSTEM_USER_NAME} for automated jobs.
     */
    public String userName()
    {
        return this.userName;
    }

    protected void setStatus(RequestStatus newStatus)
    {
        if(newStatus != this.status)
        {
            this.status = newStatus;
            // system jobs don't update job manager with 
            // termination because not in the backlog
            if(newStatus.isTerminated && !this.isSystemJob)
            {
                this.jobManager.notifyTerminated(this);
            }
            this.setDirty();
        }
    }
    
    protected void updateTaskStatusIfNeeded()
    {
        if(this.isTaskStatusDirty)
        {
            this.isTaskStatusDirty = false;

            @SuppressWarnings("unused")
            int abend = 0, active = 0, ready = 0, cancelled = 0, complete = 0, is_new = 0, waiting = 0;
            for(AbstractTask t : this.tasks)
            {
                switch(t.getStatus())
                {
                case ABEND:
                    abend++;
                    break;
                case ACTIVE:
                    active++;
                    break;
                case READY:
                    ready++;
                    break;
                case CANCELLED:
                    cancelled++;
                    break;
                case COMPLETE:
                    complete++;
                    break;
                case NEW:
                    is_new++;
                    break;
                case WAITING:
                    waiting++;
                    break;
                default:
                    break;
                
                }
            }
            
            if(complete + cancelled + abend == tasks.size())
            {
                // no active tasks
                this.setStatus(abend == 0 ? RequestStatus.COMPLETE : RequestStatus.ABEND);
            }
        }
    }
    public RequestStatus getStatus()
    {
        this.updateTaskStatusIfNeeded();
        return this.status;
    }
    
    /**
     * True if job is held - will not process.
     */
    public boolean isHeld()
    {
        return this.isHeld;
    }
    
    /**
     * Allows a held job to run.
     */
    public void release()
    {
        if(this.isHeld)
        {
            this.isHeld = false;
            
            // system jobs don't update job manager with 
            // hold because not in the backlog
            if(!this.isSystemJob) this.jobManager.notifyHoldChange(this);
            
            this.setDirty();
        }
    }
    
    public void hold()
    {
        if(!this.isHeld)
        {
            this.isHeld = true;
            // system jobs don't update job manager with 
            // hold because not in the backlog
            if(!this.isSystemJob) this.jobManager.notifyHoldChange(this);
            this.setDirty();
        }
    }
    
    public void cancel()
    {
        synchronized(this.tasks)
        {
            setStatus(RequestStatus.CANCELLED);
            if(!this.tasks.isEmpty())
            {
                this.isTaskStatusDirty = true;
                for(AbstractTask t : this.tasks)
                {
                    if(!t.isTerminated()) t.cancel();
                }
            }
        }
    }

    @Override
    public void deserializeNBT(@Nullable CompoundTag tag)
    {
        this.tasks.clear();
        
        this.deserializeID(tag);
        this.priority = Useful.safeEnumFromTag(tag, NBT_REQUEST_PRIORITY, RequestPriority.CRITICAL);
        this.userName = tag.getString(NBT_REQUEST_USER_NAME);
        this.status = Useful.safeEnumFromTag(tag, NBT_REQUEST_STATUS, RequestStatus.NEW);
        this.isHeld = tag.getBoolean(NBT_JOB_HELD_FLAG);
        this.buildID = tag.getInt(NBT_BUILD_ID);
        this.dimensionID = new Identifier(tag.getString(NBT_BUILD_DIMENSION_ID));
        
        Simulator.instance().assignedNumbersAuthority().register(this);
        
        int readyCount = 0;
        ListTag nbtTasks = tag.getList(NBT_REQUEST_CHILDREN, 10);
        if( nbtTasks != null && !nbtTasks.isEmpty())
        {
            for(Tag subTag : nbtTasks)
            {
                if(subTag != null)
                {
                    AbstractTask t = TaskType.deserializeTask((CompoundTag) subTag, this);
                    if(t != null)
                    {
                        this.tasks.add(t);
                        if(t.getStatus() == RequestStatus.READY) readyCount++;
                    }
                }
            }  
        }
        
        this.isTaskStatusDirty = true;
        
        if(!this.getStatus().isTerminated) this.onLoaded();
        
        if(readyCount > 0) this.updateReadyWork(readyCount);
        
    }

    /**
     * Called after all domain deserialization is complete.  
     * Hook for tasks to handle actions that may require other objects to be deserialized start.
     */
    public void afterDeserialization()
    {
        for(AbstractTask t : this.tasks)
        {
            t.afterDeserialization();
        }
    }
    
    @Override
    public AssignedNumber idType()
    {
        return AssignedNumber.JOB;
    }
    
    @Override
    public void serializeNBT(CompoundTag tag)
    {
        this.serializeID(tag);
        Useful.saveEnumToTag(tag, NBT_REQUEST_PRIORITY, this.priority);
        tag.putString(NBT_REQUEST_USER_NAME, this.userName);
        Useful.saveEnumToTag(tag, NBT_REQUEST_STATUS, this.status);
        tag.putBoolean(NBT_JOB_HELD_FLAG, this.isHeld);
        tag.putInt(NBT_BUILD_ID, this.buildID);
        tag.putString(NBT_BUILD_DIMENSION_ID, this.dimensionID.toString());
        
        if(!this.tasks.isEmpty())
        {
            ListTag nbtTasks = new ListTag();
            
            for(AbstractTask t : this.tasks)
            {
                nbtTasks.add(TaskType.serializeTask(t));
            }
            tag.put(NBT_REQUEST_CHILDREN, nbtTasks);
        }
    }

    /**
     * Called by contained tasks when they have a status change.
     * Should NOT be called when task is start initialized.
     */
    public void notifyTaskStatusChange(AbstractTask abstractTask, RequestStatus priorStatus)
    {
        this.isTaskStatusDirty = true;
        
        if(priorStatus == RequestStatus.READY)
        {
            this.updateReadyWork(-1);
        }
        else if(abstractTask.getStatus() == RequestStatus.READY)
        {
            this.updateReadyWork(1);
        }
    }
    
    /**
     * Task iterator is read-only.
     */
    @Override
    public Iterator<AbstractTask> iterator()
    {
        return this.tasks.iterator();
    }

    @Override
    public int getIdRaw()
    {
        return this.id;
    }

    @Override
    public void setId(int id)
    {
        this.id = id;
    }

    /**
     * For use by job manager.
     */
    public RequestPriority effectivePriority()
    {
        return effectivePriority;
    }

    /**
     * Causes {@link #effectivePriority()} to match {@link #getPriority()}
     */
    public void updateEffectivePriority()
    {
        this.effectivePriority = this.getPriority();
    }
    
    /**
     * For use by job manager.
     */
    public RequestStatus effectiveStatus()
    {
        return effectiveStatus;
    }

    /**
     * Causes {@link #effectiveStatus()} to match {@link #getStatus()}
     */
    public void updateEffectiveStatus()
    {
        this.effectiveStatus = this.getStatus();
    }

    /**
     * True if this job has inactive tasks ready to execute
     * and the job should be included in the work backlog.
     */
    public boolean hasReadyWork()
    {
        return this.readyWorkCount > 0;
    }

    @Override
    public @Nullable IDomain getDomain()
    {
        return this.jobManager.getDomain();
    }

    public int getBuildID()
    {
        return buildID;
    }

    public void setBuildID(int buildID)
    {
        this.buildID = buildID;
        this.setDirty();
    }
    
    public Build getBuild()
    {
        return Build.buildFromId(buildID);
    }

    public Identifier getDimensionID()
    {
        return dimensionID;
    }

    public void setDirty()
    {
        // system jobs aren't serialized, no need to mark owner dirty
        if(!this.isSystemJob) this.jobManager.setDirty();
    }
    
    public void setDimension(Dimension dimension)
    {
        setDimensionID(DimensionType.getId(dimension.getType()));
    }
    
    public void setDimensionID(Identifier dimensionID)
    {
        this.dimensionID = dimensionID;
        this.world = null;
        this.setDirty();
    }
    
    public World world()
    {
        if(this.world == null)
        {
            this.world = Simulator.server().getWorld(DimensionType.byId(this.dimensionID));
        }
        return this.world;
    }

    public static Job jobFromId(int id)
    {
        return (Job)  Simulator.instance().assignedNumbersAuthority().get(id, AssignedNumber.JOB);
    }
}
