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
package grondag.hard_science.simulator.transport.endpoint;

/**
 * Describes how a port is behaving or should behave in context of how it is
 * connected. Obtained from {@link Port#effectivePort(Port, int, int)}.
 * <p>
 * 
 * These outcomes are implied by the rules listed in PortFunction.
 *
 */
public enum PortMode {
    /**
     * Port is not adjacent to anything it can connect with.
     */
    DISCONNECTED(false),

    /**
     * Port is acting like a normal carrier port of its listed level.
     */
    CARRIER(true),

    /**
     * Port is acting as a bridge port and external circuit is isolated from
     * internal circuit. External circuit should be one level lower than internal.
     * Happens when bridge port mates with downlevel carrier or bridge ports.
     */
    BRIDGE(true),

    /**
     * Port is a direct port. This is the only result possible for a mated direct
     * port.
     */
    DIRECT(true),

    /**
     * Port cannot mate due to mismatched storage types.
     */
    NO_CONNECTION_STORAGE_TYPE(false),

    /**
     * Port cannot mate due to channel mismatch.
     */
    NO_CONNECTION_CHANNEL_MISMATCH(false),

    /**
     * Port cannot mate due to incompatible port types. For example, direct to
     * direct.
     */
    NO_CONNECTION_INCOMPATIBLE(false),

    /**
     * Port cannot mate due to too-wide level gap.
     */
    NO_CONNECTION_LEVEL_GAP(false);

    /**
     * True if this mode represents a state where port is attached to a carrier.
     */
    public final boolean isConnected;

    /**
     * True if this port instance can pass packets between two parents and should be
     * included in the domain carrier map.
     * <p>
     * 
     * If false and connected, then implies owning device can use this port to
     * send/receive packets.
     */
    public boolean isBridge() {
        return this == BRIDGE;
    }

    private PortMode(boolean isConnected) {
        this.isConnected = isConnected;
    }
}
