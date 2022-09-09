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

package grondag.xm.api.connect.world;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

import grondag.xm.api.modelstate.primitive.PrimitiveState;

public abstract class BlockConnectors {
	private BlockConnectors() { }

	private static final Object2ObjectOpenHashMap<Block, ImmutableSet<Block>> SETS = new Object2ObjectOpenHashMap<>();

	public static void connect(Block... blocks) {
		final ImmutableSet<Block> set = ImmutableSet.copyOf(blocks);

		for (final Block b : blocks) {
			SETS.put(b, set);
		}
	}

	public static boolean canConnect(Block a, Block b) {
		final ImmutableSet<Block> set = SETS.get(a);
		return set != null && set.contains(b);
	}

	public static BlockTest<PrimitiveState> SAME_BLOCK_OR_CONNECTABLE = ctx -> {
		final Block a = ctx.fromBlockState().getBlock();
		final Block b = ctx.toBlockState().getBlock();
		return a == b || canConnect(a, b);
	};

	public static BlockTest<PrimitiveState> AXIS_JOIN_SAME_BLOCK = ctx -> {
		// must be an axis block, obviously.
		final BlockState fromBlock = ctx.fromBlockState();

		if (!fromBlock.hasProperty(RotatedPillarBlock.AXIS)) {
			return false;
		}

		// must be same block
		final BlockState toBlock = ctx.toBlockState();

		if (fromBlock.getBlock() != toBlock.getBlock()) {
			return false;
		}

		// must be same axis
		final Axis axis = fromBlock.getValue(RotatedPillarBlock.AXIS);

		if (axis != toBlock.getValue(RotatedPillarBlock.AXIS)) {
			return false;
		}

		// must be adjacent along that axis
		final BlockPos fromPos = ctx.fromPos();
		final BlockPos toPos = ctx.toPos();
		final int dist = axis.choose(fromPos.getX(), fromPos.getY(), fromPos.getZ())
				- axis.choose(toPos.getX(), toPos.getY(), toPos.getZ());
		return Math.abs(dist) == 1;
	};

	public static BlockTest<PrimitiveState> AXIS_JOIN_SAME_OR_CONNECTABLE = ctx -> {
		// must be an axis block, obviously.
		final BlockState fromBlock = ctx.fromBlockState();

		if (!fromBlock.hasProperty(RotatedPillarBlock.AXIS)) {
			return false;
		}

		// must be same block or connectable
		final BlockState toBlock = ctx.toBlockState();
		final Block a = fromBlock.getBlock();
		final Block b = toBlock.getBlock();

		if (a != b && !canConnect(a, b)) {
			return false;
		}

		// must be same axis
		final Axis axis = fromBlock.getValue(RotatedPillarBlock.AXIS);

		if (axis != toBlock.getValue(RotatedPillarBlock.AXIS)) {
			return false;
		}

		// must be adjacent along that axis
		final BlockPos fromPos = ctx.fromPos();
		final BlockPos toPos = ctx.toPos();
		final int dist = axis.choose(fromPos.getX(), fromPos.getY(), fromPos.getZ())
				- axis.choose(toPos.getX(), toPos.getY(), toPos.getZ());
		return Math.abs(dist) == 1;
	};
}
