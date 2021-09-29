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
