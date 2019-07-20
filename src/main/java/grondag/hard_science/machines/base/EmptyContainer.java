package grondag.hard_science.machines.base;

import javax.annotation.Nonnull;

import grondag.hard_science.machines.support.ContainerLayout;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

/**
 * For when it is useful to track the "open container" without 
 * actually having or displaying any slots.
 */
public class EmptyContainer extends MachineContainer
{
    public EmptyContainer(IInventory playerInventory, MachineTileEntity te, ContainerLayout layout) 
    {
        super(null, te, layout);

        // This container references items out of our own inventory (the 9 slots we hold ourselves)
        // as well as the slots from the player inventory so that the user can transfer items between
        // both inventories. The two calls below make sure that slots are defined for both inventories.
        this.addMachineSlots();
        this.addPlayerSlots(playerInventory, layout);
    }

    @Override
    protected void addPlayerSlots(IInventory playerInventory,ContainerLayout layout)
    {
        // NOOP
    
    }

    @Override
    protected  void addMachineSlots() {};
    

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn)
    {
        return te.canInteractWith(playerIn);
    }

    @Override
    public MachineTileEntity tileEntity()
    {
        return this.te;
    }
}