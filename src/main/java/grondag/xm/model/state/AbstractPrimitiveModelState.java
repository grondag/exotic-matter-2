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
import grondag.fermion.varia.Useful;
import grondag.xm.Xm;
import grondag.xm.api.connect.model.ClockwiseRotation;
import grondag.xm.api.connect.state.CornerJoinState;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.modelstate.ModelStateFlags;
import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.paint.XmPaintRegistry;
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
public abstract class AbstractPrimitiveModelState<V extends AbstractPrimitiveModelState<V>> extends AbstractModelState implements MutableModelState {
    
    ////////////////////////////////////////// BIT-WISE ENCODING //////////////////////////////////////////
    
    /** note that sign bit on world encoder is reserved to persist static state during serialization */
    private static final BitPacker32<AbstractPrimitiveModelState> WORLD_ENCODER = new BitPacker32<AbstractPrimitiveModelState>(m -> m.worldBits,(m, b) -> m.worldBits = b);
    private static final BitPacker32<AbstractPrimitiveModelState>.IntElement POS_X = WORLD_ENCODER.createIntElement(256);
    private static final BitPacker32<AbstractPrimitiveModelState>.IntElement POS_Y = WORLD_ENCODER.createIntElement(256);
    private static final BitPacker32<AbstractPrimitiveModelState>.IntElement POS_Z = WORLD_ENCODER.createIntElement(256);
    private static final BitPacker32<AbstractPrimitiveModelState>.IntElement SPECIES = WORLD_ENCODER.createIntElement(16);
    
    
    public static final int PRIMITIVE_BIT_COUNT = 6;
    private static final BitPacker32<AbstractPrimitiveModelState> SHAPE_ENCODER = new BitPacker32<AbstractPrimitiveModelState>(m -> m.shapeBits,(m, b) -> m.shapeBits = b);
    private static final BitPacker32<AbstractPrimitiveModelState>.EnumElement<Direction.Axis> AXIS = SHAPE_ENCODER.createEnumElement(Direction.Axis.class);
    private static final BitPacker32<AbstractPrimitiveModelState>.BooleanElement AXIS_INVERTED = SHAPE_ENCODER.createBooleanElement();
    private static final BitPacker32<AbstractPrimitiveModelState>.EnumElement<ClockwiseRotation> AXIS_ROTATION = SHAPE_ENCODER.createEnumElement(ClockwiseRotation.class);
    private static final BitPacker32<AbstractPrimitiveModelState>.IntElement BLOCK_JOIN = SHAPE_ENCODER.createIntElement(CornerJoinState.STATE_COUNT);
    private static final BitPacker32<AbstractPrimitiveModelState>.IntElement MASONRY_JOIN = SHAPE_ENCODER.createIntElement(SimpleJoinState.STATE_COUNT);
    private static final BitPacker32<AbstractPrimitiveModelState>.IntElement PRIMITIVE_BITS = SHAPE_ENCODER.createIntElement(1 << PRIMITIVE_BIT_COUNT);

    static {
        assert WORLD_ENCODER.bitLength() <= 32;
        assert SHAPE_ENCODER.bitLength() <= 32;
    }
    
    ////////////////////////////////////////// FACTORY //////////////////////////////////////////
    
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
    
    ////////////////////////////////////////// GENERAL DECLARATIONS //////////////////////////////////////////
    
    protected final V self = (V)this; 
    
    @Override
    protected int computeHashCode() {
        int result = 0;
        final int limit = this.surfaceCount();
        for (int i = 0; i < limit; i++) {
            result ^= paints[i];
        }
        return result ^ HashCommon.mix(this.shapeBits | (this.worldBits << 32));
    }
    
    public abstract ModelStateFactory<V> factory();
    
    public final <T> T applyAndRelease(Function<V, T> func) {
        final T result = func.apply(self);
        this.release();
        return result;
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

    protected <T extends AbstractPrimitiveModelState> void copyInternal(T template) {
        final AbstractPrimitiveModelState other = (AbstractPrimitiveModelState) template;
        System.arraycopy(template.paints, 0, this.paints, 0, this.surfaceCount());
        worldBits = other.worldBits;
        shapeBits = other.shapeBits;
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
    
    ////////////////////////////////////////// COMPARISON //////////////////////////////////////////
    
    /**
     * Does NOT consider isStatic in comparison.<p>
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
                && this.worldBits == other.worldBits
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
    
    /**
     * Returns true if visual elements and geometry match. Does not consider species
     * in matching.
     */
    public final boolean doShapeAndAppearanceMatch(ModelState other) {
        if(other.getClass() != this.getClass()) return false;
        return primitive.doesShapeMatch(self, (V) other) && doesAppearanceMatch(other);
    }
    
    /**
     * Returns true if visual elements match. Does not consider species or geometry
     * in matching.
     */
    public final boolean doesAppearanceMatch(ModelState other) {
        return other != null && other instanceof AbstractPrimitiveModelState && doPaintsMatch((AbstractPrimitiveModelState) other);
    }
    
    ////////////////////////////////////////// SERIALIZATION //////////////////////////////////////////

    @Override
    protected int intSize() {
        return surfaceCount() + 3;
    }
    
    @Override
    protected void doSerializeToInts(int[] data, int startAt) {
        data[startAt++] = this.isStatic ? (worldBits | Useful.INT_SIGN_BIT) : worldBits;
        data[startAt++] = shapeBits;
        System.arraycopy(paints, 0, data, startAt, surfaceCount());
    }

    @Override
    protected void doDeserializeFromInts(int[] data, int startAt) {
        // sign on first long word is used to store static indicator
        this.isStatic = (Useful.INT_SIGN_BIT & data[startAt]) == Useful.INT_SIGN_BIT;
        this.worldBits = Useful.INT_SIGN_BIT_INVERSE & data[startAt++];
        this.shapeBits = data[startAt++];
        System.arraycopy(data, startAt, paints, 0, surfaceCount());
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
    
    @Override
    public void fromBytes(PacketByteBuf pBuff) {
        shapeBits = pBuff.readInt();
        worldBits = pBuff.readInt();
        final int limit = primitive.surfaces(self).size();
        for (int i = 0; i < limit; i++) {
            this.paints[i] = pBuff.readVarInt();
        }
    }
    
    @Override
    public void toBytes(PacketByteBuf pBuff) {
        pBuff.writeVarInt(primitive.index());
        pBuff.writeInt(shapeBits);
        pBuff.writeInt(worldBits);
        final int limit = primitive.surfaces(self).size();
        for (int i = 0; i < limit; i++) {
            pBuff.writeVarInt(paints[i]);
        }
    }
    
    ////////////////////////////////////////// STATE FLAGS //////////////////////////////////////////
    
    /** contains indicators derived from shape and painters */
    private int stateFlags = 0;
    
    public final int stateFlags() {
        int result = stateFlags;
        if (result == 0) {

            result = ModelStateFlags.STATE_FLAG_IS_POPULATED | primitive.stateFlags(self);

            final int surfCount = primitive.surfaces(self).size();
            for (int i = 0; i < surfCount; i++) {
                XmPaint p = paint(i);
                final int texDepth = p.textureDepth();
                for (int j = 0; j < texDepth; j++) {
                    result |= p.texture(j).stateFlags();
                }
            }
            stateFlags = result;
        }
        return result;
    }
    
    protected final void clearStateFlags() {
        stateFlags = 0;
    }
    
    ////////////////////////////////////////// PRIMITIVE //////////////////////////////////////////
    
    protected ModelPrimitive<V> primitive;

    public final ModelPrimitive<V> primitive() {
        return primitive;
    }
    
    @Override
    public final void produceQuads(Consumer<IPolygon> target) {
        primitive.produceQuads(self, target);
    }
    
    @Override
    public V geometricState() {
        return primitive.geometricState(self);
    }
    
    public final boolean hasAxisOrientation() {
      return primitive.hasAxisOrientation(self);
    }
    
    public final boolean hasAxisRotation() {
      return primitive.hasAxisRotation(self);
    }
    
    public final boolean hasAxis() {
      return primitive.hasAxis(self);
    }
    
    public final BlockOrientationType orientationType() {
      return primitive.orientationType(self);
    }
    
    protected final int surfaceCount() {
        return primitive.surfaces(self).size();
    }
    
    public final boolean isAxisOrthogonalToPlacementFace() {
        return primitive.isAxisOrthogonalToPlacementFace();
    }
    
    public Direction rotateFace(Direction face) {
        return PolyTransform.rotateFace(this, face);
    }
    
    ////////////////////////////////////////// WORLD REFRESH CONTROL //////////////////////////////////////////
    
    protected boolean isStatic = false;

    @Override
    public final boolean isStatic() {
        return this.isStatic;
    }

    @Override
    public final V setStatic(boolean isStatic) {
        this.isStatic = isStatic;
        return self;
    }
    
    ////////////////////////////////////////// PAINT //////////////////////////////////////////
    
    protected abstract int maxSurfaces();

    protected final int[] paints = new int[maxSurfaces()];
    
    public final boolean doPaintsMatch(AbstractPrimitiveModelState other) {
        final int limit = surfaceCount();
        if (limit == other.surfaceCount()) {
            final int[] paints = this.paints;
            final int[] otherPaints = other.paints;
            for (int i = 0; i < limit; i++) {
                if (otherPaints[i] != paints[i]) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    protected final void paintInner(int surfaceIndex, int paintIndex) {
        paints[surfaceIndex] = paintIndex;
    }

    public final int paintIndex(int surfaceIndex) {
        return paints[surfaceIndex];
    }
    
    public final XmPaint paint(int surfaceIndex) {
        return XmPaintRegistry.INSTANCE.get(paintIndex(surfaceIndex));
    }

    public final XmPaint paint(XmSurface surface) {
        return XmPaintRegistry.INSTANCE.get(paintIndex(surface.ordinal()));
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
    
    
    ////////////////////////////////////////// WORLD ATTRIBUTES //////////////////////////////////////////
    
    protected int worldBits;
    
    public int posX() {
        return POS_X.getValue(this);
    }
    
    public int posY() {
        return POS_Y.getValue(this);
    }
    
    public int posZ() {
        return POS_Z.getValue(this);
    }

    public final V posX(int index) {
        POS_X.setValue(index, this);
        invalidateHashCode();
        return self;
    }

    public final V posY(int index) {
        POS_Y.setValue(index, this);
        invalidateHashCode();
        return self;
    }

    public final V posZ(int index) {
        POS_Z.setValue(index, this);
        invalidateHashCode();
        return self;
    }

    public final V pos(BlockPos pos) {
        POS_X.setValue((pos.getX()), this);
        POS_Y.setValue((pos.getY()), this);
        POS_Z.setValue((pos.getZ()), this);
        invalidateHashCode();
        return self;
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
    public final int species() {
        return SPECIES.getValue(this);
    }

    public final V species(int species) {
        SPECIES.setValue(species, this);
        invalidateHashCode();
        return self;
    }
    
    ////////////////////////////////////////// SHAPE ATTRIBUTES //////////////////////////////////////////
    
    protected int shapeBits;

    public final Direction.Axis axis() {
        return AXIS.getValue(this);
    }

    public final V axis(Direction.Axis axis) {
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
    public ClockwiseRotation axisRotation() {
        return AXIS_ROTATION.getValue(this);
    }

    public final V axisRotation(ClockwiseRotation rotation) {
        AXIS_ROTATION.setValue(rotation, this);
        invalidateHashCode();
        return self;
    }

    public int primitiveBits() {
        return PRIMITIVE_BITS.getValue(this);
    }

    public final V primitiveBits(int bits) {
        PRIMITIVE_BITS.setValue(bits, this);
        return self;
    }


    ////////////////////////////////////////// RENDERING //////////////////////////////////////////
    
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
    

    @Override
    @Environment(EnvType.CLIENT)
    public void emitQuads(RenderContext context) {
        context.meshConsumer().accept(mesh());
    }
}
