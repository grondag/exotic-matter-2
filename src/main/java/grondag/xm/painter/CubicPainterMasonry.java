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
import grondag.xm.api.connect.state.SimpleJoinFaceState;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.mesh.MutableMesh;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.texture.TextureOrientation;
import grondag.xm.api.texture.TextureSet;
import net.minecraft.core.Direction;

@Internal
public abstract class CubicPainterMasonry extends AbstractQuadPainter {
	protected final static FaceQuadInputs[][] FACE_INPUTS = new FaceQuadInputs[6][SimpleJoinFaceState.values().length];

	private enum Textures {
		BOTTOM_LEFT_RIGHT, BOTTOM_LEFT, LEFT_RIGHT, BOTTOM, ALL;
	}

	static {
		// mapping is unusual in that a join indicates a border IS present on texture
		for (final Direction face : Direction.values()) {
			FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.NO_FACE.ordinal()] = null;

			FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.BOTTOM_LEFT_RIGHT.ordinal()] = new FaceQuadInputs(Textures.BOTTOM_LEFT_RIGHT.ordinal(),
					TextureOrientation.IDENTITY, false, false);
			FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_BOTTOM_LEFT.ordinal()] = new FaceQuadInputs(Textures.BOTTOM_LEFT_RIGHT.ordinal(),
					TextureOrientation.ROTATE_90, false, false);
			FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_LEFT_RIGHT.ordinal()] = new FaceQuadInputs(Textures.BOTTOM_LEFT_RIGHT.ordinal(),
					TextureOrientation.ROTATE_180, false, false);
			FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_BOTTOM_RIGHT.ordinal()] = new FaceQuadInputs(Textures.BOTTOM_LEFT_RIGHT.ordinal(),
					TextureOrientation.ROTATE_270, false, false);

			FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.BOTTOM_LEFT.ordinal()] = new FaceQuadInputs(Textures.BOTTOM_LEFT.ordinal(), TextureOrientation.IDENTITY,
					false, false);
			FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_LEFT.ordinal()] = new FaceQuadInputs(Textures.BOTTOM_LEFT.ordinal(), TextureOrientation.ROTATE_90, false,
					false);
			FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_RIGHT.ordinal()] = new FaceQuadInputs(Textures.BOTTOM_LEFT.ordinal(), TextureOrientation.ROTATE_180,
					false, false);
			FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.BOTTOM_RIGHT.ordinal()] = new FaceQuadInputs(Textures.BOTTOM_LEFT.ordinal(), TextureOrientation.ROTATE_270,
					false, false);

			FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.LEFT_RIGHT.ordinal()] = new FaceQuadInputs(Textures.LEFT_RIGHT.ordinal(), TextureOrientation.IDENTITY,
					false, false);
			FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_BOTTOM.ordinal()] = new FaceQuadInputs(Textures.LEFT_RIGHT.ordinal(), TextureOrientation.ROTATE_90, false,
					false);

			FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.BOTTOM.ordinal()] = new FaceQuadInputs(Textures.BOTTOM.ordinal(), TextureOrientation.IDENTITY, false,
					false);
			FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.LEFT.ordinal()] = new FaceQuadInputs(Textures.BOTTOM.ordinal(), TextureOrientation.ROTATE_90, false, false);
			FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP.ordinal()] = new FaceQuadInputs(Textures.BOTTOM.ordinal(), TextureOrientation.ROTATE_180, false, false);
			FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.RIGHT.ordinal()] = new FaceQuadInputs(Textures.BOTTOM.ordinal(), TextureOrientation.ROTATE_270, false, false);

			FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.ALL.ordinal()] = new FaceQuadInputs(Textures.ALL.ordinal(), TextureOrientation.IDENTITY, false, false);
		}
	}

	@SuppressWarnings("rawtypes")
	public static void paintQuads(MutableMesh stream, BaseModelState modelState, XmSurface surface, XmPaint paint, int textureIndex) {
		final MutablePolygon editor = stream.editor();
		do {
			final SimpleJoinState bjs = modelState.alternateJoin();
			final Direction face = editor.nominalFace();
			final SimpleJoinFaceState fjs = SimpleJoinFaceState.find(face, bjs);
			final FaceQuadInputs inputs = FACE_INPUTS[face.ordinal()][fjs.ordinal()];

			// if can't identify a face, skip texturing
			if (inputs == null)
				return;

			editor.lockUV(textureIndex, true);
			editor.assignLockedUVCoordinates(textureIndex);

			editor.rotation(textureIndex, inputs.rotation);
			editor.minU(textureIndex, inputs.flipU ? 1 : 0);
			editor.minV(textureIndex, inputs.flipV ? 1 : 0);
			editor.maxU(textureIndex, inputs.flipU ? 0 : 1);
			editor.maxV(textureIndex, inputs.flipV ? 0 : 1);

			final TextureSet tex = paint.texture(textureIndex);
			editor.sprite(textureIndex, tex.textureName(textureVersionForFace(face, tex, modelState), inputs.textureOffset));

			commonPostPaint(editor, modelState, surface, paint, textureIndex);

		} while (editor.next());
	}
}
