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

package grondag.xm2.model.state;

import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_HAS_AXIS_ROTATION;
import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_NEEDS_CORNER_JOIN;
import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_NEEDS_MASONRY_JOIN;
import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_NEEDS_POS;
import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_NEEDS_SIMPLE_JOIN;
import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_NEEDS_SPECIES;
import static grondag.xm2.model.state.ModelStateData.TEST_GETTER_STATIC;

import grondag.fermion.varia.Useful;
import grondag.xm2.Xm;
import grondag.xm2.XmConfig;
import grondag.xm2.api.connect.model.ClockwiseRotation;
import grondag.xm2.api.connect.state.CornerJoinState;
import grondag.xm2.api.connect.state.SimpleJoinState;
import grondag.xm2.api.connect.world.BlockNeighbors;
import grondag.xm2.api.model.ModelPrimitive;
import grondag.xm2.api.model.ModelPrimitiveRegistry;
import grondag.xm2.api.model.ModelState;
import grondag.xm2.api.model.MutableModelState;
import grondag.xm2.block.XmBlockRegistryImpl.XmBlockStateImpl;
import grondag.xm2.block.XmMasonryMatch;
import grondag.xm2.connect.CornerJoinStateSelector;
import grondag.xm2.mesh.helper.PolyTransform;
import grondag.xm2.model.primitive.AbstractModelPrimitive;
import grondag.xm2.terrain.TerrainState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;

public class ModelStateImpl extends AbstractModelState implements MutableModelState {
    protected long coreBits;
    protected long shapeBits1;
    protected long shapeBits0;

    // default to white, full alpha

    private int hashCode = -1;

    /** contains indicators derived from shape and painters */
    protected int stateFlags;

    public ModelStateImpl(ModelPrimitive primitive) {
        super(primitive);
    }

    public ModelStateImpl(ModelStateImpl template) {
        super(template.primitive);
        this.coreBits = template.coreBits;
        this.shapeBits0 = template.shapeBits0;
        this.shapeBits1 = template.shapeBits1;
        System.arraycopy(template.paints, 0, this.paints, 0, paints.length);
    }

    @Override
    public ModelStateImpl mutableCopy() {
        return new ModelStateImpl(this);
    }

    @Override
    public ModelStateImpl copyFrom(ModelState templateIn) {
        final ModelStateImpl template = (ModelStateImpl) templateIn;
        final ModelPrimitive savePrimitive = primitive();
        this.coreBits = template.coreBits;
        this.shapeBits0 = template.shapeBits0;
        this.shapeBits1 = template.shapeBits1;
        System.arraycopy(template.paints, 0, this.paints, 0, paints.length);
        ModelStateData.SHAPE.setValue(savePrimitive.index(), this);
        return this;
    }
    
    public final int[] serializeToInts() {
        int[] result = new int[16 + 6];
        result[0] = (int) (this.isStatic ? (coreBits >> 32) | Useful.INT_SIGN_BIT : (coreBits >> 32));
        result[1] = (int) (coreBits);

        result[2] = (int) (shapeBits1 >> 32);
        result[3] = (int) (shapeBits1);

        result[4] = (int) (shapeBits0 >> 32);
        result[5] = (int) (shapeBits0);

        System.arraycopy(paints, 0, result, 6, paints.length);
        
        return result;
    }

    /**
     * Note does not reset state flag - do that if calling on an existing instance.
     */
    private void deserializeFromInts(int[] bits) {
        // sign on first long word is used to store static indicator
        this.isStatic = (Useful.INT_SIGN_BIT & bits[0]) == Useful.INT_SIGN_BIT;

        this.coreBits = ((long) (Useful.INT_SIGN_BIT_INVERSE & bits[0])) << 32 | (bits[1] & 0xffffffffL);
        this.shapeBits1 = ((long) bits[2]) << 32 | (bits[3] & 0xffffffffL);
        this.shapeBits0 = ((long) bits[4]) << 32 | (bits[5] & 0xffffffffL);

        System.arraycopy(bits, 6, paints, 0, paints.length);
    }

    private void populateStateFlagsIfNeeded() {
        if (this.stateFlags == 0) {
            this.stateFlags = ModelStateFlagHelper.getFlags(this);
        }
    }

    private void clearStateFlags() {
        if (this.stateFlags != 0) {
            this.stateFlags = 0;
        }
    }

    /**
     * Does NOT consider isStatic in comparison. <br>
     * <br>
     * 
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return this == obj ? true : obj instanceof ModelStateImpl && equalsInner((ModelStateImpl) obj);
    }

    protected boolean equalsInner(ModelStateImpl other) {
        if(this.primitive == other.primitive
                && this.coreBits == other.coreBits
                && this.shapeBits0 == other.shapeBits0
                && this.shapeBits1 == other.shapeBits1) {
            final int limit = paints.length;
            for(int i = 0; i < limit; i++) {
                if(paints[i] != other.paints[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    @Override
    public boolean equalsIncludeStatic(Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof ModelStateImpl) {
            ModelStateImpl other = (ModelStateImpl) obj;
            return this.isStatic == other.isStatic && equalsInner(other);
        } else {
            return false;
        }
    }

    private void invalidateHashCode() {
        if (this.hashCode != -1)
            this.hashCode = -1;
    }

    @Override
    public int hashCode() {
        if (hashCode == -1) {
            hashCode = (int) Useful.longHash(this.coreBits ^ this.shapeBits0 ^ this.shapeBits1);
            final int limit = paints.length;
            for(int i = 0; i < limit; i++) {
                hashCode ^= paints[i];
            }
        }
        return hashCode;
    }

    // TODO: remove - blocks should know how to refresh their state - state should
    // not be coupled
    @Deprecated
    @Override
    public ModelStateImpl refreshFromWorld(XmBlockStateImpl xmState, BlockView world, BlockPos pos) {

        // Output.getLog().info("ModelState.refreshFromWorld static=" + this.isStatic +
        // " @" + pos.toString());
        if (this.isStatic)
            return this;

        populateStateFlagsIfNeeded();

        switch (((AbstractModelPrimitive) primitive()).stateFormat) {
            case BLOCK:

                if ((stateFlags & STATE_FLAG_NEEDS_POS) == STATE_FLAG_NEEDS_POS)
                    refreshBlockPosFromWorld(pos, 255);

                BlockNeighbors neighbors = null;

                if ((STATE_FLAG_NEEDS_CORNER_JOIN & stateFlags) == STATE_FLAG_NEEDS_CORNER_JOIN) {
                    neighbors = BlockNeighbors.claim(world, pos, TEST_GETTER_STATIC, xmState.blockJoinTest());
                    ModelStateData.BLOCK_JOIN.setValue(CornerJoinState.fromWorld(neighbors).ordinal(), this);

                } else if ((STATE_FLAG_NEEDS_SIMPLE_JOIN & stateFlags) == STATE_FLAG_NEEDS_SIMPLE_JOIN) {
                    neighbors = BlockNeighbors.claim(world, pos, TEST_GETTER_STATIC, xmState.blockJoinTest());
                    ModelStateData.BLOCK_JOIN.setValue(SimpleJoinState.fromWorld(neighbors).ordinal(), this);
                }

                if ((STATE_FLAG_NEEDS_MASONRY_JOIN & stateFlags) == STATE_FLAG_NEEDS_MASONRY_JOIN) {
                    if (neighbors == null) {
                        neighbors = BlockNeighbors.claim(world, pos, TEST_GETTER_STATIC, XmMasonryMatch.INSTANCE);
                    } else {
                        neighbors.withTest(XmMasonryMatch.INSTANCE);
                    }
                    ModelStateData.MASONRY_JOIN.setValue(SimpleJoinState.fromWorld(neighbors).ordinal(), this);
                }

                if (neighbors != null) {
                    neighbors.release();
                }

                break;

            case FLOW:
                // terrain blocks need larger position space to drive texture randomization
                // because doesn't have per-block rotation or version
                if ((stateFlags & STATE_FLAG_NEEDS_POS) == STATE_FLAG_NEEDS_POS)
                    refreshBlockPosFromWorld(pos, 255);
                TerrainState.produceBitsFromWorldStatically(xmState.blockState, world, pos, (t, h) -> {
                    ModelStateData.FLOW_JOIN.setValue(t, this);
                    ModelStateData.EXTRA_SHAPE_BITS.setValue(h, this);
                    return null;
                });

                break;

            default:
                break;

        }

        this.invalidateHashCode();

        return this;
    }

    /**
     * Saves world block pos relative to cube boundary specified by mask. Used by
     * BigTex surface painting for texture randomization on non-multiblock shapes.
     */
    private void refreshBlockPosFromWorld(BlockPos pos, int mask) {
        ModelStateData.POS_X.setValue((pos.getX() & mask), this);
        ModelStateData.POS_Y.setValue((pos.getY() & mask), this);
        ModelStateData.POS_Z.setValue((pos.getZ() & mask), this);
    }

    ////////////////////////////////////////////////////
    // PACKER 0 ATTRIBUTES (NOT SHAPE-DEPENDENT)
    ////////////////////////////////////////////////////

    @Override
    public Direction.Axis getAxis() {
        return ModelStateData.AXIS.getValue(this);
    }

    @Override
    public void setAxis(Direction.Axis axis) {
        ModelStateData.AXIS.setValue(axis, this);
        invalidateHashCode();
    }

    @Override
    public boolean isAxisInverted() {
        return ModelStateData.AXIS_INVERTED.getValue(this);
    }

    @Override
    public void setAxisInverted(boolean isInverted) {
        ModelStateData.AXIS_INVERTED.setValue(isInverted, this);
        invalidateHashCode();
    }

    ////////////////////////////////////////////////////
    // PACKER 2 ATTRIBUTES (NOT SHAPE-DEPENDENT)
    ////////////////////////////////////////////////////

    @Override
    public int posX() {
        return ModelStateData.POS_X.getValue(this);
    }

    @Override
    public void posX(int index) {
        ModelStateData.POS_X.setValue(index, this);
        invalidateHashCode();
    }

    @Override
    public int posY() {
        return ModelStateData.POS_Y.getValue(this);
    }

    @Override
    public void posY(int index) {
        ModelStateData.POS_Y.setValue(index, this);
        invalidateHashCode();
    }

    @Override
    public int posZ() {
        return ModelStateData.POS_Z.getValue(this);
    }

    @Override
    public void posZ(int index) {
        ModelStateData.POS_Z.setValue(index, this);
        invalidateHashCode();
    }

    @Override
    public long getStaticShapeBits() {
        return ModelStateData.EXTRA_SHAPE_BITS.getValue(this);
    }

    @Override
    public void setStaticShapeBits(long bits) {
        ModelStateData.EXTRA_SHAPE_BITS.setValue(bits, this);
        invalidateHashCode();
    }

    ////////////////////////////////////////////////////
    // PACKER 3 ATTRIBUTES (BLOCK FORMAT)
    ////////////////////////////////////////////////////

    @Override
    public int species() {
        this.populateStateFlagsIfNeeded();

        if (XmConfig.BLOCKS.debugModelState
                && !this.hasSpecies())
            Xm.LOG.warn("getSpecies on model state does not apply for shape");

        return this.hasSpecies() ? ModelStateData.SPECIES.getValue(this) : 0;
    }

    @Override
    public void species(int species) {
        this.populateStateFlagsIfNeeded();

        if (XmConfig.BLOCKS.debugModelState
                && !this.hasSpecies())
            Xm.LOG.warn("setSpecies on model state does not apply for shape");

        if (this.hasSpecies()) {
            ModelStateData.SPECIES.setValue(species, this);
            invalidateHashCode();
        }
    }

    @Override
    public CornerJoinState cornerJoin() {
        if (XmConfig.BLOCKS.debugModelState) {
            populateStateFlagsIfNeeded();
            if ((stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) == 0
                    || ((AbstractModelPrimitive) primitive()).stateFormat != StateFormat.BLOCK)
                Xm.LOG.warn("getCornerJoin on model state does not apply for shape");
        }

        return CornerJoinStateSelector.fromOrdinal(
                MathHelper.clamp(ModelStateData.BLOCK_JOIN.getValue(this), 0, CornerJoinState.STATE_COUNT - 1));
    }

    @Override
    public void cornerJoin(CornerJoinState join) {
        if (XmConfig.BLOCKS.debugModelState) {
            populateStateFlagsIfNeeded();
            if ((stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) == 0
                    || ((AbstractModelPrimitive) primitive()).stateFormat != StateFormat.BLOCK)
                Xm.LOG.warn("setCornerJoin on model state does not apply for shape");
        }

        ModelStateData.BLOCK_JOIN.setValue(join.ordinal(), this);
        invalidateHashCode();
    }

    @Override
    public SimpleJoinState simpleJoin() {
        if (XmConfig.BLOCKS.debugModelState
                && ((AbstractModelPrimitive) primitive()).stateFormat != StateFormat.BLOCK)
            Xm.LOG.warn("getSimpleJoin on model state does not apply for shape");

        // If this state is using corner join, join index is for a corner join
        // and so need to derive simple join from the corner join
        populateStateFlagsIfNeeded();
        return ((stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) == 0)
                ? SimpleJoinState.fromOrdinal(ModelStateData.BLOCK_JOIN.getValue(this))
                : cornerJoin().simpleJoin();
    }

    @Override
    public void simpleJoin(SimpleJoinState join) {
        if (XmConfig.BLOCKS.debugModelState) {
            if (((AbstractModelPrimitive) primitive()).stateFormat != StateFormat.BLOCK) {
                Xm.LOG.warn("Ignored setSimpleJoin on model state that does not apply for shape");
                return;
            }

            populateStateFlagsIfNeeded();
            if ((stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) != 0) {
                Xm.LOG.warn("Ignored setSimpleJoin on model state that uses corner join instead");
                return;
            }
        }

        ModelStateData.BLOCK_JOIN.setValue(join.ordinal(), this);
        invalidateHashCode();
    }

    @Override
    public SimpleJoinState masonryJoin() {
        if (XmConfig.BLOCKS.debugModelState
                && (((AbstractModelPrimitive) primitive()).stateFormat != StateFormat.BLOCK
                        || (stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) == 0)
                || ((stateFlags & STATE_FLAG_NEEDS_MASONRY_JOIN) == 0))
            Xm.LOG.warn("getMasonryJoin on model state does not apply for shape");

        populateStateFlagsIfNeeded();
        return SimpleJoinState.fromOrdinal(ModelStateData.MASONRY_JOIN.getValue(this));
    }

    @Override
    public void masonryJoin(SimpleJoinState join) {
        if (XmConfig.BLOCKS.debugModelState) {
            populateStateFlagsIfNeeded();
            if (((AbstractModelPrimitive) primitive()).stateFormat != StateFormat.BLOCK) {
                Xm.LOG.warn("Ignored setMasonryJoin on model state that does not apply for shape");
                return;
            }

            if (((stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) == 0)
                    || ((stateFlags & STATE_FLAG_NEEDS_MASONRY_JOIN) == 0)) {
                Xm.LOG.warn("Ignored setMasonryJoin on model state for which it does not apply");
                return;
            }
        }

        ModelStateData.MASONRY_JOIN.setValue(join.ordinal(), this);
        invalidateHashCode();
    }

    @Override
    public ClockwiseRotation getAxisRotation() {
        return ModelStateData.AXIS_ROTATION.getValue(this);
    }

    @Override
    public void setAxisRotation(ClockwiseRotation rotation) {
        populateStateFlagsIfNeeded();
        if (((AbstractModelPrimitive) primitive()).stateFormat != StateFormat.BLOCK) {
            if (XmConfig.BLOCKS.debugModelState)
                Xm.LOG.warn("Ignored setAxisRotation on model state that does not apply for shape");
            return;
        }

        if ((stateFlags & STATE_FLAG_HAS_AXIS_ROTATION) == 0) {
            if (XmConfig.BLOCKS.debugModelState)
                Xm.LOG.warn("Ignored setAxisRotation on model state for which it does not apply");
            return;
        }

        ModelStateData.AXIS_ROTATION.setValue(rotation, this);
        invalidateHashCode();
    }

    ////////////////////////////////////////////////////
    // PACKER 3 ATTRIBUTES (FLOWING TERRAIN FORMAT)
    ////////////////////////////////////////////////////

    @Override
    public long getTerrainStateKey() {
        assert ((AbstractModelPrimitive) primitive()).stateFormat == StateFormat.FLOW : "getTerrainState on model state does not apply for shape";
        return ModelStateData.FLOW_JOIN.getValue(this);
    }

    @Override
    public int getTerrainHotness() {
        assert ((AbstractModelPrimitive) primitive()).stateFormat == StateFormat.FLOW : "getTerrainState on model state does not apply for shape";
        return (int) ModelStateData.EXTRA_SHAPE_BITS.getValue(this);
    }

    @Override
    public void setTerrainStateKey(long terrainStateKey) {
        assert ((AbstractModelPrimitive) primitive()).stateFormat == StateFormat.FLOW : "getTerrainState on model state does not apply for shape";
        ModelStateData.FLOW_JOIN.setValue(terrainStateKey, this);
    }

    @Override
    public TerrainState getTerrainState() {
        assert ((AbstractModelPrimitive) primitive()).stateFormat == StateFormat.FLOW : "getTerrainState on model state does not apply for shape";
        return new TerrainState(ModelStateData.FLOW_JOIN.getValue(this),
                (int) ModelStateData.EXTRA_SHAPE_BITS.getValue(this));
    }

    @Override
    public void setTerrainState(TerrainState flowState) {
        assert ((AbstractModelPrimitive) primitive()).stateFormat == StateFormat.FLOW : "getTerrainState on model state does not apply for shape";
        ModelStateData.FLOW_JOIN.setValue(flowState.getStateKey(), this);
        ModelStateData.EXTRA_SHAPE_BITS.setValue(flowState.getHotness(), this);
        invalidateHashCode();
    }

    @Override
    public boolean hasSpecies() {
        this.populateStateFlagsIfNeeded();
        return ((this.stateFlags & STATE_FLAG_NEEDS_SPECIES) == STATE_FLAG_NEEDS_SPECIES);
    }

    @Override
    public final boolean doShapeAndAppearanceMatch(ModelState other) {
        final ModelStateImpl o = (ModelStateImpl) other;
        return (this.coreBits & ModelStateData.SHAPE_COMPARISON_MASK_0) == (o.coreBits
                & ModelStateData.SHAPE_COMPARISON_MASK_0)
                && (this.shapeBits1 & ModelStateData.SHAPE_COMPARISON_MASK_1) == (o.shapeBits1
                        & ModelStateData.SHAPE_COMPARISON_MASK_1)
                && doesAppearanceMatch(other);
    }

    @Override
    public boolean doesAppearanceMatch(ModelState other) {
        //TODO: compare paints
        return true;
    }

    // PERF: bottleneck for Pyroclasm
    @Override
    public ModelStateImpl geometricState() {
        this.populateStateFlagsIfNeeded();
        final AbstractModelPrimitive primitive = (AbstractModelPrimitive) primitive();
        ModelStateImpl result = mutableCopy();

        switch (primitive.stateFormat) {
            case BLOCK:
                result.setStaticShapeBits(this.getStaticShapeBits());
                if (this.hasAxis())
                    result.setAxis(this.getAxis());
                if (this.hasAxisOrientation())
                    result.setAxisInverted(this.isAxisInverted());
                if (this.hasAxisRotation())
                    result.setAxisRotation(this.getAxisRotation());
                if ((primitive.stateFlags(this) & STATE_FLAG_NEEDS_CORNER_JOIN) == STATE_FLAG_NEEDS_CORNER_JOIN) {
                    result.cornerJoin(this.cornerJoin());
                } else if ((this.primitive().stateFlags(this)
                        & STATE_FLAG_NEEDS_SIMPLE_JOIN) == STATE_FLAG_NEEDS_SIMPLE_JOIN) {
                    result.simpleJoin(this.simpleJoin());
                }
                break;

            case FLOW:
                ModelStateData.FLOW_JOIN.setValue(ModelStateData.FLOW_JOIN.getValue(this), result);
                break;

            default:
                break;

        }
        return result;
    }
    
    @Override
    public Direction rotateFace(Direction face) {
        return PolyTransform.rotateFace(this, face);
    }

    public static ModelStateImpl deserializeFromNBTIfPresent(CompoundTag tag) {
        ModelPrimitive shape = ModelPrimitiveRegistry.INSTANCE.get(tag.getString(ModelStateTagHelper.NBT_SHAPE));
        if(shape == null) {
            return null;
        }
        ModelStateImpl result = new ModelStateImpl(shape);
    
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
        this.coreBits = pBuff.readLong();
        this.shapeBits0 = pBuff.readLong();
        this.shapeBits1 = pBuff.readVarLong();
        final int limit = paints.length;
        for(int i = 0; i < limit; i++) {
            this.paints[i] = pBuff.readVarInt();
        }
    }

    @Override
    public void toBytes(PacketByteBuf pBuff) {
        pBuff.writeLong(this.coreBits);
        pBuff.writeLong(this.shapeBits0);
        pBuff.writeVarLong(this.shapeBits1);
        final int limit = paints.length;
        for(int i = 0; i < limit; i++) {
            pBuff.writeVarInt(paints[i]);
        }
    }

    @Override
    public boolean isImmutable() {
        return false;
    }

    @Override
    public ImmutableModelStateImpl toImmutable() {
        return new ImmutableModelStateImpl(this);
    }

    @Override
    public void paint(int surfaceIndex, int paintIndex) {
        paints[surfaceIndex] = paintIndex;
    }

    @Override
    public int paintIndex(int surfaceIndex) {
        return paints[surfaceIndex];
    }
}
