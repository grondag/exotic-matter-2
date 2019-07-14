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

package grondag.xm2.block;

import java.util.List;
import java.util.function.Function;

import grondag.xm2.api.model.ImmutablePrimitiveModelState;
import grondag.xm2.api.model.MutablePrimitiveModelState;
import grondag.xm2.api.model.PrimitiveModelState;
import grondag.xm2.block.XmBlockRegistryImpl.XmBlockStateImpl;
import grondag.xm2.collision.CollisionBoxDispatcher;
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
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

/**
 * Base class for static building blocks.
 */
public class XmSimpleBlock extends Block {
    public static final IntProperty SPECIES = IntProperty.of("xm2_species", 0, 15);

    /**
     * Hacky hack to let us inspect default model state during constructor before it
     * is saved
     */
    protected static final ThreadLocal<MutablePrimitiveModelState> INIT_STATE = new ThreadLocal<>();

    protected static Settings prepareInit(Settings blockSettings, MutablePrimitiveModelState defaultModelState) {
        INIT_STATE.set(defaultModelState);
        return blockSettings;
    }

    public static PrimitiveModelState computeModelState(XmBlockState xmState, BlockView world, BlockPos pos, boolean refreshFromWorld) {
        if (refreshFromWorld) {
            MutablePrimitiveModelState result = xmState.defaultModelState().mutableCopy();
            return result.refreshFromWorld((XmBlockStateImpl) xmState, world, pos);
        } else {
            return xmState.defaultModelState();
        }
    }

    public XmSimpleBlock(Settings blockSettings, MutablePrimitiveModelState defaultModelState) {
        super(prepareInit(blockSettings, defaultModelState));
        defaultModelState.primitive().orientationType(defaultModelState).stateFunc.accept(this.getDefaultState(),
                defaultModelState);
        XmBlockRegistryImpl.register(this, defaultModelStateFunc(defaultModelState), XmSimpleBlock::computeModelState,
                XmBorderMatch.INSTANCE);
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        final MutablePrimitiveModelState defaultState = INIT_STATE.get();
        if (defaultState != null) {
            if (defaultState.hasSpecies()) {
                builder.add(XmSimpleBlock.SPECIES);
            }
            final EnumProperty<?> orientationProp = defaultState.primitive().orientationType(defaultState).property;
            if (orientationProp != null) {
                builder.add(orientationProp);
            }
        }
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView blockView, BlockPos pos, EntityContext entityContext) {
        final PrimitiveModelState modelState = XmBlockStateAccess.get(state).getModelState(blockView, pos, true);
        return CollisionBoxDispatcher.getOutlineShape(modelState);
    }

    // TODO: add hook in or around BlockCrackParticle
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

    // TODO: add hook for landing particles
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

        MutablePrimitiveModelState modelState = XmStackHelper.getStackModelState(stack);

        if (modelState != null) {
            tooltip.add(new TranslatableText("label.shape", modelState.primitive().translationKey()));
            // TODO: restore some info about color/texture?
//            tooltip.add(new TranslatableText("label.base_color", Integer.toHexString(modelState.getColorARGB(PaintLayer.BASE))));
//            tooltip.add(new TranslatableText("label.base_texture", modelState.getTexture(PaintLayer.BASE).displayName()));
//            if (modelState.isLayerEnabled(PaintLayer.OUTER)) {
//                tooltip.add(new TranslatableText("label.outer_color", Integer.toHexString(modelState.getColorARGB(PaintLayer.OUTER))));
//                tooltip.add(new TranslatableText("label.outer_texture", modelState.getTexture(PaintLayer.OUTER).displayName()));
//            }
//            if (modelState.isLayerEnabled(PaintLayer.MIDDLE)) {
//                tooltip.add(new TranslatableText("label.middle_color", Integer.toHexString(modelState.getColorARGB(PaintLayer.MIDDLE))));
//                tooltip.add(new TranslatableText("label.middle_texture", modelState.getTexture(PaintLayer.MIDDLE).displayName()));
//            }
//            if (modelState.hasSpecies()) {
//                tooltip.add(new TranslatableText("label.species", modelState.getSpecies()));
//            }
        }
        tooltip.add(new TranslatableText("label.material", XmStackHelper.getStackSubstance(stack).localizedName()));
    }

    // TODO: add hook for landing effects
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

    // TODO: re-implement TOP support
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

    // TODO: restore or remove
//    @Override
//    public int getOcclusionKey(BlockState state, BlockView world, BlockPos pos, Direction side) {
//        return SuperDispatcher.INSTANCE
//                .getOcclusionKey(getModelStateAssumeStateIsCurrent(state, world, pos, true), side);
//    }

    // TODO: restore or remove
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

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        // TODO: add species handling
        final MutablePrimitiveModelState modelState = XmBlockStateAccess.get(this).defaultModelState.mutableCopy();
        return modelState.primitive().orientationType(modelState).placementFunc.apply(getDefaultState(), context);
    }

    public static Function<BlockState, ImmutablePrimitiveModelState> defaultModelStateFunc(MutablePrimitiveModelState baseModelState) {
        return (state) -> {
            MutablePrimitiveModelState result = baseModelState.mutableCopy();

            if (state.contains(SPECIES)) {
                result.worldState().species(state.get(SPECIES));
            }

            result.primitive().orientationType(result).stateFunc.accept(state, result);

            return result.toImmutable();
        };
    }
}
