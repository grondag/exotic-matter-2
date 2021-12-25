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

package grondag.xm.paint;

import java.io.Reader;
import java.util.Locale;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import grondag.xm.api.paint.PaintBlendMode;
import grondag.xm.api.paint.VertexProcessorRegistry;
import grondag.xm.api.paint.XmPaintFinder;
import grondag.xm.api.texture.TextureSetRegistry;

class PaintDeserializer {
	public static XmPaintImpl.Finder deserialize(Reader reader) {
		final XmPaintImpl.Finder finder = XmPaintImpl.finder();
		final JsonObject json = GsonHelper.parse(reader);

		if (json.has("layers")) {
			final JsonArray layers = GsonHelper.convertToJsonArray(json.get("layers"), "layers");

			if (!layers.isJsonNull()) {
				final int depth = layers.size();

				if (depth > 3) {
					return null;
				}

				finder.textureDepth(depth);

				for (int i = 0; i < depth; i++) {
					readLayer(layers.get(i).getAsJsonObject(), finder, i);
				}
			}
		}

		return finder;
	}

	private static void readLayer(JsonObject layer, XmPaintFinder finder, int spriteIndex) {
		if (layer.has("disableAo")) {
			finder.disableAo(spriteIndex, GsonHelper.getAsBoolean(layer, "disableAo", true));
		}

		if (layer.has("disableColorIndex")) {
			finder.disableColorIndex(spriteIndex, GsonHelper.getAsBoolean(layer, "disableColorIndex", true));
		}

		if (layer.has("disableDiffuse")) {
			finder.disableDiffuse(spriteIndex, GsonHelper.getAsBoolean(layer, "disableDiffuse", true));
		}

		if (layer.has("emissive")) {
			finder.emissive(spriteIndex, GsonHelper.getAsBoolean(layer, "emissive", true));
		}

		if (spriteIndex == 0 && layer.has("blendMode")) {
			finder.blendMode(readBlendMode(GsonHelper.getAsString(layer, "blendMode")));
		}

		if (layer.has("color")) {
			finder.textureColor(spriteIndex, color(GsonHelper.getAsString(layer, "color")));
		}

		if (layer.has("texture")) {
			finder.texture(spriteIndex, TextureSetRegistry.instance().get(new ResourceLocation(GsonHelper.getAsString(layer, "texture"))));
		}

		if (layer.has("processor")) {
			finder.vertexProcessor(spriteIndex, VertexProcessorRegistry.INSTANCE.get(new ResourceLocation(GsonHelper.getAsString(layer, "processor"))));
		}
	}

	private static PaintBlendMode readBlendMode(String val) {
		val = val.toLowerCase(Locale.ROOT);
		switch (val) {
			case "solid":
			default:
				return PaintBlendMode.SOLID;
			case "cutout":
				return PaintBlendMode.CUTOUT;
			case "cutout_mipped":
				return PaintBlendMode.CUTOUT_MIPPED;
			case "translucent":
				return PaintBlendMode.TRANSLUCENT;
		}
	}

	private static int color(String str) {
		return str.startsWith("0x") ? Integer.parseUnsignedInt(str.substring(2), 16) : Integer.parseInt(str);
	}
}
