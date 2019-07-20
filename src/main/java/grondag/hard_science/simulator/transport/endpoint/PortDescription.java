package grondag.hard_science.simulator.transport.endpoint;

import grondag.exotic_matter.varia.BitPacker64;
import grondag.hard_science.simulator.resource.EnumStorageType;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.carrier.Channel;


/**
 * Immutable description of port that always returns the same instance
 * for the same values.  Also includes a unique transient identifier.<p>
 * 
 * Dimensions that uniquely identify a port are:
 * <li>Storage Type
 * <li>Connector
 * <li>Function
 * <li>Channel (note that changing the channel effectively changes the port)
 * <li>Carrier Level</li><p>
 * 
 */
public class PortDescription<T extends StorageType<T>> implements IPortDescription<T>
{
    private static final BitPacker64<PortDescription<?>> PACKER = new BitPacker64<PortDescription<?>>(p -> p.id, (p, i) -> { throw new UnsupportedOperationException();});
    private static final BitPacker64<PortDescription<?>>.EnumElement<EnumStorageType> STORAGE_TYPE = PACKER.createEnumElement(EnumStorageType.class);
    private static final BitPacker64<PortDescription<?>>.EnumElement<PortConnector> CONNECTOR = PACKER.createEnumElement(PortConnector.class);
    private static final BitPacker64<PortDescription<?>>.EnumElement<PortFunction> FUNCTION = PACKER.createEnumElement(PortFunction.class);
    private static final BitPacker64<PortDescription<?>>.EnumElement<CarrierLevel> LEVEL = PACKER.createEnumElement(CarrierLevel.class);
    private static final BitPacker64<PortDescription<?>>.IntElement CHANNEL = PACKER.createIntElement(Channel.MIN_CHANNEL_VALUE, Channel.MAX_CHANNEL_VALUE);

    private static final PortDescription<?> ports[] = new PortDescription[1 << PACKER.bitLength()];

    
    private final T storageType;
    private final CarrierLevel level;
    private final PortFunction function;
    private final PortConnector connector;
    private final int channel;
    private final int id;
    
    @SuppressWarnings("unchecked")
    public static <V extends StorageType<V>> PortDescription<V> find(
            V storageType,
            CarrierLevel level,
            PortFunction function,
            PortConnector connector,
            int channel)
    {
        int id = (int)(
                  STORAGE_TYPE.getBits(storageType.enumType)
                | LEVEL.getBits(level)
                | FUNCTION.getBits(function)
                | CONNECTOR.getBits(connector)
                | CHANNEL.getBits(channel));
        
        PortDescription<V> result = (PortDescription<V>) ports[id];
        
        if(result == null)
        {
            synchronized(ports)
            {
                result = (PortDescription<V>) ports[id];
                if(result == null)
                {
                    result = new PortDescription<V>(id, storageType, level, function, connector, channel);
                    ports[id] = result;
                }
            }
        }
        return result;
    }
    
    /**
     * Note that face is <em>nominal</em> face.
     */
    private PortDescription(
            int id,
            T storageType,
            CarrierLevel level,
            PortFunction function,
            PortConnector connector,
            int channel)
    {
        this.id = id;
        this.storageType = storageType;
        this.level = level;
        this.function = function;
        this.connector = connector;
        this.channel = channel;
    }

    /**
     * Transient unique ID for this combination of port characteristic.
     * NOT guaranteed to be consistent across sessions/worlds 
     * because fluid IDs can change if other mods are added/removed/changed.
     */
    public int transientID()
    {
        return this.id;
    }
    
    @Override
    public T storageType()
    {
        return this.storageType;
    }

    @Override
    public CarrierLevel level()
    {
        return level;
    }

    @Override
    public PortFunction function()
    {
        return function;
    }

    @Override
    public int getChannel()
    {
        return channel;
    }

    @Override
    public PortConnector connector()
    {
        return this.connector;
    }

    /**
     * Returns this port with channel set to the given
     * channel. Instances returned from this method will be the same 
     * instance that would be returned from {@link #find(StorageType, CarrierLevel, PortFunction, PortConnector, int)}
     */
    public PortDescription<?> withChannel(int channel)
    {
        return channel == this.channel
            ? this
            : find(this.storageType, this.level, this.function, connector, channel);
    }
}