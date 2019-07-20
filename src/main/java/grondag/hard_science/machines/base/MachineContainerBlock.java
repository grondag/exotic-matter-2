package grondag.hard_science.machines.base;

import javax.annotation.Nonnull;

import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.hard_science.HardScience;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class MachineContainerBlock extends MachineBlock
{

    public MachineContainerBlock(String name, int guiID, ISuperModelState modelState)
    {
        super(name, guiID, modelState);
    }

    @Override
    public boolean onBlockActivated(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) 
    {
        // this is main difference for container blocks - activation happens server-side 
        if (world.isRemote || this.guiID < 0) {
            return true;
        }
        
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof MachineTileEntity)) 
        {
            return false;
        }
        player.openGui(HardScience.INSTANCE, this.guiID, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }
}
