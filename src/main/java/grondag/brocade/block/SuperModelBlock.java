package grondag.brocade.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.exotic_matter.ExoticMatter;
import grondag.exotic_matter.init.ModShapes;
import grondag.exotic_matter.model.render.RenderLayout;
import grondag.exotic_matter.model.render.RenderLayoutProducer;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.model.state.ModelState;
import grondag.exotic_matter.model.varia.WorldLightOpacity;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.event.RegistryEvent.Register;

/**
 * User-configurable HardScience building blocks.<br>
 * <br>
 * 
 * While most attributes are stored in stack/tile entity NBT some important
 * methods are called without reference to a location or stack. For these, we
 * have multiple instances of this block and use a different instance depending
 * on the combination of attributes needed.<br>
 * <br>
 * 
 * The choice of which block to deploy is made by the item/creative stack that
 * places the block by calling
 * {@link grondag.hard_science.init.ModSuperModelBlocks#findAppropriateSuperModelBlock(BlockSubstance substance, ISuperModelState modelState)}
 * <br>
 * <br>
 * 
 * The specific dimensions by which the block instances vary are:
 * {@link #getRenderModeSet()}, {@link #worldLightOpacity}, Block.fullBlock and
 * {@link #isHypermatter()}.
 * 
 *
 */
public class SuperModelBlock extends SuperBlockPlus {
    private static final ModelState DEFAULT_MODEL_STATE;

    static {
        DEFAULT_MODEL_STATE = new ModelState();
        DEFAULT_MODEL_STATE.setShape(ModShapes.CUBE);
    }
    /**
     * Harvest tool for this block based on block substance. Set during
     * getActualState so that harvest/tool methods can have access to
     * location-dependent substance information.
     */
    public static final PropertyEnum<BlockHarvestTool> HARVEST_TOOL = PropertyEnum.create("harvest_tool",
            BlockHarvestTool.class);

    /**
     * Higher than vanilla to allow for modded hardnesses. Value is inclusive.
     */
    public static final int MAX_HARVEST_LEVEL = 7;
    /**
     * Harvest tool for this block based on block substance. Set during
     * getActualState so that harvest/tool methods can have access to
     * location-dependent substance information.
     */
    public static final PropertyInteger HARVEST_LEVEL = PropertyInteger.create("harvest_level", 0, MAX_HARVEST_LEVEL);

    /**
     * dimensions are BlockRenderMode, worldOpacity, hypermatter (y = 1 /n = 0),
     * cube (y = 1 /n = 0)
     */
    public static final SuperModelBlock[][][][] superModelBlocks = new SuperModelBlock[RenderLayoutProducer.VALUE_COUNT][WorldLightOpacity
            .values().length][2][2];

    public static SuperModelBlock findAppropriateSuperModelBlock(BlockSubstance substance,
            ISuperModelState modelState) {
        WorldLightOpacity opacity = WorldLightOpacity.getClosest(substance, modelState);
        RenderLayoutProducer renderLayout = modelState.getRenderLayoutProducer();
        int hypermaterIndex = substance.isHyperMaterial ? 1 : 0;
        int cubeIndex = modelState.isCube() ? 1 : 0;
        return superModelBlocks[renderLayout.ordinal][opacity.ordinal()][hypermaterIndex][cubeIndex];
    }

    public static void registerSuperModelBlocks(Register<Block> event) {
        int superModelIndex = 0;

        for (RenderLayoutProducer blockRenderMode : RenderLayoutProducer.VALUES) {
            for (WorldLightOpacity opacity : WorldLightOpacity.values()) {
                // mundane non-cube
                SuperModelBlock.superModelBlocks[blockRenderMode.ordinal][opacity
                        .ordinal()][0][0] = (SuperModelBlock) new SuperModelBlock("supermodel" + superModelIndex++,
                                Material.ROCK, blockRenderMode, opacity, false, false)
                                        .setTranslationKey("super_model_block").setCreativeTab(ExoticMatter.tabMod); // all
                                                                                                                     // superblocks
                                                                                                                     // have
                                                                                                                     // same
                                                                                                                     // display
                                                                                                                     // name
                event.getRegistry()
                        .register(SuperModelBlock.superModelBlocks[blockRenderMode.ordinal][opacity.ordinal()][0][0]);

                // mundane cube
                SuperModelBlock.superModelBlocks[blockRenderMode.ordinal][opacity
                        .ordinal()][0][1] = (SuperModelBlock) new SuperModelBlock("supermodel" + superModelIndex++,
                                Material.ROCK, blockRenderMode, opacity, false, true)
                                        .setTranslationKey("super_model_block").setCreativeTab(ExoticMatter.tabMod); // all
                                                                                                                     // superblocks
                                                                                                                     // have
                                                                                                                     // same
                                                                                                                     // display
                                                                                                                     // name
                event.getRegistry()
                        .register(SuperModelBlock.superModelBlocks[blockRenderMode.ordinal][opacity.ordinal()][0][1]);

                // hypermatter non-cube
                SuperModelBlock.superModelBlocks[blockRenderMode.ordinal][opacity
                        .ordinal()][1][0] = (SuperModelBlock) new SuperModelBlock("supermodel" + superModelIndex++,
                                Material.ROCK, blockRenderMode, opacity, true, false)
                                        .setTranslationKey("super_model_block").setCreativeTab(ExoticMatter.tabMod); // all
                                                                                                                     // superblocks
                                                                                                                     // have
                                                                                                                     // same
                                                                                                                     // display
                                                                                                                     // name
                event.getRegistry()
                        .register(SuperModelBlock.superModelBlocks[blockRenderMode.ordinal][opacity.ordinal()][1][0]);

                // hypermatter cube
                SuperModelBlock.superModelBlocks[blockRenderMode.ordinal][opacity
                        .ordinal()][1][1] = (SuperModelBlock) new SuperModelBlock("supermodel" + superModelIndex++,
                                Material.ROCK, blockRenderMode, opacity, true, true)
                                        .setTranslationKey("super_model_block").setCreativeTab(ExoticMatter.tabMod); // all
                                                                                                                     // superblocks
                                                                                                                     // have
                                                                                                                     // same
                                                                                                                     // display
                                                                                                                     // name
                event.getRegistry()
                        .register(SuperModelBlock.superModelBlocks[blockRenderMode.ordinal][opacity.ordinal()][1][1]);

            }
        }
    }

    protected final WorldLightOpacity worldLightOpacity;

    protected final boolean isHyperMatter;

    /**
     * 
     * @param blockName
     * @param defaultMaterial
     * @param defaultModelState  Controls render layer visibility for this instance.
     * @param worldLightOpacity
     * @param isHyperMatter
     * @param isGeometryFullCube If true, blocks with this instance are expected to
     *                           have a full block geometry
     */
    public SuperModelBlock(String blockName, Material defaultMaterial, RenderLayoutProducer renderLayout,
            WorldLightOpacity worldLightOpacity, boolean isHyperMatter, boolean isGeometryFullCube) {
        super(blockName, defaultMaterial, DEFAULT_MODEL_STATE.clone(), renderLayout);
        this.isHyperMatter = isHyperMatter;
        this.fullBlock = isGeometryFullCube;
        this.worldLightOpacity = worldLightOpacity;
        this.lightOpacity = worldLightOpacity.opacity;
    }

    @Override
    public @Nullable TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
        return new SuperModelTileEntity();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[] { META, HARVEST_TOOL, HARVEST_LEVEL },
                new IUnlistedProperty[] { MODEL_STATE });
    }

    @Override
    public int damageDropped(@Nonnull IBlockState state) {
        // don't want species to "stick" with SuperModelblocks - so they can restack
        // species will be set again on placement anyway
        return 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getActualState(@Nonnull IBlockState state, @Nonnull IBlockAccess worldIn,
            @Nonnull BlockPos pos) {
        BlockSubstance substance = this.getSubstance(state, worldIn, pos);
        // Add substance for tool methods
        return super.getActualState(state, worldIn, pos).withProperty(HARVEST_TOOL, substance.harvestTool)
                .withProperty(HARVEST_LEVEL, substance.harvestLevel);
    }

    /**
     * {@inheritDoc} <br>
     * <br>
     * relying on {@link #getActualState(IBlockState, IBlockAccess, BlockPos)} to
     * set {@link #SUBSTANCE} property
     */
    @Override
    public int getHarvestLevel(@Nonnull IBlockState state) {
        Integer l = state.getValue(HARVEST_LEVEL);
        return l == null ? 0 : l;
    }

    /**
     * {@inheritDoc} <br>
     * <br>
     * relying on {@link #getActualState(IBlockState, IBlockAccess, BlockPos)} to
     * set {@link #SUBSTANCE} property
     */
    @Override
    @Nullable
    public String getHarvestTool(@Nonnull IBlockState state) {
        BlockHarvestTool tool = state.getValue(HARVEST_TOOL);
        return tool == null ? null : tool.toolString;
    }

    @Override
    public float getBlockHardness(@Nonnull IBlockState blockState, @Nonnull World worldIn, @Nonnull BlockPos pos) {
        return this.getSubstance(blockState, worldIn, pos).hardness;
    }

    @Override
    public float getExplosionResistance(@Nonnull World world, @Nonnull BlockPos pos, @Nullable Entity exploder,
            @Nonnull Explosion explosion) {
        return this.getSubstance(world, pos).resistance;
    }

    @Override
    public SoundType getSoundType(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos,
            @Nullable Entity entity) {
        return this.getSubstance(state, world, pos).soundType;
    }

    /**
     * SuperModel blocks light emission level is stored in tile entity. Is not part
     * of model state because does not affect rendering. However,
     * {@link #getLightValue(IBlockState)} will return 0. That version is not used
     * in vanilla forge except to determine if flat render pipeline should be used
     * for emissive blocks. Should not be a problem because render logic also checks
     * isAmbientOcclusion() on the baked model itself.
     * 
     * 
     * FIXME: in latest Forge, block renderer now checks the location-aware version
     * of getLightValue which means it will use flat lighter even when we don't want
     * it to. So we'll need to force this to zero depending on render layer. OTOH -
     * if the block actually does emit light, maybe flat lighter is OK.
     * 
     */
    @Override
    public int getLightValue(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        TileEntity myTE = world.getTileEntity(pos);
        return myTE == null || !(myTE instanceof SuperModelTileEntity) ? 0
                : ((SuperModelTileEntity) myTE).getLightValue();
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        // We only want to show one item for supermodelblocks
        // Otherwise will spam creative search / JEI
        // All do the same thing in the end.
        if (this.worldLightOpacity == WorldLightOpacity.SOLID && this.fullBlock && !this.isHyperMatter
                && this.renderLayout() == RenderLayout.SOLID_ONLY) {
            list.add(this.getSubItems().get(0));
        }
    }

    @Override
    public BlockSubstance getSubstance(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity myTE = world.getTileEntity(pos);
        return myTE == null || !(myTE instanceof SuperModelTileEntity) ? BlockSubstance.DEFAULT
                : ((SuperModelTileEntity) myTE).getSubstance();
    }

    @Override
    public boolean isGeometryFullCube(IBlockState state) {
        return this.fullBlock;
    }

    @Override
    public boolean isHypermatter() {
        return this.isHyperMatter;
    }

    /**
     * Set light level emitted by block. Inputs are masked to 0-15
     */
    public void setLightValue(IBlockState state, IBlockAccess world, BlockPos pos, int lightValue) {
        TileEntity myTE = world.getTileEntity(pos);
        if (myTE != null && myTE instanceof SuperModelTileEntity)
            ((SuperModelTileEntity) myTE).setLightValue((byte) (lightValue & 0xF));
    }

    public void setSubstance(IBlockState state, IBlockAccess world, BlockPos pos, BlockSubstance substance) {
        TileEntity myTE = world.getTileEntity(pos);
        if (myTE != null && myTE instanceof SuperModelTileEntity)
            ((SuperModelTileEntity) myTE).setSubstance(substance);
    }

    @Override
    protected WorldLightOpacity worldLightOpacity(IBlockState state) {
        return this.worldLightOpacity;
    }
}
