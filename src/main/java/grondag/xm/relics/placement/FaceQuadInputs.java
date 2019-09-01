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
package grondag.xm.relics.placement;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.xm.api.texture.TextureOrientation;

@API(status = Status.DEPRECATED)
@Deprecated
public class FaceQuadInputs {
    public final int textureOffset;
    public final TextureOrientation rotation;
    public final boolean flipU;
    public final boolean flipV;

    public FaceQuadInputs(int textureOffset, TextureOrientation rotation, boolean flipU, boolean flipV) {
        this.textureOffset = textureOffset;
        this.rotation = rotation;
        this.flipU = flipU;
        this.flipV = flipV;
    }
}
