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
import static grondag.xm.mesh.EncoderFunctions.GET_HALF_INT_HIGH;
import static grondag.xm.mesh.EncoderFunctions.GET_HALF_INT_LOW;
import static grondag.xm.mesh.EncoderFunctions.GET_INT;
import static grondag.xm.mesh.EncoderFunctions.GET_INT_FAIL;
import static grondag.xm.mesh.EncoderFunctions.GET_INT_WHITE;
import static grondag.xm.mesh.EncoderFunctions.GET_NORMAL_X_QUANTIZED;
import static grondag.xm.mesh.EncoderFunctions.GET_NORMAL_Y_QUANTIZED;
import static grondag.xm.mesh.EncoderFunctions.GET_NORMAL_Z_QUANTIZED;
import static grondag.xm.mesh.EncoderFunctions.SET_FLOAT;
import static grondag.xm.mesh.EncoderFunctions.SET_FLOAT3;
import static grondag.xm.mesh.EncoderFunctions.SET_FLOAT3_FAIL;
import static grondag.xm.mesh.EncoderFunctions.SET_FLOAT_FAIL;
import static grondag.xm.mesh.EncoderFunctions.SET_HALF_INT_HIGH;
import static grondag.xm.mesh.EncoderFunctions.SET_HALF_INT_LOW;
import static grondag.xm.mesh.EncoderFunctions.SET_INT;
import static grondag.xm.mesh.EncoderFunctions.SET_INT_FAIL;
import static grondag.xm.mesh.EncoderFunctions.SET_INT_WHITE;
import static grondag.xm.mesh.EncoderFunctions.SET_NORMAL_XYZ_QUANTIZED;
import static grondag.xm.mesh.MeshFormat.FACE_NORMAL_FORMAT_COMPUTED;
import static grondag.xm.mesh.MeshFormat.FACE_NORMAL_FORMAT_NOMINAL;
import static grondag.xm.mesh.MeshFormat.FACE_NORMAL_FORMAT_QUANTIZED;
import static grondag.xm.mesh.MeshFormat.POLY_FORMAT_COUNT;
import static grondag.xm.mesh.MeshFormat.POLY_FORMAT_SHIFT;
import static grondag.xm.mesh.MeshFormat.VERTEX_COLOR_PER_VERTEX_LAYER;
import static grondag.xm.mesh.MeshFormat.VERTEX_COLOR_SAME;
import static grondag.xm.mesh.MeshFormat.VERTEX_COLOR_SAME_BY_LAYER;
import static grondag.xm.mesh.MeshFormat.VERTEX_COLOR_WHITE;
import static grondag.xm.mesh.MeshFormat.getFaceNormalFormat;
import static grondag.xm.mesh.MeshFormat.getLayerCount;
import static grondag.xm.mesh.MeshFormat.getVertexColorFormat;
import static grondag.xm.mesh.MeshFormat.isLinked;
import static grondag.xm.mesh.MeshFormat.isTagged;
import static grondag.xm.mesh.MeshFormat.polyFormatKey;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.vram.sc.concurrency.IndexedInterner;

import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.mesh.polygon.Vec3f;
import grondag.xm.intstream.IntStream;
import grondag.xm.mesh.EncoderFunctions.FloatGetter;
import grondag.xm.mesh.EncoderFunctions.FloatSetter;
import grondag.xm.mesh.EncoderFunctions.FloatSetter3;
import grondag.xm.mesh.EncoderFunctions.IntGetter;
import grondag.xm.mesh.EncoderFunctions.IntSetter;

@Internal
class PolyEncoder {
	private static final PolyEncoder[] ENCODERS = new PolyEncoder[POLY_FORMAT_COUNT];

	private static final IndexedInterner<String> textureHandler = new IndexedInterner<>(String.class);

	private static final float MISSING_NORMAL = Float.NaN;

	static {
		for (int i = 0; i < POLY_FORMAT_COUNT; i++) {
			ENCODERS[i] = new PolyEncoder(i << POLY_FORMAT_SHIFT);
		}
	}

	public static PolyEncoder get(int format) {
		return ENCODERS[polyFormatKey(format)];
	}

	private final int stride;

	private final FloatGetter getNormalX;
	private final int getNormalXOffset;
	private final FloatGetter getNormalY;
	private final int getNormalYOffset;
	private final FloatGetter getNormalZ;
	private final int getNormalZOffset;

	private final FloatSetter3 clearNormal;
	private final FloatSetter3 setNormalXYZ;

	private final FloatGetter getU0;
	private final FloatGetter getV0;
	private final FloatSetter setU0;
	private final FloatSetter setV0;
	private final int minUOffset0;
	private final int maxUOffset0;
	private final int minVOffset0;
	private final int maxVOffset0;

	private final FloatGetter getU1;
	private final FloatGetter getV1;
	private final FloatSetter setU1;
	private final FloatSetter setV1;
	private final int minUOffset1;
	private final int maxUOffset1;
	private final int minVOffset1;
	private final int maxVOffset1;

	private final FloatGetter getU2;
	private final FloatGetter getV2;
	private final FloatSetter setU2;
	private final FloatSetter setV2;
	private final int minUOffset2;
	private final int maxUOffset2;
	private final int minVOffset2;
	private final int maxVOffset2;

	private final int linkOffset;
	private final IntGetter getLink;
	private final IntSetter setLink;

	private final int tagOffset;
	private final IntGetter getTag;
	private final IntSetter setTag;

	private final IntGetter getTexture0;
	private final IntSetter setTexture0;
	private final IntGetter getTexture1;
	private final IntSetter setTexture1;
	private final IntGetter getTexture2;
	private final IntSetter setTexture2;
	/** Holds first two textures. */
	private final int textureOffset01;
	private final int textureOffset2;

	private final IntGetter getColor0;
	private final IntSetter setColor0;
	private final IntGetter getColor1;
	private final IntSetter setColor1;
	private final IntGetter getColor2;
	private final IntSetter setColor2;
	private final int colorOffset0;
	private final int colorOffset1;
	private final int colorOffset2;

	private PolyEncoder(int format) {
		final int baseOffset = 1 + StaticEncoder.INTEGER_WIDTH;
		int offset = baseOffset;

		switch (getFaceNormalFormat(format)) {
			case FACE_NORMAL_FORMAT_COMPUTED:
				getNormalXOffset = offset++;
				getNormalX = GET_FLOAT;
				getNormalYOffset = offset++;
				getNormalY = GET_FLOAT;
				getNormalZOffset = offset++;
				getNormalZ = GET_FLOAT;
				setNormalXYZ = SET_FLOAT3;
				clearNormal = SET_FLOAT3;
				break;

			case FACE_NORMAL_FORMAT_QUANTIZED:
				getNormalXOffset = offset++;
				getNormalYOffset = getNormalXOffset;
				getNormalZOffset = getNormalXOffset;
				getNormalX = GET_NORMAL_X_QUANTIZED;
				getNormalY = GET_NORMAL_Y_QUANTIZED;
				getNormalZ = GET_NORMAL_Z_QUANTIZED;
				setNormalXYZ = SET_NORMAL_XYZ_QUANTIZED;
				clearNormal = SET_FLOAT3_FAIL;
				break;

			case FACE_NORMAL_FORMAT_NOMINAL:
			default:
				getNormalXOffset = -1;
				getNormalX = GET_FLOAT_FAIL;
				getNormalYOffset = -1;
				getNormalY = GET_FLOAT_FAIL;
				getNormalZOffset = -1;
				getNormalZ = GET_FLOAT_FAIL;
				setNormalXYZ = SET_FLOAT3_FAIL;
				clearNormal = SET_FLOAT3_FAIL;
				break;
		}

		final int layerCount = getLayerCount(format);

		// PERF: implement packed UV encoding
		getU0 = GET_FLOAT;
		getV0 = GET_FLOAT;
		setU0 = SET_FLOAT;
		setV0 = SET_FLOAT;
		minUOffset0 = offset++;
		maxUOffset0 = offset++;
		minVOffset0 = offset++;
		maxVOffset0 = offset++;

		textureOffset01 = offset++;
		getTexture0 = GET_HALF_INT_LOW;
		setTexture0 = SET_HALF_INT_LOW;

		if (layerCount > 1) {
			getU1 = GET_FLOAT;
			getV1 = GET_FLOAT;
			setU1 = SET_FLOAT;
			setV1 = SET_FLOAT;
			minUOffset1 = offset++;
			maxUOffset1 = offset++;
			minVOffset1 = offset++;
			maxVOffset1 = offset++;

			getTexture1 = GET_HALF_INT_HIGH;
			setTexture1 = SET_HALF_INT_HIGH;
		} else {
			getU1 = GET_FLOAT_FAIL;
			getV1 = GET_FLOAT_FAIL;
			setU1 = SET_FLOAT_FAIL;
			setV1 = SET_FLOAT_FAIL;
			minUOffset1 = BAD_ADDRESS;
			maxUOffset1 = BAD_ADDRESS;
			minVOffset1 = BAD_ADDRESS;
			maxVOffset1 = BAD_ADDRESS;

			getTexture1 = GET_INT_FAIL;
			setTexture1 = SET_INT_FAIL;
		}

		if (layerCount == 3) {
			getU2 = GET_FLOAT;
			getV2 = GET_FLOAT;
			setU2 = SET_FLOAT;
			setV2 = SET_FLOAT;
			minUOffset2 = offset++;
			maxUOffset2 = offset++;
			minVOffset2 = offset++;
			maxVOffset2 = offset++;

			textureOffset2 = offset++;
			getTexture2 = GET_INT;
			setTexture2 = SET_INT;
		} else {
			getU2 = GET_FLOAT_FAIL;
			getV2 = GET_FLOAT_FAIL;
			setU2 = SET_FLOAT_FAIL;
			setV2 = SET_FLOAT_FAIL;
			minUOffset2 = BAD_ADDRESS;
			maxUOffset2 = BAD_ADDRESS;
			minVOffset2 = BAD_ADDRESS;
			maxVOffset2 = BAD_ADDRESS;

			textureOffset2 = BAD_ADDRESS;
			getTexture2 = GET_INT_FAIL;
			setTexture2 = SET_INT_FAIL;
		}

		if (isLinked(format)) {
			linkOffset = offset++;
			getLink = GET_INT;
			setLink = SET_INT;
		} else {
			linkOffset = BAD_ADDRESS;
			getLink = GET_INT_FAIL;
			setLink = SET_INT_FAIL;
		}

		if (isTagged(format)) {
			tagOffset = offset++;
			getTag = GET_INT;
			setTag = SET_INT;
		} else {
			tagOffset = BAD_ADDRESS;
			getTag = IntGetter.ZERO;
			setTag = IntSetter.VOID;
		}

		switch (getVertexColorFormat(format)) {
			case VERTEX_COLOR_WHITE:
				getColor0 = GET_INT_WHITE;
				getColor1 = layerCount > 1 ? GET_INT_WHITE : GET_INT_FAIL;
				getColor2 = layerCount == 3 ? GET_INT_WHITE : GET_INT_FAIL;
				setColor0 = SET_INT_WHITE;
				setColor1 = layerCount > 1 ? SET_INT_WHITE : SET_INT_FAIL;
				setColor2 = layerCount == 3 ? SET_INT_WHITE : SET_INT_FAIL;
				colorOffset0 = BAD_ADDRESS;
				colorOffset1 = BAD_ADDRESS;
				colorOffset2 = BAD_ADDRESS;
				break;

			case VERTEX_COLOR_SAME:
				getColor0 = GET_INT;
				getColor1 = GET_INT;
				getColor2 = GET_INT;
				setColor0 = SET_INT;
				setColor1 = SET_INT;
				setColor2 = SET_INT;
				colorOffset0 = offset++;
				colorOffset1 = colorOffset0;
				colorOffset2 = colorOffset0;
				break;

			case VERTEX_COLOR_SAME_BY_LAYER:
				getColor0 = GET_INT;
				getColor1 = GET_INT;
				getColor2 = GET_INT;
				setColor0 = SET_INT;
				setColor1 = SET_INT;
				setColor2 = SET_INT;
				colorOffset0 = offset++;
				colorOffset1 = offset++;
				colorOffset2 = offset++;
				break;

			case VERTEX_COLOR_PER_VERTEX_LAYER:
			default:
				getColor0 = GET_INT_FAIL;
				getColor1 = GET_INT_FAIL;
				getColor2 = GET_INT_FAIL;
				setColor0 = SET_INT_FAIL;
				setColor1 = SET_INT_FAIL;
				setColor2 = SET_INT_FAIL;
				colorOffset0 = BAD_ADDRESS;
				colorOffset1 = BAD_ADDRESS;
				colorOffset2 = BAD_ADDRESS;
				break;
		}

		stride = offset - baseOffset;
	}

	public final int stride() {
		return stride;
	}

	public final void setFaceNormal(IntStream stream, int baseAddress, Vec3f normal) {
		setFaceNormal(stream, baseAddress, normal.x(), normal.y(), normal.z());
	}

	public final void setFaceNormal(IntStream stream, int baseAddress, float x, float y, float z) {
		setNormalXYZ.set(stream, baseAddress + getNormalXOffset, x, y, z);
	}

	public final void clearFaceNormal(IntStream stream, int baseAddress) {
		clearNormal.set(stream, baseAddress + getNormalXOffset, MISSING_NORMAL, MISSING_NORMAL, MISSING_NORMAL);
	}

	public final Vec3f getFaceNormal(IntStream stream, int baseAddress) {
		final float x = getNormalX.get(stream, baseAddress + getNormalXOffset);

		if (Float.isNaN(x)) {
			return null;
		}

		return Vec3f.create(x, getNormalY.get(stream, baseAddress + getNormalYOffset), getNormalZ.get(stream, baseAddress + getNormalZOffset));
	}

	public final float getFaceNormalX(IntStream stream, int baseAddress) {
		return getNormalX.get(stream, baseAddress + getNormalXOffset);
	}

	public final float getFaceNormalY(IntStream stream, int baseAddress) {
		return getNormalY.get(stream, baseAddress + getNormalYOffset);
	}

	public final float getFaceNormalZ(IntStream stream, int baseAddress) {
		return getNormalZ.get(stream, baseAddress + getNormalZOffset);
	}

	/**
	 * Replaces zeros with NaN and replaces NaN with zeros.
	 */
	protected static int swapZeroNonValues(int valueIn) {
		return valueIn == 0 ? Polygon.NO_LINK_OR_TAG : valueIn == Polygon.NO_LINK_OR_TAG ? 0 : valueIn;
	}

	public final int getTag(IntStream stream, int baseAddress) {
		// want to return NO_TAG if never set, so swap with default (zero) value here
		return swapZeroNonValues(getTag.get(stream, baseAddress + tagOffset));
	}

	public final void setTag(IntStream stream, int baseAddress, int tag) {
		// want to return NO_TAG if never set, so swap with default (zero) value here
		setTag.set(stream, baseAddress + tagOffset, swapZeroNonValues(tag));
	}

	public final int getLink(IntStream stream, int baseAddress) {
		// want to return NO_LINK if never set, so swap with default (zero) value here
		return swapZeroNonValues(getLink.get(stream, baseAddress + linkOffset));
	}

	public final void setLink(IntStream stream, int baseAddress, int link) {
		// want to return NO_LINK if never set, so swap with default (zero) value here
		setLink.set(stream, baseAddress + linkOffset, swapZeroNonValues(link));
	}

	public final float getMaxU(IntStream stream, int baseAddress, int layerIndex) {
		return layerIndex == 0 ? getU0.get(stream, baseAddress + maxUOffset0)
				: layerIndex == 1 ? getU1.get(stream, baseAddress + maxUOffset1) : getU2.get(stream, baseAddress + maxUOffset2);
	}

	public final void setMaxU(IntStream stream, int baseAddress, int layerIndex, float maxU) {
		if (layerIndex == 0) {
			setU0.set(stream, baseAddress + maxUOffset0, maxU);
		} else if (layerIndex == 1) {
			setU1.set(stream, baseAddress + maxUOffset1, maxU);
		} else {
			setU2.set(stream, baseAddress + maxUOffset2, maxU);
		}
	}

	public final float getMinU(IntStream stream, int baseAddress, int layerIndex) {
		return layerIndex == 0 ? getU0.get(stream, baseAddress + minUOffset0)
				: layerIndex == 1 ? getU1.get(stream, baseAddress + minUOffset1) : getU2.get(stream, baseAddress + minUOffset2);
	}

	public final void setMinU(IntStream stream, int baseAddress, int layerIndex, float minU) {
		if (layerIndex == 0) {
			setU0.set(stream, baseAddress + minUOffset0, minU);
		} else if (layerIndex == 1) {
			setU1.set(stream, baseAddress + minUOffset1, minU);
		} else {
			setU2.set(stream, baseAddress + minUOffset2, minU);
		}
	}

	public final float getMaxV(IntStream stream, int baseAddress, int layerIndex) {
		return layerIndex == 0 ? getV0.get(stream, baseAddress + maxVOffset0)
				: layerIndex == 1 ? getV1.get(stream, baseAddress + maxVOffset1) : getV2.get(stream, baseAddress + maxVOffset2);
	}

	public final void setMaxV(IntStream stream, int baseAddress, int layerIndex, float maxV) {
		if (layerIndex == 0) {
			setV0.set(stream, baseAddress + maxVOffset0, maxV);
		} else if (layerIndex == 1) {
			setV1.set(stream, baseAddress + maxVOffset1, maxV);
		} else {
			setV2.set(stream, baseAddress + maxVOffset2, maxV);
		}
	}

	public final float getMinV(IntStream stream, int baseAddress, int layerIndex) {
		return layerIndex == 0 ? getV0.get(stream, baseAddress + minVOffset0)
				: layerIndex == 1 ? getV1.get(stream, baseAddress + minVOffset1) : getV2.get(stream, baseAddress + minVOffset2);
	}

	public final void setMinV(IntStream stream, int baseAddress, int layerIndex, float minV) {
		if (layerIndex == 0) {
			setV0.set(stream, baseAddress + minVOffset0, minV);
		} else if (layerIndex == 1) {
			setV1.set(stream, baseAddress + minVOffset1, minV);
		} else {
			setV2.set(stream, baseAddress + minVOffset2, minV);
		}
	}

	public final String getTextureName(IntStream stream, int baseAddress, int layerIndex) {
		final int handle = layerIndex == 0 ? getTexture0.get(stream, baseAddress + textureOffset01)
				: layerIndex == 1 ? getTexture1.get(stream, baseAddress + textureOffset01) : getTexture2.get(stream, baseAddress + textureOffset2);

		return handle == 0 ? "" : textureHandler.fromIndex(handle);
	}

	public final void setTextureName(IntStream stream, int baseAddress, int layerIndex, String textureName) {
		final int handle = textureName == null || textureName.isEmpty() ? 0 : textureHandler.toIndex(textureName);

		if (layerIndex == 0) {
			setTexture0.set(stream, baseAddress + textureOffset01, handle);
		} else if (layerIndex == 1) {
			setTexture1.set(stream, baseAddress + textureOffset01, handle);
		} else {
			setTexture2.set(stream, baseAddress + textureOffset2, handle);
		}
	}

	public final int getVertexColor(IntStream stream, int baseAddress, int layerIndex) {
		return layerIndex == 0 ? getColor0.get(stream, baseAddress + colorOffset0)
				: layerIndex == 1 ? getColor1.get(stream, baseAddress + colorOffset1) : getColor2.get(stream, baseAddress + colorOffset2);
	}

	public final void setVertexColor(IntStream stream, int baseAddress, int layerIndex, int color) {
		if (layerIndex == 0) {
			setColor0.set(stream, baseAddress + colorOffset0, color);
		} else if (layerIndex == 1) {
			setColor1.set(stream, baseAddress + colorOffset1, color);
		} else {
			setColor2.set(stream, baseAddress + colorOffset2, color);
		}
	}
}
