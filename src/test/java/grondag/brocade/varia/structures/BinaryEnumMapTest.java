package grondag.exotic_matter.varia.structures;

import static org.junit.Assert.*;

import org.junit.Test;

import grondag.exotic_matter.varia.structures.BinaryEnumSet;
import net.minecraft.util.BlockRenderLayer;

public class BinaryEnumMapTest
{

    @Test
    public void test()
    {
        BinaryEnumSet<BlockRenderLayer> bem = new BinaryEnumSet<BlockRenderLayer>(BlockRenderLayer.class);
        
        int flags = bem.getFlagForValue(BlockRenderLayer.CUTOUT);
        
        assertTrue(bem.isFlagSetForValue(BlockRenderLayer.CUTOUT, flags));
        assertFalse(bem.isFlagSetForValue(BlockRenderLayer.SOLID, flags));
        
        flags = bem.getFlagsForIncludedValues(BlockRenderLayer.CUTOUT, BlockRenderLayer.SOLID);
        assertTrue(bem.isFlagSetForValue(BlockRenderLayer.CUTOUT, flags));
        assertTrue(bem.isFlagSetForValue(BlockRenderLayer.SOLID, flags));
        assertFalse(bem.isFlagSetForValue(BlockRenderLayer.TRANSLUCENT, flags));
        
        flags = bem.getFlagsForIncludedValues(BlockRenderLayer.CUTOUT, BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT, BlockRenderLayer.CUTOUT);
        assertTrue(bem.isFlagSetForValue(BlockRenderLayer.CUTOUT, flags));
        assertTrue(bem.isFlagSetForValue(BlockRenderLayer.SOLID, flags));
        assertTrue(bem.isFlagSetForValue(BlockRenderLayer.TRANSLUCENT, flags));
        assertFalse(bem.isFlagSetForValue(BlockRenderLayer.CUTOUT_MIPPED, flags));
        
        flags = bem.getFlagsForIncludedValues(BlockRenderLayer.values());
        assertArrayEquals(bem.getValuesForSetFlags(flags), BlockRenderLayer.values());
        
        assertTrue(bem.combinationCount() == 16);

    }

}