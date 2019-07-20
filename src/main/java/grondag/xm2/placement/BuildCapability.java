package grondag.xm2.placement;

import javax.annotation.Nullable;

import grondag.exotic_matter.simulator.Simulator;
import grondag.exotic_matter.simulator.domain.DomainUser;
import grondag.exotic_matter.simulator.domain.IUserCapability;
import grondag.exotic_matter.simulator.persistence.AssignedNumber;
import grondag.fermion.serialization.NBTDictionary;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.util.Identifier;

public class BuildCapability implements IUserCapability
{
    private static final String NBT_TAG_SELF = NBTDictionary.claim("buildCap");
    private static final String NBT_TAG_DATA = NBTDictionary.claim("buildCapData");

    private Object2IntOpenHashMap<Identifier> activeBuilds = new Object2IntOpenHashMap<>();

    private DomainUser user;
    
    /**
     * Retrieves active build in given dimension if exists,
     * creates new build in this domain and makes
     * it the active build for the player otherwise.
     */
    public Build getActiveBuild(Identifier dimensionID)
    {
        int buildID = this.activeBuilds.get(dimensionID);
        Build result = (Build)  Simulator.instance().assignedNumbersAuthority().get(buildID, AssignedNumber.BUILD);
        if(result == null || !result.isOpen())
        {
            BuildManager bm = this.user.getDomain().getCapability(BuildManager.class);
            if(bm != null)
            {
                result = bm.newBuild(dimensionID);
                this.activeBuilds.put(dimensionID, result.getId());
                this.user.getDomain().setDirty();
            }
        }
        return result;
    }
    
    /**
     * Makes given build the active build in its dimension.
     * Note that if the given build is not open, will be
     * re-assigned to a new build on retrieval.
     */
    public void setActiveBuild(Build build)
    {
        this.activeBuilds.put(build.dimensionID(), build.getId());
        this.user.getDomain().setDirty();
    }
    
    @Override
    public void deserializeNBT(@Nullable CompoundTag tag)
    {
        if(tag.containsKey(NBT_TAG_DATA))
        {
            ListTag nbtBuilds = (ListTag) tag.getTag(NBT_TAG_DATA);
            if(nbtBuilds != null && !nbtBuilds.isEmpty())
            {
                final int limit = nbtBuilds.size();
                for(int i = 0; i < limit; i++)
                {
                    String[] build = nbtBuilds.getString(i).split("#");
                    this.activeBuilds.put(new Identifier(build[0]), Integer.parseInt(build[1]));
                }
            }
        }
    }

    @Override
    public void serializeNBT(CompoundTag tag)
    {
        if(!this.activeBuilds.isEmpty())
        {
            ListTag nbtBuilds = new ListTag();
            
            for(Entry<Identifier> entry : this.activeBuilds.object2IntEntrySet())
            {
                nbtBuilds.add(new StringTag(entry.toString() + "#" + entry.getIntValue()));
            }
            tag.put(NBT_TAG_DATA, nbtBuilds);
        }
        
    }

    @Override
    public String tagName()
    {
        return NBT_TAG_SELF;
    }

    @Override
    public void setDomainUser(DomainUser user)
    {
        this.user = user;
    }

    @Override
    public boolean isSerializationDisabled()
    {
        return this.activeBuilds.isEmpty();
    }

}
