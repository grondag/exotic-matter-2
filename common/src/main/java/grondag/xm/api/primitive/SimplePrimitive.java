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

package grondag.xm.api.primitive;

import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.resources.ResourceLocation;

import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.modelstate.primitive.MutablePrimitiveState;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.orientation.api.OrientationType;
import grondag.xm.primitive.SimplePrimitiveBuilderImpl;

@Experimental
public interface SimplePrimitive extends ModelPrimitive<PrimitiveState, MutablePrimitiveState> {
	static Builder builder() {
		return SimplePrimitiveBuilderImpl.builder();
	}

	interface Builder {
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
