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
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

@API(status = EXPERIMENTAL)
public class McTextures {
    private McTextures() {}
    
    public static final TextureSet STONE = TextureSet.builder()
            .displayNameToken("mc_stone").baseTextureName("minecraft:block/stone")
            .versionCount(1).scale(SINGLE).layout(TextureLayoutMap.SINGLE).transform(STONE_LIKE)
            .renderIntent(BASE_ONLY).groups(STATIC_TILES).build("minecraft:stone");
}
