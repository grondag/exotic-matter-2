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

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

import grondag.xm.texture.TextureSetImpl;

@Experimental
public interface TextureSet {
	static TextureSetBuilder builder() {
		return TextureSetImpl.builder();
	}

	static TextureSetBuilder builder(TextureSet template) {
		return TextureSetImpl.builder(template);
	}

	static TextureSet none() {
		return TextureSetRegistry.instance().get(TextureSetRegistry.NONE_ID);
	}

	/** Registration ID. */
	ResourceLocation id();

	/**
	 * Transient id for temporary serialization. Client values may not match server
	 * values.
	 */
	int index();

	/**
	 * Passes strings to consumer for all textures to be included in texture stitch.
	 */
	void prestitch(Consumer<ResourceLocation> stitcher);

	/**
	 * For use by TESR and GUI to conveniently and quickly access default sprite.
	 */
	TextureAtlasSprite sampleSprite();

	/**
	 * Returns the actual texture name for purpose of finding a texture sprite. For
	 * palettes with a single texture per version.
	 */
	String textureName(int version);

	/**
	 * Returns the actual texture name for purpose of finding a texture sprite. For
	 * palettes with multiple textures per version.
	 */
	String textureName(int version, int index);

	/**
	 * Masks the version number provided by consumers - alternators that drive
	 * number generation may support larger number of values. Implies number of
	 * texture versions must be a power of 2
	 */
	int versionMask();

	// UGLY: rename after fixing breaks
	TextureLayoutMap map();

	TextureTransform transform();

	TextureScale scale();

	/**
	 * Determines layer that should be used for rendering this texture.
	 */
	TextureRenderIntent renderIntent();

	/** Number of alternate versions available - must be a power of 2. */
	int versionCount();

	/**
	 * Base texture file name - used to construct other text name methods. Exposed
	 * to enable programmatic construction of semantically different palates that
	 * use the same underlying texture file(s). Should include namespace.
	 */
	String baseTextureName();

	/** For border-layout textures, controls if "no border" texture is rendered. */
	boolean renderNoBorderAsTile();

	String displayNameToken();

	/**
	 * Player-friendly, localized name for this texture palette.
	 */
	default String displayName() {
		return I18n.get(displayNameToken());
	}

	/**
	 * Use {@link #sampleSprite()} when possible, not all texture formats work well
	 * without specific UV mapping.
	 */
	String sampleTextureName();

	/**
	 * Used by modelstate to know which world state must be retrieved to drive this
	 * texture (rotation and block version).
	 */
	int stateFlags();

	int textureGroupFlags();

	/**
	 * Call to force textures to be loaded into memory. Will be called automatically
	 * for textures referenced in statically-defined models but must be called
	 * explicitly for any texture sets that may be referenced dynamically at run
	 * time.
	 */
	void use();

	/**
	 * True if {@link #use()} has been called.
	 */
	boolean used();
}
