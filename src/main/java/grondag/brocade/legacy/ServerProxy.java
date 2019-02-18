package grondag.brocade.legacy;

import grondag.exotic_matter.statecache.IWorldStateCache;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
public class ServerProxy extends CommonProxy
{
    @Override
    public IWorldStateCache clientWorldStateCache()
    {
        throw new UnsupportedOperationException();
    }
}
