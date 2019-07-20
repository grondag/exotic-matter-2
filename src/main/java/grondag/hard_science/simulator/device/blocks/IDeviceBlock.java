package grondag.hard_science.simulator.device.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.exotic_matter.world.PackedBlockPos;
import grondag.hard_science.simulator.device.DeviceManager;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.Channel;
import grondag.hard_science.simulator.transport.endpoint.Port;
import net.minecraft.util.EnumFacing;

/**
 * In-world delegate of a device at a given block
 * position. Some devices could have multiple delegates.
 * Devices should register delegates when connected to 
 * to the device manager.
 */
public interface IDeviceBlock
{
    public long packedBlockPos();
    public int dimensionID();
    public IDevice device();
    
    /**
     * Finds ports on the given face that match the given values for
     * storage type, channel and carrier level. Returns null if none.<p>
     * 
     * The value for channel can be a real value, or it can be
     * {@link Channel#CONFIGURABLE_NOT_SET} to find ports
     * that could be configured to match a new carrier circuit that
     * hasn't been created yet. <p>
     * 
     * If channel given is a real value (>=0) then will also return ports 
     * on that have channel == {@link Channel#CONFIGURABLE_NOT_SET}, because
     * those could be configured to match the existing channel.
     */
    @Nullable
    public <T extends StorageType<T>> Iterable<Port<T>> getConnectablePorts(Port<T> port, EnumFacing actualFace);
    
    /**
     * Called by device block manager immediately after this block is removed from the world.
     * Use this to tear down connections, notify neighbors as needed, etc.
     */
    public void onRemoval();
    
    @Nullable
    public default IDeviceBlock getNeighbor(EnumFacing actualFace)
    {
        return DeviceManager.blockManager().getBlockDelegate(
                this.dimensionID(), 
                PackedBlockPos.offset(this.packedBlockPos(), actualFace));
    }
    
    public default String description()
    {
        return String.format("Device Block for %s @ %d,%d,%d in dim %d", 
                this.device().machineName(),
                PackedBlockPos.getX(this.packedBlockPos()),
                PackedBlockPos.getY(this.packedBlockPos()),
                PackedBlockPos.getZ(this.packedBlockPos()),
                this.dimensionID());
    }
    
    public <T extends StorageType<T>> BlockPortManager<T> portManager(T storageType);
}
