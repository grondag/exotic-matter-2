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
package grondag.xm.api.texture;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.function.Consumer;

import org.apiguardian.api.API;

import grondag.fermion.spatial.Rotation;
import grondag.xm.texture.TextureOrientationHelper;

// TODO: Restore RANDOM_CONSISTENT to distinguish multi-block textures that can and can't have rotations intermingled per-plane.
// As is, have to assume can't, but 2x2 cobble would be an example that could.
@API(status = EXPERIMENTAL)
public enum TextureOrientation {
    IDENTITY(Rotation.ROTATE_NONE, false, false),
    ROTATE_90(Rotation.ROTATE_90, false, false),
    ROTATE_180(Rotation.ROTATE_180, false, false),
    ROTATE_270(Rotation.ROTATE_270, false, false),

    FLIP_U(Rotation.ROTATE_NONE, true, false),
    FLIP_U_ROTATE_90(Rotation.ROTATE_90, true, false),
    FLIP_U_ROTATE_180(Rotation.ROTATE_180, true, false),
    FLIP_U_ROTATE_270(Rotation.ROTATE_270, true, false),

    FLIP_V(Rotation.ROTATE_NONE, false, true),
    FLIP_V_ROTATE_90(Rotation.ROTATE_90, false, true),
    FLIP_V_ROTATE_180(Rotation.ROTATE_180, false, true),
    FLIP_V_ROTATE_270(Rotation.ROTATE_270, false, true),

    FLIP_UV(Rotation.ROTATE_NONE, true, true),
    FLIP_UV_ROTATE_90(Rotation.ROTATE_90, true, true),
    FLIP_UV_ROTATE_180(Rotation.ROTATE_180, true, true),
    FLIP_UV_ROTATE_270(Rotation.ROTATE_270, true, true);

    public final Rotation rotation;
    public final boolean flipU;
    public final boolean flipV;

    private TextureOrientation(Rotation rotation, boolean flipU, boolean flipV) {
        this.rotation = rotation;
        this.flipU = flipU;
        this.flipV = flipV;
    }

    public TextureOrientation clockwise() {
        return TextureOrientationHelper.clockwise(this);
    }

    public static TextureOrientation find(Rotation rotation, boolean flipU, boolean flipV) {
        return TextureOrientationHelper.find(rotation, flipU, flipV);
    }

    public static final TextureOrientation fromOrdinal(int ordinal) {
        return TextureOrientationHelper.fromOrdinal(ordinal);
    }

    public static void forEach(Consumer<TextureOrientation> consumer) {
        TextureOrientationHelper.forEach(consumer);
    }
}
