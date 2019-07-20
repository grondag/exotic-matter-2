package grondag.hard_science.machines.energy;

import grondag.exotic_matter.serialization.IMessagePlus;
import net.minecraft.network.PacketBuffer;

/**
 * Snapshot of dynamic energy component descriptive information for display on client.
 */
public class EnergyComponentStatus implements IMessagePlus
{
    /**
     * See #IEnergyComponent{@link #storedEnergyJoules}
     */
    public long storedEnergyJoules() { return this.storedEnergyJoules; }
    private long storedEnergyJoules;
    
    
    /**
     * See #IEnergyComponent{@link #powerInputWatts}
     */
    public float powerInputWatts() { return this.powerInputWatts; }
    private float powerInputWatts;
    
    /**
     * See {@link IEnergyComponent#powerOutputWatts()}
     */
    public float powerOutputWatts() { return this.powerOutputWatts; }
    private float powerOutputWatts;
    
    public EnergyComponentStatus() {}
        
    public EnergyComponentStatus(IEnergyComponent from)
    {
        this.storedEnergyJoules = from.storedEnergyJoules();
        this.powerInputWatts = from.powerInputWatts();
        this.powerOutputWatts = from.powerOutputWatts();
    }

    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.storedEnergyJoules = pBuff.readVarLong();
        this.powerInputWatts = pBuff.readFloat();
        this.powerOutputWatts = pBuff.readFloat();
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeVarLong(this.storedEnergyJoules);
        pBuff.writeFloat(this.powerInputWatts);
        pBuff.writeFloat(this.powerOutputWatts);
    }
}
