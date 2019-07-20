package grondag.hard_science.simulator.transport.endpoint;

import grondag.hard_science.simulator.resource.ITypedStorage;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.carrier.Channel;

/**
 * Descriptive information for a port within a block.
 */
public interface IPortDescription<T extends StorageType<T>> extends ITypedStorage<T>
{
    /**
    * The configured level of this port.  Interpretation
    * for internal/external connections depends on port type and mode.
    */
    CarrierLevel level();

    PortFunction function();

    PortConnector connector();
    
    /**
     * Configured channel for external carrier of this port. 
     * Ports with different channel values cannot mate and 
     * cannot coexist on the same carrier circuit.<p>
     * 
     * Note that internal/external carrier (and thus channel) are
     * always the same for carrier ports. <p> 
     * 
     * For carrier ports, (or bridge ports in carrier mode)
     * the value returned should always be the channel of the internal 
     * carrier even if the port channel has been set to something
     * else via {@link #setChannel(int)}.
     * 
     * See notes in Channel<p>
     */
    int getChannel();

    /**
     * True if this port could attach to an adjacent port with 
     * the given level and channel. For bridge ports, assumes
     * that can be configured to operate in bridge or carrier mode.<p>
     * 
     * If this port's channel or the input channel is not configured, 
     * (or neither are) assumes port(s) can be configured to use the same channel.
     */
    public default boolean couldAttach(IPortDescription<?> toPort)
    {
        if(toPort.storageType() != this.storageType()) return false;
        
        if(this.level().ordinal() <= toPort.level().ordinal())
        {
            return couldAttach(
                    this.level(),
                    this.getChannel(),
                    this.connector(),
                    toPort.level(), 
                    toPort.getChannel(), 
                    toPort.connector(),
                    toPort.function() == PortFunction.BRIDGE);
        }
        else
        {
            return couldAttach(
                    toPort.level(), 
                    toPort.getChannel(), 
                    toPort.connector(),
                    this.level(),
                    this.getChannel(),
                    this.connector(),
                    this.function() == PortFunction.BRIDGE);
        }
    }
    
    /**
     * Version of {@link #couldAttach(IPortDescription)} for use when port
     * channels have not yet been configured. (Client-side). Ports with 
     * channel == {@value Channel#CONFIGURABLE_FOLLOWS_DEVICE} are compared
     * as if their channel had been set to the respective value provided.
     */
    public default boolean couldAttach(IPortDescription<T> toPort, int withChannel, int toChannel)
    {
        if(this.level().ordinal() <= toPort.level().ordinal())
        {
            return couldAttach(
                    this.level(),
                    this.getChannel() == Channel.CONFIGURABLE_FOLLOWS_DEVICE 
                        ? withChannel : this.getChannel(),
                    this.connector(),
                    toPort.level(), 
                    toPort.getChannel() == Channel.CONFIGURABLE_FOLLOWS_DEVICE 
                            ? toChannel : toPort.getChannel(), 
                    toPort.connector(),
                    toPort.function() == PortFunction.BRIDGE);
        }
        else
        {
            return couldAttach(
                    toPort.level(), 
                    toPort.getChannel() == Channel.CONFIGURABLE_FOLLOWS_DEVICE 
                        ? toChannel : toPort.getChannel(), 
                    toPort.connector(),
                    this.level(),
                    this.getChannel() == Channel.CONFIGURABLE_FOLLOWS_DEVICE 
                        ? withChannel : this.getChannel(),
                    this.connector(),
                    this.function() == PortFunction.BRIDGE);
        }
    }
    
    /**
     * NOT INTENDED FOR DIRECT USE<p>
     * 
     * Implementation logic for {@link #couldAttach(IPortDescription)}<p>
     * 
     * The first "from" port MUST be the power with the lower level, if 
     * either level is different. 
     * 
     */
    public static boolean couldAttach(
            CarrierLevel fromLevel, 
            int fromChannel, 
            PortConnector fromConnector,
            CarrierLevel toLevel, 
            int toChannel, 
            PortConnector toConnector,
            boolean toIsBridge
            )
    {
        if(fromConnector == toConnector)
        {
            if(fromLevel == toLevel)
            {
                return fromChannel == toChannel 
                        || fromChannel == Channel.CONFIGURABLE_NOT_SET
                        || toChannel == Channel.CONFIGURABLE_NOT_SET;
            }
            else if(toIsBridge && toLevel.below() == fromLevel)
            {
                // bridge mode
                return true;
            }
            else return false;
        }
        else return false;
    }
}