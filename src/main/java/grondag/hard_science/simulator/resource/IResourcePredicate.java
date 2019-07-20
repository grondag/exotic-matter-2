package grondag.hard_science.simulator.resource;

import java.util.function.Predicate;

import net.minecraft.item.Item;

/**
 * Extension of predicate for IResource that includes
 * information about the bulkResource that can be inspected
 * by producers or search functions when a brute-force
 * comparision against every possibility isn't realistic.<p>
 * 
 * Which attributes are implemented will depend on the 
 * type of bulkResource. Attributes that don't apply always
 * return 0 or null. <p>
 */
public interface IResourcePredicate<V extends StorageType<V>> extends Predicate<IResource<V>>
{
    /**
     * True if this predicate is the resource instance itself and
     * can have only a matching resource.  Override in IResource - leave default
     * (false) implementation everywhere else.
     */
    public default boolean isEqualityPredicate() { return false; }
    
    /**
     * For item stack type, the item.  Null in all other cases.
     */
    public default Item item() { return null; }
    
    /**
     * Meta data/damage for this resource, if applies.
     * Zero if does not apply.
     */
    public default int meta() { return 0; }
    
    /**
     * True if meta value is ignored by this predicate
     * False if does not apply.
     */
    public default boolean ignoreMeta() { return false; }
    
    /**
     * True if NBT is ignored by this predicate.
     * False if does not apply.
     */
    public default boolean ignoreNBT() { return false; }
    
    /**
     * True if Capabilities are ignored by this predicate
     * False if does not apply.
     */
    public default boolean ignoreCaps() { return false; }

//    @Nullable
//    public static IResourcePredicate<?> fromNBT(NBTTagCompound tag)
//    {
//        if(tag.hasKey(ModNBTTag.PREDICATE_TYPE))
//        {
//            return null;
//        }
//        else
//        {
//            return StorageType.fromNBTWithType(tag);
//        }
//    }
    
//    public static NBTTagCompound toNBT(IResourcePredicate<?> predicate)
//    {
//        if(predicate.isEqualityPredicate())
//        {
//            return StorageType.toNBTWithType((IResource<?>) predicate);
//        }
//        
//        return new NBTTagCompound();
//    }
}
