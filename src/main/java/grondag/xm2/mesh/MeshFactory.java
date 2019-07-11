package grondag.xm2.mesh;

import java.util.function.Consumer;

import grondag.xm2.primitives.polygon.IPolygon;
import grondag.xm2.state.ModelState;
import grondag.xm2.state.StateFormat;

public abstract class MeshFactory {
    /**
     * used by ModelState to know why type of state representation is needed by this
     * shape
     */
    public final StateFormat stateFormat;

    /**
     * bits flags used by ModelState to know which optional state elements are
     * needed by this shape
     */
    private final int stateFlags;

    /**
     * When shape is changed on ModelState, the per-shape bits will be set to this
     * value. Only need to change if shape needs some preset state.
     */
    public final long defaultShapeStateBits;

    protected MeshFactory(StateFormat stateFormat, int stateFlags) {
        this(stateFormat, stateFlags, 0L);
    }

    protected MeshFactory(StateFormat stateFormat, int stateFlags, long defaultShapeStateBits) {
        this.stateFormat = stateFormat;
        this.stateFlags = stateFlags;
        this.defaultShapeStateBits = defaultShapeStateBits;
    }

    /**
     * Override if shape has any kind of orientation to it that can be selected
     * during placement.
     */
    public BlockOrientationType orientationType(ModelState modelState) {
        return BlockOrientationType.NONE;
    }

    /**
     * How much of the sky is occluded by the shape of this block? Based on geometry
     * alone, not transparency. Returns 0 if no occlusion (unlikely result). 1-15 if
     * some occlusion. 255 if fully occludes sky.
     */
    public abstract int geometricSkyOcclusion(ModelState modelState);

    /**
     * Generator will output polygons and they will be quads or tris.
     * <p>
     * 
     * Consumer MUST NOT hold references to any of the polys received.
     */
    public abstract void produceShapeQuads(ModelState modelState, Consumer<IPolygon> target);

    /** Returns true if geometry is a full 1x1x1 cube. */
    public abstract boolean isCube(ModelState modelState);

    /**
     * If this shape uses metadata to affect geometry, retrieves the block/item
     * metadata value that should correspond to this modelstate
     */
    public int getMetaData(ModelState modelState) {
        return 0;
    }

    /**
     * If this shape uses metadata to affect geometry, will be called during block
     * placement and during refreshFromWorld. Can be ignored if the shaped has
     * another mechanism for synchronizing with block meta. (TerrainBlocks get it
     * via TerrainState, for example)
     */
    public void setMetaData(ModelState modelState, int meta) {
        // Default is to do nothing
    }

    /**
     * If true, shape can be placed on itself to become bigger.
     */
    public boolean isAdditive() {
        return false;
    }

    /**
     * True if shape mesh generator will output lamp surface quads with the given
     * model state. Used by model state to detect if model has translucent geometry
     * and possibly other qualitative attributes without generating a mesh.
     */
    public abstract boolean hasLampSurface(ModelState modelState);

    /**
     * Override to true for blocks like stairs and wedges. CubicPlacementHandler
     * will know they need to be placed in a corner instead of a face.
     */
    public boolean isAxisOrthogonalToPlacementFace() {
        return false;
    }

    public int getStateFlags(ModelState modelState) {
        return stateFlags;
    }
}
