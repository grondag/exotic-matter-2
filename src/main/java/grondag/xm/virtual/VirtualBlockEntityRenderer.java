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
package grondag.xm.virtual;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Internal
public class VirtualBlockEntityRenderer implements BlockEntityRenderer<VirtualBlockEntityWithRenderer> {

	public VirtualBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) { }

	@Override
	public void render(VirtualBlockEntityWithRenderer be, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
		if (!be.isVirtual() || !((VirtualBlockEntity) be).isVisible())
			return;

		// TODO: actually render - use 1.12 SuperBlockTESR as starting point
	}
}
