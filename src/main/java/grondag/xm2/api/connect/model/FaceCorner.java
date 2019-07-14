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

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.apiguardian.api.API.Status.STABLE;

import java.util.function.Consumer;

import org.apiguardian.api.API;

import grondag.xm2.connect.helper.FaceCornerHelper;
import net.minecraft.util.StringIdentifiable;

@API(status = STABLE)
public enum FaceCorner implements StringIdentifiable {
    TOP_LEFT(FaceEdge.LEFT_EDGE, FaceEdge.TOP_EDGE), TOP_RIGHT(FaceEdge.TOP_EDGE, FaceEdge.RIGHT_EDGE),
    BOTTOM_LEFT(FaceEdge.BOTTOM_EDGE, FaceEdge.LEFT_EDGE), BOTTOM_RIGHT(FaceEdge.RIGHT_EDGE, FaceEdge.BOTTOM_EDGE);

    /**
     * Face edge that is counterclockwise from this block corner.
     */
    public final FaceEdge leftSide;

    /**
     * Face edge that is clockwise from this block corner.
     */
    public final FaceEdge rightSide;

    public final String name;

    @API(status = INTERNAL)
    public final int ordinalBit;

    private FaceCorner(FaceEdge leftSide, FaceEdge rightSide) {
	this.name = this.name().toLowerCase();
	this.leftSide = leftSide;
	this.rightSide = rightSide;
	this.ordinalBit = 1 << this.ordinal();
    }

    public static final int COUNT = FaceCornerHelper.COUNT;

    public static FaceCorner find(FaceEdge side1, FaceEdge side2) {
	return FaceCornerHelper.find(side1, side2);
    }

    public static final FaceCorner fromOrdinal(int ordinal) {
	return FaceCornerHelper.fromOrdinal(ordinal);
    }

    public static void forEach(Consumer<FaceCorner> consumer) {
	FaceCornerHelper.forEach(consumer);
    }

    @Override
    public String asString() {
	return name;
    }
}
