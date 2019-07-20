package grondag.hard_science.simulator.jobs.tasks;

import javax.annotation.Nonnull;

import grondag.hard_science.simulator.jobs.AbstractPositionedStackTask;
import grondag.hard_science.simulator.jobs.ITask;
import grondag.hard_science.simulator.jobs.Job;
import grondag.hard_science.simulator.jobs.TaskType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class BlockProcurementTask extends AbstractPositionedStackTask
{
    /**
     * Use for new instances. Note the position is the position
     * for block placement. The position of the stack, once procured,
     * will be stored in the stack itself.<p>
     * 
     * Note that stack may be updated by block fabricator if
     * material substitution occurs.
     * 
     */
    public BlockProcurementTask(BlockPos pos, ItemStack stack)
    {
        super(pos, stack);
    }
    
    /** Use for deserialization */
    public BlockProcurementTask()
    {
        super();
    }
    
    @Override
    public boolean initialize(@Nonnull Job job)
    {
        //TODO: for now always assuming have to fabricate
        BlockFabricationTask fabTask = new BlockFabricationTask(this);
        job.addTask(fabTask);
        return super.initialize(job);
    }

    @Override
    public TaskType requestType()
    {
        return TaskType.BLOCK_PROCUREMENT;
    }

    @Override
    public void onAntecedentTerminated(ITask antecedent)
    {
        //TODO: for now, procurement tasks done
        // as soon as fabrication is done
        super.onAntecedentTerminated(antecedent);
        if(antecedent.getStatus().didEndWithoutCompleting())
        {
            this.cancel();
        }
        else
        {
            this.complete();
        }
    }
    
    
}
