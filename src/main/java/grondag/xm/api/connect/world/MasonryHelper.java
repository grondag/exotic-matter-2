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
package grondag.xm.api.connect.world;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

import grondag.xm.api.modelstate.SimpleModelState;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

// For masonry, true result means border IS present
@API(status = EXPERIMENTAL)
public class MasonryHelper implements BlockTest<SimpleModelState> {
    private MasonryHelper() {
    }

    private static final ThreadLocal<MasonryHelper> POOL = ThreadLocal.withInitial(MasonryHelper::new);

    public static BlockTest<SimpleModelState> wrap(BlockTest<SimpleModelState> test) {
        MasonryHelper result = POOL.get();
        result.test = test;
        return result;
    }
    
    private BlockTest<SimpleModelState> test;
    
    @Override
    public boolean apply(BlockTestContext<SimpleModelState> context) {

        if (context.fromModelState() == null) {
            return false;
        }

        final BlockState toBlockState = context.toBlockState();
        final BlockPos toPos = context.toPos();
        
        // if not a sibling, mortar if against full opaque
        if (!test.apply(context)) {
            return toBlockState.isFullOpaque(context.world(), toPos);
        }

        final BlockPos fromPos = context.fromPos();

        // between siblings, only mortar on three sides of cube
        // (other sibling will do the mortar on other sides)
        return (toPos.getX() == fromPos.getX() + 1 || toPos.getY() == fromPos.getY() - 1 || toPos.getZ() == fromPos.getZ() + 1);
    }
}
