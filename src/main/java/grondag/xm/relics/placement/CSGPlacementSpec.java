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

import java.util.function.BooleanSupplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fermion.position.BlockRegion;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

@API(status = Status.DEPRECATED)
@Deprecated
public class CSGPlacementSpec extends VolumetricPlacementSpec {
    public CSGPlacementSpec(ItemStack placedStack, PlayerEntity player, PlacementPosition pPos) {
        super(placedStack, player, pPos);
    }

    @Override
    protected boolean doValidate() {
        // TODO: Logic will be similar to VolumetricBuilder
        return false;
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void drawSelection(Tessellator tessellator, BufferBuilder bufferBuilder) {
        // TODO Auto-generated method stub

    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void drawPlacement(Tessellator tessellator, BufferBuilder bufferBuilder, PlacementPreviewRenderMode previewMode) {
        // TODO Auto-generated method stub

    }

    @Override
    public BooleanSupplier worldTask(ServerPlayerEntity player) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BlockRegion region() {
        // TODO Auto-generated method stub
        return null;
    }
}
