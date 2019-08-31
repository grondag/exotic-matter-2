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

import static org.apiguardian.api.API.Status.INTERNAL;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;

import org.apiguardian.api.API;

import grondag.xm.Xm;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.paint.XmPaintRegistry;
import grondag.xm.paint.XmPaintImpl.Value;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

@API(status = INTERNAL)
public class XmPaintRegistryImpl implements XmPaintRegistry, SimpleSynchronousResourceReloadListener {
    public static final XmPaintRegistryImpl INSTANCE = new XmPaintRegistryImpl();

    private final Identifier id = Xm.id("paint_registry");
    
    private XmPaintRegistryImpl() {
    };

    private final Object2ObjectOpenHashMap<Identifier, XmPaintImpl.Value> paints = new Object2ObjectOpenHashMap<>();
    
    @Override
    public synchronized XmPaint register(Identifier id, XmPaint paint) {
        XmPaintImpl.Value prior = paints.get(id);
        if(prior != null) {
            prior.copyFrom((XmPaintImpl) paint);
            return prior;
        } else {
            paints.put(id, (XmPaintImpl.Value) paint);
            return paint;
        }
    }

    public static Value byIndex(int index) {
        return XmPaintImpl.byIndex(index);
    }
    
    @Override
    public XmPaintImpl.Value get(int paintIndex) {
        return XmPaintImpl.byIndex(paintIndex);
    }

    @Override
    public XmPaintImpl.Value get(Identifier paintId) {
        XmPaintImpl.Value result = paints.get(paintId);
        if(result == null) {
            result = XmPaintImpl.finder().find();
            result.placeholder = true;
            register(paintId, result);
        }
        return result;
    }

    @Override
    public void apply(ResourceManager resourceManager) {
        for( Entry<Identifier, Value> e : paints.entrySet()) {
            Value newVal = loadPaint(e.getKey(), resourceManager, e.getValue().placeholder);
            if(newVal != null) {
                e.getValue().copyFrom(newVal);
            }
        }
    }

    @Override
    public Identifier getFabricId() {
        return id;
    }
    
    private XmPaintImpl.Value loadPaint(Identifier idIn, ResourceManager rm, boolean loadExpected) {
        Identifier id = new Identifier(idIn.getNamespace(), "paints/" + idIn.getPath() + ".json");
        
        XmPaintImpl.Value result = null;
        try(Resource res = rm.getResource(id)) {
            result = PaintDeserializer.deserialize(new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            if (loadExpected) {
                Xm.LOG.info("Unable to load paint " + idIn.toString() + " due to exception " + e.toString());
            }
        }
        
        return result;
    }
}
