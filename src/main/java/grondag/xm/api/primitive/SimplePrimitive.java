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
package grondag.xm.api.primitive;

import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus.Experimental;
import grondag.fermion.orientation.api.OrientationType;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.modelstate.primitive.MutablePrimitiveState;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.primitive.SimplePrimitiveBuilderImpl;

@Experimental
public interface SimplePrimitive extends ModelPrimitive<PrimitiveState, MutablePrimitiveState>{
	static Builder builder() {
		return SimplePrimitiveBuilderImpl.builder();
	}

	public interface Builder {
		default SimplePrimitive build(ResourceLocation id) {
			return build(id);
		}

		Builder surfaceList(XmSurfaceList list);

		Builder primitiveBitCount(int bitCount);

		Builder orientationType(OrientationType orientationType);

		Builder polyFactory(Function<PrimitiveState, XmMesh> polyFactory);

		Builder simpleJoin(boolean needsJoin);

		Builder cornerJoin(boolean needsJoin);

		Builder axisJoin(boolean needsJoin);

		Builder alternateJoinAffectsGeometry(boolean alternateJoinAffectsGeometry);

	}
}
