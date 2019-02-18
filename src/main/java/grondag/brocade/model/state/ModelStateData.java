package grondag.brocade.model.state;


import javax.annotation.Nullable;

import grondag.exotic_matter.block.ISuperBlock;
import grondag.exotic_matter.block.SuperBlockWorldAccess;
import grondag.exotic_matter.model.mesh.ModelShapes;
import grondag.exotic_matter.model.painting.PaintLayer;
import grondag.exotic_matter.model.painting.VertexProcessors;
import grondag.exotic_matter.model.texture.TexturePaletteRegistry;
import grondag.exotic_matter.terrain.TerrainState;
import grondag.exotic_matter.varia.BitPacker64;
import grondag.exotic_matter.world.CornerJoinBlockStateSelector;
import grondag.exotic_matter.world.IExtraStateFactory;
import grondag.exotic_matter.world.Rotation;
import grondag.exotic_matter.world.SimpleJoin;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelStateData
{
    /** note that sign bit on core packer is reserved to persist static state during serialization */ 
    public static final BitPacker64<ModelState> PACKER_CORE = new BitPacker64<ModelState>(m-> m.coreBits, (m, b) -> m.coreBits = b);
    public static final BitPacker64<ModelState>.IntElement SHAPE = PACKER_CORE.createIntElement(ModelShapes.MAX_SHAPES);
    public static final BitPacker64<ModelState>.IntElement POS_X = PACKER_CORE.createIntElement(256);
    public static final BitPacker64<ModelState>.IntElement POS_Y = PACKER_CORE.createIntElement(256);
    public static final BitPacker64<ModelState>.IntElement POS_Z = PACKER_CORE.createIntElement(256);
    public static final BitPacker64<ModelState>.EnumElement<EnumFacing.Axis> AXIS = PACKER_CORE.createEnumElement(EnumFacing.Axis.class);
    public static final BitPacker64<ModelState>.BooleanElement AXIS_INVERTED = PACKER_CORE.createBooleanElement();
    public static final BitPacker64<ModelState>.EnumElement<Rotation> AXIS_ROTATION = PACKER_CORE.createEnumElement(Rotation.class);
    
    
    public static final BitPacker64<ModelState> PACKER_LAYER_BASE = new BitPacker64<ModelState>(m-> m.layerBitsBase, (m, b) -> m.layerBitsBase = b);
    public static final BitPacker64<ModelState> PACKER_LAYER_LAMP = new BitPacker64<ModelState>(m-> m.layerBitsLamp, (m, b) -> m.layerBitsLamp = b);
    public static final BitPacker64<ModelState> PACKER_LAYER_MIDDLE = new BitPacker64<ModelState>(m-> m.layerBitsMiddle, (m, b) -> m.layerBitsMiddle = b);
    public static final BitPacker64<ModelState> PACKER_LAYER_OUTER = new BitPacker64<ModelState>(m-> m.layerBitsOuter, (m, b) -> m.layerBitsOuter = b);
    public static final BitPacker64<ModelState> PACKER_LAYER_CUT = new BitPacker64<ModelState>(m-> m.layerBitsCut, (m, b) -> m.layerBitsCut = b);
    
    @SuppressWarnings("unchecked")
    public static final BitPacker64<ModelState>[] PACKER_LAYERS = (BitPacker64<ModelState>[]) new BitPacker64<?>[PaintLayer.SIZE];
    
    @SuppressWarnings("unchecked")
    public static final BitPacker64<ModelState>.IntElement[] PAINT_COLOR = (BitPacker64<ModelState>.IntElement[]) new BitPacker64<?>.IntElement[PaintLayer.SIZE];
    @SuppressWarnings("unchecked")
    public static final BitPacker64<ModelState>.BooleanElement[] PAINT_IS_TRANSLUCENT = (BitPacker64<ModelState>.BooleanElement[]) new BitPacker64<?>.BooleanElement[PaintLayer.SIZE];
    @SuppressWarnings("unchecked")
    public static final BitPacker64<ModelState>.IntElement[] PAINT_ALPHA = (BitPacker64<ModelState>.IntElement[]) new BitPacker64<?>.IntElement[PaintLayer.SIZE];
    @SuppressWarnings("unchecked")
    public static final BitPacker64<ModelState>.IntElement[] PAINT_TEXTURE = (BitPacker64<ModelState>.IntElement[]) new BitPacker64<?>.IntElement[PaintLayer.SIZE];
    @SuppressWarnings("unchecked")
    public static final BitPacker64<ModelState>.BooleanElement[] PAINT_EMISSIVE = (BitPacker64<ModelState>.BooleanElement[]) new BitPacker64<?>.BooleanElement[PaintLayer.SIZE];
    @SuppressWarnings("unchecked")
    public static final BitPacker64<ModelState>.IntElement[] PAINT_VERTEX_PROCESSOR = (BitPacker64<ModelState>.IntElement[]) new BitPacker64<?>.IntElement[PaintLayer.SIZE];

    public static final BitPacker64<ModelState> PACKER_SHAPE_BLOCK = new BitPacker64<ModelState>(m-> m.shapeBits0, (m, b) -> m.shapeBits0 = b);
    public static final BitPacker64<ModelState>.IntElement SPECIES = PACKER_SHAPE_BLOCK.createIntElement(16);
    public static final BitPacker64<ModelState>.IntElement BLOCK_JOIN = PACKER_SHAPE_BLOCK.createIntElement(CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT);
    public static final BitPacker64<ModelState>.IntElement MASONRY_JOIN = PACKER_SHAPE_BLOCK.createIntElement(SimpleJoin.STATE_COUNT);

    public static final BitPacker64<ModelState> PACKER_SHAPE_FLOW = new BitPacker64<ModelState>(m-> m.shapeBits0, (m, b) -> m.shapeBits0 = b);
    public static final BitPacker64<ModelState>.LongElement FLOW_JOIN = PACKER_SHAPE_FLOW.createLongElement(TerrainState.STATE_BIT_MASK + 1);

    public static final BitPacker64<ModelState> PACKER_SHAPE_MULTIBLOCK = new BitPacker64<ModelState>(m-> m.shapeBits0, (m, b) -> m.shapeBits0 = b);

    public static final BitPacker64<ModelState> PACKER_SHAPE_EXTRA = new BitPacker64<ModelState>(m-> m.shapeBits1, (m, b) -> m.shapeBits1 = b);
    /** value semantics are owned by consumer - only constraints are size (39 bits) and does not update from world */
    public static final BitPacker64<ModelState>.LongElement EXTRA_SHAPE_BITS = PACKER_SHAPE_EXTRA.createLongElement(1L << 39);
    
    /** used to compare states quickly for border joins  */
    public static final long SHAPE_COMPARISON_MASK_0;
    public static final long SHAPE_COMPARISON_MASK_1;   
    
    static
    {
        PACKER_LAYERS[PaintLayer.BASE.ordinal()] = PACKER_LAYER_BASE;
        PACKER_LAYERS[PaintLayer.LAMP.ordinal()] = PACKER_LAYER_LAMP;
        PACKER_LAYERS[PaintLayer.CUT.ordinal()] = PACKER_LAYER_CUT;
        PACKER_LAYERS[PaintLayer.MIDDLE.ordinal()] = PACKER_LAYER_MIDDLE;
        PACKER_LAYERS[PaintLayer.OUTER.ordinal()] = PACKER_LAYER_OUTER;
        
        for(PaintLayer l : PaintLayer.values())
        {
            final int i = l.ordinal();
            // important these two come first to allow for easy default values
            PAINT_COLOR[i] = PACKER_LAYERS[i].createIntElement(0x1000000); 
            PAINT_ALPHA[i] = PACKER_LAYERS[i].createIntElement(0x100);
            PAINT_TEXTURE[i] = PACKER_LAYERS[i].createIntElement(TexturePaletteRegistry.MAX_PALETTES);
            PAINT_IS_TRANSLUCENT[i] = PACKER_LAYERS[i].createBooleanElement();
            PAINT_EMISSIVE[i] = PACKER_LAYERS[i].createBooleanElement(); 
            PAINT_VERTEX_PROCESSOR[i] = PACKER_LAYERS[i].createIntElement(VertexProcessors.MAX_PROCESSORS); 
        }
        
        SHAPE_COMPARISON_MASK_0 = SHAPE.comparisonMask() 
                | AXIS.comparisonMask()
                | AXIS_INVERTED.comparisonMask();

        SHAPE_COMPARISON_MASK_1 = EXTRA_SHAPE_BITS.comparisonMask();
    }

    /**
     * Use this as factory for model state block tests that DON'T need to refresh from world.
     */
    public static final IExtraStateFactory TEST_GETTER_STATIC = new IExtraStateFactory()
    {
        @Override
        public @Nullable ISuperModelState get(IBlockAccess worldIn, BlockPos pos, IBlockState state)
        {
            Block block = state.getBlock();
            return (block instanceof ISuperBlock) 
                    ? SuperBlockWorldAccess.access(worldIn).getModelState((ISuperBlock)block, state, pos, false)
                    : null;
        }
    };
    
    /**
     * Use this as factory for model state block tests that DO need to refresh from world.
     */
    public static final IExtraStateFactory TEST_GETTER_DYNAMIC = new IExtraStateFactory()
    {
        @Override
        public @Nullable ISuperModelState get(IBlockAccess worldIn, BlockPos pos, IBlockState state)
        {
            Block block = state.getBlock();
            return (block instanceof ISuperBlock) 
                    ? SuperBlockWorldAccess.access(worldIn).getModelState(((ISuperBlock)block), state, pos, true)
                    : null;
        }
    };
    
    public static final BitPacker64<ModelState> STATE_PACKER = new BitPacker64<ModelState>(m -> m.stateFlags, (m, b) -> m.stateFlags = (int)b);
    
    /**
     * For readability.
     */
    public static final int STATE_FLAG_NONE = 0;

    
    /** see {@link #STATE_FLAG_IS_POPULATED} */
    public static final BitPacker64<ModelState>.BooleanElement STATE_BIT_IS_POPULATED = STATE_PACKER.createBooleanElement();
    /* 
     * Enables lazy derivation - set after derivation is complete.
     * NB - check logic assumes that ALL bits are zero for simplicity.
     */
    public static final int STATE_FLAG_IS_POPULATED = (int) STATE_BIT_IS_POPULATED.comparisonMask();

    
    /** see {@link #STATE_FLAG_NEEDS_CORNER_JOIN} */
    public static final BitPacker64<ModelState>.BooleanElement STATE_BIT_NEEDS_CORNER_JOIN = STATE_PACKER.createBooleanElement();
    /** 
     * Applies to block-type states.  
     * True if is a block type state and requires full join state.
     */
    public static final int STATE_FLAG_NEEDS_CORNER_JOIN = (int) STATE_BIT_NEEDS_CORNER_JOIN.comparisonMask();

    
    /** see {@link #STATE_FLAG_NEEDS_SIMPLE_JOIN} */
    public static final BitPacker64<ModelState>.BooleanElement STATE_BIT_NEEDS_SIMPLE_JOIN = STATE_PACKER.createBooleanElement();
    /** 
     * Applies to block-type states.  
     * True if is a block type state and requires full join state.
     */
    public static final int STATE_FLAG_NEEDS_SIMPLE_JOIN = (int) STATE_BIT_NEEDS_SIMPLE_JOIN.comparisonMask();

    
    /** see {@link #STATE_FLAG_NEEDS_MASONRY_JOIN} */
    public static final BitPacker64<ModelState>.BooleanElement STATE_BIT_NEEDS_MASONRY_JOIN = STATE_PACKER.createBooleanElement();
    
    /** 
     * Applies to block-type states.  
     * True if is a block type state and requires masonry join info.
     */
    public static final int STATE_FLAG_NEEDS_MASONRY_JOIN = (int) STATE_BIT_NEEDS_MASONRY_JOIN.comparisonMask();


    /** see {@link #STATE_FLAG_NEEDS_POS} */
    public static final BitPacker64<ModelState>.BooleanElement STATE_BIT_NEEDS_POS = STATE_PACKER.createBooleanElement();
    /** 
     * True if position (big-tex) world state is needed. Applies for block and flow state formats.
     */
    public static final int STATE_FLAG_NEEDS_POS = (int) STATE_BIT_NEEDS_POS.comparisonMask();

    
    /** see {@link #STATE_FLAG_NEEDS_SPECIES} */
    public static final BitPacker64<ModelState>.BooleanElement STATE_BIT_NEEDS_SPECIES = STATE_PACKER.createBooleanElement();
    public static final int STATE_FLAG_NEEDS_SPECIES = (int) STATE_BIT_NEEDS_SPECIES.comparisonMask();

    
    /** see {@link #STATE_FLAG_HAS_AXIS} */
    public static final BitPacker64<ModelState>.BooleanElement STATE_BIT_HAS_AXIS = STATE_PACKER.createBooleanElement();
    public static final int STATE_FLAG_HAS_AXIS = (int) STATE_BIT_HAS_AXIS.comparisonMask();

    
    /** see {@link #STATE_FLAG_NEEDS_TEXTURE_ROTATION} */
    public static final BitPacker64<ModelState>.BooleanElement STATE_BIT_NEEDS_TEXTURE_ROTATION = STATE_PACKER.createBooleanElement();
    public static final int STATE_FLAG_NEEDS_TEXTURE_ROTATION = (int) STATE_BIT_NEEDS_TEXTURE_ROTATION.comparisonMask();

    
    /** see {@link #STATE_FLAG_HAS_AXIS_ORIENTATION} */
    public static final BitPacker64<ModelState>.BooleanElement STATE_BIT_HAS_AXIS_ORIENTATION = STATE_PACKER.createBooleanElement();
    public static final int STATE_FLAG_HAS_AXIS_ORIENTATION = (int) STATE_BIT_HAS_AXIS_ORIENTATION.comparisonMask();

    /** see {@link #STATE_FLAG_HAS_AXIS_ROTATION} */
    public static final BitPacker64<ModelState>.BooleanElement STATE_BIT_HAS_AXIS_ROTATION = STATE_PACKER.createBooleanElement();
    /** Set if shape can be rotated around an axis. Only applies to block models; multiblock models manage this situationally. */
    public static final int STATE_FLAG_HAS_AXIS_ROTATION = (int) STATE_BIT_HAS_AXIS_ROTATION.comparisonMask();


    /** see {@link #STATE_FLAG_HAS_TRANSLUCENT_GEOMETRY} */
    public static final BitPacker64<ModelState>.BooleanElement STATE_BIT_HAS_TRANSLUCENT_GEOMETRY = STATE_PACKER.createBooleanElement();
    /** Set if either Base/Cut or Lamp (if present) paint layers are translucent */
    public static final int STATE_FLAG_HAS_TRANSLUCENT_GEOMETRY = (int) STATE_BIT_HAS_TRANSLUCENT_GEOMETRY.comparisonMask();
    
    /** see {@link #STATE_FLAG_HAS_SOLID_RENDER} */
    public static final BitPacker64<ModelState>.BooleanElement STATE_BIT_HAS_SOLID_RENDER = STATE_PACKER.createBooleanElement();
    /** True if any layer sould render in the solid block render layer */
    public static final int STATE_FLAG_HAS_SOLID_RENDER = (int) STATE_BIT_HAS_SOLID_RENDER.comparisonMask();
    
    /** see {@link #STATE_FLAG_HAS_TRANSLUCENT_RENDER} */
    public static final BitPacker64<ModelState>.BooleanElement STATE_BIT_HAS_TRANSLUCENT_RENDER = STATE_PACKER.createBooleanElement();
    /** True if any layer should render in the translucent block render layer */
    public static final int STATE_FLAG_HAS_TRANSLUCENT_RENDER = (int) STATE_BIT_HAS_TRANSLUCENT_RENDER.comparisonMask();
    
    /** use this to turn off flags that should not be used with non-block state formats */
    public static final int STATE_FLAG_DISABLE_BLOCK_ONLY = ~(
            STATE_FLAG_NEEDS_CORNER_JOIN | STATE_FLAG_NEEDS_SIMPLE_JOIN | STATE_FLAG_NEEDS_MASONRY_JOIN
            | STATE_FLAG_NEEDS_SPECIES | STATE_FLAG_NEEDS_TEXTURE_ROTATION);
    
    //hide constructor
    private ModelStateData()
    {
        super();
    }
}
