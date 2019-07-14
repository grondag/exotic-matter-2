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

package grondag.xm2.block;

import grondag.xm2.connect.api.world.BlockTest;
import grondag.xm2.connect.api.world.BlockTestContext;
import grondag.xm2.model.state.ModelState;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

// For masonry, true result means border IS present
public class XmMasonryMatch implements BlockTest {
    private XmMasonryMatch() {}
    
    public static final XmMasonryMatch INSTANCE = new XmMasonryMatch();
    
    @Override
    public boolean apply(BlockTestContext context) {
        
        if(context.fromModelState() == null) {
            return false;
        }
        
        final ModelState fromState = (ModelState)context.fromModelState();
        final ModelState toState = (ModelState)context.toModelState();
        final BlockState toBlockState = context.toBlockState();
        final BlockState fromBlockState = context.fromBlockState();
        final BlockPos toPos = context.toPos();
        
        // if not a sibling, mortar if against full opaque
        if(fromBlockState.getBlock() != toBlockState.getBlock() || toState == null) {
            return toBlockState.isFullOpaque(context.world(), toPos);
        }
        
        // no mortar between siblings with same species
        if(fromState.getSpecies() == toState.getSpecies()) {
            return false;
        };
        
        final BlockPos fromPos = context.fromPos();
        
        // between siblings, only mortar on three sides of cube
        // (other sibling will do the mortar on other sides)
        return(toPos.getX() == fromPos.getX() + 1 
                || toPos.getY() == fromPos.getY() - 1
                || toPos.getZ() == fromPos.getZ() + 1);
    }
}
