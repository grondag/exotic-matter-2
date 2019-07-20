package grondag.hard_science.machines.matbuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.exotic_matter.serialization.IReadWriteNBT;
import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.exotic_matter.simulator.ISimulationTickable;
import grondag.hard_science.machines.support.ThroughputRegulator;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.device.IDeviceComponent;
import grondag.hard_science.simulator.resource.IResourcePredicate;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.ContainerUsage;
import grondag.hard_science.simulator.storage.FluidContainer;
import grondag.hard_science.simulator.storage.ItemContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

public class BufferManager implements IReadWriteNBT, IItemHandler, IDeviceComponent, ISimulationTickable
{
    private static final String NBT_BUFFER_ITEMS_IN = NBTDictionary.claim("buffItemsIn");
    private static final String NBT_BUFFER_ITEMS_OUT = NBTDictionary.claim("buffItemsOut");
    private static final String NBT_BUFFER_FLUIDS_IN = NBTDictionary.claim("buffFluidsIn");
    private static final String NBT_BUFFER_FLUIDS_OUT = NBTDictionary.claim("buffFluidsOut");
    
    private final IDevice owner;
    private final @Nullable ItemContainer itemInput;
    private final @Nullable ItemContainer itemOutput;
    private final @Nullable FluidContainer fluidInput;
    private final @Nullable FluidContainer fluidOutput;
    
    public BufferManager(
            IDevice owner,
            long itemInputSize,
            IResourcePredicate<StorageTypeStack> itemInputPredicate,
            long itemOutputSize,
            long fluidInputSize,
            IResourcePredicate<StorageTypeFluid> fluidInputPredicate,
            long fluidOutputSize
            )
    {
        this.owner = owner;
        
        if(itemInputSize == 0)
        {
            this.itemInput = null;
        }
        else
        {
            this.itemInput = new ItemContainer(owner, ContainerUsage.PRIVATE_BUFFER_IN, Integer.MAX_VALUE);
            this.itemInput.setCapacity(itemInputSize);
            this.itemInput.setContentPredicate(itemInputPredicate);
            this.itemInput.setRegulator(new ThroughputRegulator.Tracking<>());
        }
        
        if(itemOutputSize == 0)
        {
            this.itemOutput = null;
        }
        else
        {
            this.itemOutput = new ItemContainer(owner, ContainerUsage.PUBLIC_BUFFER_OUT, Integer.MAX_VALUE);
            this.itemOutput.setCapacity(itemOutputSize);
            this.itemOutput.setRegulator(new ThroughputRegulator.Tracking<>());
        }
        
        if(fluidInputSize == 0)
        {
            this.fluidInput = null;
        }
        else
        {
            this.fluidInput = new FluidContainer(owner, ContainerUsage.PRIVATE_BUFFER_IN, Integer.MAX_VALUE);
            this.fluidInput.setCapacity(fluidInputSize);
            this.fluidInput.setContentPredicate(fluidInputPredicate);
            this.fluidInput.setRegulator(new ThroughputRegulator.Tracking<>());
        }
        
        if(fluidOutputSize == 0)
        {
            this.fluidOutput = null;
        }
        else
        {
            this.fluidOutput = new FluidContainer(owner, ContainerUsage.PUBLIC_BUFFER_OUT, Integer.MAX_VALUE);
            this.fluidOutput.setCapacity(fluidOutputSize);
            this.fluidOutput.setRegulator(new ThroughputRegulator.Tracking<>());
        }
    }

    @Nullable 
    public ItemContainer itemInput() 
    {
        return this.itemInput;
    }
    
    @Nullable 
    public ItemContainer itemOutput()
    {
        return this.itemOutput;
    }
    
    @Nullable 
    public FluidContainer fluidInput()
    {
        return this.fluidInput;
    }
    
    @Nullable 
    public FluidContainer fluidOutput()
    {
        return this.fluidOutput;
    }
    
    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag)
    {
        if(this.itemInput != null && tag.hasKey(NBT_BUFFER_ITEMS_IN))
            this.itemInput.deserializeNBT(tag.getCompoundTag(NBT_BUFFER_ITEMS_IN));

        if(this.itemOutput != null && tag.hasKey(NBT_BUFFER_ITEMS_OUT))
            this.itemOutput.deserializeNBT(tag.getCompoundTag(NBT_BUFFER_ITEMS_OUT));

        if(this.fluidInput != null && tag.hasKey(NBT_BUFFER_FLUIDS_IN))
            this.fluidInput.deserializeNBT(tag.getCompoundTag(NBT_BUFFER_FLUIDS_IN));

        if(this.fluidOutput != null && tag.hasKey(NBT_BUFFER_FLUIDS_OUT))
            this.fluidOutput.deserializeNBT(tag.getCompoundTag(NBT_BUFFER_FLUIDS_OUT));
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        if(this.itemInput != null)
            tag.setTag(NBT_BUFFER_ITEMS_IN, this.itemInput.serializeNBT());
        if(this.itemOutput != null)
            tag.setTag(NBT_BUFFER_ITEMS_OUT, this.itemOutput.serializeNBT());
        if(this.fluidInput != null)
            tag.setTag(NBT_BUFFER_FLUIDS_IN, this.fluidInput.serializeNBT());
        if(this.fluidOutput != null)
            tag.setTag(NBT_BUFFER_FLUIDS_OUT, this.fluidOutput.serializeNBT());
    }

    @Override
    public int getSlots()
    {
        return this.inSlots() + this.outSlots();
    }

    private int inSlots()
    {
        return this.itemInput == null ? 0 : this.itemInput.getSlots();
    }
    
    private int outSlots()
    {
        return this.itemOutput == null ? 0 : this.itemOutput.getSlots();
    }
    
    @Override
    public ItemStack getStackInSlot(int slot)
    {
        int inLength = inSlots();
        int outLength = outSlots();
        
        if(slot < inLength)
        {
            return this.itemInput.getStackInSlot(slot);
        }
        else if(slot < inLength + outLength)
        {
            return this.itemOutput.getStackInSlot(slot - inLength);
        }
        else return ItemStack.EMPTY;
    }

    @Override
    @Nullable
    public ItemStack insertItem(int slot, @SuppressWarnings("null") @Nonnull ItemStack stack, boolean simulate)
    {
        if(this.itemInput != null && slot < inSlots())
        {
            return this.itemInput.insertItem(slot, stack, simulate);
        }
        else 
        {
            return stack;
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if(this.itemOutput == null) return ItemStack.EMPTY;
        
        int inLength = inSlots();
        if(slot >= inLength)
        {
            return this.itemOutput.extractItem(slot - inLength, amount, simulate);
        }
        else return ItemStack.EMPTY;    
    }

    @Override
    public int getSlotLimit(int slot)
    {
        int inLength = inSlots();
        int outLength = outSlots();
        
        if(slot < inLength)
        {
            return this.itemInput.getSlotLimit(slot);
        }
        else if(slot < inLength + outLength)
        {
            return this.itemOutput.getSlotLimit(slot - inLength);
        }
        else return 0;
    }

    @Override
    public void onConnect()
    {
        IDeviceComponent.super.onConnect();
        if(this.itemInput != null) this.itemInput.onConnect();
        if(this.itemOutput != null) this.itemOutput.onConnect();
        if(this.fluidInput != null) this.fluidInput.onConnect();
        if(this.fluidOutput != null) this.fluidOutput.onConnect();
    }

    @Override
    public void onDisconnect()
    {
        IDeviceComponent.super.onDisconnect();
        if(this.itemInput != null) this.itemInput.onDisconnect();
        if(this.itemOutput != null) this.itemOutput.onDisconnect();
        if(this.fluidInput != null) this.fluidInput.onDisconnect();
        if(this.fluidOutput != null) this.fluidOutput.onDisconnect();
    }

    @Nullable
    public FluidContainer bufferHDPE()
    {
        //TODO stub
        return null;
    }

    @Override
    public IDevice device()
    {
        return this.owner;
    }

    /**
     * True if actually has buffers
     */
    public boolean isReal()
    {
        return this.itemInput != null || this.itemOutput != null || this.fluidInput != null || this.fluidInput != null;
    }
}
