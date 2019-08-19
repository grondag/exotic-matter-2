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

import grondag.xm.api.item.XmItem;
import grondag.xm.api.modelstate.ModelState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public abstract class SingleStackPlacementSpec extends AbstractPlacementSpec {
    /**
     * Stack that should be placed in the world. Populated during
     * {@link #doValidate()} Default is AIR (for excavations) if not set.
     */
    protected ItemStack outputStack = Items.AIR.getStackForRender();

    protected SingleStackPlacementSpec(ItemStack placedStack, PlayerEntity player, PlacementPosition pPos) {
        super(placedStack, player, pPos);
    }

    @Override
    protected ModelState previewModelState() {
        return this.outputStack == null ? super.previewModelState() : XmItem.modelState(this.outputStack);
    }
}
