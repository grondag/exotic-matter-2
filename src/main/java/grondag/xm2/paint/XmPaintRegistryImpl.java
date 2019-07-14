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

package grondag.xm2.paint;

import grondag.xm2.api.paint.XmPaint;
import grondag.xm2.api.paint.XmPaintRegistry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.Identifier;

public class XmPaintRegistryImpl implements XmPaintRegistry {
    public static final XmPaintRegistryImpl INSTANCE = new XmPaintRegistryImpl();

    private XmPaintRegistryImpl() {
    };

    private final Object2ObjectOpenHashMap<Identifier, XmPaintImpl.Value> paints = new Object2ObjectOpenHashMap<>();

    @Override
    public boolean register(Identifier id, XmPaint paint) {
        return paints.putIfAbsent(id, (XmPaintImpl.Value) paint) == null;
    }

    @Override
    public XmPaintImpl.Value get(int paintIndex) {
        return XmPaintImpl.byIndex(paintIndex);
    }

    @Override
    public XmPaintImpl.Value get(Identifier paintId) {
        return paints.get(paintId);
    }
}
