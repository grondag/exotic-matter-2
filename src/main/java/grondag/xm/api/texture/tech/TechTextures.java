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
package grondag.xm.api.texture.tech;

import static grondag.xm.api.texture.TextureGroup.STATIC_DETAILS;
import static grondag.xm.api.texture.TextureRenderIntent.BASE_OR_OVERLAY_CUTOUT_OKAY;
import static grondag.xm.api.texture.TextureScale.SINGLE;
import static grondag.xm.api.texture.TextureTransform.IDENTITY;
import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

import grondag.xm.api.texture.TextureLayoutMap;
import grondag.xm.api.texture.TextureSet;

@API(status = INTERNAL)
public enum TechTextures {
	;

	public static final TextureSet DECAL_PLUS = TextureSet.builder().displayNameToken("decal_plus")
			.baseTextureName("exotic-matter:block/plus").versionCount(1)
			.scale(SINGLE).layout(TextureLayoutMap.SINGLE).transform(IDENTITY)
			.renderIntent(BASE_OR_OVERLAY_CUTOUT_OKAY).groups(STATIC_DETAILS).build("exotic-matter:decal_plus");

	public static final TextureSet DECAL_MINUS = TextureSet.builder().displayNameToken("decal_minus")
			.baseTextureName("exotic-matter:block/minus").versionCount(1)
			.scale(SINGLE).layout(TextureLayoutMap.SINGLE).transform(IDENTITY)
			.renderIntent(BASE_OR_OVERLAY_CUTOUT_OKAY).groups(STATIC_DETAILS).build("exotic-matter:decal_minus");
}
