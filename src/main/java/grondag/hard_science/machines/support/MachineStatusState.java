package grondag.hard_science.machines.support;

import grondag.exotic_matter.serialization.IMessagePlus;
import grondag.exotic_matter.varia.BitPacker64;
import net.minecraft.network.PacketBuffer;


/**
 * Transient machine information - packaged into a class for sending over network.
 * No NBT serialization because it is not persisted.
 * @author grondag
 *
 */
public class MachineStatusState implements IMessagePlus
{
 private static BitPacker64<MachineStatusState> PACKER = new BitPacker64<MachineStatusState>(s -> s.bits, (s, b) -> s.bits = b);
    
    private static BitPacker64<MachineStatusState>.BooleanElement PACKED_REDSTONE_POWER = PACKER.createBooleanElement();
    private static BitPacker64<MachineStatusState>.BooleanElement PACKED_HAS_BACKLOG = PACKER.createBooleanElement();

    private static final int DEFAULT_BITS;
    
    private int maxBacklog;
    private int currentBacklog;
    
    static
    {
        int bits = 0;
        DEFAULT_BITS = bits;
    }
    
    private long bits = DEFAULT_BITS;
    
    
    //////////////////////////////////////////////////////////////////////
    // ACCESS METHODS
    //////////////////////////////////////////////////////////////////////
    
    public boolean hasRedstonePower() { return PACKED_REDSTONE_POWER.getValue(this); }
    public void setHasRestonePower(boolean value) { PACKED_REDSTONE_POWER.setValue(value, this); }
    
    public boolean hasBacklog() { return PACKED_HAS_BACKLOG.getValue(this); }
    public void setHasBacklog(boolean value) { PACKED_HAS_BACKLOG.setValue(value, this); }

    public int getMaxBacklog() { return this.maxBacklog; }
    public void setMaxBacklog(int value) { this.maxBacklog = value; }

    /**
     * Largest number of incomplete work units that has existed since this machine was last idle,
     * either because it was off or because there were no work units to complete.
     * Used to indicate progress.
     */
    public int getCurrentBacklog() { return this.currentBacklog; }
    
    /**
     * Current number of incomplete work units in the current run. Zero if idle.
     */
    public void setCurrentBacklog(int value) { this.currentBacklog = value; }
    
    //////////////////////////////////////////////////////////////////////
    // Serialization stuff
    //////////////////////////////////////////////////////////////////////

    
    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.bits = pBuff.readVarLong();
        if(this.hasBacklog())
        {
            this.maxBacklog = pBuff.readVarInt();
            this.currentBacklog = pBuff.readVarInt();
        }
        
    }
    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeVarLong(this.bits);
        if(this.hasBacklog())
        {
            pBuff.writeVarInt(this.maxBacklog);
            pBuff.writeVarInt(this.currentBacklog);
        }
        
    }
}
