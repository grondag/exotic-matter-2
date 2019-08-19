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

import net.minecraft.util.math.Direction;

@SuppressWarnings("rawtypes")
public enum OrientationType {
    NONE(NoOrientation.class),
    AXIS(Direction.Axis.class),
    HORIZONTAL_FACE(HorizontalFace.class),
    FACE(Direction.class),
    EDGE(CubeEdge.class),
    ROTATION(CubeRotation.class),
    CORNER(CubeCorner.class);

    public final Class<? extends Enum> enumClass;

    private OrientationType(Class<? extends Enum> enumClass) {
        this.enumClass = enumClass;
    }
}
