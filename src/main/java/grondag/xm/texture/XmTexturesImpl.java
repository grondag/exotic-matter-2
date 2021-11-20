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

package grondag.xm.texture;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;

import io.vram.frex.api.texture.SpriteInjector;

import grondag.xm.Xm;
import grondag.xm.api.texture.XmTextures;
import grondag.xm.paint.XmPaintRegistryImpl;

@Internal
public class XmTexturesImpl {
	/**
	 * Main purpose of being here is to force instantiation of other static members.
	 */
	public static void init() {
		Xm.LOG.debug("Registering Exotic Matter textures");

		// Force registration
		XmTextures.EMPTY.use();

		SpriteInjector.injectOnAtlasStitch(TextureAtlas.LOCATION_BLOCKS, XmTexturesImpl::registerTextures);
	}

	private static void registerTextures(SpriteInjector injector) {
		// need to resolve/use texture names at this point
		XmPaintRegistryImpl.INSTANCE.onResourceManagerReload(Minecraft.getInstance().getResourceManager());
		final TextureSetRegistryImpl texReg = TextureSetRegistryImpl.INSTANCE;

		texReg.forEach(set -> {
			if (set.used()) {
				set.prestitch(id -> injector.inject(id));
			}
		});
	}
}
