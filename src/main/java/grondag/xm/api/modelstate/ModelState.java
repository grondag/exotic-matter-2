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
import java.util.function.Consumer;

import grondag.xm.mesh.polygon.IPolygon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.Direction;

public interface ModelState {
    MutableModelState mutableCopy();

    /**
     * Persisted but not part of hash nor included in equals comparison. If true,
     * refreshFromWorldState does nothing.
     */
    boolean isStatic();
    
    boolean equalsIncludeStatic(Object obj);
    
    boolean isImmutable();
    
    ModelState toImmutable();

    @Environment(EnvType.CLIENT)
    List<BakedQuad> getBakedQuads(BlockState state, Direction face, Random rand);

    @Environment(EnvType.CLIENT)
    void emitQuads(RenderContext context);
    
    /**
     * Output polygons must be quads or tris. Consumer MUST NOT hold references to
     * any of the polys received.
     */
    void produceQuads(Consumer<IPolygon> target);
    
    void serializeNBT(CompoundTag tag);

    default CompoundTag toTag() {
        CompoundTag result = new CompoundTag();
        serializeNBT(result);
        return result;
    }

    void toBytes(PacketByteBuf pBuff);

  /**
  * Returns a copy of this model state with only the bits that matter for
  * geometry. Used as lookup key for block damage models.
  */
    ModelState geometricState();
}
