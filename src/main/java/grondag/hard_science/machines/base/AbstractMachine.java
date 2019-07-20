package grondag.hard_science.machines.base;

import javax.annotation.Nullable;

import grondag.hard_science.machines.support.MachineControlState;
import grondag.hard_science.machines.support.MachineControlState.ControlMode;
import grondag.hard_science.machines.support.MachineStatusState;
import grondag.hard_science.simulator.device.AbstractDevice;
import net.minecraft.server.network.ServerPlayerEntity;

public abstract class AbstractMachine extends AbstractDevice
{
    private MachineControlState controlState = new MachineControlState();
    protected MachineStatusState statusState = new MachineStatusState();
    
    /**
     * If non-null will send notifications.
     */
    public @Nullable MachineTileEntity machineTE;
    
    public AbstractMachine()
    {
        this.controlState.hasMaterialBuffer(this.getBufferManager().isReal());
        this.controlState.hasPowerSupply(!this.energyManager.isEmpty());
    }
    
    public boolean hasBacklog()
    {
        return this.statusState.hasBacklog();
    }
    
    /**
     * Max backlog depth since machine was last idle or power cycled.
     * Automatically maintained y {@link #setCurrentBacklog(int)}
     */
    public int getMaxBacklog()
    {
        return this.hasBacklog() ? this.statusState.getMaxBacklog() : 0;
    }
    
    public int getCurrentBacklog()
    {
        return this.hasBacklog() ? this.statusState.getCurrentBacklog() : 0;
    }
    
    public void setCurrentBacklog(int value)
    {
        if(!this.hasBacklog()) return;
        
        if(value != this.statusState.getCurrentBacklog())
        {
            this.statusState.setCurrentBacklog(value);
            this.markTEPlayerUpdateDirty(false);
        }
        
        int maxVal = Math.max(value, this.getMaxBacklog());
        if(value == 0) maxVal = 0;
        
        if(maxVal != this.getMaxBacklog())
        {
            this.statusState.setMaxBacklog(maxVal);
            this.markTEPlayerUpdateDirty(false);
        }        
    }
    
    /**
     * Handles packet from player to toggle power on or off.
     * Returns false if denied.
     */
    public boolean togglePower(ServerPlayerEntity player)
    {
        // clear backlog on power cycle if we have one
        this.setCurrentBacklog(0);
        
        //FIXME: check user permissions
        
        // called by packet handler on server side
        switch(this.getControlState().getControlMode())
        {
        case OFF:
            this.getControlState().setControlMode(ControlMode.ON);
            break;
            
        case OFF_WITH_REDSTONE:
            this.getControlState().setControlMode(ControlMode.ON_WITH_REDSTONE);
            break;
            
        case ON:
            this.getControlState().setControlMode(ControlMode.OFF);
            break;
            
        case ON_WITH_REDSTONE:
            this.getControlState().setControlMode(ControlMode.OFF_WITH_REDSTONE);
            break;
            
        default:
            break;
            
        }
        this.setDirty();
        return true;
    }
    
    /**
     * Called when get packet from client.
     */
    public boolean toggleRedstoneControl(ServerPlayerEntity player)
    {
        //FIXME: check user permissions
        
        if(!this.hasRedstoneControl()) return false;
        
        switch(this.getControlState().getControlMode())
        {
        case OFF:
            this.getControlState().setControlMode(this.hasRedstonePowerSignal() ? ControlMode.OFF_WITH_REDSTONE : ControlMode.ON_WITH_REDSTONE);
            break;
            
        case OFF_WITH_REDSTONE:
            this.getControlState().setControlMode(this.hasRedstonePowerSignal() ? ControlMode.OFF : ControlMode.ON);
            break;
            
        case ON:
            this.getControlState().setControlMode(this.hasRedstonePowerSignal() ? ControlMode.ON_WITH_REDSTONE : ControlMode.OFF_WITH_REDSTONE);
            break;
            
        case ON_WITH_REDSTONE:
            this.getControlState().setControlMode(this.hasRedstonePowerSignal() ? ControlMode.ON : ControlMode.OFF);
            break;
            
        default:
            break;
            
        }
        this.setDirty();
        return true;
    }
    
    public void markTEPlayerUpdateDirty(boolean isUrgent)
    {
        if(this.machineTE != null)
        {
            if(this.machineTE.isInvalid())
            {
                this.machineTE = null;
            }
            else
            {
                this.machineTE.markPlayerUpdateDirty(isUrgent);
            }
        }
    }
    
    public MachineControlState getControlState()
    {
        return this.controlState;
    }
    
    public MachineStatusState getStatusState()
    {
        return this.statusState;
    }

    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag)
    {
        super.deserializeNBT(tag);
        this.getControlState().deserializeNBT(tag);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        super.serializeNBT(tag);
        this.getControlState().serializeNBT(tag);
    }
    
    /**
     * Make false to disable on/off switch.
     */
    public boolean hasOnOff() { return true;}
    
    /**
     * Make false to disable redstone control.
     */
    public boolean hasRedstoneControl() { return true; }
    
    public static boolean computeIsOn(MachineControlState controlState, MachineStatusState statusState)
    {
        switch(controlState.getControlMode())
        {
        case ON:
            return true;
            
        case OFF_WITH_REDSTONE:
            return !statusState.hasRedstonePower();
            
        case ON_WITH_REDSTONE:
            return statusState.hasRedstonePower();
            
        case OFF:
        default:
            return false;
        
        }
    }
    
    @Override
    public boolean isOn()
    {
        if(!this.hasOnOff()) return false;
        return computeIsOn(this.getControlState(), this.getStatusState());
    }
    
    public boolean isRedstoneControlEnabled()
    {
        return this.hasRedstoneControl() && this.getControlState().getControlMode().isRedstoneControlEnabled;
    }
    
    public boolean hasRedstonePowerSignal()
    {
        return this.getStatusState().hasRedstonePower();
    }
    
//    protected void restock()
//    {
//        BufferManager bufferInfo = this.getBufferManager();
//        
//        if(!bufferInfo.canRestockAny() || this.machineTE == null) return;
//        
//        //FIXME: temp hack - should use world from location and check for loaded chunk
//        World teWorld = machineTE.getWorld();
//        
//        for(EnumFacing face : EnumFacing.VALUES)
//        {
//            
//            TileEntity tileentity = teWorld.getTileEntity(this.getLocation().offset(face));
//            if (tileentity != null)
//            {
//                // Currently no power cost for pulling in items - would complicate fuel loading for fuel cells.
//                // If want to add this will require privileged handling or machines must get 
//                // power some other way to load fuel - seems tedious.
//                // Assuming this is covered by the "emergency" power supply that uses ambient energy harvesters.
//                IItemHandler capability = tileentity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face.getOpposite());
//                if(bufferInfo.restock(capability)) this.setDirty();
//            }
//        }
//    }
    
    /**
     * Call when power stops production.
     * Power supply will forgive itself when it successfully provides power.
     */
    protected void blamePowerSupply()
    {
        if(!this.energyManager().isFailureCause())
        {
            this.energyManager().setFailureCause(true);
            this.markTEPlayerUpdateDirty(false);
        }
    }
    
    /**
     * Consumes the given number of joules from the power supply,
     * returning true if all power was available. If not all power
     * is available then returns false and blames the power supply.
     */
    protected boolean provideAllPowerOrBlameSupply(long joules)
    {
        long result = this.energyManager().provideEnergy(joules, false, false);
        if(result != joules)
        {
            this.blamePowerSupply();
            return false;
        }
        return true;
    }

    /**
     * Consumes up to the given number of joules from the power supply,
     * returning the number actually consumed. 
     * If the amount is less than requested blames the power supply.
     */
    protected long provideSomePowerAndBlameSupply(long joules)
    {
        long result = this.energyManager().provideEnergy(joules, true, false);
        if(result != joules) this.blamePowerSupply();
        return result;
    }
}
