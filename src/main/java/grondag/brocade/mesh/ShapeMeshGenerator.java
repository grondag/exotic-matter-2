package grondag.brocade.mesh;

import java.util.function.Consumer;

import grondag.brocade.block.ISuperBlock;
import grondag.brocade.model.dispatch.SideShape;
import grondag.brocade.model.state.ISuperModelState;
import grondag.brocade.model.state.StateFormat;
import grondag.brocade.primitives.polygon.IPolygon;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public abstract class ShapeMeshGenerator {
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

    protected ShapeMeshGenerator(StateFormat stateFormat, int stateFlags) {
        this(stateFormat, stateFlags, 0L);
    }

    protected ShapeMeshGenerator(StateFormat stateFormat, int stateFlags, long defaultShapeStateBits) {
        this.stateFormat = stateFormat;
        this.stateFlags = stateFlags;
        this.defaultShapeStateBits = defaultShapeStateBits;
    }

    /**
     * Override if shape has any kind of orientation to it that can be selected
     * during placement.
     */
    public BlockOrientationType orientationType(ISuperModelState modelState) {
        return BlockOrientationType.NONE;
    }

    /**
     * How much of the sky is occluded by the shape of this block? Based on geometry
     * alone, not transparency. Returns 0 if no occlusion (unlikely result). 1-15 if
     * some occlusion. 255 if fully occludes sky.
     */
    public abstract int geometricSkyOcclusion(ISuperModelState modelState);

    /**
     * Generator will output polygons and they will be quads or tris.
     * <p>
     * 
     * Consumer MUST NOT hold references to any of the polys received.
     */
    public abstract void produceShapeQuads(ISuperModelState modelState, Consumer<IPolygon> target);

    /** Returns true if geometry is a full 1x1x1 cube. */
    public abstract boolean isCube(ISuperModelState modelState);

    /**
     * If this shape uses metadata to affect geometry, retrieves the block/item
     * metadata value that should correspond to this modelstate
     */
    public int getMetaData(ISuperModelState modelState) {
        return 0;
    }

    /**
     * If this shape uses metadata to affect geometry, will be called during block
     * placement and during refreshFromWorld. Can be ignored if the shaped has
     * another mechanism for synchronizing with block meta. (TerrainBlocks get it
     * via TerrainState, for example)
     */
    public void setMetaData(ISuperModelState modelState, int meta) {
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
    public abstract boolean hasLampSurface(ISuperModelState modelState);

    /**
     * Override to true for blocks like stairs and wedges. CubicPlacementHandler
     * will know they need to be placed in a corner instead of a face.
     */
    public boolean isAxisOrthogonalToPlacementFace() {
        return false;
    }

    public abstract boolean rotateBlock(BlockState blockState, World world, BlockPos pos, Direction axis,
            ISuperBlock block, ISuperModelState modelState);

    public abstract SideShape sideShape(ISuperModelState modelState, Direction side);

    public int getStateFlags(ISuperModelState modelState) {
        return stateFlags;
    }
}
