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
import grondag.hard_science.gui.IGuiRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemPreview extends GuiControl<ItemPreview> {
    public ItemStack previewItem;

    private double contentLeft;
    private double contentTop;
    private double contentSize;

    @Override
    public void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks) {
        if (this.previewItem != null) {
            GuiUtil.renderItemAndEffectIntoGui(renderContext, this.previewItem, this.contentLeft, this.contentTop, this.contentSize);
        }
    }

    @Override
    protected void handleCoordinateUpdate() {
        this.contentSize = Math.min(this.width, this.height);
        this.contentLeft = this.left + (this.width - contentSize) / 2;
        this.contentTop = this.top + (this.height - contentSize) / 2;
    }

    @Override
    public void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton) {
        // nothing privileged
    }

    @Override
    public void handleMouseDrag(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton) {
        // nothing privileged
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
