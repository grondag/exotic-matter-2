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

package grondag.xm.texture;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import grondag.xm.api.modelstate.ModelStateFlags;
import grondag.xm.api.texture.TextureSet;
import grondag.xm.api.texture.TextureSetBuilder;

@Internal
public class TextureSetImpl extends AbstractTextureSet implements TextureSet {
	public static TextureSetBuilder builder() {
		return new TextureSetBuilderImpl();
	}

	public static TextureSetBuilder builder(TextureSet template) {
		final TextureSetBuilderImpl result = new TextureSetBuilderImpl();
		result.copyFrom((AbstractTextureSet) template);
		return result;
	}

	public final ResourceLocation id;
	public final int versionMask;
	public final int stateFlags;
	public final String baseTextureName;
	private boolean used = false;

	TextureSetImpl(ResourceLocation id, AbstractTextureSet template) {
		this.id = id;
		baseTextureName = template.rawBaseTextureName;
		copyFrom(template);
		versionMask = Math.max(0, template.versionCount - 1);
		layoutMap = template.layoutMap;

		int flags = template.scale.modelStateFlag | template.layoutMap.layout.modelStateFlag;

		// textures with randomization options also require position information
		if (template.transform.hasRandom) {
			flags |= ModelStateFlags.POSITION;
		}

		if (template.versionCount > 1) {
			flags |= ModelStateFlags.POSITION;
		}

		stateFlags = flags;

		TextureSetRegistryImpl.INSTANCE.add(this);
	}

	@Override
	public ResourceLocation id() {
		return id;
	}

	@Override
	public int index() {
		return TextureSetRegistryImpl.INSTANCE.indexOf(this);
	}

	@Override
	public int stateFlags() {
		return stateFlags;
	}

	@Override
	public void prestitch(Consumer<ResourceLocation> stitcher) {
		layoutMap.prestitch(this, stitcher);
	}

	@Override
	public String sampleTextureName() {
		return layoutMap.sampleTextureName(this);
	}

	private TextureAtlasSprite sampleSprite;

	@Override
	public TextureAtlasSprite sampleSprite() {
		TextureAtlasSprite result = sampleSprite;

		if (result == null) {
			result = TextureSetHelper.blockAtas().getSprite(new ResourceLocation(sampleTextureName()));
			sampleSprite = result;
		}

		return result;
	}

	@Override
	public String textureName(int version) {
		return layoutMap.buildTextureName(this, version & versionMask, 0);
	}

	@Override
	public String textureName(int version, int index) {
		return layoutMap.buildTextureName(this, version & versionMask, index);
	}

	@Override
	public int versionMask() {
		return versionMask;
	}

	@Override
	public String baseTextureName() {
		return baseTextureName;
	}

	@Override
	public void use() {
		used = true;
	}

	@Override
	public boolean used() {
		return used;
	}
}
