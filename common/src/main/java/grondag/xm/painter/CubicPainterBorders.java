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

import static grondag.xm.api.texture.TextureNameFunction.BORDER_CORNERS_ALL;
import static grondag.xm.api.texture.TextureNameFunction.BORDER_CORNERS_BL_TR;
import static grondag.xm.api.texture.TextureNameFunction.BORDER_CORNERS_BL_TR_BR;
import static grondag.xm.api.texture.TextureNameFunction.BORDER_CORNERS_TL_TR;
import static grondag.xm.api.texture.TextureNameFunction.BORDER_CORNER_TR;
import static grondag.xm.api.texture.TextureNameFunction.BORDER_MIXED_TOP_BL_BR;
import static grondag.xm.api.texture.TextureNameFunction.BORDER_MIXED_TOP_BR;
import static grondag.xm.api.texture.TextureNameFunction.BORDER_MIXED_TOP_RIGHT_BL;
import static grondag.xm.api.texture.TextureNameFunction.BORDER_NONE;
import static grondag.xm.api.texture.TextureNameFunction.BORDER_SIDES_ALL;
import static grondag.xm.api.texture.TextureNameFunction.BORDER_SIDES_TOP_BOTTOM;
import static grondag.xm.api.texture.TextureNameFunction.BORDER_SIDES_TOP_LEFT_RIGHT;
import static grondag.xm.api.texture.TextureNameFunction.BORDER_SIDES_TOP_RIGHT;
import static grondag.xm.api.texture.TextureNameFunction.BORDER_SIDE_TOP;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.Direction;

import grondag.xm.api.connect.state.CornerJoinFaceStates;
import grondag.xm.api.connect.state.CornerJoinState;
import grondag.xm.api.mesh.MutableMesh;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.texture.TextureOrientation;
import grondag.xm.api.texture.TextureSet;

@Internal
public abstract class CubicPainterBorders extends AbstractQuadPainter {
	protected static final FaceQuadInputs[][] FACE_INPUTS = new FaceQuadInputs[6][CornerJoinFaceStates.COUNT];
	/**
	 * Used only when a border is rendered in the solid layer. Declared at module
	 * level so that we can check for it.
	 */
	private static final FaceQuadInputs NO_BORDER = new FaceQuadInputs(BORDER_NONE, TextureOrientation.IDENTITY, false, false);

	static {
		for (final Direction face : Direction.values()) {
			// First one will only be used if we are rendering in solid layer.
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_NO_CORNERS.ordinal()] = NO_BORDER;
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.NO_FACE.ordinal()] = null; // NULL FACE
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.NONE.ordinal()] = new FaceQuadInputs(BORDER_SIDES_ALL, TextureOrientation.IDENTITY, false, false);

			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.BOTTOM.ordinal()] = new FaceQuadInputs(BORDER_SIDES_TOP_LEFT_RIGHT, TextureOrientation.IDENTITY, false,
					false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.LEFT.ordinal()] = new FaceQuadInputs(BORDER_SIDES_TOP_LEFT_RIGHT, TextureOrientation.ROTATE_90, false,
					false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP.ordinal()] = new FaceQuadInputs(BORDER_SIDES_TOP_LEFT_RIGHT, TextureOrientation.ROTATE_180, false,
					false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.RIGHT.ordinal()] = new FaceQuadInputs(BORDER_SIDES_TOP_LEFT_RIGHT, TextureOrientation.ROTATE_270, false,
					false);

			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.BOTTOM_LEFT_NO_CORNER.ordinal()] = new FaceQuadInputs(BORDER_SIDES_TOP_RIGHT, TextureOrientation.IDENTITY,
					false, false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_LEFT_NO_CORNER.ordinal()] = new FaceQuadInputs(BORDER_SIDES_TOP_RIGHT, TextureOrientation.ROTATE_90,
					false, false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_RIGHT_NO_CORNER.ordinal()] = new FaceQuadInputs(BORDER_SIDES_TOP_RIGHT, TextureOrientation.ROTATE_180,
					false, false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.BOTTOM_RIGHT_NO_CORNER.ordinal()] = new FaceQuadInputs(BORDER_SIDES_TOP_RIGHT, TextureOrientation.ROTATE_270,
					false, false);

			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.BOTTOM_LEFT_RIGHT_NO_CORNERS.ordinal()] = new FaceQuadInputs(BORDER_SIDE_TOP, TextureOrientation.IDENTITY,
					false, false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_BOTTOM_LEFT_NO_CORNERS.ordinal()] = new FaceQuadInputs(BORDER_SIDE_TOP, TextureOrientation.ROTATE_90,
					false, false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_LEFT_RIGHT_NO_CORNERS.ordinal()] = new FaceQuadInputs(BORDER_SIDE_TOP, TextureOrientation.ROTATE_180,
					false, false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_BOTTOM_RIGHT_NO_CORNERS.ordinal()] = new FaceQuadInputs(BORDER_SIDE_TOP, TextureOrientation.ROTATE_270,
					false, false);

			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.LEFT_RIGHT.ordinal()] = new FaceQuadInputs(BORDER_SIDES_TOP_BOTTOM, TextureOrientation.IDENTITY, false,
					false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_BOTTOM.ordinal()] = new FaceQuadInputs(BORDER_SIDES_TOP_BOTTOM, TextureOrientation.ROTATE_90, false,
					false);

			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.BOTTOM_LEFT_RIGHT_BR.ordinal()] = new FaceQuadInputs(BORDER_MIXED_TOP_BR, TextureOrientation.IDENTITY,
					false, false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.BOTTOM_LEFT_RIGHT_BL.ordinal()] = new FaceQuadInputs(BORDER_MIXED_TOP_BR, TextureOrientation.IDENTITY,
					true, false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_BOTTOM_LEFT_BL.ordinal()] = new FaceQuadInputs(BORDER_MIXED_TOP_BR, TextureOrientation.ROTATE_90, false,
					false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_BOTTOM_LEFT_TL.ordinal()] = new FaceQuadInputs(BORDER_MIXED_TOP_BR, TextureOrientation.ROTATE_90, true,
					false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_LEFT_RIGHT_TL.ordinal()] = new FaceQuadInputs(BORDER_MIXED_TOP_BR, TextureOrientation.ROTATE_180, false,
					false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_LEFT_RIGHT_TR.ordinal()] = new FaceQuadInputs(BORDER_MIXED_TOP_BR, TextureOrientation.ROTATE_180, true,
					false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_BOTTOM_RIGHT_TR.ordinal()] = new FaceQuadInputs(BORDER_MIXED_TOP_BR, TextureOrientation.ROTATE_270,
					false, false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_BOTTOM_RIGHT_BR.ordinal()] = new FaceQuadInputs(BORDER_MIXED_TOP_BR, TextureOrientation.ROTATE_270, true,
					false);

			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.BOTTOM_LEFT_RIGHT_BL_BR.ordinal()] = new FaceQuadInputs(BORDER_MIXED_TOP_BL_BR,
					TextureOrientation.IDENTITY, false, false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_BOTTOM_LEFT_TL_BL.ordinal()] = new FaceQuadInputs(BORDER_MIXED_TOP_BL_BR, TextureOrientation.ROTATE_90,
					false, false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_LEFT_RIGHT_TL_TR.ordinal()] = new FaceQuadInputs(BORDER_MIXED_TOP_BL_BR, TextureOrientation.ROTATE_180,
					false, false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_BOTTOM_RIGHT_TR_BR.ordinal()] = new FaceQuadInputs(BORDER_MIXED_TOP_BL_BR, TextureOrientation.ROTATE_270,
					false, false);

			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.BOTTOM_LEFT_BL.ordinal()] = new FaceQuadInputs(BORDER_MIXED_TOP_RIGHT_BL, TextureOrientation.IDENTITY,
					false, false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_LEFT_TL.ordinal()] = new FaceQuadInputs(BORDER_MIXED_TOP_RIGHT_BL, TextureOrientation.ROTATE_90, false,
					false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_RIGHT_TR.ordinal()] = new FaceQuadInputs(BORDER_MIXED_TOP_RIGHT_BL, TextureOrientation.ROTATE_180, false,
					false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.BOTTOM_RIGHT_BR.ordinal()] = new FaceQuadInputs(BORDER_MIXED_TOP_RIGHT_BL, TextureOrientation.ROTATE_270,
					false, false);

			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TR.ordinal()] = new FaceQuadInputs(BORDER_CORNER_TR, TextureOrientation.IDENTITY, false, false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_BR.ordinal()] = new FaceQuadInputs(BORDER_CORNER_TR, TextureOrientation.ROTATE_90, false, false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_BL.ordinal()] = new FaceQuadInputs(BORDER_CORNER_TR, TextureOrientation.ROTATE_180, false, false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TL.ordinal()] = new FaceQuadInputs(BORDER_CORNER_TR, TextureOrientation.ROTATE_270, false, false);

			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TL_TR.ordinal()] = new FaceQuadInputs(BORDER_CORNERS_TL_TR, TextureOrientation.IDENTITY, false,
					false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TR_BR.ordinal()] = new FaceQuadInputs(BORDER_CORNERS_TL_TR, TextureOrientation.ROTATE_90, false, false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_BL_BR.ordinal()] = new FaceQuadInputs(BORDER_CORNERS_TL_TR, TextureOrientation.ROTATE_180, false, false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TL_BL.ordinal()] = new FaceQuadInputs(BORDER_CORNERS_TL_TR, TextureOrientation.ROTATE_270, false, false);

			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TR_BL.ordinal()] = new FaceQuadInputs(BORDER_CORNERS_BL_TR, TextureOrientation.IDENTITY, false,
					false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TL_BR.ordinal()] = new FaceQuadInputs(BORDER_CORNERS_BL_TR, TextureOrientation.ROTATE_90, false, false);

			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TR_BL_BR.ordinal()] = new FaceQuadInputs(BORDER_CORNERS_BL_TR_BR, TextureOrientation.IDENTITY, false,
					false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TL_BL_BR.ordinal()] = new FaceQuadInputs(BORDER_CORNERS_BL_TR_BR, TextureOrientation.ROTATE_90, false,
					false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TL_TR_BL.ordinal()] = new FaceQuadInputs(BORDER_CORNERS_BL_TR_BR, TextureOrientation.ROTATE_180, false,
					false);
			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TL_TR_BR.ordinal()] = new FaceQuadInputs(BORDER_CORNERS_BL_TR_BR, TextureOrientation.ROTATE_270, false,
					false);

			FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TL_TR_BL_BR.ordinal()] = new FaceQuadInputs(BORDER_CORNERS_ALL, TextureOrientation.IDENTITY, false,
					false);
		}
	}

	@SuppressWarnings("rawtypes")
	public static void paintQuads(MutableMesh stream, BaseModelState modelState, XmSurface surface, XmPaint paint, int textureIndex) {
		final MutablePolygon editor = stream.editor();

		do {
			final CornerJoinState bjs = modelState.cornerJoin();
			final Direction face = editor.nominalFace();
			final FaceQuadInputs inputs = FACE_INPUTS[face.ordinal()][bjs.faceState(face).ordinal()];

			// if can't identify a face, skip texturing
			if (inputs == null) {
				continue;
			}

			final TextureSet tex = paint.texture(textureIndex);

			// don't render the "no border" texture unless this is a tile of some kind
			if (inputs == NO_BORDER && !tex.renderNoBorderAsTile()) {
				continue;
			}

			editor.lockUV(textureIndex, true);
			editor.assignLockedUVCoordinates(textureIndex);

			editor.rotation(textureIndex, inputs.rotation);
			//            cubeInputs.rotateBottom = false;
			editor.minU(textureIndex, inputs.flipU ? 1 : 0);
			editor.minV(textureIndex, inputs.flipV ? 1 : 0);
			editor.maxU(textureIndex, inputs.flipU ? 0 : 1);
			editor.maxV(textureIndex, inputs.flipV ? 0 : 1);
			editor.sprite(textureIndex, tex.textureName(textureVersionForFace(face, tex, modelState), inputs.textureOffset));

			commonPostPaint(editor, modelState, surface, paint, textureIndex);
		} while (editor.next());
	}
}
