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

package grondag.xm2.mesh.stream;

import static grondag.xm2.mesh.stream.EncoderFunctions.BAD_ADDRESS;
import static grondag.xm2.mesh.stream.EncoderFunctions.GET_FLOAT;
import static grondag.xm2.mesh.stream.EncoderFunctions.GET_FLOAT_FAIL;
import static grondag.xm2.mesh.stream.EncoderFunctions.GET_HALF_INT_HIGH;
import static grondag.xm2.mesh.stream.EncoderFunctions.GET_HALF_INT_LOW;
import static grondag.xm2.mesh.stream.EncoderFunctions.GET_INT;
import static grondag.xm2.mesh.stream.EncoderFunctions.GET_INT_FAIL;
import static grondag.xm2.mesh.stream.EncoderFunctions.GET_INT_WHITE;
import static grondag.xm2.mesh.stream.EncoderFunctions.GET_NORMAL_X_QUANTIZED;
import static grondag.xm2.mesh.stream.EncoderFunctions.GET_NORMAL_Y_QUANTIZED;
import static grondag.xm2.mesh.stream.EncoderFunctions.GET_NORMAL_Z_QUANTIZED;
import static grondag.xm2.mesh.stream.EncoderFunctions.SET_FLOAT;
import static grondag.xm2.mesh.stream.EncoderFunctions.SET_FLOAT3;
import static grondag.xm2.mesh.stream.EncoderFunctions.SET_FLOAT3_FAIL;
import static grondag.xm2.mesh.stream.EncoderFunctions.SET_FLOAT_FAIL;
import static grondag.xm2.mesh.stream.EncoderFunctions.SET_HALF_INT_HIGH;
import static grondag.xm2.mesh.stream.EncoderFunctions.SET_HALF_INT_LOW;
import static grondag.xm2.mesh.stream.EncoderFunctions.SET_INT;
import static grondag.xm2.mesh.stream.EncoderFunctions.SET_INT_FAIL;
import static grondag.xm2.mesh.stream.EncoderFunctions.SET_INT_WHITE;
import static grondag.xm2.mesh.stream.EncoderFunctions.SET_NORMAL_XYZ_QUANTIZED;
import static grondag.xm2.mesh.stream.PolyStreamFormat.FACE_NORMAL_FORMAT_COMPUTED;
import static grondag.xm2.mesh.stream.PolyStreamFormat.FACE_NORMAL_FORMAT_NOMINAL;
import static grondag.xm2.mesh.stream.PolyStreamFormat.FACE_NORMAL_FORMAT_QUANTIZED;
import static grondag.xm2.mesh.stream.PolyStreamFormat.POLY_FORMAT_COUNT;
import static grondag.xm2.mesh.stream.PolyStreamFormat.POLY_FORMAT_SHIFT;
import static grondag.xm2.mesh.stream.PolyStreamFormat.VERTEX_COLOR_PER_VERTEX_LAYER;
import static grondag.xm2.mesh.stream.PolyStreamFormat.VERTEX_COLOR_SAME;
import static grondag.xm2.mesh.stream.PolyStreamFormat.VERTEX_COLOR_SAME_BY_LAYER;
import static grondag.xm2.mesh.stream.PolyStreamFormat.VERTEX_COLOR_WHITE;
import static grondag.xm2.mesh.stream.PolyStreamFormat.getFaceNormalFormat;
import static grondag.xm2.mesh.stream.PolyStreamFormat.getLayerCount;
import static grondag.xm2.mesh.stream.PolyStreamFormat.getVertexColorFormat;
import static grondag.xm2.mesh.stream.PolyStreamFormat.isLinked;
import static grondag.xm2.mesh.stream.PolyStreamFormat.isTagged;
import static grondag.xm2.mesh.stream.PolyStreamFormat.polyFormatKey;

import grondag.fermion.intstream.IIntStream;
import grondag.fermion.structures.IndexedInterner;
import grondag.xm2.mesh.polygon.IPolygon;
import grondag.xm2.mesh.stream.EncoderFunctions.FloatGetter;
import grondag.xm2.mesh.stream.EncoderFunctions.FloatSetter;
import grondag.xm2.mesh.stream.EncoderFunctions.FloatSetter3;
import grondag.xm2.mesh.stream.EncoderFunctions.IntGetter;
import grondag.xm2.mesh.stream.EncoderFunctions.IntSetter;
import grondag.xm2.mesh.vertex.Vec3f;

public class PolyEncoder {
    private static final PolyEncoder[] ENCODERS = new PolyEncoder[POLY_FORMAT_COUNT];

    private static final IndexedInterner<String> textureHandler = new IndexedInterner<String>(String.class);

    private static final float MISSING_NORMAL = Float.NaN;

    static {
	for (int i = 0; i < POLY_FORMAT_COUNT; i++)
	    ENCODERS[i] = new PolyEncoder(i << POLY_FORMAT_SHIFT);
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
    /** holds first two textures */
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

	default:
	case FACE_NORMAL_FORMAT_NOMINAL:
	    getNormalXOffset = -1;
	    getNormalX = GET_FLOAT_FAIL;
	    getNormalYOffset = -1;
	    getNormalY = GET_FLOAT_FAIL;
	    getNormalZOffset = -1;
	    getNormalZ = GET_FLOAT_FAIL;
	    setNormalXYZ = SET_FLOAT3_FAIL;
	    clearNormal = SET_FLOAT3_FAIL;
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
	    getTag = GET_INT_FAIL;
	    setTag = SET_INT_FAIL;
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

	default:
	case VERTEX_COLOR_PER_VERTEX_LAYER:
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

    public final void setFaceNormal(IIntStream stream, int baseAddress, Vec3f normal) {
	setFaceNormal(stream, baseAddress, normal.x(), normal.y(), normal.z());
    }

    public final void setFaceNormal(IIntStream stream, int baseAddress, float x, float y, float z) {
	setNormalXYZ.set(stream, baseAddress + getNormalXOffset, x, y, z);
    }

    public final void clearFaceNormal(IIntStream stream, int baseAddress) {
	clearNormal.set(stream, baseAddress + getNormalXOffset, MISSING_NORMAL, MISSING_NORMAL, MISSING_NORMAL);
    }

    public final Vec3f getFaceNormal(IIntStream stream, int baseAddress) {
	final float x = getNormalX.get(stream, baseAddress + getNormalXOffset);
	if (Float.isNaN(x))
	    return null;
	return Vec3f.create(x, getNormalY.get(stream, baseAddress + getNormalYOffset),
		getNormalZ.get(stream, baseAddress + getNormalZOffset));
    }

    public final float getFaceNormalX(IIntStream stream, int baseAddress) {
	return getNormalX.get(stream, baseAddress + getNormalXOffset);
    }

    public final float getFaceNormalY(IIntStream stream, int baseAddress) {
	return getNormalY.get(stream, baseAddress + getNormalYOffset);
    }

    public final float getFaceNormalZ(IIntStream stream, int baseAddress) {
	return getNormalZ.get(stream, baseAddress + getNormalZOffset);
    }

    /**
     * Replaces zeros with NaN and replaces NaN with zeros.
     */
    protected static int swapZeroNaNValues(int valueIn) {
	return valueIn == 0 ? IPolygon.NO_LINK_OR_TAG : valueIn == IPolygon.NO_LINK_OR_TAG ? 0 : valueIn;
    }

    public final int getTag(IIntStream stream, int baseAddress) {
	// want to return NO_TAG if never set, so swap with default (zero) value here
	return swapZeroNaNValues(getTag.get(stream, baseAddress + tagOffset));
    }

    public final void setTag(IIntStream stream, int baseAddress, int tag) {
	// want to return NO_TAG if never set, so swap with default (zero) value here
	setTag.set(stream, baseAddress + tagOffset, swapZeroNaNValues(tag));
    }

    public final int getLink(IIntStream stream, int baseAddress) {
	// want to return NO_LINK if never set, so swap with default (zero) value here
	return swapZeroNaNValues(getLink.get(stream, baseAddress + linkOffset));
    }

    public final void setLink(IIntStream stream, int baseAddress, int link) {
	// want to return NO_LINK if never set, so swap with default (zero) value here
	setLink.set(stream, baseAddress + linkOffset, swapZeroNaNValues(link));
    }

    public final float getMaxU(IIntStream stream, int baseAddress, int layerIndex) {
	return layerIndex == 0 ? getU0.get(stream, baseAddress + maxUOffset0)
		: layerIndex == 1 ? getU1.get(stream, baseAddress + maxUOffset1)
			: getU2.get(stream, baseAddress + maxUOffset2);
    }

    public final void setMaxU(IIntStream stream, int baseAddress, int layerIndex, float maxU) {
	if (layerIndex == 0)
	    setU0.set(stream, baseAddress + maxUOffset0, maxU);
	else if (layerIndex == 1)
	    setU1.set(stream, baseAddress + maxUOffset1, maxU);
	else
	    setU2.set(stream, baseAddress + maxUOffset2, maxU);
    }

    public final float getMinU(IIntStream stream, int baseAddress, int layerIndex) {
	return layerIndex == 0 ? getU0.get(stream, baseAddress + minUOffset0)
		: layerIndex == 1 ? getU1.get(stream, baseAddress + minUOffset1)
			: getU2.get(stream, baseAddress + minUOffset2);
    }

    public final void setMinU(IIntStream stream, int baseAddress, int layerIndex, float minU) {
	if (layerIndex == 0)
	    setU0.set(stream, baseAddress + minUOffset0, minU);
	else if (layerIndex == 1)
	    setU1.set(stream, baseAddress + minUOffset1, minU);
	else
	    setU2.set(stream, baseAddress + minUOffset2, minU);
    }

    public final float getMaxV(IIntStream stream, int baseAddress, int layerIndex) {
	return layerIndex == 0 ? getV0.get(stream, baseAddress + maxVOffset0)
		: layerIndex == 1 ? getV1.get(stream, baseAddress + maxVOffset1)
			: getV2.get(stream, baseAddress + maxVOffset2);
    }

    public final void setMaxV(IIntStream stream, int baseAddress, int layerIndex, float maxV) {
	if (layerIndex == 0)
	    setV0.set(stream, baseAddress + maxVOffset0, maxV);
	else if (layerIndex == 1)
	    setV1.set(stream, baseAddress + maxVOffset1, maxV);
	else
	    setV2.set(stream, baseAddress + maxVOffset2, maxV);
    }

    public final float getMinV(IIntStream stream, int baseAddress, int layerIndex) {
	return layerIndex == 0 ? getV0.get(stream, baseAddress + minVOffset0)
		: layerIndex == 1 ? getV1.get(stream, baseAddress + minVOffset1)
			: getV2.get(stream, baseAddress + minVOffset2);
    }

    public final void setMinV(IIntStream stream, int baseAddress, int layerIndex, float minV) {
	if (layerIndex == 0)
	    setV0.set(stream, baseAddress + minVOffset0, minV);
	else if (layerIndex == 1)
	    setV1.set(stream, baseAddress + minVOffset1, minV);
	else
	    setV2.set(stream, baseAddress + minVOffset2, minV);
    }

    public final String getTextureName(IIntStream stream, int baseAddress, int layerIndex) {
	final int handle = layerIndex == 0 ? getTexture0.get(stream, baseAddress + textureOffset01)
		: layerIndex == 1 ? getTexture1.get(stream, baseAddress + textureOffset01)
			: getTexture2.get(stream, baseAddress + textureOffset2);

	return handle == 0 ? null : textureHandler.fromHandle(handle);
    }

    public final void setTextureName(IIntStream stream, int baseAddress, int layerIndex, String textureName) {
	final int handle = textureName == null || textureName.isEmpty() ? 0 : textureHandler.toHandle(textureName);
	if (layerIndex == 0)
	    setTexture0.set(stream, baseAddress + textureOffset01, handle);
	else if (layerIndex == 1)
	    setTexture1.set(stream, baseAddress + textureOffset01, handle);
	else
	    setTexture2.set(stream, baseAddress + textureOffset2, handle);
    }

    public final int getVertexColor(IIntStream stream, int baseAddress, int layerIndex) {
	return layerIndex == 0 ? getColor0.get(stream, baseAddress + colorOffset0)
		: layerIndex == 1 ? getColor1.get(stream, baseAddress + colorOffset1)
			: getColor2.get(stream, baseAddress + colorOffset2);
    }

    public final void setVertexColor(IIntStream stream, int baseAddress, int layerIndex, int color) {
	if (layerIndex == 0)
	    setColor0.set(stream, baseAddress + colorOffset0, color);
	else if (layerIndex == 1)
	    setColor1.set(stream, baseAddress + colorOffset1, color);
	else
	    setColor2.set(stream, baseAddress + colorOffset2, color);
    }
}
