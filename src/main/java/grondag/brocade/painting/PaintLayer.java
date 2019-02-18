package grondag.brocade.painting;

import grondag.exotic_matter.varia.structures.BinaryEnumSet;
import net.minecraft.util.text.translation.I18n;

/**
 * Primitive models can have up to five paint layers.
 * These are vaguely akin to shaders or materials in that
 * each layer will have a texture and other appearance-defining
 * attributes.<p>
 * 
 * The layers have names that describe typical use but
 * they can be used for anything, provided the mesh generator
 * and the model state agree on which surfaces should get which paint.<p>
 * 
 * z-position of layers is per enum order.
 * 
 */
public enum PaintLayer
{
    /** 
     * Typically used as the undecorated appearance the primary surface, often the only paint layer.
     * Lowest (unmodified) z position.  
     */
    BASE(0),
    
    /**
     * Typically used to render sides or bottoms, or the cut surfaces
     * of CSG outputs.
     */
    CUT(0),
    
    /**
     * Typically used to render a secondary surface within a model.
     */
    LAMP(0),
    
    /**
     * Typically used to decorate the primary surface.
     */
    MIDDLE(1),
    
    /**
     * Typically used to decorate the primary surface.
     */
    OUTER(2);
    

    /** Convenience for values().length */
    public static final int SIZE;
    
    /** Convenience for values() */
    public static final PaintLayer VALUES[];
    
    static
    {
        SIZE = values().length;
        VALUES = values();
    }
    
    public static BinaryEnumSet<PaintLayer> BENUMSET = new BinaryEnumSet<>(PaintLayer.class);
    
    private PaintLayer(int textureLayerIndex)
    {
        this.textureLayerIndex = textureLayerIndex;
    }
    
    public final int textureLayerIndex;
    
    public String localizedName()
    {
        return I18n.translateToLocal("paintlayer." + this.name().toLowerCase());
    }
    
}
