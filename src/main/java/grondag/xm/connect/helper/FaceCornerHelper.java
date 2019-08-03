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

import grondag.xm.api.connect.model.FaceCorner;
import grondag.xm.api.connect.model.FaceEdge;

@API(status = INTERNAL)
public abstract class FaceCornerHelper {
    private FaceCornerHelper() {
    }

    private static final FaceCorner[] VALUES = FaceCorner.values();
    public static final int COUNT = VALUES.length;
    private static FaceCorner[][] LOOKUP = new FaceCorner[4][4];

    static {
        for (FaceCorner corner : VALUES) {
            LOOKUP[corner.leftSide.ordinal()][corner.rightSide.ordinal()] = corner;
            LOOKUP[corner.rightSide.ordinal()][corner.leftSide.ordinal()] = corner;
        }
    }

    public static FaceCorner find(FaceEdge side1, FaceEdge side2) {
        return LOOKUP[side1.ordinal()][side2.ordinal()];
    }

    public static final FaceCorner fromOrdinal(int ordinal) {
        return VALUES[ordinal];
    }

    public static void forEach(Consumer<FaceCorner> consumer) {
        for (FaceCorner val : VALUES) {
            consumer.accept(val);
        }
    }
}
