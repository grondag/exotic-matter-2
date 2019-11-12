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
package grondag.xm.api.orientation;

import static net.minecraft.util.math.Direction.DOWN;
import static net.minecraft.util.math.Direction.EAST;
import static net.minecraft.util.math.Direction.NORTH;
import static net.minecraft.util.math.Direction.SOUTH;
import static net.minecraft.util.math.Direction.UP;
import static net.minecraft.util.math.Direction.WEST;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

import grondag.xm.connect.helper.FaceEdgeHelper;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;

@API(status = EXPERIMENTAL)
public enum FaceEdge implements StringIdentifiable {
    TOP_EDGE(SOUTH, NORTH, UP, UP, UP, UP),
    BOTTOM_EDGE(NORTH, SOUTH, DOWN, DOWN, DOWN, DOWN),
    LEFT_EDGE(WEST, WEST, EAST, WEST, NORTH, SOUTH),
    RIGHT_EDGE(EAST, EAST, WEST, EAST, SOUTH, NORTH);

    // for a given face, which face is at the position identified by this enum?
    private final Direction relativeLookup[];

    @API(status = INTERNAL)
    public final int ordinalBit;

    public final String name;

    private FaceEdge(Direction... relativeLookup) {
        name = name().toLowerCase();
        this.relativeLookup = relativeLookup;
        ordinalBit = 1 << ordinal();
    }

    public FaceEdge clockwise() {
        switch (this) {
        case BOTTOM_EDGE:
            return LEFT_EDGE;
        case LEFT_EDGE:
            return TOP_EDGE;
        case RIGHT_EDGE:
            return BOTTOM_EDGE;
        case TOP_EDGE:
            return RIGHT_EDGE;
        default:
            return null;
        }
    }

    public FaceEdge counterClockwise() {
        switch (this) {
        case BOTTOM_EDGE:
            return RIGHT_EDGE;
        case LEFT_EDGE:
            return BOTTOM_EDGE;
        case RIGHT_EDGE:
            return TOP_EDGE;
        case TOP_EDGE:
            return LEFT_EDGE;
        default:
            return null;
        }
    }

    /**
     * Returns the block face next to this FaceSide on the given block face.
     */
    public Direction toWorld(Direction face) {
        return relativeLookup[face.ordinal()];
    }

    /**
     * Determines if the given sideFace is TOP, BOTTOM, DEFAULT_LEFT or
     * DEFAULT_RIGHT of onFace. If none (sideFace on same orthogonalAxis as onFace),
     */
    @Nullable
    public static FaceEdge fromWorld(Direction edgeFace, Direction onFace) {
        return FaceEdgeHelper.fromWorld(edgeFace, onFace);
    }

    public static final int COUNT = FaceEdgeHelper.COUNT;

    public static final FaceEdge fromOrdinal(int ordinal) {
        return FaceEdgeHelper.fromOrdinal(ordinal);
    }

    public static void forEach(Consumer<FaceEdge> consumer) {
        FaceEdgeHelper.forEach(consumer);
    }

    @Override
    public String asString() {
        return name;
    }
}
