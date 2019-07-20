package grondag.hard_science.simulator.resource;


import javax.annotation.Nullable;

import com.google.common.base.Objects;

import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * Identifier for resources.
 * Instances with same inputs will have same hashCode and return true for equals().
 * Moreover, due to caching, instances with same inputs should always be the same instance.
 */
public class FluidResource extends AbstractResource<StorageType.StorageTypeFluid>
{
    private Fluid fluid;
    private NBTTagCompound tag;
    private int hash = -1;
    
    // lazy instantiate and cache
    private FluidStack stack = null;
    
    public FluidResource(Fluid fluid, NBTTagCompound tag)
    {
        this.fluid = fluid;
        this.tag = tag;
    }
    
    public static FluidResource fromStack(FluidStack stack)
    {
        if(stack == null) return (FluidResource) StorageType.FLUID.emptyResource;
        return new FluidResource(stack.getFluid(), stack.tag);
    }
    
    /**
     * Returns a new stack containing one of this item.
     * Will always be a new instance/copy.     */
    public FluidStack sampleFluidStack()
    {
        if(this.fluid == null) return null;
        
        if(this.stack == null)
        {
            stack = new FluidStack(fluid, Fluid.BUCKET_VOLUME);
            stack.tag = this.tag;
        }
        return stack.copy();
    }
    
    public FluidStack newStackWithLiters(int liters)
    {
        if(this.fluid == null) return null;
        
        FluidStack result = new FluidStack(this.fluid, Fluid.BUCKET_VOLUME);
        result.tag = this.tag;
        result.amount = liters;
        return result;
    }
    
    public Fluid getFluid()
    {
        return this.fluid;
    }

    public boolean hasTagCompound()
    {
        return this.tag != null;
    }
    
    @Nullable
    public NBTTagCompound getTagCompound()
    {
        return this.tag;
    }
   
    public boolean isStackEqual(FluidStack stack)
    {
        if(stack == null) return this == StorageType.FLUID.emptyResource;
        
        return stack.getFluid() == this.fluid
            && Objects.equal(stack.tag, this.tag);
    }
    
    @Override
    public String displayName()
    {
        return this.stack == null 
                ? this.sampleFluidStack().getLocalizedName()
                : this.stack.getLocalizedName();
    }

    @Override
    public FluidResourceWithQuantity withQuantity(long quantity)
    {
        return new FluidResourceWithQuantity(this, quantity);
    }
    
    @Override
    public String toString()
    {
        return this.displayName();
    }

    @Override
    public int hashCode()
    {
        int h = this.hash;
        if(h == -1)
        {
            h = this.fluid == null ? 0 : this.fluid.hashCode();
            
            if(this.tag != null)
            {
                h = h * 7919 + this.tag.hashCode();
            }
            
            this.hash = h;
        }
        return h;
    }

  
    @Override
    public boolean isResourceEqual(@Nullable IResource<?> other)
    {
        if(other == this) return true;
        if(other == null) return false;
        if(other instanceof FluidResource)
        {
            FluidResource resource = (FluidResource)other;
            return resource.fluid == this.fluid
                    && Objects.equal(resource.tag, this.tag);
        }
        return false;
    }

    @Override
    public StorageTypeFluid storageType()
    {
        return StorageType.FLUID;
    }
}
