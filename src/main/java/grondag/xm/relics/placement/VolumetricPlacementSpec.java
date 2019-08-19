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
package grondag.xm.relics.placement;

import java.util.HashSet;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fermion.position.CubicBlockRegion;
import grondag.xm.XmConfig;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@API(status = Status.DEPRECATED)
@Deprecated
abstract class VolumetricPlacementSpec extends SingleStackPlacementSpec {
    protected final boolean isHollow;
    protected final BlockPos offsetPos;
    /**
     * Reference this instead of
     * {@link IPlacementItem#isFixedRegionEnabled(ItemStack)} because stack property
     * is typically reset after builder is instantiated.
     */
    protected final boolean isFixedRegion;
    protected final boolean isAdjustmentEnabled;

    protected VolumetricPlacementSpec(ItemStack placedStack, PlayerEntity player, PlacementPosition pPos) {
        super(placedStack, player, pPos);
        this.isHollow = this.selectionMode == TargetMode.HOLLOW_REGION;
        this.offsetPos = this.placementItem.getRegionSize(placedStack, true);
        this.isFixedRegion = this.placementItem.isFixedRegionEnabled(placedStack);
        this.isAdjustmentEnabled = !this.isFixedRegion && !this.isExcavation && !this.isSelectionInProgress
                && this.placementItem.getRegionOrientation(placedStack) == RegionOrientation.AUTOMATIC;
    }

    /**
     * Clears the exclusion list in the given block region and adds obstacles
     * checked within the region to the exclusion list. Does not fully validate
     * region - is intended for preview use only.
     * <p>
     * Stops checking after finding 16 obstacles. Checks are only performed if the
     * selection mode is <code>COMPLETE_REGION</code> because otherwise the
     * placement cannot be prevented by obstructions.
     * 
     * @param region
     */
    protected void excludeObstaclesInRegion(CubicBlockRegion region) {
        region.clearExclusions();

        if (this.selectionMode != TargetMode.COMPLETE_REGION)
            return;

        HashSet<BlockPos> set = new HashSet<BlockPos>();

        World world = this.player.world;

        int checkCount = 0, foundCount = 0;

        if (this.isExcavation) {
            for (BlockPos pos : region.positions()) {
                if (!World.isValid(pos)) {
                    set.add(pos.toImmutable());
                    if (foundCount++ == 16)
                        break;
                } else {
                    BlockState blockState = world.getBlockState(pos);
                    if (blockState.isAir() || !this.effectiveFilterMode.shouldAffectBlock(blockState, world, pos, this.placedStack(), this.isVirtual)) {
                        set.add(pos.toImmutable());
                        if (foundCount++ == 16)
                            break;
                    }
                }
                if (checkCount++ >= XmConfig.BLOCKS.maxPlacementCheckCount)
                    break;
            }
        } else {
            for (BlockPos pos : region.includedPositions()) {
                if (!World.isValid(pos)) {
                    set.add(pos.toImmutable());
                    if (foundCount++ == 16)
                        break;
                } else {
                    BlockState blockState = world.getBlockState(pos);
                    if (!this.effectiveFilterMode.shouldAffectBlock(blockState, world, pos, this.placedStack(), this.isVirtual)) {
                        set.add(pos.toImmutable());
                        if (foundCount++ == 16)
                            break;
                    }
                }
                if (checkCount++ >= XmConfig.BLOCKS.maxPlacementCheckCount)
                    break;
            }
        }
        region.exclude(set);
    }

    /**
     * Returns true if the region has no obstacles or obstacles do not matter. Must
     * call AFTER {@link #excludeObstaclesInRegion(CubicBlockRegion)} or result will
     * be meaningless.
     */
    protected boolean canPlaceRegion(CubicBlockRegion region) {
        return region.exclusions().isEmpty() || this.selectionMode != TargetMode.COMPLETE_REGION;
    }
}
