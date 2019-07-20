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

import javax.annotation.Nonnull;

import grondag.fermion.varia.ItemHelper;
import grondag.hard_science.gui.GuiUtil;
import grondag.hard_science.network.C2S_OpenContainerStorageInteraction;
import grondag.hard_science.network.C2S_OpenContainerStorageInteraction.Action;
import grondag.hard_science.simulator.resource.ItemResourceDelegate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;

/**
 * Used as a callback by controls to container to handle mouse input on items
 * within the control.
 */
@Environment(EnvType.CLIENT)
public interface IClickHandler<T> {
    public void handleMouseClick(MinecraftClient mc, int mouseButton, T target);

    public void handleMouseDrag(MinecraftClient mc, int mouseButton, T target);

    public static class StorageClickHandlerStack implements IClickHandler<ItemResourceDelegate> {
        public static final StorageClickHandlerStack INSTANCE = new StorageClickHandlerStack();

        private StorageClickHandlerStack() {
        }

        @Override
        public void handleMouseClick(MinecraftClient mc, int mouseButton, @Nonnull ItemResourceDelegate target) {
            Action action = null;

            boolean isShift = Screen.hasShiftDown();

            ItemStack heldStack = mc.player.inventory.getMainHandStack();

            // if alt/right/middle clicking on same bulkResource, don't count that as a
            // deposit
            if (heldStack != null && !heldStack.isEmpty()
                    && !(ItemHelper.canStacksCombine(heldStack, target.displayStack()) && (Screen.hasAltDown() || mouseButton > 0))) {
                // putting something in
                if (mouseButton == GuiUtil.MOUSE_LEFT && !Screen.hasAltDown()) {
                    action = Action.PUT_ALL_HELD;
                } else {
                    action = Action.PUT_ONE_HELD;
                }
            } else {
                if (mouseButton == GuiUtil.MOUSE_LEFT && !Screen.hasAltDown()) {
                    action = isShift ? Action.QUICK_MOVE_STACK : Action.TAKE_STACK;
                } else if (mouseButton == GuiUtil.MOUSE_MIDDLE || Screen.hasAltDown()) {
                    action = isShift ? Action.QUICK_MOVE_ONE : Action.TAKE_ONE;
                } else if (mouseButton == GuiUtil.MOUSE_RIGHT) {
                    action = isShift ? Action.QUICK_MOVE_HALF : Action.TAKE_HALF;
                }
            }

            if (action != null)
                C2S_OpenContainerStorageInteraction.sendPacket(action, target);
        }

        @Override
        public void handleMouseDrag(MinecraftClient mc, int mouseButton, @Nonnull ItemResourceDelegate target) {
            // doesn't seem like a useful interaction
        }

    }
}
