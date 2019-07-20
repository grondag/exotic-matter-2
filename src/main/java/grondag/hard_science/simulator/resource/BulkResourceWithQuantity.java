package grondag.hard_science.simulator.resource;

import java.util.Comparator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.matter.MassUnits;
import grondag.hard_science.matter.MatterPhase;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeBulk;
import net.minecraft.nbt.NBTTagCompound;

public class BulkResourceWithQuantity extends AbstractResourceWithQuantity<StorageTypeBulk>
{

    public BulkResourceWithQuantity(BulkResource resource, long nanoLiters)
    {
        super(resource, nanoLiters);
    }
    
    public BulkResourceWithQuantity()
    {
        super();
    }
    
    public BulkResourceWithQuantity(NBTTagCompound tag)
    {
        super(tag);
    }
    
    /**
     * Is nanoliters for bulk resources.
     */
    @Override
    public long getQuantity()
    {
        return super.getQuantity();
    }

    @Override
    public StorageTypeBulk storageType()
    {
        return StorageType.PRIVATE;
    }
    
    public static final Comparator<? super AbstractResourceWithQuantity<StorageTypeBulk>> 
        SORT_BY_QTY_ASC = new Comparator<AbstractResourceWithQuantity<StorageTypeBulk>>()
    {
        @Override
        public int compare(@Nullable AbstractResourceWithQuantity<StorageTypeBulk> o1, @Nullable AbstractResourceWithQuantity<StorageTypeBulk> o2)
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
    
    public static final Comparator<? super AbstractResourceWithQuantity<StorageTypeBulk>> 
        SORT_BY_QTY_DESC = new Comparator<AbstractResourceWithQuantity<StorageTypeBulk>>()
    {
        @Override
        public int compare(@Nullable AbstractResourceWithQuantity<StorageTypeBulk> o1, @Nullable AbstractResourceWithQuantity<StorageTypeBulk> o2)
        {
            return SORT_BY_QTY_ASC.compare(o2, o1);
        }
        
    };
    
    
    public String systemName()
    {
        return ((BulkResource)this.resource()).systemName();
    }
    
    @Override
    public String toString()
    {
        return this.resource().displayName() 
                + ", "
                + this.quantityLabel();
    }
    
    public String quantityLabel()
    {
        BulkResource r = (BulkResource) this.resource();
        return r.phase() == MatterPhase.SOLID
                // x1000 because 1L = 1000g at density = 1.0
                ? MassUnits.formatMass((long) (this.quantity * r.density() * 1000), false)
                : VolumeUnits.formatVolume(this.quantity, false);
    }
}
