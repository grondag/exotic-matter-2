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

import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;

import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;

@API(status = STABLE)
public enum ClockwiseRotation implements StringIdentifiable {
    ROTATE_NONE(0, Direction.SOUTH),
    ROTATE_90(90, Direction.WEST),
    ROTATE_180(180, Direction.NORTH),
    ROTATE_270(270, Direction.EAST);

    public final String name;

    /**
     * Useful for locating model file names that use degrees as a suffix.
     */
    public final int degrees;

    /**
     * Opposite of degress - useful for GL transforms. 0 and 180 are same, 90 and
     * 270 are flipped
     */
    public final int degreesInverse;

    /**
     * Horizontal face that corresponds to this rotation for SuperBlocks that have a
     * single rotated face.
     */
    public final Direction horizontalFace;

    private static ClockwiseRotation[] FROM_HORIZONTAL_FACING = new ClockwiseRotation[6];

    static {
        FROM_HORIZONTAL_FACING[Direction.NORTH.ordinal()] = ROTATE_180;
        FROM_HORIZONTAL_FACING[Direction.EAST.ordinal()] = ROTATE_270;
        FROM_HORIZONTAL_FACING[Direction.SOUTH.ordinal()] = ROTATE_NONE;
        FROM_HORIZONTAL_FACING[Direction.WEST.ordinal()] = ROTATE_90;
        FROM_HORIZONTAL_FACING[Direction.UP.ordinal()] = ROTATE_NONE;
        FROM_HORIZONTAL_FACING[Direction.DOWN.ordinal()] = ROTATE_NONE;
    }

    private ClockwiseRotation(int degrees, Direction horizontalFace) {
        this.name = this.name().toLowerCase();
        this.degrees = degrees;
        this.degreesInverse = (360 - degrees) % 360;
        this.horizontalFace = horizontalFace;
    }

    @Override
    public String asString() {
        return name;
    }

    public ClockwiseRotation clockwise() {
        switch (this) {
        case ROTATE_180:
            return ROTATE_270;
        case ROTATE_270:
            return ROTATE_NONE;
        case ROTATE_90:
            return ROTATE_180;
        case ROTATE_NONE:
        default:
            return ROTATE_90;
        }
    }

    /**
     * Gives the rotation with horiztonalFace matching the given NSEW face For up
     * and down will return ROTATE_NONE
     */
    public static ClockwiseRotation fromHorizontalFacing(Direction face) {
        return FROM_HORIZONTAL_FACING[face.ordinal()];
    }
}
