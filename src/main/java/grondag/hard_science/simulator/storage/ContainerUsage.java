package grondag.hard_science.simulator.storage;

public enum ContainerUsage
{
    /**
     * All input and output is published to domain event manager
     * and contents are registered with domain storage manager.
     * (But only while the device is connected to the device manager.)<p>
     * 
     * All updates must occur on service thread for storage type.
     * (Again, only while the device is connected to the device manager.)
     * This includes actions on world thread! They must be
     * scheduled on service thread and wait for completion!<p>
     * 
     * Because all updates must occur on the same thread,
     * storage containers generally do not require synchronization.
     */
    STORAGE(true),

    /**
     * For holding device input locally after it is received
     * from the stroage/transport network. Contents are not 
     * listed in the domain storage manager and no storage 
     * events are fired.<p>
     * 
     * Updates to this object should be synchronized because
     * can occur on different threads.<p>
     */
    PRIVATE_BUFFER_IN(false),
    
    /**
     * For holding device output locally before it is put
     * on the storage/transport network. Contents are not 
     * listed in the domain storage manager and no storage 
     * events are fired.<p>
     * 
     * Updates to this object should be synchronized because
     * can occur on different threads.<p>
     */
    PRIVATE_BUFFER_OUT(false),
    
    
    /**
     * Dedicated (usually) small storage for holding device 
     * output if space isn't available on the public network.
     * Contents ARE listed in the domain storage manager and 
     * storage events ARE fired. Access must occur on service thread.<p>
     * 
     * Functional difference from STORAGE is the logistics
     * service won't try to store anything here. And inventory
     * producer will try draw from public output buffers before
     * pulling from public storage.<p>
     */
    PUBLIC_BUFFER_OUT(true);
    
    
    /**
     * True if contents are listed with the storage manager
     * and storage events will be fired WHILE CONNECTED
     */
    public final boolean isListed;
    
    
    private ContainerUsage(boolean isListed)
    {
        this.isListed = isListed;
    }
}
