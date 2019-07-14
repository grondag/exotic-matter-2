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

package grondag.xm2.mesh.vertex;

import grondag.xm2.mesh.polygon.PolygonAccessor.VertexLayer;

public abstract class AbstractVertex<T extends AbstractVertex<T>> implements IMutableVertex {
    protected abstract VertexLayer<T>[] layerVertexArray();

    @SuppressWarnings("unchecked")
    @Override
    public final int getColor(int layerIndex) {
	return layerVertexArray()[layerIndex].colorGetter.get((T) this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final float getU(int layerIndex) {
	return layerVertexArray()[layerIndex].uGetter.get((T) this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final float getV(int layerIndex) {
	return layerVertexArray()[layerIndex].vGetter.get((T) this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void setColor(int layerIndex, int color) {
	layerVertexArray()[layerIndex].colorSetter.set((T) this, color);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void setUV(int layerIndex, float u, float v) {
	layerVertexArray()[layerIndex].uvSetter.set((T) this, u, v);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void setU(int layerIndex, float u) {
	layerVertexArray()[layerIndex].uSetter.set((T) this, u);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void setV(int layerIndex, float v) {
	layerVertexArray()[layerIndex].vSetter.set((T) this, v);
    }
}
