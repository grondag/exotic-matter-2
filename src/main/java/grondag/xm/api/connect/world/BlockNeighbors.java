/*
 * Copyright © Original Authors
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
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.api.connect.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import grondag.fermion.orientation.api.CubeCorner;
import grondag.fermion.orientation.api.CubeEdge;
import grondag.fermion.orientation.api.HorizontalEdge;
import grondag.fermion.orientation.api.HorizontalFace;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.connect.BlocksNeighborsImpl;

/**
 * Provides lazy, cached access to the block state for a single block position
 * and the 26 adjacent block positions.
 *
 * <p>If provided with a state function, will also provide lazy, cached access to
 * results of that function.
 *
 * <p>If provided with a boolean-valued function defined by {@link BlockTest} will
 * provide lazy, cached access to the results of that function. Note that the
 * test function can be replaced via {@link #withTest(BlockTest)} to support
 * performant evaluation in uses cases that require results of more than one
 * test.
 *
 * <p>Instance methods are not thread-safe and this class is intended to be
 * accessed as re-used, threadlocal instances via
 * {@link #threadLocal(BlockGetter, BlockPos)} and its variants.
 *
 * <p>If thread local access is insufficient (for example, if two instances are
 * needed in the same scope) then {@link #claim(BlockGetter, BlockPos)} (or one of
 * its variants) can be used to retrieve or create a potentially pooled
 * instance. If {@link #release()} is then called before the instance goes out
 * of scope, it will be returned to a pool for later re-used.
 *
 * <p>Both usage patterns (threadlocal and claim()/release()) will generally result
 * in no- or low- allocation. Using these patterns may help prevent
 * garbage-collection related lag spikes on clients when this utility is used
 * during chunk rebuilds or other scenarios that result in high call volume.
 *
 * <p>Note that instances provided by {@link #claim(BlockGetter, BlockPos)} have no
 * connection to the re-use pool and no references are retained to them once
 * claimed. Calling {@link #release()} is unnecessary and has no effect for
 * instances returned by {@link #threadLocal(BlockGetter, BlockPos)}.
 */
public interface BlockNeighbors {
	void release();

	BlockNeighbors withBlockState(BlockState myState);

	BlockNeighbors withTest(BlockTest<?> blockTest);

	BlockState blockState();

	BlockState blockState(Direction face);

	BlockState blockState(CubeEdge corner);

	BlockState blockState(CubeCorner corner);

	default BlockState blockState(HorizontalFace face) {
		return blockState(face.face);
	}

	default BlockState blockState(Direction face1, Direction face2) {
		return blockState(CubeEdge.find(face1, face2));
	}

	default BlockState blockState(HorizontalEdge corner) {
		return blockState(corner.left.face, corner.right.face);
	}

	default BlockState blockState(Direction face1, Direction face2, Direction face3) {
		return blockState(CubeCorner.find(face1, face2, face3));
	}

	BlockEntity blockEntity();

	BlockEntity blockEntity(Direction face);

	BlockEntity blockEntity(CubeEdge corner);

	BlockEntity blockEntity(CubeCorner corner);

	default BlockEntity blockEntity(HorizontalFace face) {
		return blockEntity(face.face);
	}

	default BlockEntity blockEntity(Direction face1, Direction face2) {
		return blockEntity(CubeEdge.find(face1, face2));
	}

	default BlockEntity blockEntity(HorizontalEdge corner) {
		return blockEntity(corner.left.face, corner.right.face);
	}

	default BlockEntity blockEntity(Direction face1, Direction face2, Direction face3) {
		return blockEntity(CubeCorner.find(face1, face2, face3));
	}

	Object modelState();

	Object modelState(Direction face);

	Object modelState(CubeEdge corner);

	Object modelState(CubeCorner corner);

	default Object modelState(Direction face1, Direction face2, Direction face3) {
		return modelState(CubeCorner.find(face1, face2, face3));
	}

	default Object modelState(HorizontalFace face) {
		return modelState(face.face);
	}

	default Object modelState(Direction face1, Direction face2) {
		return modelState(CubeEdge.find(face1, face2));
	}

	default Object modelState(HorizontalEdge corner) {
		return modelState(corner.left.face, corner.right.face);
	}

	/** use this to override world results. */
	void override(Direction face, boolean override);

	boolean result(Direction face);

	boolean result(CubeEdge corner);

	boolean result(CubeCorner corner);

	default boolean result(Direction face1, Direction face2) {
		return result(CubeEdge.find(face1, face2));
	}

	default boolean result(HorizontalFace face) {
		return result(face.face);
	}

	default boolean result(HorizontalEdge corner) {
		return result(corner.left.face, corner.right.face);
	}

	default boolean result(Direction face1, Direction face2, Direction face3) {
		return result(CubeCorner.find(face1, face2, face3));
	}

	static BlockNeighbors threadLocal(BlockGetter world, int x, int y, int z, ModelStateFunction stateFunc, BlockTest<?> test) {
		return BlocksNeighborsImpl.threadLocal(world, x, y, z, stateFunc, test);
	}

	static BlockNeighbors threadLocal(BlockGetter world, int x, int y, int z, ModelStateFunction stateFunc) {
		return threadLocal(world, x, y, z, stateFunc, null);
	}

	static BlockNeighbors threadLocal(BlockGetter world, int x, int y, int z, BlockTest<?> test) {
		return threadLocal(world, x, y, z, null, test);
	}

	static BlockNeighbors threadLocal(BlockGetter world, int x, int y, int z) {
		return threadLocal(world, x, y, z, null, null);
	}

	static BlockNeighbors threadLocal(BlockGetter world, BlockPos pos, ModelStateFunction stateFunc, BlockTest<?> test) {
		return threadLocal(world, pos.getX(), pos.getY(), pos.getZ(), stateFunc, test);
	}

	static BlockNeighbors threadLocal(BlockGetter world, BlockPos pos, ModelStateFunction stateFunc) {
		return threadLocal(world, pos, stateFunc, null);
	}

	static BlockNeighbors threadLocal(BlockGetter world, BlockPos pos, BlockTest<?> test) {
		return threadLocal(world, pos, null, test);
	}

	static BlockNeighbors threadLocal(BlockGetter world, BlockPos pos) {
		return threadLocal(world, pos, null, null);
	}

	static BlockNeighbors claim(BlockGetter world, int x, int y, int z, ModelStateFunction stateFunc, BlockTest<?> test) {
		return BlocksNeighborsImpl.claim(world, x, y, z, stateFunc, test);
	}

	static BlockNeighbors claim(BlockGetter world, int x, int y, int z, ModelStateFunction stateFunc) {
		return claim(world, x, y, z, stateFunc, null);
	}

	static BlockNeighbors claim(BlockGetter world, int x, int y, int z, BlockTest<?> test) {
		return claim(world, x, y, z, null, test);
	}

	static BlockNeighbors claim(BlockGetter world, int x, int y, int z) {
		return claim(world, x, y, z, null, null);
	}

	static BlockNeighbors claim(BlockGetter world, BlockPos pos, ModelStateFunction stateFunc, BlockTest<?> test) {
		return claim(world, pos.getX(), pos.getY(), pos.getZ(), stateFunc, test);
	}

	static BlockNeighbors claimIfNull(BlockNeighbors neighbors, BlockGetter world, BlockPos pos, ModelStateFunction stateFunc, BlockTest<?> test) {
		return neighbors == null ? claim(world, pos.getX(), pos.getY(), pos.getZ(), stateFunc, test) : neighbors.withTest(test);
	}

	static BlockNeighbors claimIfNull(BlockNeighbors neighbors, BlockGetter world, BlockPos pos, ModelStateFunction stateFunc, BlockTest<PrimitiveState> test, BlockState blockState) {
		return neighbors == null ? claim(world, pos.getX(), pos.getY(), pos.getZ(), stateFunc, test).withBlockState(blockState) : neighbors.withTest(test);
	}

	static BlockNeighbors claim(BlockGetter world, BlockPos pos, ModelStateFunction stateFunc) {
		return claim(world, pos, stateFunc, null);
	}

	static BlockNeighbors claim(BlockGetter world, BlockPos pos, BlockTest<?> test) {
		return claim(world, pos, null, test);
	}

	static BlockNeighbors claim(BlockGetter world, BlockPos pos) {
		return claim(world, pos, null, null);
	}
}
