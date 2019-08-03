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

import grondag.xm.api.connect.model.HorizontalFace;
import net.minecraft.util.math.Direction;

@API(status = INTERNAL)
public abstract class HorizontalFaceHelper {
    private HorizontalFaceHelper() {
    }

    private static final HorizontalFace[] VALUES = HorizontalFace.values();
    public static final int COUNT = VALUES.length;

    private static final HorizontalFace HORIZONTAL_FACE_LOOKUP[] = new HorizontalFace[6];

    static {
        for (HorizontalFace hFace : HorizontalFace.values()) {
            HORIZONTAL_FACE_LOOKUP[hFace.face.ordinal()] = hFace;
        }
    }

    public static HorizontalFace find(Direction face) {
        return HORIZONTAL_FACE_LOOKUP[face.ordinal()];
    }

    public static final HorizontalFace fromOrdinal(int ordinal) {
        return VALUES[ordinal];
    }

    public static void forEach(Consumer<HorizontalFace> consumer) {
        for (HorizontalFace val : VALUES) {
            consumer.accept(val);
        }
    }
}
