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
package grondag.xm.terrain;

import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.util.Identifier;

import grondag.xm.api.modelstate.base.BaseModelStateFactory;
import grondag.xm.api.primitive.base.AbstractPrimitive;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.api.terrain.TerrainModelState;
import grondag.xm.api.terrain.TerrainPrimitive;

@Internal
public abstract class AbstractTerrainPrimitive extends AbstractPrimitive<TerrainModelState, TerrainModelState.Mutable> implements TerrainPrimitive {
	protected AbstractTerrainPrimitive(Identifier id, int stateFlags, BaseModelStateFactory<TerrainModelState, TerrainModelState.Mutable> factory, Function<TerrainModelState, XmSurfaceList> surfaceFunc) {
		super(id, stateFlags, factory, surfaceFunc);
	}
}
