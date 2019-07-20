package grondag.hard_science.simulator.demand;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.annotation.Nullable;

import grondag.exotic_matter.simulator.job.RequestStatus;
import grondag.fermion.structures.SimpleUnorderedArrayList;
import grondag.hard_science.simulator.fobs.NewProcurementTask;
import grondag.hard_science.simulator.fobs.NewTask;
import grondag.hard_science.simulator.fobs.TaskPriority;
import grondag.hard_science.simulator.resource.StorageType;

public class AbstractBroker<V extends StorageType<V>> implements IBroker<V>
{
    protected final BrokerManager brokerManager;
    
    protected SimpleUnorderedArrayList<IProducer<V>> producers = new SimpleUnorderedArrayList<IProducer<V>>();
    protected ConcurrentSkipListSet<NewProcurementTask<V>> requests = new ConcurrentSkipListSet<NewProcurementTask<V>>(new Comparator<NewProcurementTask<V>>() 
            {
                @Override
                public int compare(@Nullable NewProcurementTask<V> o1, @Nullable NewProcurementTask<V> o2)
                {
                    return o1.priority().compareTo(o2.priority());
                }
            });
    
    private Set<NewProcurementTask<V>> requestsReadOnly = Collections.unmodifiableSet(this.requests);

    public AbstractBroker(BrokerManager brokerManager)
    {
        super();
        this.brokerManager = brokerManager;
    }

    @Override
    public synchronized void registerRequest(NewProcurementTask<V> request)
    {
        if(this.requests.add(request))
        {
            this.notifyProducersOfDemandChange(request);
            request.addListener(this);
        }
    }

    @Override
    public synchronized void unregisterRequest(NewProcurementTask<V> request)
    {
        request.removeListener(this);
        this.requests.remove(request);
    }

    @Override
    public void notifyStatusChange(NewTask task, RequestStatus oldStatus)
    {
        if(task.status().isTerminated)
        {
            task.removeListener(this);
            this.requests.remove(task);
        }
    }

    /**
     * Called by request if new demands are added, perhaps because
     * WIP was cancelled. Signals broker to wake up any producers
     * that have given up on existing demands.<p>
     * 
     * {@inheritDoc}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public synchronized void notifyDemandChange(NewTask task)
    {
        if(task instanceof NewProcurementTask)
        {
            this.notifyProducersOfDemandChange((NewProcurementTask)task);
        }
    }

    protected void notifyProducersOfDemandChange(NewProcurementTask<V> task)
    {
        for(IProducer<V> p : this.producers)
        {
            p.notifyNewDemand(this, task);
        }
    }
    /**
     * Called by request if priority changes but should still be 
     * tracked by this broker. <p>
     * 
     *  {@inheritDoc}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public synchronized void notifyPriorityChange(NewTask task, TaskPriority oldPriority)
    {
        // remove and re-add to update sort
        this.requests.remove(task);
        if(task instanceof NewProcurementTask)
        {
            this.requests.add((NewProcurementTask)task);    
        }
    }

    @Override
    public Collection<NewProcurementTask<V>> openRequests()
    {
        return this.requestsReadOnly;
    }

    @Override
    public synchronized void registerProducer(IProducer<V> producer)
    {
        this.producers.addIfNotPresent(producer);
    }

    @Override
    public synchronized void unregisterProducer(IProducer<V> producer)
    {
        this.producers.removeIfPresent(producer);
    }
}