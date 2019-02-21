package grondag.brocade.collision;

import com.google.common.collect.ImmutableList;

import grondag.fermion.config.FermionConfig;
import grondag.brocade.model.state.ISuperModelState;
import grondag.fermion.varia.Useful;
import net.minecraft.util.math.BoundingBox;

public class OptimizingBoxList implements Runnable {
    // singleton is fine because called from a single thread
    private static final OptimalBoxGenerator boxGen = new OptimalBoxGenerator();

    private ImmutableList<BoundingBox> wrapped;
    private ISuperModelState modelState;

    OptimizingBoxList(ImmutableList<BoundingBox> initialList, ISuperModelState modelState) {
        this.wrapped = initialList;
        this.modelState = modelState;
    }

    protected ImmutableList<BoundingBox> getList() {
        return wrapped;
    }

    @Override
    public void run() {
        final OptimalBoxGenerator generator = boxGen;
        modelState.getShape().meshFactory().produceShapeQuads(modelState, generator);

//        generator.generateCalibrationOutput();

        final int oldSize = wrapped.size();
        double oldVolume = Useful.volumeAABB(wrapped);
        double trueVolume = generator.prepare();
        if (trueVolume == 0)
            assert oldSize == 0 : "Fast collision box non-empty but detailed is empty";
        else if (trueVolume != -1) {
            if (oldSize > FermionConfig.BLOCKS.collisionBoxBudget
                    || Math.abs(trueVolume - oldVolume) > OptimalBoxGenerator.VOXEL_VOLUME * 2)
                wrapped = generator.build();
        }
//        if((CollisionBoxDispatcher.QUEUE.size() & 0xFF) == 0)
//            System.out.println("Queue depth = " + CollisionBoxDispatcher.QUEUE.size());

        modelState = null;
    }
}
