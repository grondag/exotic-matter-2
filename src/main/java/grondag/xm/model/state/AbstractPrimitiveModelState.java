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

package grondag.xm.model.state;

import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_CORNER_JOIN;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_SPECIES;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import grondag.fermion.bits.BitPacker32;
import grondag.xm.Xm;
import grondag.xm.api.connect.model.ClockwiseRotation;
import grondag.xm.api.connect.state.CornerJoinState;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.ModelPrimitive;
import grondag.xm.api.surface.XmSurface;
import grondag.xm.api.surface.XmSurfaceList;
import grondag.xm.connect.CornerJoinStateSelector;
import grondag.xm.mesh.helper.PolyTransform;
import grondag.xm.mesh.polygon.IPolygon;
import grondag.xm.model.varia.BlockOrientationType;
import grondag.xm.painting.QuadPaintHandler;
import it.unimi.dsi.fastutil.HashCommon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class AbstractPrimitiveModelState<V extends AbstractPrimitiveModelState<V>> extends AbstractWorldModelState implements MutableModelState {
    private static final BitPacker32<AbstractPrimitiveModelState> SHAPE_PACKER = new BitPacker32<AbstractPrimitiveModelState>(m -> m.shapeBits,
            (m, b) -> m.shapeBits = b);

    private static final BitPacker32<AbstractPrimitiveModelState>.EnumElement<Direction.Axis> AXIS = SHAPE_PACKER.createEnumElement(Direction.Axis.class);

    private static final BitPacker32<AbstractPrimitiveModelState>.BooleanElement AXIS_INVERTED = SHAPE_PACKER.createBooleanElement();

    private static final BitPacker32<AbstractPrimitiveModelState>.EnumElement<ClockwiseRotation> AXIS_ROTATION = SHAPE_PACKER
            .createEnumElement(ClockwiseRotation.class);

    private static final BitPacker32<AbstractPrimitiveModelState>.IntElement BLOCK_JOIN = SHAPE_PACKER.createIntElement(CornerJoinState.STATE_COUNT);

    private static final BitPacker32<AbstractPrimitiveModelState>.IntElement MASONRY_JOIN = SHAPE_PACKER.createIntElement(SimpleJoinState.STATE_COUNT);

    public static final int PRIMITIVE_BIT_COUNT = 6;
    private static final BitPacker32<AbstractPrimitiveModelState>.IntElement PRIMITIVE_BITS = SHAPE_PACKER
            .createIntElement(1 << PRIMITIVE_BIT_COUNT);

    static {
        assert SHAPE_PACKER.bitLength() <= 32;
    }
    
    public static class ModelStateFactory<T extends AbstractPrimitiveModelState<T>> {
        private final ArrayBlockingQueue<T> POOL = new ArrayBlockingQueue<>(4096);
        
        private final Supplier<T> factory;
        
        ModelStateFactory(Supplier<T> factory) {
            this.factory = factory;
        }
        
        
        public final T claim(ModelPrimitive<T> primitive) {
            T result = POOL.poll();
            if (result == null) {
                result = factory.get();
                result.isImmutable = false;
            }
            result.retain();
            result.primitive = primitive;
            return result;
        }
        
        public final T claim(T template, ModelPrimitive<T> primitive) {
            T result = claim(primitive);
            result.copyFrom(template);
            return result;
        }
        
        public final T claim(T template) {
            return claim(template, template.primitive);
        }
        
        public final T fromTag(ModelPrimitive<T> shape, CompoundTag tag) {
            T result = claim(shape);

            if (tag.containsKey(ModelStateTagHelper.NBT_MODEL_BITS)) {
                int[] stateBits = tag.getIntArray(ModelStateTagHelper.NBT_MODEL_BITS);
                if (stateBits.length != 22) {
                    Xm.LOG.warn("Bad or missing data encounter during ModelState NBT deserialization.");
                } else {
                    result.deserializeFromInts(stateBits);
                }
            }

            // textures and vertex processors serialized by name because registered can
            // change if mods/config change
            //        String layers = tag.getString(NBT_LAYERS);
            //        if (layers.isEmpty()) {
            //            String[] names = layers.split(",");
            //            if (names.length != 0) {
            //                int i = 0;
            //                for (PaintLayer l : PaintLayer.VALUES) {
            //                    if (ModelStateData.PAINT_TEXTURE[l.ordinal()].getValue(this) != 0) {
            //                        TextureSet tex = TextureSetRegistryImpl.INSTANCE.getById(new Identifier(names[i++]));
            //                        ModelStateData.PAINT_TEXTURE[l.ordinal()].setValue(tex.index(), this);
            //                        if (i == names.length)
            //                            break;
            //                    }
            //
            //                    if (ModelStateData.PAINT_VERTEX_PROCESSOR[l.ordinal()].getValue(this) != 0) {
            //                        VertexProcessor vp = VertexProcessors.get(names[i++]);
            //                        ModelStateData.PAINT_VERTEX_PROCESSOR[l.ordinal()].setValue(vp.ordinal, this);
            //                        if (i == names.length)
            //                            break;
            //                    }
            //                }
            //            }
            //        }

            result.clearStateFlags();
            return result;
        }

        public final T fromBuffer(ModelPrimitive<T> shape, PacketByteBuf buf) {
            T result = claim(shape);
            result.fromBytes(buf);
            return result;
        }
    }
    
    public abstract ModelStateFactory<V> factory();

    protected int shapeBits;

    /** contains indicators derived from shape and painters */
    private int stateFlags = 0;
    
    protected ModelPrimitive<V> primitive;
    
    protected final V self = (V)this;

    public ModelPrimitive<V> primitive() {
        return primitive;
    }
    
    public final int stateFlags() {
        int result = stateFlags;
        if (result == 0) {
            result = ModelStateVaria.getFlags(this);
            stateFlags = result;
        }
        return result;
    }

    protected final void clearStateFlags() {
        stateFlags = 0;
    }
    
    @Override
    public final V setStatic(boolean isStatic) {
        setStaticInner(isStatic);
        return self;
    }

    public final V posX(int index) {
        posXInner(index);
        return self;
    }

    public final V posY(int index) {
        posYInner(index);
        return self;
    }

    public final V posZ(int index) {
        posZInner(index);
        return self;
    }
    
    @Override
    protected <T extends AbstractModelState> void copyInternal(T template) {
        super.copyInternal(template);
        final AbstractPrimitiveModelState other = (AbstractPrimitiveModelState) template;
        this.shapeBits = other.shapeBits;
    }

    @Override
    protected void doSerializeToInts(int[] data, int startAt) {
        data[startAt] = shapeBits;
        super.doSerializeToInts(data, startAt + 1);
    }

    @Override
    protected void doDeserializeFromInts(int[] data, int startAt) {
        this.shapeBits = data[startAt];
        super.doDeserializeFromInts(data, startAt + 1);
    }

    @Override
    protected int computeHashCode() {
        return super.computeHashCode() ^ HashCommon.mix(this.shapeBits);
    }

    @Override
    protected int intSize() {
        return super.intSize() + 2;
    }
    
    ////////////////////////////////////////////////////
    // PACKER 0 ATTRIBUTES (NOT SHAPE-DEPENDENT)
    ////////////////////////////////////////////////////

    public final Direction.Axis getAxis() {
        return AXIS.getValue(this);
    }

    public final V setAxis(Direction.Axis axis) {
        AXIS.setValue(axis, this);
        invalidateHashCode();
        return self;
    }

    public boolean isAxisInverted() {
        return AXIS_INVERTED.getValue(this);
    }

    public final V setAxisInverted(boolean isInverted) {
        AXIS_INVERTED.setValue(isInverted, this);
        invalidateHashCode();
        return self;
    }

    ////////////////////////////////////////////////////
    // PACKER 3 ATTRIBUTES (BLOCK FORMAT)
    ////////////////////////////////////////////////////

    public CornerJoinState cornerJoin() {
        return CornerJoinStateSelector.fromOrdinal(MathHelper.clamp(BLOCK_JOIN.getValue(this), 0, CornerJoinState.STATE_COUNT - 1));
    }

    public final V cornerJoin(CornerJoinState join) {
        BLOCK_JOIN.setValue(join.ordinal(), this);
        invalidateHashCode();
        return self;
    }

    public SimpleJoinState simpleJoin() {
        // If this state is using corner join, join index is for a corner join
        // and so need to derive simple join from the corner join
        final int stateFlags = stateFlags();
        return ((stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) == 0) ? SimpleJoinState.fromOrdinal(BLOCK_JOIN.getValue(this)) : cornerJoin().simpleJoin();
    }

    public final V simpleJoin(SimpleJoinState join) {
        BLOCK_JOIN.setValue(join.ordinal(), this);
        invalidateHashCode();
        return self;
    }

    public SimpleJoinState masonryJoin() {
        return SimpleJoinState.fromOrdinal(MASONRY_JOIN.getValue(this));
    }

    public final V masonryJoin(SimpleJoinState join) {
        MASONRY_JOIN.setValue(join.ordinal(), this);
        invalidateHashCode();
        return self;
    }

  /**
  * For machines and other blocks with a privileged horizontal face, North is
  * considered the zero rotation.
  */
    public ClockwiseRotation getAxisRotation() {
        return AXIS_ROTATION.getValue(this);
    }

    public final V setAxisRotation(ClockwiseRotation rotation) {
        AXIS_ROTATION.setValue(rotation, this);
        invalidateHashCode();
        return self;
    }

    /**
     * Returns true if visual elements and geometry match. Does not consider species
     * in matching.
     */
    public final boolean doShapeAndAppearanceMatch(ModelState other) {
        if(other.getClass() != this.getClass()) return false;
        return primitive.doesShapeMatch(self, (V) other) && doesAppearanceMatch(other);
    }

    public Direction rotateFace(Direction face) {
        return PolyTransform.rotateFace(this, face);
    }

    @Override
    public void serializeNBT(CompoundTag tag) {
        tag.putIntArray(ModelStateTagHelper.NBT_MODEL_BITS, this.serializeToInts());

        // shape is serialized by name because registered shapes can change if
        // mods/config change
        tag.putString(ModelStateTagHelper.NBT_SHAPE, this.primitive().id().toString());

        // TODO: serialization for paint/surface map
        // textures and vertex processors serialized by name because registered can
        // change if mods/config change
//        StringBuilder layers = new StringBuilder();
//       
//        if (layers.length() != 0)
//            tag.putString(NBT_LAYERS, layers.toString());
    }

    public int primitiveBits() {
        return PRIMITIVE_BITS.getValue(this);
    }

    protected final void primitiveBitsInner(int bits) {
        PRIMITIVE_BITS.setValue(bits, this);
    }

    @Override
    protected final int surfaceCount() {
        return primitive.surfaces(self).size();
    }

    /**
     * Means that one or more elements (like a texture) uses species. Does not mean
     * that the shape or block actually capture or generate species other than 0.
     */
    public final boolean hasSpecies() {
        final int stateFlags = stateFlags();
        return ((stateFlags & STATE_FLAG_NEEDS_SPECIES) == STATE_FLAG_NEEDS_SPECIES);
    }

    /**
     * Will return 0 if model state does not include species. This is more
     * convenient than checking each place species is used.
     * 
     * @return
     */
    public int species() {
        return SPECIES.getValue(this);
    }

    public final V species(int species) {
        SPECIES.setValue(species, this);
        invalidateHashCode();
        return self;
    }

    @Override
    public void fromBytes(PacketByteBuf pBuff) {
        shapeBits = pBuff.readInt();
        blockBits = pBuff.readInt();
        final int limit = primitive.surfaces(self).size();
        for (int i = 0; i < limit; i++) {
            this.paints[i] = pBuff.readVarInt();
        }
    }
    
    public final V primitiveBits(int bits) {
        primitiveBitsInner(bits);
        return self;
    }

    public final V pos(BlockPos pos) {
        posInner(pos);
        return self;
    }
    
    public final V paint(int surfaceIndex, int paintIndex) {
        paintInner(surfaceIndex, paintIndex);
        return self;
    }
    
    public final V paint(XmSurface surface, XmPaint paint) {
      return paint(surface.ordinal(), paint.index());
    }
    
    public final V paint(XmSurface surface, int paintIndex) {
      return paint(surface.ordinal(), paintIndex);
    }

    public final V paintAll(XmPaint paint) {
      return paintAll(paint.index());
    }

    public final V paintAll(int paintIndex) {
      XmSurfaceList slist = primitive().surfaces(self);
      final int limit = slist.size();
      for (int i = 0; i < limit; i++) {
          paint(i, paintIndex);
      }
      return self;
    }

    public final <T> T applyAndRelease(Function<V, T> func) {
        final T result = func.apply(self);
        this.release();
        return result;
    }
    
    /**
     * Does NOT consider isStatic in comparison. <br>
     * <br>
     * 
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(Object obj) {
        return this == obj ? true : obj != null && obj.getClass() == this.getClass() && equalsInner(obj);
    }

    protected boolean equalsInner(Object obj) {
        final PrimitiveModelState other = (PrimitiveModelState) obj;
        return this.primitive == other.primitive 
                && this.blockBits == other.blockBits
                && this.shapeBits == other.shapeBits
                && doPaintsMatch(other);
    }

    @Override
    public final boolean equalsIncludeStatic(Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof PrimitiveModelState) {
            PrimitiveModelState other = (PrimitiveModelState) obj;
            return this.isStatic == other.isStatic && equalsInner(other);
        } else {
            return false;
        }
    }

    @Override
    public void toBytes(PacketByteBuf pBuff) {
        pBuff.writeVarInt(primitive.index());
        pBuff.writeInt(shapeBits);
        pBuff.writeInt(blockBits);
        final int limit = primitive.surfaces(self).size();
        for (int i = 0; i < limit; i++) {
            pBuff.writeVarInt(paints[i]);
        }
    }

    @Environment(EnvType.CLIENT)
    private Mesh mesh = null;

    @Environment(EnvType.CLIENT)
    private List<BakedQuad>[] quadLists = null;
    
    @Environment(EnvType.CLIENT)
    private Mesh mesh() {
        Mesh result = mesh;
        if (result == null) {
            result = QuadPaintHandler.paint(this);
            mesh = result;
        }
        return result;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public List<BakedQuad> getBakedQuads(BlockState state, Direction face, Random rand) {
        List<BakedQuad>[] lists = quadLists;
        if (lists == null) {
            lists = ModelHelper.toQuadLists(mesh());
            quadLists = lists;
        }
        List<BakedQuad> result = lists[face == null ? 6 : face.getId()];
        return result == null ? ImmutableList.of() : result;
    }
    
  public final boolean isAxisOrthogonalToPlacementFace() {
          return primitive().isAxisOrthogonalToPlacementFace();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void emitQuads(RenderContext context) {
        context.meshConsumer().accept(mesh());
    }
    
    @Override
    public void produceQuads(Consumer<IPolygon> target) {
        primitive.produceQuads(self, target);
    }
    
    @Override
    public V geometricState() {
        return primitive.geometricState(self);
    }
    
    public final boolean hasAxisOrientation() {
      return primitive().hasAxisOrientation(self);
    }
    
    public final boolean hasAxisRotation() {
      return primitive().hasAxisRotation(self);
    }
    
    public final boolean hasAxis() {
      return primitive().hasAxis(self);
    }
    
    public final BlockOrientationType orientationType() {
      return primitive().orientationType(self);
    }
    
    @Override
    protected final void onLastRelease() {
        factory().POOL.offer(self);
    }

    @Override
    public V toImmutable() {
        if(this.isImmutable) {
            return self;
        } else {
            V result = factory().claim(self);
            result.isImmutable = true;
            return result;
        }
    }

    @Override
    public V copyFrom(ModelState template) {
        copyInternal((V)template);
        return self;
    }

    @Override
    public V releaseToImmutable() {
        final V result = this.toImmutable();
        this.release();
        return result;
    }
    
    @Override
    public V mutableCopy() {
        return factory().claim(self, primitive);
    }
}
