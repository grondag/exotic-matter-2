/*******************************************************************************
 * Copyright 2019 grondag
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.xm.api.block;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

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

@API(status = EXPERIMENTAL)
public class XmProperties {
    public static final DirectionProperty X_ORTHO_FACING = DirectionProperty.of("xm_x_facing", d -> d.getAxis() != Axis.X);
    public static final DirectionProperty Z_ORTHO_FACING = DirectionProperty.of("xm_y_facing", d -> d.getAxis() != Axis.Z);
    public static final IntProperty SPECIES = IntProperty.of("xm_species", 0, 15);
    
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
    
    public static SimpleModelStateMap.Modifier SPECIES_MODIFIER = (modelState, blockState) -> {
        final int species = blockState.get(SPECIES);
        modelState.species(species);
        return modelState;
    };
}
