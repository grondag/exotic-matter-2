package grondag.xm2.block.virtual;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ExcavationRenderTask {

    /**
     * Called when task is complete or canceled at the given block position
     * @param consumer
     */
    void addCompletionListener(Consumer<BlockPos> consumer);

    boolean isComplete();
    
    boolean isExchange();

    void forEachPosition(Consumer<BlockPos> consumer);
    
    World world();
    
    boolean visibleTo(PlayerEntity player);
}
