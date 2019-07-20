package grondag.hard_science.machines.matbuffer;

import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import net.minecraft.network.PacketBuffer;

/**
 * Snapshot of material buffer descriptive information for display on client.
 */
public class ClientBufferInfo
{
    public final ClientContainerInfo<StorageTypeStack> itemInput 
        = new ClientContainerInfo<>(StorageType.ITEM);
    
    public final ClientContainerInfo<StorageTypeStack> itemOutput 
        = new ClientContainerInfo<>(StorageType.ITEM);

    public final ClientContainerInfo<StorageTypeFluid> fluidInput 
        = new ClientContainerInfo<>(StorageType.FLUID);

    public final ClientContainerInfo<StorageTypeFluid> fluidOutput 
        = new ClientContainerInfo<>(StorageType.FLUID);


    public static void toBytes(BufferManager bufferManager, PacketBuffer pBuff)
    {
        ClientContainerInfo.toBytes(bufferManager.itemInput(), pBuff);
        ClientContainerInfo.toBytes(bufferManager.itemOutput(), pBuff);
        ClientContainerInfo.toBytes(bufferManager.fluidInput(), pBuff);
        ClientContainerInfo.toBytes(bufferManager.fluidOutput(), pBuff);
    }

    public void fromBytes(PacketBuffer pBuff)
    {
        this.itemInput.fromBytes(pBuff);
        this.itemOutput.fromBytes(pBuff);
        this.fluidInput.fromBytes(pBuff);
        this.fluidOutput.fromBytes(pBuff);
    }

    public boolean hasFailureCause()
    {
        return !(this.itemInput.blames.isEmpty() && this.fluidInput.blames.isEmpty());
    }

}
