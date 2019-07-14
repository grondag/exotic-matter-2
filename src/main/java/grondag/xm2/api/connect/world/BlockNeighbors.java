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

package grondag.xm2.api.connect.world;

import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;

import grondag.xm2.api.connect.model.BlockCorner;
import grondag.xm2.api.connect.model.BlockEdge;
import grondag.xm2.api.connect.model.HorizontalEdge;
import grondag.xm2.api.connect.model.HorizontalFace;
import grondag.xm2.connect.BlocksNeighborsImpl;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

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
@API(status = STABLE)
public interface BlockNeighbors {
    void release();

    BlockNeighbors withTest(BlockTest blockTest);

    BlockState blockState();

    BlockState blockState(Direction face);

    BlockState blockState(BlockEdge corner);

    BlockState blockState(BlockCorner corner);

    default BlockState blockState(HorizontalFace face) {
        return blockState(face.face);
    }

    default BlockState blockState(Direction face1, Direction face2) {
        return blockState(BlockEdge.find(face1, face2));
    }

    default BlockState blockState(HorizontalEdge corner) {
        return blockState(corner.face1.face, corner.face2.face);
    }

    default BlockState blockState(Direction face1, Direction face2, Direction face3) {
        return blockState(BlockCorner.find(face1, face2, face3));
    }

    Object modelState();

    Object modelState(Direction face);

    Object modelState(BlockEdge corner);

    Object modelState(BlockCorner corner);

    default Object modelState(Direction face1, Direction face2, Direction face3) {
        return modelState(BlockCorner.find(face1, face2, face3));
    }

    default Object modelState(HorizontalFace face) {
        return modelState(face.face);
    }

    default Object modelState(Direction face1, Direction face2) {
        return modelState(BlockEdge.find(face1, face2));
    }

    default Object modelState(HorizontalEdge corner) {
        return modelState(corner.face1.face, corner.face2.face);
    }

    /** use this to override world results */
    void override(Direction face, boolean override);

    boolean result(Direction face);

    boolean result(BlockEdge corner);

    boolean result(BlockCorner corner);

    public default boolean result(Direction face1, Direction face2) {
        return result(BlockEdge.find(face1, face2));
    }

    public default boolean result(HorizontalFace face) {
        return result(face.face);
    }

    public default boolean result(HorizontalEdge corner) {
        return result(corner.face1.face, corner.face2.face);
    }

    public default boolean result(Direction face1, Direction face2, Direction face3) {
        return result(BlockCorner.find(face1, face2, face3));
    }

    public static BlockNeighbors threadLocal(BlockView world, int x, int y, int z, ModelStateFunction stateFunc, BlockTest test) {
        return BlocksNeighborsImpl.threadLocal(world, x, y, z, stateFunc, test);
    }

    public static BlockNeighbors threadLocal(BlockView world, int x, int y, int z, ModelStateFunction stateFunc) {
        return threadLocal(world, x, y, z, stateFunc, null);
    }

    public static BlockNeighbors threadLocal(BlockView world, int x, int y, int z, BlockTest test) {
        return threadLocal(world, x, y, z, null, test);
    }

    public static BlockNeighbors threadLocal(BlockView world, int x, int y, int z) {
        return threadLocal(world, x, y, z, null, null);
    }

    public static BlockNeighbors threadLocal(BlockView world, BlockPos pos, ModelStateFunction stateFunc, BlockTest test) {
        return threadLocal(world, pos.getX(), pos.getY(), pos.getZ(), stateFunc, test);
    }

    public static BlockNeighbors threadLocal(BlockView world, BlockPos pos, ModelStateFunction stateFunc) {
        return threadLocal(world, pos, stateFunc, null);
    }

    public static BlockNeighbors threadLocal(BlockView world, BlockPos pos, BlockTest test) {
        return threadLocal(world, pos, null, test);
    }

    public static BlockNeighbors threadLocal(BlockView world, BlockPos pos) {
        return threadLocal(world, pos, null, null);
    }

    public static BlockNeighbors claim(BlockView world, int x, int y, int z, ModelStateFunction stateFunc, BlockTest test) {
        return BlocksNeighborsImpl.claim(world, x, y, z, stateFunc, test);
    }

    public static BlockNeighbors claim(BlockView world, int x, int y, int z, ModelStateFunction stateFunc) {
        return claim(world, x, y, z, stateFunc, null);
    }

    public static BlockNeighbors claim(BlockView world, int x, int y, int z, BlockTest test) {
        return claim(world, x, y, z, null, test);
    }

    public static BlockNeighbors claim(BlockView world, int x, int y, int z) {
        return claim(world, x, y, z, null, null);
    }

    public static BlockNeighbors claim(BlockView world, BlockPos pos, ModelStateFunction stateFunc, BlockTest test) {
        return claim(world, pos.getX(), pos.getY(), pos.getZ(), stateFunc, test);
    }

    public static BlockNeighbors claim(BlockView world, BlockPos pos, ModelStateFunction stateFunc) {
        return claim(world, pos, stateFunc, null);
    }

    public static BlockNeighbors claim(BlockView world, BlockPos pos, BlockTest test) {
        return claim(world, pos, null, test);
    }

    public static BlockNeighbors claim(BlockView world, BlockPos pos) {
        return claim(world, pos, null, null);
    }
}
