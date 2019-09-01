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

import static grondag.xm.api.texture.TextureGroup.STATIC_TILES;
import static grondag.xm.api.texture.TextureRenderIntent.BASE_ONLY;
import static grondag.xm.api.texture.TextureScale.SINGLE;
import static grondag.xm.api.texture.TextureTransform.STONE_LIKE;
import static grondag.xm.api.texture.TextureTransform.IDENTITY;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

@API(status = EXPERIMENTAL)
public class McTextures {
    private McTextures() {}
    
    public static final TextureSet STONE = TextureSet.builder()
            .displayNameToken("mc_stone").baseTextureName("minecraft:block/stone")
            .versionCount(1).scale(SINGLE).layout(TextureLayoutMap.SINGLE).transform(STONE_LIKE)
            .renderIntent(BASE_ONLY).groups(STATIC_TILES).build("minecraft:stone");
    
    public static final TextureSet ANDESITE = single("andesite");
    public static final TextureSet BRICK = single("bricks");
    public static final TextureSet COBBLESTONE = single("cobblestone");
    public static final TextureSet DARK_PRISMARINE = single("dark_prismarine");
    public static final TextureSet DIORITE = single("diorite");
    public static final TextureSet END_STONE_BRICK = single("end_stone_bricks");
    public static final TextureSet GRANITE = single("granite");
    public static final TextureSet MOSSY_COBBLESTONE = single("mossy_cobblestone");
    public static final TextureSet MOSSY_STONE_BRICK = single("mossy_stone_bricks");
    public static final TextureSet NETHER_BRICK = single("nether_bricks");
    public static final TextureSet POLISHED_ANDESITE = single("polished_andesite");
    public static final TextureSet POLISHED_DIORITE = single("polished_diorite");
    public static final TextureSet POLISHED_GRANITE = single("polished_granite");
    public static final TextureSet PRISMARINE_BRICK = single("prismarine_bricks");
    public static final TextureSet PRISMARINE = single("prismarine");
    public static final TextureSet PURPUR_BLOCK = single("purpur_block");
    public static final TextureSet QUARTZ_BLOCK_TOP = single("quartz_block_top");
    public static final TextureSet QUARTZ_BLOCK_BOTTOM = single("quartz_block_bottom");
    public static final TextureSet QUARTZ_BLOCK_SIDE = single("quartz_block_side");
    public static final TextureSet RED_NETHER_BRICK = single("red_nether_bricks");
    public static final TextureSet RED_SANDSTONE = single("red_sandstone");
    public static final TextureSet RED_SANDSTONE_TOP = single("red_sandstone_top");
    public static final TextureSet RED_SANDSTONE_BOTTOM = single("red_sandstone_bottom");
    public static final TextureSet SANDSTONE = single("sandstone");
    public static final TextureSet SANDSTONE_TOP = single("sandstone_top");
    public static final TextureSet SANDSTONE_BOTTOM = single("sandstone_bottom");
    public static final TextureSet SMOOTH_QUARTZ = single("smooth_quartz");
    public static final TextureSet STONE_BRICK = single("stone_bricks");
    
    public static TextureSet single(String name) {
        return TextureSet.builder()
                .displayNameToken("mc_" + name).baseTextureName("minecraft:block/" + name)
                .versionCount(1).scale(SINGLE).layout(TextureLayoutMap.SINGLE).transform(IDENTITY)
                .renderIntent(BASE_ONLY).groups(STATIC_TILES).build("minecraft:" + name);
    }
}
