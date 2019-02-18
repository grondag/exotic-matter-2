package grondag.exotic_matter;

import org.junit.Test;

import grondag.exotic_matter.varia.ColorHelper;
import grondag.exotic_matter.varia.ColorHelper.CMYK;

public class ColorHelperTest
{

    @Test
    public void test()
    {
        assert Math.abs(ColorHelper.red(0x7F00AA) - 0.5F) < 0.01;
                
        CMYK cmyk = ColorHelper.cmyk(0xFFFFFFFF);
        assert cmyk.cyan == 0;
        assert cmyk.yellow == 0;
        assert cmyk.magenta == 0;
        assert cmyk.keyBlack == 0;

        cmyk = ColorHelper.cmyk(0xFF00FF00);
        assert cmyk.cyan == 1;
        assert cmyk.yellow == 1;
        assert cmyk.magenta == 0;
        assert cmyk.keyBlack == 0;

        cmyk = ColorHelper.cmyk(0x0000FFFF);
        assert cmyk.cyan == 1;
        assert cmyk.yellow == 0;
        assert cmyk.magenta == 0;
        assert cmyk.keyBlack == 0;
    }

}
