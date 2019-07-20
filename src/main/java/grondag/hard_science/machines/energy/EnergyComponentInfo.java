package grondag.hard_science.machines.energy;

import grondag.exotic_matter.serialization.IMessagePlus;
import net.minecraft.network.PacketBuffer;

/**
 * Snapshot of static energy component descriptive information for display on client.
 */
public class EnergyComponentInfo implements IMessagePlus
{
    /**
     * See {@link IEnergyComponent#maxStoredEnergyJoules()}
     */
    public long maxStoredEnergyJoules() { return this.maxStoredEnergyJoules; }
    private long maxStoredEnergyJoules;
    
    /**
     * See {@link IEnergyComponent#maxPowerInputWatts()}
     */
    public float maxPowerInputWatts() { return this.maxPowerInputWatts; }
    private float maxPowerInputWatts;
    
    /**
     * See {@link IEnergyComponent#maxPowerOutputWatts()}
     */
    public float maxPowerOutputWatts() { return this.maxPowerOutputWatts; }
    private float maxPowerOutputWatts;
    
    public EnergyComponentInfo() {}
    
    public EnergyComponentInfo(IEnergyComponent from)
    {
        this.maxStoredEnergyJoules = from.maxStoredEnergyJoules();
        this.maxPowerInputWatts = from.maxPowerInputWatts();
        this.maxPowerOutputWatts = from.maxPowerOutputWatts();
    }

    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.maxStoredEnergyJoules = pBuff.readVarLong();
        this.maxPowerInputWatts = pBuff.readFloat();
        this.maxPowerOutputWatts = pBuff.readFloat();
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeVarLong(this.maxStoredEnergyJoules);
        pBuff.writeFloat(this.maxPowerInputWatts);
        pBuff.writeFloat(this.maxPowerOutputWatts);        
    }
}
