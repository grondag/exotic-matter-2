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

package grondag.xm.paint;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

import grondag.xm.Xm;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.paint.XmPaintRegistry;
import grondag.xm.paint.XmPaintImpl.Value;

/**
 * NB: the default paint at index zero has a special significance/requirement.
 * Because it used as the default paint value in a model state implementation
 * it needs to have no flags that would affect model state flags, especially block joins.
 * We create the default value in the constructor here to ensure this requirement
 * is always met.
 */
@Internal
public class XmPaintRegistryImpl implements XmPaintRegistry, SimpleSynchronousResourceReloadListener {
	public static final XmPaintRegistryImpl INSTANCE = new XmPaintRegistryImpl();

	private final ResourceLocation DEFAULT_PAINT_ID = Xm.id("default");
	private final ResourceLocation id = Xm.id("paint_registry");

	private XmPaintRegistryImpl() {
		// see header notes
		register(DEFAULT_PAINT_ID, XmPaintImpl.DEFAULT_PAINT);
	}

	private final Object2ObjectOpenHashMap<ResourceLocation, XmPaintImpl.Value> paints = new Object2ObjectOpenHashMap<>();

	@Override
	public synchronized XmPaint register(ResourceLocation id, XmPaint paint) {
		final XmPaintImpl.Value prior = paints.get(id);

		if (prior != null) {
			prior.copyFrom((XmPaintImpl) paint);
			return prior;
		} else {
			paints.put(id, (XmPaintImpl.Value) paint);
			return paint;
		}
	}

	@Override
	public XmPaintImpl.Value get(ResourceLocation paintId) {
		XmPaintImpl.Value result = paints.get(paintId);

		if (result == null) {
			result = XmPaintImpl.finder().id(paintId).find();
			result.external = true;
			register(paintId, result);
		}

		return result;
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		for (final Entry<ResourceLocation, Value> e : paints.entrySet()) {
			final ResourceLocation key = e.getKey();

			if (key.equals(DEFAULT_PAINT_ID)) {
				continue;
			}

			final XmPaintImpl newVal = loadPaint(key, resourceManager, e.getValue().external);

			if (newVal != null) {
				e.getValue().copyFrom(newVal);
			}
		}
	}

	@Override
	public ResourceLocation getFabricId() {
		return id;
	}

	private XmPaintImpl loadPaint(ResourceLocation idIn, ResourceManager rm, boolean loadExpected) {
		final ResourceLocation id = new ResourceLocation(idIn.getNamespace(), "paints/" + idIn.getPath() + ".json");

		XmPaintImpl result = XmPaintImpl.DEFAULT_PAINT;

		try (Resource res = rm.getResource(id)) {
			result = PaintDeserializer.deserialize(new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8));
		} catch (final Exception e) {
			if (loadExpected) {
				Xm.LOG.info("Unable to load paint " + idIn.toString() + " due to exception " + e.toString());
			}
		}

		return result;
	}
}
