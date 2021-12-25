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

import it.unimi.dsi.fastutil.HashCommon;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.Direction;

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
				rotation = rotation.clockwise(saltHash & 3);
				textureVersion = (textureVersion + (saltHash >> 2)) & tex.versionMask();
			}

			editor.rotation(textureIndex, rotation);
			editor.sprite(textureIndex, tex.textureName(textureVersion));
			editor.contractUV(textureIndex, true);

			commonPostPaint(editor, modelState, surface, paint, textureIndex);
		} while (editor.next());
	}
}
