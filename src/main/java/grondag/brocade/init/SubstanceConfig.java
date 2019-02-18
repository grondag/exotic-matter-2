package grondag.brocade.init;

import grondag.exotic_matter.block.BlockHarvestTool;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresMcRestart;

public class SubstanceConfig
{
    @RequiresMcRestart
    @Comment("Material hardness. 2 is typical for things like rock, wood. Anything above 10 is extreme. -1 is unbreakable")
    @RangeInt(min = -1, max = 2000)
    public int hardness;

    @RequiresMcRestart
    @Comment("Tool used to break block.")
    public BlockHarvestTool harvestTool;

    @RequiresMcRestart
    @Comment("Level of tool needed to break block. Range 1-3 is normal for vanilla.")
    @RangeInt(min = 0, max = 10)
    public int harvestLevel;

    @RequiresMcRestart
    @Comment("Material explosion resistance")
    @RangeInt(min = 1, max = 2000)
    public int resistance;

    @RequiresMcRestart
    @Comment("Material speed modifier for entities walking on its surface.")
    @RangeDouble(min = 0.25, max = 2.0)
    public double walkSpeedFactor;

    @RequiresMcRestart
    @Comment("If non-zero, can catch flame and spread. Numbers are typically small (1 or 2)")
    public int flammability = 0;
    
    @RequiresMcRestart
    @Comment("If true, will damage and set fire to colliding entities.")
    public boolean isBurning = false;

    @RequiresMcRestart
    @Comment("Determeins AI handling for this block. Normal value is BLOCKED.")
    public ConfigPathNodeType pathNodeType = ConfigPathNodeType.BLOCKED;

    public SubstanceConfig(int hardness, BlockHarvestTool harvestTool, int harvestLevel, int resistance, double walkSpeedFactor)
    {
        this.hardness = hardness;
        this.harvestTool = harvestTool;
        this.harvestLevel = harvestLevel;
        this.resistance = resistance;
        this.walkSpeedFactor = walkSpeedFactor;
    }
    
    public SubstanceConfig withFlammability(int flammability)
    {
        this.flammability = flammability;
        return this;
    }
    
    public SubstanceConfig setBurning()
    {
        this.isBurning = true;
        return this;
    }
    
    public SubstanceConfig withPathNodeType(ConfigPathNodeType pathNodeType)
    {
        this.pathNodeType = pathNodeType;
        return this;
    }
}