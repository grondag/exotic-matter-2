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
import grondag.xm2.state.ModelState;
import net.minecraft.block.BlockState;

public class XmBorderMatch implements BlockTest {
    private XmBorderMatch() {}
    
    public static final XmBorderMatch INSTANCE = new XmBorderMatch();
    
    @Override
    public boolean apply(BlockTestContext context) {
        final ModelState fromState = (ModelState)context.fromModelState();
        final ModelState toState = (ModelState)context.toModelState();
        final BlockState toBlockState = context.toBlockState();
        final BlockState fromBlockState = context.fromBlockState();
        
        if(fromBlockState.getBlock() != toBlockState.getBlock() || fromState == null || toState == null) {
            return false;
        }
        
        return fromState.doShapeAndAppearanceMatch(toState) && fromState.getSpecies() == toState.getSpecies();
    }
}