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
package grondag.xm.virtual;

import static org.apiguardian.api.API.Status.INTERNAL;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;
import org.apiguardian.api.API;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@API(status = INTERNAL)
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
