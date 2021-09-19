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

import java.util.function.Consumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus.Experimental;
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

	/** Registration ID */
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
	 * For use by TESR and GUI to conveniently and quickly access default sprite
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

	/** number of alternate versions available - must be a power of 2 */
	int versionCount();

	/**
	 * Base texture file name - used to construct other text name methods. Exposed
	 * to enable programmatic construction of semantically different palates that
	 * use the same underlying texture file(s). Should include namespace.
	 */
	String baseTextureName();

	/** for border-layout textures, controls if "no border" texture is rendered */
	boolean renderNoBorderAsTile();

	String displayNameToken();

	/**
	 * Player-friendly, localized name for this texture palette
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
	 * texture (rotation and block version)
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
