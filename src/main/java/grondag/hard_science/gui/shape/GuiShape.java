package grondag.hard_science.gui.shape;

import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.hard_science.gui.control.Panel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiShape extends Panel
{

    public GuiShape(boolean isVertical)
    {
        super(isVertical);
    }
    
    /** called before control is displayed and whenever modelstate changes */
    public abstract void loadSettings(ISuperModelState modelState);
    
    /** called to detect user changes - return true if model state was changed */
    public abstract boolean saveSettings(ISuperModelState modelState);

}
