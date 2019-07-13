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

package grondag.xm2.primitives.stream;

import grondag.fermion.intstream.IIntStream;
import grondag.fermion.structures.IndexedInterner;
import grondag.fermion.varia.BitPacker32;
import grondag.fermion.world.Rotation;
import grondag.xm2.surface.impl.XmSurfaceImpl;
import net.minecraft.block.BlockRenderLayer;

public class StaticEncoder {
    private static final BitPacker32<StaticEncoder> BITPACKER = new BitPacker32<StaticEncoder>(null, null);

    private static final IndexedInterner<XmSurfaceImpl> xmSurfaces = new IndexedInterner<>(XmSurfaceImpl.class);
    
    private static final int BIT_OFFSET = 1;
    private static final int TEXTURE_PIPELINE_OFFSET = 2;
    // PERF: can probably pack this into bitpacker once old surface is gone and using paint
    private static final int SURFACE_OFFSET = 3;
    private static final int UV_WRAP_DIST_OFFSET = 4;
    
    /**
     * How many integers in the stream are needed for static encoding. This is in
     * addition to the format header.
     */
    public static final int INTEGER_WIDTH = 4;

    public static XmSurfaceImpl surface(IIntStream stream, int baseAddress) {
    	return xmSurfaces.fromHandle(stream.get(baseAddress + SURFACE_OFFSET));
    }
    
    public static void surface(IIntStream stream, int baseAddress, XmSurfaceImpl surface) {
    	stream.set(baseAddress + SURFACE_OFFSET, xmSurfaces.toHandle(surface));
    }
    
    public static float uvWrapDistance(IIntStream stream, int baseAddress) {
    	return Float.intBitsToFloat(stream.get(baseAddress + UV_WRAP_DIST_OFFSET));
    }
    
    public static void uvWrapDistance(IIntStream stream, int baseAddress, float uvWrapDistance) {
    	stream.set(baseAddress + UV_WRAP_DIST_OFFSET, Float.floatToRawIntBits(uvWrapDistance));
    }
    
    public static int getPipelineIndex(IIntStream stream, int baseAddress) {
        return stream.get(baseAddress + TEXTURE_PIPELINE_OFFSET) >>> 16;
    }

    public static void setPipelineIndex(IIntStream stream, int baseAddress, int pipelineIndex) {
        final int surfaceVal = stream.get(baseAddress + TEXTURE_PIPELINE_OFFSET) & 0x0000FFFF;
        stream.set(baseAddress + TEXTURE_PIPELINE_OFFSET, surfaceVal | (pipelineIndex << 16));
    }

    @SuppressWarnings("unchecked")
    private static final BitPacker32<StaticEncoder>.BooleanElement[] CONTRACT_UV = (BitPacker32<StaticEncoder>.BooleanElement[]) new BitPacker32<?>.BooleanElement[3];

    static {
        CONTRACT_UV[0] = BITPACKER.createBooleanElement();
        CONTRACT_UV[1] = BITPACKER.createBooleanElement();
        CONTRACT_UV[2] = BITPACKER.createBooleanElement();
    }

    public static boolean shouldContractUVs(IIntStream stream, int baseAddress, int layerIndex) {
        // want default to be true - easiest way is to flip here so that 0 bit gives
        // right default
        return !CONTRACT_UV[layerIndex].getValue(stream.get(baseAddress + BIT_OFFSET));
    }

    public static void setContractUVs(IIntStream stream, int baseAddress, int layerIndex, boolean shouldContract) {
        // want default to be true - easiest way is to flip here so that 0 bit gives
        // right default
        final int bits = stream.get(baseAddress + BIT_OFFSET);
        stream.set(baseAddress + BIT_OFFSET, CONTRACT_UV[layerIndex].setValue(!shouldContract, bits));
    }

    @SuppressWarnings("unchecked")
    private static final BitPacker32<StaticEncoder>.EnumElement<Rotation>[] ROTATION = (BitPacker32<StaticEncoder>.EnumElement<Rotation>[]) new BitPacker32<?>.EnumElement<?>[3];

    static {
        ROTATION[0] = BITPACKER.createEnumElement(Rotation.class);
        ROTATION[1] = BITPACKER.createEnumElement(Rotation.class);
        ROTATION[2] = BITPACKER.createEnumElement(Rotation.class);
    }

    public static Rotation getRotation(IIntStream stream, int baseAddress, int layerIndex) {
        return ROTATION[layerIndex].getValue(stream.get(baseAddress + BIT_OFFSET));
    }

    public static void setRotation(IIntStream stream, int baseAddress, int layerIndex, Rotation rotation) {
        final int bits = stream.get(baseAddress + BIT_OFFSET);
        stream.set(baseAddress + BIT_OFFSET, ROTATION[layerIndex].setValue(rotation, bits));
    }

    private static final BitPacker32<StaticEncoder>.IntElement SALT = BITPACKER.createIntElement(256);

    public static int getTextureSalt(IIntStream stream, int baseAddress) {
        return SALT.getValue(stream.get(baseAddress + BIT_OFFSET));
    }

    public static void setTextureSalt(IIntStream stream, int baseAddress, int salt) {
        final int bits = stream.get(baseAddress + BIT_OFFSET);
        stream.set(baseAddress + BIT_OFFSET, SALT.setValue(salt, bits));
    }

    @SuppressWarnings("unchecked")
    private static final BitPacker32<StaticEncoder>.BooleanElement[] LOCK_UV = (BitPacker32<StaticEncoder>.BooleanElement[]) new BitPacker32<?>.BooleanElement[3];

    static {
        LOCK_UV[0] = BITPACKER.createBooleanElement();
        LOCK_UV[1] = BITPACKER.createBooleanElement();
        LOCK_UV[2] = BITPACKER.createBooleanElement();
    }

    public static boolean isLockUV(IIntStream stream, int baseAddress, int layerIndex) {
        return LOCK_UV[layerIndex].getValue(stream.get(baseAddress + BIT_OFFSET));
    }

    public static void setLockUV(IIntStream stream, int baseAddress, int layerIndex, boolean lockUV) {
        final int bits = stream.get(baseAddress + BIT_OFFSET);
        stream.set(baseAddress + BIT_OFFSET, LOCK_UV[layerIndex].setValue(lockUV, bits));
    }

    @SuppressWarnings("unchecked")
    private static final BitPacker32<StaticEncoder>.BooleanElement[] EMISSIVE = (BitPacker32<StaticEncoder>.BooleanElement[]) new BitPacker32<?>.BooleanElement[3];

    static {
        EMISSIVE[0] = BITPACKER.createBooleanElement();
        EMISSIVE[1] = BITPACKER.createBooleanElement();
        EMISSIVE[2] = BITPACKER.createBooleanElement();
    }

    public static boolean isEmissive(IIntStream stream, int baseAddress, int layerIndex) {
        return EMISSIVE[layerIndex].getValue(stream.get(baseAddress + BIT_OFFSET));
    }

    public static void setEmissive(IIntStream stream, int baseAddress, int layerIndex, boolean isEmissive) {
        final int bits = stream.get(baseAddress + BIT_OFFSET);
        stream.set(baseAddress + BIT_OFFSET, EMISSIVE[layerIndex].setValue(isEmissive, bits));
    }

    @SuppressWarnings("unchecked")
    private static final BitPacker32<StaticEncoder>.EnumElement<BlockRenderLayer>[] RENDER_LAYER = (BitPacker32<StaticEncoder>.EnumElement<BlockRenderLayer>[]) new BitPacker32<?>.EnumElement<?>[3];

    static {
        RENDER_LAYER[0] = BITPACKER.createEnumElement(BlockRenderLayer.class);
        RENDER_LAYER[1] = BITPACKER.createEnumElement(BlockRenderLayer.class);
        RENDER_LAYER[2] = BITPACKER.createEnumElement(BlockRenderLayer.class);

        assert BITPACKER.bitLength() <= 32;
    }

    public static BlockRenderLayer getRenderLayer(IIntStream stream, int baseAddress, int layerIndex) {
        return RENDER_LAYER[layerIndex].getValue(stream.get(baseAddress + BIT_OFFSET));
    }

    public static void setRenderLayer(IIntStream stream, int baseAddress, int layerIndex, BlockRenderLayer layer) {
        final int bits = stream.get(baseAddress + BIT_OFFSET);
        stream.set(baseAddress + BIT_OFFSET, RENDER_LAYER[layerIndex].setValue(layer, bits));
    }
}
