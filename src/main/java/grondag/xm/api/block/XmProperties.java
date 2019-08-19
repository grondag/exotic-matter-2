package grondag.xm.api.block;

import grondag.xm.Xm;
import grondag.xm.api.modelstate.SimpleModelStateMap;
import grondag.xm.api.orientation.CubeCorner;
import grondag.xm.api.orientation.CubeEdge;
import grondag.xm.api.orientation.CubeRotation;
import grondag.xm.api.orientation.HorizontalFace;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class XmProperties {
    public static final DirectionProperty X_ORTHO_FACING = DirectionProperty.of("xm_x_facing", d -> d.getAxis() != Axis.X);
    public static final DirectionProperty Z_ORTHO_FACING = DirectionProperty.of("xm_y_facing", d -> d.getAxis() != Axis.Z);
    public static final IntProperty SPECIES = IntProperty.of(Xm.idString("xm_species"), 0, 15);
    
    public static final EnumProperty<Direction.Axis> AXIS = Properties.AXIS;
    public static final EnumProperty<HorizontalFace> HORIZONTAL_FACE = EnumProperty.of("xm_horiz_face", HorizontalFace.class);
    public static final DirectionProperty FACE = Properties.FACING;
    public static final EnumProperty<CubeCorner> CORNER = EnumProperty.of("xm_corner", CubeCorner.class);
    public static final EnumProperty<CubeEdge> EDGE = EnumProperty.of("xm_edge", CubeEdge.class);
    public static final EnumProperty<CubeRotation> ROTATION = EnumProperty.of("xm_rotation", CubeRotation.class);
    
    public static SimpleModelStateMap.Modifier AXIS_MODIFIER = (modelState, blockState) -> {
        final Direction.Axis axis = blockState.get(AXIS);
        modelState.orientationIndex(axis.ordinal());
        return modelState;
    };
    
    public static SimpleModelStateMap.Modifier HORIZONTAL_FACE_MODIFIER = (modelState, blockState) -> {
        final HorizontalFace face = blockState.get(HORIZONTAL_FACE);
        modelState.orientationIndex(face.ordinal());
        return modelState;
    };
    
    public static SimpleModelStateMap.Modifier FACE_MODIFIER = (modelState, blockState) -> {
        final Direction face = blockState.get(FACE);
        modelState.orientationIndex(face.ordinal());
        return modelState;
    };
    
    public static SimpleModelStateMap.Modifier CORNER_MODIFIER = (modelState, blockState) -> {
        final CubeCorner corner = blockState.get(CORNER);
        modelState.orientationIndex(corner.ordinal());
        return modelState;
    };
    
    public static SimpleModelStateMap.Modifier EDGE_MODIFIER = (modelState, blockState) -> {
        final CubeEdge edge = blockState.get(EDGE);
        modelState.orientationIndex(edge.ordinal());
        return modelState;
    };
    
    public static SimpleModelStateMap.Modifier ROTATION_MODIFIER = (modelState, blockState) -> {
        final CubeRotation edge = blockState.get(ROTATION);
        modelState.orientationIndex(edge.ordinal());
        return modelState;
    };
}
