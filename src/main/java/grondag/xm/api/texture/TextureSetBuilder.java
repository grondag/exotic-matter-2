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

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus.Experimental;

// TODO: add fallback texture specification
@Experimental
public interface TextureSetBuilder {
	TextureSetBuilder versionCount(int versionCount);

	TextureSetBuilder scale(TextureScale scale);

	TextureSetBuilder layout(TextureLayoutMap layout);

	TextureSetBuilder transform(TextureTransform rotation);

	TextureSetBuilder renderIntent(TextureRenderIntent renderIntent);

	TextureSetBuilder groups(TextureGroup... groups);

	TextureSetBuilder renderNoBorderAsTile(boolean renderNoBorderAsTile);

	/**
	 * Include namespace!
	 */
	TextureSetBuilder baseTextureName(String baseTextureName);

	TextureSetBuilder displayNameToken(String displayNameToken);

	TextureSet build(ResourceLocation id);

	default TextureSet build(String nameSpace, String path) {
		return build(new ResourceLocation(nameSpace, path));
	}

	default TextureSet build(String idString) {
		return build(new ResourceLocation(idString));
	}
}
