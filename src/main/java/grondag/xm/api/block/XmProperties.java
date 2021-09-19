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

import org.jetbrains.annotations.ApiStatus.Experimental;
import grondag.fermion.orientation.api.CubeCorner;
import grondag.fermion.orientation.api.CubeEdge;
import grondag.fermion.orientation.api.CubeRotation;
import grondag.fermion.orientation.api.HorizontalEdge;
import grondag.fermion.orientation.api.HorizontalFace;
import grondag.xm.api.modelstate.primitive.SimplePrimitiveStateMutator;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

@Experimental
public class XmProperties {
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
	public static final EnumProperty<HorizontalFace> HORIZONTAL_FACE = EnumProperty.create("xm_horiz_face", HorizontalFace.class);
	public static final EnumProperty<HorizontalEdge> HORIZONTAL_EDGE = EnumProperty.create("xm_horiz_edge", HorizontalEdge.class);
	public static final DirectionProperty FACE = BlockStateProperties.FACING;
	public static final EnumProperty<CubeCorner> CORNER = EnumProperty.create("xm_corner", CubeCorner.class);
	public static final EnumProperty<CubeEdge> EDGE = EnumProperty.create("xm_edge", CubeEdge.class);
	public static final EnumProperty<CubeRotation> ROTATION = EnumProperty.create("xm_rotation", CubeRotation.class);

	public static SimplePrimitiveStateMutator AXIS_MODIFIER = (modelState, blockState) -> {
		final Direction.Axis axis = blockState.getValue(AXIS);
		modelState.orientationIndex(axis.ordinal());
		return modelState;
	};

	public static SimplePrimitiveStateMutator HORIZONTAL_FACE_MODIFIER = (modelState, blockState) -> {
		final HorizontalFace face = blockState.getValue(HORIZONTAL_FACE);
		modelState.orientationIndex(face.ordinal());
		return modelState;
	};

	public static SimplePrimitiveStateMutator HORIZONTAL_EDGE_MODIFIER = (modelState, blockState) -> {
		final HorizontalEdge edge = blockState.getValue(HORIZONTAL_EDGE);
		modelState.orientationIndex(edge.ordinal());
		return modelState;
	};

	public static SimplePrimitiveStateMutator FACE_MODIFIER = (modelState, blockState) -> {
		final Direction face = blockState.getValue(FACE);
		modelState.orientationIndex(face.ordinal());
		return modelState;
	};

	public static SimplePrimitiveStateMutator CORNER_MODIFIER = (modelState, blockState) -> {
		final CubeCorner corner = blockState.getValue(CORNER);
		modelState.orientationIndex(corner.ordinal());
		return modelState;
	};

	public static SimplePrimitiveStateMutator EDGE_MODIFIER = (modelState, blockState) -> {
		final CubeEdge edge = blockState.getValue(EDGE);
		modelState.orientationIndex(edge.ordinal());
		return modelState;
	};

	public static SimplePrimitiveStateMutator ROTATION_MODIFIER = (modelState, blockState) -> {
		final CubeRotation edge = blockState.getValue(ROTATION);
		modelState.orientationIndex(edge.ordinal());
		return modelState;
	};
}
