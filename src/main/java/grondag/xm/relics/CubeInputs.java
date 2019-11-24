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
package grondag.xm.relics;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.texture.TextureOrientation;
import net.minecraft.util.math.Direction;

@API(status = Status.DEPRECATED)
@Deprecated
public class CubeInputs {
	public float u0;
	public float v0;
	public float u1;
	public float v1;
	public String textureName = "";
	public int color = 0xFFFFFFFF;
	public TextureOrientation textureRotation = TextureOrientation.IDENTITY;
	public boolean rotateBottom = false;
	public boolean isOverlay = false;
	public boolean isItem = false;
	public boolean isFullBrightness = false;
	public XmSurface surface;

	public CubeInputs() {
		// Minimum needed to prevent NPE
		textureRotation = TextureOrientation.IDENTITY;
	}

	public CubeInputs(int color, TextureOrientation textureRotation, String textureName, boolean flipU, boolean flipV, boolean isOverlay, boolean isItem) {
		this.color = color;
		this.textureRotation = textureRotation;
		this.textureName = textureName;
		this.isOverlay = isOverlay;
		this.isItem = isItem;
		u0 = flipU ? 1 : 0;
		v0 = flipV ? 1 : 0;
		u1 = flipU ? 0 : 1;
		v1 = flipV ? 0 : 1;
		rotateBottom = true;
	}

	public void appendFace(WritableMesh stream, Direction side) {
		final MutablePolygon q = stream.writer();

		q.lockUV(0, true);
		q.rotation(0, (rotateBottom && side == Direction.DOWN) ? textureRotation.clockwise().clockwise() : textureRotation);
		q.sprite(0, textureName);
		q.surface(surface);

		final float minBound = isOverlay ? -0.0002f : 0.0f;
		final float maxBound = isOverlay ? 1.0002f : 1.0f;
		q.nominalFace(side);

		switch (side) {
		case UP:
			q.vertex(0, minBound, maxBound, minBound, u0, v0, color);
			q.vertex(1, minBound, maxBound, maxBound, u0, v1, color);
			q.vertex(2, maxBound, maxBound, maxBound, u1, v1, color);
			q.vertex(3, maxBound, maxBound, minBound, u1, v0, color);
			break;

		case DOWN:
			q.vertex(0, minBound, minBound, maxBound, u1, v1, color);
			q.vertex(1, minBound, minBound, minBound, u1, v0, color);
			q.vertex(2, maxBound, minBound, minBound, u0, v0, color);
			q.vertex(3, maxBound, minBound, maxBound, u0, v1, color);
			break;

		case WEST:
			q.vertex(0, minBound, maxBound, minBound, u0, v0, color);
			q.vertex(1, minBound, minBound, minBound, u0, v1, color);
			q.vertex(2, minBound, minBound, maxBound, u1, v1, color);
			q.vertex(3, minBound, maxBound, maxBound, u1, v0, color);
			break;

		case EAST:
			q.vertex(0, maxBound, maxBound, maxBound, u0, v0, color);
			q.vertex(1, maxBound, minBound, maxBound, u0, v1, color);
			q.vertex(2, maxBound, minBound, minBound, u1, v1, color);
			q.vertex(3, maxBound, maxBound, minBound, u1, v0, color);
			break;

		case NORTH:
			q.vertex(0, maxBound, maxBound, minBound, u0, v0, color);
			q.vertex(1, maxBound, minBound, minBound, u0, v1, color);
			q.vertex(2, minBound, minBound, minBound, u1, v1, color);
			q.vertex(3, minBound, maxBound, minBound, u1, v0, color);
			break;

		case SOUTH:
			q.vertex(0, minBound, maxBound, maxBound, u0, v0, color);
			q.vertex(1, minBound, minBound, maxBound, u0, v1, color);
			q.vertex(2, maxBound, minBound, maxBound, u1, v1, color);
			q.vertex(3, maxBound, maxBound, maxBound, u1, v0, color);
			break;
		}

		q.append();
	}
}
