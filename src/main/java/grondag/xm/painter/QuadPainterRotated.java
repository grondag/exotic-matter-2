/*
 * Copyright © Original Authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.painter;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.Direction;

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
			if (faceState.isJoined(corner.rightSide)) {
				return faceState.needsCorner(corner) ? RotatableQuadrant.CORNER : RotatableQuadrant.FULL;
			} else {
				return RotatableQuadrant.SIDE_RIGHT;
			}
		} else if (faceState.isJoined(corner.rightSide)) {
			return RotatableQuadrant.SIDE_LEFT;
		} else {
			return RotatableQuadrant.ROUND;
		}
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
