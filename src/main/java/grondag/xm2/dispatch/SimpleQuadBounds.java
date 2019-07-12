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

package grondag.xm2.dispatch;

import net.minecraft.util.math.Direction;

public class SimpleQuadBounds {
    public Direction face;
    public float x0;
    public float y0;
    public float x1;
    public float y1;
    public float depth;
    public Direction topFace;

    public SimpleQuadBounds(Direction face, float x0, float y0, float x1, float y1, float depth, Direction topFace) {
        this.face = face;
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.depth = depth;
        this.topFace = topFace;
    }
}
