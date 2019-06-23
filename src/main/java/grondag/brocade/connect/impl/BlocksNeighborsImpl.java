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

package grondag.brocade.connect.impl;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.concurrent.ArrayBlockingQueue;

import org.apiguardian.api.API;

import grondag.brocade.connect.api.model.BlockEdge;
import grondag.brocade.connect.api.world.BlockNeighbors;
import grondag.brocade.connect.api.world.BlockTest;
import grondag.brocade.connect.api.world.BlockTestContext;
import grondag.brocade.connect.api.world.ModelStateFunction;
import grondag.brocade.connect.api.model.BlockCorner;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;

/**
 * Convenient way to gather and test block states for blocks adjacent to a given
 * position. Position is immutable, blockstates are looked up lazily and values
 * are cached for reuse.
 */
@API(status = INTERNAL)
public class BlocksNeighborsImpl implements BlockNeighbors, BlockTestContext {
    private static final int STATE_COUNT = 6 + 12 + 8;
    private static final BlockState EMPTY_BLOCK_STATE[] = new BlockState[STATE_COUNT];
    private static final Object EMPTY_MODEL_STATE[] = new Object[STATE_COUNT];
    
    private static ThreadLocal<BlocksNeighborsImpl> THREADLOCAL = ThreadLocal.withInitial(BlocksNeighborsImpl::new);
    
    public static BlockNeighbors threadLocal(BlockView world, int x, int y, int z, ModelStateFunction stateFunc, BlockTest blockTest) {
        return THREADLOCAL.get().prepare(world, x, y, z, stateFunc, blockTest);
    }
    
    private static final ArrayBlockingQueue<BlocksNeighborsImpl> POOL = new ArrayBlockingQueue<>(64);
    
    public static BlocksNeighborsImpl claim(BlockView world, int x, int y, int z, ModelStateFunction stateFunc, BlockTest blockTest) {
        BlocksNeighborsImpl result = POOL.poll();
        if (result == null) {
            result = new BlocksNeighborsImpl();
        }
        result.allowReclaim = true;
        return result.prepare(world, x, y, z, stateFunc, blockTest);
    }
    
    private static void release(BlocksNeighborsImpl instance) {
        if(instance.allowReclaim) {
            instance.allowReclaim = false;
            POOL.offer(instance);
        }
    }
    
    private final BlockState blockStates[] = new BlockState[STATE_COUNT];
    private final Object modelStates[] = new Object[STATE_COUNT];
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
    private final BlockPos.Mutable myPos = new BlockPos.Mutable();
    private Object myModelState;
    
    // valid during tests - the "to" values
    private final BlockPos.Mutable targetPos = new BlockPos.Mutable();
    private BlockState targetBlockState = Blocks.AIR.getDefaultState();
    private Object targetModelState = null;
            
    protected BlocksNeighborsImpl () {
    }
    
    BlocksNeighborsImpl prepare(BlockView world, int x, int y, int z, ModelStateFunction stateFunc, BlockTest blockTest) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        myPos.set(x, y, z);
        this.stateFunc = stateFunc;
        this.blockTest = blockTest;
        completionFlags = blockTest == null ? -1 : 0;
        resultFlags = 0;
        System.arraycopy(EMPTY_BLOCK_STATE, 0, blockStates, 0, STATE_COUNT);
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
    
    private void setPos(BlockPos.Mutable pos, BlockEdge edge) {
        final Vec3i vec = edge.vector;
        pos.set(x + vec.getX(), y + vec.getY(), z + vec.getZ());
    }
    
    private void setPos(BlockPos.Mutable pos, BlockCorner corner) {
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
        BlockState result = this.myBlockState;
        if (result == null) {
            result = world.getBlockState(mutablePos.set(x, y, z));
            this.myBlockState = result;
        }
        return result;
    }

    @Override
    public BlockState blockState(BlockEdge edge) {
        BlockState result = blockStates[edge.superOrdinal];
        if (result == null) {
            setPos(mutablePos, edge);
            result = world.getBlockState(mutablePos);
            blockStates[edge.superOrdinal] = result;
        }
        return result;
    }

    @Override
    public BlockState blockState(BlockCorner corner) {
        BlockState result = blockStates[corner.superOrdinal];
        if (result == null) {
            setPos(mutablePos, corner);
            result = world.getBlockState(mutablePos);
            blockStates[corner.superOrdinal] = result;
        }
        return result;
    }

    //////////////////////////////
    // MODEL STATE
    //////////////////////////////

    @Override
    public Object modelState() {
        if(this.stateFunc == null) 
            return null;
        
        Object result = this.myModelState;
        if (result == null) {
            BlockState state = this.blockState();
            mutablePos.set(x, y, z);
            result = this.stateFunc.get(this.world, state, mutablePos);
            this.myModelState = result;
        }
        return result;
    }
    
    @Override
    public Object modelState(Direction face) {
        if(this.stateFunc == null) 
            return null;
        
        Object result = modelStates[face.ordinal()];
        if (result == null) {
            BlockState state = this.blockState(face);
            setPos(mutablePos, face);
            result = this.stateFunc.get(this.world, state, mutablePos);
            modelStates[face.ordinal()] = result;
        }
        return result;
    }

    @Override
    public Object modelState(BlockEdge edge) {
        if(this.stateFunc == null) 
            return null;
        
        Object result = modelStates[edge.superOrdinal];
        if (result == null) {
            BlockState state = blockState(edge);
            setPos(mutablePos, edge);
            result = this.stateFunc.get(this.world, state, mutablePos);
            modelStates[edge.superOrdinal] = result;
        }
        return result;
    }

    @Override
    public Object modelState(BlockCorner corner) {
        if(this.stateFunc == null) 
            return null;
        
        Object result = modelStates[corner.superOrdinal];
        if (result == null) {
            BlockState state = blockState(corner);
            setPos(mutablePos, corner);
            result = this.stateFunc.get(this.world, state, mutablePos);
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
    
    private boolean doTest(Direction face) {
        targetModelState = stateFunc == null ? null : modelState(face);
        targetBlockState = blockState(face);
        setPos(targetPos, face);
        return blockTest.apply(this);
    }

    private boolean doTest(BlockEdge edge) {
        targetModelState = stateFunc == null ? null : modelState(edge);
        targetBlockState = blockState(edge);
        setPos(targetPos, edge);
        return blockTest.apply(this);
    }

    private boolean doTest(BlockCorner corner) {
        targetModelState = stateFunc == null ? null : modelState(corner);
        targetBlockState = blockState(corner);
        setPos(targetPos, corner);
        return blockTest.apply(this);
    }
    
    @Override
    public void override(Direction face, boolean override) {
        if(face == null) {
            return;
        }
        
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
        if(face == null) {
            return false;
        }
        
        int bitFlag = 1 << face.ordinal();
        if ((completionFlags & bitFlag) != bitFlag) {
            if (doTest(face)) {
                resultFlags |= bitFlag;
            }
            completionFlags |= bitFlag;
        }
        return (resultFlags & bitFlag) == bitFlag;
    }

    @Override
    public boolean result(BlockEdge edge) {
        if(edge == null) {
            return false;
        }
        
        if ((completionFlags & edge.superOrdinalBit) != edge.superOrdinalBit) {
            if (doTest(edge)) {
                resultFlags |= edge.superOrdinalBit;
            }
            completionFlags |= edge.superOrdinalBit;
        }
        return (resultFlags & edge.superOrdinalBit) == edge.superOrdinalBit;
    }

    @Override
    public boolean result(BlockCorner corner) {
        if(corner == null) {
            return false;
        }
        
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
    public Object fromModelState() {
        return modelState();
    }

    @Override
    public BlockPos toPos() {
        return targetPos;
    }

    @Override
    public BlockState toBlockState() {
        return targetBlockState;
    }

    @Override
    public Object toModelState() {
        return targetModelState;
    }
}
