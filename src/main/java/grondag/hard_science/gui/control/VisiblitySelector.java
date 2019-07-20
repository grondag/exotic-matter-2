/*******************************************************************************
 * Copyright 2019 grondag
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.hard_science.gui.control;

import static grondag.exotic_matter.varia.HorizontalAlignment.*;
import static grondag.exotic_matter.varia.VerticalAlignment.*;

import grondag.hard_science.gui.GuiUtil;
import grondag.hard_science.gui.IGuiRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VisiblitySelector extends GuiControl<VisiblitySelector> {
    private final VisibilityPanel target;

    private double buttonHeight;

    public VisiblitySelector(VisibilityPanel target) {
        this.target = target;
    }

    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks) {
        double y = this.top;

        int hoverIndex = this.getButtonIndex(mouseX, mouseY);

        for (int i = 0; i < target.size(); i++) {
            String label = target.getLabel(i);

            GuiUtil.drawBoxRightBottom(this.left, y, this.right, y + this.buttonHeight, 1, BUTTON_COLOR_ACTIVE);
            int buttonColor = i == hoverIndex ? BUTTON_COLOR_FOCUS : i == this.target.getVisiblityIndex() ? BUTTON_COLOR_ACTIVE : BUTTON_COLOR_INACTIVE;
            GuiUtil.drawRect(this.left + 2, y + 2, this.right - 2, y + this.buttonHeight - 2, buttonColor);

            int textColor = i == hoverIndex ? TEXT_COLOR_FOCUS : i == this.target.getVisiblityIndex() ? TEXT_COLOR_ACTIVE : TEXT_COLOR_INACTIVE;
            GuiUtil.drawAlignedStringNoShadow(renderContext.fontRenderer(), label, (float) this.left, (float) y, (float) this.width, (float) this.buttonHeight,
                    textColor, CENTER, MIDDLE);

            y += this.buttonHeight;
        }

    }

    private int getButtonIndex(int mouseX, int mouseY) {
        this.refreshContentCoordinatesIfNeeded();
        if (mouseX < this.left || mouseX > this.right || this.buttonHeight == 0)
            return NO_SELECTION;

        int selection = (int) ((mouseY - this.top) / this.buttonHeight);

        return (selection < 0 || selection >= this.target.size()) ? NO_SELECTION : selection;
    }

    @Override
    protected void handleCoordinateUpdate() {
        if (this.target.size() > 0) {
            this.buttonHeight = this.height / this.target.size();
        }
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton) {
        int clickIndex = this.getButtonIndex(mouseX, mouseY);

        if (clickIndex != NO_SELECTION && clickIndex != this.target.getVisiblityIndex()) {
            this.target.setVisiblityIndex(clickIndex);
            GuiUtil.playPressedSound(mc);
        }
    }

    @Override
    protected void handleMouseDrag(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton) {
        // ignore
    }

    @Override
    protected void handleMouseScroll(int mouseX, int mouseY, int scrollDelta) {
        // ignore
    }

    @Override
    public void drawToolTip(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks) {
        // TODO Auto-generated method stub

    }

}
