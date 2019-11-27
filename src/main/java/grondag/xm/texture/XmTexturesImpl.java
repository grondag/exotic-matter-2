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
package grondag.xm.texture;

import static grondag.xm.api.texture.TextureGroup.HIDDEN_TILES;
import static grondag.xm.api.texture.TextureRenderIntent.BASE_ONLY;
import static grondag.xm.api.texture.TextureScale.GIANT;
import static grondag.xm.api.texture.TextureScale.LARGE;
import static grondag.xm.api.texture.TextureScale.MEDIUM;
import static grondag.xm.api.texture.TextureScale.SINGLE;
import static grondag.xm.api.texture.TextureScale.SMALL;
import static grondag.xm.api.texture.TextureScale.TINY;
import static grondag.xm.api.texture.TextureTransform.IDENTITY;
import static grondag.xm.api.texture.TextureTransform.ROTATE_180;
import static grondag.xm.api.texture.TextureTransform.ROTATE_270;
import static grondag.xm.api.texture.TextureTransform.ROTATE_90;
import static grondag.xm.api.texture.TextureTransform.ROTATE_RANDOM;
import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

import grondag.xm.Xm;
import grondag.xm.api.texture.TextureLayoutMap;
import grondag.xm.api.texture.TextureSet;
import grondag.xm.api.texture.XmTextures;
import grondag.xm.paint.XmPaintRegistryImpl;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback.Registry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteAtlasTexture;

@API(status = INTERNAL)
public class XmTexturesImpl {

	/**
	 * Main purpose of being here is to force instantiation of other static members.
	 */
	public static void init() {
		Xm.LOG.debug("Registering Exotic Matter textures");

		@SuppressWarnings("unused")
		final
		TextureSet dummy = XmTextures.TILE_COBBLE;

		ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEX).register(XmTexturesImpl::registerTextures);
	}

	private static void registerTextures(SpriteAtlasTexture atlas, Registry registry) {
		// need to resolve/use texture names at this point
		XmPaintRegistryImpl.INSTANCE.apply(MinecraftClient.getInstance().getResourceManager());
		final TextureSetRegistryImpl texReg = TextureSetRegistryImpl.INSTANCE;

		texReg.forEach(set -> {
			if (set.used()) {
				set.prestitch(id -> registry.register(id));
			}
		});
	}

	// ======================================================================
	// TEST/DEBUG TEXTURES - NOT LOADED UNLESS NEEDED
	// ======================================================================

	// but still load placeholders so we don't lose test texture attributes on
	// blocks if test textures are temporarily disabled

	public static final TextureSet BIGTEX_TEST_SINGLE = TextureSet.builder().displayNameToken("bigtex_test_single")
			.baseTextureName("exotic-matter:block/bigtex_single").versionCount(1)
			.scale(SMALL).layout(TextureLayoutMap.SINGLE).transform(IDENTITY).renderIntent(BASE_ONLY).groups(HIDDEN_TILES).build("exotic-matter:bigtex_test_single");

	public static final TextureSet BIGTEX_TEST1 = TextureSet.builder().displayNameToken("big_tex_test1")
			.baseTextureName("exotic-matter:block/bigtex").versionCount(4).scale(TINY)
			.layout(TextureLayoutMap.VERSIONED).transform(ROTATE_RANDOM).renderIntent(BASE_ONLY).groups(HIDDEN_TILES).build("exotic-matter:big_tex_test1");

	public static final TextureSet BIGTEX_TEST2 = TextureSet.builder(BIGTEX_TEST1).displayNameToken("big_tex_test2").scale(SMALL).build("exotic-matter:big_tex_test2");

	public static final TextureSet BIGTEX_TEST3 = TextureSet.builder(BIGTEX_TEST1).displayNameToken("big_tex_test3").scale(MEDIUM).build("exotic-matter:big_tex_test3");

	public static final TextureSet BIGTEX_TEST4 = TextureSet.builder(BIGTEX_TEST1).displayNameToken("big_tex_test4").scale(LARGE).build("exotic-matter:big_tex_test4");

	public static final TextureSet BIGTEX_TEST5 = TextureSet.builder(BIGTEX_TEST1).displayNameToken("big_tex_test5").scale(GIANT).build("exotic-matter:big_tex_test5");

	public static final TextureSet TEST = TextureSet.builder().displayNameToken("test")
			.baseTextureName("exotic-matter:block/test_1").versionCount(1).scale(SINGLE)
			.layout(TextureLayoutMap.SINGLE).transform(IDENTITY).renderIntent(BASE_ONLY).groups(HIDDEN_TILES).build("exotic-matter:test");

	public static final TextureSet TEST_90 = TextureSet.builder(TEST).displayNameToken("test_90").transform(ROTATE_90).build("exotic-matter:test_90");

	public static final TextureSet TEST_180 = TextureSet.builder(TEST).displayNameToken("test_180").transform(ROTATE_90).build("exotic-matter:test_180");

	public static final TextureSet TEST_270 = TextureSet.builder(TEST).displayNameToken("test_270").transform(ROTATE_90).build("exotic-matter:test_270");

	public static final TextureSet TEST_4X4 = TextureSet.builder().displayNameToken("test4x4")
			.baseTextureName("exotic-matter:block/test4x4").versionCount(1).scale(SMALL)
			.layout(TextureLayoutMap.SINGLE).transform(IDENTITY).renderIntent(BASE_ONLY).groups(HIDDEN_TILES).build("exotic-matter:test4x4");

	public static final TextureSet TEST_4x4_90 = TextureSet.builder(TEST_4X4).displayNameToken("test4x4_90").transform(ROTATE_90).build("exotic-matter:test4x4_90");

	public static final TextureSet TEST_4x4_180 = TextureSet.builder(TEST_4X4).displayNameToken("test4x4_180").transform(ROTATE_180).build("exotic-matter:test4x4_180");

	public static final TextureSet TEST_4x4_270 = TextureSet.builder(TEST_4X4).displayNameToken("test4x4_270").transform(ROTATE_270).build("exotic-matter:test4x4_270");

}
