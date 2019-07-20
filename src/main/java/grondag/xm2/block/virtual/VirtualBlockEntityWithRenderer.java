package grondag.xm2.block.virtual;

import net.minecraft.block.entity.BlockEntityType;

/**
 * Only purpose is to exclude tile entities that don't need TESR
 * from chunk rendering loop. Code is identical to SuperTileEntityTESR.
 */
public class VirtualBlockEntityWithRenderer extends VirtualBlockEntity
{
    public VirtualBlockEntityWithRenderer(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
    }
}