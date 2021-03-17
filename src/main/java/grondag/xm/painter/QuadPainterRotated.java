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
package grondag.xm.painter;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.util.math.Direction;

import grondag.fermion.orientation.api.FaceCorner;
import grondag.xm.api.connect.state.CornerJoinFaceState;
import grondag.xm.api.connect.state.CornerJoinFaceStates;
import grondag.xm.api.mesh.MutableMesh;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.paint.RotatableQuadrant;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.texture.TextureSet;

/**
 * Applies quadrant-style border textures. Quads must have a nominal face. Will
 * split quads that span quadrants.
 */
@Internal
public abstract class QuadPainterRotated extends AbstractQuadPainter {
	private static final RotatableQuadrant[][] TEXTURE_MAP = new RotatableQuadrant[FaceCorner.values().length][CornerJoinFaceStates.COUNT];

	private static RotatableQuadrant textureMap(FaceCorner corner, CornerJoinFaceState faceState) {
		if (faceState.isJoined(corner.leftSide)) {
			if (faceState.isJoined(corner.rightSide))
				return faceState.needsCorner(corner) ? RotatableQuadrant.CORNER : RotatableQuadrant.FULL;
			else
				return RotatableQuadrant.SIDE_RIGHT;
		} else if (faceState.isJoined(corner.rightSide))
			return RotatableQuadrant.SIDE_LEFT;
		else
			return RotatableQuadrant.ROUND;
	}

	static {
		for (final FaceCorner corner : FaceCorner.values()) {
			CornerJoinFaceStates.forEach(faceState -> {
				TEXTURE_MAP[corner.ordinal()][faceState.ordinal()] = textureMap(corner, faceState);
			});
		}
	}

	@SuppressWarnings("rawtypes")
	public static void paintQuads(MutableMesh stream, BaseModelState modelState, XmSurface surface, XmPaint paint, int textureIndex) {
		final MutablePolygon editor = stream.editor();

		do {
			editor.lockUV(textureIndex, true);
			editor.assignLockedUVCoordinates(textureIndex);

			final FaceCorner quadrant = QuadrantSplitter.uvQuadrant(editor, textureIndex);
			if (quadrant == null) {
				QuadrantSplitter.split(stream, textureIndex);
				// skip the (now-deleted) original and paint split outputs later in loop
				assert editor.isDeleted();
				continue;
			}

			final Direction nominalFace = editor.nominalFace();
			final TextureSet tex = paint.texture(textureIndex);
			final int textureVersion = tex.versionMask() & (textureHashForFace(nominalFace, tex, modelState) >> (quadrant.ordinal() * 4));

			editor.sprite(textureIndex, tex.textureName(textureVersion));
			editor.contractUV(textureIndex, true);

			final CornerJoinFaceState faceState = modelState.cornerJoin().faceState(nominalFace);

			TEXTURE_MAP[quadrant.ordinal()][faceState.ordinal()].applyForQuadrant(editor, textureIndex, quadrant);

			commonPostPaint(editor, modelState, surface, paint, textureIndex);

		} while (editor.next());
	}
}
