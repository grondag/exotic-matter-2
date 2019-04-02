package grondag.brocade.api.block;

import grondag.brocade.model.state.ISuperModelState;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.IntegerProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

//TODO: mixin to block
public interface BrocadeBlock {
    public static final IntegerProperty HEAT = IntegerProperty.create("brocade_heat", 0, 15);
    public static final IntegerProperty SPECIES = IntegerProperty.create("brocade_species", 0, 15);
    public static final IntegerProperty HEIGHT = IntegerProperty.create("brocade_species", 0, 12);
    
    default boolean brocade_isTerrain(BlockState state) {
        return false;
    }
    
    default boolean brocade_isHot(BlockState state) {
        return false;
    }

    default boolean brocade_isTerrainFiller(BlockState state) {
        return false;
    }

    default boolean brocade_isTerrainHeight(BlockState state) {
        return false;
    }
    
    /**
     * Returns an instance of the default model state for this block. Because model
     * states are mutable, every call returns a new instance.
     */
    ISuperModelState getDefaultModelState();
    
    /**
     * If last parameter is false, does not perform a refresh from world for
     * world-dependent state attributes. Use this option to prevent infinite
     * recursion when need to reference some static state ) information in order to
     * determine dynamic world state. Block tests are main use case for false.
     * 
     * 
     */
    ISuperModelState getModelState(BlockView world, BlockPos pos, boolean refreshFromWorldIfNeeded);

    /**
     * Use when absolutely certain given block state is current.
     */
    ISuperModelState getModelStateAssumeStateIsCurrent(BlockState state, BlockView world, BlockPos pos,
            boolean refreshFromWorldIfNeeded);

    /**
     * Returns model state without caching the value in any way. May use a cached
     * value to satisfy the request.
     * <p>
     * 
     * At least one vanilla routine passes in a block state that does not match
     * world. (After block updates, passes in previous state to detect collision box
     * changes.) <br>
     * <br>
     * 
     * We don't want to update our current state based on stale block state, so for
     * TE blocks the refresh must be coded so we don't inject bad (stale) modelState
     * into TE. <br>
     * <br>
     * 
     * However, we do want to honor the given world state if species is different
     * than current. We do this by directly changing species, because that is only
     * thing that can changed in model state based on block state, and also affects
     * collision box. <br>
     * <br>
     * 
     * NOTE: there is probably still a bug here, because collision box can change
     * based on other components of model state (orthogonalAxis, for example) and
     * those changes may not be detected by path finding.
     */
    ISuperModelState computeModelState(BlockState state, BlockView world, BlockPos pos,
            boolean refreshFromWorldIfNeeded);

}
