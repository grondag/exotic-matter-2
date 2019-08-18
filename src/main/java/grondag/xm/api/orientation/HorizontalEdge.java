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

import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

import grondag.xm.connect.helper.HorizontalCornerHelper;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Vec3i;

/**
 * A subset of {@link BlockEdge}, includes only the edges in the horizontal
 * plane.
 */
@API(status = STABLE)
public enum HorizontalEdge implements StringIdentifiable {
    NORTH_EAST(HorizontalFace.NORTH, HorizontalFace.EAST), NORTH_WEST(HorizontalFace.NORTH, HorizontalFace.WEST),
    SOUTH_EAST(HorizontalFace.SOUTH, HorizontalFace.EAST), SOUTH_WEST(HorizontalFace.SOUTH, HorizontalFace.WEST);

    public final HorizontalFace face1;
    public final HorizontalFace face2;

    public final Vec3i vector;

    public final String name;

    private HorizontalEdge(HorizontalFace face1, HorizontalFace face2) {
        this.name = this.name().toLowerCase();
        this.face1 = face1;
        this.face2 = face2;
        this.vector = new Vec3i(face1.face.getVector().getX() + face2.face.getVector().getX(), 0,
                face1.face.getVector().getZ() + face2.face.getVector().getZ());
    }

    public static final int COUNT = HorizontalCornerHelper.COUNT;

    /**
     * Will return null if inputs do not specify a horizontal block edge.
     */
    @Nullable
    public static HorizontalEdge find(HorizontalFace face1, HorizontalFace face2) {
        return HorizontalCornerHelper.find(face1, face2);
    }

    public static HorizontalEdge fromOrdinal(int ordinal) {
        return HorizontalCornerHelper.fromOrdinal(ordinal);
    }

    public static void forEach(Consumer<HorizontalEdge> consumer) {
        HorizontalCornerHelper.forEach(consumer);
    }

    @Override
    public String asString() {
        return name;
    }
}
