package grondag.hard_science.simulator.resource;

import java.util.Comparator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

public class FluidResourceWithQuantity extends AbstractResourceWithQuantity<StorageType.StorageTypeFluid>
{

    public FluidResourceWithQuantity(FluidResource resource, long nanoLiters)
    {
        super(resource, nanoLiters);
    }
    
    public FluidResourceWithQuantity()
    {
        super();
    }
    
    public FluidResourceWithQuantity(NBTTagCompound tag)
    {
        super(tag);
    }
    
    public static FluidResourceWithQuantity fromStack(FluidStack stack)
    {
        if(stack == null || stack.amount == 0) 
            return (FluidResourceWithQuantity) StorageType.FLUID.emptyResource.withQuantity(0);
        
        return new FluidResourceWithQuantity(
                FluidResource.fromStack(stack),
                VolumeUnits.liters2nL(stack.amount));
    }
    
    @Override
    public StorageTypeFluid storageType()
    {
        return StorageType.FLUID;
    }
    
    public static final Comparator<? super AbstractResourceWithQuantity<StorageTypeFluid>> 
        SORT_BY_QTY_ASC = new Comparator<AbstractResourceWithQuantity<StorageTypeFluid>>()
    {
        @Override
        public int compare(@Nullable AbstractResourceWithQuantity<StorageTypeFluid> o1, @Nullable AbstractResourceWithQuantity<StorageTypeFluid> o2)
        {  
            if(o1 == null)
            {
                if(o2 == null) 
                {
                    return 0;
                }
                return  1;
            }
            else if(o2 == null) 
            {
                return -1;
            }
            int result = Long.compare(o1.quantity, o2.quantity);
            return result == 0 ? o1.resource().displayName().compareTo(o2.resource().displayName()) : result;
        }
    };
    
    public static final Comparator<? super AbstractResourceWithQuantity<StorageTypeFluid>> 
        SORT_BY_QTY_DESC = new Comparator<AbstractResourceWithQuantity<StorageTypeFluid>>()
    {
        @Override
        public int compare(@Nullable AbstractResourceWithQuantity<StorageTypeFluid> o1, @Nullable AbstractResourceWithQuantity<StorageTypeFluid> o2)
        {
            return SORT_BY_QTY_ASC.compare(o2, o1);
        }
        
    };
    
    @Override
    public String toString()
    {
        return String.format("%s x %,dL", this.resource().toString(), VolumeUnits.nL2Liters(this.getQuantity()));
    }
}
