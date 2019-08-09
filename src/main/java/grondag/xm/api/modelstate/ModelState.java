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
package grondag.xm.api.modelstate;

import java.util.List;
import java.util.Random;

import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.paint.XmPaintRegistry;
import grondag.xm.api.surface.XmSurface;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.Direction;

public interface ModelState extends ModelWorldState, ModelPrimitiveState {
    MutableModelState mutableCopy();

    /**
     * Persisted but not part of hash nor included in equals comparison. If true,
     * refreshFromWorldState does nothing.
     */
    boolean isStatic();
    
    boolean isImmutable();
    
    ModelState toImmutable();

    @Environment(EnvType.CLIENT)
    List<BakedQuad> getBakedQuads(BlockState state, Direction face, Random rand);

    @Environment(EnvType.CLIENT)
    void emitQuads(RenderContext context);
    
    int paintIndex(int surfaceIndex);

    default XmPaint paint(int surfaceIndex) {
        return XmPaintRegistry.INSTANCE.get(paintIndex(surfaceIndex));
    }

    default XmPaint paint(XmSurface surface) {
        return XmPaintRegistry.INSTANCE.get(paintIndex(surface.ordinal()));
    }

    /**
     * Returns true if visual elements and geometry match. Does not consider species
     * in matching.
     */
    boolean doShapeAndAppearanceMatch(ModelState other);

    /**
     * Returns true if visual elements match. Does not consider species or geometry
     * in matching.
     */
    boolean doesAppearanceMatch(ModelState other);

    void serializeNBT(CompoundTag tag);

    default CompoundTag toTag() {
        CompoundTag result = new CompoundTag();
        serializeNBT(result);
        return result;
    }

    void toBytes(PacketByteBuf pBuff);

    int stateFlags();
}
