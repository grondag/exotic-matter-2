/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
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
 */

package grondag.xm.painter;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.Direction;

import grondag.xm.api.connect.state.SimpleJoinFaceState;
import grondag.xm.api.mesh.MutableMesh;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.paint.RotatableCableQuadrant;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.texture.TextureSet;
import grondag.xm.orientation.api.FaceCorner;

/**
 * Applies quadrant-style connected (non-corner) textures. Quads must have a nominal face. Will
 * split quads that span quadrants.
 */
@Internal
public abstract class QuadPainterCable extends AbstractQuadPainter {
	private static final RotatableCableQuadrant[][] TEXTURE_MAP = new RotatableCableQuadrant[FaceCorner.values().length][SimpleJoinFaceState.COUNT];

	private static RotatableCableQuadrant textureMap(FaceCorner corner, SimpleJoinFaceState faceState) {
		if (faceState == SimpleJoinFaceState.NO_FACE) {
			return RotatableCableQuadrant.INSIDE_CORNER;
		} else if (faceState.isJoined(corner.leftSide)) {
			if (faceState.isJoined(corner.rightSide)) {
				return RotatableCableQuadrant.INSIDE_CORNER;
			} else {
				return RotatableCableQuadrant.SIDE_RIGHT;
			}
		} else if (faceState.isJoined(corner.rightSide)) {
			return RotatableCableQuadrant.SIDE_LEFT;
		} else {
			if (faceState.isJoined(corner.rightSide.opposite())) {
				if (faceState.isJoined(corner.leftSide.opposite())) {
					return RotatableCableQuadrant.OUTSIDE_CORNER;
				} else {
					return RotatableCableQuadrant.END_CAP_LEFT;
				}
			} else {
				return RotatableCableQuadrant.END_CAP_RIGHT;
			}
		}
	}

	static {
		final SimpleJoinFaceState[] faceStates = SimpleJoinFaceState.values();

		for (final FaceCorner corner : FaceCorner.values()) {
			for (final SimpleJoinFaceState faceState : faceStates) {
				TEXTURE_MAP[corner.ordinal()][faceState.ordinal()] = textureMap(corner, faceState);
			}
		}
	}

	// UGLY: copypasta from QuadPainterRotated - above and one line below are different

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

			// NB: this is the line that is different from the corner-join version
			final SimpleJoinFaceState faceState = modelState.simpleJoin().faceState(nominalFace);

			TEXTURE_MAP[quadrant.ordinal()][faceState.ordinal()].applyForQuadrant(editor, textureIndex, quadrant);

			commonPostPaint(editor, modelState, surface, paint, textureIndex);
		} while (editor.next());
	}
}
