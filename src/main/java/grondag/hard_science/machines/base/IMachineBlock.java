package grondag.hard_science.machines.base;

import javax.annotation.Nullable;

import grondag.artbox.ArtBoxTextures;
import grondag.exotic_matter.block.ISuperBlock;
import grondag.exotic_matter.block.SuperBlockWorldAccess;
import grondag.exotic_matter.block.SuperTileEntity;
import grondag.exotic_matter.simulator.domain.DomainManager;
import grondag.exotic_matter.simulator.domain.IDomain;
import grondag.exotic_matter.simulator.domain.Privilege;
import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.machines.energy.MachinePower;
import grondag.hard_science.simulator.device.DeviceManager;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.endpoint.IPortLayout;
import grondag.hard_science.simulator.transport.endpoint.PortLayout;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IMachineBlock
{
    /**
     * Returns machine instance associated with block at this position, if any.
     */
    @Nullable
    public default AbstractMachine machine(World world, BlockPos pos)
    {
        assert !world.isRemote : "Attempt to access Machine on client.";
    
        if(world.isRemote) return null;
        
        if(world.getBlockState(pos).getBlock() != this)
        {
            assert false : "Block mismatch on device lookup request.";
            return null;
        }
        
        return (AbstractMachine)DeviceManager.getDevice(world, pos);
    }
    
    /**
     * Used to create new machine instances appropriate for this block.
     */
    public AbstractMachine createNewMachine();
    
    /**
     * Handler for block placement - call this from Block#OnBlockPlacedBy
     * BEFORE calling super.
     */
    public default void handleOnBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        if(worldIn.isRemote) return;
        
        if(Configurator.logDeviceChanges)
            HardScience.INSTANCE.info("MachineBlock.onBlockPlacedBy: @ %d.%d.%d in dim %d", 
                    pos.getX(), pos.getY(), pos.getZ(), worldIn.provider.getDimension());

        assert this.machine(worldIn, pos) == null
                : "Found existing machine on block placement.";
        
        // restore placed machines or initialize them with simulation
        AbstractMachine machine = this.createNewMachine();
        this.handleMachinePlacement(machine, worldIn, pos, state);
        
        if(stack.hasTagCompound())
        {
            NBTTagCompound serverTag = SuperTileEntity.getServerTag(stack.getTagCompound());
            if(serverTag.hasKey(MachineTileEntity.NBT_DEVICE_STATE))
            {
                machine.deserializeNBT(serverTag.getCompoundTag(MachineTileEntity.NBT_DEVICE_STATE));
            }
        }
        
        machine.setLocation(pos, worldIn);
        
        // new machines will default to public domain
        // change to player's active domain on placement
        // machines that are restored from stack may have
        // a specific existing domain, which should not be replaced.
        if(machine.getDomain() == null || machine.getDomain() == DomainManager.instance().defaultDomain())
        {
            IDomain domain = DomainManager.instance().getActiveDomain((EntityPlayerMP) placer);
            if(domain == null || !domain.hasPrivilege((EntityPlayer) placer, Privilege.ADD_NODE))
            {
                domain = DomainManager.instance().defaultDomain();
            }
            machine.setDomain(domain);
        }
        
        // machine fully ready to be connected to domain now
        DeviceManager.addDevice(machine);
        machine.onConnect();
        
        TileEntity blockTE = worldIn.getTileEntity(pos);
        if (blockTE != null && blockTE instanceof MachineTileEntity) 
        {
            ((MachineTileEntity)blockTE).onMachinePlaced(machine);
        }

    }

    /**
     * Retrieves ports available on faces of this device.
     * Port faces are transformed to match block orientation
     * if the port layout has an orientation.<p>
     * 
     * If this machine has item and power ports that use 
     * species for carrier segregation, ports will have channel set 
     * to block species of the given block state.<p>
     * 
     * Used server-side to initialize machines.  Used client-side
     * to render connectivity of cables.
     */
    public default IPortLayout portLayout(IBlockAccess worldIn, BlockPos pos, IBlockState state)
    {
        return this.nominalPortLayout().localize(SuperBlockWorldAccess.access(worldIn).getModelState((ISuperBlock)state.getBlock(), pos, true));
    }
    
    /**
     * The port layout for this machine before any 
     * configuration or orientation. Typically will
     * be implemented as a static constant value.
     * @return
     */
    public PortLayout nominalPortLayout();
    
    /**
     * Called immediately after a new machine is created during
     * block placement to allow machine-specific configuration
     * that is based on world state. Typically this is where the
     * machine would capture things like species and orientation
     * that depend on block/tile entity state and which may not
     * be available later on when the chunk is unloaded.  Machine
     * should serialize this information if needed for operation.
     */
    public default void handleMachinePlacement(AbstractMachine machine, World worldIn, BlockPos pos, IBlockState state)
    {
        if(machine.hasChannel() && state.getPropertyKeys().contains(ISuperBlock.META))
        {
            machine.setChannel(state.getValue(ISuperBlock.META));
        }
    }
    

    /**
     * Handler for block break - call this from Block#breakBlock
     * BEFORE calling super.
     */
    public default void handleBreakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        if(!(worldIn == null || worldIn.isRemote))
        {
            if(Configurator.logDeviceChanges)
                HardScience.INSTANCE.info("MachineBlock.breakBlock: @ %d.%d.%d in dim %d", 
                        pos.getX(), pos.getY(), pos.getZ(), worldIn.provider.getDimension());
            
            IDevice device = DeviceManager.getDevice(worldIn, pos);
            
//            assert device != null : "Null device on machine break block.";
            
            if(device != null) DeviceManager.removeDevice(device);
        }
    }
    
    /**
     * Call from addProbeInfo
     */
    public default void addMachineProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data)
    { 
        if(world.isRemote) return;
        
        AbstractMachine machine = this.machine(world, data.getPos());
        
        if(machine == null) return;

        probeInfo.text(machine.machineName().toUpperCase());
        probeInfo.text(I18n.translateToLocalFormatted("probe.machine.domain", 
                machine.getDomain() == null ? I18n.translateToLocal("misc.unassigned") : machine.getDomain().getName()));
        
        
        if(machine.hasTransportManager(StorageType.ITEM))
        {
//            if(machine.blockManager().itemCircuit() != null)
//            {
//                probeInfo.text(I18n.translateToLocalFormatted("Item circuit: %d  version: %d", 
//                        machine.blockManager().itemCircuit().carrierAddress(),
//                        machine.blockManager().itemCircuit().bridgeVersion()));
//            }
            probeInfo.text("Item Legs: " + machine.itemTransport().legs(null).toString()); 
        }
        
        if(!machine.energyManager().isEmpty())
        {
            probeInfo.progress(machine.energyManager().storedEnergyJoules(), 
                    machine.energyManager().maxStoredEnergyJoules());
            probeInfo.text("Stored Energy: " + MachinePower.formatEnergy(
                    machine.energyManager().storedEnergyJoules(), false));
            float netCharge = machine.energyManager().netStorageWatts();
            
            probeInfo.text((netCharge >= 0 ? "Charge Rate: " : "Discharge Rate: ")
                    + MachinePower.formatPower(
                            (long) netCharge, true));
        }
        if(machine.hasTransportManager(StorageType.POWER))
        {
//            if(machine.blockManager().powerCircuit() != null)
//            {
//                probeInfo.text(I18n.translateToLocalFormatted("Power circuit: %d  version: %d", 
//                        machine.blockManager().powerCircuit().carrierAddress(),
//                        machine.blockManager().powerCircuit().bridgeVersion()));
//            }
            probeInfo.text("Power Legs: " + machine.tranportManager(StorageType.POWER).legs(null).toString());
        }
    }
    
    /**
     * Controls icon rendered on machine face / sides
     */
    @SideOnly(Side.CLIENT)
    public default TextureAtlasSprite getSymbolSprite()
    {
        return ArtBoxTextures.DECAL_LARGE_SQUARE.getSampleSprite();
    }
}
