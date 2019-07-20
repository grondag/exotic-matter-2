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

package grondag.xm2.api.texture;

import grondag.fermion.world.Rotation;

// TODO: Restore RANDOM_CONSISTENT to distinguish multi-block textures that can and can't have rotations intermingled per-plane.
// As is, have to assume can't, but 2x2 cobble would be an example that could.

public enum TextureRotation {
    ROTATE_NONE(Rotation.ROTATE_NONE), ROTATE_90(Rotation.ROTATE_90), ROTATE_180(Rotation.ROTATE_180), ROTATE_270(Rotation.ROTATE_270),
    ROTATE_RANDOM(Rotation.ROTATE_NONE);

    public final Rotation rotation;

    private TextureRotation(Rotation rotation) {
        this.rotation = rotation;
    }
}
