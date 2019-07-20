package grondag.hard_science.network.server_to_client;

import grondag.exotic_matter.network.AbstractServerToPlayerPacket;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.energy.ClientEnergyInfo;
import grondag.hard_science.machines.matbuffer.ClientBufferInfo;
import grondag.hard_science.machines.support.MachineControlState;
import grondag.hard_science.machines.support.MachineStatusState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketMachineStatusUpdateListener extends AbstractServerToPlayerPacket<PacketMachineStatusUpdateListener>
{
    private MachineTileEntity te;
    
    public BlockPos pos;
    public MachineControlState controlState;
    public ClientBufferInfo materialBufferInfo;
    public MachineStatusState statusState;
    public ClientEnergyInfo powerSupplyInfo;
    public String machineName;
    
    public PacketMachineStatusUpdateListener() {}
    
    public PacketMachineStatusUpdateListener(MachineTileEntity te)
    {
        this.te = te;
        this.pos = te.getPos();
        this.controlState = te.machine().getControlState();
        this.statusState = te.machine().getStatusState();
        
        if(this.controlState.hasPowerSupply())
        {
            this.powerSupplyInfo = new ClientEnergyInfo(te.machine().energyManager());
        }
        
        this.machineName = te.machine().machineName();
    }

    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.pos = pBuff.readBlockPos();
        this.controlState = new MachineControlState();
        this.controlState.fromBytes(pBuff);
        this.statusState = new MachineStatusState();
        this.statusState.fromBytes(pBuff);
        
        if(this.controlState.hasMaterialBuffer()) 
        {
            this.materialBufferInfo = new ClientBufferInfo();
            this.materialBufferInfo.fromBytes(pBuff);
        }
        if(this.controlState.hasPowerSupply()) 
        {
            this.powerSupplyInfo = new ClientEnergyInfo();
            this.powerSupplyInfo.fromBytes(pBuff);
        }
        this.machineName = pBuff.readString(8);
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeBlockPos(pos);
        this.controlState.toBytes(pBuff);
        this.statusState.toBytes(pBuff);

        if(this.controlState.hasMaterialBuffer()) 
        {
            ClientBufferInfo.toBytes(this.te.machine().getBufferManager(), pBuff);
        }

        if(this.controlState.hasPowerSupply())
        {
            this.powerSupplyInfo.toBytes(pBuff);
        }
        
        pBuff.writeString(this.machineName);
    }

    @Override
    protected void handle(PacketMachineStatusUpdateListener message, MessageContext context)
    {
        TileEntity te = Minecraft.getMinecraft().player.world.getTileEntity(message.pos);
        if(te != null && te instanceof MachineTileEntity)
        {
            ((MachineTileEntity)te).handleMachineStatusUpdate(message);
        }
    }

}
