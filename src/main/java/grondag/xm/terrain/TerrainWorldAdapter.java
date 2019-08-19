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
package grondag.xm.terrain;

import static it.unimi.dsi.fastutil.HashCommon.arraySize;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.function.Supplier;

import org.apiguardian.api.API;

import grondag.fermion.position.PackedBlockPos;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ExtendedBlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

/**
 * Caches expensive world state lookups until next prepare() call. For
 * server-side use on server thread only. Doesn't try to track state changes
 * while not in active use, and all state changes must be single threaded and
 * occur through this instance.
 * 
 * TODO: add caching for flow height - do with as part of SuperBlockState TODO:
 * reinstate usage or remove
 */
@API(status = INTERNAL)
public class TerrainWorldAdapter implements ExtendedBlockView {
    protected World world;

    @SuppressWarnings("serial")

    public static class FastMap<V> extends Long2ObjectOpenHashMap<V> {
        /**
         * DOES NOT SUPPORT ZERO-VALUED KEYS
         * <p>
         * 
         * Only computes key 1x and scans arrays 1x for a modest savings. Here because
         * block updates are the on-tick performance bottleneck for lava sim.
         */
        private V computeFast(final long k, final Supplier<V> v) {
            final long[] key = this.key;
            int pos = (int) it.unimi.dsi.fastutil.HashCommon.mix(k) & mask;
            long curr = key[pos];

            // The starting point.
            if (curr != 0) {
                if (curr == k)
                    return value[pos];
                while (!((curr = key[pos = (pos + 1) & mask]) == (0)))
                    if (curr == k)
                        return value[pos];
            }

            final V result = v.get();
            key[pos] = k;
            value[pos] = result;
            if (size++ >= maxFill)
                rehash(arraySize(size + 1, f));
            return result;
        }
    }

    protected FastMap<BlockState> blockStates = new FastMap<>();
    protected FastMap<TerrainState> terrainStates = new FastMap<>();

    public TerrainWorldAdapter() {

    }

    public TerrainWorldAdapter(World world) {
        this.prepare(world);
    }

    public void prepare(World world) {
        this.world = world;
        this.blockStates.clear();
        this.terrainStates.clear();
    }

    public World wrapped() {
        return this.world;
    }

    @Override
    public BlockState getBlockState(final BlockPos pos) {
        long packedBlockPos = PackedBlockPos.pack(pos);
        return blockStates.computeFast(packedBlockPos, () -> world.getBlockState(pos));
    }

    private final BlockPos.Mutable getBlockPos = new BlockPos.Mutable();

    public BlockState getBlockState(long packedBlockPos) {
        return blockStates.computeFast(packedBlockPos, () -> {
            PackedBlockPos.unpackTo(packedBlockPos, getBlockPos);
            return world.getBlockState(getBlockPos);
        });
    }

    private final BlockPos.Mutable getTerrainPos = new BlockPos.Mutable();

    public TerrainState terrainState(BlockState state, long packedBlockPos) {
        return terrainStates.computeFast(packedBlockPos, () -> {
            PackedBlockPos.unpackTo(packedBlockPos, getTerrainPos);
            return TerrainState.terrainState(this, state, getTerrainPos);
        });
    }

    public TerrainState terrainState(BlockState state, BlockPos pos) {
        return terrainStates.computeFast(PackedBlockPos.pack(pos), () -> {
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
        BlockState oldState = getBlockState(packedBlockPos);

        if (newState == oldState)
            return;

        blockStates.put(packedBlockPos, newState);
        applyBlockState(packedBlockPos, oldState, newState);

        if (callback)
            this.onBlockStateChange(packedBlockPos, oldState, newState);
    }

    /**
     * Handles application of block state to world. Override for deferred updates or
     * use cases where world should not be affected directly.
     */
    protected void applyBlockState(long packedBlockPos, BlockState oldState, BlockState newState) {
        world.setBlockState(PackedBlockPos.unpack(packedBlockPos), newState);
    }

    /**
     * Called for all block state changes, even if not a terrain block.
     */
    protected void onBlockStateChange(long packedBlockPos, BlockState oldBlockState, BlockState newBlockState) {

    }

    public void setBlockState(BlockPos blockPos, BlockState newState) {
        this.setBlockState(PackedBlockPos.pack(blockPos), newState);
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
    public Biome getBiome(BlockPos var1) {
        return world.getBiome(var1);
    }

    @Override
    public int getLightLevel(LightType var1, BlockPos var2) {
        return world.getLightLevel(var1, var2);
    }
}
