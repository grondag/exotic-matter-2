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

package grondag.xm.mesh;

import static grondag.xm.mesh.EncoderFunctions.BAD_ADDRESS;
import static grondag.xm.mesh.EncoderFunctions.GET_FLOAT;
import static grondag.xm.mesh.EncoderFunctions.GET_FLOAT_FAIL;
import static grondag.xm.mesh.EncoderFunctions.GET_INT;
import static grondag.xm.mesh.EncoderFunctions.GET_INT_FAIL;
import static grondag.xm.mesh.EncoderFunctions.GET_NORMAL_X_QUANTIZED;
import static grondag.xm.mesh.EncoderFunctions.GET_NORMAL_Y_QUANTIZED;
import static grondag.xm.mesh.EncoderFunctions.GET_NORMAL_Z_QUANTIZED;
import static grondag.xm.mesh.EncoderFunctions.SET_FLOAT;
import static grondag.xm.mesh.EncoderFunctions.SET_FLOAT2;
import static grondag.xm.mesh.EncoderFunctions.SET_FLOAT2_FAIL;
import static grondag.xm.mesh.EncoderFunctions.SET_FLOAT3;
import static grondag.xm.mesh.EncoderFunctions.SET_FLOAT3_FAIL;
import static grondag.xm.mesh.EncoderFunctions.SET_FLOAT_FAIL;
import static grondag.xm.mesh.EncoderFunctions.SET_INT;
import static grondag.xm.mesh.EncoderFunctions.SET_INT_FAIL;
import static grondag.xm.mesh.MeshFormat.VERTEX_COLOR_PER_VERTEX_LAYER;
import static grondag.xm.mesh.MeshFormat.VERTEX_FORMAT_COUNT;
import static grondag.xm.mesh.MeshFormat.VERTEX_FORMAT_SHIFT;
import static grondag.xm.mesh.MeshFormat.VERTEX_NORMAL_FACE;
import static grondag.xm.mesh.MeshFormat.VERTEX_NORMAL_QUANTIZED;
import static grondag.xm.mesh.MeshFormat.VERTEX_NORMAL_REGULAR;
import static grondag.xm.mesh.MeshFormat.VERTEX_UV_BY_LAYER;
import static grondag.xm.mesh.MeshFormat.getLayerCount;
import static grondag.xm.mesh.MeshFormat.getVertexColorFormat;
import static grondag.xm.mesh.MeshFormat.getVertexNormalFormat;
import static grondag.xm.mesh.MeshFormat.isMutable;
import static grondag.xm.mesh.MeshFormat.setLayerCount;
import static grondag.xm.mesh.MeshFormat.setQuantizedPos;
import static grondag.xm.mesh.MeshFormat.setVertexColorFormat;
import static grondag.xm.mesh.MeshFormat.setVertexNormalFormat;
import static grondag.xm.mesh.MeshFormat.setVertexUVFormat;
import static grondag.xm.mesh.MeshFormat.vertexFormatKey;

import org.jetbrains.annotations.ApiStatus.Internal;

import grondag.xm.api.mesh.polygon.Vec3f;
import grondag.xm.intstream.IntStream;
import grondag.xm.mesh.EncoderFunctions.FloatGetter;
import grondag.xm.mesh.EncoderFunctions.FloatSetter;
import grondag.xm.mesh.EncoderFunctions.FloatSetter2;
import grondag.xm.mesh.EncoderFunctions.FloatSetter3;
import grondag.xm.mesh.EncoderFunctions.IntGetter;
import grondag.xm.mesh.EncoderFunctions.IntSetter;

@Internal
class VertexEncoder {
	private static final VertexEncoder[] ENCODERS = new VertexEncoder[VERTEX_FORMAT_COUNT];

	private static final VertexEncoder MUTABLE;

	static {
		for (int i = 0; i < VERTEX_FORMAT_COUNT; i++) {
			ENCODERS[i] = new VertexEncoder(i << VERTEX_FORMAT_SHIFT);
		}

		int mutableFormat = 0;
		mutableFormat = setLayerCount(mutableFormat, 3);
		mutableFormat = setVertexColorFormat(mutableFormat, VERTEX_COLOR_PER_VERTEX_LAYER);
		mutableFormat = setQuantizedPos(mutableFormat, false);
		mutableFormat = setVertexNormalFormat(mutableFormat, VERTEX_NORMAL_REGULAR);
		mutableFormat = setVertexUVFormat(mutableFormat, VERTEX_UV_BY_LAYER);

		assert getLayerCount(mutableFormat) == 3;
		assert getVertexColorFormat(mutableFormat) == VERTEX_COLOR_PER_VERTEX_LAYER;

		MUTABLE = ENCODERS[vertexFormatKey(mutableFormat)];

		assert MUTABLE.hasColor();
		assert MUTABLE.hasNormals();
	}

	/**
	 * All mutable formats have same full-feature binary format for
	 * data-compatibility with format changes.
	 */
	public static VertexEncoder get(int format) {
		return isMutable(format) ? MUTABLE : ENCODERS[vertexFormatKey(format)];
	}

	private final int vertexStride;
	private final boolean hasNormals;
	private final boolean hasColor;

	private final FloatGetter getPos;
	private final FloatSetter setPos;
	private final FloatSetter3 setPosXYZ;
	private final int offsetPosX;
	private final int offsetPosY;
	private final int offsetPosZ;

	private final FloatGetter getNormalX;
	private final FloatGetter getNormalY;
	private final FloatGetter getNormalZ;
	private final FloatSetter3 setNormalXYZ;
	private final int offsetNormalX;
	private final int offsetNormalY;
	private final int offsetNormalZ;

	private final IntGetter getColor0;
	private final IntGetter getColor1;
	private final IntGetter getColor2;
	private final IntSetter setColor0;
	private final IntSetter setColor1;
	private final IntSetter setColor2;
	private final int offsetColor0;
	private final int offsetColor1;
	private final int offsetColor2;

	private final FloatGetter getU0;
	private final FloatGetter getV0;
	private final FloatSetter setU0;
	private final FloatSetter setV0;
	private final FloatSetter2 setUV0;
	private final int offsetU0;
	private final int offsetV0;

	private final FloatGetter getU1;
	private final FloatGetter getV1;
	private final FloatSetter setU1;
	private final FloatSetter setV1;
	private final FloatSetter2 setUV1;
	private final int offsetU1;
	private final int offsetV1;

	private final FloatGetter getU2;
	private final FloatGetter getV2;
	private final FloatSetter setU2;
	private final FloatSetter setV2;
	private final FloatSetter2 setUV2;
	private final int offsetU2;
	private final int offsetV2;

	private final boolean multiUV;

	private VertexEncoder(int format) {
		int offset = 0;

		// PERF: quantize position
		offsetPosX = offset++;
		offsetPosY = offset++;
		offsetPosZ = offset++;
		getPos = GET_FLOAT;
		setPos = SET_FLOAT;
		setPosXYZ = SET_FLOAT3;

		switch (getVertexNormalFormat(format)) {
			case VERTEX_NORMAL_QUANTIZED:
				hasNormals = true;
				getNormalX = GET_NORMAL_X_QUANTIZED;
				getNormalY = GET_NORMAL_Y_QUANTIZED;
				getNormalZ = GET_NORMAL_Z_QUANTIZED;
				setNormalXYZ = SET_FLOAT3_FAIL;
				offsetNormalX = offset++;
				offsetNormalY = offsetNormalX;
				offsetNormalZ = offsetNormalX;
				break;

			case VERTEX_NORMAL_REGULAR:
				hasNormals = true;
				getNormalX = GET_FLOAT;
				getNormalY = GET_FLOAT;
				getNormalZ = GET_FLOAT;
				setNormalXYZ = SET_FLOAT3;
				offsetNormalX = offset++;
				offsetNormalY = offset++;
				offsetNormalZ = offset++;
				break;

			case VERTEX_NORMAL_FACE:
			default:
				hasNormals = false;
				getNormalX = GET_FLOAT_FAIL;
				getNormalY = GET_FLOAT_FAIL;
				getNormalZ = GET_FLOAT_FAIL;
				setNormalXYZ = SET_FLOAT3_FAIL;
				offsetNormalX = BAD_ADDRESS;
				offsetNormalY = BAD_ADDRESS;
				offsetNormalZ = BAD_ADDRESS;
				break;
		}

		final int layerCount = getLayerCount(format);

		// PERF: quantize UV

		multiUV = MeshFormat.getVertexUVFormat(format) == MeshFormat.VERTEX_UV_BY_LAYER;

		getU0 = GET_FLOAT;
		getV0 = GET_FLOAT;
		setU0 = SET_FLOAT;
		setV0 = SET_FLOAT;
		setUV0 = SET_FLOAT2;
		offsetU0 = offset++;
		offsetV0 = offset++;

		getU1 = layerCount > 1 ? GET_FLOAT : GET_FLOAT_FAIL;
		getV1 = layerCount > 1 ? GET_FLOAT : GET_FLOAT_FAIL;
		setU1 = multiUV && layerCount > 1 ? SET_FLOAT : SET_FLOAT_FAIL;
		setV1 = multiUV && layerCount > 1 ? SET_FLOAT : SET_FLOAT_FAIL;
		setUV1 = multiUV && layerCount > 1 ? SET_FLOAT2 : SET_FLOAT2_FAIL;
		offsetU1 = multiUV && layerCount > 1 ? offset++ : offsetU0;
		offsetV1 = multiUV && layerCount > 1 ? offset++ : offsetV0;

		getU2 = layerCount == 3 ? GET_FLOAT : GET_FLOAT_FAIL;
		getV2 = layerCount == 3 ? GET_FLOAT : GET_FLOAT_FAIL;
		setU2 = multiUV && layerCount == 3 ? SET_FLOAT : SET_FLOAT_FAIL;
		setV2 = multiUV && layerCount == 3 ? SET_FLOAT : SET_FLOAT_FAIL;
		setUV2 = multiUV && layerCount == 3 ? SET_FLOAT2 : SET_FLOAT2_FAIL;
		offsetU2 = multiUV && layerCount == 3 ? offset++ : offsetU0;
		offsetV2 = multiUV && layerCount == 3 ? offset++ : offsetV0;

		hasColor = getVertexColorFormat(format) == VERTEX_COLOR_PER_VERTEX_LAYER;

		if (hasColor) {
			getColor0 = GET_INT;
			getColor1 = layerCount > 1 ? GET_INT : GET_INT_FAIL;
			getColor2 = layerCount == 3 ? GET_INT : GET_INT_FAIL;
			setColor0 = SET_INT;
			setColor1 = layerCount > 1 ? SET_INT : SET_INT_FAIL;
			setColor2 = layerCount == 3 ? SET_INT : SET_INT_FAIL;
			offsetColor0 = offset++;
			offsetColor1 = layerCount > 1 ? offset++ : BAD_ADDRESS;
			offsetColor2 = layerCount == 3 ? offset++ : BAD_ADDRESS;
		} else {
			getColor0 = GET_INT_FAIL;
			getColor1 = GET_INT_FAIL;
			getColor2 = GET_INT_FAIL;
			setColor0 = SET_INT_FAIL;
			setColor1 = SET_INT_FAIL;
			setColor2 = SET_INT_FAIL;
			offsetColor0 = BAD_ADDRESS;
			offsetColor1 = BAD_ADDRESS;
			offsetColor2 = BAD_ADDRESS;
		}

		vertexStride = offset;
	}

	public int vertexStride() {
		return vertexStride;
	}

	public boolean hasNormals() {
		return hasNormals;
	}

	public Vec3f getVertexNormal(IntStream stream, int vertexAddress, int vertexIndex) {
		if (!hasNormals) {
			return null;
		}

		final int base = vertexAddress + vertexIndex * vertexStride;
		final Vec3f result = Vec3f.create(getNormalX.get(stream, base + offsetNormalX), getNormalY.get(stream, base + offsetNormalY),
				getNormalZ.get(stream, base + offsetNormalZ));

		return result == Vec3f.ZERO ? null : result;
	}

	/**
	 * To distinguish between normals that have been set vs not set, we swap the
	 * interpretation of zero and NaN.
	 */
	private float interpretMissingNormal(float rawValue) {
		if (rawValue == 0) {
			return Float.NaN;
		} else if (Float.isNaN(rawValue)) {
			return 0;
		} else {
			return rawValue;
		}
	}

	public boolean hasVertexNormal(IntStream stream, int vertexAddress, int vertexIndex) {
		return hasNormals && getNormalX.get(stream, vertexAddress + vertexIndex * vertexStride + offsetNormalX) != 0;
	}

	public float getVertexNormalX(IntStream stream, int vertexAddress, int vertexIndex) {
		return interpretMissingNormal(getNormalX.get(stream, vertexAddress + vertexIndex * vertexStride + offsetNormalX));
	}

	public float getVertexNormalY(IntStream stream, int vertexAddress, int vertexIndex) {
		return interpretMissingNormal(getNormalY.get(stream, vertexAddress + vertexIndex * vertexStride + offsetNormalY));
	}

	public float getVertexNormalZ(IntStream stream, int vertexAddress, int vertexIndex) {
		return interpretMissingNormal(getNormalZ.get(stream, vertexAddress + vertexIndex * vertexStride + offsetNormalZ));
	}

	public void setVertexNormal(IntStream stream, int vertexAddress, int vertexIndex, float normalX, float normalY, float normalZ) {
		setNormalXYZ.set(stream, vertexAddress + vertexIndex * vertexStride + offsetNormalX, interpretMissingNormal(normalX), interpretMissingNormal(normalY),
				interpretMissingNormal(normalZ));
	}

	public float getVertexX(IntStream stream, int vertexAddress, int vertexIndex) {
		return getPos.get(stream, vertexAddress + vertexIndex * vertexStride + offsetPosX);
	}

	public float getVertexY(IntStream stream, int vertexAddress, int vertexIndex) {
		return getPos.get(stream, vertexAddress + vertexIndex * vertexStride + offsetPosY);
	}

	public float getVertexZ(IntStream stream, int vertexAddress, int vertexIndex) {
		return getPos.get(stream, vertexAddress + vertexIndex * vertexStride + offsetPosZ);
	}

	public void setVertexPos(IntStream stream, int vertexAddress, int vertexIndex, float x, float y, float z) {
		setPosXYZ.set(stream, vertexAddress + vertexIndex * vertexStride + offsetPosX, x, y, z);
	}

	public void setVertexX(IntStream stream, int vertexAddress, int vertexIndex, float x) {
		setPos.set(stream, vertexAddress + vertexIndex * vertexStride + offsetPosX, x);
	}

	public void setVertexY(IntStream stream, int vertexAddress, int vertexIndex, float y) {
		setPos.set(stream, vertexAddress + vertexIndex * vertexStride + offsetPosY, y);
	}

	public void setVertexZ(IntStream stream, int vertexAddress, int vertexIndex, float z) {
		setPos.set(stream, vertexAddress + vertexIndex * vertexStride + offsetPosZ, z);
	}

	public boolean hasColor() {
		return hasColor;
	}

	public int getVertexColor(IntStream stream, int vertexAddress, int layerIndex, int vertexIndex) {
		return layerIndex == 0 ? getColor0.get(stream, vertexAddress + vertexIndex * vertexStride + offsetColor0)
				: layerIndex == 1 ? getColor1.get(stream, vertexAddress + vertexIndex * vertexStride + offsetColor1)
						: getColor2.get(stream, vertexAddress + vertexIndex * vertexStride + offsetColor2);
	}

	public void setVertexColor(IntStream stream, int vertexAddress, int layerIndex, int vertexIndex, int color) {
		if (layerIndex == 0) {
			setColor0.set(stream, vertexAddress + vertexIndex * vertexStride + offsetColor0, color);
		} else if (layerIndex == 1) {
			setColor1.set(stream, vertexAddress + vertexIndex * vertexStride + offsetColor1, color);
		} else {
			setColor2.set(stream, vertexAddress + vertexIndex * vertexStride + offsetColor2, color);
		}
	}

	public float getVertexU(IntStream stream, int vertexAddress, int layerIndex, int vertexIndex) {
		return layerIndex == 0 ? getU0.get(stream, vertexAddress + vertexIndex * vertexStride + offsetU0)
				: layerIndex == 1 ? getU1.get(stream, vertexAddress + vertexIndex * vertexStride + offsetU1)
						: getU2.get(stream, vertexAddress + vertexIndex * vertexStride + offsetU2);
	}

	public void setVertexU(IntStream stream, int vertexAddress, int layerIndex, int vertexIndex, float u) {
		if (layerIndex == 0) {
			setU0.set(stream, vertexAddress + vertexIndex * vertexStride + offsetU0, u);
		} else if (layerIndex == 1) {
			setU1.set(stream, vertexAddress + vertexIndex * vertexStride + offsetU1, u);
		} else {
			setU2.set(stream, vertexAddress + vertexIndex * vertexStride + offsetU2, u);
		}
	}

	public float getVertexV(IntStream stream, int vertexAddress, int layerIndex, int vertexIndex) {
		return layerIndex == 0 ? getV0.get(stream, vertexAddress + vertexIndex * vertexStride + offsetV0)
				: layerIndex == 1 ? getV1.get(stream, vertexAddress + vertexIndex * vertexStride + offsetV1)
						: getV2.get(stream, vertexAddress + vertexIndex * vertexStride + offsetV2);
	}

	public void setVertexV(IntStream stream, int vertexAddress, int layerIndex, int vertexIndex, float v) {
		if (layerIndex == 0) {
			setV0.set(stream, vertexAddress + vertexIndex * vertexStride + offsetV0, v);
		} else if (layerIndex == 1) {
			setV1.set(stream, vertexAddress + vertexIndex * vertexStride + offsetV1, v);
		} else {
			setV2.set(stream, vertexAddress + vertexIndex * vertexStride + offsetV2, v);
		}
	}

	public void setVertexUV(IntStream stream, int vertexAddress, int layerIndex, int vertexIndex, float u, float v) {
		if (layerIndex == 0) {
			setUV0.set(stream, vertexAddress + vertexIndex * vertexStride + offsetU0, u, v);
		} else if (layerIndex == 1) {
			setUV1.set(stream, vertexAddress + vertexIndex * vertexStride + offsetU1, u, v);
		} else {
			setUV2.set(stream, vertexAddress + vertexIndex * vertexStride + offsetU2, u, v);
		}
	}

	/**
	 * True unless single layer or all layers have same uv.
	 */
	public boolean multiUV() {
		return multiUV;
	}
}
