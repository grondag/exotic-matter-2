package grondag.brocade.legacy.block;

import java.util.Map;



import com.google.common.collect.Maps;

import grondag.brocade.block.BlockHarvestTool;
import grondag.brocade.model.varia.SuperDispatcher;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;



/**
 * SuperBlocks all use the same underlying dispatcher.
 */

public class SuperStateMapper extends DefaultStateMapper {
    public static final SuperStateMapper INSTANCE = new SuperStateMapper();

    private SuperStateMapper() {
    }

    @Override
    public Map<BlockState, ModelResourceLocation> putStateModelLocations(Block block) {
        Map<BlockState, ModelResourceLocation> mapLocations = Maps.newLinkedHashMap();

        if (block instanceof ISuperBlock) {
            SuperBlock superBlock = (SuperBlock) block;
            for (int i = 0; i < 16; i++) {
                BlockState state = superBlock.getDefaultState().withProperty(ISuperBlock.META, i);

                if (block instanceof SuperModelBlock) {
                    for (BlockHarvestTool tool : BlockHarvestTool.values()) {
                        for (int l = 0; l <= SuperModelBlock.MAX_HARVEST_LEVEL; l++) {
                            mapLocations.put(
                                    state.withProperty(SuperModelBlock.HARVEST_TOOL, tool)
                                            .withProperty(SuperModelBlock.HARVEST_LEVEL, l),
                                    new ModelResourceLocation(
                                            SuperDispatcher.INSTANCE.getDelegate(superBlock).getModelResourceString()));
                        }
                    }
                } else {
                    mapLocations.put(state, new ModelResourceLocation(
                            SuperDispatcher.INSTANCE.getDelegate(superBlock).getModelResourceString()));
                }
            }
        }
        return mapLocations;
    }
}