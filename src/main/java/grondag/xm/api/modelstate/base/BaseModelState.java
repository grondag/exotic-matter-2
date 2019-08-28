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
package grondag.xm.api.modelstate.base;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import org.apiguardian.api.API;

import grondag.xm.api.connect.state.CornerJoinState;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.orientation.OrientationType;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.ModelPrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.Direction;

@API(status = EXPERIMENTAL)
public interface BaseModelState<R extends BaseModelState<R, W>, W extends MutableBaseModelState<R,W>> extends ModelState {
    
    BaseModelStateFactory<R, W> factory();

    @Override
    R toImmutable();

    @Override
    W mutableCopy();

    /**
     * Does NOT consider isStatic in comparison.<p>
     * 
     * {@inheritDoc}
     */
    @Override
    boolean equals(Object obj);

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

    @Override
    void toTag(CompoundTag tag);

    void fromBytes(PacketByteBuf pBuff);

    @Override
    void toBytes(PacketByteBuf pBuff);

    int stateFlags();

    ModelPrimitive<R, W> primitive();

    @Override
    void emitPolygons(Consumer<Polygon> target);

    @Override
    W geometricState();

    int orientationIndex();

    OrientationType orientationType();

    @Override
    boolean isStatic();

    boolean doPaintsMatch(ModelState other);

    int paintIndex(int surfaceIndex);

    XmPaint paint(int surfaceIndex);

    XmPaint paint(XmSurface surface);

    int posX();

    int posY();

    int posZ();

    /**
     * Means that one or more elements (like a texture) uses species. Does not mean
     * that the shape or block actually capture or generate species other than 0.
     */
    boolean hasSpecies();

    /**
     * Will return 0 if model state does not include species. This is more
     * convenient than checking each place species is used.
     * 
     * @return
     */
    int species();

    CornerJoinState cornerJoin();
    
    SimpleJoinState simpleJoin();
    
    SimpleJoinState masonryJoin();


    int primitiveBits();

    ////////////////////////////////////////// RENDERING //////////////////////////////////////////
    
    @Override
    @Environment(EnvType.CLIENT)
    List<BakedQuad> bakedQuads(BlockState state, Direction face, Random rand);

    @Override
    @Environment(EnvType.CLIENT)
    void emitQuads(RenderContext context);
}
