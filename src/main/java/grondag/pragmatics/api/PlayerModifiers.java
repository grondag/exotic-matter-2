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
package grondag.pragmatics.api;

import grondag.pragmatics.impl.PlayerModifierAccess;
import net.minecraft.entity.player.PlayerEntity;

public interface PlayerModifiers {
    static boolean isShiftPressed(PlayerEntity player) {
        return PlayerModifierAccess.isShiftPressed(player);
    }

    static boolean isControlPressed(PlayerEntity player) {
        return PlayerModifierAccess.isControlPressed(player);
    }

    static boolean isAltPressed(PlayerEntity player) {
        return PlayerModifierAccess.isAltPressed(player);
    }

    static boolean isSuperPressed(PlayerEntity player) {
        return PlayerModifierAccess.isSuperPressed(player);
    }
}
