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

import static org.apiguardian.api.API.Status.STABLE;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

import grondag.xm2.connect.helper.HorizontalFaceHelper;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

/**
 * A subset of {@link Direction}, includes only the face in the horizontal
 * plane.
 */
@API(status = STABLE)
public enum HorizontalFace implements StringIdentifiable {
    NORTH(Direction.NORTH), EAST(Direction.EAST), SOUTH(Direction.SOUTH), WEST(Direction.WEST);

    public final Direction face;

    public final Vec3i vector;

    public final String name;

    private HorizontalFace(Direction face) {
	this.name = this.name().toLowerCase();
	this.face = face;
	this.vector = face.getVector();
    }

    public HorizontalFace left() {
	if (this.ordinal() == 0) {
	    return HorizontalFace.values()[3];
	} else {
	    return HorizontalFace.values()[this.ordinal() - 1];
	}
    }

    public HorizontalFace right() {
	if (this.ordinal() == 3) {
	    return HorizontalFace.values()[0];
	} else {
	    return HorizontalFace.values()[this.ordinal() + 1];
	}
    }

    public static final int COUNT = HorizontalFaceHelper.COUNT;

    /**
     * Will return null if input is not a horizontal face.
     */
    @Nullable
    public static HorizontalFace find(Direction face) {
	return HorizontalFaceHelper.find(face);
    }

    public static final HorizontalFace fromOrdinal(int ordinal) {
	return HorizontalFaceHelper.fromOrdinal(ordinal);
    }

    public static void forEach(Consumer<HorizontalFace> consumer) {
	HorizontalFaceHelper.forEach(consumer);
    }

    @Override
    public String asString() {
	return name;
    }
}
