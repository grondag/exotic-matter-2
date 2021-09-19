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

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.ApiStatus.Internal;
import grondag.fermion.varia.Useful;
import grondag.xm.api.mesh.MutableMesh;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.texture.TextureOrientation;
import grondag.xm.api.texture.TextureSet;
import grondag.xm.api.texture.TextureTransform;

@Internal
public abstract class CubicPainterTiles extends AbstractQuadPainter {
	@SuppressWarnings("rawtypes")
	public static void paintQuads(MutableMesh stream, BaseModelState modelState, XmSurface surface, XmPaint paint, int textureIndex) {
		final MutablePolygon editor = stream.editor();

		do {
			editor.lockUV(textureIndex, true);
			editor.assignLockedUVCoordinates(textureIndex);


			final Direction nominalFace = editor.nominalFace();
			final TextureSet tex = paint.texture(textureIndex);

			// UGLY: need a more generic way to handle vs hardcoding in this painter
			if (paint.texture(textureIndex).transform() == TextureTransform.DIAGONAL) {
				for (int i = 0; i < 4; ++i) {
					final float u = editor.u(i, textureIndex);
					final float v = editor.v(i, textureIndex);
					editor.uv(i, textureIndex, 0.5f * v + 0.5f * u, 0.5f - 0.5f * u + 0.5f * v);
				}
			}

			TextureOrientation rotation = textureRotationForFace(nominalFace, tex, modelState);

			int textureVersion = textureVersionForFace(nominalFace, tex, modelState);

			final int salt = editor.textureSalt();
			if (salt != 0 && tex.transform().hasRandom) {
				final int saltHash = HashCommon.mix(salt);
				rotation = Useful.offsetEnumValue(rotation, saltHash & 3);
				textureVersion = (textureVersion + (saltHash >> 2)) & tex.versionMask();
			}

			editor.rotation(textureIndex, rotation);
			editor.sprite(textureIndex, tex.textureName(textureVersion));
			editor.contractUV(textureIndex, true);

			commonPostPaint(editor, modelState, surface, paint, textureIndex);

		} while (editor.next());
	}
}
