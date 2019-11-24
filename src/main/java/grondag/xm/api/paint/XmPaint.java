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
package grondag.xm.api.paint;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

import grondag.xm.api.texture.TextureSet;
import grondag.xm.paint.XmPaintImpl;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.util.Identifier;

@API(status = EXPERIMENTAL)
public interface XmPaint {
	static XmPaintFinder finder() {
		return XmPaintImpl.finder();
	}

	int MAX_TEXTURE_DEPTH = 3;

	int index();

	@Nullable
	BlendMode blendMode(int textureIndex);

	boolean disableColorIndex(int textureIndex);

	TextureSet texture(int textureIndex);

	int textureColor(int textureIndex);

	int textureDepth();

	boolean emissive(int textureIndex);

	boolean disableDiffuse(int textureIndex);

	boolean disableAo(int textureIndex);

	@Nullable
	Identifier shader();

	@Nullable
	Identifier condition();

	VertexProcessor vertexProcessor(int textureIndex);
}
