package grondag.hard_science.simulator.jobs;

import javax.annotation.Nullable;

import grondag.exotic_matter.simulator.job.RequestPriority;
import grondag.exotic_matter.simulator.job.RequestStatus;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Job that eats all method calls.
 */
public class NullJob extends Job
{
    public static final NullJob INSTANCE = new NullJob();
    
    private NullJob() {}
    
    @Override
    public RequestPriority getPriority()
    {
        return RequestPriority.MINIMAL;
    }

    @Override
    public void setPriority(RequestPriority priority)
    {
        // NOOP
    }

    @Override
    public String userName()
    {
        return "NONE";
    }

    @Override
    protected void setStatus(RequestStatus newStatus)
    {
        // NOOP
    }

    @Override
    public RequestStatus getStatus()
    {
        return RequestStatus.COMPLETE;
    }

    @Override
    public void cancel()
    {
        // NOOP
    }

    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag)
    {
        // NOOP
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        // NOOP
    }

    @Override
    public void notifyTaskStatusChange(AbstractTask abstractTask, RequestStatus priorStatus)
    {
        // NOOP
    }

    @Override
    public int getIdRaw()
    {
        return 0;
    }

    @Override
    public void setId(int id)
    {
        // NOOP
    }

    @Override
    public RequestPriority effectivePriority()
    {
        return RequestPriority.MINIMAL;
    }

    @Override
    public void updateEffectivePriority()
    {
        // NOOP
    }

    @Override
    public RequestStatus effectiveStatus()
    {
        return RequestStatus.COMPLETE;
    }

    @Override
    public void updateEffectiveStatus()
    {
        // NOOP
    }

    @Override
    public boolean hasReadyWork()
    {
        return false;
    }

}
