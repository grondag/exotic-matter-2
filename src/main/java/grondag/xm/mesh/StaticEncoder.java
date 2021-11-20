/*
 * Copyright © Original Authors
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
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.mesh;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.vram.bitkit.BitPacker32;
import io.vram.sc.concurrency.IndexedInterner;

import grondag.fermion.intstream.IntStream;
import grondag.xm.api.paint.PaintBlendMode;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.texture.TextureOrientation;

@Internal
class StaticEncoder {
	private static final BitPacker32<StaticEncoder> BITPACKER = new BitPacker32<>(null, null);
	private static final BitPacker32<StaticEncoder> BITPACKER_2 = new BitPacker32<>(null, null);

	private static final IndexedInterner<XmSurface> xmSurfaces = new IndexedInterner<>(XmSurface.class);

	private static final int BIT_OFFSET = 1;
	private static final int BIT_OFFSET_2 = 2;
	private static final int TEXTURE_PIPELINE_OFFSET = 3;
	private static final int SURFACE_OFFSET = 4;
	private static final int UV_WRAP_DIST_OFFSET = 5;

	/**
	 * How many integers in the stream are needed for static encoding. This is in
	 * addition to the format header.
	 */
	public static final int INTEGER_WIDTH = 5;

	public static XmSurface surface(IntStream stream, int baseAddress) {
		return xmSurfaces.fromIndex(stream.get(baseAddress + SURFACE_OFFSET));
	}

	public static void surface(IntStream stream, int baseAddress, XmSurface surface) {
		stream.set(baseAddress + SURFACE_OFFSET, xmSurfaces.toIndex(surface));
	}

	public static float uvWrapDistance(IntStream stream, int baseAddress) {
		return Float.intBitsToFloat(stream.get(baseAddress + UV_WRAP_DIST_OFFSET));
	}

	public static void uvWrapDistance(IntStream stream, int baseAddress, float uvWrapDistance) {
		stream.set(baseAddress + UV_WRAP_DIST_OFFSET, Float.floatToRawIntBits(uvWrapDistance));
	}

	public static int getPipelineIndex(IntStream stream, int baseAddress) {
		return stream.get(baseAddress + TEXTURE_PIPELINE_OFFSET) >>> 16;
	}

	public static void setPipelineIndex(IntStream stream, int baseAddress, int pipelineIndex) {
		final int surfaceVal = stream.get(baseAddress + TEXTURE_PIPELINE_OFFSET) & 0x0000FFFF;
		stream.set(baseAddress + TEXTURE_PIPELINE_OFFSET, surfaceVal | (pipelineIndex << 16));
	}

	@SuppressWarnings("unchecked")
	private static final BitPacker32<StaticEncoder>.BooleanElement[] CONTRACT_UV = (BitPacker32<StaticEncoder>.BooleanElement[]) new BitPacker32<?>.BooleanElement[3];

	static {
		CONTRACT_UV[0] = BITPACKER_2.createBooleanElement();
		CONTRACT_UV[1] = BITPACKER_2.createBooleanElement();
		CONTRACT_UV[2] = BITPACKER_2.createBooleanElement();
	}

	public static boolean shouldContractUVs(IntStream stream, int baseAddress, int layerIndex) {
		// want default to be true - easiest way is to flip here so that 0 bit gives
		// right default
		return !CONTRACT_UV[layerIndex].getValue(stream.get(baseAddress + BIT_OFFSET_2));
	}

	public static void setContractUVs(IntStream stream, int baseAddress, int layerIndex, boolean shouldContract) {
		// want default to be true - easiest way is to flip here so that 0 bit gives
		// right default
		final int bits = stream.get(baseAddress + BIT_OFFSET_2);
		stream.set(baseAddress + BIT_OFFSET_2, CONTRACT_UV[layerIndex].setValue(!shouldContract, bits));
	}

	@SuppressWarnings("unchecked")
	private static final BitPacker32<StaticEncoder>.EnumElement<TextureOrientation>[] ROTATION = (BitPacker32<StaticEncoder>.EnumElement<TextureOrientation>[]) new BitPacker32<?>.EnumElement<?>[3];

	static {
		ROTATION[0] = BITPACKER.createEnumElement(TextureOrientation.class);
		ROTATION[1] = BITPACKER.createEnumElement(TextureOrientation.class);
		ROTATION[2] = BITPACKER.createEnumElement(TextureOrientation.class);
	}

	public static TextureOrientation getRotation(IntStream stream, int baseAddress, int layerIndex) {
		return ROTATION[layerIndex].getValue(stream.get(baseAddress + BIT_OFFSET));
	}

	public static void setRotation(IntStream stream, int baseAddress, int layerIndex, TextureOrientation rotation) {
		final int bits = stream.get(baseAddress + BIT_OFFSET);
		stream.set(baseAddress + BIT_OFFSET, ROTATION[layerIndex].setValue(rotation, bits));
	}

	private static final BitPacker32<StaticEncoder>.IntElement SALT = BITPACKER_2.createIntElement(256);

	public static int getTextureSalt(IntStream stream, int baseAddress) {
		return SALT.getValue(stream.get(baseAddress + BIT_OFFSET_2));
	}

	public static void setTextureSalt(IntStream stream, int baseAddress, int salt) {
		final int bits = stream.get(baseAddress + BIT_OFFSET_2);
		stream.set(baseAddress + BIT_OFFSET_2, SALT.setValue(salt, bits));
	}

	@SuppressWarnings("unchecked")
	private static final BitPacker32<StaticEncoder>.BooleanElement[] LOCK_UV = (BitPacker32<StaticEncoder>.BooleanElement[]) new BitPacker32<?>.BooleanElement[3];

	static {
		LOCK_UV[0] = BITPACKER_2.createBooleanElement();
		LOCK_UV[1] = BITPACKER_2.createBooleanElement();
		LOCK_UV[2] = BITPACKER_2.createBooleanElement();
	}

	public static boolean isLockUV(IntStream stream, int baseAddress, int layerIndex) {
		return LOCK_UV[layerIndex].getValue(stream.get(baseAddress + BIT_OFFSET_2));
	}

	public static void setLockUV(IntStream stream, int baseAddress, int layerIndex, boolean lockUV) {
		final int bits = stream.get(baseAddress + BIT_OFFSET_2);
		stream.set(baseAddress + BIT_OFFSET_2, LOCK_UV[layerIndex].setValue(lockUV, bits));
	}

	//PERF: improve LOR
	@SuppressWarnings("unchecked")
	private static final BitPacker32<StaticEncoder>.BooleanElement[] EMISSIVE = (BitPacker32<StaticEncoder>.BooleanElement[]) new BitPacker32<?>.BooleanElement[3];
	@SuppressWarnings("unchecked")
	private static final BitPacker32<StaticEncoder>.BooleanElement[] AO = (BitPacker32<StaticEncoder>.BooleanElement[]) new BitPacker32<?>.BooleanElement[3];
	@SuppressWarnings("unchecked")
	private static final BitPacker32<StaticEncoder>.BooleanElement[] DIFFUSE = (BitPacker32<StaticEncoder>.BooleanElement[]) new BitPacker32<?>.BooleanElement[3];

	static {
		EMISSIVE[0] = BITPACKER.createBooleanElement();
		EMISSIVE[1] = BITPACKER.createBooleanElement();
		EMISSIVE[2] = BITPACKER.createBooleanElement();
		AO[0] = BITPACKER.createBooleanElement();
		AO[1] = BITPACKER.createBooleanElement();
		AO[2] = BITPACKER.createBooleanElement();
		DIFFUSE[0] = BITPACKER.createBooleanElement();
		DIFFUSE[1] = BITPACKER.createBooleanElement();
		DIFFUSE[2] = BITPACKER.createBooleanElement();
	}

	public static boolean isEmissive(IntStream stream, int baseAddress, int layerIndex) {
		return EMISSIVE[layerIndex].getValue(stream.get(baseAddress + BIT_OFFSET));
	}

	public static void setEmissive(IntStream stream, int baseAddress, int layerIndex, boolean disable) {
		final int bits = stream.get(baseAddress + BIT_OFFSET);
		stream.set(baseAddress + BIT_OFFSET, EMISSIVE[layerIndex].setValue(disable, bits));
	}

	public static boolean disableAo(IntStream stream, int baseAddress, int layerIndex) {
		return AO[layerIndex].getValue(stream.get(baseAddress + BIT_OFFSET));
	}

	public static void disableAo(IntStream stream, int baseAddress, int layerIndex, boolean disable) {
		final int bits = stream.get(baseAddress + BIT_OFFSET);
		stream.set(baseAddress + BIT_OFFSET, AO[layerIndex].setValue(disable, bits));
	}

	public static boolean disableDiffuse(IntStream stream, int baseAddress, int layerIndex) {
		return DIFFUSE[layerIndex].getValue(stream.get(baseAddress + BIT_OFFSET));
	}

	public static void disableDiffuse(IntStream stream, int baseAddress, int layerIndex, boolean isEmissive) {
		final int bits = stream.get(baseAddress + BIT_OFFSET);
		stream.set(baseAddress + BIT_OFFSET, DIFFUSE[layerIndex].setValue(isEmissive, bits));
	}

	private static final BitPacker32<StaticEncoder>.EnumElement<PaintBlendMode> RENDER_LAYER = BITPACKER.createEnumElement(PaintBlendMode.class);

	static {
		assert BITPACKER.bitLength() <= 32;
		assert BITPACKER_2.bitLength() <= 32;
	}

	public static PaintBlendMode getRenderLayer(IntStream stream, int baseAddress) {
		return RENDER_LAYER.getValue(stream.get(baseAddress + BIT_OFFSET));
	}

	public static void setRenderLayer(IntStream stream, int baseAddress, PaintBlendMode layer) {
		final int bits = stream.get(baseAddress + BIT_OFFSET);
		stream.set(baseAddress + BIT_OFFSET, RENDER_LAYER.setValue(layer, bits));
	}
}
