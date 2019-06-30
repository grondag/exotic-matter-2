package grondag.xm2.mesh;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import grondag.fermion.shadow.jankson.annotation.Nullable;
import grondag.xm2.placement.BlockOrientationHandler;
import grondag.xm2.state.ModelState;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.EnumProperty;

public enum BlockOrientationType {
    NONE(null, (s, c) -> s, (b, m) -> {}), 
    AXIS(   BlockOrientationHandler.AXIS_PROP, 
            BlockOrientationHandler::axisBlockState, 
            BlockOrientationHandler::axisModelState),
    FACE(   BlockOrientationHandler.FACE_PROP, 
            BlockOrientationHandler::faceBlockState,
            BlockOrientationHandler::faceModelState), 
    EDGE(   BlockOrientationHandler.EDGE_PROP, 
            BlockOrientationHandler::edgeBlockState,
            BlockOrientationHandler::edgeModelState), 
    CORNER( BlockOrientationHandler.CORNER_PROP,
            BlockOrientationHandler::cornerBlockState,
            BlockOrientationHandler::cornerModelState);
    
    public final @Nullable EnumProperty<?> property;
    
    public final BiFunction<BlockState, ItemPlacementContext, BlockState> placementFunc;
    
    /**
     * Updates the model state from block state for orientation.
     */
    public final BiConsumer<BlockState, ModelState> stateFunc;
    
    private BlockOrientationType(
            EnumProperty<?> property, 
            BiFunction<BlockState, ItemPlacementContext, BlockState> placementFunc,
            BiConsumer<BlockState, ModelState> stateFunc) {
        this.property = property;
        this.placementFunc = placementFunc;
        this.stateFunc = stateFunc;
    }
}
