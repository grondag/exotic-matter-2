package grondag.hard_science.machines.base;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.exotic_matter.block.BlockSubstance;
import grondag.exotic_matter.block.SuperBlockPlus;
import grondag.exotic_matter.model.color.BlockColorMapProvider;
import grondag.exotic_matter.model.color.Chroma;
import grondag.exotic_matter.model.color.Hue;
import grondag.exotic_matter.model.color.Luminance;
import grondag.exotic_matter.model.painting.PaintLayer;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.model.state.ModelState;
import grondag.exotic_matter.model.texture.ITexturePalette;
import grondag.exotic_matter.model.varia.WorldLightOpacity;
import grondag.hard_science.HardScience;
import grondag.hard_science.gui.control.machine.RenderBounds;
import grondag.hard_science.init.ModSubstances;
import grondag.hard_science.machines.support.MachineItemBlock;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.registries.IForgeRegistry;

public abstract class MachineBlock extends SuperBlockPlus implements IMachineBlock
{
    public static final Material MACHINE_MATERIAL = new Material(MapColor.SILVER_STAINED_HARDENED_CLAY) 
    {
        @Override
        public boolean isToolNotRequired() { return true; }

        @Override
        public EnumPushReaction getMobilityFlag() { return EnumPushReaction.BLOCK; }
    };
    
    public final int guiID;
    
    public MachineBlock(String name, int guiID, ISuperModelState modelState)
    {
        super(name, MACHINE_MATERIAL, modelState, modelState.getRenderLayoutProducer());
        this.guiID = guiID;
        this.setHarvestLevel(null, 0);
        this.setHardness(1);
    }
    
    protected static ISuperModelState creatBasicMachineModelState(ITexturePalette decalTex, ITexturePalette borderTex)
    {
        ISuperModelState modelState = new ModelState();
        modelState.setShape(grondag.hard_science.init.ModShapes.MACHINE);
        modelState.setTexture(PaintLayer.BASE, grondag.exotic_matter.init.ModTextures.BLOCK_NOISE_MODERATE);
        modelState.setColorMap(PaintLayer.BASE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.AZURE, Chroma.WHITE, Luminance.MEDIUM_LIGHT));

        modelState.setTexture(PaintLayer.LAMP, grondag.exotic_matter.init.ModTextures.BLOCK_NOISE_SUBTLE);
        modelState.setColorMap(PaintLayer.LAMP, BlockColorMapProvider.INSTANCE.getColorMap(Hue.AZURE, Chroma.PURE_NETURAL, Luminance.EXTRA_DARK));
        
        if(decalTex != null)
        {
            modelState.setTexture(PaintLayer.MIDDLE, decalTex);
            modelState.setColorMap(PaintLayer.MIDDLE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.AZURE, Chroma.PURE_NETURAL, Luminance.BRILLIANT));
            modelState.setTranslucent(PaintLayer.MIDDLE, true);
            modelState.setAlpha(PaintLayer.MIDDLE, 0x19);
        }
        
        if(borderTex != null)
        {
            modelState.setTexture(PaintLayer.OUTER, borderTex);
            modelState.setColorMap(PaintLayer.OUTER, BlockColorMapProvider.INSTANCE.getColorMap(Hue.COBALT, Chroma.GREY, Luminance.MEDIUM_DARK));
        }
        return modelState;
    }
    
    @Override
    public void registerItems(IForgeRegistry<Item> itemReg)
    {
        ItemBlock itemBlock = new MachineItemBlock(this);
        itemBlock.setRegistryName(this.getRegistryName());
        itemReg.register(itemBlock);        
    }
    
    @Override
    public int damageDropped(@Nonnull IBlockState state)
    {
        // don't want species to "stick" with machines - is purely cosmetic
        return 0;
    }
    
    @Override
    public void neighborChanged(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos)
    {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        TileEntity myTE = worldIn.getTileEntity(pos);
        if(myTE != null && myTE instanceof MachineTileEntity)
        {
            ((MachineTileEntity)myTE).updateRedstonePower();
        }
    }

    @Override
    public boolean onBlockActivated(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) 
    {
        // for simple blocks without a container, activation is client-only
        if (world.isRemote && this.guiID >= 0) 
        {
            TileEntity te = world.getTileEntity(pos);
            if (te != null && te instanceof MachineTileEntity)
            {
                player.openGui(HardScience.INSTANCE, this.guiID, world, pos.getX(), pos.getY(), pos.getZ());
                return true;
            }
        }
        return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
    }

    /**
     * Default machine implementation only handles single-block machines.<p>
     * 
     * {@inheritDoc}
     */
    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state)
    {
        this.handleBreakBlock(worldIn, pos, state);
        super.breakBlock(worldIn, pos, state);
    }

    protected float hitX (EnumFacing side, float hitX, float hitZ)
    {
        switch (side) 
        {
            case NORTH:
                return 1 - hitX;
                
            case SOUTH:
                return hitX;
                
            case WEST:
                return hitZ;
                
            case EAST:
                return 1 - hitZ;
                
            default:
                return 0;
        }
    }
    
    @Override
    public void onBlockClicked(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EntityPlayer playerIn)
    {
        if (worldIn.isRemote) return;

        MachineTileEntity machineTE = (MachineTileEntity) getTileEntityReliably(worldIn, pos);
        
        if(machineTE.machine() == null) return;
        
        if(!machineTE.machine().hasOnOff() && !machineTE.machine().hasRedstoneControl()) return;

        RayTraceResult rayResult = net.minecraftforge.common.ForgeHooks.rayTraceEyes(playerIn, 
                ((EntityPlayerMP) playerIn).interactionManager.getBlockReachDistance() + 1);
        
        if (rayResult == null) return;



        EnumFacing side = rayResult.sideHit;
        if (machineTE.getCachedModelState().getAxisRotation().horizontalFace != side) return;

        // translate to block frame of reference
        float hitX = (float)(rayResult.hitVec.x - pos.getX());
        float hitY = (float)(rayResult.hitVec.y - pos.getY());
        float hitZ = (float)(rayResult.hitVec.z - pos.getZ());
        
        // translate to 2d face coordinates
        float faceX = this.hitX(side, hitX, hitZ);
        float faceY = 1f - hitY;

        if(machineTE.machine().hasOnOff() && RenderBounds.BOUNDS_ON_OFF.contains(faceX, faceY))
        {
            if(machineTE.togglePower((EntityPlayerMP) playerIn))
            {
                worldIn.playSound(null, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, .2f, ((worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * .7f + 1) * 2);
            }
        }
        else if(machineTE.machine().hasRedstoneControl() && RenderBounds.BOUNDS_REDSTONE.contains(faceX, faceY))
        {
            if(machineTE.toggleRedstoneControl((EntityPlayerMP) playerIn))
            {
                worldIn.playSound(null, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, .2f, ((worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * .7f + 1) * 2);
            }
        }
        else
        {
            super.onBlockClicked(worldIn, pos, playerIn);
        }
      

    }
    
    @Override
    public boolean addDestroyEffects(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull ParticleManager manager)
    {
        return true;
    }

    @Override
    public boolean addHitEffects(@Nonnull IBlockState blockState, @Nonnull World world, @Nonnull RayTraceResult target, @Nonnull ParticleManager manager)
    {
        return true;
    }

    @Override
    public boolean addLandingEffects(@Nonnull IBlockState state, @Nonnull WorldServer worldObj, @Nonnull BlockPos blockPosition, @Nonnull IBlockState iblockstate, @Nonnull EntityLivingBase entity,
            int numberOfParticles)
    {
        return true;
    }

    /**
     * On server-side after machine block has been placed will
     * restore machine state from stack (if present) or 
     * create new machine state and register with simulation.<p>
     * 
     * Relies on machine state to be saved by {@link #writeModNBT(NBTTagCompound)}
     * for harvested machines.<p>
     * 
     * New machines are added to the placer's domain. Machines with an 
     * existing domain are not changed.<p>
     * 
     * Location of the machine is always changed to event parameters.
     * 
     * {@inheritDoc}
     */
    @Override
    public void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        this.handleOnBlockPlacedBy(worldIn, pos, state, placer, stack);
    }
    
    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list)
    {
        // We allow metadata for machine blocks to support texturing
        // but we only want to show one item in creative search / JEI
        // No functional difference.
        list.add(this.getSubItems().get(0));
    }
    
    @Override
    public BlockSubstance getSubstance(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return ModSubstances.MACHINE;
    }

    @Override
    public BlockSubstance defaultSubstance()
    {
        return ModSubstances.MACHINE;
    }
    
    @Override
    public boolean isGeometryFullCube(IBlockState state)
    {
        return true;
    }

    @Override
    public boolean isHypermatter()
    {
        return false;
    }

    @Override
    protected WorldLightOpacity worldLightOpacity(IBlockState state)
    {
        return WorldLightOpacity.SOLID;
    }
    
//    @Override
//    public EnumBlockRenderType getRenderType(IBlockState iBlockState)
//    {
//      return EnumBlockRenderType.INVISIBLE;
//    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag advanced)
    {
        //NOOP for now on machines - don't want all the stuff we get for normal superblocks
    }

    @Override
    public void addProbeInfo(@Nullable ProbeMode mode, @Nullable IProbeInfo probeInfo, @Nullable EntityPlayer player, @Nullable World world, @Nullable IBlockState blockState, @Nullable IProbeHitData data)
    { 
        this.addMachineProbeInfo(mode, probeInfo, player, world, blockState, data);
    }
    
    @Override
    public void handleMachinePlacement(AbstractMachine machine, World worldIn, BlockPos pos, IBlockState state)
    {
        // captures device channel
        IMachineBlock.super.handleMachinePlacement(machine, worldIn, pos, state);
        
        if(machine instanceof AbstractSimpleMachine)
        {
            ((AbstractSimpleMachine)machine).setPortLayout(this.portLayout(worldIn, pos, state));
        }
    }
}
