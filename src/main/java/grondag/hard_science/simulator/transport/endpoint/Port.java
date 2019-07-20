package grondag.hard_science.simulator.transport.endpoint;

import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.device.IDeviceComponent;
import grondag.hard_science.simulator.resource.ITypedStorage;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.Carrier;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.carrier.Channel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * Represents a physical port on a device.
 * Subclassed for the various port types.<p>
 * 
 * Methods related to network topology should ONLY be
 * called from the connection manager thread and will 
 * throw an exception if called otherwise.  This avoids
 * the need for synchronization of these methods.
 *
 */
public abstract class Port<T extends StorageType<T>> 
    implements IDeviceComponent, ITypedStorage<T>, IPortDescription<T>
{
    private PortDescription<T> wrapped;
    
    private final BlockPos pos;
    
    private final EnumFacing face;
    
    protected PortMode mode = PortMode.DISCONNECTED;
    
    /**
     * True if port has been successfully attached.
     * False if not. <p>
     * 
     * Generally synonymous with mate != null and
     * externalCircuit != null but could be some edge
     * cases or time windows when those do not hold and
     * wanted to have an indicator that wasn't overloaded.
     */
    protected boolean isAttached = false;
    
    /**
     * If port has mated, should contain a reference 
     * to a port on an adjacent (or wirelessly connected) device.
     */
    private Port<T> mate;
    
    /**
     * See {@link #externalCircuit()}<p>
     * 
     * Be careful about accessing directly depending on intent.
     * Is overridden by carrier ports.
     */
    protected Carrier<T> externalCircuit;
    
    public Port(
            T storageType,
            PortFunction function,
            PortConnector connector,
            CarrierLevel level, 
            @Nullable BlockPos pos, 
            @Nullable EnumFacing face)
    {
        this.wrapped = PortDescription.find(storageType, level, function, connector, 
                Channel.channelOverride(Channel.CONFIGURABLE_NOT_SET, level, storageType));
        this.pos = pos;
        this.face = face;
    }

    @Override
    public T storageType()
    {
        return this.wrapped.storageType();
    }
    
    @Override
    public PortConnector connector()
    {
        return this.wrapped.connector();
    }
    @Override
    public int getChannel()
    {
        return this.wrapped.getChannel(); 
    }
    
    /**
     * Sets channel for this port.<p>
     * 
     * For bridge and direct ports, this channel
     * only applies to the exterior carrier. 
     * A channel can be set for carrier ports (and bridge
     * ports in carrier mode) but it will not be used
     * unless the port is changed to direct or bridge mode.<p>
     * 
     * This port should not be attached when called.<p>
     * 
     * Ignored for non-fluid top-level ports
     */
    @SuppressWarnings("unchecked")
    public void setChannel(int channel)
    {
        assert !this.isAttached() && this.mate == null && this.externalCircuit == null
                : "Port.setMode: Attempt to change port channel while port connected.";

        this.wrapped = (PortDescription<T>) this.wrapped.withChannel(
                Channel.channelOverride(Channel.CONFIGURABLE_NOT_SET, this.wrapped.level(), this.wrapped.storageType()));
    }

    /**
     * If port is mated and carrier circuit has formed, reference to the 
     * external carrier circuit. Null otherwise. <p>
     * 
     * Is always the same as {@link #internalCircuit()} for ports in carrier mode.
     */
    @Nullable
    public Carrier<T> externalCircuit()
    {
        assert this.mode.isConnected || this.isAttached || externalCircuit == null
                : "Non-null external circuit for disconnected port";
        
        return externalCircuit;
    }
    
    /**
     * Reference to device's internal carrier if there is one.
     * Will always be null for direct ports and will always
     * be the same as {@link #externalCircuit()} for carrier ports.<p>
     * 
     * Will also be null if no carrier or bridge ports on a device are mated.
     */
    @Nullable
    public abstract Carrier<T> internalCircuit();
    
    /**
     * Attaches to the provided circuit and handles internal record keeping.
     * Assumes that {@link Port#connectionResult(Port, Port)} has
     * already been called and result for this port passed in via the mode
     * parameter.<p>
     * 
     * Thows assertion errors if attachment is not possible.<p>
     * 
     * Calls {@link IDevice#refreshTransport(grondag.hard_science.simulator.resource.StorageType)} 
     * if attachment is successful.<p>
     * 
     * Does NOT call attach on mated port. Caller must do so.
     * But does save reference to mate for notification and inspection.<p>
     * 
     * SHOULD ONLY BE CALLED FROM CONNECTION MANAGER THREAD.
     */
    public void attach(
            Carrier<T> externalCircuit,
            Port<T> mate)
    {
        assert this.confirmServiceThread() : "Transport logic running outside transport thread";
        
        assert !this.isAttached()
                : "Port attach request when already attached.";
        
        assert this.mode.isConnected : "Request to attach port with disconnected mode";
        
        // NB: important to check private value here because carrier ports
        // will override and give a non-null value (internal carrier) pre-attach
        assert this.externalCircuit == null
                : "Port attach request with non-null external circuit.";
        
        assert this.getChannel() == externalCircuit.channel
                : "Port attach with mismatched channels";
        
        assert this.externalLevel() == externalCircuit.level()
                : "Port attach with mismatched levels";
        
        if(Configurator.logTransportNetwork) 
            HardScience.INSTANCE.info("Port.attach %s: port attach for %s to circuit %d with mate %s",
                    this.device().machineName(),
                    this.toString(),
                    externalCircuit.carrierAddress(),
                    mate.toString());

        
        if(Configurator.logTransportNetwork) 
            HardScience.INSTANCE.info("Port.attach %s: port mode = %s", 
                    this.device().machineName(),
                    this.mode);
        
        // circuit will expect this before attachment
        this.externalCircuit = externalCircuit;
        externalCircuit.attach(this, false);
        this.mate = mate;
        this.isAttached =true;
        
        this.device().refreshTransport(this.storageType());
    }
    
    /**
     * Detaches from externalCircuit and removes reference to it.
     * Also sets mate reference to null and changes port mode to
     * DISCONNNECTED.<p>
     * 
     * Calls {@link IDevice#refreshTransport(grondag.hard_science.simulator.resource.StorageType)}<p>
     * 
     * Does NOT call mate detach  Is expected caller will get mate
     * reference before calling and ensure mate is also detached.<p>
     * 
     * SHOULD ONLY BE CALLED FROM CONNECTION MANAGER THREAD.
     */
    public void detach()
    {
        assert this.confirmServiceThread() : "Transport logic running outside transport thread";
        
        assert this.isAttached()
                : "Port dettach request when not attached.";
        
        if(Configurator.logTransportNetwork) 
            HardScience.INSTANCE.info("Port.detach %s: port detach for %s",
                    this.device().machineName(),
                    this.toString());
        
        if(this.externalCircuit != null)
        {
            this.externalCircuit.detach(this);
            this.externalCircuit = null;
        }
        this.mate = null;
        this.isAttached =false;
        this.mode = PortMode.DISCONNECTED;
        this.device().refreshTransport(this.storageType());
        
    }

    /**
     * See {@link #mate}
     */
    public Port<T> mate()
    {
        return this.mate;
    }
    
    public boolean isAttached()
    {
        return this.isAttached;
    }
    
    /**
     * For use by merge/split operations.<p>
     * 
     * Swaps *any* non-null reference to the old circuit held by this port
     * to use the new non-null reference.  If does not holds a reference to a 
     * to the old circuit, does nothing.<p>
     * 
     * Calls {@link IDevice#refreshTransport(grondag.hard_science.simulator.resource.StorageType)} 
     * if any swap occurs.<p>
     * 
     * Does NOT otherwise perform any notifications or cause any side effects.
     * For example, does not add itself as a port on the new circuit.
     * Caller is expected to handle any such necessary accounting.<p>
     * 
     * SHOULD ONLY BE CALLED FROM CONNECTION MANAGER THREAD.
     */
    public void swapCircuit(Carrier<T> oldCircuit, Carrier<T> newCircuit)
    {
        assert this.confirmServiceThread() : "Transport logic running outside transport thread";
        if(this.externalCircuit == oldCircuit)
        {
            if(Configurator.logTransportNetwork) 
                HardScience.INSTANCE.info("Port.swapCircuit %s: replacing external circuit %d with new circuit %d",
                        this.device().machineName(),
                        oldCircuit.carrierAddress(),
                        newCircuit.carrierAddress());

            this.externalCircuit = newCircuit;
            this.device().refreshTransport(this.storageType());
        }
    }
    
    /**
     * If this port is a carrier port on a device with an internal carrier, 
     * iterates all <em>other</em> ports on the device attached to the same carrier.
     * Equivalently, all ports on the device that share the carrier that
     * is externally visible on this port.<p>
     * 
     * Always empty for direct ports and for bridge ports in bridge mode.
     * Bridge ports are handled this way because the internal carrier is not
     * directly accessible via the bridge port. (That's what makes it a bridge.)<p>
     * 
     * Bridge ports in carrier mode will behave just like carrier ports.<p>
     * 
     * With {@link #mate()}, enables search within a circuit topology.
     */
    public Iterable<Port<T>> carrierMates()
    {
        return Collections.emptyList();
    }

    /**
     * For TOP and debug display.
     * Null for wireless.
     */
    @Nullable
    public BlockPos pos()
    {
        return this.pos;
    }
    
    @Nullable
    public EnumFacing getFace()
    {
        return this.face;
    }
    
    /**
     * For TOP and debug display
     */
    @Override
    public String toString()
    {
        return String.format(
            "%s/%s-%s %d/%d on %s @ %s, mode=%s", 
            this.storageType().enumType.toString(),
            this.function().toString(), 
            this.level().toString(),
            this.internalCircuit() == null ? 0 : this.internalCircuit().carrierAddress(),
            this.externalCircuit() == null ? 0 : this.externalCircuit().carrierAddress(),
            this.device().machineName(),
            this.pos() == null || this.getFace() == null
                ? "N/A"
                : String.format("%d.%d.%d:%s", 
                        this.pos().getX(), 
                        this.pos().getY(), 
                        this.pos().getZ(), 
                        this.getFace().toString()),
           this.mode.toString()
        );
    }
    
    public PortMode getMode()
    {
        return this.mode;
    }
    
    /**
     * Sets mode that will be used when port is 
     * connected.  Should not be called while port is connected.
     */
    public void setMode(PortMode mode)
    {
        if(Configurator.logTransportNetwork) 
            HardScience.INSTANCE.info("Port.setMode %s: mode = %s", 
                    this.device().machineName(),
                    mode.toString());

        if(this.mode == mode)
        {
            if(Configurator.logTransportNetwork) 
                HardScience.INSTANCE.info("Port.setMode %s: no effect - target mode was already set", 
                        this.device().machineName());
            return;
        }
        
        assert !this.isAttached() && this.mate == null && this.externalCircuit == null
            : "Port.setMode: Attempt to change port mode while port connected.";
        
                    
        this.mode = mode;
    }

    /**
    * For Carrier and Bridge ports, the level of the carrier
    * within the device. <p>
    * 
    * For Carrier ports, will be same as {@link #externalLevel()}.<p>
    * Will be null for Direct ports.  
    */
    @Nullable
    public final CarrierLevel internalLevel()
    {
        return this.function() == PortFunction.DIRECT
                ? null
                : this.level(); 
    }

    /**
     * The carrier level offered to or expected of potential mate ports.
     * Will be same as {@link #internalLevel()} for Carrier ports. For bridge
     * ports in bride mode will be one level lower than internal.
     */
    public final CarrierLevel externalLevel()
    {
        return this.externalLevel(this.mode);
    }

    /**
     * Forecasts what external level will be with the given mode.
     */
    public CarrierLevel externalLevel(PortMode mode)
    {
        return this.function() == PortFunction.BRIDGE && mode == PortMode.BRIDGE
                ? this.level().below()
                : this.level();
    }
    
    @Override
    public PortFunction function()
    {
        return this.wrapped.function();
    }
}
