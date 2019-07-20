package grondag.hard_science.simulator.jobs;

import javax.annotation.Nullable;

import grondag.fermion.serialization.NBTDictionary;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;

/**
 * Task with a position attribute.
 */
public abstract class AbstractPositionedTask extends AbstractTask
{
    private static final String NBT_TASK_POSITION = NBTDictionary.claim("taskPos");

    private BlockPos pos;
  
    protected AbstractPositionedTask(BlockPos pos)
    {
        super(true);
        this.pos = pos;
    }
    
    protected AbstractPositionedTask()
    {
        super(false);
    }

    @Override
    public void deserializeNBT(@Nullable CompoundTag tag)
    {
        super.deserializeNBT(tag);
        this.pos = BlockPos.fromLong(tag.getLong(NBT_TASK_POSITION));
    }

    @Override
    public void serializeNBT(CompoundTag tag)
    {
        super.serializeNBT(tag);
        tag.putLong(NBT_TASK_POSITION, this.pos.asLong());
    }
    
    public BlockPos pos()
    {
        return this.pos;
    }

}
