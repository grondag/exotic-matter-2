package grondag.hard_science.machines.impl.logistics;

import javax.annotation.Nullable;

import grondag.hard_science.init.ModItems;
import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.storage.ContainerUsage;
import grondag.hard_science.simulator.storage.ItemContainer;
import net.minecraft.nbt.NBTTagCompound;

public abstract class SmartChestMachine extends AbstractSimpleMachine
{
    protected final ItemContainer itemStorage;
    
    protected SmartChestMachine()
    {
        super();
        this.itemStorage = new ItemContainer(this, ContainerUsage.STORAGE, 
                this.dedicated() ? 1 : Integer.MAX_VALUE);
        this.itemStorage.setContentPredicate( r -> 
        {
            if(r == null || !(r instanceof ItemResource)) return false;
            
            ItemResource ir = (ItemResource)r;
            
            return !ir.hasTagCompound() ||
                    !(
                        ir.getItem() == ModItems.smart_chest
                        || ir.getItem() == ModItems.smart_bin
                    );
        });
               
        this.itemStorage.setCapacity(this.dedicated() ? 4000 : 2000);
    }

    protected abstract boolean dedicated();
    
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
        this.itemStorage.deserializeNBT(tag);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        super.serializeNBT(tag);
        this.itemStorage.serializeNBT(tag);
    }

    @Override
    public void onConnect()
    {
        super.onConnect();
        this.itemStorage.onConnect();
    }

    @Override
    public void onDisconnect()
    {
        this.itemStorage.onDisconnect();
        super.onDisconnect();
    }
    
    @Override
    public ItemContainer itemStorage()
    {
        return this.itemStorage;
    }
    
    public static class Flexible extends SmartChestMachine
    {
        @Override
        protected boolean dedicated() { return false; }
    }
    
    public static class Dedicated extends SmartChestMachine
    {
        @Override
        protected boolean dedicated() { return true; }
    }
}
