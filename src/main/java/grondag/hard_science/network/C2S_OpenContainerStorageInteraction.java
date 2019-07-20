package grondag.hard_science.network;

import javax.annotation.Nonnull;

import grondag.hard_science.machines.base.MachineContainer;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.support.MachineStorageContainer;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.ItemResourceDelegate;
import grondag.hard_science.simulator.resource.ItemResourceWithQuantity;
import grondag.hard_science.simulator.storage.ItemStorageListener;
import grondag.hard_science.simulator.transport.management.LogisticsService;
import grondag.pragmatics.impl.PlayerModifierAccess;
import grondag.xm2.Xm;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

/**
 * Sent when player interacts with the GUI of an IStorage (vs container slots).
 * IStorage has no concept of slots.
 */
public class C2S_OpenContainerStorageInteraction
{
    public static enum Action
    {
        /** move targeted stack to player's inventory */
        QUICK_MOVE_STACK,
        
        /** move half of targeted item, up to half a stack, to player's inventory */
        QUICK_MOVE_HALF,
        
        /** move one of targeted item to player's inventory */
        QUICK_MOVE_ONE,
        
        /** if player has an empty hand or holds the target item, add one to held */
        TAKE_ONE,
        
        /** if player has an empty hand, take half of targeted item, up to half a stack*/
        TAKE_HALF,
        
        /** if player has an empty hand, take full stack of targeted item */
        TAKE_STACK,
        
        /** if player holds a stack, deposit one of it into storage. target is ignored/can be null */
        PUT_ONE_HELD,
        
        /** if player holds a stack, deposit all of it into storage. target is ignored/can be null */
        PUT_ALL_HELD
    }
    
    public static final Identifier ID = new Identifier(Xm.MODID, "POCSI");
    
    @Environment(EnvType.CLIENT)
    public static void sendPacket(Action action, ItemResourceDelegate target) {
        final PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeEnumConstant(action);
        buf.writeInt(target.handle());
        ClientSidePacketRegistry.INSTANCE.sendToServer(ID, buf);
    }
    
    public static void accept(PacketContext context, PacketByteBuf buf) {
        final Action action = buf.readEnumConstant(Action.class);
        final int resourceHandle = buf.readInt();
        final ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
        
        MachineTileEntity te = MachineContainer.getOpenContainerTileEntity(player);
        if(te == null || !(te instanceof MachineTileEntity)) return;
        
        if(!(player.container instanceof MachineStorageContainer)) return;
        
        MachineStorageContainer container = (MachineStorageContainer)player.container;
                
        ItemStorageListener listener = container.getItemListener(player);
        
        if(listener == null || listener.isDead()) return;
        
        ItemResource targetResource = (ItemResource) listener.getResourceForHandle(resourceHandle);
        
        switch(action)
        {
            case PUT_ALL_HELD:
                doPut(false, player, listener);
                return;
            
            case PUT_ONE_HELD:
                doPut(true, player, listener);
                return;
            
            case QUICK_MOVE_HALF:
            {
                if(targetResource == null) return;
                int toMove = Math.max(1, (int) Math.min(targetResource.sampleItemStack().getMaxCount() / 2, listener.getQuantityStored(targetResource) / 2));
                doQuickMove(toMove, player, targetResource, listener);
                return;
            }
                
            case QUICK_MOVE_ONE:
                if(targetResource == null) return;
                doQuickMove(1, player, targetResource, listener);
                return;

            case QUICK_MOVE_STACK:
            {
                if(targetResource == null) return;
                int toMove = (int) Math.min(targetResource.sampleItemStack().getMaxCount(), listener.getQuantityStored(targetResource));
                doQuickMove(toMove, player, targetResource, listener);
                return;
            }

            case TAKE_ONE:
                doTake(1, player, targetResource, listener);
                return;
           
            case TAKE_HALF:
            {
                if(targetResource == null) return;
                int toTake = Math.max(1, (int) Math.min(targetResource.sampleItemStack().getMaxCount() / 2, listener.getQuantityStored(targetResource) / 2));
                doTake(toTake, player, targetResource, listener);
                return;
            }

            case TAKE_STACK:
            {
                if(targetResource == null) return;
                int toTake = (int) Math.min(targetResource.sampleItemStack().getMaxCount(), listener.getQuantityStored(targetResource));
                doTake(toTake, player, targetResource, listener);
                return;
            }
            
            default:
                return;
        }
    }
    

    
    private static void doPut(boolean single, ServerPlayerEntity player, ItemStorageListener listener)
    {
        ItemStack heldStack = player.inventory.getMainHandStack();
        
        if(heldStack != null && !heldStack.isEmpty())
        {
            ItemResourceWithQuantity heldResource = ItemResourceWithQuantity.fromStack(heldStack);
            int added = 0;
            try
            {
                added = LogisticsService.ITEM_SERVICE.executor.submit( () ->
                {
                    return (int) listener.add(heldResource.resource(), single ? 1 : heldStack.getCount(), false, null);
                }, true).get();
            }
            catch (Exception e)
            {
                Xm.LOG.error("Error in open container item handling", e);
            }
            if(added > 0) heldStack.decrement(added);
            player.updateHeldItem();
        }
        return;        
    }

    private static void doQuickMove(int howMany, ServerPlayerEntity player, ItemResource targetResource, ItemStorageListener listener)
    {
        if(howMany == 0) return;
        int toMove = 0;
        try
        {
            toMove = LogisticsService.ITEM_SERVICE.executor.submit( () ->
            {
                return (int) listener.takeUpTo(targetResource, howMany, false, null);
            }, true).get();
        }
        catch (Exception e)
        {
            Xm.LOG.error("Error in open container item handling", e);
        }
        
        if(toMove == 0) return;
        ItemStack newStack = targetResource.sampleItemStack();
        newStack.setCount(toMove);
        player.inventory.addItemStackToInventory(newStack);
        if(!newStack.isEmpty())
        {
            InventoryHelper.spawnItemStack(player.world, player.posX, player.posY, player.posZ, newStack);;
        }
    }
    
    /**
     * Note: assumes player held item is empty and does not check for this.
     */
    private static void doTake(int howMany, ServerPlayerEntity player, ItemResource targetResource, ItemStorageListener listener)
    {
        if(howMany == 0) return;

        ItemStack heldStack = player.inventory.getMainHandStack();
        
        if(heldStack != null && !heldStack.isEmpty())
        {
            boolean heldStackMatchesTarget = targetResource.isStackEqual(heldStack);
            if(!heldStackMatchesTarget) return;
            if(heldStack.getCount() >= heldStack.getMaxStackSize()) return;
            howMany = Math.min(howMany, heldStack.getMaxStackSize() - heldStack.getCount());
        }
        else
        {
            howMany = Math.min(howMany, targetResource.sampleItemStack().getMaxStackSize());
        }
        
        final int finalHowMany = howMany;
        
        int toAdd = 0;
        try
        {
            toAdd = LogisticsService.ITEM_SERVICE.executor.submit( () ->
            {
                return (int) listener.takeUpTo(targetResource, finalHowMany, false, null);
            }, true).get();
        }
        catch (Exception e)
        {
            Xm.LOG.error("Error in open container item handling", e);
        }
            
        if(toAdd == 0) return;
        
        if(heldStack != null && !heldStack.isEmpty())
        {
            heldStack.grow(toAdd);
        }
        else
        {
            ItemStack newStack = targetResource.sampleItemStack();
            newStack.setCount(toAdd);
            player.inventory.setItemStack(newStack);
        }
        player.updateItem();
    }
}
