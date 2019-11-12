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
package grondag.xm.connect;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Function;

import org.apiguardian.api.API;

import grondag.xm.api.connect.world.BlockNeighbors;
import grondag.xm.api.connect.world.BlockTest;
import grondag.xm.api.connect.world.BlockTestContext;
import grondag.xm.api.connect.world.ModelStateFunction;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.orientation.CubeCorner;
import grondag.xm.api.orientation.CubeEdge;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;

/**
 * Convenient way to gather and test block states for blocks adjacent to a given
 * position. Position is immutable, blockstates are looked up lazily and values
 * are cached for reuse.
 */
@SuppressWarnings("rawtypes")
@API(status = INTERNAL)
public class BlocksNeighborsImpl implements BlockNeighbors, BlockTestContext {
    private static final int STATE_COUNT = 6 + 12 + 8;
    private static final BlockState EMPTY_BLOCK_STATE[] = new BlockState[STATE_COUNT];
    private static final BlockEntity EMPTY_BLOCK_ENTITY[] = new BlockEntity[STATE_COUNT];
    private static final ModelState EMPTY_MODEL_STATE[] = new ModelState[STATE_COUNT];
    private static final BlockEntity MISSING_BLOCK_ENTITY = new BlockEntity(null) {};

    static {
        Arrays.fill(EMPTY_BLOCK_ENTITY, MISSING_BLOCK_ENTITY);
    }

    private static ThreadLocal<BlocksNeighborsImpl> THREADLOCAL = ThreadLocal.withInitial(BlocksNeighborsImpl::new);

    public static BlockNeighbors threadLocal(BlockView world, int x, int y, int z, ModelStateFunction stateFunc, BlockTest<?> blockTest) {
        return THREADLOCAL.get().prepare(world, x, y, z, stateFunc, blockTest);
    }

    private static final ArrayBlockingQueue<BlocksNeighborsImpl> POOL = new ArrayBlockingQueue<>(64);

    public static BlocksNeighborsImpl claim(BlockView world, int x, int y, int z, ModelStateFunction stateFunc, BlockTest<?> blockTest) {
        BlocksNeighborsImpl result = POOL.poll();
        if (result == null) {
            result = new BlocksNeighborsImpl();
        }
        result.allowReclaim = true;
        return result.prepare(world, x, y, z, stateFunc, blockTest);
    }

    private static void release(BlocksNeighborsImpl instance) {
        if (instance.allowReclaim) {
            instance.allowReclaim = false;
            POOL.offer(instance);
        }
    }

    private final BlockState blockStates[] = new BlockState[STATE_COUNT];
    private final BlockEntity blockEntities[] = new BlockEntity[STATE_COUNT];
    private final ModelState modelStates[] = new ModelState[STATE_COUNT];
    private final BlockPos.Mutable mutablePos = new BlockPos.Mutable();

    private boolean allowReclaim = false;

    private int completionFlags = 0;
    private int resultFlags = 0;

    private BlockView world;
    private int x;
    private int y;
    private int z;
    private ModelStateFunction stateFunc;

    private BlockTest blockTest;
    private BlockState myBlockState;
    private BlockEntity myBlockEntity = MISSING_BLOCK_ENTITY;
    private final BlockPos.Mutable myPos = new BlockPos.Mutable();
    private ModelState myModelState;


    protected BlocksNeighborsImpl() {
    }

    BlocksNeighborsImpl prepare(BlockView world, int x, int y, int z, ModelStateFunction stateFunc, BlockTest blockTest) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        myPos.set(x, y, z);
        this.stateFunc = stateFunc;
        this.blockTest = blockTest;
        myBlockState = null;
        myBlockEntity = MISSING_BLOCK_ENTITY;
        myModelState = null;
        completionFlags = blockTest == null ? -1 : 0;
        resultFlags = 0;
        System.arraycopy(EMPTY_BLOCK_STATE, 0, blockStates, 0, STATE_COUNT);
        System.arraycopy(EMPTY_BLOCK_ENTITY, 0, blockEntities, 0, STATE_COUNT);
        System.arraycopy(EMPTY_MODEL_STATE, 0, modelStates, 0, STATE_COUNT);
        return this;
    }

    @Override
    public void release() {
        release(this);
    }

    //////////////////////////////
    // POSITION
    //////////////////////////////

    private void setPos(BlockPos.Mutable pos, Direction face) {
        final Vec3i vec = face.getVector();
        pos.set(x + vec.getX(), y + vec.getY(), z + vec.getZ());
    }

    private void setPos(BlockPos.Mutable pos, CubeEdge edge) {
        final Vec3i vec = edge.vector;
        pos.set(x + vec.getX(), y + vec.getY(), z + vec.getZ());
    }

    private void setPos(BlockPos.Mutable pos, CubeCorner corner) {
        final Vec3i vec = corner.vector;
        pos.set(x + vec.getX(), y + vec.getY(), z + vec.getZ());
    }

    //////////////////////////////
    // BLOCK STATE
    //////////////////////////////

    @Override
    public BlockState blockState(Direction face) {
        BlockState result = blockStates[face.ordinal()];
        if (result == null) {
            setPos(mutablePos, face);
            result = world.getBlockState(mutablePos);
            blockStates[face.ordinal()] = result;
        }
        return result;
    }

    @Override
    public BlockState blockState() {
        BlockState result = myBlockState;
        if (result == null) {
            result = world.getBlockState(mutablePos.set(x, y, z));
            myBlockState = result;
        }
        return result;
    }

    @Override
    public BlockState blockState(CubeEdge edge) {
        BlockState result = blockStates[edge.superOrdinal];
        if (result == null) {
            setPos(mutablePos, edge);
            result = world.getBlockState(mutablePos);
            blockStates[edge.superOrdinal] = result;
        }
        return result;
    }

    @Override
    public BlockState blockState(CubeCorner corner) {
        BlockState result = blockStates[corner.superOrdinal];
        if (result == null) {
            setPos(mutablePos, corner);
            result = world.getBlockState(mutablePos);
            blockStates[corner.superOrdinal] = result;
        }
        return result;
    }

    //////////////////////////////
    // BLOCK ENTITY
    //////////////////////////////

    @Override
    public BlockEntity blockEntity(Direction face) {
        BlockEntity result = blockEntities[face.ordinal()];
        if (result == MISSING_BLOCK_ENTITY) {
            setPos(mutablePos, face);
            result = world.getBlockEntity(mutablePos);
            blockEntities[face.ordinal()] = result;
        }
        return result;
    }

    @Override
    public BlockEntity blockEntity() {
        BlockEntity result = myBlockEntity;
        if (result == MISSING_BLOCK_ENTITY) {
            result = world.getBlockEntity(mutablePos.set(x, y, z));
            myBlockEntity = result;
        }
        return result;
    }

    @Override
    public BlockEntity blockEntity(CubeEdge edge) {
        BlockEntity result = blockEntities[edge.superOrdinal];
        if (result == MISSING_BLOCK_ENTITY) {
            setPos(mutablePos, edge);
            result = world.getBlockEntity(mutablePos);
            blockEntities[edge.superOrdinal] = result;
        }
        return result;
    }

    @Override
    public BlockEntity blockEntity(CubeCorner corner) {
        BlockEntity result = blockEntities[corner.superOrdinal];
        if (result == MISSING_BLOCK_ENTITY) {
            setPos(mutablePos, corner);
            result = world.getBlockEntity(mutablePos);
            blockEntities[corner.superOrdinal] = result;
        }
        return result;
    }

    //////////////////////////////
    // MODEL STATE
    //////////////////////////////

    @Override
    public ModelState modelState() {
        if (stateFunc == null)
            return null;

        ModelState result = myModelState;
        if (result == null) {
            final BlockState state = this.blockState();
            mutablePos.set(x, y, z);
            result = stateFunc.get(world, state, mutablePos);
            myModelState = result;
        }
        return result;
    }

    @Override
    public ModelState modelState(Direction face) {
        if (stateFunc == null)
            return null;

        ModelState result = modelStates[face.ordinal()];
        if (result == null) {
            final BlockState state = this.blockState(face);
            setPos(mutablePos, face);
            result = stateFunc.get(world, state, mutablePos);
            modelStates[face.ordinal()] = result;
        }
        return result;
    }

    @Override
    public ModelState modelState(CubeEdge edge) {
        if (stateFunc == null)
            return null;

        ModelState result = modelStates[edge.superOrdinal];
        if (result == null) {
            final BlockState state = blockState(edge);
            setPos(mutablePos, edge);
            result = stateFunc.get(world, state, mutablePos);
            modelStates[edge.superOrdinal] = result;
        }
        return result;
    }

    @Override
    public ModelState modelState(CubeCorner corner) {
        if (stateFunc == null)
            return null;

        ModelState result = modelStates[corner.superOrdinal];
        if (result == null) {
            final BlockState state = blockState(corner);
            setPos(mutablePos, corner);
            result = stateFunc.get(world, state, mutablePos);
            modelStates[corner.superOrdinal] = result;
        }
        return result;
    }

    //////////////////////////
    // TESTS
    //////////////////////////

    @Override
    public BlocksNeighborsImpl withTest(BlockTest blockTest) {
        this.blockTest = blockTest;
        completionFlags = 0;
        resultFlags = 0;
        return this;
    }

    // valid during tests - the "to" values
    private final BlockPos.Mutable targetPos = new BlockPos.Mutable();
    private Function<BlocksNeighborsImpl, BlockState> targetBlockState;
    private Function<BlocksNeighborsImpl, ModelState> targetModelState;
    private Function<BlocksNeighborsImpl, BlockEntity> targetBlockEntity;
    private Object targetLocation;

    private static final Function<BlocksNeighborsImpl, BlockState> FACE_BLOCKSTATE = bn -> bn.blockState((Direction)bn.targetLocation);
    private static final Function<BlocksNeighborsImpl, BlockState> EDGE_BLOCKSTATE = bn -> bn.blockState((CubeEdge)bn.targetLocation);
    private static final Function<BlocksNeighborsImpl, BlockState> CORNER_BLOCKSTATE = bn -> bn.blockState((CubeCorner)bn.targetLocation);

    private static final Function<BlocksNeighborsImpl, BlockEntity> FACE_BLOCKENTITY = bn -> bn.blockEntity((Direction)bn.targetLocation);
    private static final Function<BlocksNeighborsImpl, BlockEntity> EDGE_BLOCKENTITY = bn -> bn.blockEntity((CubeEdge)bn.targetLocation);
    private static final Function<BlocksNeighborsImpl, BlockEntity> CORNER_BLOCKENTITY = bn -> bn.blockEntity((CubeCorner)bn.targetLocation);

    private static final Function<BlocksNeighborsImpl, ModelState> FACE_MODELSTATE = bn -> bn.modelState((Direction)bn.targetLocation);
    private static final Function<BlocksNeighborsImpl, ModelState> EDGE_MODELSTATE = bn -> bn.modelState((CubeEdge)bn.targetLocation);
    private static final Function<BlocksNeighborsImpl, ModelState> CORNER_MODELSTATE = bn -> bn.modelState((CubeCorner)bn.targetLocation);

    @SuppressWarnings("unchecked")
    private boolean doTest(Direction face) {
        targetLocation = face;
        targetModelState = FACE_MODELSTATE;
        targetBlockState = FACE_BLOCKSTATE;
        targetBlockEntity = FACE_BLOCKENTITY;
        setPos(targetPos, face);
        return blockTest.apply(this);
    }

    @SuppressWarnings("unchecked")
    private boolean doTest(CubeEdge edge) {
        targetLocation = edge;
        targetModelState = EDGE_MODELSTATE;
        targetBlockState = EDGE_BLOCKSTATE;
        targetBlockEntity = EDGE_BLOCKENTITY;
        setPos(targetPos, edge);
        return blockTest.apply(this);
    }

    @SuppressWarnings("unchecked")
    private boolean doTest(CubeCorner corner) {
        targetLocation = corner;
        targetModelState = CORNER_MODELSTATE;
        targetBlockState = CORNER_BLOCKSTATE;
        targetBlockEntity = CORNER_BLOCKENTITY;
        setPos(targetPos, corner);
        return blockTest.apply(this);
    }

    @Override
    public void override(Direction face, boolean override) {
        if (face == null)
            return;

        final int bitFlag = 1 << face.ordinal();
        completionFlags |= bitFlag;
        if (override) {
            resultFlags |= bitFlag;
        } else {
            resultFlags &= ~bitFlag;
        }
    }

    @Override
    public boolean result(Direction face) {
        if (face == null)
            return false;

        final int bitFlag = 1 << face.ordinal();
        if ((completionFlags & bitFlag) != bitFlag) {
            if (doTest(face)) {
                resultFlags |= bitFlag;
            }
            completionFlags |= bitFlag;
        }
        return (resultFlags & bitFlag) == bitFlag;
    }

    @Override
    public boolean result(CubeEdge edge) {
        if (edge == null)
            return false;

        if ((completionFlags & edge.superOrdinalBit) != edge.superOrdinalBit) {
            if (doTest(edge)) {
                resultFlags |= edge.superOrdinalBit;
            }
            completionFlags |= edge.superOrdinalBit;
        }
        return (resultFlags & edge.superOrdinalBit) == edge.superOrdinalBit;
    }

    @Override
    public boolean result(CubeCorner corner) {
        if (corner == null)
            return false;

        if ((completionFlags & corner.superOrdinalBit) != corner.superOrdinalBit) {
            if (doTest(corner)) {
                resultFlags |= corner.superOrdinalBit;
            }
            completionFlags |= corner.superOrdinalBit;
        }
        return (resultFlags & corner.superOrdinalBit) == corner.superOrdinalBit;
    }

    @Override
    public BlockView world() {
        return world;
    }

    @Override
    public BlockPos fromPos() {
        return myPos;
    }

    @Override
    public BlockState fromBlockState() {
        return blockState();
    }

    @Override
    public BlockEntity fromBlockEntity() {
        return blockEntity();
    }

    @Override
    public ModelState fromModelState() {
        return modelState();
    }

    @Override
    public BlockPos toPos() {
        return targetPos;
    }

    @Override
    public BlockState toBlockState() {
        return targetBlockState.apply(this);
    }

    @Override
    public BlockEntity toBlockEntity() {
        return targetBlockEntity.apply(this);
    }

    @Override
    public ModelState toModelState() {
        return targetModelState.apply(this);
    }
}
