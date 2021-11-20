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

package grondag.xm.api.collision;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@Experimental
public class CollisionShapes {
	private CollisionShapes() { }

	/**
	 * Useful for shapes that are handled as a full cube by dispatcher but aren't really.
	 */
	public static final VoxelShape CUBE_WITH_CUTOUTS;

	static {
		final double p = 1.0/16.0;
		final double q = 1 - p;

		VoxelShape shape = Shapes.box(p, p, p, q, q, q);

		shape = Shapes.or(shape, Shapes.box(0, 0, 0, p, p, 1));
		shape = Shapes.or(shape, Shapes.box(0, 0, 0, p, 1, p));
		shape = Shapes.or(shape, Shapes.box(0, 0, 0, 1, p, p));

		shape = Shapes.or(shape, Shapes.box(q, q, 0, 1, 1, 1));
		shape = Shapes.or(shape, Shapes.box(q, 0, q, 1, 1, 1));
		shape = Shapes.or(shape, Shapes.box(0, q, q, 1, 1, 1));

		shape = Shapes.or(shape, Shapes.box(q, 0, 0, 1, p, 1));
		shape = Shapes.or(shape, Shapes.box(q, 0, 0, 1, 1, p));
		shape = Shapes.or(shape, Shapes.box(0, q, 0, 1, 1, p));

		shape = Shapes.or(shape, Shapes.box(0, q, 0, p, 1, 1));
		shape = Shapes.or(shape, Shapes.box(0, 0, q, p, 1, 1));
		shape = Shapes.or(shape, Shapes.box(0, 0, q, 1, p, 1));

		CUBE_WITH_CUTOUTS = shape;
	}
}
