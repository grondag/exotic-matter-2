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
import java.util.function.Function;

import grondag.xm.api.connect.state.CornerJoinState;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.ModelPrimitive;
import grondag.xm.api.surface.XmSurface;
import grondag.xm.mesh.polygon.Polygon;
import grondag.xm.model.varia.BlockOrientationType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public interface PrimitiveModelState<R extends PrimitiveModelState<R, W>, W extends PrimitiveModelState.Mutable<R,W>> extends ModelState {
    
    public static interface ModelStateFactory<R extends PrimitiveModelState<R, W>, W extends PrimitiveModelState.Mutable<R,W>> {

        W claim(ModelPrimitive<R, W> primitive);

        W fromBuffer(ModelPrimitive<R, W> primitive, PacketByteBuf buf);

        W fromTag(ModelPrimitive<R, W> primitive, CompoundTag tag);

    }
    
    ModelStateFactory<R, W> factory();

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

    @Override
    boolean equalsIncludeStatic(Object obj);

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
    void serializeNBT(CompoundTag tag);

    void fromBytes(PacketByteBuf pBuff);

    @Override
    void toBytes(PacketByteBuf pBuff);

    int stateFlags();

    ModelPrimitive<R, W> primitive();

    @Override
    void produceQuads(Consumer<Polygon> target);

    @Override
    W geometricState();

    int orientationIndex();

    BlockOrientationType orientationType();

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
    List<BakedQuad> getBakedQuads(BlockState state, Direction face, Random rand);

    @Override
    @Environment(EnvType.CLIENT)
    void emitQuads(RenderContext context);
    
    public static interface Mutable<R extends PrimitiveModelState<R, W>, W extends PrimitiveModelState.Mutable<R, W>> extends PrimitiveModelState<R, W>, ModelState.Mutable {
        @Override
        W copyFrom(ModelState template);

        @Override
        R releaseToImmutable();
        
        @Override
        W setStatic(boolean isStatic);

        W paint(int surfaceIndex, int paintIndex);

        W paint(int surfaceIndex, XmPaint paint);
        
        W paint(XmSurface surface, XmPaint paint);

        W paint(XmSurface surface, int paintIndex);

        W paintAll(XmPaint paint);

        W paintAll(int paintIndex);

        W posX(int index);

        W posY(int index);

        W posZ(int index);

        W pos(BlockPos pos);
        
        W species(int species);

        W orientationIndex(int index);

        W cornerJoin(CornerJoinState join);

        W simpleJoin(SimpleJoinState join);

        W masonryJoin(SimpleJoinState join);
        
        W primitiveBits(int bits);

        <T> T applyAndRelease(Function<ModelState, T> func);
        
        W apply(Consumer<W> consumer);
    }
}
