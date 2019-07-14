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

package grondag.xm2.connect;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

import grondag.xm2.api.connect.state.SimpleJoinState;
import grondag.xm2.api.connect.world.BlockNeighbors;
import net.minecraft.util.math.Direction;

@API(status = INTERNAL)
public class SimpleJoinStateImpl implements SimpleJoinState {
    private final int joins;

    @Override
    public boolean isJoined(Direction face) {
	final int flag = 1 << face.ordinal();
	return (joins & flag) == flag;
    }

    @Override
    public int ordinal() {
	return (int) joins;
    }

    private SimpleJoinStateImpl(int joins) {
	this.joins = joins;
    }

    private static final Direction[] FACES = Direction.values();

    private static final SimpleJoinStateImpl JOINS[] = new SimpleJoinStateImpl[STATE_COUNT];

    static {
	for (int i = 0; i < 64; i++) {
	    JOINS[i] = new SimpleJoinStateImpl(i);
	}
    }

    public static SimpleJoinStateImpl fromOrdinal(int index) {
	return JOINS[index];
    }

    public static SimpleJoinStateImpl fromWorld(BlockNeighbors testResults) {
	return fromOrdinal(ordinalFromWorld(testResults));
    }

    public static int ordinalFromWorld(BlockNeighbors testResults) {
	byte j = 0;
	for (int i = 0; i < 6; i++) {
	    if (testResults.result(FACES[i])) {
		j |= (1 << i);
	    }
	}
	return j;
    }
}
