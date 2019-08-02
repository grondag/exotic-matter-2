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

package grondag.xm2.placement;

import java.util.function.BooleanSupplier;

import grondag.fermion.position.BlockRegion;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public interface IPlacementSpec {

    /**
     * Checks for obstacles (if matters according to stack settings) Check for world
     * boundaries. Adjusts selection to avoid obstacles (if configured to do so)
     * Returns true is selection can be placed and have a non-empty result. This is
     * called during preview and will not check every block position for large
     * regions. Also called before placement and placement will not occur if returns
     * false.
     * <P>
     * 
     * Detailed checked for every position are performed incrementally in
     * {@link #worldTask(EntityPlayerMP)}.
     */
    public boolean validate();

    /**
     * If stack is in selection mode, renders selection region that would result if
     * the selection region is terminated at the given position.
     * <p>
     * 
     * For CSG shapes, this may be a shape other than a cuboid.
     * <p>
     * 
     * If placement is valid (via {@link #validate()}) buffers quads or lines
     * showing where it will be in world. If placement is not valid because of
     * obstacles, uses color to indicate obstructed/invalid and show where at least
     * some of the obstacles are (if not too expensive).
     */

    void renderPreview(float tickDelta, ClientPlayerEntity player);

    /**
     * Encapsulates all work needed to build a spec and apply it to the world. What
     * it does will depend on the nature of the spec:
     * <p>
     * 
     * Non-virtual specs: single (real) blocks, excavation and exchange specs that
     * do not place or change virtual blocks will determine affected block positions
     * and directly submit new construction jobs in the player's active domain.
     * <p>
     * 
     * Virtual placement specs will place or modify virtual blocks in the world, and
     * associate those virtual blocks with the player's currently active build.
     * <p>
     * 
     * Build specs will compile the active build, capturing all virtual blocks
     * associated with the player's active build as entries, and then submit a new
     * construction job in the player's active domain. The entries will also be
     * saved with the build (for later re-used if desired) and the build closed.
     */
    public abstract BooleanSupplier worldTask(ServerPlayerEntity player);

    /**
     * True if builder is consistent with EXCAVATION placement result type.
     * 
     * @return
     */
    public boolean isExcavation();

    /**
     * Enumerates block positions potentially affected. Intended for use by species
     * selection, but could have other uses in future.
     */
    public BlockRegion region();

}
