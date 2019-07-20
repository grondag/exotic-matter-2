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
package grondag.hard_science.machines.base;

import grondag.hard_science.machines.support.ContainerLayout;
import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;

public abstract class MachineContainer extends Container {

    protected MachineTileEntity te;
    public final ContainerLayout layout;

    public MachineContainer(Inventory playerInventory, MachineTileEntity te, ContainerLayout layout) {
        // FIXME: paramters are made up - new for 1.14
        super(null, 27);
        this.te = te;
        this.layout = layout;

        // This container references items out of our own inventory (the 9 slots we hold
        // ourselves)
        // as well as the slots from the player inventory so that the user can transfer
        // items between
        // both inventories. The two calls below make sure that slots are defined for
        // both inventories.
        this.addMachineSlots();
        this.addPlayerSlots(playerInventory, layout);
    }

    protected void addPlayerSlots(Inventory playerInventory, ContainerLayout layout) {
        // Slots for the hotbar - start at slot 0
        for (int col = 0; col < 9; ++col) {
            int x = layout.playerInventoryLeft + col * layout.slotSpacing;
            int y = layout.playerInventoryTop + layout.slotSpacing * 2 + 16 + layout.externalMargin;
            this.addSlot(new Slot(playerInventory, col, x, y));
        }

        // Slots for the main inventory - start at slot 9;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int x = layout.playerInventoryLeft + col * layout.slotSpacing;
                int y = row * layout.slotSpacing + layout.playerInventoryTop;
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, x, y));
            }
        }

    }

    // Example of usage, courtesy McJty:
    // IItemHandler itemHandler =
    // this.te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    // int x = 10;
    // int y = 6;
    //
    // // Add our own slots
    // int slotIndex = 0;
    // for (int i = 0; i < itemHandler.getSlots(); i++) {
    // addSlotToContainer(new SlotItemHandler(itemHandler, slotIndex, x, y));
    // slotIndex++;
    // x += 18;
    // }

    protected void addMachineSlots() {
    };

    @Override
    public boolean canUse(PlayerEntity playerIn) {
        return te.canUse(playerIn);

    }

    public MachineTileEntity tileEntity() {
        return this.te;
    }

    public static MachineTileEntity getOpenContainerTileEntity(PlayerEntity player) {
        Container container = player.container;

        if (container == null || !(container instanceof MachineContainer))
            return null;

        return ((MachineContainer) container).tileEntity();
    }

}
