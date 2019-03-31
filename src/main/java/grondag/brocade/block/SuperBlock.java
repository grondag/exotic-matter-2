package grondag.brocade.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;




import com.google.common.collect.ImmutableList;

import grondag.brocade.BrocadeConfig;
import grondag.brocade.BrocadeConfig.BlockSettings.ProbeInfoLevel;
import grondag.brocade.painting.PaintLayer;
import grondag.brocade.model.render.RenderLayout;
import grondag.brocade.model.render.RenderLayoutProducer;
import grondag.brocade.model.state.ISuperModelState;
import grondag.brocade.model.state.MetaUsage;
import grondag.brocade.model.state.ModelState;
import grondag.brocade.collision.ICollisionHandler;
import grondag.brocade.model.varia.ParticleDiggingSuperBlock;
import grondag.brocade.model.varia.SideShape;
import grondag.brocade.model.varia.SuperDispatcher;
import grondag.brocade.model.varia.WorldLightOpacity;
import grondag.exotic_matter.placement.SuperItemBlock;
import grondag.fermion.varia.Color;
import grondag.fermion.varia.Color.EnumHCLFailureMode;
import grondag.fermion.varia.SuperBlockBorderMatch;
import grondag.fermion.world.IBlockTest;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoAccessor;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.Direction;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BoundingBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.Optional;


import net.minecraftforge.registries.IForgeRegistry;

/**
 * Base class for HardScience building blocks.
 */

@Optional.InterfaceList({
        @Optional.Interface(iface = "mcjty.theoneprobe.api.IProbeInfoAccessor", modid = "theoneprobe") })
public abstract class SuperBlock extends Block implements IProbeInfoAccessor, ISuperBlock {

    /** non-null if this drops something other than itself */
    private Item dropItem;

    /** Allow silk harvest. Defaults true. Use setAllowSilk to change */
    private boolean allowSilkHarvest = true;

    /** change in constructor to have different appearance */
    protected int[] defaultModelStateBits;

    /** change in constructor to have fewer variants */
    protected int metaCount = 16;

    /** see {@link #isAssociatedBlock(Block)} */
    protected Block associatedBlock;

    private final RenderLayoutProducer renderLayoutProducer;

    /**
     * Sub-items for the block. Initialized in {@link #createSubItems()}
     */
    private List<ItemStack> subItems;

    public SuperBlock(String blockName, Material defaultMaterial, ISuperModelState defaultModelState,
            RenderLayoutProducer renderLayout) {
        super(defaultMaterial);

        // these values are fail-safes - should never be used normally
        this.setHarvestLevel("pickaxe", 1);
        setSoundType(SoundType.STONE);
        setHardness(2);
        setResistance(50);

        this.setRegistryName(blockName);
        this.setTranslationKey(blockName);
        this.associatedBlock = this;

        this.lightOpacity = 0;

        this.defaultModelStateBits = defaultModelState.serializeToInts();

        this.renderLayoutProducer = renderLayout == null ? defaultModelState.getRenderLayoutProducer() : renderLayout;
    }

    /**
     * Factory for block test that should be used for border/shape joins for this
     * block. Used in model state refresh from world.
     */
    @Override
    public IBlockTest blockJoinTest(IBlockAccess worldIn, BlockState state, BlockPos pos,
            ISuperModelState modelState) {
        return new SuperBlockBorderMatch(this, modelState, true);
    }

    @Override
    public void addCollisionBoxToList(BlockState state, World worldIn, BlockPos pos,
            BoundingBox entityBox, List<BoundingBox> collidingBoxes, Entity entityIn,
            boolean p_185477_7_) {
        ISuperModelState modelState = SuperBlockWorldAccess.access(worldIn).getModelState(this, pos, true);
        ICollisionHandler collisionHandler = modelState.getShape().meshFactory().collisionHandler();

        BoundingBox localMask = entityBox.offset(-pos.getX(), -pos.getY(), -pos.getZ());

        List<BoundingBox> bounds = collisionHandler.getCollisionBoxes(modelState);

        for (BoundingBox aabb : bounds) {
            if (localMask.intersects(aabb)) {
                collidingBoxes.add(aabb.offset(pos.getX(), pos.getY(), pos.getZ()));
            }
        }

    }

    @Override
    
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
        BlockState blockState = world.getBlockState(pos);
        if (blockState.getBlock() != this) {
            // somehow got called for a different block
            if (blockState.getBlock() instanceof SuperBlock) {
                // if the block at given position is somehow also a SuperBlock, call particle
                // handler for it
                ((blockState.getBlock())).addDestroyEffects(world, pos, manager);
            } else {
                // handle as a vanilla block
                return false;
            }
        }

        ISuperModelState modelState = SuperBlockWorldAccess.access(world).getModelState(this, pos, false);

        for (int j = 0; j < 4; ++j) {
            for (int k = 0; k < 4; ++k) {
                for (int l = 0; l < 4; ++l) {
                    double d0 = ((double) j + 0.5D) / 4.0D;
                    double d1 = ((double) k + 0.5D) / 4.0D;
                    double d2 = ((double) l + 0.5D) / 4.0D;
                    manager.addEffect(
                            (new ParticleDiggingSuperBlock(world, (double) pos.getX() + d0, (double) pos.getY() + d1,
                                    (double) pos.getZ() + d2, d0 - 0.5D, d1 - 0.5D, d2 - 0.5D, blockState, modelState))
                                            .setBlockPos(pos));
                }
            }
        }

        return true;
    }

    @Override
    
    public boolean addHitEffects(BlockState blockState, World world, RayTraceResult target,
            ParticleManager manager) {
        if (blockState.getBlock() != this) {
            // somehow got called for a different block
            if (blockState.getBlock() instanceof SuperBlock) {
                // if the block at given position is somehow also a SuperBlock, call particle
                // handler for it
                ((blockState.getBlock())).addHitEffects(blockState, world, target, manager);
            } else {
                // handle as a vanilla block
                return false;
            }
        }

        BlockPos pos = target.getBlockPos();
        ISuperModelState modelState = SuperBlockWorldAccess.access(world).getModelState(this, pos, false);

        Direction side = target.sideHit;

        if (blockState.getRenderType() != EnumBlockRenderType.INVISIBLE) {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();

            BoundingBox BoundingBox = blockState.getBoundingBox(world, pos);
            double d0 = (double) i
                    + ThreadLocalRandom.current().nextDouble()
                            * (BoundingBox.maxX - BoundingBox.minX - 0.20000000298023224D)
                    + 0.10000000149011612D + BoundingBox.minX;
            double d1 = (double) j
                    + ThreadLocalRandom.current().nextDouble()
                            * (BoundingBox.maxY - BoundingBox.minY - 0.20000000298023224D)
                    + 0.10000000149011612D + BoundingBox.minY;
            double d2 = (double) k
                    + ThreadLocalRandom.current().nextDouble()
                            * (BoundingBox.maxZ - BoundingBox.minZ - 0.20000000298023224D)
                    + 0.10000000149011612D + BoundingBox.minZ;

            if (side == Direction.DOWN) {
                d1 = (double) j + BoundingBox.minY - 0.10000000149011612D;
            }

            if (side == Direction.UP) {
                d1 = (double) j + BoundingBox.maxY + 0.10000000149011612D;
            }

            if (side == Direction.NORTH) {
                d2 = (double) k + BoundingBox.minZ - 0.10000000149011612D;
            }

            if (side == Direction.SOUTH) {
                d2 = (double) k + BoundingBox.maxZ + 0.10000000149011612D;
            }

            if (side == Direction.WEST) {
                d0 = (double) i + BoundingBox.minX - 0.10000000149011612D;
            }

            if (side == Direction.EAST) {
                d0 = (double) i + BoundingBox.maxX + 0.10000000149011612D;
            }

            manager.addEffect(
                    (new ParticleDiggingSuperBlock(world, d0, d1, d2, 0.0D, 0.0D, 0.0D, blockState, modelState))
                            .setBlockPos(pos).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
        }

        return true;
    }

    @Override
    
    public void addInformation(ItemStack stack, World world, List<String> tooltip,
            ITooltipFlag advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.translate("label.meta") + ": " + stack.getMetadata());

        ISuperModelState modelState = SuperBlockStackHelper.getStackModelState(stack);

        if (modelState != null) {
            tooltip.add(I18n.translate("label.shape") + ": " + modelState.getShape().localizedName());
            tooltip.add(I18n.translate("label.base_color") + ": "
                    + Integer.toHexString(modelState.getColorARGB(PaintLayer.BASE)));
            tooltip.add(I18n.translate("label.base_texture") + ": "
                    + modelState.getTexture(PaintLayer.BASE).displayName());
            if (modelState.isLayerEnabled(PaintLayer.OUTER)) {
                tooltip.add(I18n.translate("label.outer_color") + ": "
                        + Integer.toHexString(modelState.getColorARGB(PaintLayer.OUTER)));
                tooltip.add(I18n.translate("label.outer_texture") + ": "
                        + modelState.getTexture(PaintLayer.OUTER).displayName());
            }
            if (modelState.isLayerEnabled(PaintLayer.MIDDLE)) {
                tooltip.add(I18n.translate("label.middle_color") + ": "
                        + Integer.toHexString(modelState.getColorARGB(PaintLayer.MIDDLE)));
                tooltip.add(I18n.translate("label.middle_texture") + ": "
                        + modelState.getTexture(PaintLayer.MIDDLE).displayName());
            }
            if (modelState.hasSpecies()) {
                tooltip.add(I18n.translate("label.species") + ": " + modelState.getSpecies());
            }
        }
        tooltip.add(I18n.translate("label.material") + ": "
                + SuperBlockStackHelper.getStackSubstance(stack).localizedName());
    }

    @Override
    
    public boolean addLandingEffects(BlockState state, WorldServer worldObj,
            BlockPos blockPosition, BlockState BlockState, LivingEntity entity,
            int numberOfParticles) {
        // This is server-side, so to get matching particle color/texture I'd have to
        // create a custom particle type similar to ParticleBlockDust,
        // register it and add a handler for it. Light-colored quartz particles are fine
        // IMO - is just kicking up dust, not breaking anything.

        worldObj.spawnParticle(EnumParticleTypes.BLOCK_DUST, entity.posX, entity.posY, entity.posZ, numberOfParticles,
                0.0D, 0.0D, 0.0D, 0.15000000596046448D,
                new int[] { Block.getStateId(Blocks.QUARTZ_BLOCK.getDefaultState()) });
        return true;
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player,
            World world, BlockState blockState, IProbeHitData data) {
        if (blockState == null || world == null || data == null || probeInfo == null)
            return;

        ISuperModelState modelState = SuperBlockWorldAccess.access(world).computeModelState(this, blockState,
                data.getPos(), true);

        probeInfo.text(I18n.translate("label.shape") + ": " + modelState.getShape().localizedName());
        probeInfo.text(I18n.translate("label.base_color") + ": "
                + Integer.toHexString(modelState.getColorARGB(PaintLayer.BASE)));
        probeInfo.text(I18n.translate("label.base_texture") + ": "
                + modelState.getTexture(PaintLayer.BASE).displayName());
        if (modelState.isLayerEnabled(PaintLayer.OUTER)) {
            probeInfo.text(I18n.translate("label.outer_color") + ": "
                    + Integer.toHexString(modelState.getColorARGB(PaintLayer.OUTER)));
            probeInfo.text(I18n.translate("label.outer_texture") + ": "
                    + modelState.getTexture(PaintLayer.OUTER).displayName());
        }
        if (modelState.isLayerEnabled(PaintLayer.MIDDLE)) {
            probeInfo.text(I18n.translate("label.middle_color") + ": "
                    + Integer.toHexString(modelState.getColorARGB(PaintLayer.MIDDLE)));
            probeInfo.text(I18n.translate("label.middle_texture") + ": "
                    + modelState.getTexture(PaintLayer.MIDDLE).displayName());
        }
        if (modelState.hasSpecies()) {
            probeInfo.text(I18n.translate("label.species") + ": " + modelState.getSpecies());
        }

        if (BrocadeConfig.BLOCKS.probeInfoLevel != ProbeInfoLevel.BASIC) {
            if (modelState.hasAxis()) {
                probeInfo.text(I18n.translate("label.axis") + ": " + modelState.getAxis());
                if (modelState.hasAxisOrientation()) {
                    probeInfo.text(I18n.translate("label.axis_inverted") + ": " + modelState.isAxisInverted());
                }
            }
            if (modelState.hasAxisRotation()) {
                probeInfo.text(I18n.translate("label.model_rotation") + ": " + modelState.getAxisRotation());
            }
            probeInfo.text(I18n.translate("label.position") + ": " + modelState.getPosX() + ", "
                    + modelState.getPosY() + ", " + modelState.getPosZ());
        }

        probeInfo.text(I18n.translate("label.material") + ": "
                + this.getSubstance(blockState, world, data.getPos()).localizedName());

        if (BrocadeConfig.BLOCKS.probeInfoLevel == ProbeInfoLevel.DEBUG) {
            probeInfo.text(I18n.translate("label.meta") + ": " + blockState.getValue(ISuperBlock.META));

            probeInfo.text(I18n.translate("label.full_block") + ": " + this.fullBlock);
            probeInfo.text("isOpaqueCube(): " + this.isOpaqueCube(blockState));
            probeInfo.text("isFullCube(): " + this.isFullCube(blockState));
            probeInfo.text("getUseNB: " + this.getUseNeighborBrightness(blockState));
            probeInfo.text("getLightOpacity: " + this.getLightOpacity(blockState));
            probeInfo.text("getAmbientOcclusionLightValue: " + this.getAmbientOcclusionLightValue(blockState));
            probeInfo
                    .text("getPackedLightmapCoords: " + this.getPackedLightmapCoords(blockState, world, data.getPos()));

            BlockState upState = world.getBlockState(data.getPos().up());
            probeInfo.text("UP isFullBlock: " + upState.isFullBlock());
            probeInfo.text("UP isOpaqueCube(): " + upState.isOpaqueCube());
            probeInfo.text("UP isFullCube(): " + upState.isFullCube());
            probeInfo.text("UP getUseNB: " + upState.useNeighborBrightness());
            probeInfo.text("UP getLightOpacity: " + upState.getLightOpacity());
            probeInfo.text("UP getAmbientOcclusionLightValue: " + upState.getAmbientOcclusionLightValue());
            probeInfo.text("UP getPackedLightmapCoords: "
                    + this.getPackedLightmapCoords(blockState, world, data.getPos().up()));
        }
    }

    /**
     * {@inheritDoc} <br>
     * <br>
     * 
     * Added by forge to allow better control over fence/wall/pane connections.
     * SuperBlocks determine connectivity with each other through other means and
     * are not going to be compatible with regular fences, panes, etc. Always false
     * for that reason.
     */
    @Override
    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, Direction facing) {
        return false;
    }

    @Override
    public boolean canBeReplacedByLeaves(BlockState state, IBlockAccess world,
            BlockPos pos) {
        return false;
    }

//    /** Used by world ray tracing.  
//     * All superblocks are normally going to be collidable, so default implementation works.
//     */
//    @Override
//    public boolean canCollideCheck(BlockState state, boolean hitIfLiquid)
//    {
//        return super.canCollideCheck(state, hitIfLiquid);
//    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockAccess world, BlockPos pos,
            Direction side) {
        return false;
    }

    /**
     * {@inheritDoc} <br>
     * <br>
     * Mobs can't spawn on hypermatter.
     */
    @Override
    public boolean canCreatureSpawn(BlockState state, IBlockAccess world, BlockPos pos,
            SpawnPlacementType type) {
        return !this.isHypermatter() && super.canCreatureSpawn(state, world, pos, type);
    }

    /**
     * {@inheritDoc} <br>
     * <br>
     * Hypermatter is indestructable by normal explosions. If it does blow up
     * somehow it shouldn't drop as a block.
     */
    @Override
    public boolean canDropFromExplosion(Explosion explosionIn) {
        return !this.isHypermatter();
    }

    @Override
    public boolean canEntityDestroy(BlockState state, IBlockAccess world, BlockPos pos,
            Entity entity) {
        return super.canEntityDestroy(state, world, pos, entity);
    }

    /**
     * Accessed via state implementation. Used to determine if an entity can spawn
     * on this block. Has no actual or extended state properties when referenced.
     * 
     * All superblocks allow spawning unless made of hypermatter.
     */
    @Override
    public boolean canEntitySpawn(BlockState state, Entity entityIn) {
        return super.canEntitySpawn(state, entityIn) && BrocadeConfig.HYPERSTONE.allowMobSpawning || !this.isHypermatter();
    }

    @Override
    public boolean canPlaceTorchOnTop(BlockState state, IBlockAccess world, BlockPos pos) {
        return SuperBlockWorldAccess.access(world).computeModelState(this, state, pos, true)
                .sideShape(Direction.UP).holdsTorch;
    }

    /**
     * This is queried before getActualState, which means it cannot be determined
     * from world.
     * 
     * We could report that we render in all layers but return no quads. However,
     * this means RenderChunk does quite a bit of work asking us for stuff that
     * isn't there.
     * 
     * Instead we persist it in the block instance and set block states that point
     * to the appropriate block instance for the model they represent. This could
     * force some block state changes in the world however if model state changes -
     * but those changes are not likely. Main drawback of this approach is that it
     * consumes more block ids.
     * 
     * If any rendering is done by TESR, then don't render in any layer because too
     * hard to get render depth perfectly aligned that way. TESR will also render
     * normal block layers.
     */
    @Override
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        return this.renderLayoutProducer.renderLayout().containsBlockRenderLayer(layer);
    }

    @Override
    public boolean canSilkHarvest() {
        return allowSilkHarvest;
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, BlockState state,
            EntityPlayer player) {
        return this.canSilkHarvest();
    }

    @Override
    public RayTraceResult collisionRayTrace(BlockState blockState, World worldIn,
            BlockPos pos, Vec3d start, Vec3d end) {
        ArrayList<BoundingBox> bounds = new ArrayList<BoundingBox>();

        this.addCollisionBoxToList(blockState, worldIn, pos,
                new BoundingBox(start.x, start.y, start.z, end.x, end.y, end.z), bounds, null, false);

        RayTraceResult retval = null;
        double shortestDistance = 1;

        for (BoundingBox aabb : bounds) {
            RayTraceResult candidate = aabb.calculateIntercept(start, end);
            if (candidate != null) {
                double candidateDistance = candidate.hitVec.squareDistanceTo(start);
                if (retval == null || candidateDistance < shortestDistance) {
                    retval = candidate;
                    shortestDistance = candidateDistance;
                }
            }
        }

        return retval == null ? null : new RayTraceResult(retval.hitVec, retval.sideHit, pos);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[] { META }, new IUnlistedProperty[] { MODEL_STATE });
    }

    @Override
    public int damageDropped(BlockState state) {
        return state.getValue(META);
    }

    @Override
    public boolean doesSideBlockRendering(BlockState state, IBlockAccess world,
            BlockPos pos, Direction face) {
        ISuperModelState modelState = SuperBlockWorldAccess.access(world).getModelState(this, state, pos, true);
        return !modelState.hasTranslucentGeometry() && modelState.sideShape(face).occludesOpposite;
    }

    @Override
    public PathNodeType getAiPathNodeType(BlockState state, IBlockAccess world,
            BlockPos pos) {
        if (this.isBurning(world, pos))
            return PathNodeType.DAMAGE_FIRE;

        return this.getSubstance(state, world, pos).pathNodeType;
    }

    @Override
    public float[] getBeaconColorMultiplier(BlockState state, World world,
            BlockPos pos, BlockPos beaconPos) {
        if (this.getSubstance(state, world, pos).isTranslucent) {
            ISuperModelState modelState = SuperBlockWorldAccess.access(world).computeModelState(this, state, pos,
                    false);

            Color lamp = Color.fromRGB(modelState.getColorARGB(PaintLayer.BASE)).lumify();
            float saturation = modelState.getAlpha(PaintLayer.BASE) / 255f;
            Color beaconLamp = Color.fromHCL(lamp.HCL_H, lamp.HCL_C * saturation, Color.HCL_MAX,
                    EnumHCLFailureMode.REDUCE_CHROMA);
            int color = beaconLamp.RGB_int;

            float[] result = new float[3];
            result[0] = ((color >> 16) & 0xFF) / 255f;
            result[1] = ((color >> 8) & 0xFF) / 255f;
            result[2] = (color & 0xFF) / 255f;
            return result;
        }
        return null;
    }

    /**
     * Only meaningful use is for itemRenderer which checks this to know if it
     * should do depth checking on item renders. Get no state here, so always report
     * that we should.
     */
    @Override
    
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    /**
     * Used in many places and seems to provide min/max bounds for rendering
     * purposes. For example, seems to determine at what height rain falls. In most
     * cases is same as collision bounding box.
     */
    @Override
    public BoundingBox getBoundingBox(BlockState state, IBlockAccess worldIn,
            BlockPos pos) {
        return SuperBlockWorldAccess.access(worldIn).computeModelState(this, state, pos, true)
                .getCollisionBoundingBox();
    }

    @Override
    public BoundingBox getCollisionBoundingBox(BlockState state, IBlockAccess worldIn,
            BlockPos pos) {
        return SuperBlockWorldAccess.access(worldIn).computeModelState(this, state, pos, true)
                .getCollisionBoundingBox();
    }

    /**
     * Returns an instance of the default model state for this block. Because model
     * states are mutable, every call returns a new instance.
     */
    @Override
    public ISuperModelState getDefaultModelState() {
        return new ModelState(this.defaultModelStateBits);
    }

    /**
     * Main reason for override is that we have to add NBT to stack for ItemBlock
     * drops. Also don't use fortune for our drops.
     */
    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, BlockState state,
            int fortune) {
        Item dropItem = this.dropItem;

        if (dropItem == null) {
            ItemStack dropStack = getStackFromBlock(state, world, pos);
            if (dropStack.isEmpty())
                return ImmutableList.of();
            else
                return Collections.singletonList(dropStack);
        } else {
            int count = quantityDropped(world, pos, state);
            return Collections.singletonList(new ItemStack(dropItem, count, 0));
        }
    }

    /**
     * Determines which model should be displayed via MODEL_STATE.
     */
    @Override
    public BlockState getExtendedState(BlockState state, IBlockAccess world,
            BlockPos pos) {
        return ((IExtendedBlockState) state).withProperty(MODEL_STATE,
                SuperBlockWorldAccess.access(world).getModelState(this, state, pos, true));
    }

    /**
     * Would always return 0 anyway because we aren't in the list of encouragements
     * that the Fire block maintains.
     */
    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, Direction face) {
        return 0;
    }

    /** lowest-tier wood has a small chance of burning */
    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, Direction face) {
        return this.getSubstance(world, pos).flammability;
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, BlockState state) {
        // Do not trust the state passed in, because nobody should be calling this
        // method anyway.
        BlockState goodState = worldIn.getBlockState(pos);
        return getStackFromBlock(goodState, worldIn, pos);
    }

    /**
     * {@inheritDoc} <br>
     * <br>
     * 
     * DO NOT USE THIS FOR SUPERBLOCKS! Use
     * {@link #getStackFromBlock(BlockState, IBlockAccess, BlockPos)} instead.
     * 
     * Also, yes, I overrode this method just to add this warning.
     */
    @Override
    public Item getItemDropped(BlockState state, Random rand, int fortune) {
        return super.getItemDropped(state, rand, fortune);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return this.getLocalizedName();
    }

    /**
     * Used by chunk for world lighting and to determine height map. Blocks with 0
     * opacity are apparently ignored for height map generation.
     * 
     * 0 means fully transparent values 1-15 are various degrees of opacity 255
     * means fully opaque values 16-254 have no meaning
     * 
     * Chunk uses location-dependent version if the chunk is loaded.
     * 
     * We return a non-zero estimate here which forces this block to be considered
     * in sky/height maps. Actual light value will generally be obtained via the
     * location-dependent method.
     */
    @Override
    public int getLightOpacity(BlockState state) {
        return this.worldLightOpacity(state).opacity;
    }

    /**
     * Location-dependent version of {@link #getLightOpacity(BlockState)} Gives
     * more granular transparency information when chunk is loaded.
     * 
     * Any value over 0 prevents a block from seeing the sky.
     */
    @Override
    public int getLightOpacity(BlockState state, IBlockAccess world, BlockPos pos) {
        ISuperModelState modelState = SuperBlockWorldAccess.access(world).getModelState(this, state, pos, false);
        if (this.getSubstance(state, world, pos).isTranslucent) {
            return WorldLightOpacity.opacityFromAlpha(modelState.getAlpha(PaintLayer.BASE));
        } else {
            return modelState.geometricSkyOcclusion();
        }
    }

    /**
     * Number of supported meta values for this block.
     */
    @Override
    public int getMetaCount() {
        return this.metaCount;
    }

    /**
     * Usage of meta is overloaded and dependent on other aspects of state, so just
     * storing the raw value.
     */
    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(META);
    }

    /**
     * If last parameter is false, does not perform a refresh from world for
     * world-dependent state attributes. Use this option to prevent infinite
     * recursion when need to reference some static state ) information in order to
     * determine dynamic world state. Block tests are main use case for false.
     * 
     */
    @Override
    public ISuperModelState getModelState(ISuperBlockAccess world, BlockPos pos, boolean refreshFromWorldIfNeeded) {
        return getModelStateAssumeStateIsCurrent(world.getBlockState(pos), world, pos, refreshFromWorldIfNeeded);
    }

    /**
     * At least one vanilla routine passes in a block state that does not match
     * world. (After block updates, passes in previous state to detect collision box
     * changes.) <br>
     * <br>
     * 
     * We don't want to update our current state based on stale block state, so for
     * TE blocks the refresh must be coded so we don't inject bad (stale) modelState
     * into TE. <br>
     * <br>
     * 
     * However, we do want to honor the given world state if species is different
     * than current. We do this by directly changing species, because that is only
     * thing that can changed in model state based on block state, and also affects
     * collision box. <br>
     * <br>
     * 
     * NOTE: there is probably still a bug here, because collision box can change
     * based on other components of model state (orthogonalAxis, for example) and
     * those changes may not be detected by path finding.
     */
    @Override
    public ISuperModelState computeModelState(BlockState state, ISuperBlockAccess world, BlockPos pos,
            boolean refreshFromWorldIfNeeded) {
        ISuperModelState result = this.getDefaultModelState();
        if (refreshFromWorldIfNeeded) {
            result.refreshFromWorld(state, world, pos);
        } else {
            // do a "lite" refresh that won't cause a stack overflow
            result.setMetaData(state.getValue(META));
        }
        return result;
    }

    /**
     * Use when absolutely certain given block state is current.
     */
    @Override
    public ISuperModelState getModelStateAssumeStateIsCurrent(BlockState state, ISuperBlockAccess world, BlockPos pos,
            boolean refreshFromWorldIfNeeded) {
        if (state instanceof IExtendedBlockState) {
            ISuperModelState result = ((IExtendedBlockState) state).getValue(ISuperBlock.MODEL_STATE);
            if (result != null)
                return result;
        }

        // for mundane (non-TE) blocks don't need to worry about state being persisted,
        // logic is same for old and current states
        return computeModelState(state, world, pos, refreshFromWorldIfNeeded);
    }

    @Override
    
    public int getOcclusionKey(BlockState state, IBlockAccess world, BlockPos pos, Direction side) {
        return SuperDispatcher.INSTANCE
                .getOcclusionKey(SuperBlockWorldAccess.access(world).getModelState(this, state, pos, true), side);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, World world, BlockPos pos,
            EntityPlayer player) {
        // Do not trust the state passed in, because WAILA passes in a default state.
        // Doing so causes us to pass in bad meta value which determines a bad model key
        // which is then cached, leading to strange render problems for blocks just
        // placed up updated.
        BlockState goodState = world.getBlockState(pos);

        return getStackFromBlock(goodState, world, pos);
    }

    @Override
    public ItemStack getStackFromBlock(BlockState state, IBlockAccess world, BlockPos pos) {
        ItemStack result = this.getSubItems().get(this.damageDropped(state));
        // important to copy here - otherwise end up updating instance held in block
        return result == null ? ItemStack.EMPTY : result.copy();
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(META, meta);
    }

    /**
     * {@inheritDoc} <br>
     * <br>
     * 
     * Confusingly named because is really the back end for Item.getSubItems. Used
     * by Creative and JEI to show a list of blocks.
     */
    @Override
    
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        items.addAll(getSubItems());
    }

    @Override
    public void registerItems(IForgeRegistry<Item> itemReg) {
        ItemBlock itemBlock = new SuperItemBlock(this);
        itemBlock.setRegistryName(this.getRegistryName());
        itemReg.register(itemBlock);
    }

    @Override
    public final List<ItemStack> getSubItems() {
        List<ItemStack> result = this.subItems;
        if (result == null) {
            result = this.createSubItems();
            this.subItems = result;
        }
        return result;
    }

    /**
     * Override and alter the items in the list or replace with a different list to
     * control sub items for this block. Only called once per block instance.
     */
    protected List<ItemStack> createSubItems() {
        return this.defaultSubItems();
    }

    /**
     * Default implementation for {@link #createSubItems()}
     */
    protected final List<ItemStack> defaultSubItems() {
        ImmutableList.Builder<ItemStack> itemBuilder = new ImmutableList.Builder<ItemStack>();
        for (int i = 0; i < this.metaCount; i++) {
            ItemStack stack = new ItemStack(this, 1, i);
            ISuperModelState modelState = this.getDefaultModelState();
            if (modelState.metaUsage() != MetaUsage.NONE || i > 0) {
                modelState.setMetaData(i);
            }
            SuperBlockStackHelper.setStackModelState(stack, modelState);
            SuperBlockStackHelper.setStackSubstance(stack, this.defaultSubstance());
            itemBuilder.add(stack);
        }
        return itemBuilder.build();
    }

    /**
     * Controls material-dependent properties
     */
    @Override
    public abstract BlockSubstance getSubstance(BlockState state, IBlockAccess world, BlockPos pos);

    @Override
    public BlockSubstance getSubstance(IBlockAccess world, BlockPos pos) {
        return this.getSubstance(world.getBlockState(pos), world, pos);
    }

    /**
     * {@inheritDoc} <br>
     * <br>
     * 
     * Should be true if shape is not a full cube or fully transparent.
     */
    @Override
    public boolean getUseNeighborBrightness(BlockState state) {
        return this.worldLightOpacity(state) == WorldLightOpacity.TRANSPARENT || !this.isGeometryFullCube(state);
    }

    // overridden to allow for world-sensitive drops
    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, BlockState state,
            TileEntity te, ItemStack stack) {
        StatBase stats = StatList.getBlockStats(this);
        if (stats != null)
            player.addStat(stats);

        player.addExhaustion(0.025F);

        if (stack != null && this.canSilkHarvest(worldIn, pos, state, player)
                && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0) {
            java.util.List<ItemStack> items = new java.util.ArrayList<ItemStack>();

            // this is the part that is different from Vanilla
            ItemStack itemstack = getStackFromBlock(state, worldIn, pos);

            if (!itemstack.isEmpty()) {
                items.add(getStackFromBlock(state, worldIn, pos));
            }

            net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(items, worldIn, pos, state, 0, 1.0f, true,
                    player);
            for (ItemStack item : items) {
                spawnAsEntity(worldIn, pos, item);
            }
        } else {
            harvesters.set(player);
            int i = stack == null ? 0 : EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
            this.dropBlockAsItem(worldIn, pos, state, i);
            harvesters.set(null);
        }
    }

    /**
     * This is an egregious hack to avoid performance hit of instanceof. (Based on
     * performance profile results.) <br>
     * <br>
     * 
     * Default value of {@link #associatedBlock} is set to this instance in
     * constructor. If not changed, will have same behavior as vanilla. Change it to
     * a reference value to have this block be recognized as part of a group.
     * Initially used for flow blocks so that they can be detected quickly.
     */
    @Override
    public boolean isAssociatedBlock(Block other) {
        return other == this.associatedBlock;
    }

    /**
     * {@inheritDoc} <br>
     * <br>
     * 
     * In a rare and ironic nod to vanilla magical thinking, Hypermatter acts as a
     * beacon base. I will almost certainly regret this.
     */
    @Override
    public boolean isBeaconBase(IBlockAccess worldObj, BlockPos pos, BlockPos beacon) {
        return this.isHypermatter() || super.isBeaconBase(worldObj, pos, beacon);
    }

    /**
     * {@inheritDoc} <br>
     * <br>
     * 
     * Determines result of {@link #getAmbientOcclusionLightValue(BlockState)}
     */
    @Override
    public boolean isBlockNormalCube(BlockState state) {
        return this.isGeometryFullCube(state) && this.worldLightOpacity(state) == WorldLightOpacity.SOLID;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, BlockState state, BlockPos pos, Direction face) {
        return SuperBlockWorldAccess.access(world).getModelState(this, pos, true).sideShape(face) == SideShape.SOLID
                ? BlockFaceShape.SOLID
                : BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isBurning(IBlockAccess world, BlockPos pos) {
        return this.getSubstance(world, pos).isBurning;
    }

    @Override
    public boolean isFlammable(IBlockAccess world, BlockPos pos, Direction face) {
        return this.getSubstance(world, pos).flammability > 0;
    }

    /**
     * True if this is an instance of an IFlowBlock and also a filler block. Avoids
     * performance hit of casting to the IFlowBlock Interface. (Based on performance
     * profile results.)
     */
    @Override
    public boolean isFlowFiller() {
        return false;
    }

    /**
     * True if this is an instance of an IFlowBlock and also a height block. Avoids
     * performance hit of casting to the IFlowBlock Interface. (Based on performance
     * profile results.)
     */
    @Override
    public boolean isFlowHeight() {
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * Accessed via state implementation. Used in AI pathfinding, explosions and
     * occlusion culling. Has no actual or extended state properties when
     * referenced. <br>
     * <br>
     * 
     * Vanilla implementation uses fullBlock instance variable, which is derived at
     * construction from {@link #isOpaqueCube(BlockState)}
     */
    @Override
    public boolean isFullBlock(BlockState state) {
        return this.isGeometryFullCube(state);
    }

    /**
     * Used many places in rendering and AI. Must be true for block to cause
     * suffocation. Input state provided has no extended properties. Result appears
     * to be based on geometry - if block is a full 1.0 cube return true, false
     * otherwise.
     * 
     * Is also used in derivation of {@link #isFullyOpaque(BlockState)} and
     * {@link #isNormalCube(BlockState)}
     */
    @Override
    public boolean isFullCube(BlockState state) {
        return this.isGeometryFullCube(state);
    }

    /**
     * With {@link #isSubstanceTranslucent(BlockState)} makes all the block test
     * methods work when full location information not available.
     * 
     * Only addresses geometry - does this block fully occupy a 1x1x1 cube? True if
     * so. False otherwise.
     */
    @Override
    public abstract boolean isGeometryFullCube(BlockState state);

    @Override
    public abstract boolean isHypermatter();

    /**
     * Only true for virtual blocks. Prevents "instanceof" checking.
     */
    @Override
    public boolean isVirtual() {
        return false;
    }

    /**
     * {@inheritDoc} <br>
     * <br>
     * 
     * Vanilla version will return false if material is not fully opaque. Ours is
     * based solely on geometry && solidity.
     * 
     */
    @Override
    public boolean isNormalCube(BlockState state, IBlockAccess world, BlockPos pos) {
        return SuperBlockWorldAccess.access(world).computeModelState(this, state, pos, true).isCube()
                && this.getSubstance(state, world, pos).material.isSolid();
    }

    /**
     * {@inheritDoc} <br>
     * <br>
     * 
     * Value given for the default state is also used in Block constructor to
     * determine value of fullBlock which in turn is used to determine initial value
     * of lightOpacity.
     */
    @Override
    public boolean isOpaqueCube(BlockState state) {
        return this.isGeometryFullCube(state) && this.worldLightOpacity(state) == WorldLightOpacity.SOLID;
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return this.getSubstance(worldIn, pos).material.isReplaceable();
    }

    @Override
    public boolean isSideSolid(BlockState base_state, IBlockAccess world, BlockPos pos, Direction side) {
        return SuperBlockWorldAccess.access(world).computeModelState(this, base_state, pos, true)
                .sideShape(side).holdsTorch;
    }

    @Override
    
    public boolean isTranslucent(BlockState state) {
        return this.worldLightOpacity(state) != WorldLightOpacity.SOLID;
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        BlockSubstance substance = this.getSubstance(worldIn, pos);
        if (!entityIn.isSneaking() && substance.walkSpeedFactor != 0.0) {
            entityIn.motionX *= substance.walkSpeedFactor;
            entityIn.motionZ *= substance.walkSpeedFactor;
        }
    }

    /**
     * World-aware version called from getDrops because logic may need more than
     * metadata. Other versions (not overriden) should not be called.
     */
    @Override
    public int quantityDropped(IBlockAccess world, BlockPos pos, BlockState state) {
        return 1;
    }

    /** should never be used */
    @Override
    public int quantityDropped(BlockState state, int fortune, Random random) {
        assert false : "Unsupported call to SuperBlock.quantityDropped(BlockState state, int fortune, Random random)";
        return 0;
    }

    /** should never be used */
    @Override
    public int quantityDropped(Random random) {
        assert false : "Unsupported call to SuperBlock.quantityDropped(Random random)";
        return 0;
    }

    /** should never be used */
    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        assert false : "Unsupported call to SuperBlock.quantityDroppedWithBonus";
        return 0;
    }

    /**
     * should never be used - all handled in
     * {@link #collisionRayTrace(BlockState, World, BlockPos, Vec3d, Vec3d)}
     */
    @Override
    protected RayTraceResult rayTrace(BlockPos pos, Vec3d start, Vec3d end, BoundingBox boundingBox) {
        assert false : "Unsupported call to SuperBlock.rayTrace on block with custom collision handler";
        return super.rayTrace(pos, start, end, boundingBox);
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, Direction axis) {
        return false;
        // Rotation currently not supported.
        // Code below does not work because need to refresh modelstate and send to
        // client

//        BlockState blockState = world.getBlockState(pos);
//        return this.getModelStateAssumeStateIsCurrent(blockState, world, pos, true).rotateBlock(blockState, world, pos, axis, this);
    }

    @Override
    public SuperBlock setAllowSilkHarvest(boolean allow) {
        this.allowSilkHarvest = allow;
        return this;
    }

    /**
     * Sets a drop other than this block if desired.
     */
    @Override
    public ISuperBlock setDropItem(Item dropItem) {
        this.dropItem = dropItem;
        return this;
    }

    /**
     * Want to avoid the synchronization penalty of pooled block pos. For use only
     * in
     * {@link #shouldSideBeRendered(BlockState, IBlockAccess, BlockPos, Direction)}
     */
    protected static ThreadLocal<MutableBlockPos> shouldSideBeRenderedPos = new ThreadLocal<MutableBlockPos>() {

        @Override
        protected MutableBlockPos initialValue() {
            return new MutableBlockPos();
        }
    };

    @Override
    
    public boolean shouldSideBeRendered(BlockState blockState, IBlockAccess blockAccess, BlockPos pos,
            Direction side) {
        if (this.getSubstance(blockState, blockAccess, pos).isTranslucent) {
            final MutableBlockPos mpos = shouldSideBeRenderedPos.get().setPos(pos).move(side);

            BlockState otherBlockState = blockAccess.getBlockState(mpos);
            Block block = otherBlockState.getBlock();
            if (block instanceof SuperBlock) {
                ISuperBlock sBlock = (ISuperBlock) block;
                // only match with blocks with same "virtuality" as this one
                if (this.isVirtual() == sBlock.isVirtual()
                        && sBlock.getSubstance(otherBlockState, blockAccess, mpos).isTranslucent) {
                    ISuperBlockAccess access = SuperBlockWorldAccess.access(blockAccess);

                    ISuperModelState myModelState = access.getModelState(this, blockState, pos, false);
                    ISuperModelState otherModelState = access.getModelState(sBlock, otherBlockState, mpos, false);
                    // for transparent blocks, want blocks with same apperance and species to join
                    return (myModelState.hasSpecies() && myModelState.getSpecies() != otherModelState.getSpecies())
                            || !myModelState.doShapeAndAppearanceMatch(otherModelState);

                }
            }
        }

        return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }

    /**
     * Used in conjunction with {@link #isGeometryFullCube(BlockState)} to make all
     * the other full/normal/opaque/translucent methods work when they don't have
     * full location information. NB: default vanilla implementation is simply
     * this.translucent
     * 
     * 
     * Should return true if the substance is not fully opaque. Has nothing to do
     * with block geometry.
     */
    protected abstract WorldLightOpacity worldLightOpacity(BlockState state);

    @Override
    public final RenderLayout renderLayout() {
        return renderLayoutProducer.renderLayout();
    }
}
