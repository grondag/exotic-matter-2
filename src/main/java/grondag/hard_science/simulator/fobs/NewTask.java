package grondag.hard_science.simulator.fobs;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.serialization.IReadWriteNBT;
import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.exotic_matter.simulator.domain.IDomain;
import grondag.exotic_matter.simulator.domain.IDomainMember;
import grondag.exotic_matter.simulator.job.RequestStatus;
import grondag.exotic_matter.varia.Useful;
import grondag.exotic_matter.varia.structures.SimpleUnorderedArrayList;
import net.minecraft.nbt.NBTTagCompound;

//TODO: need to consolidate this with old task implementation or clean up

public abstract class NewTask implements IReadWriteNBT, IDomainMember //, IIdentified//, 
{
    private SimpleUnorderedArrayList<INewTaskListener> listeners;
//    private int id = IIdentified.UNASSIGNED_ID;
    private final ITaskContainer container;
    protected RequestStatus status = RequestStatus.NEW;
    
    protected NewTask(ITaskContainer container)
    {
        this.container = container;
    }
    
    public final synchronized void addListener(INewTaskListener listener)
    {
        if(this.listeners == null) this.listeners = new SimpleUnorderedArrayList<>();
        this.listeners.addIfNotPresent(listener);
    }
    
    public final synchronized void removeListener(INewTaskListener listener)
    {
        if(this.listeners != null) this.listeners.removeIfPresent(listener);
    }

    protected Iterable<INewTaskListener>listeners()
    {
        return this.listeners == null ? ImmutableList.of() : this.listeners;
    }
    
    protected void notifyStatusChange(RequestStatus oldStatus)
    {
        this.listeners().forEach(l -> l.notifyStatusChange(this, oldStatus));
    }
    
    public TaskPriority priority()
    {
        return this.container.priority();
    }
    
    public RequestStatus status()
    {
        return this.status;
    }
    
//    @Override
//    public int getIdRaw()
//    {
//        return this.id;
//    }
//
//    @Override
//    public int getId()
//    {
//        // necessary to disambiguate
//        return IIdentified.super.getId();
//    }
//    
//    @Override
//    public void setId(int id)
//    {
//        this.id = id;
//    }
//    @Override
//    public AssignedNumber idType()
//    {
//        return AssignedNumber.TASK;
//    }
    
    private final static String NBT_TASK_STATUS = NBTDictionary.claim("taskStatus");
    
    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag)
    {
//        this.deserializeID(tag);
        this.status = Useful.safeEnumFromTag(tag, NBT_TASK_STATUS, RequestStatus.NEW);
    }

    @Override
    public synchronized void serializeNBT(NBTTagCompound tag)
    {
//        this.serializeID(tag);
        Useful.saveEnumToTag(tag, NBT_TASK_STATUS, this.status);
    }
    
    public void cancel()
    {
        assert !this.status.isTerminated 
        : "AbstractTask.cancel called on task with terminal status";

        RequestStatus oldStatus = this.status;
        this.status = RequestStatus.CANCELLED;
        this.notifyStatusChange(oldStatus);
    }

    @Override
    public @Nullable IDomain getDomain()
    {
        return this.container.getDomain();
    }
    
    
}
