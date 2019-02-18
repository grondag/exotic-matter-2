package grondag.brocade.terrain;

import java.util.HashMap;

import javax.annotation.Nullable;

import com.google.common.collect.HashBiMap;

import net.minecraft.block.Block;

/** tracks which terrain blocks can be frozen or thawed from each other */
public class TerrainBlockRegistry
{
    private HashBiMap<Block, Block> stateMap = HashBiMap.create(16);
    private HashBiMap<Block, Block> fillerMap = HashBiMap.create(16);
    private HashMap<Block, Block> cubicMap = new HashMap<Block, Block>(16);
    public static final TerrainBlockRegistry TERRAIN_STATE_REGISTRY = new TerrainBlockRegistry();
    
    
    public void registerStateTransition(Block dynamicBlock, Block staticBlock)
    {
        stateMap.put(dynamicBlock, staticBlock);
    }
    
    @Nullable
    public Block getStaticBlock(Block dynamicBlock)
    {
        return this.stateMap.get(dynamicBlock);
    }
    
    public Block getDynamicBlock(Block staticBlock)
    {
        return this.stateMap.inverse().get(staticBlock);
    }
    
    public void registerFiller(Block heightBlock, Block fillerBlock)
    {
        fillerMap.put(heightBlock, fillerBlock);
    }
    
    @Nullable
    public Block getFillerBlock(Block hieghtBlock)
    {
        return this.fillerMap.get(hieghtBlock);
    }
    
    public Block getHeightBlock(Block fillerBlock)
    {
        return this.fillerMap.inverse().get(fillerBlock);
    }
    
    public void registerCubic(Block flowBlock, Block cubicBlock)
    {
        this.cubicMap.put(flowBlock, cubicBlock);
    }
    
    public Block getCubicBlock(Block flowBlock)
    {
        return this.cubicMap.get(flowBlock);
    }
}
