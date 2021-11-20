/*
 * Copyright © Original Authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.virtual;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Internal
public class VirtualBlockEntityRenderer implements BlockEntityRenderer<VirtualBlockEntityWithRenderer> {
	public VirtualBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) { }

	@Override
	public void render(VirtualBlockEntityWithRenderer be, float f, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, int j) {
		if (!be.isVirtual() || !((VirtualBlockEntity) be).isVisible()) {
		}

		// TODO: actually render - use 1.12 SuperBlockTESR as starting point
	}
}
