package grondag.hard_science.machines.matbuffer;

import java.util.ArrayList;
import java.util.List;

import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.storage.IResourceContainer;
import net.minecraft.network.PacketBuffer;

public class ClientContainerInfo<T extends StorageType<T>>
{
    public final ArrayList<AbstractResourceWithQuantity<T>> contents = new ArrayList<>();
    public final ArrayList<IResource<T>> blames = new ArrayList<>();
    public long capacity;
    public long used;
    public final T storageType;
    
    public ClientContainerInfo(T storageType)
    {
        this.storageType = storageType;
    }
    
    public void fromBytes(PacketBuffer pBuff)
    {
        this.capacity = pBuff.readVarLong();
        this.used = pBuff.readVarLong();
        this.contents.clear();
        this.blames.clear();
        
        int recordCount = pBuff.readVarInt();
        if(recordCount == 0) return;

        
        for(int i = 0; i < recordCount; i++)
        {
            AbstractResourceWithQuantity<T> rwq = storageType.fromBytesWithQty(pBuff);
            if(rwq.getQuantity() == 0)
                blames.add(rwq.resource());
            else
                contents.add(rwq);
        }
    }
    
    public static <V extends StorageType<V>> void toBytes(IResourceContainer<V> container, PacketBuffer pBuff)
    {
        if(container == null)
        {
            pBuff.writeVarLong(0);
            pBuff.writeVarLong(0);
            pBuff.writeVarInt(0);
            return;
        }
        
        pBuff.writeVarLong(container.getCapacity());
        pBuff.writeVarLong(container.usedCapacity());
        
        List<AbstractResourceWithQuantity<V>> contents = container.findAll();
        List<IResource<V>> blames = container.getRegulator().blames();

        int count = blames.size() + contents.size();
        pBuff.writeVarInt(count);
        if(count == 0) return;
        
        if(!contents.isEmpty())
            contents.forEach(p -> p.storageType().toBytes(p, pBuff));
        
        if(!blames.isEmpty())
            blames.forEach(p -> p.storageType().toBytes(p.withQuantity(0), pBuff));
    }
    
}
