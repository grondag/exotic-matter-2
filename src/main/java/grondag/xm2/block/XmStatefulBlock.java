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

import grondag.xm2.api.model.MutablePrimitiveModelState;
import grondag.xm2.api.model.ModelPrimitiveState;
import grondag.xm2.block.XmBlockRegistryImpl.XmBlockStateImpl;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/** base class for tile entity blocks */
public class XmStatefulBlock extends XmSimpleBlock implements BlockEntityProvider {
    /**
     * Prevent concurrency weirdness in
     * {@link #getTileEntityReliably(World, BlockPos)}
     */
    private static final Object BLOCK_ENTITY_AD_HOCK_CREATION_LOCK = new Object();

    private final BlockEntityType<?> blockEntityType;

    public XmStatefulBlock(Settings blockSettings, MutablePrimitiveModelState defaultModelState, BlockEntityType<?> blockEntityType) {
        super(blockSettings, defaultModelState);
        this.blockEntityType = blockEntityType;
    }

    @Override
    public BlockEntity createBlockEntity(BlockView worldIn) {
        return new XmTileEntity(blockEntityType);
    }

    public BlockEntity getTileEntityReliably(World world, BlockPos pos) {
        BlockEntity result = world.getBlockEntity(pos);
        if (result == null) {
            synchronized (BLOCK_ENTITY_AD_HOCK_CREATION_LOCK) {
                result = world.getBlockEntity(pos);
                if (result == null) {
                    result = createBlockEntity(world);
                    world.setBlockEntity(pos, result);
                }
            }
        }
        return result;
    }

    public static ModelPrimitiveState computeModelState(XmBlockState xmStateIn, BlockView world, BlockPos pos, boolean refreshFromWorldIfNeeded) {
        final BlockEntity myTE = world.getBlockEntity(pos);
        final XmBlockStateImpl xmState = (XmBlockStateImpl) xmStateIn;

        if (myTE != null
                && myTE instanceof XmTileEntity) {
            return ((XmTileEntity) myTE).getModelState(xmState, world, pos, refreshFromWorldIfNeeded);
        } else {
            return XmSimpleBlock.computeModelState(xmState, world, pos, refreshFromWorldIfNeeded);
        }
    }

    /**
     * Note this does not send an update to client. At this time, isn't needed
     * because model state is always set on server immediately after block state is
     * set, in the same thread, and so tile entity data always goes along with block
     * state packet.
     */
    public void setModelState(World world, BlockPos pos, ModelPrimitiveState modelState) {
        BlockEntity blockTE = world.getBlockEntity(pos);
        if (blockTE != null
                && blockTE instanceof XmTileEntity) {
            ((XmTileEntity) blockTE).setModelState(modelState);
        }
    }
}
