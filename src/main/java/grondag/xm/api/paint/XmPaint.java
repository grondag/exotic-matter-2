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

import grondag.xm.api.texture.TextureSet;
import grondag.xm.paint.XmPaintImpl;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * Paints control the appearance of mesh surfaces.<p>
 *
 * Paints are complex objects with serialization space
 * requirements large enough to require special handling
 * when used in abundance. <p>
 *
 * To manage this challenge, paints have three types of
 * visibility that are meant to be mutually exclusive:<p>
 *
 * <em>Anonymous Paints</em>: Paints instances returned by PaintFinder
 * are interned but have no identity or persistence on their own.  They can be
 * serialized/de-serialized using the methods provided on this interface.<p>
 *
 * <em>Registered Paints</em>: Paints that are registered are distinct
 * from anonymous instances returned by PaintFinder and can be created
 * and retrieved with the Paint Registry.  These paints are ideal
 * for mods with fixed mesh and paint configurations where can paints
 * can be retrieved and applied in initialization and never need to be
 * serialized. <p>
 *
 * <em>Indexed Paints</em>: With PaintInddex, an anonymous paint can associated with an immutable
 * numeric index at run time.  The association is persisted with the world
 * and can be serialized and later used to reconstruct an anonymous paint
 * instance from the numeric instance.  The paint associated with an index
 * can also be changed or removed. These dynamically registered paints are
 * meant for mods that create new paints at run time and  need compact serialization.
 *
 */
@Experimental
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
	 * Non-null if paint is registered.
	 * @return
	 */
	@Nullable
	Identifier id();

	/**
	 * NO_INDEX if paint is not indexed.
	 * @return
	 */
	int index();

	/**
	 * If paint is registered or indexed, will serialized based on registered ID instead of paint configuration.
	 */
	CompoundTag toTag();

	/**
	 * If paint is registered or indexed, will serialized based on registered ID instead of paint configuration.
	 */
	void toBytes(PacketByteBuf pBuff);

	/**
	 * Serializes paint configuration, discarding registered identity or index if present.
	 */
	CompoundTag toFixedTag();

	/**
	 * Serializes paint configuration, discarding registered identity or index if present.
	 */
	void toFixedBytes(PacketByteBuf pBuff);

	static XmPaint fromTag(CompoundTag tag, @Nullable PaintIndex paintIndex) {
		return XmPaintImpl.fromTag(tag, paintIndex);
	}

	static XmPaint fromBytes(PacketByteBuf pBuff, @Nullable PaintIndex paintIndex) {
		return XmPaintImpl.fromBytes(pBuff, paintIndex);
	}

	int NO_INDEX = -1;
}
