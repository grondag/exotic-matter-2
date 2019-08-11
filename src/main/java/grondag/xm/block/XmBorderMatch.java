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

package grondag.xm.block;

import grondag.xm.api.connect.world.BlockTest;
import grondag.xm.api.connect.world.BlockTestContext;
import grondag.xm.api.modelstate.PrimitiveModelState;
import net.minecraft.block.BlockState;

@SuppressWarnings("rawtypes")
public class XmBorderMatch implements BlockTest<PrimitiveModelState> {
    private XmBorderMatch() {
    }

    public static final XmBorderMatch INSTANCE = new XmBorderMatch();

    @Override
    public boolean apply(BlockTestContext<PrimitiveModelState> context) {
        final PrimitiveModelState fromState = context.fromModelState();
        final PrimitiveModelState toState = context.toModelState();
        final BlockState toBlockState = context.toBlockState();
        final BlockState fromBlockState = context.fromBlockState();

        if (fromBlockState.getBlock() != toBlockState.getBlock() || fromState == null || toState == null) {
            return false;
        }

        return fromState.doShapeAndAppearanceMatch(toState) && fromState.species() == toState.species();
    }
}
