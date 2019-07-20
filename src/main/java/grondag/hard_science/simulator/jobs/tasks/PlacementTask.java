package grondag.hard_science.simulator.jobs.tasks;

import javax.annotation.Nullable;

import grondag.fermion.serialization.NBTDictionary;
import grondag.hard_science.simulator.jobs.AbstractTask;
import grondag.hard_science.simulator.jobs.ITask;
import grondag.hard_science.simulator.jobs.TaskType;
import net.minecraft.nbt.CompoundTag;

/**
 * For placing blocks. Relies on procurement task for data.
 *
 */
public class PlacementTask extends AbstractTask
{
    private static final String NBT_PROCUREMENT_TASK_ID = NBTDictionary.claim("procTaskID");

    private int procurementTaskID;
    
    /** 
     * Don't use directly - lazily deserialized.
     */
    private BlockProcurementTask procurementTask;
    
    /**
     * Use for new instances. Creates dependency on input task.
     */
    public PlacementTask(BlockProcurementTask procurementTask)
    {
        super(true);
        this.procurementTaskID = procurementTask.getId();
        this.procurementTask = procurementTask;
        AbstractTask.link(procurementTask, this);
    }
    
    /** Use for deserialization */
    public PlacementTask()
    {
        super(false);
    }
    
    @Override
    public TaskType requestType()
    {
        return TaskType.PLACEMENT;
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
