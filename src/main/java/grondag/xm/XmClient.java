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

package grondag.xm;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

import net.minecraft.client.render.RenderLayers;
import net.minecraft.resource.ResourceType;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

import grondag.xm.collision.CollisionDispatcherImpl;
import grondag.xm.dispatch.XmDispatcher;
import grondag.xm.dispatch.XmVariantProvider;
import grondag.xm.mesh.helper.PolyTransformImpl;
import grondag.xm.modelstate.AbstractPrimitiveModelState;
import grondag.xm.network.Packets;
import grondag.xm.paint.PaintSynchronizerImpl;
import grondag.xm.paint.XmPaintRegistryImpl;
import grondag.xm.primitive.ModelPrimitiveRegistryImpl;
import grondag.xm.texture.XmTexturesImpl;

@API(status = INTERNAL)
public class XmClient implements ClientModInitializer {
	public static PaintSynchronizerImpl paintSync = new PaintSynchronizerImpl();

	@Override
	public void onInitializeClient() {
		XmTexturesImpl.init();
		ModelLoadingRegistry.INSTANCE.registerVariantProvider(r -> new XmVariantProvider());
		InvalidateRenderStateCallback.EVENT.register(XmClient::invalidate);
		Packets.initializeClient();
		AbstractPrimitiveModelState.useClientHandler();
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(XmPaintRegistryImpl.INSTANCE);

		SidedHelper.RENDER_LAYER_REMAPPER  = (b, s) -> BlockRenderLayerMap.INSTANCE.putBlock(b, RenderLayers.getBlockLayer(s));
		SidedHelper.RENDER_LAYER_REMAPS.forEach(SidedHelper.RENDER_LAYER_REMAPPER);
		SidedHelper.RENDER_LAYER_REMAPS.clear();
	}

	public static void invalidate() {
		PolyTransformImpl.invalidateCache();
		XmDispatcher.INSTANCE.clear();
		CollisionDispatcherImpl.clear();
		ModelPrimitiveRegistryImpl.INSTANCE.invalidateCache();
	}
}
