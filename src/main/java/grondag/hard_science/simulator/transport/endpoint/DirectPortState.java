package grondag.hard_science.simulator.transport.endpoint;

import javax.annotation.Nullable;

import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.Carrier;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;


public class DirectPortState<T extends StorageType<T>> extends Port<T>
{
    /**
     * Physical device on which this port is present.
     */
    private final IDevice device;
    
    public DirectPortState(IDevice device, T storageType, PortConnector connector, CarrierLevel level, BlockPos pos, EnumFacing face)
    {
        super(storageType, PortFunction.DIRECT, connector, level, pos, face);
        this.device = device;
    }
    
    public DirectPortState(IDevice device, IPortDescription<T> spec, BlockPos pos, EnumFacing face)
    {
        super(spec.storageType(), PortFunction.DIRECT, spec.connector(), spec.level(), pos, face);
        this.device = device;
        this.setChannel(spec.getChannel());
    }

    @Override
    public @Nullable Carrier<T> internalCircuit()
    {
        return null;
    }
    
    @Override
    public IDevice device()
    {
        return this.device;
    }

    @Override
    public CarrierLevel level()
    {
        return this.externalLevel();
    }
}
