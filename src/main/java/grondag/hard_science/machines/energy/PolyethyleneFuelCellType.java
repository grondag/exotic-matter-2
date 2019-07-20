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
package grondag.hard_science.machines.energy;

public class PolyethyleneFuelCellType {
    public static final PolyethyleneFuelCellType BASIC = new PolyethyleneFuelCellType(false);
    public static final PolyethyleneFuelCellType ADVANCED = new PolyethyleneFuelCellType(true);

    /**
     * If true, cell(s) is/are coupled with a thermoelectric generator to convert
     * waste heat and boost efficiency.
     */
    public final boolean hasThermalCapture;

    /**
     * How good we are at turning PE into electricity. Includes benefit of
     * thermoelectric if present.
     */
    public final float conversionEfficiency;

    /**
     * To compute actual fuel usage.
     */
    public final float fuelNanoLitersPerJoule;

    public final float joulesPerFuelNanoLiters;

    private PolyethyleneFuelCellType(boolean hasThermalCapture) {
        this.hasThermalCapture = hasThermalCapture;
        this.conversionEfficiency = hasThermalCapture ? MachinePower.POLYETHYLENE_BOOSTED_CONVERSION_EFFICIENCY
                : MachinePower.POLYETHYLENE_CONVERSION_EFFICIENCY;
        this.joulesPerFuelNanoLiters = MachinePower.JOULES_PER_POLYETHYLENE_NANOLITER * this.conversionEfficiency;
        this.fuelNanoLitersPerJoule = 1f / this.joulesPerFuelNanoLiters;

    }
}
