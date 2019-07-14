/*******************************************************************************
 * Copyright 2019 grondag
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package grondag.xm2.collision;

import com.google.common.collect.ImmutableList;

import grondag.fermion.config.FermionConfig;
import grondag.fermion.varia.Useful;
import grondag.xm2.model.state.ModelState;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class OptimizingBoxList implements Runnable {
    // singleton is fine because called from a single thread
    private static final OptimalBoxGenerator boxGen = new OptimalBoxGenerator();

    // PERF: may no longer need these?
    private ImmutableList<Box> wrapped;
    private ModelState modelState;
    private VoxelShape shape;

    OptimizingBoxList(FastBoxGenerator generator, ModelState modelState) {
	this.modelState = modelState;
	this.wrapped = generator.build();
	this.shape = makeShapeFromBoxes(wrapped);
    }

    protected ImmutableList<Box> getList() {
	return wrapped;
    }

    protected VoxelShape getShape() {
	return shape;
    }

    private static VoxelShape makeShapeFromBoxes(ImmutableList<Box> boxes) {
	if (boxes.isEmpty()) {
	    return VoxelShapes.empty();
	}
	VoxelShape shape = VoxelShapes.cuboid(boxes.get(0));
	final int limit = boxes.size();
	for (int i = 1; i < limit; i++) {
	    shape = VoxelShapes.union(shape, VoxelShapes.cuboid(boxes.get(i)));
	}
	return shape;
    }

    @Override
    public void run() {
	final OptimalBoxGenerator generator = boxGen;
	modelState.getShape().produceQuads(modelState, generator);

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
	    shape = makeShapeFromBoxes(wrapped);
	}
//        if((CollisionBoxDispatcher.QUEUE.size() & 0xFF) == 0)
//            System.out.println("Queue depth = " + CollisionBoxDispatcher.QUEUE.size());

	modelState = null;
    }
}
