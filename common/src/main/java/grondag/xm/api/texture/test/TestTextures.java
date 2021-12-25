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

package grondag.xm.api.texture.test;

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

import org.jetbrains.annotations.ApiStatus.Internal;

import grondag.xm.api.texture.TextureLayoutMap;
import grondag.xm.api.texture.TextureSet;

@Internal
public final class TestTextures {
	private TestTextures() { }

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
