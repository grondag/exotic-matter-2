package grondag.hard_science.machines.impl.processing;

import grondag.hard_science.machines.base.MachineTileEntityTickable;
import net.minecraftforge.items.IItemHandler;


public class MicronizerTileEntity extends MachineTileEntityTickable
{

    @Override
    public IItemHandler getItemHandler()
    {
        if(this.world == null || this.world.isRemote) return null;
        if(this.machine() == null) return null;
        return this.machine().getBufferManager();
    }
   
}
