package grondag.hard_science.gui.shape;

import grondag.exotic_matter.model.state.ISuperModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSimpleShape extends GuiShape
{
    public GuiSimpleShape(boolean isVertical)
    {
        super(isVertical);
    }

    @Override
    public void loadSettings(ISuperModelState modelState)
    {
        //ignore
    }

    @Override
    public boolean saveSettings(ISuperModelState modelState)
    {
        return false;
    }

}
