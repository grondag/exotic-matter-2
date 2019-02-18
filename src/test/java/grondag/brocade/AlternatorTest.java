package grondag.exotic_matter;

import org.junit.Test;

import grondag.exotic_matter.world.Alternator;
import grondag.exotic_matter.world.IAlternator;
import net.minecraft.util.math.BlockPos;

public class AlternatorTest
{

    @Test
    public void test()
    {
        IAlternator alt = Alternator.getAlternator(8, 1234);
        
        assert(alt.getAlternate(new BlockPos(1, 1, 1)) == alt.getAlternate(new BlockPos(1, 1, 1)));
        
        // could theoretically fail - seed selected for testing so it doesn't
        assert(alt.getAlternate(new BlockPos(1, 1, 1)) != alt.getAlternate(new BlockPos(2, 1, 1)));
        
        // multiblock test
        alt = Alternator.getAlternator(8, 1234, 4);
        
        // should all match in same multiblock
        assert(alt.getAlternate(new BlockPos(0, 0, 0)) == alt.getAlternate(new BlockPos(1, 1, 1)));
        assert(alt.getAlternate(new BlockPos(0, 0, 0)) == alt.getAlternate(new BlockPos(7, 5, 9)));
        assert(alt.getAlternate(new BlockPos(0, 0, 0)) == alt.getAlternate(new BlockPos(15, 15, 15)));
        
        // should not match in diff multiblocks
        assert(alt.getAlternate(new BlockPos(0, 0, 0)) != alt.getAlternate(new BlockPos(16, 16, 16)));
        assert(alt.getAlternate(new BlockPos(8, 8, 8)) != alt.getAlternate(new BlockPos(-8, -8, -8)));
        
        
    }

}