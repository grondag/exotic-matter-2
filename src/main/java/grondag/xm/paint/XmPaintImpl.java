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
package grondag.xm.paint;

import static grondag.xm.api.paint.XmPaint.MAX_TEXTURE_DEPTH;
import static org.apiguardian.api.API.Status.INTERNAL;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apiguardian.api.API;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import grondag.fermion.bits.BitPacker32;
import grondag.xm.Xm;
import grondag.xm.api.paint.PaintBlendMode;
import grondag.xm.api.paint.PaintIndex;
import grondag.xm.api.paint.VertexProcessor;
import grondag.xm.api.paint.VertexProcessorRegistry;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.paint.XmPaintFinder;
import grondag.xm.api.paint.XmPaintRegistry;
import grondag.xm.api.texture.TextureSet;
import grondag.xm.api.texture.TextureSetRegistry;

@API(status = INTERNAL)
public class XmPaintImpl {
	private static final BitPacker32<XmPaintImpl> PAINT_BITS = new BitPacker32<>(p -> p.paintBits, (p, b) -> p.paintBits = b);

	public static Finder finder() {
		return new Finder();
	}

	// offset additively by sprite index.
	private static final int EMISSIVE_INDEX_START = 0;
	private static final int DIFFUSE_INDEX_START = EMISSIVE_INDEX_START + MAX_TEXTURE_DEPTH;
	private static final int AO_INDEX_START = DIFFUSE_INDEX_START + MAX_TEXTURE_DEPTH;
	private static final int COLOR_DISABLE_INDEX_START = AO_INDEX_START + MAX_TEXTURE_DEPTH;

	@SuppressWarnings("unchecked")
	private static final BitPacker32<XmPaintImpl>.BooleanElement[] FLAGS = new BitPacker32.BooleanElement[COLOR_DISABLE_INDEX_START + MAX_TEXTURE_DEPTH];

	private static final BitPacker32<XmPaintImpl>.EnumElement<PaintBlendMode> BLEND_MODE = PAINT_BITS.createEnumElement(PaintBlendMode.class);

	private static final BitPacker32<XmPaintImpl>.IntElement TEXTURE_DEPTH;

	private static final int DEFAULT_PAINT_BITS;

	private static final Object2ObjectOpenHashMap<XmPaintImpl, Value> MAP = new Object2ObjectOpenHashMap<>();

	static {
		TEXTURE_DEPTH = PAINT_BITS.createIntElement(1, MAX_TEXTURE_DEPTH);

		FLAGS[EMISSIVE_INDEX_START + 0] = PAINT_BITS.createBooleanElement();
		FLAGS[EMISSIVE_INDEX_START + 1] = PAINT_BITS.createBooleanElement();
		FLAGS[EMISSIVE_INDEX_START + 2] = PAINT_BITS.createBooleanElement();
		FLAGS[DIFFUSE_INDEX_START + 0] = PAINT_BITS.createBooleanElement();
		FLAGS[AO_INDEX_START + 0] = PAINT_BITS.createBooleanElement();
		FLAGS[DIFFUSE_INDEX_START + 1] = PAINT_BITS.createBooleanElement();
		FLAGS[AO_INDEX_START + 1] = PAINT_BITS.createBooleanElement();
		FLAGS[DIFFUSE_INDEX_START + 2] = PAINT_BITS.createBooleanElement();
		FLAGS[AO_INDEX_START + 2] = PAINT_BITS.createBooleanElement();
		FLAGS[COLOR_DISABLE_INDEX_START + 0] = PAINT_BITS.createBooleanElement();
		FLAGS[COLOR_DISABLE_INDEX_START + 1] = PAINT_BITS.createBooleanElement();
		FLAGS[COLOR_DISABLE_INDEX_START + 2] = PAINT_BITS.createBooleanElement();


		assert PAINT_BITS.bitLength() <= 32;

		DEFAULT_PAINT_BITS = BLEND_MODE.setValue(PaintBlendMode.DEFAULT, 0);
	}

	/** null for anonymous and indexed */
	@Nullable protected Identifier id;

	/** XmPaint.NO_INDEX for anonymous and registered */
	protected int index = XmPaint.NO_INDEX;

	protected int paintBits = DEFAULT_PAINT_BITS;
	protected int color0 = 0xFFFFFFFF;
	protected int color1 = 0xFFFFFFFF;
	protected int color2 = 0xFFFFFFFF;
	protected Identifier shader = null;
	protected Identifier condition = null;
	protected VertexProcessor vertexProcessor0 = VertexProcessorDefault.INSTANCE;
	protected VertexProcessor vertexProcessor1 = VertexProcessorDefault.INSTANCE;
	protected VertexProcessor vertexProcessor2 = VertexProcessorDefault.INSTANCE;
	protected TextureSet textureSet0 = TextureSet.none();
	protected TextureSet textureSet1 = textureSet0;
	protected TextureSet textureSet2 = textureSet0;

	protected void copyFrom(XmPaintImpl template) {
		paintBits = template.paintBits;
		color0 = template.color0;
		color1 = template.color1;
		color2 = template.color2;
		vertexProcessor0 = template.vertexProcessor0;
		vertexProcessor1 = template.vertexProcessor1;
		vertexProcessor2 = template.vertexProcessor2;
		textureSet0 = template.textureSet0;
		textureSet1 = template.textureSet1;
		textureSet2 = template.textureSet2;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof XmPaintImpl) {
			final XmPaintImpl other = (XmPaintImpl) obj;

			if (id != null) {
				return id.equals(other.id);
			} else if (index !=  XmPaint.NO_INDEX) {
				return index == other.index;
			} else {
				return other.id == null
						&& other.index == XmPaint.NO_INDEX
						&& paintBits == other.paintBits
						&& color0 == other.color0
						&& color1 == other.color1
						&& color2 == other.color2
						&& textureSet0 == other.textureSet0
						&& textureSet1 == other.textureSet1
						&& textureSet2 == other.textureSet2
						&& vertexProcessor0 == other.vertexProcessor0
						&& vertexProcessor1 == other.vertexProcessor1
						&& vertexProcessor2 == other.vertexProcessor2;
			}
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		if (id != null) {
			return id.hashCode();
		}

		if(index != XmPaint.NO_INDEX) {
			return index;
		}

		int result = HashCommon.mix(paintBits);
		result = result * 31 + HashCommon.mix(color0);
		result = result * 31 + vertexProcessor0.hashCode();
		result = result * 31 + textureSet0.hashCode();
		final int depth = textureDepth();

		if (depth > 1) {
			result = result * 31 + HashCommon.mix(color1);
			result = result * 31 + vertexProcessor1.hashCode();
			result = result * 31 + textureSet1.hashCode();

			if (depth == 3) {
				result = result * 31 + HashCommon.mix(color2);
				result = result * 31 + vertexProcessor2.hashCode();
				result = result * 31 + textureSet2.hashCode();
			}
		}

		if (shader != null) {
			result = result * 31 + shader.hashCode();
		}

		if (condition != null) {
			result = result * 31 + condition.hashCode();
		}

		return result;
	}

	public @Nullable PaintBlendMode blendMode() {
		return BLEND_MODE.getValue(this);
	}

	@Deprecated
	public @Nullable PaintBlendMode blendMode(int textureIndex) {
		return textureIndex == 0 ? blendMode() : PaintBlendMode.TRANSLUCENT;
	}

	public boolean disableColorIndex(int textureIndex) {
		return FLAGS[COLOR_DISABLE_INDEX_START + textureIndex].getValue(this);
	}

	public int textureColor(int textureIndex) {
		switch (textureIndex) {
		case 0:
			return color0;
		case 1:
			return color1;
		case 2:
			return color2;
		default:
			throw new IndexOutOfBoundsException("Invalid texture index: " + textureIndex);
		}
	}

	public int textureDepth() {
		return TEXTURE_DEPTH.getValue(this);
	}

	public TextureSet texture(int textureIndex) {
		switch (textureIndex) {
		case 0:
			return textureSet0;
		case 1:
			return textureSet1;
		case 2:
			return textureSet2;
		default:
			throw new IndexOutOfBoundsException("Invalid texture index: " + textureIndex);
		}
	}

	public boolean emissive(int textureIndex) {
		return FLAGS[EMISSIVE_INDEX_START + textureIndex].getValue(this);
	}

	public boolean disableDiffuse(int textureIndex) {
		return FLAGS[DIFFUSE_INDEX_START + textureIndex].getValue(this);
	}

	public boolean disableAo(int textureIndex) {
		return FLAGS[AO_INDEX_START + textureIndex].getValue(this);
	}

	public @Nullable Identifier shader() {
		return shader;
	}

	public @Nullable Identifier condition() {
		return shader;
	}

	public Identifier id() {
		return id;
	}

	public int index() {
		return index;
	}

	public VertexProcessor vertexProcessor(int textureIndex) {
		switch (textureIndex) {
		case 0:
			return vertexProcessor0;
		case 1:
			return vertexProcessor1;
		case 2:
			return vertexProcessor2;
		default:
			throw new IndexOutOfBoundsException("Invalid texture index: " + textureIndex);
		}
	}

	public static class Value extends XmPaintImpl implements XmPaint {
		private int hashCode;
		boolean external = false;

		protected Value(XmPaintImpl template) {
			id = template.id;
			index = template.index;
			copyFrom(template);
		}

		@Override
		protected void copyFrom(XmPaintImpl template) {
			super.copyFrom(template);
			hashCode = template.hashCode();
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public CompoundTag toTag() {
			if (this == DEFAULT_PAINT) {
				return new CompoundTag();
			} else if (id != null) {
				final CompoundTag result = new CompoundTag();
				result.putString(TAG_ID, id.toString());
				return result;
			} else if (index != XmPaint.NO_INDEX) {
				final CompoundTag result = new CompoundTag();
				result.putInt(TAG_INDEX, index);
				return result;
			} else  {
				return toFixedTag();
			}
		}

		@Override
		public CompoundTag toFixedTag() {
			final CompoundTag result = new CompoundTag();
			final int depth = textureDepth();
			final int[] words = new int[TAG_INDEX_COLOR_0 + depth];
			words[TAG_INDEX_PAINT_BITS] = paintBits;
			words[TAG_INDEX_COLOR_0] = color0;
			result.putString(TAG_TEX_0, textureSet0.id().toString());

			int header = 0;

			if (shader != null) {
				result.putString(TAG_SHADER, shader.toString());
				header |= FLAG_HAS_SHADER;
			}

			if (condition != null) {
				result.putString(TAG_CONDITION, condition.toString());
				header |= FLAG_HAS_CONDITION;
			}

			if (vertexProcessor0 != VertexProcessorDefault.INSTANCE) {
				result.putString(TAG_VP_0, VertexProcessorRegistry.INSTANCE.getId(vertexProcessor0).toString());
				header |= FLAG_HAS_VP0;
			}

			if (depth > 1) {
				words[TAG_INDEX_COLOR_1] = color1;
				result.putString(TAG_TEX_1, textureSet1.id().toString());

				if (vertexProcessor1 != VertexProcessorDefault.INSTANCE) {
					result.putString(TAG_VP_1, VertexProcessorRegistry.INSTANCE.getId(vertexProcessor1).toString());
					header |= FLAG_HAS_VP1;
				}

				if (depth == 3) {
					words[TAG_INDEX_COLOR_2] = color2;
					result.putString(TAG_TEX_2, textureSet2.id().toString());

					if (vertexProcessor2 != VertexProcessorDefault.INSTANCE) {
						result.putString(TAG_VP_2, VertexProcessorRegistry.INSTANCE.getId(vertexProcessor2).toString());
						header |= FLAG_HAS_VP2;
					}
				}
			}

			words[TAG_INDEX_HEADER_BITS] = header;
			result.putIntArray(TAG_BITS, words);

			return result;
		}

		@Override
		public void toBytes(PacketByteBuf pBuff) {
			if (this == DEFAULT_PAINT) {
				pBuff.writeVarInt(XmPaint.NO_INDEX);
			} else if (id != null) {
				pBuff.writeVarInt(FLAG_HAS_ID);
				pBuff.writeString(id.toString());
			} else if (index != XmPaint.NO_INDEX) {
				pBuff.writeVarInt(FLAG_HAS_INDEX);
				pBuff.writeVarInt(index);
			} else {
				toFixedBytes(pBuff);
			}
		}

		@Override
		public void toFixedBytes(PacketByteBuf pBuff) {
			final int depth = textureDepth();

			int header = 0;

			if (shader != null) {
				header |= FLAG_HAS_SHADER;
			}

			if (condition != null) {
				header |= FLAG_HAS_CONDITION;
			}

			if (vertexProcessor0 != VertexProcessorDefault.INSTANCE) {
				header |= FLAG_HAS_VP0;
			}

			if (depth > 1) {

				if (vertexProcessor1 != VertexProcessorDefault.INSTANCE) {
					header |= FLAG_HAS_VP1;
				}

				if (depth == 3) {

					if (vertexProcessor2 != VertexProcessorDefault.INSTANCE) {
						header |= FLAG_HAS_VP2;
					}
				}
			}

			pBuff.writeVarInt(header);
			pBuff.writeInt(paintBits);
			pBuff.writeInt(color0);
			pBuff.writeString(textureSet0.id().toString());

			if (shader != null) {
				pBuff.writeString(shader.toString());
			}

			if (condition != null) {
				pBuff.writeString(condition.toString());
			}

			if (vertexProcessor0 != VertexProcessorDefault.INSTANCE) {
				pBuff.writeString(VertexProcessorRegistry.INSTANCE.getId(vertexProcessor0).toString());
			}

			if (depth > 1) {
				pBuff.writeInt(color1);
				pBuff.writeString(textureSet1.id().toString());

				if (vertexProcessor1 != VertexProcessorDefault.INSTANCE) {
					pBuff.writeString(VertexProcessorRegistry.INSTANCE.getId(vertexProcessor1).toString());
				}

				if (depth == 3) {
					pBuff.writeInt(color2);
					pBuff.writeString(textureSet2.id().toString());

					if (vertexProcessor2 != VertexProcessorDefault.INSTANCE) {
						pBuff.writeString(VertexProcessorRegistry.INSTANCE.getId(vertexProcessor2).toString());
					}
				}
			}
		}
	}

	public static class Finder extends XmPaintImpl implements XmPaintFinder {
		public Finder() {
			clear();
		}

		public Finder id(Identifier id) {
			assert index == XmPaint.NO_INDEX;
			assert this.id == null;
			this.id = id;
			return this;
		}

		public Finder index(int index) {
			assert index == XmPaint.NO_INDEX;
			assert id == null;
			this.index = index;
			return this;
		}

		@Override
		public synchronized Value find() {
			Value result = MAP.get(this);

			if (result == null) {
				result = new Value(this);
				MAP.put(result, result);
			}

			id = null;
			return result;
		}

		@Override
		public XmPaintFinder copy(XmPaint paint) {
			copyFrom((XmPaintImpl) paint);
			return this;
		}

		@Override
		public XmPaintFinder clear() {
			id = null;
			paintBits = DEFAULT_PAINT_BITS;
			color0 = 0xFFFFFFFF;
			color1 = 0xFFFFFFFF;
			color2 = 0xFFFFFFFF;
			shader = null;
			condition = null;
			vertexProcessor0 = VertexProcessorDefault.INSTANCE;
			vertexProcessor1 = VertexProcessorDefault.INSTANCE;
			vertexProcessor2 = VertexProcessorDefault.INSTANCE;
			textureSet0 = TextureSet.none();
			textureSet1 = textureSet0;
			textureSet2 = textureSet0;
			return this;
		}

		@Override
		public XmPaintFinder textureDepth(int depth) {
			if (depth < 1 || depth > MAX_TEXTURE_DEPTH) {
				throw new IndexOutOfBoundsException("Invalid texture depth: " + depth);
			}
			TEXTURE_DEPTH.setValue(depth, this);
			return this;
		}

		@Override
		public XmPaintFinder textureColor(int textureIndex, int colorARBG) {
			switch (textureIndex) {
			case 0:
				color0 = colorARBG;
				break;
			case 1:
				color1 = colorARBG;
				break;
			case 2:
				color2 = colorARBG;
				break;
			default:
				throw new IndexOutOfBoundsException("Invalid texture index: " + textureIndex);
			}
			return this;
		}

		@Override
		public XmPaintFinder texture(int textureIndex, TextureSet texture) {
			switch (textureIndex) {
			case 0:
				textureSet0 = texture;
				break;
			case 1:
				textureSet1 = texture;
				break;
			case 2:
				textureSet2 = texture;
				break;
			default:
				throw new IndexOutOfBoundsException("Invalid texture index: " + textureIndex);
			}
			texture.use();
			return this;
		}

		@Override
		public XmPaintFinder blendMode(PaintBlendMode blendMode) {
			BLEND_MODE.setValue(blendMode, this);
			return this;
		}

		@Deprecated
		@Override
		public XmPaintFinder blendMode(int textureIndex, PaintBlendMode blendMode) {
			return textureIndex == 0 ? blendMode(blendMode) : this;
		}

		@Override
		public XmPaintFinder disableColorIndex(int textureIndex, boolean disable) {
			FLAGS[COLOR_DISABLE_INDEX_START + textureIndex].setValue(disable, this);
			return this;
		}

		@Override
		public XmPaintFinder disableDiffuse(int textureIndex, boolean disable) {
			FLAGS[DIFFUSE_INDEX_START + textureIndex].setValue(disable, this);
			return this;
		}

		@Override
		public XmPaintFinder disableAo(int textureIndex, boolean disable) {
			FLAGS[AO_INDEX_START + textureIndex].setValue(disable, this);
			return this;
		}

		@Override
		public XmPaintFinder emissive(int textureIndex, boolean isEmissive) {
			FLAGS[EMISSIVE_INDEX_START + textureIndex].setValue(isEmissive, this);
			return this;
		}

		@Override
		public XmPaintFinder shader(Identifier shader) {
			this.shader = shader;
			return this;
		}

		@Override
		public XmPaintFinder condition(Identifier condition) {
			this.condition = condition;
			return this;
		}

		@Override
		public XmPaintFinder vertexProcessor(int textureIndex, VertexProcessor vertexProcessor) {
			switch (textureIndex) {
			case 0:
				vertexProcessor0 = vertexProcessor;
				break;
			case 1:
				vertexProcessor1 = vertexProcessor;
				break;
			case 2:
				vertexProcessor2 = vertexProcessor;
				break;
			default:
				throw new IndexOutOfBoundsException("Invalid texture index: " + textureIndex);
			}
			return this;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final BitPacker32 TAG_PACKER = new BitPacker32(null, null);

	@SuppressWarnings({ "rawtypes", "unused" }) // For future use
	private static final BitPacker32.IntElement TAG_VERSION = TAG_PACKER.createIntElement(1024);

	@SuppressWarnings("rawtypes")
	private static final BitPacker32.BooleanElement TAG_HAS_SHADER = TAG_PACKER.createBooleanElement();
	private static final int FLAG_HAS_SHADER = TAG_HAS_SHADER.comparisonMask();
	@SuppressWarnings("rawtypes")
	private static final BitPacker32.BooleanElement TAG_HAS_CONDITION = TAG_PACKER.createBooleanElement();
	private static final int FLAG_HAS_CONDITION = TAG_HAS_CONDITION.comparisonMask();
	@SuppressWarnings("rawtypes")
	private static final BitPacker32.BooleanElement TAG_HAS_VP0 = TAG_PACKER.createBooleanElement();
	private static final int FLAG_HAS_VP0 =  TAG_HAS_VP0.comparisonMask();
	@SuppressWarnings("rawtypes")
	private static final BitPacker32.BooleanElement TAG_HAS_VP1 = TAG_PACKER.createBooleanElement();
	private static final int FLAG_HAS_VP1 =  TAG_HAS_VP1.comparisonMask();
	@SuppressWarnings("rawtypes")
	private static final BitPacker32.BooleanElement TAG_HAS_VP2 = TAG_PACKER.createBooleanElement();
	private static final int FLAG_HAS_VP2 =  TAG_HAS_VP2.comparisonMask();
	@SuppressWarnings("rawtypes")
	private static final BitPacker32.BooleanElement TAG_HAS_ID = TAG_PACKER.createBooleanElement();
	private static final int FLAG_HAS_ID =  TAG_HAS_ID.comparisonMask();
	@SuppressWarnings("rawtypes")
	private static final BitPacker32.BooleanElement TAG_HAS_INDEX = TAG_PACKER.createBooleanElement();
	private static final int FLAG_HAS_INDEX =  TAG_HAS_INDEX.comparisonMask();


	private static final int TAG_INDEX_HEADER_BITS = 0;
	private static final int TAG_INDEX_PAINT_BITS = 1;
	private static final int TAG_INDEX_COLOR_0 = 2;
	private static final int TAG_INDEX_COLOR_1 = 3;
	private static final int TAG_INDEX_COLOR_2 = 4;

	private static final String TAG_ID = "id";
	private static final String TAG_INDEX = "ix";
	private static final String TAG_SHADER = "sh";
	private static final String TAG_CONDITION = "cn";
	private static final String TAG_BITS = "bt";
	private static final String TAG_TEX_0 = "t0";
	private static final String TAG_TEX_1 = "t1";
	private static final String TAG_TEX_2 = "t2";

	private static final String TAG_VP_0 = "v0";
	private static final String TAG_VP_1 = "v1";
	private static final String TAG_VP_2 = "v2";

	public static XmPaintImpl.Value fromTag(CompoundTag tag, @Nullable PaintIndex paintIndex) {
		if (tag.isEmpty()) {
			return XmPaintImpl.DEFAULT_PAINT;
		}

		if (tag.contains(TAG_ID)) {
			return XmPaintRegistryImpl.INSTANCE.get(new Identifier(tag.getString(TAG_ID)));
		}

		if (tag.contains(TAG_INDEX)) {
			if (paintIndex == null) {
				Xm.LOG.warn("Attempt to deserialize indexed paint with null paint index. Default paint used.");
				return XmPaintImpl.DEFAULT_PAINT;
			}

			return (Value) paintIndex.fromIndex(tag.getInt(TAG_INDEX));
		}

		// check in case someone stuffed other bits in our empty default tag
		if (!tag.contains(TAG_BITS)) {
			return XmPaintImpl.DEFAULT_PAINT;
		}

		final Finder finder = finder();

		final int[] words = tag.getIntArray(TAG_BITS);
		final int header  = words[TAG_INDEX_HEADER_BITS];
		finder.paintBits = words[TAG_INDEX_PAINT_BITS];
		finder.color0 = words[TAG_INDEX_COLOR_0];
		finder.textureSet0 = TextureSetRegistry.instance().get(new Identifier(tag.getString(TAG_TEX_0)));

		if ((header & FLAG_HAS_SHADER) == FLAG_HAS_SHADER) {
			finder.shader =  new Identifier(tag.getString(TAG_SHADER));
		}

		if ((header & FLAG_HAS_CONDITION) == FLAG_HAS_CONDITION) {
			finder.condition =  new Identifier(tag.getString(TAG_CONDITION));
		}

		if ((header & FLAG_HAS_VP0) == FLAG_HAS_VP0) {
			finder.vertexProcessor0 =  VertexProcessorRegistry.INSTANCE.get(new Identifier(tag.getString(TAG_VP_0)));
		}

		final int depth = finder.textureDepth();

		if (depth > 1) {
			finder.color1 = words[TAG_INDEX_COLOR_1];
			finder.textureSet1 = TextureSetRegistry.instance().get(new Identifier(tag.getString(TAG_TEX_1)));

			if ((header & FLAG_HAS_VP1) == FLAG_HAS_VP1) {
				finder.vertexProcessor1 =  VertexProcessorRegistry.INSTANCE.get(new Identifier(tag.getString(TAG_VP_1)));
			}

			if (depth == 3) {
				finder.color2 = words[TAG_INDEX_COLOR_2];
				finder.textureSet2 = TextureSetRegistry.instance().get(new Identifier(tag.getString(TAG_TEX_2)));

				if ((header & FLAG_HAS_VP2) == FLAG_HAS_VP2) {
					finder.vertexProcessor1 =  VertexProcessorRegistry.INSTANCE.get(new Identifier(tag.getString(TAG_VP_2)));
				}
			}
		}

		return finder.find();
	}

	public static XmPaint fromBytes(PacketByteBuf pBuff, @Nullable PaintIndex paintIndex) {
		final int header = pBuff.readVarInt();

		if (header == XmPaint.NO_INDEX) {
			return DEFAULT_PAINT;
		}

		if ((header & FLAG_HAS_ID) == FLAG_HAS_ID) {
			final Identifier id = Identifier.tryParse(pBuff.readString());
			return XmPaintRegistry.INSTANCE.get(id);
		}

		if ((header & FLAG_HAS_INDEX) == FLAG_HAS_INDEX) {
			final int index = pBuff.readVarInt();

			if (paintIndex == null) {
				Xm.LOG.warn("Attempt to deserialize indexed paint with null paint index. Default paint used.");
				return XmPaintImpl.DEFAULT_PAINT;
			}

			return paintIndex.fromIndex(index);
		}

		final Finder finder = finder();

		finder.paintBits = pBuff.readInt();
		finder.color0 = pBuff.readInt();
		finder.textureSet0 = TextureSetRegistry.instance().get(new Identifier(pBuff.readString()));

		if ((header & FLAG_HAS_SHADER) == FLAG_HAS_SHADER) {
			finder.shader =  new Identifier(pBuff.readString());
		}

		if ((header & FLAG_HAS_CONDITION) == FLAG_HAS_CONDITION) {
			finder.condition =  new Identifier(pBuff.readString());
		}

		if ((header & FLAG_HAS_VP0) == FLAG_HAS_VP0) {
			finder.vertexProcessor0 =  VertexProcessorRegistry.INSTANCE.get(new Identifier(pBuff.readString()));
		}

		final int depth = finder.textureDepth();

		if (depth > 1) {
			finder.color1 = pBuff.readInt();
			finder.textureSet1 = TextureSetRegistry.instance().get(new Identifier(pBuff.readString()));

			if ((header & FLAG_HAS_VP1) == FLAG_HAS_VP1) {
				finder.vertexProcessor1 =  VertexProcessorRegistry.INSTANCE.get(new Identifier(pBuff.readString()));
			}

			if (depth == 3) {
				finder.color2 = pBuff.readInt();
				finder.textureSet2 = TextureSetRegistry.instance().get(new Identifier(pBuff.readString()));

				if ((header & FLAG_HAS_VP2) == FLAG_HAS_VP2) {
					finder.vertexProcessor1 =  VertexProcessorRegistry.INSTANCE.get(new Identifier(pBuff.readString()));
				}
			}
		}

		return finder.find();
	}

	public static final XmPaintImpl.Value DEFAULT_PAINT = finder().find();
}
