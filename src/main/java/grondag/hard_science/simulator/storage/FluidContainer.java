package grondag.hard_science.simulator.storage;

import javax.annotation.Nullable;

import grondag.hard_science.HardScience;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.FluidResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.transport.management.LogisticsService;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class FluidContainer extends ResourceContainer<StorageTypeFluid> implements IFluidHandler
{
    /**
     * Content key should be either at BulkBufferPurpose entry
     * or a specific fluid resource.  If it is specific fluid
     * resource, this container will only accept that fluid.
     */
    public FluidContainer(IDevice owner, ContainerUsage usage, int maxSlots)
    {
        super(StorageType.FLUID, owner, usage, maxSlots);
        this.setCapacity(VolumeUnits.liters2nL(32000));
    }

    private class TankProps implements IFluidTankProperties
    {
        private final FluidResource resource;
       
        private TankProps(FluidResource resource)
        {
            this.resource = resource;
        }

        @Override
        public @Nullable FluidStack getContents()
        {
            if(FluidContainer.this.usedCapacity() == 0) return null;
            
            FluidStack result = (this.resource).sampleFluidStack().copy();
            result.amount = (int) Math.min(
                    VolumeUnits.nL2Liters(FluidContainer.this.getQuantityStored(this.resource)),
                    Integer.MAX_VALUE);
            return result;
        }

        @Override
        public int getCapacity()
        {
            return (int) Math.min(
                    VolumeUnits.nL2Liters(FluidContainer.this.getCapacity()),
                    Integer.MAX_VALUE);
        }

        @Override
        public boolean canFill()
        {
            return this.resource == null
                   ? FluidContainer.this.availableCapacity() >= VolumeUnits.LITER.nL
                   : FluidContainer.this.availableCapacityFor(this.resource) >= VolumeUnits.LITER.nL;
        }

        @Override
        public boolean canDrain()
        {
            return this.resource == null
                    ? false
                    : FluidContainer.this.getQuantityStored(this.resource) >= VolumeUnits.LITER.nL;
        }

        @Override
        public boolean canFillFluidType(@Nullable FluidStack fluidStack)
        {
            // must have capacity for at least 1mb
            return FluidContainer.this.availableCapacityFor(FluidResource.fromStack(fluidStack)) >= VolumeUnits.LITER.nL;
        }

        @Override
        public boolean canDrainFluidType(@Nullable FluidStack fluidStack)
        {
            // must contain at least 1mb
            return FluidContainer.this.takeUpTo(
                    FluidResource.fromStack(fluidStack), 
                    VolumeUnits.LITER.nL, true) == VolumeUnits.LITER.nL; 
        }
    };
    
    @Override
    public IFluidTankProperties[] getTankProperties()
    {
        IFluidTankProperties[] result = new IFluidTankProperties[this.slots.size()];
        if(!this.slots.isEmpty())
        {
            int i = 0;
            for( AbstractResourceWithQuantity<StorageTypeFluid> s : this.slots)
            {
                result[i++] = new TankProps((FluidResource) s.resource());
            }
        }
        return result;
    }

    @Override
    public int fill(@Nullable FluidStack stack, boolean doFill)
    {
        if (stack == null || stack.amount <= 0)
        {
            return 0;
        }
        
        final FluidResource resourceIn = FluidResource.fromStack(stack);
        
        if(!this.isResourceAllowed(resourceIn)) return 0;
    
        try
        {
            return LogisticsService.FLUID_SERVICE.executor.submit( () ->
            {
                // Prevent fractional liters.
                long requested = VolumeUnits.liters2nL((long) VolumeUnits.nL2Liters(this.availableCapacity()));
                requested = Math.min(requested, VolumeUnits.liters2nL(stack.amount));
                long filled = this.add(resourceIn, requested, !doFill, null);
                
                return (int) VolumeUnits.nL2Liters(filled);
            }, true).get();
        }
        catch (Exception e)
        {
            HardScience.INSTANCE.error("Error in fluid handler", e);
            return 0;
        }
        
    }

    @Override
    public @Nullable FluidStack drain(@Nullable FluidStack stack, boolean doDrain)
    {
        if(stack == null) return null;
        
        final FluidResource resourceOut = FluidResource.fromStack(stack);
        
        if(FluidContainer.this.getQuantityStored(resourceOut) == 0) 
        {
            FluidStack result = stack.copy();
            result.amount = 0;
            return result;
        }
        
        try
        {
            return LogisticsService.FLUID_SERVICE.executor.submit( () ->
            {
                long drained = VolumeUnits.liters2nL(stack.amount);
                drained = this.takeUpTo(resourceOut, drained, !doDrain, null);
                return resourceOut.newStackWithLiters((int) VolumeUnits.nL2Liters(drained));
            }, true).get();
        }
        catch (Exception e)
        {
            HardScience.INSTANCE.error("Error in fluid handler", e);
            return null;
        }
    }

    @Override
    public @Nullable FluidStack drain(int maxDrain, boolean doDrain)
    {
        // if no stack offered, then use first stack in the storage
        if(this.slots.isEmpty()) return null;
        FluidResource resourceOut = (FluidResource) this.slots().get(0).resource();
        int mb = (int) Math.min(maxDrain, slots.getQuantity(resourceOut));
        return this.drain(resourceOut.newStackWithLiters(mb), doDrain);
    }
}
