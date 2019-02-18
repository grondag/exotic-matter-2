package grondag.brocade.primitives.polygon;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import grondag.exotic_matter.model.painting.Surface;
import grondag.exotic_matter.model.primitives.polygon.PolygonAccessor.Layer;
import grondag.exotic_matter.model.primitives.vertex.Vec3f;
import grondag.exotic_matter.varia.BitPacker64;
import grondag.exotic_matter.world.Rotation;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

public abstract class AbstractPolygon<T extends AbstractPolygon<T>>  implements IPolygon
{
    protected static final BitPacker64<AbstractPolygon<?>> BITPACKER = new BitPacker64<AbstractPolygon<?>>(p -> p.stateBits, (p, b) -> p.stateBits = b);

    protected static final BitPacker64<AbstractPolygon<?>>.EnumElement<EnumFacing> NOMINAL_FACE_BITS = BITPACKER.createEnumElement(EnumFacing.class);
    protected static final BitPacker64<AbstractPolygon<?>>.IntElement PIPELINE_INDEX = BITPACKER.createIntElement(1024);
    
    @SuppressWarnings("unchecked")
    protected static final BitPacker64<AbstractPolygon<?>>.EnumElement<Rotation>[] ROTATION_BITS = new BitPacker64.EnumElement[3];
    
    @SuppressWarnings("unchecked")
    protected static final BitPacker64<AbstractPolygon<?>>.EnumElement<BlockRenderLayer>[] RENDERPASS_BITS = new BitPacker64.EnumElement[3];
   
    @SuppressWarnings("unchecked")
    protected static final BitPacker64<AbstractPolygon<?>>.BooleanElement[] LOCKUV_BIT = new BitPacker64.BooleanElement[3];

    @SuppressWarnings("unchecked")
    protected static final BitPacker64<AbstractPolygon<?>>.BooleanElement[] EMISSIVE_BIT = new BitPacker64.BooleanElement[3];
    
    @SuppressWarnings("unchecked")
    protected static final BitPacker64<AbstractPolygon<?>>.BooleanElement[] CONTRACTUV_BITS = new BitPacker64.BooleanElement[3];
    
    protected static final BitPacker64<AbstractPolygon<?>>.IntElement SALT_BITS = BITPACKER.createIntElement(256);

    protected static final long DEFAULT_BITS;
    static
    {
        ROTATION_BITS[0] = BITPACKER.createEnumElement(Rotation.class);
        ROTATION_BITS[1] = BITPACKER.createEnumElement(Rotation.class);
        ROTATION_BITS[2] = BITPACKER.createEnumElement(Rotation.class);
        
        RENDERPASS_BITS[0] = BITPACKER.createEnumElement(BlockRenderLayer.class);
        RENDERPASS_BITS[1] = BITPACKER.createEnumElement(BlockRenderLayer.class);
        RENDERPASS_BITS[2] = BITPACKER.createEnumElement(BlockRenderLayer.class);
        
        LOCKUV_BIT[0] = BITPACKER.createBooleanElement();
        LOCKUV_BIT[1] = BITPACKER.createBooleanElement();
        LOCKUV_BIT[2] = BITPACKER.createBooleanElement();
        
        EMISSIVE_BIT[0] = BITPACKER.createBooleanElement();
        EMISSIVE_BIT[1] = BITPACKER.createBooleanElement();
        EMISSIVE_BIT[2] = BITPACKER.createBooleanElement();
        
        CONTRACTUV_BITS[0] = BITPACKER.createBooleanElement();
        CONTRACTUV_BITS[1] = BITPACKER.createBooleanElement();
        CONTRACTUV_BITS[2] = BITPACKER.createBooleanElement();
        
        assert BITPACKER.bitLength() <= 64;
        
        long defaultBits = 0;
        defaultBits |= ROTATION_BITS[0].getBits(Rotation.ROTATE_NONE);
        defaultBits |= ROTATION_BITS[1].getBits(Rotation.ROTATE_NONE);
        defaultBits |= ROTATION_BITS[2].getBits(Rotation.ROTATE_NONE);
        
        defaultBits |= RENDERPASS_BITS[0].getBits(BlockRenderLayer.SOLID);
        defaultBits |= RENDERPASS_BITS[1].getBits(BlockRenderLayer.SOLID);
        defaultBits |= RENDERPASS_BITS[2].getBits(BlockRenderLayer.SOLID);
        
        defaultBits |= LOCKUV_BIT[0].getBits(false);
        defaultBits |= LOCKUV_BIT[1].getBits(false);
        defaultBits |= LOCKUV_BIT[2].getBits(false);
        
        defaultBits |= EMISSIVE_BIT[0].getBits(false);
        defaultBits |= EMISSIVE_BIT[1].getBits(false);
        defaultBits |= EMISSIVE_BIT[2].getBits(false);
        
        defaultBits |= CONTRACTUV_BITS[0].getBits(true);
        defaultBits |= CONTRACTUV_BITS[1].getBits(true);
        defaultBits |= CONTRACTUV_BITS[2].getBits(true);
        
        DEFAULT_BITS = defaultBits;
    }

    protected long stateBits = DEFAULT_BITS;

    protected @Nullable Vec3f faceNormal;
    protected Surface surface = Surface.NO_SURFACE;

    protected abstract Layer<T>[] layerAccess();
    
    public AbstractPolygon<T> load(IPolygon template)
    {
        copyPolyAttributesFrom(template);
        final int limit = this.vertexCount();
        
        if(limit == template.vertexCount())
        {
            for(int i = 0; i < limit; i++)
                this.copyVertexFromImpl(i, template, i);
        }
        
        return this;
    }
    
    protected void copyPolyAttributesFrom(IPolygon template)
    {
        setNominalFaceImpl(template.getNominalFace());
        setPipelineIndexImpl(template.getPipelineIndex());
        setSurfaceImpl(template.getSurface());
        clearFaceNormalImpl();
        
        final int layerCount = template.layerCount();
        
        for(int l = 0; l < layerCount; l++)
        {
            setMaxUImpl(l, template.getMaxU(l));
            setMaxVImpl(l, template.getMaxV(l));
            setMinUImpl(l, template.getMinU(l));
            setMinVImpl(l, template.getMinV(l));
            setEmissiveImpl(l, template.isEmissive(l));
            setRenderLayerImpl(l, template.getRenderLayer(l));
            setLockUVImpl(l, template.isLockUV(l));
            setShouldContractUVsImpl(l, template.shouldContractUVs(l));
            setRotationImpl(l, template.getRotation(l));
            setTextureNameImpl(l, template.getTextureName(l));
            setTextureSaltImpl(template.getTextureSalt());
        }
    }
    
    private static ThreadLocal<Pair<VertexAdapter.Inner, VertexAdapter.Fixed>> adapters = new ThreadLocal<Pair<VertexAdapter.Inner, VertexAdapter.Fixed>>()
    {
        @Override
        protected Pair<VertexAdapter.Inner, VertexAdapter.Fixed> initialValue()
        {
            return Pair.of(new VertexAdapter.Inner(), new VertexAdapter.Fixed());
        }
    };
    
    protected void copyVertexFromImpl(int targetIndex, IPolygon source, int sourceIndex)
    {
        final Pair<VertexAdapter.Inner, VertexAdapter.Fixed> help = adapters.get();
        help.getLeft().prepare(this, targetIndex)
            .copyFrom(help.getRight().prepare(source, sourceIndex));
    }
    
    @Override
    public final EnumFacing getNominalFace()
    {
        return NOMINAL_FACE_BITS.getValue(this);
    }
    
    /** supports mutable interface */
    protected final void setNominalFaceImpl(EnumFacing face)
    {
        NOMINAL_FACE_BITS.setValue(face, this);
    }

    @Override
    public final int getPipelineIndex()
    {
        return PIPELINE_INDEX.getValue(this);
    }

    /** supports mutable interface */
    protected final void setPipelineIndexImpl(int pipelineIndex)
    {
        PIPELINE_INDEX.setValue(pipelineIndex, this);
    }
    
    @Override
    public final boolean shouldContractUVs(int layerIndex)
    {
        assert layerIndex < this.layerCount();
        return CONTRACTUV_BITS[layerIndex].getValue(this);
    }

    /** supports mutable interface */
    protected final void setShouldContractUVsImpl(int layerIndex, boolean contractUVs)
    {
        assert layerIndex < this.layerCount();
        CONTRACTUV_BITS[layerIndex].setValue(contractUVs, this);
    }
    
    @Override
    public final Rotation getRotation(int layerIndex)
    {
        assert layerIndex < this.layerCount();
        return ROTATION_BITS[layerIndex].getValue(this);
    }

    /** supports mutable interface */
    protected final void setRotationImpl(int layerIndex, Rotation rotation)
    {
        assert layerIndex < this.layerCount();
        ROTATION_BITS[layerIndex].setValue(rotation, this);
    }
    
    @Override
    public final int getTextureSalt()
    {
        return SALT_BITS.getValue(this);
    }

    /** supports mutable interface */
    protected final void setTextureSaltImpl(int salt)
    {
        SALT_BITS.setValue(salt, this);
    }
    
    @Override
    public final boolean isLockUV(int layerIndex)
    {
        assert layerIndex < this.layerCount();
        return LOCKUV_BIT[layerIndex].getValue(this);
    }

    /** supports mutable interface */
    protected final void setLockUVImpl(int layerIndex, boolean lockUV)
    {
        assert layerIndex < this.layerCount();
        LOCKUV_BIT[layerIndex].setValue(lockUV, this);
    }
    
    @Override
    public final BlockRenderLayer getRenderLayer(int layerIndex)
    {
        assert layerIndex < this.layerCount();
        return RENDERPASS_BITS[layerIndex].getValue(this);
    }
    
    /** supports mutable interface */
    protected final void setRenderLayerImpl(int layerIndex, BlockRenderLayer layer)
    {
        assert layerIndex < this.layerCount();
        RENDERPASS_BITS[layerIndex].setValue(layer, this);
    }
    
    @Override
    public final boolean isEmissive(int layerIndex)
    {
        assert layerIndex < this.layerCount();
        return EMISSIVE_BIT[layerIndex].getValue(this);
    }

    /** supports mutable interface */
    protected final void setEmissiveImpl(int layerIndex, boolean emissive)
    {
        assert layerIndex < this.layerCount();
        EMISSIVE_BIT[layerIndex].setValue(emissive, this);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public final float getMaxU(int layerIndex)
    {
        return layerAccess()[layerIndex].uMaxGetter.get((T) this);
    }

    /** supports mutable interface */
    @SuppressWarnings("unchecked")
    protected final void setMaxUImpl(int layerIndex, float maxU)
    {
        layerAccess()[layerIndex].uMaxSetter.set((T) this, maxU);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final float getMaxV(int layerIndex)
    {
        return layerAccess()[layerIndex].vMaxGetter.get((T) this);
    }
    
    /** supports mutable interface */
    @SuppressWarnings("unchecked")
    protected final void setMaxVImpl(int layerIndex, float maxV)
    {
        layerAccess()[layerIndex].vMaxSetter.set((T) this, maxV);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final float getMinU(int layerIndex)
    {
        return layerAccess()[layerIndex].uMinGetter.get((T) this);
    }
    
    /** supports mutable interface */
    @SuppressWarnings("unchecked")
    protected final void setMinUImpl(int layerIndex, float minU)
    {
        layerAccess()[layerIndex].uMinSetter.set((T) this, minU);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final float getMinV(int layerIndex)
    {
        return layerAccess()[layerIndex].vMinGetter.get((T) this);
    }

    /** supports mutable interface */
    @SuppressWarnings("unchecked")
    protected final void setMinVImpl(int layerIndex, float minV)
    {
        layerAccess()[layerIndex].vMinSetter.set((T) this, minV);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public final String getTextureName(int layerIndex)
    {
        return layerAccess()[layerIndex].textureGetter.get((T) this);
    }
 
    /** supports mutable interface */
    @SuppressWarnings("unchecked")
    protected final void setTextureNameImpl(int layerIndex, String textureName)
    {
        layerAccess()[layerIndex].textureSetter.set((T) this, textureName);
    }
    
    @Override
    public final Vec3f getFaceNormal()
    {
        Vec3f result = this.faceNormal;
        if(result == null)
        {
            result = computeFaceNormal();
            this.faceNormal = result;
        }
        return result;
    }
    
    /** supports mutable interface */
    protected final void clearFaceNormalImpl()
    {
        this.faceNormal = null;
    }


    @Override
    public final Surface getSurface()
    {
        return surface;
    }
    
    /** supports mutable interface */
    protected final void setSurfaceImpl(Surface surface)
    {
        this.surface = surface;
    }

    /** supports mutable interface */
    protected abstract void setVertexPosImpl(int vertexIndex, float x, float y, float z);
    
    /** supports mutable interface */
    protected abstract void setVertexPosImpl(int vertexIndex, Vec3f pos);
    
    /** supports mutable interface */
    protected abstract void setVertexLayerImpl(int layerIndex, int vertexIndex, float u, float v, int color);
    
    /** supports mutable interface */
    protected abstract void setVertexNormalImpl(int vertexIndex, @Nullable Vec3f normal);

    /** supports mutable interface */
    protected abstract void setVertexNormalImpl(int vertexIndex, float x, float y, float z);

    /** supports mutable interface */
    protected abstract void setVertexColorImpl(int layerIndex, int vertexIndex, int color);

    /** supports mutable interface */
    protected abstract void setVertexUVImpl(int layerIndex, int vertexIndex, float u, float v);

    /** supports mutable interface */
    protected abstract void setVertexUImpl(int layerIndex, int vertexIndex, float u);
    
    /** supports mutable interface */
    protected abstract void setVertexVImpl(int layerIndex, int vertexIndex, float v);
    
    /** supports mutable interface */
    protected abstract void setVertexGlowImpl(int vertexIndex, int glow);

    /** supports mutable interface */
    protected final void setVertexImpl(int vertexIndex, float x, float y, float z, float u, float v, int color, int glow)
    {
        setVertexPosImpl(vertexIndex, x, y, z);
        setVertexLayerImpl(0, vertexIndex, u, v, color);
        setVertexGlowImpl(vertexIndex, glow);
    }
}
