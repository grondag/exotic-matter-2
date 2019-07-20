package grondag.hard_science.simulator.fobs;

import javax.annotation.Nullable;

import grondag.exotic_matter.simulator.domain.IDomain;
import grondag.exotic_matter.simulator.domain.IDomainCapability;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Container for classes that aren't serialized or prioritized.
 * 
 * This container does not keep references to its tasks and
 * provides no way to retrieve tasks associated with it.
 */
public class TransientTaskContainer implements ITaskContainer, IDomainCapability
{
    private IDomain domain;

    @Override
    public @Nullable IDomain getDomain()
    {
        return this.domain;
    }

    @Override
    public TaskPriority priority()
    {
        return TaskPriority.NONE;
    }

    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag)
    {
        //NOOP - no persisted state
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        //NOOP - no persisted state
    }

    @Override
    public void setDirty()
    {
        //NOOP - no persisted state
    }

    @Override
    public String tagName()
    {
        assert false : "Attempt to serialize Transient Task Container";
        return "";
    }

    @Override
    public void setDomain(IDomain domain)
    {
        this.domain = domain;
    }

    @Override
    public boolean isSerializationDisabled()
    {
        return true;
    }
}
