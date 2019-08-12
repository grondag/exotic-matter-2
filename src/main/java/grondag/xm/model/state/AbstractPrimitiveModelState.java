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
import grondag.xm.api.modelstate.PrimitiveModelState;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.paint.XmPaintRegistry;
import grondag.xm.api.primitive.ModelPrimitive;
import grondag.xm.api.surface.XmSurface;
import grondag.xm.api.surface.XmSurfaceList;
import grondag.xm.connect.CornerJoinStateSelector;
import grondag.xm.mesh.helper.PolyTransform;
import grondag.xm.mesh.polygon.Polygon;
import grondag.xm.model.varia.BlockOrientationType;
import grondag.xm.painting.QuadPaintHandler;
import it.unimi.dsi.fastutil.HashCommon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class AbstractPrimitiveModelState
    <V extends AbstractPrimitiveModelState<V, R, W>, R extends PrimitiveModelState<R, W>, W extends PrimitiveModelState.Mutable<R,W>> 
    extends AbstractModelState 
    implements ModelState.Mutable, PrimitiveModelState<R, W>, PrimitiveModelState.Mutable<R, W>
{
    
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
    
    public static class ModelStateFactoryImpl<T extends AbstractPrimitiveModelState<T, R, W>, R extends PrimitiveModelState<R, W>, W extends PrimitiveModelState.Mutable<R,W>> 
        implements ModelStateFactory<R, W> 
    {
        private final ArrayBlockingQueue<T> POOL = new ArrayBlockingQueue<>(4096);
        
        private final Supplier<T> factory;
        
        public ModelStateFactoryImpl(Supplier<T> factory) {
            this.factory = factory;
        }
        
        public final T claimInner(ModelPrimitive<R, W> primitive) {
            T result = POOL.poll();
            if (result == null) {
                result = factory.get();
                result.isImmutable = false;
            }
            result.retain();
            result.primitive = primitive;
            return result;
        }
        
        protected final T claimInner(T template) {
            T result = POOL.poll();
            if (result == null) {
                result = factory.get();
                result.isImmutable = false;
            }
            result.retain();
            result.primitive = template.primitive;
            result.copyInternal(template);
            return result;
        }
        
        @Override
        public final W claim(ModelPrimitive<R, W> primitive) {
            return (W) claimInner(primitive);
        }
        
        public final W claim(R template, ModelPrimitive<R, W> primitive) {
            W result = claim(primitive);
            result.copyFrom(template);
            return result;
        }
        
        public final W claim(R template) {
            return claim(template, template.primitive());
        }
        
        @Override
        public final W fromTag(ModelPrimitive<R, W> shape, CompoundTag tag) {
            T result = claimInner(shape);

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
            return (W) result;
        }

        @Override
        public final W fromBuffer(ModelPrimitive<R, W> shape, PacketByteBuf buf) {
            T result = claimInner(shape);
            result.fromBytes(buf);
            return (W) result;
        }
    }
    
    ////////////////////////////////////////// GENERAL DECLARATIONS //////////////////////////////////////////
    
    @Override
    protected int computeHashCode() {
        int result = 0;
        final int limit = this.surfaceCount();
        for (int i = 0; i < limit; i++) {
            result ^= paints[i];
        }
        return result ^ HashCommon.mix(this.shapeBits | (this.worldBits << 32));
    }
    
    protected abstract ModelStateFactoryImpl<V, R, W> factoryImpl();
    
    @Override
    public final ModelStateFactory<R, W> factory() {
        return factoryImpl();
    }
    
    @Override
    public final <T> T applyAndRelease(Function<ModelState, T> func) {
        final T result = func.apply((W)this);
        this.release();
        return result;
    }
    
    @Override
    protected final void onLastRelease() {
        factoryImpl().POOL.offer((V)this);
    }

    @Override
    public R toImmutable() {
        if(this.isImmutable) {
            return (R)this;
        } else {
            V result = factoryImpl().claimInner((V)this);
            result.isImmutable = true;
            return (R)result;
        }
    }

    protected <T extends AbstractPrimitiveModelState> void copyInternal(T template) {
        final AbstractPrimitiveModelState other = (AbstractPrimitiveModelState) template;
        System.arraycopy(template.paints, 0, this.paints, 0, this.surfaceCount());
        worldBits = other.worldBits;
        shapeBits = other.shapeBits;
    }
    
    @Override
    public W copyFrom(ModelState template) {
        copyInternal((V)template);
        return (W)this;
    }

    @Override
    public R releaseToImmutable() {
        final R result = this.toImmutable();
        this.release();
        return result;
    }
    
    @Override
    public W mutableCopy() {
        return (W) factoryImpl().claimInner((V)this);
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
        final AbstractPrimitiveModelState other = (AbstractPrimitiveModelState) obj;
        return this.primitive == other.primitive 
                && this.worldBits == other.worldBits
                && this.shapeBits == other.shapeBits
                && doPaintsMatchNative(other);
    }

    @Override
    public final boolean equalsIncludeStatic(Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof AbstractPrimitiveModelState) {
            AbstractPrimitiveModelState other = (AbstractPrimitiveModelState) obj;
            return this.isStatic == other.isStatic && equalsInner(other);
        } else {
            return false;
        }
    }
    
    /**
     * Returns true if visual elements and geometry match. Does not consider species
     * in matching.
     */
    @Override
    public final boolean doShapeAndAppearanceMatch(ModelState other) {
        if(other.getClass() != this.getClass()) return false;
        return primitive.doesShapeMatch((R)this, (R)other) && doesAppearanceMatch(other);
    }
    
    /**
     * Returns true if visual elements match. Does not consider species or geometry
     * in matching.
     */
    @Override
    public final boolean doesAppearanceMatch(ModelState other) {
        return other != null && doPaintsMatch(other);
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
        final int limit = primitive.surfaces((R)this).size();
        for (int i = 0; i < limit; i++) {
            this.paints[i] = pBuff.readVarInt();
        }
    }
    
    @Override
    public void toBytes(PacketByteBuf pBuff) {
        pBuff.writeVarInt(primitive.index());
        pBuff.writeInt(shapeBits);
        pBuff.writeInt(worldBits);
        final int limit = primitive.surfaces((R)this).size();
        for (int i = 0; i < limit; i++) {
            pBuff.writeVarInt(paints[i]);
        }
    }
    
    ////////////////////////////////////////// STATE FLAGS //////////////////////////////////////////
    
    /** contains indicators derived from shape and painters */
    private int stateFlags = 0;
    
    @Override
    public final int stateFlags() {
        int result = stateFlags;
        if (result == 0) {

            result = ModelStateFlags.STATE_FLAG_IS_POPULATED | primitive.stateFlags((R)this);

            final int surfCount = primitive.surfaces((R)this).size();
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
    
    protected ModelPrimitive<R, W> primitive;

    @Override
    public final ModelPrimitive<R, W> primitive() {
        return primitive;
    }
    
    @Override
    public final void produceQuads(Consumer<Polygon> target) {
        primitive.produceQuads((R)this, target);
    }
    
    @Override
    public W geometricState() {
        return primitive.geometricState((R)this);
    }
    
    @Override
    public final boolean hasAxisOrientation() {
      return primitive.hasAxisOrientation((R)this);
    }
    
    @Override
    public final boolean hasAxisRotation() {
      return primitive.hasAxisRotation((R)this);
    }
    
    @Override
    public final boolean hasAxis() {
      return primitive.hasAxis((R)this);
    }
    
    @Override
    public final BlockOrientationType orientationType() {
      return primitive.orientationType((R)this);
    }
    
    protected final int surfaceCount() {
        return primitive.surfaces((R)this).size();
    }
    
    @Override
    public final boolean isAxisOrthogonalToPlacementFace() {
        return primitive.isAxisOrthogonalToPlacementFace();
    }
    
    @Override
    public Direction rotateFace(Direction face) {
        return PolyTransform.rotateFace((R)this, face);
    }
    
    ////////////////////////////////////////// WORLD REFRESH CONTROL //////////////////////////////////////////
    
    protected boolean isStatic = false;

    @Override
    public final boolean isStatic() {
        return this.isStatic;
    }

    @Override
    public final W setStatic(boolean isStatic) {
        this.isStatic = isStatic;
        return (W)this;
    }
    
    ////////////////////////////////////////// PAINT //////////////////////////////////////////
    
    protected abstract int maxSurfaces();

    protected final int[] paints = new int[maxSurfaces()];
    
    @Override
    public final boolean doPaintsMatch(ModelState other) {
        return other instanceof AbstractPrimitiveModelState && doPaintsMatchNative((AbstractPrimitiveModelState) other);
    }
    
    protected final boolean doPaintsMatchNative(AbstractPrimitiveModelState other) {
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
        }
        return false;
    }

    protected final void paintInner(int surfaceIndex, int paintIndex) {
        paints[surfaceIndex] = paintIndex;
    }

    @Override
    public final int paintIndex(int surfaceIndex) {
        return paints[surfaceIndex];
    }
    
    @Override
    public final XmPaint paint(int surfaceIndex) {
        return XmPaintRegistry.INSTANCE.get(paintIndex(surfaceIndex));
    }

    @Override
    public final XmPaint paint(XmSurface surface) {
        return XmPaintRegistry.INSTANCE.get(paintIndex(surface.ordinal()));
    }
    
    @Override
    public final W paint(int surfaceIndex, int paintIndex) {
        paintInner(surfaceIndex, paintIndex);
        return (W)this;
    }
    
    @Override
    public final W paint(int surfaceIndex, XmPaint paint) {
      return paint(surfaceIndex, paint.index());
    }
    
    @Override
    public final W paint(XmSurface surface, XmPaint paint) {
      return paint(surface.ordinal(), paint.index());
    }
    
    @Override
    public final W paint(XmSurface surface, int paintIndex) {
      return paint(surface.ordinal(), paintIndex);
    }

    @Override
    public final W paintAll(XmPaint paint) {
      return paintAll(paint.index());
    }

    @Override
    public final W paintAll(int paintIndex) {
      XmSurfaceList slist = primitive().surfaces((R)this);
      final int limit = slist.size();
      for (int i = 0; i < limit; i++) {
          paint(i, paintIndex);
      }
      return (W)this;
    }
    
    
    ////////////////////////////////////////// WORLD ATTRIBUTES //////////////////////////////////////////
    
    protected int worldBits;
    
    @Override
    public int posX() {
        return POS_X.getValue(this);
    }
    
    @Override
    public int posY() {
        return POS_Y.getValue(this);
    }
    
    @Override
    public int posZ() {
        return POS_Z.getValue(this);
    }

    @Override
    public final W posX(int index) {
        POS_X.setValue(index, this);
        invalidateHashCode();
        return (W)this;
    }

    @Override
    public final W posY(int index) {
        POS_Y.setValue(index, this);
        invalidateHashCode();
        return (W)this;
    }

    @Override
    public final W posZ(int index) {
        POS_Z.setValue(index, this);
        invalidateHashCode();
        return (W)this;
    }

    @Override
    public final W pos(BlockPos pos) {
        POS_X.setValue((pos.getX()), this);
        POS_Y.setValue((pos.getY()), this);
        POS_Z.setValue((pos.getZ()), this);
        invalidateHashCode();
        return (W)this;
    }
    
    /**
     * Means that one or more elements (like a texture) uses species. Does not mean
     * that the shape or block actually capture or generate species other than 0.
     */
    @Override
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
    @Override
    public final int species() {
        return SPECIES.getValue(this);
    }

    @Override
    public final W species(int species) {
        SPECIES.setValue(species, this);
        invalidateHashCode();
        return (W)this;
    }
    
    ////////////////////////////////////////// SHAPE ATTRIBUTES //////////////////////////////////////////
    
    protected int shapeBits;

    @Override
    public final Direction.Axis axis() {
        return AXIS.getValue(this);
    }

    @Override
    public final W axis(Direction.Axis axis) {
        AXIS.setValue(axis, this);
        invalidateHashCode();
        return (W)this;
    }

    @Override
    public boolean isAxisInverted() {
        return AXIS_INVERTED.getValue(this);
    }

    @Override
    public final W setAxisInverted(boolean isInverted) {
        AXIS_INVERTED.setValue(isInverted, this);
        invalidateHashCode();
        return (W)this;
    }

    @Override
    public CornerJoinState cornerJoin() {
        return CornerJoinStateSelector.fromOrdinal(MathHelper.clamp(BLOCK_JOIN.getValue(this), 0, CornerJoinState.STATE_COUNT - 1));
    }

    @Override
    public final W cornerJoin(CornerJoinState join) {
        BLOCK_JOIN.setValue(join.ordinal(), this);
        invalidateHashCode();
        return (W)this;
    }

    @Override
    public SimpleJoinState simpleJoin() {
        // If this state is using corner join, join index is for a corner join
        // and so need to derive simple join from the corner join
        final int stateFlags = stateFlags();
        return ((stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) == 0) ? SimpleJoinState.fromOrdinal(BLOCK_JOIN.getValue(this)) : cornerJoin().simpleJoin();
    }

    @Override
    public final W simpleJoin(SimpleJoinState join) {
        BLOCK_JOIN.setValue(join.ordinal(), this);
        invalidateHashCode();
        return (W)this;
    }

    @Override
    public SimpleJoinState masonryJoin() {
        return SimpleJoinState.fromOrdinal(MASONRY_JOIN.getValue(this));
    }

    @Override
    public final W masonryJoin(SimpleJoinState join) {
        MASONRY_JOIN.setValue(join.ordinal(), this);
        invalidateHashCode();
        return (W)this;
    }

    /**
     * For machines and other blocks with a privileged horizontal face, North is
     * considered the zero rotation.
     */
    @Override
    public ClockwiseRotation axisRotation() {
        return AXIS_ROTATION.getValue(this);
    }

    @Override
    public final W axisRotation(ClockwiseRotation rotation) {
        AXIS_ROTATION.setValue(rotation, this);
        invalidateHashCode();
        return (W)this;
    }

    @Override
    public int primitiveBits() {
        return PRIMITIVE_BITS.getValue(this);
    }

    @Override
    public final W primitiveBits(int bits) {
        PRIMITIVE_BITS.setValue(bits, this);
        return (W)this;
    }


    ////////////////////////////////////////// RENDERING //////////////////////////////////////////
    
    @Environment(EnvType.CLIENT)
    private Mesh mesh = null;

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
    public final void emitQuads(RenderContext context) {
        context.meshConsumer().accept(mesh());
    }
    
    @Environment(EnvType.CLIENT)
    private Sprite particleSprite = null;
    
    @Environment(EnvType.CLIENT)
    private int particleColorARBG = 0;
    
    @Override
    @Environment(EnvType.CLIENT)
    public final Sprite particleSprite() {
        if(particleSprite == null) {
            final Mesh mesh = mesh();
            mesh.forEach(q -> {
                if(particleSprite == null) {
                    SpriteFinder finder = SpriteFinder.get(MinecraftClient.getInstance().getSpriteAtlas());
                    particleSprite = finder.find(q, 0);
                    particleColorARBG = q.spriteColor(0, 0);
                }
            });
            if(particleSprite == null) {
                particleSprite = MissingSprite.getMissingSprite();
            }
        }
        return particleSprite;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public final int particleColorARBG() {
        final int result = particleColorARBG;
        if(result == 0) {
            // if zero then probably requires lookup
            particleSprite();
            return particleColorARBG;
        } else {
            return result;
        }
    }
    
    @Environment(EnvType.CLIENT)
    private List<BakedQuad>[] quadLists = null;

    @Override
    @Environment(EnvType.CLIENT)
    public final List<BakedQuad> getBakedQuads(BlockState state, Direction face, Random rand) {
        List<BakedQuad>[] lists = quadLists;
        if (lists == null) {
            lists = ModelHelper.toQuadLists(mesh());
            quadLists = lists;
        }
        List<BakedQuad> result = lists[face == null ? 6 : face.getId()];
        return result == null ? ImmutableList.of() : result;
    }
}
