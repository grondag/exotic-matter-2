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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;

@Environment(EnvType.CLIENT)
public class VirtualBlockEntityRenderer extends BlockEntityRenderer<VirtualBlockEntityWithRenderer> {

    public static final VirtualBlockEntityRenderer INSTANCE = new VirtualBlockEntityRenderer();

    @Override
    public void render(VirtualBlockEntityWithRenderer be, double double_1, double double_2, double double_3, float float_1, int int_1) {
        if (!be.isVirtual() || !((VirtualBlockEntity) be).isVisible())
            return;

        // TODO: actually render - use 1.12 SuperBlockTESR as starting point
        super.render(be, double_1, double_2, double_3, float_1, int_1);
    }
}
