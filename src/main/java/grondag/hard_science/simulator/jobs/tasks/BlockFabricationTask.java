package grondag.hard_science.simulator.jobs.tasks;

import javax.annotation.Nullable;

import grondag.fermion.serialization.NBTDictionary;
import grondag.hard_science.simulator.jobs.AbstractTask;
import grondag.hard_science.simulator.jobs.ITask;
import grondag.hard_science.simulator.jobs.TaskType;
import net.minecraft.nbt.CompoundTag;

public class BlockFabricationTask extends AbstractTask
{
    private static final String NBT_PROCUREMENT_TASK_ID = NBTDictionary.claim("procTaskID");

    private int procurementTaskID;
    
    /** 
     * Don't use directly - lazily deserialized.
     */
    private @Nullable BlockProcurementTask procurementTask;
    
    /**
     * Use for new instances. Automatically
     * make procurement task dependent on this task.
     */
    public BlockFabricationTask(BlockProcurementTask procurementTask)
    {
        super(true);
        this.procurementTaskID = procurementTask.getId();
        this.procurementTask = procurementTask;
        AbstractTask.link(this, procurementTask);
    }
    
    /** Use for deserialization */
    public BlockFabricationTask()
    {
        super(false);
    }
    
    @Override
    public TaskType requestType()
    {
        return TaskType.BLOCK_FABRICATION;
    }

    @Override
    public void deserializeNBT(@Nullable CompoundTag tag)
    {
        super.deserializeNBT(tag);
        this.procurementTaskID = tag.getInt(NBT_PROCUREMENT_TASK_ID);
    }

    @Override
    public void serializeNBT(CompoundTag tag)
    {
        super.serializeNBT(tag);
        tag.putInt(NBT_PROCUREMENT_TASK_ID, this.procurementTaskID);
    }
    
    public BlockProcurementTask procurementTask()
    {
        if(this.procurementTask == null)
        {
            this.procurementTask = (BlockProcurementTask) ITask.taskFromId(procurementTaskID);
        }
        return this.procurementTask;
    }
}
