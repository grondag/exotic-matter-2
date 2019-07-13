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

package grondag.xm2.primitives;

import grondag.fermion.world.Rotation;
import grondag.xm2.painting.Surface;
import grondag.xm2.primitives.polygon.IMutablePolygon;
import grondag.xm2.primitives.stream.IWritablePolyStream;
import grondag.xm2.surface.impl.XmSurfaceImpl;
import net.minecraft.util.math.Direction;

public class CubeInputs {
	public static final int NO_SURFACE = -1;
	
    public float u0;
    public float v0;
    public float u1;
    public float v1;
    public String textureName = "";
    public int color = 0xFFFFFFFF;
    public Rotation textureRotation = Rotation.ROTATE_NONE;
    public boolean rotateBottom = false;
    public boolean isOverlay = false;
    public boolean isItem = false;
    public boolean isFullBrightness = false;
    public Surface surfaceInstance;
    public XmSurfaceImpl surface;

    public CubeInputs() {
        // Minimum needed to prevent NPE
        this.textureRotation = Rotation.ROTATE_NONE;
        this.surfaceInstance = Surface.NO_SURFACE;
    }

    public CubeInputs(int color, Rotation textureRotation, String textureName, boolean flipU, boolean flipV,
            boolean isOverlay, boolean isItem) {
        this.color = color;
        this.textureRotation = textureRotation;
        this.textureName = textureName;
        this.isOverlay = isOverlay;
        this.isItem = isItem;
        this.u0 = flipU ? 1 : 0;
        this.v0 = flipV ? 1 : 0;
        this.u1 = flipU ? 0 : 1;
        this.v1 = flipV ? 0 : 1;
        this.rotateBottom = true;
        this.surfaceInstance = Surface.NO_SURFACE;
    }

    public void appendFace(IWritablePolyStream stream, Direction side) {
        final IMutablePolygon q = stream.writer();

        q.setLockUV(0, true);
        q.setRotation(0, (rotateBottom && side == Direction.DOWN) ? this.textureRotation.clockwise().clockwise()
                : this.textureRotation);
        q.setTextureName(0, this.textureName);
        q.setSurface(this.surfaceInstance);
        q.surface(this.surface);

        float minBound = this.isOverlay ? -0.0002f : 0.0f;
        float maxBound = this.isOverlay ? 1.0002f : 1.0f;
        q.setNominalFace(side);

        switch (side) {
        case UP:
            q.setVertex(0, minBound, maxBound, minBound, u0, v0, this.color);
            q.setVertex(1, minBound, maxBound, maxBound, u0, v1, this.color);
            q.setVertex(2, maxBound, maxBound, maxBound, u1, v1, this.color);
            q.setVertex(3, maxBound, maxBound, minBound, u1, v0, this.color);
            break;

        case DOWN:
            q.setVertex(0, minBound, minBound, maxBound, u1, v1, this.color);
            q.setVertex(1, minBound, minBound, minBound, u1, v0, this.color);
            q.setVertex(2, maxBound, minBound, minBound, u0, v0, this.color);
            q.setVertex(3, maxBound, minBound, maxBound, u0, v1, this.color);
            break;

        case WEST:
            q.setVertex(0, minBound, maxBound, minBound, u0, v0, this.color);
            q.setVertex(1, minBound, minBound, minBound, u0, v1, this.color);
            q.setVertex(2, minBound, minBound, maxBound, u1, v1, this.color);
            q.setVertex(3, minBound, maxBound, maxBound, u1, v0, this.color);
            break;

        case EAST:
            q.setVertex(0, maxBound, maxBound, maxBound, u0, v0, this.color);
            q.setVertex(1, maxBound, minBound, maxBound, u0, v1, this.color);
            q.setVertex(2, maxBound, minBound, minBound, u1, v1, this.color);
            q.setVertex(3, maxBound, maxBound, minBound, u1, v0, this.color);
            break;

        case NORTH:
            q.setVertex(0, maxBound, maxBound, minBound, u0, v0, this.color);
            q.setVertex(1, maxBound, minBound, minBound, u0, v1, this.color);
            q.setVertex(2, minBound, minBound, minBound, u1, v1, this.color);
            q.setVertex(3, minBound, maxBound, minBound, u1, v0, this.color);
            break;

        case SOUTH:
            q.setVertex(0, minBound, maxBound, maxBound, u0, v0, this.color);
            q.setVertex(1, minBound, minBound, maxBound, u0, v1, this.color);
            q.setVertex(2, maxBound, minBound, maxBound, u1, v1, this.color);
            q.setVertex(3, maxBound, maxBound, maxBound, u1, v0, this.color);
            break;
        }

        stream.append();
    }
}
