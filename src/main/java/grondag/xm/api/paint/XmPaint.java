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

package grondag.xm.api.paint;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import grondag.xm.api.texture.TextureSet;
import grondag.xm.paint.XmPaintImpl;

/**
 * Paints control the appearance of mesh surfaces.
 *
 * <p>Paints are complex objects with serialization space
 * requirements large enough to require special handling
 * when used in abundance.
 *
 * <p>To manage this challenge, paints have three types of
 * visibility that are meant to be mutually exclusive:
 *
 * <p><em>Anonymous Paints</em>: Paints instances returned by PaintFinder
 * are interned but have no identity or persistence on their own.  They can be
 * serialized/de-serialized using the methods provided on this interface.
 *
 * <p><em>Registered Paints</em>: Paints that are registered are distinct
 * from anonymous instances returned by PaintFinder and can be created
 * and retrieved with the Paint Registry.  These paints are ideal
 * for mods with fixed mesh and paint configurations where can paints
 * can be retrieved and applied in initialization and never need to be
 * serialized.
 *
 * <p><em>Indexed Paints</em>: With PaintInddex, an anonymous paint can associated with an immutable
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
	ResourceLocation shader();

	@Nullable
	ResourceLocation condition();

	VertexProcessor vertexProcessor(int textureIndex);

	/**
	 * Non-null if paint is registered.
	 * @return
	 */
	@Nullable
	ResourceLocation id();

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
	void toBytes(FriendlyByteBuf pBuff);

	/**
	 * Serializes paint configuration, discarding registered identity or index if present.
	 */
	CompoundTag toFixedTag();

	/**
	 * Serializes paint configuration, discarding registered identity or index if present.
	 */
	void toFixedBytes(FriendlyByteBuf pBuff);

	static XmPaint fromTag(CompoundTag tag, @Nullable PaintIndex paintIndex) {
		return XmPaintImpl.fromTag(tag, paintIndex);
	}

	static XmPaint fromBytes(FriendlyByteBuf pBuff, @Nullable PaintIndex paintIndex) {
		return XmPaintImpl.fromBytes(pBuff, paintIndex);
	}

	int NO_INDEX = -1;
}
