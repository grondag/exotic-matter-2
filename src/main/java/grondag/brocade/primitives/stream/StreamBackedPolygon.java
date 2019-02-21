package grondag.brocade.primitives.stream;

import grondag.brocade.painting.Surface;
import grondag.brocade.primitives.polygon.IPolygon;
import grondag.brocade.primitives.vertex.Vec3f;
import grondag.fermion.functions.PrimitiveFunctions.IntToIntFunction;
import grondag.fermion.intstream.IIntStream;
import grondag.fermion.world.Rotation;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.util.math.Direction;

public class StreamBackedPolygon implements IPolygon {
    protected static final int NO_ADDRESS = -1;

    protected static final IntToIntFunction VERTEX_FUNC_TRIANGLE = (v) -> v == 3 ? 0 : v;
    protected static final IntToIntFunction VERTEX_FUNC_STRAIGHT = (v) -> v;
    protected IntToIntFunction vertexIndexer = VERTEX_FUNC_STRAIGHT;

    /**
     * Address of our header within the stream. Do not change directly, use
     * {@link #position(int)}
     */
    protected int baseAddress = NO_ADDRESS;

    protected PolyEncoder polyEncoder;

    /**
     * Offset from base address where vertex data starts.<br>
     * Set when format changes
     */
    protected int vertexOffset = NO_ADDRESS;

    /**
     * Stream address where vertex data starts.<br>
     * Set when format or address changes
     */
    protected int vertexAddress = NO_ADDRESS;

    protected VertexEncoder vertexEncoder;

    /**
     * Offset from base address where glow data starts.<br>
     * Set when format changes.
     */
    protected int glowOffset = NO_ADDRESS;

    /**
     * Start of vertex glow data, if present.<br>
     * Set when format or address changes.
     */
    protected int glowAddress = NO_ADDRESS;

    protected GlowEncoder glowEncoder;

    /**
     * Count of ints consumed in stream.<br>
     * Set when format or address changes.
     */
    protected int stride = 0;

    protected IIntStream stream;

    protected int format() {
        return stream.get(baseAddress);
    }

    /**
     * Reads header from given stream address and all subsequent operations reflect
     * data at that position within the stream.
     */
    protected void moveTo(int baseAddress) {
        this.baseAddress = baseAddress;
        loadFormat();
    }

    protected void setFormat(int newFormat) {
        if (newFormat != format()) {
            stream.set(baseAddress, newFormat);
            loadFormat();
        }
    }

    protected void loadFormat() {
        final int format = format();
        final int vertexCount = PolyStreamFormat.getVertexCount(format);
        this.vertexIndexer = vertexCount == 3 ? VERTEX_FUNC_TRIANGLE : VERTEX_FUNC_STRAIGHT;
        this.polyEncoder = PolyEncoder.get(format);
        this.vertexEncoder = VertexEncoder.get(format);
        this.glowEncoder = GlowEncoder.get(format);
        this.vertexOffset = PolyStreamFormat.polyStride(format, false);
        this.glowOffset = vertexOffset + vertexEncoder.vertexStride() * vertexCount;
        this.vertexAddress = baseAddress + vertexOffset;
        this.glowAddress = baseAddress + glowOffset;
        this.stride = glowOffset + glowEncoder.stride(format);
        assert this.stride == PolyStreamFormat.polyStride(format, true);
    }

    /**
     * Use when stream is in an invalid state and want us to blow up if accessed.
     */
    public void invalidate() {
        baseAddress = EncoderFunctions.BAD_ADDRESS;
        stride = 0;
    }

    /**
     * Number of ints consumed by the current format
     */
    public final int stride() {
        return stride;
    }

    @Override
    public final boolean isMarked() {
        return PolyStreamFormat.isMarked(format());
    }

    @Override
    public final void flipMark() {
        setMark(!isMarked());
    }

    @Override
    public final void setMark(boolean isMarked) {
        setFormat(PolyStreamFormat.setMarked(format(), isMarked));
    }

    @Override
    public final boolean isDeleted() {
        return PolyStreamFormat.isDeleted(format());
    }

    @Override
    public final void setDeleted() {
        setFormat(PolyStreamFormat.setDeleted(format(), true));
    }

    @Override
    public final int getTag() {
        return polyEncoder.getTag(stream, baseAddress);
    }

    @Override
    public final void setTag(int tag) {
        polyEncoder.setTag(stream, baseAddress, tag);
    }

    @Override
    public final int getLink() {
        return polyEncoder.getLink(stream, baseAddress);
    }

    @Override
    public final void setLink(int link) {
        polyEncoder.setLink(stream, baseAddress, link);
    }

    @Override
    public final int vertexCount() {
        return PolyStreamFormat.getVertexCount(format());
    }

    @Override
    @Deprecated
    public final Vec3f getPos(int index) {
        return Vec3f.create(getVertexX(index), getVertexY(index), getVertexZ(index));
    }

    /**
     * Gets current face normal format and if normal needs to be computed, does so
     * and updates both the normal and the format.
     * <p>
     * 
     * Returns true if nominal face normal should be used.
     */
    private boolean checkFaceNormal() {
        final int normalFormat = PolyStreamFormat.getFaceNormalFormat(format());
        if (normalFormat == PolyStreamFormat.FACE_NORMAL_FORMAT_NOMINAL)
            return true;
        else if (normalFormat == PolyStreamFormat.FACE_NORMAL_FORMAT_COMPUTED
                && Float.isNaN(polyEncoder.getFaceNormalX(stream, baseAddress))) {
            Vec3f normal = this.computeFaceNormal();
            polyEncoder.setFaceNormal(stream, baseAddress, normal);
        }
        return false;
    }

    @Override
    public final Vec3f getFaceNormal() {
        return checkFaceNormal() ? Vec3f.forFace(getNominalFace()) : polyEncoder.getFaceNormal(stream, baseAddress);
    }

    @Override
    public final float getFaceNormalX() {
        return checkFaceNormal() ? Vec3f.forFace(getNominalFace()).x()
                : polyEncoder.getFaceNormalX(stream, baseAddress);
    }

    @Override
    public final float getFaceNormalY() {
        return checkFaceNormal() ? Vec3f.forFace(getNominalFace()).y()
                : polyEncoder.getFaceNormalY(stream, baseAddress);
    }

    @Override
    public final float getFaceNormalZ() {
        return checkFaceNormal() ? Vec3f.forFace(getNominalFace()).z()
                : polyEncoder.getFaceNormalZ(stream, baseAddress);
    }

    @Override
    public final Direction getNominalFace() {
        return PolyStreamFormat.getNominalFace(format());
    }

    @Override
    public final Surface getSurface() {
        return StaticEncoder.getSurface(stream, baseAddress);
    }

    @Override
    @Deprecated
    public final Vec3f getVertexNormal(int vertexIndex) {
        return vertexEncoder.hasVertexNormal(stream, vertexAddress, vertexIndexer.apply(vertexIndex))
                ? vertexEncoder.getVertexNormal(stream, vertexAddress, vertexIndexer.apply(vertexIndex))
                : getFaceNormal();
    }

    @Override
    public final boolean hasVertexNormal(int vertexIndex) {
        return vertexEncoder.hasVertexNormal(stream, vertexAddress, vertexIndexer.apply(vertexIndex));
    }

    @Override
    public final float getVertexNormalX(int vertexIndex) {
        return vertexEncoder.hasVertexNormal(stream, vertexAddress, vertexIndexer.apply(vertexIndex))
                ? vertexEncoder.getVertexNormalX(stream, vertexAddress, vertexIndexer.apply(vertexIndex))
                : getFaceNormalX();
    }

    @Override
    public final float getVertexNormalY(int vertexIndex) {
        return vertexEncoder.hasVertexNormal(stream, vertexAddress, vertexIndexer.apply(vertexIndex))
                ? vertexEncoder.getVertexNormalY(stream, vertexAddress, vertexIndexer.apply(vertexIndex))
                : getFaceNormalY();
    }

    @Override
    public final float getVertexNormalZ(int vertexIndex) {
        return vertexEncoder.hasVertexNormal(stream, vertexAddress, vertexIndexer.apply(vertexIndex))
                ? vertexEncoder.getVertexNormalZ(stream, vertexAddress, vertexIndexer.apply(vertexIndex))
                : getFaceNormalZ();
    }

    @Override
    public final float getMaxU(int layerIndex) {
        return polyEncoder.getMaxU(stream, baseAddress, layerIndex);
    }

    @Override
    public final float getMaxV(int layerIndex) {
        return polyEncoder.getMaxV(stream, baseAddress, layerIndex);
    }

    @Override
    public final float getMinU(int layerIndex) {
        return polyEncoder.getMinU(stream, baseAddress, layerIndex);
    }

    @Override
    public final float getMinV(int layerIndex) {
        return polyEncoder.getMinV(stream, baseAddress, layerIndex);
    }

    @Override
    public final int layerCount() {
        return PolyStreamFormat.getLayerCount(format());
    }

    @Override
    public final String getTextureName(int layerIndex) {
        return polyEncoder.getTextureName(stream, baseAddress, layerIndex);
    }

    @Override
    public final boolean shouldContractUVs(int layerIndex) {
        return StaticEncoder.shouldContractUVs(stream, baseAddress, layerIndex);
    }

    @Override
    public final Rotation getRotation(int layerIndex) {
        return StaticEncoder.getRotation(stream, baseAddress, layerIndex);
    }

    @Override
    public final float getVertexX(int vertexIndex) {
        return vertexEncoder.getVertexX(stream, vertexAddress, vertexIndexer.apply(vertexIndex));
    }

    @Override
    public final float getVertexY(int vertexIndex) {
        return vertexEncoder.getVertexY(stream, vertexAddress, vertexIndexer.apply(vertexIndex));
    }

    @Override
    public final float getVertexZ(int vertexIndex) {
        return vertexEncoder.getVertexZ(stream, vertexAddress, vertexIndexer.apply(vertexIndex));
    }

    @Override
    public final int getVertexColor(int layerIndex, int vertexIndex) {
        return vertexEncoder.hasColor()
                ? vertexEncoder.getVertexColor(stream, vertexAddress, layerIndex, vertexIndexer.apply(vertexIndex))
                : polyEncoder.getVertexColor(stream, baseAddress, layerIndex);
    }

    @Override
    public final int getVertexGlow(int vertexIndex) {
        return glowEncoder.getGlow(stream, glowAddress, vertexIndexer.apply(vertexIndex));
    }

    @Override
    public final float getVertexU(int layerIndex, int vertexIndex) {
        return vertexEncoder.getVertexU(stream, vertexAddress, layerIndex, vertexIndexer.apply(vertexIndex));
    }

    @Override
    public final float getVertexV(int layerIndex, int vertexIndex) {
        return vertexEncoder.getVertexV(stream, vertexAddress, layerIndex, vertexIndexer.apply(vertexIndex));
    }

    @Override
    public final int getTextureSalt() {
        return StaticEncoder.getTextureSalt(stream, baseAddress);
    }

    @Override
    public final boolean isLockUV(int layerIndex) {
        return StaticEncoder.isLockUV(stream, baseAddress, layerIndex);
    }

    @Override
    public BlockRenderLayer getRenderLayer(int layerIndex) {
        return StaticEncoder.getRenderLayer(stream, baseAddress, layerIndex);
    }

    @Override
    public boolean isEmissive(int layerIndex) {
        return StaticEncoder.isEmissive(stream, baseAddress, layerIndex);
    }

    //TODO: convert to material
//    @Override
//    public int getPipelineIndex() {
//        return StaticEncoder.getPipelineIndex(stream, baseAddress);
//    }

    @Override
    public int streamAddress() {
        return baseAddress;
    }
}
