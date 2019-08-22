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
package grondag.xm.mesh;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

import grondag.fermion.bits.BitPacker32;
import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.mesh.polygon.Vec3f;
import net.minecraft.util.math.Direction;

@API(status = INTERNAL)
class MeshFormat {
    private static final BitPacker32<MeshFormat> BITPACKER = new BitPacker32<MeshFormat>(null, null);

    private static final BitPacker32<MeshFormat>.BooleanElement IS_MUTABLE = BITPACKER.createBooleanElement();

    public static boolean isMutable(int formatIn) {
        return IS_MUTABLE.getValue(formatIn);
    }

    public static int setMutable(int formatIn, boolean isMarked) {
        return IS_MUTABLE.setValue(isMarked, formatIn);
    }

    public static final int MUTABLE_FLAG = IS_MUTABLE.comparisonMask();

    private static final BitPacker32<MeshFormat>.IntElement VERTEX_COUNT = BITPACKER.createIntElement(3, 3 + 127);

    public static int getVertexCount(int formatIn) {
        return VERTEX_COUNT.getValue(formatIn);
    }

    public static int setVertexCount(int formatIn, int vertexCount) {
        assert vertexCount > 2;
        assert vertexCount <= 3 + 127;
        return VERTEX_COUNT.setValue(vertexCount, formatIn);
    }

    private static final BitPacker32<MeshFormat>.EnumElement<Direction> NOMINAL_FACE = BITPACKER.createEnumElement(Direction.class);

    public static Direction getNominalFace(int formatIn) {
        return NOMINAL_FACE.getValue(formatIn);
    }

    public static int setNominalFace(int formatIn, Direction face) {
        return NOMINAL_FACE.setValue(face, formatIn);
    }

    private static final BitPacker32<MeshFormat>.NullableEnumElement<Direction> CULL_FACE = BITPACKER.createNullableEnumElement(Direction.class);

    public static Direction getCullFace(int formatIn) {
        return CULL_FACE.getValue(formatIn);
    }

    public static int setCullFace(int formatIn, Direction face) {
        return CULL_FACE.setValue(face, formatIn);
    }
    
    private static final BitPacker32<MeshFormat>.BooleanElement IS_DELETED = BITPACKER.createBooleanElement();

    public static boolean isDeleted(int formatIn) {
        return IS_DELETED.getValue(formatIn);
    }

    public static int setDeleted(int formatIn, boolean isDeleted) {
        return IS_DELETED.setValue(isDeleted, formatIn);
    }

    // Note the packers below that affect poly / vertex layouts need to be adjacent
    // to
    // each other so that masks can be used as keys for encoders.

    private static final BitPacker32<MeshFormat>.BooleanElement HAS_LINK = BITPACKER.createBooleanElement();

    public static boolean isLinked(int formatIn) {
        return HAS_LINK.getValue(formatIn);
    }

    /**
     * IMPORTANT: changes poly layout and must be set before poly is written.
     */
    public static int setLinked(int formatIn, boolean isLinked) {
        return HAS_LINK.setValue(isLinked, formatIn);
    }

    private static final BitPacker32<MeshFormat>.BooleanElement HAS_TAG = BITPACKER.createBooleanElement();

    public static boolean isTagged(int formatIn) {
        return HAS_TAG.getValue(formatIn);
    }

    /**
     * IMPORTANT: changes poly layout and must be set before poly is written.
     */
    public static int setTagged(int formatIn, boolean isTagged) {
        return HAS_TAG.setValue(isTagged, formatIn);
    }

    /**
     * use full precision face normal - normal needs to be computed from vertices
     */
    public static final int FACE_NORMAL_FORMAT_COMPUTED = 0;
    /** use quantized normal - normal will be computed when poly is written */
    public static final int FACE_NORMAL_FORMAT_QUANTIZED = 1;
    /**
     * use normal of nominal face - used when a poly about to be written is found to
     * have a face normal that matches the nominal face. Requires no storage.
     */
    public static final int FACE_NORMAL_FORMAT_NOMINAL = 2;

    private static final BitPacker32<MeshFormat>.IntElement FACE_NORMAL_FORMAT = BITPACKER.createIntElement(4);

    public static int getFaceNormalFormat(int formatIn) {
        return FACE_NORMAL_FORMAT.getValue(formatIn);
    }

    public static int setFaceNormalFormat(int formatIn, int format) {
        return FACE_NORMAL_FORMAT.setValue(format, formatIn);
    }

    private static final BitPacker32<MeshFormat>.BooleanElement HALF_PRECISION_POLY_UV = BITPACKER.createBooleanElement();

    public static boolean isHalfPrecisionPolyUV(int formatIn) {
        return HALF_PRECISION_POLY_UV.getValue(formatIn);
    }

    public static int setHalfPrecisionPolyUV(int formatIn, boolean isHalf) {
        return HALF_PRECISION_POLY_UV.setValue(isHalf, formatIn);
    }

    private static final BitPacker32<MeshFormat>.IntElement LAYER_COUNT = BITPACKER.createIntElement(1, 3);

    public static int getLayerCount(int formatIn) {
        return LAYER_COUNT.getValue(formatIn);
    }

    public static int setLayerCount(int formatIn, int layerCount) {
        return LAYER_COUNT.setValue(layerCount, formatIn);
    }

    /** all vertices are white */
    public static final int VERTEX_COLOR_WHITE = 0;
    /** all vertices have same color, irrespective of layer */
    public static final int VERTEX_COLOR_SAME = 1;
    /** all vertices in a layer share same color, layers are different */
    public static final int VERTEX_COLOR_SAME_BY_LAYER = 2;
    /** assign vertex color to each layer/vertex */
    public static final int VERTEX_COLOR_PER_VERTEX_LAYER = 3;

    private static final BitPacker32<MeshFormat>.IntElement VERTEX_COLOR_FORMAT = BITPACKER.createIntElement(4);

    public static int getVertexColorFormat(int formatIn) {
        return VERTEX_COLOR_FORMAT.getValue(formatIn);
    }

    public static int setVertexColorFormat(int formatIn, int colorFormat) {
        return VERTEX_COLOR_FORMAT.setValue(colorFormat, formatIn);
    }

    private static final BitPacker32<MeshFormat>.BooleanElement QUANTIZED_POS = BITPACKER.createBooleanElement();

    public static boolean isQuantizedPos(int formatIn) {
        return QUANTIZED_POS.getValue(formatIn);
    }

    public static int setQuantizedPos(int formatIn, boolean isQuantized) {
        return QUANTIZED_POS.setValue(isQuantized, formatIn);
    }

    /** use face normal as vertex normals */
    public static final int VERTEX_NORMAL_FACE = 0;
    /** quantized normals */
    public static final int VERTEX_NORMAL_QUANTIZED = 1;
    /** full precision normals */
    public static final int VERTEX_NORMAL_REGULAR = 2;

    private static final BitPacker32<MeshFormat>.IntElement VERTEX_NORMAL_FORMAT = BITPACKER.createIntElement(3);

    public static int getVertexNormalFormat(int formatIn) {
        return VERTEX_NORMAL_FORMAT.getValue(formatIn);
    }

    public static int setVertexNormalFormat(int formatIn, int normFormat) {
        return VERTEX_NORMAL_FORMAT.setValue(normFormat, formatIn);
    }

    /** different UV in each layer */
    public static final int VERTEX_UV_BY_LAYER = 0;
    /** all layers have same UV */
    public static final int VERTEX_UV_SAME = 1;
    /** different UV in each layer, half precision */
    public static final int VERTEX_UV_BY_LAYER_HALF = 2;
    /** all layers have same UV, half precision */
    public static final int VERTEX_UV_SAME_HALF = 3;

    private static final BitPacker32<MeshFormat>.IntElement VERTEX_UV_FORMAT = BITPACKER.createIntElement(4);

    public static int getVertexUVFormat(int formatIn) {
        return VERTEX_UV_FORMAT.getValue(formatIn);
    }

    public static int setVertexUVFormat(int formatIn, int uvFormat) {
        return VERTEX_UV_FORMAT.setValue(uvFormat, formatIn);
    }

    /** values are zero for every vertex */
    public static final int VERTEX_GLOW_NONE = 0;
    /** all vertices have same non-zero value */
    public static final int VERTEX_GLOW_SAME = 1;
    /** each vertex has a glow value */
    public static final int VERTEX_GLOW_PER_VERTEX = 2;

    private static final BitPacker32<MeshFormat>.IntElement VERTEX_GLOW_FORMAT = BITPACKER.createIntElement(3);

    public static int getVertexGlowFormat(int formatIn) {
        return VERTEX_GLOW_FORMAT.getValue(formatIn);
    }

    public static int setVertexGlowFormat(int formatIn, int glowFormat) {
        return VERTEX_GLOW_FORMAT.setValue(glowFormat, formatIn);
    }
    
    // static features
//    isMutable   2   1       poly    yes yes
//    vertexCount 128 7       vertex  no  no
//    nominalFace 6   3       poly    no  no
//    isMarked    2   1   stream metadata poly    no  no
//    isDeleted   2   1   stream metadata poly    no  no

    // poly-encoder features
//    isLinked
//    isTagged
//    isCSG
//    faceNormal  4   2   Dynamic/Cached/Quantized/Nominal    poly    yes no
//    uvFormat    2   1   full/half   layer   yes no

    // poly and vertex features
//    layerCount  3   2   if mutable, how many layers are used    layer   yes yes
//    vertexColor 4   2   white/same/same by layer/each   vertex layer    yes yes

    // vertex encoder features
//    vertexPos   2   1   Regular/Quantized   vertex  no  yes
//    vertexNormals   3   2   Face/Regular/Quantized  vertex  no  yes
//    vertexUV    4   2   Same/Per Layer/Same Half/Per Layer Half vertex layer    no  yes

    // glow encoder features
//    vertexGlow  3   2   None/Same/Per Vertex    vertex layer    no  no

    final static int POLY_FORMAT_SHIFTED_MASK;
    final static int POLY_FORMAT_SHIFT;
    final static int POLY_FORMAT_COUNT;

    final static int POLY_FORMAT_MUTABLE_UNSHIFTED_MASK;
    final static int POLY_FORMAT_MUTABLE_BITS;

    final static int VERTEX_FORMAT_MASK;
    final static int VERTEX_FORMAT_SHIFT;
    final static int VERTEX_FORMAT_COUNT;

    public static final int HAS_LINK_FLAG;
    public static final int HAS_TAG_FLAG;

    static {
        final int polyMask = HAS_LINK.comparisonMask() | HAS_TAG.comparisonMask() | FACE_NORMAL_FORMAT.comparisonMask()
                | HALF_PRECISION_POLY_UV.comparisonMask() | LAYER_COUNT.comparisonMask() | VERTEX_COLOR_FORMAT.comparisonMask();

        POLY_FORMAT_SHIFT = Integer.numberOfTrailingZeros(polyMask);
        POLY_FORMAT_SHIFTED_MASK = polyMask >> POLY_FORMAT_SHIFT;
        POLY_FORMAT_COUNT = POLY_FORMAT_SHIFTED_MASK + 1;

        HAS_LINK_FLAG = HAS_LINK.comparisonMask();
        HAS_TAG_FLAG = HAS_TAG.comparisonMask();
        
        // force certain features to full flexibility for mutable formats
        // doesn't include CSG bounds
        POLY_FORMAT_MUTABLE_UNSHIFTED_MASK = ~(HAS_LINK.comparisonMask() | HAS_TAG.comparisonMask() | FACE_NORMAL_FORMAT.comparisonMask()
                | HALF_PRECISION_POLY_UV.comparisonMask() | LAYER_COUNT.comparisonMask() | VERTEX_COLOR_FORMAT.comparisonMask());

        int mutableBits = setLinked(0, true);
        mutableBits = setTagged(mutableBits, true);
        mutableBits = setFaceNormalFormat(mutableBits, FACE_NORMAL_FORMAT_COMPUTED);
        mutableBits = setHalfPrecisionPolyUV(mutableBits, false);
        mutableBits = setLayerCount(mutableBits, 3);
        mutableBits = setVertexColorFormat(mutableBits, VERTEX_COLOR_PER_VERTEX_LAYER);
        POLY_FORMAT_MUTABLE_BITS = mutableBits;

        final int vertexMask = LAYER_COUNT.comparisonMask() | VERTEX_COLOR_FORMAT.comparisonMask() | QUANTIZED_POS.comparisonMask()
                | VERTEX_NORMAL_FORMAT.comparisonMask() | VERTEX_UV_FORMAT.comparisonMask();

        VERTEX_FORMAT_SHIFT = Integer.numberOfTrailingZeros(vertexMask);
        VERTEX_FORMAT_MASK = vertexMask >> VERTEX_FORMAT_SHIFT;
        VERTEX_FORMAT_COUNT = VERTEX_FORMAT_MASK + 1;
        
        assert BITPACKER.bitLength() <= 32;
    }

    public static int polyFormatKey(int formatIn) {
        // force certain features to full flexibility for mutable formats
        if (isMutable(formatIn))
            formatIn = (formatIn & POLY_FORMAT_MUTABLE_UNSHIFTED_MASK) | POLY_FORMAT_MUTABLE_BITS;
        return (formatIn >> POLY_FORMAT_SHIFT) & POLY_FORMAT_SHIFTED_MASK;
    }

    public static int vertexFormatKey(int formatIn) {
        return (formatIn >> VERTEX_FORMAT_SHIFT) & VERTEX_FORMAT_MASK;
    }

    /**
     * Returns smallest format that contain all layers and vertices of the given
     * poly, plus any metadata declared in formatTags (tags, links, or bounds).
     * Never copies marks.
     * <p>
     * 
     * Ignore mutable flag in input poly and flag - output format will always have
     * mutable = false because this meant for optimal storage and there would be no
     * guarantee changes could be stored in the format.
     */
    public static int minimalFixedFormat(Polygon polyIn, int formatFlags) {
        int result = formatFlags;
        final int layerCount = polyIn.spriteDepth();
        assert layerCount >= 1;

        final int vertexCount = polyIn.vertexCount();
        assert vertexCount >= 3;

        if(polyIn.tag() != Polygon.NO_LINK_OR_TAG) {
            result |= HAS_TAG_FLAG;
        }
        
        result = setLayerCount(result, layerCount);
        result = setVertexCount(result, vertexCount);

        assert !polyIn.isDeleted();

        Direction nominalFace = polyIn.nominalFace();
        result = setNominalFace(result, nominalFace);
        
        result = setCullFace(result, polyIn.cullFace());

        Vec3f faceNormal = polyIn.faceNormal();
        if (faceNormal.equals(Vec3f.forFace(nominalFace)))
            result = setFaceNormalFormat(result, FACE_NORMAL_FORMAT_NOMINAL);
        else
            result = setFaceNormalFormat(result, FACE_NORMAL_FORMAT_COMPUTED);

        boolean allFaceNormal = true;
        int firstGlow = polyIn.glow(0);
        boolean allSameGlow = true;
        boolean allSameUV = layerCount > 1;

        int color0 = polyIn.color(0, 0);
        int color1 = layerCount > 1 ? polyIn.color(0, 1) : 0;
        int color2 = layerCount == 3 ? polyIn.color(0, 2) : 0;
        /**
         * True if all vertices in each layer are same color as each other. Does not
         * mean all layers are same color.
         */
        boolean allVertexSameColor = true;

        for (int v = 0; v < vertexCount; v++) {
            // glow
            if (allSameGlow && v > 0 && polyIn.glow(v) != firstGlow)
                allSameGlow = false;

            // vertex normal
            if (allFaceNormal && polyIn.hasNormal(v) && !polyIn.vertexNormal(v).equals(faceNormal))
                allFaceNormal = false;

            if (allVertexSameColor & v > 0 && polyIn.color(v, 0) != color0)
                allVertexSameColor = false;

            if (layerCount > 1) {
                // vertex uv format
                if (allSameUV && (polyIn.u(v, 0) != polyIn.u(v, 1) || polyIn.v(v, 0) != polyIn.v(v, 1)))
                    allSameUV = false;

                // vertex color
                if (allVertexSameColor & v > 0 && polyIn.color(v, 1) != color1)
                    allVertexSameColor = false;

                if (layerCount == 3) {
                    // vertex uv format
                    if (allSameUV && (polyIn.u(v, 0) != polyIn.u(v, 2) || polyIn.v(v, 0) != polyIn.v(v, 2)))
                        allSameUV = false;

                    // vertex color
                    if (allVertexSameColor & v > 0 && polyIn.color(v, 2) != color2)
                        allVertexSameColor = false;
                }
            }
        }

        result = setVertexNormalFormat(result, allFaceNormal ? VERTEX_NORMAL_FACE : VERTEX_NORMAL_REGULAR);

        if (allSameGlow)
            result = setVertexGlowFormat(result, firstGlow == 0 ? VERTEX_GLOW_NONE : VERTEX_GLOW_SAME);
        else
            result = setVertexGlowFormat(result, VERTEX_GLOW_PER_VERTEX);

        result = setVertexUVFormat(result, allSameUV ? VERTEX_UV_SAME : VERTEX_UV_BY_LAYER);

        if (allVertexSameColor) {
            if (layerCount == 1 || (color0 == color1 && (layerCount == 2 || color1 == color2)))
                result = setVertexColorFormat(result, color0 == 0xFFFFFFFF ? VERTEX_COLOR_WHITE : VERTEX_COLOR_SAME);
            else
                result = setVertexColorFormat(result, VERTEX_COLOR_SAME_BY_LAYER);
        } else
            result = setVertexColorFormat(result, VERTEX_COLOR_PER_VERTEX_LAYER);

        return result & ~MUTABLE_FLAG;
    }

    /**
     * Computes the size of the input polygon, in integers, if stored using the
     * optimal format given by {@link #minimalFixedFormat(Polygon, int)}. Includes
     * size vertex data.
     */
    public static int minimalFixedSize(Polygon polyIn, int formatFlags) {
        return polyStride(minimalFixedFormat(polyIn, formatFlags), true);
    }

    /**
     * Computes size of a polygon with the given format, in integers. Size includes
     * vertex data if specified. Otherwise everything except.
     */
    public static int polyStride(int newFormat, boolean includeVertices) {
        int result = 1 + StaticEncoder.INTEGER_WIDTH + PolyEncoder.get(newFormat).stride();
        if (includeVertices)
            result += VertexEncoder.get(newFormat).vertexStride() * getVertexCount(newFormat) + GlowEncoder.get(newFormat).stride(newFormat);
        return result;
    }
}
