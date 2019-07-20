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

import java.util.List;

import grondag.exotic_matter.model.texture.ITexturePalette;
import grondag.exotic_matter.model.texture.TextureRotationType;
import grondag.exotic_matter.world.Rotation;
import grondag.exotic_matter.world.WorldInfo;
import grondag.hard_science.gui.GuiUtil;
import grondag.hard_science.gui.IGuiRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TexturePicker extends TabBar<ITexturePalette> {
    public int borderColor = 0xFFFFFFFF;
    public int baseColor = 0;
    public boolean renderAlpha = true;

    public TexturePicker(List<ITexturePalette> items, double left, double top) {
        super(items);
        this.setItemsPerRow(8);
    }

    @Override
    protected void drawItem(ITexturePalette item, Minecraft mc, RenderItem itemRender, double left, double top, float partialTicks, boolean isHighlighted) {

        int size = this.actualItemPixels();

        // if texture is translucent provide a background
        if (this.renderAlpha)
            GuiUtil.drawRect(left, top, left + size, top + size, this.baseColor);

        Rotation rotation = item.rotation().rotationType() == TextureRotationType.RANDOM ? Rotation.VALUES[(int) ((WorldInfo.currentTimeMillis() >> 11) & 3)]
                : item.rotation().rotation;

        TextureAtlasSprite tex = item.getSampleSprite();
        GuiUtil.drawTexturedRectWithColor(left, top, this.zLevel, tex, size, size, this.borderColor, item.textureScale(), rotation, renderAlpha);
    }

    @Override
    protected void setupItemRendering() {
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected void drawToolTip(ITexturePalette item, IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks) {
        renderContext.drawToolTip(item.displayName(), mouseX, mouseY);
    }
}
