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



import grondag.fermion.varia.BitPacker64;
import grondag.xm2.api.connect.model.ClockwiseRotation;
import grondag.xm2.api.connect.state.CornerJoinState;
import grondag.xm2.api.connect.state.SimpleJoinState;
import grondag.xm2.api.connect.world.ModelStateFunction;
import grondag.xm2.block.XmBlockStateAccess;
import grondag.xm2.model.registry.ModelShapes;
import grondag.xm2.terrain.TerrainState;
import net.minecraft.util.math.Direction;

public class ModelStateData {
    /**
     * note that sign bit on core packer is reserved to persist static state during
     * serialization
     */
    public static final BitPacker64<ModelStateImpl> PACKER_CORE = new BitPacker64<ModelStateImpl>(m -> m.coreBits,
            (m, b) -> m.coreBits = b);
    public static final BitPacker64<ModelStateImpl>.IntElement SHAPE = PACKER_CORE.createIntElement(ModelShapes.MAX_SHAPES);
    public static final BitPacker64<ModelStateImpl>.IntElement POS_X = PACKER_CORE.createIntElement(256);
    public static final BitPacker64<ModelStateImpl>.IntElement POS_Y = PACKER_CORE.createIntElement(256);
    public static final BitPacker64<ModelStateImpl>.IntElement POS_Z = PACKER_CORE.createIntElement(256);
    public static final BitPacker64<ModelStateImpl>.EnumElement<Direction.Axis> AXIS = PACKER_CORE
            .createEnumElement(Direction.Axis.class);
    public static final BitPacker64<ModelStateImpl>.BooleanElement AXIS_INVERTED = PACKER_CORE.createBooleanElement();
    public static final BitPacker64<ModelStateImpl>.EnumElement<ClockwiseRotation> AXIS_ROTATION = PACKER_CORE
            .createEnumElement(ClockwiseRotation.class);

    public static final BitPacker64<ModelStateImpl> PACKER_SHAPE_BLOCK = new BitPacker64<ModelStateImpl>(m -> m.shapeBits0,
            (m, b) -> m.shapeBits0 = b);
    public static final BitPacker64<ModelStateImpl>.IntElement SPECIES = PACKER_SHAPE_BLOCK.createIntElement(16);
    public static final BitPacker64<ModelStateImpl>.IntElement BLOCK_JOIN = PACKER_SHAPE_BLOCK
            .createIntElement(CornerJoinState.STATE_COUNT);
    public static final BitPacker64<ModelStateImpl>.IntElement MASONRY_JOIN = PACKER_SHAPE_BLOCK
            .createIntElement(SimpleJoinState.STATE_COUNT);

    public static final BitPacker64<ModelStateImpl> PACKER_SHAPE_FLOW = new BitPacker64<ModelStateImpl>(m -> m.shapeBits0,
            (m, b) -> m.shapeBits0 = b);
    public static final BitPacker64<ModelStateImpl>.LongElement FLOW_JOIN = PACKER_SHAPE_FLOW
            .createLongElement(TerrainState.STATE_BIT_MASK + 1);

    public static final BitPacker64<ModelStateImpl> PACKER_SHAPE_MULTIBLOCK = new BitPacker64<ModelStateImpl>(m -> m.shapeBits0,
            (m, b) -> m.shapeBits0 = b);

    public static final BitPacker64<ModelStateImpl> PACKER_SHAPE_EXTRA = new BitPacker64<ModelStateImpl>(m -> m.shapeBits1,
            (m, b) -> m.shapeBits1 = b);
    /**
     * value semantics are owned by consumer - only constraints are size (39 bits)
     * and does not update from world
     */
    public static final BitPacker64<ModelStateImpl>.LongElement EXTRA_SHAPE_BITS = PACKER_SHAPE_EXTRA
            .createLongElement(1L << 39);

    /** used to compare states quickly for border joins */
    public static final long SHAPE_COMPARISON_MASK_0;
    public static final long SHAPE_COMPARISON_MASK_1;

    static {
        SHAPE_COMPARISON_MASK_0 = SHAPE.comparisonMask() | AXIS.comparisonMask() | AXIS_INVERTED.comparisonMask();

        SHAPE_COMPARISON_MASK_1 = EXTRA_SHAPE_BITS.comparisonMask();
    }

    /**
     * Use this as factory for model state block tests that DON'T need to refresh
     * from world.
     */
    public static final ModelStateFunction TEST_GETTER_STATIC = (w, b, p) -> XmBlockStateAccess.modelState(b, w, p, false);

    /**
     * Use this as factory for model state block tests that DO need to refresh from
     * world.
     */
    public static final ModelStateFunction TEST_GETTER_DYNAMIC = (w, b, p) -> XmBlockStateAccess.modelState(b, w, p, true);

    public static final BitPacker64<ModelStateImpl> STATE_PACKER = new BitPacker64<ModelStateImpl>(m -> m.stateFlags,
            (m, b) -> m.stateFlags = (int) b);

    /**
     * For readability.
     */
    public static final int STATE_FLAG_NONE = 0;

    /** see {@link #STATE_FLAG_IS_POPULATED} */
    public static final BitPacker64<ModelStateImpl>.BooleanElement STATE_BIT_IS_POPULATED = STATE_PACKER
            .createBooleanElement();
    /*
     * Enables lazy derivation - set after derivation is complete. NB - check logic
     * assumes that ALL bits are zero for simplicity.
     */
    public static final int STATE_FLAG_IS_POPULATED = (int) STATE_BIT_IS_POPULATED.comparisonMask();

    /** see {@link #STATE_FLAG_NEEDS_CORNER_JOIN} */
    public static final BitPacker64<ModelStateImpl>.BooleanElement STATE_BIT_NEEDS_CORNER_JOIN = STATE_PACKER
            .createBooleanElement();
    /**
     * Applies to block-type states. True if is a block type state and requires full
     * join state.
     */
    public static final int STATE_FLAG_NEEDS_CORNER_JOIN = (int) STATE_BIT_NEEDS_CORNER_JOIN.comparisonMask();

    /** see {@link #STATE_FLAG_NEEDS_SIMPLE_JOIN} */
    public static final BitPacker64<ModelStateImpl>.BooleanElement STATE_BIT_NEEDS_SIMPLE_JOIN = STATE_PACKER
            .createBooleanElement();
    /**
     * Applies to block-type states. True if is a block type state and requires full
     * join state.
     */
    public static final int STATE_FLAG_NEEDS_SIMPLE_JOIN = (int) STATE_BIT_NEEDS_SIMPLE_JOIN.comparisonMask();

    /** see {@link #STATE_FLAG_NEEDS_MASONRY_JOIN} */
    public static final BitPacker64<ModelStateImpl>.BooleanElement STATE_BIT_NEEDS_MASONRY_JOIN = STATE_PACKER
            .createBooleanElement();

    /**
     * Applies to block-type states. True if is a block type state and requires
     * masonry join info.
     */
    public static final int STATE_FLAG_NEEDS_MASONRY_JOIN = (int) STATE_BIT_NEEDS_MASONRY_JOIN.comparisonMask();

    /** see {@link #STATE_FLAG_NEEDS_POS} */
    public static final BitPacker64<ModelStateImpl>.BooleanElement STATE_BIT_NEEDS_POS = STATE_PACKER
            .createBooleanElement();
    /**
     * True if position (big-tex) world state is needed. Applies for block and flow
     * state formats.
     */
    public static final int STATE_FLAG_NEEDS_POS = (int) STATE_BIT_NEEDS_POS.comparisonMask();

    /** see {@link #STATE_FLAG_NEEDS_SPECIES} */
    public static final BitPacker64<ModelStateImpl>.BooleanElement STATE_BIT_NEEDS_SPECIES = STATE_PACKER
            .createBooleanElement();
    public static final int STATE_FLAG_NEEDS_SPECIES = (int) STATE_BIT_NEEDS_SPECIES.comparisonMask();

    /** see {@link #STATE_FLAG_HAS_AXIS} */
    public static final BitPacker64<ModelStateImpl>.BooleanElement STATE_BIT_HAS_AXIS = STATE_PACKER.createBooleanElement();
    public static final int STATE_FLAG_HAS_AXIS = (int) STATE_BIT_HAS_AXIS.comparisonMask();

    /** see {@link #STATE_FLAG_NEEDS_TEXTURE_ROTATION} */
    public static final BitPacker64<ModelStateImpl>.BooleanElement STATE_BIT_NEEDS_TEXTURE_ROTATION = STATE_PACKER
            .createBooleanElement();
    public static final int STATE_FLAG_NEEDS_TEXTURE_ROTATION = (int) STATE_BIT_NEEDS_TEXTURE_ROTATION.comparisonMask();

    /** see {@link #STATE_FLAG_HAS_AXIS_ORIENTATION} */
    public static final BitPacker64<ModelStateImpl>.BooleanElement STATE_BIT_HAS_AXIS_ORIENTATION = STATE_PACKER
            .createBooleanElement();
    public static final int STATE_FLAG_HAS_AXIS_ORIENTATION = (int) STATE_BIT_HAS_AXIS_ORIENTATION.comparisonMask();

    /** see {@link #STATE_FLAG_HAS_AXIS_ROTATION} */
    public static final BitPacker64<ModelStateImpl>.BooleanElement STATE_BIT_HAS_AXIS_ROTATION = STATE_PACKER
            .createBooleanElement();
    /**
     * Set if shape can be rotated around an axis. Only applies to block models;
     * multiblock models manage this situationally.
     */
    public static final int STATE_FLAG_HAS_AXIS_ROTATION = (int) STATE_BIT_HAS_AXIS_ROTATION.comparisonMask();

    /** see {@link #STATE_FLAG_HAS_TRANSLUCENT_GEOMETRY} */
    public static final BitPacker64<ModelStateImpl>.BooleanElement STATE_BIT_HAS_TRANSLUCENT_GEOMETRY = STATE_PACKER
            .createBooleanElement();
    /** Set if either Base/Cut or Lamp (if present) paint layers are translucent */
    public static final int STATE_FLAG_HAS_TRANSLUCENT_GEOMETRY = (int) STATE_BIT_HAS_TRANSLUCENT_GEOMETRY
            .comparisonMask();

    /** see {@link #STATE_FLAG_HAS_SOLID_RENDER} */
    public static final BitPacker64<ModelStateImpl>.BooleanElement STATE_BIT_HAS_SOLID_RENDER = STATE_PACKER
            .createBooleanElement();
    /** True if any layer sould render in the solid block render layer */
    public static final int STATE_FLAG_HAS_SOLID_RENDER = (int) STATE_BIT_HAS_SOLID_RENDER.comparisonMask();

    /** see {@link #STATE_FLAG_HAS_TRANSLUCENT_RENDER} */
    public static final BitPacker64<ModelStateImpl>.BooleanElement STATE_BIT_HAS_TRANSLUCENT_RENDER = STATE_PACKER
            .createBooleanElement();
    /** True if any layer should render in the translucent block render layer */
    public static final int STATE_FLAG_HAS_TRANSLUCENT_RENDER = (int) STATE_BIT_HAS_TRANSLUCENT_RENDER.comparisonMask();

    /**
     * use this to turn off flags that should not be used with non-block state
     * formats
     */
    public static final int STATE_FLAG_DISABLE_BLOCK_ONLY = ~(STATE_FLAG_NEEDS_CORNER_JOIN
            | STATE_FLAG_NEEDS_SIMPLE_JOIN | STATE_FLAG_NEEDS_MASONRY_JOIN | STATE_FLAG_NEEDS_SPECIES
            | STATE_FLAG_NEEDS_TEXTURE_ROTATION);

    // hide constructor
    private ModelStateData() {
        super();
    }
}
