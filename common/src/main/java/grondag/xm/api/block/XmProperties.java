/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.xm.api.block;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import grondag.xm.api.modelstate.primitive.SimplePrimitiveStateMutator;
import grondag.xm.orientation.api.CubeCorner;
import grondag.xm.orientation.api.CubeEdge;
import grondag.xm.orientation.api.CubeRotation;
import grondag.xm.orientation.api.HorizontalEdge;
import grondag.xm.orientation.api.HorizontalFace;

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
