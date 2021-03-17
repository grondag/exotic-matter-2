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
package grondag.xm.api.texture;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.xm.api.paint.PaintBlendMode;

/**
 * Describes if/how this texture can be rendered in alpha or cutout modes. Used
 * to select the optimal (or least bad) block render layer for each paint layer.
 */
@Experimental
public enum TextureRenderIntent {

	/**
	 * Texture is fully opaque everywhere. Rendering in cutout will give a solid
	 * texture unless color is set to a transparent value. Is only intended for
	 * rendering as a base texture in either solid or cutout layers.
	 */
	BASE_ONLY(true, false, true),

	/**
	 * Texture will render with holes in cutout layer. Also doesn't have pleasing
	 * color information for transparent areas and will not render well in solid
	 * layer. Is only intended for rendering as an overlay in translucent layer.
	 */
	OVERLAY_ONLY(false, true, false),

	/**
	 * Texture will render with holes in cutout layer but does have pleasing color
	 * information for translucent areas. It can also be rendered as a base texture
	 * in solid layer (but not cutout). Can also be rendered as overlay in
	 * translucent layer.
	 */
	BASE_OR_OVERLAY_NO_CUTOUT(true, true, false),

	/**
	 * Texture will render as solid surface in cutout layer (all areas are at least
	 * 50% opaque) and has pleasing color information for translucent areas. It can
	 * be rendered as a base texture in either solid or cutout layers. Can also be
	 * rendered as overlay in translucent layer. These are the most flexible
	 * textures.
	 */
	BASE_OR_OVERLAY_CUTOUT_OKAY(true, true, true);

	public final boolean canRenderAsOverlay;
	public final boolean canRenderAsBase;
	public final boolean canRenderAsBaseInCutoutLayer;

	TextureRenderIntent(boolean base, boolean overlay, boolean flexible) {
		canRenderAsBase = base;
		canRenderAsOverlay = overlay;
		canRenderAsBaseInCutoutLayer = flexible;
	}

	public boolean isCompatibleWith(PaintBlendMode blendMode) {
		switch (blendMode) {
		case CUTOUT:
			return false;

		case CUTOUT_MIPPED:
			return this == BASE_ONLY || this == BASE_OR_OVERLAY_CUTOUT_OKAY;

		case SOLID:
			return this != OVERLAY_ONLY;

		case TRANSLUCENT:
			return true;

		default:
			return false;

		}
	}
}
