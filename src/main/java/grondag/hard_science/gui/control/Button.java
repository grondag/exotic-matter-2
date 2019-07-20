package grondag.hard_science.gui.control;

import static grondag.hard_science.gui.control.GuiControl.*;

import javax.annotation.Nonnull;

import static grondag.fermion.varia.HorizontalAlignment.*;
import static grondag.fermion.varia.VerticalAlignment.*;

import grondag.hard_science.gui.GuiUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.AbstractButtonWidget;

@Environment(EnvType.CLIENT)
public class Button extends AbstractButtonWidget
{
    public int buttonColor = BUTTON_COLOR_ACTIVE;
    public int disabledColor = BUTTON_COLOR_INACTIVE;
    public int hoverColor = BUTTON_COLOR_FOCUS;
    public int textColor = TEXT_COLOR_ACTIVE;
    
    // from 1.12 - not part of 1.14
    protected int buttonId;

    public Button(int buttonId, int x, int y, int width, int height, String buttonText)
    {
        super(x, y, width, height, buttonText);
        this.buttonId = buttonId;
    }

    public void resize(int x, int y, int width, int height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    //TODO: add narration logic
    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        if (this.visible)
        {
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int i = this.getYImage(this.isHovered);
            int color = i == 0 ? this.disabledColor : i == 2 ? this.hoverColor : this.buttonColor;

            MinecraftClient mc = MinecraftClient.getInstance();
            GuiUtil.drawRect(this.x, this.y, this.x + this.width - 1, this.y + this.height - 1, color);
            TextRenderer fontrenderer = mc.textRenderer;
            GuiUtil.drawAlignedStringNoShadow(fontrenderer, this.getMessage(), this.x, this.y, this.width, this.height, this.textColor, CENTER, MIDDLE);
        }
    }
}
