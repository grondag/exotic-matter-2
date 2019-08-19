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

package grondag.xm.connect.helper;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.function.Consumer;

import org.apiguardian.api.API;

import grondag.xm.api.orientation.CubeEdge;
import net.minecraft.util.math.Direction;

@API(status = INTERNAL)
public abstract class CubeEdgeHelper {
    private CubeEdgeHelper() {
    }

    private static final CubeEdge[] VALUES = CubeEdge.values();
    public static final int COUNT = VALUES.length;
    private static final CubeEdge[][] CORNER_LOOKUP = new CubeEdge[6][6];

    static {
        for (CubeEdge corner : VALUES) {
            CORNER_LOOKUP[corner.face1.ordinal()][corner.face2.ordinal()] = corner;
            CORNER_LOOKUP[corner.face2.ordinal()][corner.face1.ordinal()] = corner;
        }
    }

    public static CubeEdge find(Direction face1, Direction face2) {
        return CORNER_LOOKUP[face1.ordinal()][face2.ordinal()];
    }

    public static final CubeEdge fromOrdinal(int ordinal) {
        return VALUES[ordinal];
    }

    public static void forEach(Consumer<CubeEdge> consumer) {
        for (CubeEdge val : VALUES) {
            consumer.accept(val);
        }
    }
}
