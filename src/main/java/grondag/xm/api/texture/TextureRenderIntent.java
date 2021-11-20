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
