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

package grondag.xm.terrain;

import static it.unimi.dsi.fastutil.HashCommon.arraySize;

import java.util.function.Supplier;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

// TODO: add caching for flow height - do with as part of SuperBlockState
// TODO: reinstate usage or remove

/**
 * Caches expensive world state lookups until next prepare() call. For
 * server-side use on server thread only. Doesn't try to track state changes
 * while not in active use, and all state changes must be single threaded and
 * occur through this instance.
 */
@Internal
public class TerrainWorldAdapter implements BlockGetter {
	protected Level world;

	@SuppressWarnings("serial")
	public static class FastMap<V> extends Long2ObjectOpenHashMap<V> {
		/**
		 * DOES NOT SUPPORT ZERO-VALUED KEYS.
		 * Only computes key 1x and scans arrays 1x for a modest savings. Here because
		 * block updates are the on-tick performance bottleneck for lava sim.
		 */
		private V computeFast(final long k, final Supplier<V> v) {
			final long[] key = this.key;
			int pos = (int) it.unimi.dsi.fastutil.HashCommon.mix(k) & mask;
			long curr = key[pos];

			// The starting point.
			if (curr != 0) {
				if (curr == k) {
					return value[pos];
				}

				while (!((curr = key[pos = (pos + 1) & mask]) == (0)))
					if (curr == k) {
						return value[pos];
					}
			}

			final V result = v.get();
			key[pos] = k;
			value[pos] = result;

			if (size++ >= maxFill) {
				rehash(arraySize(size + 1, f));
			}

			return result;
		}
	}

	protected FastMap<BlockState> blockStates = new FastMap<>();
	protected FastMap<TerrainState> terrainStates = new FastMap<>();

	public TerrainWorldAdapter() { }

	public TerrainWorldAdapter(Level world) {
		prepare(world);
	}

	public void prepare(Level world) {
		this.world = world;
		blockStates.clear();
		terrainStates.clear();
	}

	public Level wrapped() {
		return world;
	}

	@Override
	public BlockState getBlockState(final BlockPos pos) {
		final long packedBlockPos = pos.asLong();
		return blockStates.computeFast(packedBlockPos, () -> world.getBlockState(pos));
	}

	private final BlockPos.MutableBlockPos getBlockPos = new BlockPos.MutableBlockPos();

	public BlockState getBlockState(long packedBlockPos) {
		return blockStates.computeFast(packedBlockPos, () -> world.getBlockState(getBlockPos.set(packedBlockPos)));
	}

	private final BlockPos.MutableBlockPos getTerrainPos = new BlockPos.MutableBlockPos();

	public TerrainState terrainState(BlockState state, long packedBlockPos) {
		return terrainStates.computeFast(packedBlockPos, () -> TerrainState.terrainState(this, state, getTerrainPos.set(packedBlockPos)));
	}

	public TerrainState terrainState(BlockState state, BlockPos pos) {
		return terrainStates.computeFast(pos.asLong(), () -> {
			return TerrainState.terrainState(this, state, pos);
		});
	}

	public TerrainState terrainState(long packedBlockPos) {
		return terrainState(getBlockState(packedBlockPos), packedBlockPos);
	}

	/**
	 * Note this doesn't invalidate terrain state cache. Need to do that directly
	 * before using anything that needs it if changing terrain surface.
	 */
	public void setBlockState(long packedBlockPos, BlockState newState) {
		setBlockState(packedBlockPos, newState, true);
	}

	/**
	 * Use when you want to control
	 * {@link #onBlockStateChange(long, BlockState, BlockState)} call back.
	 */
	protected void setBlockState(long packedBlockPos, BlockState newState, boolean callback) {
		final BlockState oldState = getBlockState(packedBlockPos);

		if (newState == oldState) {
			return;
		}

		blockStates.put(packedBlockPos, newState);
		applyBlockState(packedBlockPos, oldState, newState);

		if (callback) {
			onBlockStateChange(packedBlockPos, oldState, newState);
		}
	}

	/**
	 * Handles application of block state to world. Override for deferred updates or
	 * use cases where world should not be affected directly.
	 */
	protected void applyBlockState(long packedBlockPos, BlockState oldState, BlockState newState) {
		world.setBlockAndUpdate(BlockPos.of(packedBlockPos), newState);
	}

	/**
	 * Called for all block state changes, even if not a terrain block.
	 */
	protected void onBlockStateChange(long packedBlockPos, BlockState oldBlockState, BlockState newBlockState) {
		// NOOP
	}

	public void setBlockState(BlockPos blockPos, BlockState newState) {
		this.setBlockState(blockPos.asLong(), newState);
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		return world.getBlockEntity(pos);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return world.getFluidState(pos);
	}

	@Override
	public int getHeight() {
		return world.getHeight();
	}

	@Override
	public int getMinBuildHeight() {
		return world.getMinBuildHeight();
	}
}
