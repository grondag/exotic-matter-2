package grondag.brocade.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.IStringSerializable;

public enum BlockHarvestTool implements IStringSerializable
{
    ANY(null),
    PICK("pickaxe"),
    AXE("axe"),
    SHOVEL("shovel");
    
    /**
     * String MC uses to compare test for this tool type.
     * Null means any tool can harvest.
     */
    @Nullable 
    public final String toolString;
    
    private BlockHarvestTool(@Nullable String toolString)
    {
        this.toolString = toolString;
    }

    @Override
    public @Nonnull String getName()
    {
        return this.name().toLowerCase();
    }
}
