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

package grondag.xm2.mesh.polygon;

import grondag.xm2.mesh.vertex.Vec3f;

public class PolygonAccessor {
    // UGLY: these interfaces are generic (ha ha) and should probably live somewhere
    // else for reuse.

    @FunctionalInterface
    public static interface FloatGetter<T> {
	float get(T input);
    }

    @FunctionalInterface
    public static interface FloatSetter<T> {
	void set(T input, float value);
    }

    @FunctionalInterface
    public static interface Float2Setter<T> {
	void set(T input, float u, float v);
    }

    @FunctionalInterface
    public static interface Float3Setter<T> {
	void set(T input, float x, float y, float z);
    }

    @FunctionalInterface
    public static interface IntGetter<T> {
	int get(T input);
    }

    @FunctionalInterface
    public static interface IntSetter<T> {
	void set(T input, int value);
    }

    @FunctionalInterface
    public static interface ObjectGetter<T, V> {
	V get(T input);
    }

    @FunctionalInterface
    public static interface NullableObjectGetter<T, V> {
	V get(T input);
    }

    @FunctionalInterface
    public static interface ObjectSetter<T, V> {
	void set(T input, V value);
    }

    @FunctionalInterface
    public static interface NullableObjectSetter<T, V> {
	void set(T input, V value);
    }

    public static class Layer<T> {
	public FloatGetter<T> uMinGetter;
	public FloatSetter<T> uMinSetter;

	public FloatGetter<T> vMinGetter;
	public FloatSetter<T> vMinSetter;

	public FloatGetter<T> uMaxGetter;
	public FloatSetter<T> uMaxSetter;

	public FloatGetter<T> vMaxGetter;
	public FloatSetter<T> vMaxSetter;

	public ObjectGetter<T, String> textureGetter;
	public ObjectSetter<T, String> textureSetter;
    }

    public static class Vertex<T> {
	public ObjectGetter<T, Vec3f> posGetter;
	public ObjectSetter<T, Vec3f> posSetter;

	public FloatGetter<T> xGetter;
	public FloatGetter<T> yGetter;
	public FloatGetter<T> zGetter;
	public Float3Setter<T> xyzSetter;

	public NullableObjectGetter<T, Vec3f> normalGetter;
	public NullableObjectSetter<T, Vec3f> normalSetter;

	public FloatGetter<T> normXGetter;
	public FloatGetter<T> normYGetter;
	public FloatGetter<T> normZGetter;
	public Float3Setter<T> normXYZSetter;

	public IntGetter<T> glowGetter;
	public IntSetter<T> glowSetter;
    }

    public static class VertexLayer<T> {
	public FloatGetter<T> uGetter;
	public FloatSetter<T> uSetter;

	public FloatGetter<T> vGetter;
	public FloatSetter<T> vSetter;

	public Float2Setter<T> uvSetter;

	public IntGetter<T> colorGetter;
	public IntSetter<T> colorSetter;

    }
}
