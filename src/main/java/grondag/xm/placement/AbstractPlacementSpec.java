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
package grondag.xm.placement;

import static grondag.xm.placement.PlacementPreviewRenderMode.OBSTRUCTED;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import grondag.xm.api.collision.CollisionDispatcher;
import grondag.xm.api.item.XmItem;
import grondag.xm.api.modelstate.ModelState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

abstract class AbstractPlacementSpec implements IPlacementSpec {
    /**
     * Stack player is holding to do the placement.
     */
    private final ItemStack heldStack;

    protected final PlacementItem placementItem;
    protected final PlayerEntity player;
    protected final PlacementPosition pPos;
    protected @Nullable Boolean isValid = null;
    protected final TargetMode selectionMode;
    protected final boolean isExcavation;
    protected final boolean isVirtual;
    protected final boolean isSelectionInProgress;

    /**
     * From stack but adjusted to a value that makes sense if we are excavating.
     */
    protected final FilterMode effectiveFilterMode;

    protected AbstractPlacementSpec(ItemStack heldStack, PlayerEntity player, PlacementPosition pPos) {
        this.heldStack = heldStack;
        this.player = player;
        this.pPos = pPos;
        this.placementItem = (PlacementItem) heldStack.getItem();
        this.isSelectionInProgress = this.placementItem.isFixedRegionSelectionInProgress(heldStack);
        this.selectionMode = this.placementItem.getTargetMode(heldStack);
        this.isExcavation = this.placementItem.isExcavator(heldStack);
        this.isVirtual = this.placementItem.isVirtual(heldStack);

        FilterMode filterMode = this.placementItem.getFilterMode(heldStack);

        // if excavating, adjust filter mode if needed so that it does something
        if (isExcavation && filterMode == FilterMode.FILL_REPLACEABLE)
            filterMode = FilterMode.REPLACE_SOLID;
        this.effectiveFilterMode = filterMode;
    }

    /**
     * Type-specific logic for {@link #validate()}. Populate obstacles if
     * applicable.
     * 
     * @return Same semantics as {@link #validate()}
     */
    protected abstract boolean doValidate();

    @Override
    public final boolean validate() {
        if (isValid == null) {
            isValid = doValidate();
        }
        return isValid;
    }

    @Override
    public boolean isExcavation() {
        return this.isExcavation;
    }

    @Environment(EnvType.CLIENT)
    protected abstract void drawSelection(Tessellator tessellator, BufferBuilder bufferBuilder);

    @Environment(EnvType.CLIENT)
    protected abstract void drawPlacement(Tessellator tessellator, BufferBuilder bufferBuilder, PlacementPreviewRenderMode previewMode);

    /**
     * Location used for {@link #drawPlacementPreview(Tessellator, BufferBuilder)}.
     * Override if the placement region does not include target position in
     * {@link #pPos}. Will generally not be used for excavations.
     */
    @Environment(EnvType.CLIENT)
    protected BlockPos previewPos() {
        return this.pPos.inPos;
    }

    /**
     * The model state (if applies) that should be used to render placement preview.
     * Override with context-dependent version if available.
     */
    @Nullable
    protected ModelState previewModelState() {
        return XmItem.modelState(this.heldStack);
    }

    public ItemStack placedStack() {
        return heldStack;
    }

    public PlacementPosition placementPosition() {
        return this.pPos;
    }

    public PlayerEntity player() {
        return this.player;
    }

    /**
     * Draw single-block sample to show shape/orientation of block to be be placed.
     * Does not render for excavations.
     */
    @Environment(EnvType.CLIENT)
    protected void drawPlacementPreview(Tessellator tessellator, BufferBuilder bufferBuilder) {
        if (this.previewPos() == null || this.isExcavation)
            return;

        GlStateManager.disableDepthTest();

        ModelState placementModelState = this.previewModelState();
        if (placementModelState == null) {
            // No model state, draw generic box
            BlockPos pos = this.previewPos();
            bufferBuilder.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
            WorldRenderer.buildBoxOutline(bufferBuilder, pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, OBSTRUCTED.red,
                    OBSTRUCTED.green, OBSTRUCTED.blue, 1f);
            tessellator.draw();
        } else {
            // Draw collision boxes
            GlStateManager.lineWidth(1.0F);
            for (Box blockAABB : CollisionDispatcher.boxesFor(placementModelState)) {
                bufferBuilder.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
                WorldRenderer.buildBoxOutline(bufferBuilder, blockAABB.minX, blockAABB.minY, blockAABB.minZ, blockAABB.maxX, blockAABB.maxY, blockAABB.maxZ, 1f,
                        1f, 1f, 1f);
                tessellator.draw();
            }
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public final void renderPreview(float tickDelta, ClientPlayerEntity player) {
        this.validate();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBufferBuilder();

        double d0 = player.prevRenderX + (player.x - player.prevRenderX) * tickDelta;
        double d1 = player.prevRenderY + (player.y - player.prevRenderY) * tickDelta;
        double d2 = player.prevRenderZ + (player.z - player.prevRenderZ) * tickDelta;

        bufferBuilder.setOffset(-d0, -d1, -d2);

        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture();
        GlStateManager.depthMask(false);

        // prevent z-fighting
        GlStateManager.enablePolygonOffset();
        GlStateManager.polygonOffset(-1, -1);

        if (this.isSelectionInProgress) {
            this.drawSelection(tessellator, bufferBuilder);
        } else if (this.isExcavation) {
            this.drawPlacement(tessellator, bufferBuilder, PlacementPreviewRenderMode.EXCAVATE);
        } else if (this.isValid) {
            this.drawPlacement(tessellator, bufferBuilder, PlacementPreviewRenderMode.PLACE);
        } else {
            this.drawPlacement(tessellator, bufferBuilder, PlacementPreviewRenderMode.OBSTRUCTED);
        }

        bufferBuilder.setOffset(0, 0, 0);

        GlStateManager.disablePolygonOffset();
        GlStateManager.enableDepthTest();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
    }
}
