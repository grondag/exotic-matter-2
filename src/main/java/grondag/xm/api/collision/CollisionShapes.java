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
package grondag.xm.api.collision;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

@Experimental
public class CollisionShapes {

	private CollisionShapes() {}

	/**
	 * Useful for shapes that are handled as a full cube by dispatcher but aren't really.
	 */
	public static final VoxelShape CUBE_WITH_CUTOUTS;

	static {

		final double p = 1.0/16.0;
		final double q = 1 - p;

		VoxelShape shape = VoxelShapes.cuboid(p, p, p, q, q, q);

		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0, p, p, 1));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0, p, 1, p));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0, 1, p, p));

		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(q, q, 0, 1, 1, 1));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(q, 0, q, 1, 1, 1));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, q, q, 1, 1, 1));

		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(q, 0, 0, 1, p, 1));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(q, 0, 0, 1, 1, p));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, q, 0, 1, 1, p));

		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, q, 0, p, 1, 1));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, q, p, 1, 1));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, q, 1, p, 1));

		CUBE_WITH_CUTOUTS = shape;
	}
}
