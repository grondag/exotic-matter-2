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
package grondag.xm.relics.placement;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.xm.virtual.ExcavationRenderManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;

@API(status = Status.DEPRECATED)
@Deprecated
public abstract class PlacementPreviewRenderer {
    private PlacementPreviewRenderer() {
    }

    public static void renderPreview(float tickDelta) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        ItemStack stack = PlacementItem.getHeldPlacementItem(player);

        if (player.isSneaking())
            if (stack != null) {
                PlacementItem placer = (PlacementItem) stack.getItem();
                PlacementResult result = PlacementHandler.predictPlacementResults(player, stack, placer);
                if (result.builder() != null)
                    result.builder().renderPreview(tickDelta, player);
            }

        ExcavationRenderManager.render(tickDelta, player);
    }
}
