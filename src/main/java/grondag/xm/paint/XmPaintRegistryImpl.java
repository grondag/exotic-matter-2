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
package grondag.xm.paint;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

import grondag.xm.Xm;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.paint.XmPaintRegistry;
import grondag.xm.paint.XmPaintImpl.Value;

/**
 *
 * NB: the default paint at index zero has a special significance/requirement.
 * Because it used as the default paint value in a model state implementation
 * it needs to have no flags that would affect model state flags, especially block joins.
 * We create the default value in the constructor here to ensure this requirement
 * is always met.
 */
@Internal
public class XmPaintRegistryImpl implements XmPaintRegistry, SimpleSynchronousResourceReloadListener {
	public static final XmPaintRegistryImpl INSTANCE = new XmPaintRegistryImpl();

	private final Identifier DEFAULT_PAINT_ID = Xm.id("default");
	private final Identifier id = Xm.id("paint_registry");

	private XmPaintRegistryImpl() {
		// see header notes
		register(DEFAULT_PAINT_ID, XmPaintImpl.DEFAULT_PAINT);
	}

	private final Object2ObjectOpenHashMap<Identifier, XmPaintImpl.Value> paints = new Object2ObjectOpenHashMap<>();

	@Override
	public synchronized XmPaint register(Identifier id, XmPaint paint) {
		final XmPaintImpl.Value prior = paints.get(id);

		if(prior != null) {
			prior.copyFrom((XmPaintImpl) paint);
			return prior;
		} else {
			paints.put(id, (XmPaintImpl.Value) paint);
			return paint;
		}
	}

	@Override
	public XmPaintImpl.Value get(Identifier paintId) {
		XmPaintImpl.Value result = paints.get(paintId);

		if(result == null) {
			result = XmPaintImpl.finder().id(paintId).find();
			result.external = true;
			register(paintId, result);
		}

		return result;
	}

	@Override
	public void apply(ResourceManager resourceManager) {
		for( final Entry<Identifier, Value> e : paints.entrySet()) {
			final Identifier key = e.getKey();

			if (key.equals(DEFAULT_PAINT_ID)) {
				continue;
			}

			final XmPaintImpl newVal = loadPaint(key, resourceManager, e.getValue().external);

			if(newVal != null) {
				e.getValue().copyFrom(newVal);
			}
		}
	}

	@Override
	public Identifier getFabricId() {
		return id;
	}

	private XmPaintImpl loadPaint(Identifier idIn, ResourceManager rm, boolean loadExpected) {
		final Identifier id = new Identifier(idIn.getNamespace(), "paints/" + idIn.getPath() + ".json");

		XmPaintImpl result = XmPaintImpl.DEFAULT_PAINT;

		try(Resource res = rm.getResource(id)) {
			result = PaintDeserializer.deserialize(new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8));
		} catch (final Exception e) {
			if (loadExpected) {
				Xm.LOG.info("Unable to load paint " + idIn.toString() + " due to exception " + e.toString());
			}
		}

		return result;
	}
}
