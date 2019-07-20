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
package grondag.hard_science.simulator.transport;

public enum TransportMode {
    /**
     * Transport via connected bus without packaging. Requires that from node be
     * able to send raw bulkResource and to node can accept raw bulkResource.
     */
    CONNECTED_DIRECT,

    /**
     * Transport via connected bus inside a package. Used when bulkResource was
     * already packaged for some other reason (previous leg of trip) or because
     * receiver requires it. (Hold in storage for drone pickup, for example.)
     */
    CONNECTED_PACKAGED,

    /**
     * Drone will pick up at start node and drop off at target node. Must always be
     * packaged.
     */
    DRONE_PACKAGED;

    public boolean isConnected() {
        return this != DRONE_PACKAGED;
    }
}
