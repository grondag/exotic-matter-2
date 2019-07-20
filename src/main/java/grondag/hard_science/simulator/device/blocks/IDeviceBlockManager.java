package grondag.hard_science.simulator.device.blocks;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.endpoint.Port;

/**
 * Manages the device block delegates for a device.
 * 
 * Hierarchical structure
 *  Device
 *      DeviceBlock(s)
 *          Connector Instance : Connection Instance
 *              Port Instance : Transport Node
 * 
 */
public interface IDeviceBlockManager
{
    /**
     * All device blocks for this device.
     */
    public Collection<IDeviceBlock> blocks();

    /** 
     * Will be called by owning device when added to world.
     * Should register all device blocks with DeviceWorldManager.
     * Happens before transport manager connect.
     */
    public void connect();

    /** 
     * Will be called by owning device when removed from world. 
     * Should unregister all device blocks with DeviceWorldManager.
     */
    public void disconnect();

    /**
     * Get all currently attached ports on this device 
     * with the given StorageType, irrespective of channel
     *
     * @param storageType  Matches ports of this type.
     */
    public <T extends StorageType<T>> List<Port<T>> getAttachedPorts(T storageType);

}
