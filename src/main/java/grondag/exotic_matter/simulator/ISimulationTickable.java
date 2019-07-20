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
package grondag.exotic_matter.simulator;

//TODO: move to Timeshare
public interface ISimulationTickable {
    /**
     * If true, then {@link #doOnTick(int)} will be called during world tick from
     * server thread. Is generally only checked at setup so result should not be
     * dynamic.
     */
    public default boolean doesUpdateOnTick() {
        return false;
    }

    /**
     * See {@link #doesUpdateOnTick()}
     */
    public default void doOnTick() {
    }

    /**
     * If true, then {@link #doOffTick(int)} will be called once per server tick
     * from simulation thread pool. Is generally only checked at setup so result
     * should not be dynamic.
     */
    public default boolean doesUpdateOffTick() {
        return false;
    }

    /**
     * See {@link #doesUpdateOffTick()}
     */
    public default void doOffTick() {
    }
}
