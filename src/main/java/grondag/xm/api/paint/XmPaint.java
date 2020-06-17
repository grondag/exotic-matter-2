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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;

import grondag.xm.api.texture.TextureSet;
import grondag.xm.paint.XmPaintImpl;

@API(status = EXPERIMENTAL)
public interface XmPaint {
	static XmPaintFinder finder() {
		return XmPaintImpl.finder();
	}

	int MAX_TEXTURE_DEPTH = 3;

	@Deprecated
	@Nullable
	PaintBlendMode blendMode(int textureIndex);

	PaintBlendMode blendMode();

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

	/**
	 * True when this paint is registered defined in a JSON file.
	 * Paints like this aren't loaded during registration and thus cannot
	 * be copied or transformed during registration.
	 */
	boolean external();

	/**
	 * Non-null if paint is registered.
	 * @return
	 */
	@Nullable
	Identifier id();

	/**
	 * If paint is registered, will serialized based on registered ID instead of paint configuration.
	 */
	CompoundTag toTag();

	/**
	 * Serializes paint configuration, discarding registered identity if present.
	 */
	CompoundTag toFixedTag();

	static XmPaint fromTag(CompoundTag tag) {
		return XmPaintImpl.fromTag(tag);
	}
}
