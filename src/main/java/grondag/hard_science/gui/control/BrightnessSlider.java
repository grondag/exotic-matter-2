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

import grondag.hard_science.gui.GuiUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BrightnessSlider extends Slider {
    private static final String LABEL = "Brightness";

    public BrightnessSlider(Minecraft mc) {
        super(mc, 16, LABEL, 0.22);
        this.choiceWidthFactor = 0.1;
    }

    /** alias for readability */
    public void setBrightness(int brightness) {
        this.setSelectedIndex(brightness & 0xF);
    }

    /** alias for readability */
    public int getBrightness() {
        return this.getSelectedIndex();
    }

    @Override
    protected void drawChoice(Minecraft mc, RenderItem itemRender, float partialTicks) {
        int color = 0xFFFECE | (((255 * this.selectedTabIndex / 15) & 0xFF) << 24);

        GuiUtil.drawRect(this.labelRight, this.top, this.labelRight + this.choiceWidth, this.bottom, color);

        int textColor = this.selectedTabIndex > 6 ? 0xFF000000 : 0xFFFFFFFF;

        GuiUtil.drawAlignedStringNoShadow(mc.fontRenderer, Integer.toString(this.selectedTabIndex), this.labelRight, this.top, this.choiceWidth, this.height,
                textColor, HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE);
    }

}
