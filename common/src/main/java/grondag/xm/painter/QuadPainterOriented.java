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

import grondag.xm.api.connect.state.CornerJoinFaceState;
import grondag.xm.api.connect.state.CornerJoinFaceStates;
import grondag.xm.api.mesh.MutableMesh;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.texture.TextureSet;
import grondag.xm.api.texture.XmTextures;
import grondag.xm.orientation.api.FaceCorner;

/**
 * Applies quadrant-style border textures. Quads must have a nominal face. Will
 * split quads that span quadrants.
 */
@Internal
public abstract class QuadPainterOriented extends AbstractQuadPainter {
	static final int ALL_BORDERS = 0;
	static final int HORIZONTAL_BORDERS = 1;
	static final int VERTICAL_BORDERS = 2;
	static final int CORNERS = 3;
	static final int NO_BORDERS = 4;

	@SuppressWarnings("rawtypes")
	public static void paintBorders(MutableMesh stream, BaseModelState modelState, XmSurface surface, XmPaint paint, final int spriteIndex) {
		paintQuads(stream, modelState, surface, paint, spriteIndex, true);
	}

	@SuppressWarnings("rawtypes")
	public static void paintTiles(MutableMesh stream, BaseModelState modelState, XmSurface surface, XmPaint paint, final int spriteIndex) {
		paintQuads(stream, modelState, surface, paint, spriteIndex, false);
	}

	@SuppressWarnings("rawtypes")
	private static void paintQuads(MutableMesh stream, BaseModelState modelState, XmSurface surface, XmPaint paint, final int spriteIndex, boolean bordered) {
		final MutablePolygon editor = stream.editor();

		do {
			editor.lockUV(spriteIndex, true);
			editor.assignLockedUVCoordinates(spriteIndex);

			final Direction nominalFace = editor.nominalFace();
			final CornerJoinFaceState faceState = modelState.cornerJoin().faceState(nominalFace);
			final int textureIndex;

			if (faceState == CornerJoinFaceStates.ALL_NO_CORNERS) {
				if (bordered) {
					continue;
				} else {
					textureIndex = NO_BORDERS;
				}
			} else if (faceState == CornerJoinFaceStates.NONE) {
				textureIndex = ALL_BORDERS;
			} else if (faceState == CornerJoinFaceStates.LEFT_RIGHT) {
				textureIndex = HORIZONTAL_BORDERS;
			} else if (faceState == CornerJoinFaceStates.TOP_BOTTOM) {
				textureIndex = VERTICAL_BORDERS;
			} else if (faceState == CornerJoinFaceStates.ALL_TL_TR_BL_BR) {
				textureIndex = CORNERS;
			} else {
				// needs split
				final FaceCorner quadrant = QuadrantSplitter.uvQuadrant(editor, spriteIndex);

				if (quadrant == null) {
					QuadrantSplitter.split(stream, spriteIndex);
					// skip the (now-deleted) original and paint split outputs later in loop
					assert editor.isDeleted();
					continue;
				}

				textureIndex = textureIndex(quadrant, faceState);
			}

			final TextureSet tex;

			if (bordered && textureIndex == NO_BORDERS) {
				// PERF: find some way to omit these entirely - maybe filter out downstream
				tex = XmTextures.EMPTY;
			} else {
				tex = paint.texture(spriteIndex);
			}

			final int textureVersion = tex.versionMask() & (textureHashForFace(nominalFace, tex, modelState));
			editor.sprite(spriteIndex, tex.textureName(textureVersion, textureIndex));
			editor.contractUV(spriteIndex, true);
			commonPostPaint(editor, modelState, surface, paint, spriteIndex);
		} while (editor.next());
	}

	static int textureIndex(FaceCorner quadrant, CornerJoinFaceState faceState) {
		final boolean left = faceState.isJoined(quadrant.leftSide);
		final boolean right = faceState.isJoined(quadrant.rightSide);

		if (left) {
			if (right) {
				return faceState.needsCorner(quadrant) ? CORNERS : NO_BORDERS;
			} else {
				return quadrant.leftSide.isHorizontal() ? HORIZONTAL_BORDERS : VERTICAL_BORDERS;
			}
		} else if (right) {
			return quadrant.rightSide.isHorizontal() ? HORIZONTAL_BORDERS : VERTICAL_BORDERS;
		} else {
			return ALL_BORDERS;
		}
	}
}
