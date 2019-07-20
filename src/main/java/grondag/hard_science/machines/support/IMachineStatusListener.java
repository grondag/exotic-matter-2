package grondag.hard_science.machines.support;

import grondag.exotic_matter.serialization.IMessagePlus;
import grondag.hard_science.machines.base.MachineTileEntity;

/**
 * For dispatching machine status updates to client for use in GUI or HUD.
 * Is based on tile entity, not simulation, because player can only be looking
 * at machine that are loaded and thus have an active tile entity.
 * This allows support for machines that aren't backed by the simulation.
 */
public interface IMachineStatusListener<T extends IMessagePlus>
{

    /**
     * Sends new status information. This replaces prior status.
     * Machine update packets are small so we don't bother with deltas.
     */
    void handleStatusUpdate(MachineTileEntity sender, T update);
    
    /**
     * Will be called if the machine is destroyed or goes offline.
     */
    public void handleMachineDisconnect(MachineTileEntity sender);
    
    /**
     * Used by machine to remove orphaned/dead listeners.
     */
    public boolean isClosed();

    
}