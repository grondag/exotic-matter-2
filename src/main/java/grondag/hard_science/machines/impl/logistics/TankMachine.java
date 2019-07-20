package grondag.hard_science.machines.impl.logistics;

import javax.annotation.Nullable;

import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.resource.IResourcePredicate;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.storage.ContainerUsage;
import grondag.hard_science.simulator.storage.FluidContainer;
import net.minecraft.nbt.NBTTagCompound;

public abstract class TankMachine extends AbstractSimpleMachine
{
    protected final FluidContainer fluidStorage;
    
    protected TankMachine()
    {
        super();
        this.fluidStorage = new FluidContainer(this, ContainerUsage.STORAGE, 
                this.dedicated() ? 1 : Integer.MAX_VALUE);
    }
    
    protected abstract boolean dedicated();
    
    public void setContentPredicate(IResourcePredicate<StorageTypeFluid> predicate)
    {
        this.fluidStorage.setContentPredicate(predicate);
    }
    
    /**
     * 1 block = 1 MC bucket = 1 kiloliter
     */
    public void setCapacityInBlocks(int blocks)
    {
        this.fluidStorage.setCapacity(VolumeUnits.blocks2nL(blocks));
    }
    
    @Override
    public boolean hasOnOff()
    {
        return true;
    }

    @Override
    public boolean hasRedstoneControl()
    {
        return false;
    }

    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag)
    {
        super.deserializeNBT(tag);
        this.fluidStorage.deserializeNBT(tag);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        super.serializeNBT(tag);
        this.fluidStorage.serializeNBT(tag);
    }

    @Override
    public void onConnect()
    {
        super.onConnect();
        this.fluidStorage.onConnect();
    }

    @Override
    public void onDisconnect()
    {
        this.fluidStorage.onDisconnect();
        super.onDisconnect();
    }
    
    @Override
    public FluidContainer fluidStorage()
    {
        return this.fluidStorage;
    }
    
    public static class Flexible extends TankMachine
    {
        @Override
        protected boolean dedicated() { return false; }
    }
    
    public static class Dedicated extends TankMachine
    {
        @Override
        protected boolean dedicated() { return true; }
    }
}
