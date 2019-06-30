package grondag.xm2.terrain;

import net.minecraft.block.Block;
import net.minecraft.block.Material;

public class DepletedFluidBlock extends Block {
    public DepletedFluidBlock() {
        super(Settings.of(Material.STRUCTURE_VOID));
    }
}
