package grondag.hard_science.machines.support;

import javax.annotation.Nonnull;

import grondag.exotic_matter.placement.SuperItemBlock;
import grondag.hard_science.machines.base.MachineBlock;
import net.minecraft.item.ItemStack;

public class MachineItemBlock extends SuperItemBlock
{
    
    public static final int MAX_DAMAGE = 100;
    
    public static final int CAPACITY_COLOR = 0xFF6080FF;
        
    public MachineItemBlock(MachineBlock block)
    {
        super(block);
        this.setMaxDamage(MAX_DAMAGE);
    }

    @Override
    public int getRGBDurabilityForDisplay(@Nonnull ItemStack stack)
    {
        return CAPACITY_COLOR;
    }
    
}
