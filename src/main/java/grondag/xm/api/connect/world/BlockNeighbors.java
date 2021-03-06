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
package grondag.xm.api.connect.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import grondag.fermion.orientation.api.CubeCorner;
import grondag.fermion.orientation.api.CubeEdge;
import grondag.fermion.orientation.api.HorizontalEdge;
import grondag.fermion.orientation.api.HorizontalFace;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.connect.BlocksNeighborsImpl;

/**
 * Provides lazy, cached access to the block state for a single block position
 * and the 26 adjacent block positions.
 * <p>
 *
 * If provided with a state function, will also provide lazy, cached access to
 * results of that function.
 * <p>
 *
 * If provided with a boolean-valued function defined by {@link BlockTest} will
 * provide lazy, cached access to the results of that function. Note that the
 * test function can be replaced via {@link #withTest(BlockTest)} to support
 * performant evaluation in uses cases that require results of more than one
 * test.
 * <p>
 *
 * Instance methods are not thread-safe and this class is intended to be
 * accessed as re-used, threadlocal instances via
 * {@link #threadLocal(BlockView, BlockPos)} and its variants.
 * <p>
 *
 * If thread local access is insufficient (for example, if two instances are
 * needed in the same scope) then {@link #claim(BlockView, BlockPos)} (or one of
 * its variants) can be used to retrieve or create a potentially pooled
 * instance. If {@link #release()} is then called before the instance goes out
 * of scope, it will be returned to a pool for later re-used.
 * <p>
 *
 * Both usage patterns (threadlocal and claim()/release()) will generally result
 * in no- or low- allocation. Using these patterns may help prevent
 * garbage-collection related lag spikes on clients when this utility is used
 * during chunk rebuilds or other scenarios that result in high call volume.
 * <p>
 *
 * Note that instances provided by {@link #claim(BlockView, BlockPos)} have no
 * connection to the re-use pool and no references are retained to them once
 * claimed. Calling {@link #release()} is unnecessary and has no effect for
 * instances returned by {@link #threadLocal(BlockView, BlockPos)}.
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

	/** use this to override world results */
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

	static BlockNeighbors threadLocal(BlockView world, int x, int y, int z, ModelStateFunction stateFunc, BlockTest<?> test) {
		return BlocksNeighborsImpl.threadLocal(world, x, y, z, stateFunc, test);
	}

	static BlockNeighbors threadLocal(BlockView world, int x, int y, int z, ModelStateFunction stateFunc) {
		return threadLocal(world, x, y, z, stateFunc, null);
	}

	static BlockNeighbors threadLocal(BlockView world, int x, int y, int z, BlockTest<?> test) {
		return threadLocal(world, x, y, z, null, test);
	}

	static BlockNeighbors threadLocal(BlockView world, int x, int y, int z) {
		return threadLocal(world, x, y, z, null, null);
	}

	static BlockNeighbors threadLocal(BlockView world, BlockPos pos, ModelStateFunction stateFunc, BlockTest<?> test) {
		return threadLocal(world, pos.getX(), pos.getY(), pos.getZ(), stateFunc, test);
	}

	static BlockNeighbors threadLocal(BlockView world, BlockPos pos, ModelStateFunction stateFunc) {
		return threadLocal(world, pos, stateFunc, null);
	}

	static BlockNeighbors threadLocal(BlockView world, BlockPos pos, BlockTest<?> test) {
		return threadLocal(world, pos, null, test);
	}

	static BlockNeighbors threadLocal(BlockView world, BlockPos pos) {
		return threadLocal(world, pos, null, null);
	}

	static BlockNeighbors claim(BlockView world, int x, int y, int z, ModelStateFunction stateFunc, BlockTest<?> test) {
		return BlocksNeighborsImpl.claim(world, x, y, z, stateFunc, test);
	}

	static BlockNeighbors claim(BlockView world, int x, int y, int z, ModelStateFunction stateFunc) {
		return claim(world, x, y, z, stateFunc, null);
	}

	static BlockNeighbors claim(BlockView world, int x, int y, int z, BlockTest<?> test) {
		return claim(world, x, y, z, null, test);
	}

	static BlockNeighbors claim(BlockView world, int x, int y, int z) {
		return claim(world, x, y, z, null, null);
	}

	static BlockNeighbors claim(BlockView world, BlockPos pos, ModelStateFunction stateFunc, BlockTest<?> test) {
		return claim(world, pos.getX(), pos.getY(), pos.getZ(), stateFunc, test);
	}

	static BlockNeighbors claimIfNull(BlockNeighbors neighbors, BlockView world, BlockPos pos, ModelStateFunction stateFunc, BlockTest<?> test) {
		return neighbors == null ? claim(world, pos.getX(), pos.getY(), pos.getZ(), stateFunc, test) : neighbors.withTest(test);
	}

	static BlockNeighbors claimIfNull(BlockNeighbors neighbors, BlockView world, BlockPos pos, ModelStateFunction stateFunc, BlockTest<PrimitiveState> test, BlockState blockState) {
		return neighbors == null ? claim(world, pos.getX(), pos.getY(), pos.getZ(), stateFunc, test).withBlockState(blockState) : neighbors.withTest(test);
	}

	static BlockNeighbors claim(BlockView world, BlockPos pos, ModelStateFunction stateFunc) {
		return claim(world, pos, stateFunc, null);
	}

	static BlockNeighbors claim(BlockView world, BlockPos pos, BlockTest<?> test) {
		return claim(world, pos, null, test);
	}

	static BlockNeighbors claim(BlockView world, BlockPos pos) {
		return claim(world, pos, null, null);
	}
}
