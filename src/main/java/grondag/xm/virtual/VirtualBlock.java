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

package grondag.xm.virtual;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import grondag.xm.api.modelstate.ModelState;

@Internal
public interface VirtualBlock {
	/**
	 * True if block at the given position is actually solid (not replaceable) or is
	 * virtual and visible to the given player.
	 */
	default boolean isVirtuallySolid(BlockPos pos, Player player) {
		return !player.level.getBlockState(pos).getMaterial().isReplaceable();
	}

	/**
	 * Convenience call for {@link #isVirtual()} when you don't know what type of
	 * block it is. Will return false for any block that doesn't implement
	 * ISuperBlock.
	 */
	static boolean isVirtualBlock(Block block) {
		return block instanceof VirtualBlock;
	}

	/**
	 * Convenience call for
	 * {@link #isVirtuallySolid(ExtendedBlockView, BlockPos, EntityPlayer)} when you
	 * don't know what type of block it is. Will return false for any block that
	 * doesn't implement ISuperBlock.
	 */
	static boolean isVirtuallySolidBlock(BlockPos pos, Player player) {
		return isVirtuallySolidBlock(player.level.getBlockState(pos), pos, player);
	}

	/**
	 * Convenience call for
	 * {@link #isVirtuallySolid(ExtendedBlockView, BlockPos, EntityPlayer)} when you
	 * don't know what type of block it is. Will return negation of
	 * {@link #isReplaceable(ExtendedBlockView, BlockPos)} for any block that
	 * doesn't implement ISuperBlock.
	 */
	// UGLY: really needed? Seems redundant of isVirtuallySolid, plus have mixins  now
	static boolean isVirtuallySolidBlock(BlockState state, BlockPos pos, Player player) {
		final Block block = state.getBlock();
		return isVirtualBlock(block) ? ((VirtualBlock) block).isVirtuallySolid(pos, player) : !state.getMaterial().isReplaceable();
	}

	static BlockState findAppropriateVirtualBlock(ModelState modelState) {
		// TODO Auto-generated method stub
		return null;
	}
}
