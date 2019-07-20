package grondag.hard_science.simulator.transport.endpoint;

import javax.annotation.Nullable;

import grondag.exotic_matter.model.primitives.Transform;
import grondag.exotic_matter.model.primitives.Transform.FaceMap;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.hard_science.init.ModPortLayouts;
import grondag.hard_science.init.ModRegistries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Specifies all ports present on the faces of a block and
 * acts as factory for port instances at a specific BlockPos.<p>
 * 
 * Semantics for orientation of port faces in construction 
 * methods are the same used within model state and mesh generation: 
 * Y axis/UP is the default "top" of the machine and north face is 
 * the default "front."<p>
 * 
 * Transformed wrapper uses model state of the block as placed in world
 * to derive actual channel and facing of the ports. When serialized, the transformed
 * wrapper retains both the underlying layout and this transformation.<p>
 * 
 * See {@link Transform#getFaceMap(ISuperModelState)} for more 
 * information on model/block orientation.<p>
 * 
 * Block Device implementations should serialize port layout 
 * (including channel and orientation) because layout and model state are 
 * typically provided by block state / tile entity and will not 
 * be available to the device simulation if the chunk isn't loaded.<p> 
 */
public class PortLayout extends IForgeRegistryEntry.Impl<PortLayout> implements IPortLayout
{
    private static final String NBT_PORT_LAYOUT_NAME = NBTDictionary.claim("plName");
    private static final String NBT_PORT_LAYOUT_FACEMAP = NBTDictionary.claim("plFaceMap");
    
    /**
     * Cache localized instances. 
     */
    private final Localized[] localCache = new Localized[32];
    
    private final PortFace[] faces = new PortFace[6];
    
    public PortLayout(
            String name, 
            PortFace up,
            PortFace down,
            PortFace east,
            PortFace west,
            PortFace north,
            PortFace south)
    {
        this.setRegistryName(name);
        this.faces[EnumFacing.UP.ordinal()] = up;
        this.faces[EnumFacing.DOWN.ordinal()] = down;
        this.faces[EnumFacing.EAST.ordinal()] = east;
        this.faces[EnumFacing.WEST.ordinal()] = west;
        this.faces[EnumFacing.NORTH.ordinal()] = north;
        this.faces[EnumFacing.SOUTH.ordinal()] = south;
    }

    @Override
    public PortFace getFace(EnumFacing face)
    {
        return this.faces[face.ordinal()];
    }
    
    public IPortLayout localize(ISuperModelState modelState)
    {
        return this.localize(Transform.computeTransformKey(modelState));
    }
    
    private IPortLayout localize(int transformKey)
    {
        Localized local = this.localCache[transformKey];
        if(local == null)
        {
            local = new Localized(Transform.getFaceMap(transformKey));
            this.localCache[transformKey] = local;
        }
        return local;
    }
    
    public static IPortLayout fromNBT(NBTTagCompound nbt)
    {
        if(nbt == null || !nbt.hasKey(NBT_PORT_LAYOUT_NAME))
            return ModPortLayouts.empty;
                
        PortLayout result = ModRegistries.portLayoutRegistry.getValue(
                new ResourceLocation(nbt.getString(NBT_PORT_LAYOUT_NAME)));
        
        if(nbt.hasKey(NBT_PORT_LAYOUT_FACEMAP))
        {
            int index = nbt.getInteger(NBT_PORT_LAYOUT_FACEMAP);
            return result.localize(index);
        }
        else return result;
    }

    public static NBTTagCompound toNBT(IPortLayout layout)
    {
        NBTTagCompound result = new NBTTagCompound();
        result.setString(NBT_PORT_LAYOUT_NAME, layout.getRegistryName().toString());
        if(layout instanceof Localized)
        {
            result.setInteger(NBT_PORT_LAYOUT_FACEMAP, ((Localized)layout).faceMap.index);
        }
        return result;
    }
    
    private class Localized implements IPortLayout
    {
        private final FaceMap faceMap;
        
        private Localized(FaceMap faceMap)
        {
            this.faceMap = faceMap;
        }

        @Override
        @Nullable
        public ResourceLocation getRegistryName()
        {
            return PortLayout.this.getRegistryName();
        }

        @Override
        public PortFace getFace(EnumFacing face)
        {
            return PortLayout.this.getFace(this.faceMap.map(face));
        }
    }
}
