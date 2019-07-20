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
package grondag.hard_science.machines.support;

import javax.annotation.Nonnull;

import grondag.exotic_matter.placement.SuperItemBlock;
import grondag.hard_science.machines.base.MachineBlock;
import net.minecraft.item.ItemStack;

public class MachineItemBlock extends SuperItemBlock {

    public static final int MAX_DAMAGE = 100;

    public static final int CAPACITY_COLOR = 0xFF6080FF;

    public MachineItemBlock(MachineBlock block) {
        super(block);
        this.setMaxDamage(MAX_DAMAGE);
    }

    @Override
    public int getRGBDurabilityForDisplay(@Nonnull ItemStack stack) {
        return CAPACITY_COLOR;
    }

}
