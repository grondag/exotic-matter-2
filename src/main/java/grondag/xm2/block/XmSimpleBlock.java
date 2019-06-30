package grondag.xm2.block;

import java.util.List;

import grondag.xm2.collision.CollisionBoxDispatcher;
import grondag.xm2.connect.api.world.BlockTest;
import grondag.xm2.painting.PaintLayer;
import grondag.xm2.state.ModelState;
import grondag.xm2.state.ModelStateImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateFactory.Builder;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

/**
 * Base class for static building blocks.
 */
public class XmSimpleBlock extends Block implements XmBlock {
    //TODO: use an immutable instance instead?
    /** change in constructor to have different appearance */
    protected int[] defaultModelStateBits;

    /** Hacky hack to let us inspect default model state during constructor before it is saved */
    protected static final ThreadLocal<ModelState> INIT_STATE = new ThreadLocal<>();
    
    public static XmSimpleBlock create(Settings blockSettings, ModelState defaultModelState) {
        INIT_STATE.set(defaultModelState);
        return new XmSimpleBlock(blockSettings, defaultModelState);
    }
    
    protected XmSimpleBlock(Settings blockSettings, ModelState defaultModelState) {
        super(blockSettings);
        this.defaultModelStateBits = defaultModelState.serializeToInts();
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        final ModelState defaultState = INIT_STATE.get();
        if(defaultState != null) {
            if(defaultState.hasSpecies()) {
                builder.add(SPECIES);
            }
            final EnumProperty<?> orientationProp = defaultState.getShape().meshFactory().orientationType(defaultState).property;
            if(orientationProp != null) {
                builder.add(orientationProp);
            }
        }
    }

    /**
     * Factory for block test that should be used for border/shape joins for this
     * block. Used in model state refresh from world.
     */
    @Override
    public BlockTest blockJoinTest() {
        return XmBorderMatch.INSTANCE;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView blockView, BlockPos pos, EntityContext entityContext) {
        ModelState modelState = getModelStateAssumeStateIsCurrent(state, blockView, pos, true);
        return CollisionBoxDispatcher.getOutlineShape(modelState);
    }

    //TODO: add hook in or around BlockCrackParticle
//    @Override
//    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
//        BlockState blockState = world.getBlockState(pos);
//        if (blockState.getBlock() != this) {
//            // somehow got called for a different block
//            if (blockState.getBlock() instanceof SuperBlock) {
//                // if the block at given position is somehow also a SuperBlock, call particle
//                // handler for it
//                ((blockState.getBlock())).addDestroyEffects(world, pos, manager);
//            } else {
//                // handle as a vanilla block
//                return false;
//            }
//        }
//
//        ISuperModelState modelState = SuperBlockWorldAccess.access(world).getModelState(this, pos, false);
//
//        for (int j = 0; j < 4; ++j) {
//            for (int k = 0; k < 4; ++k) {
//                for (int l = 0; l < 4; ++l) {
//                    double d0 = ((double) j + 0.5D) / 4.0D;
//                    double d1 = ((double) k + 0.5D) / 4.0D;
//                    double d2 = ((double) l + 0.5D) / 4.0D;
//                    manager.addEffect(
//                            (new ParticleDiggingSuperBlock(world, (double) pos.getX() + d0, (double) pos.getY() + d1,
//                                    (double) pos.getZ() + d2, d0 - 0.5D, d1 - 0.5D, d2 - 0.5D, blockState, modelState))
//                                            .setBlockPos(pos));
//                }
//            }
//        }
//
//        return true;
//    }

    //TODO: add hook for landing particles
//    @Override
//    public boolean addHitEffects(BlockState blockState, World world, RayTraceResult target,
//            ParticleManager manager) {
//        if (blockState.getBlock() != this) {
//            // somehow got called for a different block
//            if (blockState.getBlock() instanceof SuperBlock) {
//                // if the block at given position is somehow also a SuperBlock, call particle
//                // handler for it
//                ((blockState.getBlock())).addHitEffects(blockState, world, target, manager);
//            } else {
//                // handle as a vanilla block
//                return false;
//            }
//        }
//
//        BlockPos pos = target.getBlockPos();
//        ISuperModelState modelState = SuperBlockWorldAccess.access(world).getModelState(this, pos, false);
//
//        Direction side = target.sideHit;
//
//        if (blockState.getRenderType() != EnumBlockRenderType.INVISIBLE) {
//            int i = pos.getX();
//            int j = pos.getY();
//            int k = pos.getZ();
//
//            BoundingBox BoundingBox = blockState.getBoundingBox(world, pos);
//            double d0 = (double) i
//                    + ThreadLocalRandom.current().nextDouble()
//                            * (BoundingBox.maxX - BoundingBox.minX - 0.20000000298023224D)
//                    + 0.10000000149011612D + BoundingBox.minX;
//            double d1 = (double) j
//                    + ThreadLocalRandom.current().nextDouble()
//                            * (BoundingBox.maxY - BoundingBox.minY - 0.20000000298023224D)
//                    + 0.10000000149011612D + BoundingBox.minY;
//            double d2 = (double) k
//                    + ThreadLocalRandom.current().nextDouble()
//                            * (BoundingBox.maxZ - BoundingBox.minZ - 0.20000000298023224D)
//                    + 0.10000000149011612D + BoundingBox.minZ;
//
//            if (side == Direction.DOWN) {
//                d1 = (double) j + BoundingBox.minY - 0.10000000149011612D;
//            }
//
//            if (side == Direction.UP) {
//                d1 = (double) j + BoundingBox.maxY + 0.10000000149011612D;
//            }
//
//            if (side == Direction.NORTH) {
//                d2 = (double) k + BoundingBox.minZ - 0.10000000149011612D;
//            }
//
//            if (side == Direction.SOUTH) {
//                d2 = (double) k + BoundingBox.maxZ + 0.10000000149011612D;
//            }
//
//            if (side == Direction.WEST) {
//                d0 = (double) i + BoundingBox.minX - 0.10000000149011612D;
//            }
//
//            if (side == Direction.EAST) {
//                d0 = (double) i + BoundingBox.maxX + 0.10000000149011612D;
//            }
//
//            manager.addEffect(
//                    (new ParticleDiggingSuperBlock(world, d0, d1, d2, 0.0D, 0.0D, 0.0D, blockState, modelState))
//                            .setBlockPos(pos).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
//        }
//
//        return true;
//    }
    
    

    @Environment(EnvType.CLIENT)
    @Override
    public void buildTooltip(ItemStack stack, BlockView world, List<Text> tooltip, TooltipContext context) {
        super.buildTooltip(stack, world, tooltip, context);
        tooltip.add(new TranslatableText("label.meta", stack.getDamage()));

        ModelState modelState = XmStackHelper.getStackModelState(stack);

        if (modelState != null) {
            tooltip.add(new TranslatableText("label.shape", modelState.getShape().localizedName()));
            tooltip.add(new TranslatableText("label.base_color", Integer.toHexString(modelState.getColorARGB(PaintLayer.BASE))));
            tooltip.add(new TranslatableText("label.base_texture", modelState.getTexture(PaintLayer.BASE).displayName()));
            if (modelState.isLayerEnabled(PaintLayer.OUTER)) {
                tooltip.add(new TranslatableText("label.outer_color", Integer.toHexString(modelState.getColorARGB(PaintLayer.OUTER))));
                tooltip.add(new TranslatableText("label.outer_texture", modelState.getTexture(PaintLayer.OUTER).displayName()));
            }
            if (modelState.isLayerEnabled(PaintLayer.MIDDLE)) {
                tooltip.add(new TranslatableText("label.middle_color", Integer.toHexString(modelState.getColorARGB(PaintLayer.MIDDLE))));
                tooltip.add(new TranslatableText("label.middle_texture", modelState.getTexture(PaintLayer.MIDDLE).displayName()));
            }
            if (modelState.hasSpecies()) {
                tooltip.add(new TranslatableText("label.species", modelState.getSpecies()));
            }
        }
        tooltip.add(new TranslatableText("label.material", XmStackHelper.getStackSubstance(stack).localizedName()));
    }

    //TODO: add hook for landing effects
//    @Override
//    public boolean addLandingEffects(BlockState state, WorldServer worldObj,
//            BlockPos blockPosition, BlockState BlockState, LivingEntity entity,
//            int numberOfParticles) {
//        // This is server-side, so to get matching particle color/texture I'd have to
//        // create a custom particle type similar to ParticleBlockDust,
//        // register it and add a handler for it. Light-colored quartz particles are fine
//        // IMO - is just kicking up dust, not breaking anything.
//
//        worldObj.spawnParticle(EnumParticleTypes.BLOCK_DUST, entity.posX, entity.posY, entity.posZ, numberOfParticles,
//                0.0D, 0.0D, 0.0D, 0.15000000596046448D,
//                new int[] { Block.getStateId(Blocks.QUARTZ_BLOCK.getDefaultState()) });
//        return true;
//    }

    //TODO: re-implement TOP support
//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player,
//            World world, BlockState blockState, IProbeHitData data) {
//        if (blockState == null || world == null || data == null || probeInfo == null)
//            return;
//
//        ISuperModelState modelState = SuperBlockWorldAccess.access(world).computeModelState(this, blockState,
//                data.getPos(), true);
//
//        probeInfo.text(I18n.translate("label.shape") + ": " + modelState.getShape().localizedName());
//        probeInfo.text(I18n.translate("label.base_color") + ": "
//                + Integer.toHexString(modelState.getColorARGB(PaintLayer.BASE)));
//        probeInfo.text(I18n.translate("label.base_texture") + ": "
//                + modelState.getTexture(PaintLayer.BASE).displayName());
//        if (modelState.isLayerEnabled(PaintLayer.OUTER)) {
//            probeInfo.text(I18n.translate("label.outer_color") + ": "
//                    + Integer.toHexString(modelState.getColorARGB(PaintLayer.OUTER)));
//            probeInfo.text(I18n.translate("label.outer_texture") + ": "
//                    + modelState.getTexture(PaintLayer.OUTER).displayName());
//        }
//        if (modelState.isLayerEnabled(PaintLayer.MIDDLE)) {
//            probeInfo.text(I18n.translate("label.middle_color") + ": "
//                    + Integer.toHexString(modelState.getColorARGB(PaintLayer.MIDDLE)));
//            probeInfo.text(I18n.translate("label.middle_texture") + ": "
//                    + modelState.getTexture(PaintLayer.MIDDLE).displayName());
//        }
//        if (modelState.hasSpecies()) {
//            probeInfo.text(I18n.translate("label.species") + ": " + modelState.getSpecies());
//        }
//
//        if (BrocadeConfig.BLOCKS.probeInfoLevel != ProbeInfoLevel.BASIC) {
//            if (modelState.hasAxis()) {
//                probeInfo.text(I18n.translate("label.axis") + ": " + modelState.getAxis());
//                if (modelState.hasAxisOrientation()) {
//                    probeInfo.text(I18n.translate("label.axis_inverted") + ": " + modelState.isAxisInverted());
//                }
//            }
//            if (modelState.hasAxisRotation()) {
//                probeInfo.text(I18n.translate("label.model_rotation") + ": " + modelState.getAxisRotation());
//            }
//            probeInfo.text(I18n.translate("label.position") + ": " + modelState.getPosX() + ", "
//                    + modelState.getPosY() + ", " + modelState.getPosZ());
//        }
//
//        probeInfo.text(I18n.translate("label.material") + ": "
//                + this.getSubstance(blockState, world, data.getPos()).localizedName());
//
//        if (BrocadeConfig.BLOCKS.probeInfoLevel == ProbeInfoLevel.DEBUG) {
//            probeInfo.text(I18n.translate("label.meta") + ": " + blockState.getValue(ISuperBlock.META));
//
//            probeInfo.text(I18n.translate("label.full_block") + ": " + this.fullBlock);
//            probeInfo.text("isOpaqueCube(): " + this.isOpaqueCube(blockState));
//            probeInfo.text("isFullCube(): " + this.isFullCube(blockState));
//            probeInfo.text("getUseNB: " + this.getUseNeighborBrightness(blockState));
//            probeInfo.text("getLightOpacity: " + this.getLightOpacity(blockState));
//            probeInfo.text("getAmbientOcclusionLightValue: " + this.getAmbientOcclusionLightValue(blockState));
//            probeInfo
//                    .text("getPackedLightmapCoords: " + this.getPackedLightmapCoords(blockState, world, data.getPos()));
//
//            BlockState upState = world.getBlockState(data.getPos().up());
//            probeInfo.text("UP isFullBlock: " + upState.isFullBlock());
//            probeInfo.text("UP isOpaqueCube(): " + upState.isOpaqueCube());
//            probeInfo.text("UP isFullCube(): " + upState.isFullCube());
//            probeInfo.text("UP getUseNB: " + upState.useNeighborBrightness());
//            probeInfo.text("UP getLightOpacity: " + upState.getLightOpacity());
//            probeInfo.text("UP getAmbientOcclusionLightValue: " + upState.getAmbientOcclusionLightValue());
//            probeInfo.text("UP getPackedLightmapCoords: "
//                    + this.getPackedLightmapCoords(blockState, world, data.getPos().up()));
//        }
//    }


    /**
     * Returns an instance of the default model state for this block. Because model
     * states are mutable, every call returns a new instance.
     */
    @Override
    public ModelState getDefaultModelState() {
        return new ModelStateImpl(this.defaultModelStateBits);
    }

    /**
     * If last parameter is false, does not perform a refresh from world for
     * world-dependent state attributes. Use this option to prevent infinite
     * recursion when need to reference some static state ) information in order to
     * determine dynamic world state. Block tests are main use case for false.
     * 
     */
    @Override
    public ModelState getModelState(BlockView world, BlockPos pos, boolean refreshFromWorldIfNeeded) {
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
    public ModelState computeModelState(BlockState state, BlockView world, BlockPos pos, boolean refreshFromWorldIfNeeded) {
        ModelState result = this.getDefaultModelState();
        
        if(state.contains(SPECIES)) {
            result.setMetaData(state.get(SPECIES));
        }
        
        result.getShape().meshFactory().orientationType(result).stateFunc.accept(state, result);

        if (refreshFromWorldIfNeeded) {
            result.refreshFromWorld(state, world, pos);
        }
        
        return result;
    }

    /**
     * Use when absolutely certain given block state is current.
     */
    @Override
    public ModelState getModelStateAssumeStateIsCurrent(BlockState state, BlockView world, BlockPos pos, boolean refreshFromWorldIfNeeded) {
        // for mundane (non-TE) blocks don't need to worry about state being persisted,
        // logic is same for old and current states
        return computeModelState(state, world, pos, refreshFromWorldIfNeeded);
    }

  //TODO: restore or remove
//    @Override
//    public int getOcclusionKey(BlockState state, BlockView world, BlockPos pos, Direction side) {
//        return SuperDispatcher.INSTANCE
//                .getOcclusionKey(getModelStateAssumeStateIsCurrent(state, world, pos, true), side);
//    }

    //TODO: restore or remove
    // overridden to allow for world-sensitive drops
//    @Override
//    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, BlockState state,
//            TileEntity te, ItemStack stack) {
//        StatBase stats = StatList.getBlockStats(this);
//        if (stats != null)
//            player.addStat(stats);
//
//        player.addExhaustion(0.025F);
//
//        if (stack != null && this.canSilkHarvest(worldIn, pos, state, player)
//                && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0) {
//            java.util.List<ItemStack> items = new java.util.ArrayList<ItemStack>();
//
//            // this is the part that is different from Vanilla
//            ItemStack itemstack = getStackFromBlock(state, worldIn, pos);
//
//            if (!itemstack.isEmpty()) {
//                items.add(getStackFromBlock(state, worldIn, pos));
//            }
//
//            net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(items, worldIn, pos, state, 0, 1.0f, true,
//                    player);
//            for (ItemStack item : items) {
//                spawnAsEntity(worldIn, pos, item);
//            }
//        } else {
//            harvesters.set(player);
//            int i = stack == null ? 0 : EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
//            this.dropBlockAsItem(worldIn, pos, state, i);
//            harvesters.set(null);
//        }
//    }

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
     * Only true for virtual blocks. Prevents "instanceof" checking.
     */
    @Override
    public boolean isVirtual() {
        return false;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        //TODO: add species handling
        final ModelState modelState = this.getDefaultModelState();
        return modelState.getShape().meshFactory().orientationType(modelState).placementFunc.apply(getDefaultState(), context);
    }
}
