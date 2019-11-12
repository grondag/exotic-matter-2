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

import grondag.xm.collision.CollisionDispatcherImpl;
import grondag.xm.dispatch.XmDispatcher;
import grondag.xm.dispatch.XmVariantProvider;
import grondag.xm.mesh.helper.PolyTransformImpl;
import grondag.xm.modelstate.AbstractPrimitiveModelState;
import grondag.xm.network.Packets;
import grondag.xm.paint.XmPaintRegistryImpl;
import grondag.xm.primitive.ModelPrimitiveRegistryImpl;
import grondag.xm.texture.XmTexturesImpl;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.render.InvalidateRenderStateCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

@API(status = INTERNAL)
public class XmClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        XmTexturesImpl.init();
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(r -> new XmVariantProvider());
        InvalidateRenderStateCallback.EVENT.register(XmClient::invalidate);
        Packets.initializeClient();
        AbstractPrimitiveModelState.useClientHandler();
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(XmPaintRegistryImpl.INSTANCE);
    }

    public static void invalidate() {
        PolyTransformImpl.invalidateCache();
        XmDispatcher.INSTANCE.clear();
        CollisionDispatcherImpl.clear();
        ModelPrimitiveRegistryImpl.INSTANCE.invalidateCache();
    }
}
