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

package grondag.xm.connect;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import grondag.fermion.orientation.api.CubeCorner;
import grondag.fermion.orientation.api.CubeEdge;
import grondag.xm.Xm;
import grondag.xm.api.connect.world.BlockNeighbors;
import grondag.xm.api.connect.world.BlockTest;
import grondag.xm.api.connect.world.BlockTestContext;
import grondag.xm.api.connect.world.ModelStateFunction;
import grondag.xm.api.modelstate.ModelState;

/**
 * Convenient way to gather and test block states for blocks adjacent to a given
 * position. Position is immutable, blockstates are looked up lazily and values
 * are cached for reuse.
 */
@SuppressWarnings("rawtypes")
@Internal
public class BlocksNeighborsImpl implements BlockNeighbors, BlockTestContext {
	private static final int STATE_COUNT = 6 + 12 + 8;
	private static final BlockState[] EMPTY_BLOCK_STATE = new BlockState[STATE_COUNT];
	private static final BlockEntity[] EMPTY_BLOCK_ENTITY = new BlockEntity[STATE_COUNT];
	private static final ModelState[] EMPTY_MODEL_STATE = new ModelState[STATE_COUNT];
	private static final BlockEntity MISSING_BLOCK_ENTITY = new BlockEntity(BlockEntityType.STRUCTURE_BLOCK, new BlockPos(0, 0, 0), Blocks.AIR.defaultBlockState()) { };

	static {
		Arrays.fill(EMPTY_BLOCK_ENTITY, MISSING_BLOCK_ENTITY);
	}

	private static ThreadLocal<BlocksNeighborsImpl> THREADLOCAL = ThreadLocal.withInitial(BlocksNeighborsImpl::new);

	public static BlockNeighbors threadLocal(BlockGetter world, int x, int y, int z, ModelStateFunction stateFunc, BlockTest<?> blockTest) {
		return THREADLOCAL.get().prepare(world, x, y, z, stateFunc, blockTest);
	}

	private static final ArrayBlockingQueue<BlocksNeighborsImpl> POOL = new ArrayBlockingQueue<>(64);

	public static BlocksNeighborsImpl claim(BlockGetter world, int x, int y, int z, ModelStateFunction stateFunc, BlockTest<?> blockTest) {
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

	private final BlockState[] blockStates = new BlockState[STATE_COUNT];
	private final BlockEntity[] blockEntities = new BlockEntity[STATE_COUNT];
	private final ModelState[] modelStates = new ModelState[STATE_COUNT];
	private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

	private boolean allowReclaim = false;

	private int completionFlags = 0;
	private int resultFlags = 0;

	private BlockGetter world;
	private int x;
	private int y;
	private int z;
	private ModelStateFunction stateFunc;

	private BlockTest blockTest;
	private BlockState myBlockState;
	private BlockEntity myBlockEntity = MISSING_BLOCK_ENTITY;
	private final BlockPos.MutableBlockPos myPos = new BlockPos.MutableBlockPos();
	private ModelState myModelState;

	protected BlocksNeighborsImpl() { }

	BlocksNeighborsImpl prepare(BlockGetter world, int x, int y, int z, ModelStateFunction stateFunc, BlockTest blockTest) {
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
	public BlocksNeighborsImpl withBlockState(BlockState state) {
		myBlockState = state;
		return this;
	}

	@Override
	public void release() {
		release(this);
	}

	//////////////////////////////
	// POSITION
	//////////////////////////////

	private void setPos(BlockPos.MutableBlockPos pos, Direction face) {
		final Vec3i vec = face.getNormal();
		pos.set(x + vec.getX(), y + vec.getY(), z + vec.getZ());
	}

	private void setPos(BlockPos.MutableBlockPos pos, CubeEdge edge) {
		final Vec3i vec = edge.vector;
		pos.set(x + vec.getX(), y + vec.getY(), z + vec.getZ());
	}

	private void setPos(BlockPos.MutableBlockPos pos, CubeCorner corner) {
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

			// Try to catch XB #12
			try {
				result = world.getBlockState(mutablePos);
			} catch (final Exception e) {
				result = Blocks.AIR.defaultBlockState();
				Xm.LOG.warn("Unable to retrieve neighbor block state due to error. Block shape/appearance may be incorrect.", e);
			}

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
		if (stateFunc == null) return null;

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
		if (stateFunc == null) return null;

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
		if (stateFunc == null) return null;

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
		if (stateFunc == null) return null;

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
	private final BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos();
	private Function<BlocksNeighborsImpl, BlockState> targetBlockState;
	private Function<BlocksNeighborsImpl, ModelState> targetModelState;
	private Function<BlocksNeighborsImpl, BlockEntity> targetBlockEntity;
	private Object targetLocation;

	private static final Function<BlocksNeighborsImpl, BlockState> FACE_BLOCKSTATE = bn -> bn.blockState((Direction) bn.targetLocation);
	private static final Function<BlocksNeighborsImpl, BlockState> EDGE_BLOCKSTATE = bn -> bn.blockState((CubeEdge) bn.targetLocation);
	private static final Function<BlocksNeighborsImpl, BlockState> CORNER_BLOCKSTATE = bn -> bn.blockState((CubeCorner) bn.targetLocation);

	private static final Function<BlocksNeighborsImpl, BlockEntity> FACE_BLOCKENTITY = bn -> bn.blockEntity((Direction) bn.targetLocation);
	private static final Function<BlocksNeighborsImpl, BlockEntity> EDGE_BLOCKENTITY = bn -> bn.blockEntity((CubeEdge) bn.targetLocation);
	private static final Function<BlocksNeighborsImpl, BlockEntity> CORNER_BLOCKENTITY = bn -> bn.blockEntity((CubeCorner) bn.targetLocation);

	private static final Function<BlocksNeighborsImpl, ModelState> FACE_MODELSTATE = bn -> bn.modelState((Direction) bn.targetLocation);
	private static final Function<BlocksNeighborsImpl, ModelState> EDGE_MODELSTATE = bn -> bn.modelState((CubeEdge) bn.targetLocation);
	private static final Function<BlocksNeighborsImpl, ModelState> CORNER_MODELSTATE = bn -> bn.modelState((CubeCorner) bn.targetLocation);

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
		if (face == null) return;

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
		if (face == null) return false;

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
		if (edge == null) return false;

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
		if (corner == null) return false;

		if ((completionFlags & corner.superOrdinalBit) != corner.superOrdinalBit) {
			if (doTest(corner)) {
				resultFlags |= corner.superOrdinalBit;
			}

			completionFlags |= corner.superOrdinalBit;
		}

		return (resultFlags & corner.superOrdinalBit) == corner.superOrdinalBit;
	}

	@Override
	public BlockGetter world() {
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

	@Override
	public Direction toFace() {
		return targetLocation instanceof Direction ? (Direction) targetLocation : null;
	}
}
