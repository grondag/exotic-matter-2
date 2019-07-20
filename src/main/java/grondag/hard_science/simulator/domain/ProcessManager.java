package grondag.hard_science.simulator.domain;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.exotic_matter.simulator.domain.IDomain;
import grondag.exotic_matter.simulator.domain.IDomainCapability;
import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.machines.impl.processing.MicronizerInputSelector;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.FluidResource;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.FluidStorageManager;
import grondag.hard_science.simulator.storage.ItemStorageManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreIngredient;

public class ProcessManager implements IDomainCapability
{
    
    private static final String NBT_SELF = NBTDictionary.claim("processMgr");
    private static final String NBT_FLUID_SETTINGS = NBTDictionary.claim("processFluids");
    private static final String NBT_INGREDIENT_SETTINGS = NBTDictionary.claim("processIngredients");
    private static final String NBT_TARGET_LEVEL= NBTDictionary.claim("targetLevel");
    private static final String NBT_RESERVE_LEVEL= NBTDictionary.claim("reserveLevel");
    private static final String NBT_PROCESS_INGREDIENT = NBTDictionary.claim("procIng");
    
    private IDomain domain;
    
    private final HashMap<IResource<StorageTypeFluid>, FluidProcessInfo> fluidInfos = new HashMap<>();
    private final HashMap<String, IngredientProcessInfo> ingredientInfos = new HashMap<>();
    
    public final MicronizerInputSelector micronizerInputSelector;
    
    public abstract class ProcessInfo
    {
        protected long reserveStockLevel;
        protected long targetStockLevel;
        
        public long reserveStockLevel() { return this.reserveStockLevel; }
        public long targetStockLevel() { return this.targetStockLevel; }
        
        protected ProcessInfo() {};
        
        protected ProcessInfo(NBTTagCompound tag)
        {
            this.targetStockLevel = tag.getLong(NBT_TARGET_LEVEL);
            this.reserveStockLevel = tag.getLong(NBT_RESERVE_LEVEL);
            this.normalize();
        }
        
        protected void writeNBT(NBTTagCompound tag)
        {
            tag.setLong(NBT_TARGET_LEVEL, targetStockLevel);
            tag.setLong(NBT_RESERVE_LEVEL, reserveStockLevel);
        }
        
        /**
         * Scrubs bad/nonesense levels
         */
        protected void normalize()
        {
            if(this.reserveStockLevel < 0) this.reserveStockLevel = 0;
            
            if(this.targetStockLevel < this.reserveStockLevel)
                this.targetStockLevel = this.reserveStockLevel;
        }
        
        /** target less reserve, can be zero */
        public long stockIntervalSize() { return this.targetStockLevel - this.reserveStockLevel; }
        
        /**
         * Returns the difference between resource level and target stocking level.
         * Value is not fully reliable because not limited to storage service thread.
         * Returns 0 if at or above target stock level.
         */
        public long demand()
        {
            if(this.targetStockLevel <= 0) return 0;
            return Math.max(0, this.targetStockLevel - this.onHand()); 
        }
        
        /**
         * Returns estimate of current available inventory.
         */
        public abstract long onHand();
        
        /**
         * Returns 0-1 indicator of demand level, meanings as follows:<br>
         * 0 to 0.5    Target demand<br>
         * 0.5 to 1.0  Reserve demand<p>
         * 
         * Examples:<br>
         * 1    Nothing on hand, reserve and target levels defined<br>
         * 0.75    Half of reserve level met, none of target level satisfied<br>
         * 0.5 Reserve level met (or no reserve), but none of target level met<br>
         * 0.25    Reserve level met (or no reserve), half of target level met<br>
         * 0   All levels met, and/or no level(s) defined<br>
         */
        public float demandFactor()
        {
            if(this.targetStockLevel == 0) return 0;
            
            float result = 0;
            long onHand = this.onHand();
            
            if(onHand < this.reserveStockLevel)
            {
                result = 0.5f + 0.5f * (1f - (float)onHand / this.reserveStockLevel);
            }
            else if(onHand < this.targetStockLevel)
            {
                // note that logically target != reserve if we get here
                if(onHand < targetStockLevel)
                {
                    float coverage = onHand - this.reserveStockLevel;
                    float span = this.targetStockLevel - this.reserveStockLevel;
                    
                    result = 0.5f * (1f - coverage/span);
                }
            }
            return result;
        }
        
        /**
         * Returns 0+ indicator of availability, meanings as follows:<br>
         * 0  On hand is at or below reserve stocking level<br>
         * 0 to 1.0  On hand it between reserve stocking level and target stocking level<p>
         * 2+ On hand is above target stocking level. 2 mean 1 above, 3 is 2 above, etc.
         */
        public float availabilityFactor()
        {
            long onHand = this.onHand();
            
            if(onHand <= this.reserveStockLevel) return 0;
            
            if(onHand > this.targetStockLevel) return onHand - this.targetStockLevel + 1;
            
            // note that logically reserve and target must 
            // be different if we get to this point
            return (onHand - this.reserveStockLevel) / (float) this.stockIntervalSize();
            
        }
        
        public void setReserveStockLevel(long level)
        {
            this.reserveStockLevel = level;
            this.normalize();
            domain.setDirty();
        }
        
        public long getReserveStockLevel()
        {
            return this.reserveStockLevel;
        }
        
        public void setTargetStockLevel(long level)
        {
            this.targetStockLevel = level;
            this.normalize();
            domain.setDirty();
        }
        
        public long getTargetStockLevel()
        {
            return this.targetStockLevel;
        }

    }
    
    public class FluidProcessInfo extends ProcessInfo
    {
        private final IResource<StorageTypeFluid> resource;
        
        private FluidProcessInfo(IResource<StorageTypeFluid> resource)
        {
            super();
            this.resource = resource;
        }
        
        private FluidProcessInfo(NBTTagCompound tag)
        {
            super(tag);
            this.resource = StorageType.FLUID.fromNBT(tag);
        }
        
        private NBTTagCompound toNBT()
        {
            NBTTagCompound result = StorageType.FLUID.toNBT(resource);
            super.writeNBT(result);
            return result;
        }
        
        public IResource<StorageTypeFluid> resource() { return this.resource; }
        
        @Override
        public long onHand()
        {
            return domain.getCapability(FluidStorageManager.class).getEstimatedAvailable(resource);
        }
    }
    
    public class IngredientProcessInfo extends ProcessInfo
    {
        private final Ingredient ingredient;
        private final String ingString;
        
        private IngredientProcessInfo(String ingString)
        {
            super();
            this.ingredient = readIngredient(ingString);
            this.ingString = ingString;
        }
        
        private IngredientProcessInfo(NBTTagCompound tag)
        {
            super(tag);
            this.ingString = tag.getString(NBT_PROCESS_INGREDIENT);
            this.ingredient = readIngredient(ingString);
        }
        
        private NBTTagCompound toNBT()
        {
            NBTTagCompound result = new NBTTagCompound();
            result.setString(NBT_PROCESS_INGREDIENT, this.ingString);
            super.writeNBT(result);
            return result;
        }
        
        public Ingredient ingredient() { return this.ingredient; }
        
        
        @Override
        public long onHand()
        {
            List<AbstractResourceWithQuantity<StorageTypeStack>> stocked 
                = domain.getCapability(ItemStorageManager.class).findEstimatedAvailable(this.ingredient);
            
            long avail = 0;
            
            if(!stocked.isEmpty())
            {
                for(AbstractResourceWithQuantity<StorageTypeStack> rwq : stocked)
                {
                    avail += rwq.getQuantity();
                }
            }
            return avail; 
        }
    }
    
    private FluidProcessInfo readFluidCSV(String csv)
    {
        try
        {
            String args[] = csv.split(",");
            if(args.length == 3)
            {
                Fluid fluid = FluidRegistry.getFluid(args[0].trim());
                IResource<StorageTypeFluid> res = new FluidResource(fluid, null);
                FluidProcessInfo result = new FluidProcessInfo(res);
                result.reserveStockLevel = VolumeUnits.liters2nL(Long.parseLong(args[1].trim()));
                result.targetStockLevel = VolumeUnits.liters2nL(Long.parseLong(args[2].trim()));
                
                if(result.targetStockLevel < result.reserveStockLevel)
                    result.targetStockLevel = result.reserveStockLevel;
                return result;
            }
        }
        catch(Exception e)
        {
            HardScience.INSTANCE.error("Unable to parse fluid resource processing configuration", e);
        }
        return null;
    }
    
    public static Ingredient readIngredient(String ingString)
    {
        String itemArgs[] = ingString.split(":");
        
        if(itemArgs.length < 2) return null;
        
        if(itemArgs[0].equals("ore"))
        {
            // oredict
            return new OreIngredient(itemArgs[1]);
        }
        else
        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemArgs[0], itemArgs[1]));
            if(item == null ) return Ingredient.EMPTY;
            
            if(itemArgs.length == 3)
            {
                // metadata provided
                return Ingredient.fromStacks(new ItemStack(
                      item, 1, 
                      Integer.parseInt(itemArgs[2])));
            }
            else
            {
                // no metadata
                return Ingredient.fromItem(item);
            }
        }
    }
    
    private IngredientProcessInfo readItemCSV(String csv)
    {
        try
        {
            String args[] = csv.split(",");
            if(args.length == 3)
            {
                Ingredient ing = readIngredient(args[0].trim());
                if(ing == null || ing == Ingredient.EMPTY) return null;
                
                IngredientProcessInfo result = new IngredientProcessInfo(args[0]);
                result.reserveStockLevel = Long.parseLong(args[1].trim());
                result.targetStockLevel = Long.parseLong(args[2].trim());
                return result;
            }
        }
        catch(Exception e)
        {
            HardScience.INSTANCE.error("Unable to parse item resource processing configuration", e);
        }
        return null;
    }
    
    ProcessManager()
    {
        this.micronizerInputSelector = new MicronizerInputSelector(this);
        
        //load defaults
        for(String csv : Configurator.PROCESSING.fluidResourceDefaults)
        {
            FluidProcessInfo info = readFluidCSV(csv);
            if(info != null) 
            {
                info.normalize();
                this.fluidInfos.put(info.resource, info);
            }
        }
        
        //load defaults
        for(String csv : Configurator.PROCESSING.itemResourceDefaults)
        {
            IngredientProcessInfo info = readItemCSV(csv);
            if(info != null && info.ingredient != Ingredient.EMPTY) 
            {
                info.normalize();
                this.ingredientInfos.put(info.ingString, info);
            }
        }
    }
    
    /**
     * Will replace if already exists.
     */
    public FluidProcessInfo putInfo(FluidResource resource, long reserveLevel, long targetLevel)
    {
        FluidProcessInfo result = new FluidProcessInfo(resource);
        result.reserveStockLevel = reserveLevel;
        result.targetStockLevel = targetLevel;
        this.fluidInfos.put(resource, result);
        this.setDirty();
        return result;
    }
    
    /**
     * Will replace if already exists.
     */
    public IngredientProcessInfo putInfo(String ingredientString, long reserveLevel, long targetLevel)
    {
        IngredientProcessInfo result = new IngredientProcessInfo(ingredientString);
        result.reserveStockLevel = reserveLevel;
        result.targetStockLevel = targetLevel;
        this.ingredientInfos.put(ingredientString, result);
        this.setDirty();
        return result;
    }
    
    public FluidProcessInfo getInfo(FluidResource resource)
    {
        return this.fluidInfos.get(resource);
    }
    
    /**
     * For use in control GUI
     */
    public ImmutableList<FluidProcessInfo> allFluidInfos()
    {
        return ImmutableList.copyOf(this.fluidInfos.values());
    }
    
    /**
     * Searches for ingredients that match the given item resource
     * and chooses the one with the highest target levels.
     */
    public IngredientProcessInfo getInfo(ItemResource resource)
    {
        ItemStack stack = resource.sampleItemStack();
        
        IngredientProcessInfo result = null;
        for(IngredientProcessInfo pi : this.ingredientInfos.values())
        {
            if(pi.ingredient.test(stack))
            {
                if(result == null 
                        || pi.targetStockLevel < result.targetStockLevel
                        || pi.reserveStockLevel < result.reserveStockLevel)
                {
                    result = pi;
                }
            }
        }
        return result;
    }
    
    /**
     * For use in control GUI
     */
    public IngredientProcessInfo getInfo(String ingredientString)
    {
        return this.ingredientInfos.get(ingredientString);
    }
    
    /**
     * For use in control GUI
     */
    public ImmutableList<IngredientProcessInfo> allIngredientInfos()
    {
        return ImmutableList.copyOf(this.ingredientInfos.values());
    }
    
    @Override
    public @Nullable IDomain getDomain()
    {
        return this.domain;
    }

    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag)
    {
        if(tag.hasKey(NBT_FLUID_SETTINGS))
        {
            this.fluidInfos.clear();
            
            NBTTagList tags = tag.getTagList(NBT_FLUID_SETTINGS, 10);
            if( tags != null && !tags.hasNoTags())
            {
                for (int i = 0; i < tags.tagCount(); ++i)
                {
                    try
                    {
                        FluidProcessInfo pi = new FluidProcessInfo(tags.getCompoundTagAt(i));
                        if(pi.resource != null)  this.fluidInfos.put(pi.resource, pi);
                    }
                    catch(Exception e)
                    {
                        HardScience.INSTANCE.error("Unable to read fluid process settings", e);
                    }
                }
            }
        }
        
        if(tag.hasKey(NBT_INGREDIENT_SETTINGS))
        {
            this.ingredientInfos.clear();
            
            NBTTagList tags = tag.getTagList(NBT_INGREDIENT_SETTINGS, 10);
            if( tags != null && !tags.hasNoTags())
            {
                for (int i = 0; i < tags.tagCount(); ++i)
                {
                    try
                    {
                        IngredientProcessInfo pi = new IngredientProcessInfo(tags.getCompoundTagAt(i));
                        if(pi.ingredient != null && pi.ingredient != Ingredient.EMPTY)  
                                this.ingredientInfos.put(pi.ingString, pi);
                    }
                    catch(Exception e)
                    {
                        HardScience.INSTANCE.error("Unable to read ingredient process settings", e);
                    }                
                }   
            }
        }
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        if(!this.fluidInfos.isEmpty())
        {
            NBTTagList tags = new NBTTagList();
            for(FluidProcessInfo pi : this.fluidInfos.values())
            {
                tags.appendTag(pi.toNBT());
            }
            tag.setTag(NBT_FLUID_SETTINGS, tags);
        }
        
        if(!this.ingredientInfos.isEmpty())
        {
            NBTTagList tags = new NBTTagList();
            for(IngredientProcessInfo pi : this.ingredientInfos.values())
            {
                tags.appendTag(pi.toNBT());
            }
            tag.setTag(NBT_INGREDIENT_SETTINGS, tags);
        }
    }

    @Override
    public void setDirty()
    {
        if(this.domain != null) this.domain.setDirty();
    }

    @Override
    public String tagName()
    {
        return NBT_SELF;
    }

    @Override
    public void setDomain(IDomain domain)
    {
        this.domain = domain;
    }
}
