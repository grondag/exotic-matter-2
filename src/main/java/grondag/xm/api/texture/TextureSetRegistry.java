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

import net.minecraft.resources.ResourceLocation;

import grondag.xm.Xm;
import grondag.xm.texture.TextureSetRegistryImpl;

@Experimental
public interface TextureSetRegistry {
	static TextureSetRegistry instance() {
		return TextureSetRegistryImpl.INSTANCE;
	}

	/**
	 * Will always be associated with index 0.
	 */
	ResourceLocation NONE_ID = new ResourceLocation(Xm.MODID, "none");

	TextureSet get(ResourceLocation id);

	TextureSet get(int index);

	void forEach(Consumer<TextureSet> consumer);
}
