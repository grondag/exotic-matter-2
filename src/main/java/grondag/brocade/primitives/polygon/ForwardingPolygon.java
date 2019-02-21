package grondag.brocade.primitives.polygon;

import grondag.brocade.painting.Surface;
import grondag.brocade.primitives.vertex.Vec3f;
import grondag.fermion.world.Rotation;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.util.math.Direction;

public class ForwardingPolygon implements IPolygon {
    @FunctionalInterface
    public static interface BooleanGetter {
        boolean getValue();
    }

    @FunctionalInterface
    public static interface BooleanSetter {
        void setValue(boolean value);
    }

    @FunctionalInterface
    public static interface IntGetter {
        int getValue();
    }

    @FunctionalInterface
    public static interface IntSetter {
        void setValue(int value);
    }

    public static final BooleanSetter DEFAULT_BOOL_SETTER = (b) -> {
        throw new UnsupportedOperationException();
    };
    public static final BooleanGetter DEFAULT_BOOL_GETTER = () -> false;

    public static final IntSetter DEFAULT_INT_SETTER = (i) -> {
        throw new UnsupportedOperationException();
    };
    public static final IntGetter DEFAULT_INT_GETTER = () -> IPolygon.NO_LINK_OR_TAG;

    public IPolygon wrapped;

    public BooleanSetter markSetter = DEFAULT_BOOL_SETTER;
    public BooleanGetter markGetter = DEFAULT_BOOL_GETTER;

    public BooleanSetter deletedSetter = DEFAULT_BOOL_SETTER;
    public BooleanGetter deletedGetter = DEFAULT_BOOL_GETTER;

    public IntSetter linkSetter = DEFAULT_INT_SETTER;
    public IntGetter linkGetter = DEFAULT_INT_GETTER;

    public IntSetter tagSetter = DEFAULT_INT_SETTER;
    public IntGetter tagGetter = DEFAULT_INT_GETTER;

    @Override
    public int vertexCount() {
        return wrapped.vertexCount();
    }

    @Override
    public Vec3f getPos(int index) {
        return wrapped.getPos(index);
    }

    @Override
    public Vec3f getFaceNormal() {
        return wrapped.getFaceNormal();
    }

    @Override
    public Direction getNominalFace() {
        return wrapped.getNominalFace();
    }

    @Override
    public Surface getSurface() {
        return wrapped.getSurface();
    }

    @Override
    public Vec3f getVertexNormal(int vertexIndex) {
        return wrapped.getVertexNormal(vertexIndex);
    }

    @Override
    public boolean hasVertexNormal(int vertexIndex) {
        return wrapped.hasVertexNormal(vertexIndex);
    }

    @Override
    public float getVertexNormalX(int vertexIndex) {
        return wrapped.getVertexNormalX(vertexIndex);
    }

    @Override
    public float getVertexNormalY(int vertexIndex) {
        return wrapped.getVertexNormalY(vertexIndex);
    }

    @Override
    public float getVertexNormalZ(int vertexIndex) {
        return wrapped.getVertexNormalZ(vertexIndex);
    }

    @Override
    public float getMaxU(int layerIndex) {
        return wrapped.getMaxU(layerIndex);
    }

    @Override
    public float getMaxV(int layerIndex) {
        return wrapped.getMaxV(layerIndex);
    }

    @Override
    public float getMinU(int layerIndex) {
        return wrapped.getMinU(layerIndex);
    }

    @Override
    public float getMinV(int layerIndex) {
        return wrapped.getMinV(layerIndex);
    }

    @Override
    public int layerCount() {
        return wrapped.layerCount();
    }

    @Override
    public String getTextureName(int layerIndex) {
        return wrapped.getTextureName(layerIndex);
    }

    @Override
    public boolean shouldContractUVs(int layerIndex) {
        return wrapped.shouldContractUVs(layerIndex);
    }

    @Override
    public Rotation getRotation(int layerIndex) {
        return wrapped.getRotation(layerIndex);
    }

    @Override
    public float getVertexX(int vertexIndex) {
        return wrapped.getVertexX(vertexIndex);
    }

    @Override
    public float getVertexY(int vertexIndex) {
        return wrapped.getVertexY(vertexIndex);
    }

    @Override
    public float getVertexZ(int vertexIndex) {
        return wrapped.getVertexZ(vertexIndex);
    }

    @Override
    public int getVertexColor(int layerIndex, int vertexIndex) {
        return wrapped.getVertexColor(layerIndex, vertexIndex);
    }

    @Override
    public int getVertexGlow(int vertexIndex) {
        return wrapped.getVertexGlow(vertexIndex);
    }

    @Override
    public float getVertexU(int layerIndex, int vertexIndex) {
        return wrapped.getVertexU(layerIndex, vertexIndex);
    }

    @Override
    public float getVertexV(int layerIndex, int vertexIndex) {
        return wrapped.getVertexV(layerIndex, vertexIndex);
    }

    @Override
    public int getTextureSalt() {
        return wrapped.getTextureSalt();
    }

    @Override
    public boolean isLockUV(int layerIndex) {
        return wrapped.isLockUV(layerIndex);
    }

    @Override
    public boolean hasRenderLayer(BlockRenderLayer layer) {
        return wrapped.hasRenderLayer(layer);
    }

    @Override
    public BlockRenderLayer getRenderLayer(int layerIndex) {
        return wrapped.getRenderLayer(layerIndex);
    }

    @Override
    public boolean isEmissive(int textureLayerIndex) {
        return wrapped.isEmissive(textureLayerIndex);
    }

    @Override
    public boolean isMarked() {
        return markGetter.getValue();
    }

    @Override
    public void setMark(boolean isMarked) {
        markSetter.setValue(isMarked);
    }

    @Override
    public void setDeleted() {
        deletedSetter.setValue(true);
    }

    @Override
    public boolean isDeleted() {
        return deletedGetter.getValue();
    }

    @Override
    public void setLink(int link) {
        linkSetter.setValue(link);
    }

    @Override
    public int getLink() {
        return linkGetter.getValue();
    }

    @Override
    public void setTag(int tag) {
        tagSetter.setValue(tag);
    }

    @Override
    public int getTag() {
        return tagGetter.getValue();
    }
}
