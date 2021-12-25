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

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import grondag.xm.api.modelstate.primitive.PrimitiveState;

// For masonry, true result means border IS present
@Experimental
public class MasonryHelper implements BlockTest<PrimitiveState> {
	private MasonryHelper() {
	}

	private static final ThreadLocal<MasonryHelper> POOL = ThreadLocal.withInitial(MasonryHelper::new);

	public static BlockTest<PrimitiveState> wrap(BlockTest<PrimitiveState> test) {
		final MasonryHelper result = POOL.get();
		result.test = test;
		return result;
	}

	private BlockTest<PrimitiveState> test;

	@Override
	public boolean apply(BlockTestContext<PrimitiveState> context) {
		if (context.fromModelState() == null) {
			return false;
		}

		final BlockState toBlockState = context.toBlockState();
		final BlockPos toPos = context.toPos();

		// if not a sibling, mortar if against full opaque
		if (!test.apply(context)) {
			return toBlockState.isSolidRender(context.world(), toPos);
		}

		final BlockPos fromPos = context.fromPos();

		// between siblings, only mortar on three sides of cube
		// (other sibling will do the mortar on other sides)
		return (toPos.getX() == fromPos.getX() + 1 || toPos.getY() == fromPos.getY() - 1 || toPos.getZ() == fromPos.getZ() + 1);
	}
}
