package grondag.hard_science.gui.shape;

import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.hard_science.gui.control.Slider;
import grondag.hard_science.gui.control.Toggle;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiStackedPlates extends GuiShape
{

    private Toggle isSlab;
    private Slider thickness;
    
    /** used to detect what changed during mouse handling */
    private boolean lastIsSlab;
    
    @SuppressWarnings("deprecation")
    public GuiStackedPlates(Minecraft mc)
    {
        super(true);
        this.isSlab = new Toggle().setLabel(I18n.translateToLocal("label.half_slab"));
        this.thickness = new Slider(mc, 16, I18n.translateToLocal("label.thickness"), 0.2);
        this.add(isSlab);
        this.add(thickness);
    }
    
    private boolean isSlab(ISuperModelState modelState)
    {
        return modelState.getMetaData() == 7;
    }

    @Override
    public void loadSettings(ISuperModelState modelState)
    {
        this.isSlab.setOn(isSlab(modelState));
        this.thickness.setSelectedIndex(modelState.getMetaData());
        saveLast();
    }

    @Override
    public boolean saveSettings(ISuperModelState modelState)
    {
        int t = this.thickness.getSelectedIndex();
        if(t  != modelState.getMetaData())
        {
            modelState.setMetaData(t);
            return true;
        }
        else
        {
            return false;
        }
    }

    private void saveLast()
    {
        this.lastIsSlab = this.isSlab.isOn();
    }
    
    private void handleMouse()
    {
        if(this.isSlab.isOn())
        {
            // if user clicks slab toggle, set to half thickness
            if(!this.lastIsSlab)
            {
                this.thickness.setSelectedIndex(7);
            }
            else
            {
                this.isSlab.setOn(this.thickness.getSelectedIndex() == 7);
            }
        }
        else
        {
            if(this.thickness.getSelectedIndex() == 7) this.isSlab.setOn(true);
        }
        this.saveLast();
    }
    
    @Override
    public void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        super.handleMouseClick(mc, mouseX, mouseY, clickedMouseButton);
        this.handleMouse();
    }

    @Override
    public void handleMouseDrag(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        super.handleMouseDrag(mc, mouseX, mouseY, clickedMouseButton);
        this.handleMouse();
    }

    
}
