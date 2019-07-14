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

package grondag.xm2.api.connect.model;

import static grondag.xm2.api.connect.model.ClockwiseRotation.ROTATE_180;
import static grondag.xm2.api.connect.model.ClockwiseRotation.ROTATE_270;
import static grondag.xm2.api.connect.model.ClockwiseRotation.ROTATE_90;
import static grondag.xm2.api.connect.model.ClockwiseRotation.ROTATE_NONE;
import static org.apiguardian.api.API.Status.INTERNAL;
import static org.apiguardian.api.API.Status.STABLE;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

import grondag.xm2.connect.helper.BlockEdgeHelper;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3i;

/**
 * Defines the twelve edges of a block and the relative position of neighboring
 * blocks diagonally adjacent to those edges.
 */
@API(status = STABLE)
public enum BlockEdge implements StringIdentifiable {
    DOWN_SOUTH(Direction.DOWN, Direction.SOUTH, ROTATE_180), DOWN_WEST(Direction.DOWN, Direction.WEST, ROTATE_270),
    DOWN_NORTH(Direction.DOWN, Direction.NORTH, ROTATE_NONE), DOWN_EAST(Direction.DOWN, Direction.EAST, ROTATE_90),
    UP_NORTH(Direction.UP, Direction.NORTH, ROTATE_180), UP_EAST(Direction.UP, Direction.EAST, ROTATE_90),
    UP_SOUTH(Direction.UP, Direction.SOUTH, ROTATE_NONE), UP_WEST(Direction.UP, Direction.WEST, ROTATE_270),
    NORTH_EAST(Direction.NORTH, Direction.EAST, ROTATE_90), NORTH_WEST(Direction.NORTH, Direction.WEST, ROTATE_270),
    SOUTH_EAST(Direction.SOUTH, Direction.EAST, ROTATE_270), SOUTH_WEST(Direction.SOUTH, Direction.WEST, ROTATE_90);

    public final Direction face1;
    public final Direction face2;
    public final String name;

    /**
     * Used to position models like stairs/wedges. Representation rotation around
     * the parallel axis such that face1 and face2 are most occluded. Based on
     * "default" model occluding north and down faces. Use the axis implied by
     * face1.
     */

    public final ClockwiseRotation rotation;

    public final Vec3i vector;

    /**
     * Ordinal sequence that includes all faces, corner and far corners. Used to
     * index them in a mixed array.
     */
    @API(status = INTERNAL)
    public final int superOrdinal;

    @API(status = INTERNAL)
    public final int superOrdinalBit;

    /**
     * Will be null if not a horizontal edge.
     */
    @Nullable
    public final HorizontalEdge horizontalEdge;

    private BlockEdge(Direction face1, Direction face2, ClockwiseRotation rotation) {
        this.name = this.name().toLowerCase();
        this.face1 = face1;
        this.face2 = face2;
        this.rotation = rotation;
        superOrdinal = 6 + this.ordinal();
        superOrdinalBit = 1 << superOrdinal;

        Vec3i v1 = face1.getVector();
        Vec3i v2 = face2.getVector();
        vector = new Vec3i(v1.getX() + v2.getX(), v1.getY() + v2.getY(), v1.getZ() + v2.getZ());

        if (face1.getAxis() == Axis.Y
                || face2.getAxis() == Axis.Y)
            horizontalEdge = null;
        else
            horizontalEdge = HorizontalEdge.find(HorizontalFace.find(face1), HorizontalFace.find(face2));
    }

    public static final int COUNT = BlockEdgeHelper.COUNT;

    /**
     * Will be null if the inputs do not specify an edge.
     */
    @Nullable
    public static BlockEdge find(Direction face1, Direction face2) {
        return BlockEdgeHelper.find(face1, face2);
    }

    public static final BlockEdge fromOrdinal(int ordinal) {
        return BlockEdgeHelper.fromOrdinal(ordinal);
    }

    public static void forEach(Consumer<BlockEdge> consumer) {
        BlockEdgeHelper.forEach(consumer);
    }

    @Override
    public String asString() {
        return name;
    }
}
