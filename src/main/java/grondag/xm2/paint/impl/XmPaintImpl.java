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

package grondag.xm2.paint.impl;

import static grondag.xm2.paint.api.XmPaint.MAX_TEXTURE_DEPTH;

import javax.annotation.Nullable;

import grondag.fermion.varia.BitPacker64;
import grondag.xm2.paint.api.XmPaint;
import grondag.xm2.paint.api.XmPaintFinder;
import grondag.xm2.painting.VertexProcessor;
import grondag.xm2.painting.VertexProcessorDefault;
import grondag.xm2.texture.api.TextureSet;
import grondag.xm2.texture.api.TextureSetRegistry;
import grondag.xm2.texture.impl.TextureSetRegistryImpl;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.util.Identifier;

public class XmPaintImpl {
    private static final BitPacker64<XmPaintImpl> PAINT_BITS = new BitPacker64<XmPaintImpl>(p -> p.paintBits, (p, b) -> p.paintBits = b);
    
	public static Finder finder() {
		return new Finder();
	}
    
    // offset additively by sprite index.
    private static final int EMISSIVE_INDEX_START = 0;
    private static final int DIFFUSE_INDEX_START = EMISSIVE_INDEX_START + MAX_TEXTURE_DEPTH;
    private static final int AO_INDEX_START = DIFFUSE_INDEX_START + MAX_TEXTURE_DEPTH;
    private static final int COLOR_DISABLE_INDEX_START = AO_INDEX_START + MAX_TEXTURE_DEPTH;
    
    @SuppressWarnings("unchecked")
    private static final BitPacker64<XmPaintImpl>.BooleanElement[] FLAGS = new BitPacker64.BooleanElement[COLOR_DISABLE_INDEX_START + MAX_TEXTURE_DEPTH];
    
    @SuppressWarnings("unchecked")
    private static final BitPacker64<XmPaintImpl>.IntElement[] TEXTURES = new BitPacker64.IntElement[MAX_TEXTURE_DEPTH];
    
    @SuppressWarnings("unchecked")
    private static final BitPacker64<XmPaintImpl>.NullableEnumElement<BlockRenderLayer> BLEND_MODES[] = new BitPacker64.NullableEnumElement[MAX_TEXTURE_DEPTH];
    
    private static final BitPacker64<XmPaintImpl>.IntElement TEXTURE_DEPTH;
    
    private static final long DEFAULT_PAINT_BITS;
    
    private static final ObjectArrayList<Value> LIST = new ObjectArrayList<>();
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
        
        TEXTURES[0] = PAINT_BITS.createIntElement(TextureSetRegistry.MAX_TEXTURE_SETS);
        TEXTURES[1] = PAINT_BITS.createIntElement(TextureSetRegistry.MAX_TEXTURE_SETS);
        TEXTURES[2] = PAINT_BITS.createIntElement(TextureSetRegistry.MAX_TEXTURE_SETS);
        
        BLEND_MODES[0] = PAINT_BITS.createNullableEnumElement(BlockRenderLayer.class);
        BLEND_MODES[1] = PAINT_BITS.createNullableEnumElement(BlockRenderLayer.class);
        BLEND_MODES[2] = PAINT_BITS.createNullableEnumElement(BlockRenderLayer.class);
        
        assert PAINT_BITS.bitLength() <= 64;
        
        long defaultBits = 0;
        BLEND_MODES[0].setValue(null, defaultBits);
        BLEND_MODES[1].setValue(null, defaultBits);
        BLEND_MODES[2].setValue(null, defaultBits);
        DEFAULT_PAINT_BITS = defaultBits;
    }
    
    public static Value byIndex(int index) {
        return LIST.get(index);
    }
    
    protected long paintBits = DEFAULT_PAINT_BITS;
    protected int color0 = 0xFFFFFFFF;
    protected int color1 = 0xFFFFFFFF;
    protected int color2 = 0xFFFFFFFF;
    protected Identifier shader = null;
    protected Identifier condition = null;
    protected VertexProcessor vertexProcessor0 = VertexProcessorDefault.INSTANCE;
    protected VertexProcessor vertexProcessor1 = VertexProcessorDefault.INSTANCE;
    protected VertexProcessor vertexProcessor2 = VertexProcessorDefault.INSTANCE;
    
    @Override
	public boolean equals(Object obj) {
		if(obj != null && obj instanceof XmPaintImpl) {
			final XmPaintImpl other = (XmPaintImpl)obj;
			return paintBits == other.paintBits
					&& color0 == other.color0
					&& color1 == other.color1
					&& color2 == other.color2
					&& vertexProcessor0 == other.vertexProcessor0
					&& vertexProcessor1 == other.vertexProcessor1
					&& vertexProcessor2 == other.vertexProcessor2;
		} else {
			return false;
		}
	}
    
    @Override
	public int hashCode() {
    	int result = (int)HashCommon.mix(paintBits);
    	result ^= HashCommon.mix(color0);
    	result ^= vertexProcessor0.hashCode();
    	final int depth = textureDepth();
    	if(depth > 1) {
    		result ^= HashCommon.mix(color1);
    		result ^= vertexProcessor1.hashCode();
    		if(depth == 3) {
    			result ^= HashCommon.mix(color2);
    			result ^= vertexProcessor2.hashCode();
    		}
    	}
    	if(shader != null) {
    		result ^= shader.hashCode();
    	}
    	if(condition != null) {
    		result ^= condition.hashCode();
    	}
    	return result;
	}


	public @Nullable BlockRenderLayer blendMode(int textureIndex) {
        return BLEND_MODES[textureIndex].getValue(this);
    }

    public boolean disableColorIndex(int textureIndex) {
        return FLAGS[COLOR_DISABLE_INDEX_START + textureIndex].getValue(this);
    }

	public int textureColor(int textureIndex) {
		switch(textureIndex) {
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
		if (textureIndex < 0 || textureIndex >= MAX_TEXTURE_DEPTH) {
            throw new IndexOutOfBoundsException("Invalid texture index: " + textureIndex);
        }
        return TextureSetRegistryImpl.INSTANCE.getByIndex(TEXTURES[textureIndex].getValue(this));
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
    
    public VertexProcessor vertexProcessor(int textureIndex) {
		switch(textureIndex) {
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
        private final int index;
        private final int hashCode;
        
        protected Value(int index, XmPaintImpl template) {
            this.index = index;
            this.paintBits = template.paintBits;
            this.color0 = template.color0;
            this.color1 = template.color1;
            this.color2 = template.color2;
            this.hashCode = template.hashCode();
        }

        @Override
    	public int hashCode() {
        	return hashCode;
        }
        
		@Override
		public int index() {
			return index;
		}
    }
	
	public static class Finder extends XmPaintImpl implements XmPaintFinder {
		public Finder() {
			clear();
		}
		
		@Override
		public synchronized XmPaint find() {
            Value result = MAP.get(this);
            if (result == null) {
                result = new Value(LIST.size(), this);
                LIST.add(result);
                MAP.put(result, result);
            }
            return result;
		}

		@Override
		public XmPaintFinder clear() {
            paintBits = DEFAULT_PAINT_BITS;
            color0 = 0xFFFFFFFF;
            color1 = 0xFFFFFFFF;
            color2 = 0xFFFFFFFF;
            shader = null;
            condition = null;
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
			switch(textureIndex) {
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
			if (textureIndex < 0 || textureIndex >= MAX_TEXTURE_DEPTH) {
                throw new IndexOutOfBoundsException("Invalid texture index: " + textureIndex);
            }
            TEXTURES[textureIndex].setValue(texture.index(), this);
            texture.use();
            return this;
		}

		@Override
		public XmPaintFinder blendMode(int textureIndex, BlockRenderLayer blendMode) {
			BLEND_MODES[textureIndex].setValue(blendMode, this);
			return this;
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
			switch(textureIndex) {
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
}
