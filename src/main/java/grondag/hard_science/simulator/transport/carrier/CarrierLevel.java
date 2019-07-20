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
package grondag.hard_science.simulator.transport.carrier;

import javax.annotation.Nullable;

public enum CarrierLevel {
    /**
     * Sub-sonic bus, moderate volume/power.
     */
    BOTTOM,

    /**
     * Sub-sonic, high capacity, multi-path bus.
     */
    MIDDLE,

    /**
     * Supersonic/superconducting switches and interconnects.
     */
    TOP;

    public boolean isBottom() {
        return this == BOTTOM;
    }

    public boolean isTop() {
        return this == TOP;
    }

    @Nullable
    public CarrierLevel above() {
        switch (this) {
        case BOTTOM:
            return MIDDLE;

        case MIDDLE:
            return TOP;

        case TOP:
        default:
            return null;
        }
    }

    @Nullable
    public CarrierLevel below() {
        switch (this) {
        case TOP:
            return MIDDLE;

        case MIDDLE:
            return BOTTOM;

        case BOTTOM:
        default:
            return null;
        }
    }
}
