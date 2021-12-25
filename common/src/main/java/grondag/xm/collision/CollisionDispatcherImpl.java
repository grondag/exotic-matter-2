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

package grondag.xm.collision;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import io.vram.sc.cache.ObjectSimpleLoadingCache;

import grondag.xm.api.modelstate.ModelState;

@Internal
public class CollisionDispatcherImpl {
	private static final ObjectSimpleLoadingCache<ModelState, VoxelShape> modelCache = new ObjectSimpleLoadingCache<>(
			CollisionDispatcherImpl::load, k -> k.toImmutable(), 0xFFF);

	private static final ObjectSimpleLoadingCache<VoxelVolumeKey, VoxelShape> volCache = new ObjectSimpleLoadingCache<>(
			k -> k.build(), k -> k.toImmutable(), 0xFFF);

	private static ThreadLocal<MeshVoxelizer> fastBoxGen = new ThreadLocal<>() {
		@Override
		protected MeshVoxelizer initialValue() {
			return new MeshVoxelizer();
		}
	};

	public static VoxelShape shapeFor(ModelState modelState) {
		return modelState == null ? Shapes.block() : modelCache.get(modelState.geometricState());
	}

	/**
	 * Clears the cache.
	 */
	public static void clear() {
		modelCache.clear();
		volCache.clear();
	}

	private static VoxelShape load(ModelState key) {
		final MeshVoxelizer generator = fastBoxGen.get();
		key.emitPolygons(generator);

		final VoxelVolumeKey vKey = generator.build();

		return volCache.get(vKey);
	}
}
