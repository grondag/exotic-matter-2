package grondag.hard_science.machines.base;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.exotic_matter.block.SuperSimpleBlock;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.hard_science.init.ModSubstances;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public abstract class MachineSimpleBlock extends SuperSimpleBlock implements IMachineBlock
{
    protected MachineSimpleBlock(String blockName, ISuperModelState defaultModelState)
    {
        super(blockName, ModSubstances.MACHINE, defaultModelState);
        this.metaCount = 1;
        this.setHarvestLevel(null, 0);
        this.setHardness(1);
    }

    //allow mined blocks to stack
    @Override
    public int damageDropped(@Nonnull IBlockState state)
    {
        return 0;
    }

    //allow mined blocks to stack
    @Override
    public ItemStack getStackFromBlock(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return this.getSubItems().get(0);
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
    
    @Override
    public boolean isHypermatter()
    {
        return false;
    }
    
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
    public void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack)
    {
        this.handleOnBlockPlacedBy(worldIn, pos, state, placer, stack);
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }
    
    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state)
    {
        this.handleBreakBlock(worldIn, pos, state);
        super.breakBlock(worldIn, pos, state);
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
