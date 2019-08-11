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
package grondag.xm.model.state;

import grondag.xm.block.XmBlockRegistryImpl.XmBlockStateImpl;
import grondag.xm.terrain.TerrainState;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

//TODO: move to terrain package
public class TerrainModelStateImpl extends AbstractPrimitiveModelState<TerrainModelStateImpl, TerrainModelState, TerrainModelState.Mutable> implements TerrainModelState.Mutable {
    public static final ModelStateFactoryImpl<TerrainModelStateImpl, TerrainModelState, TerrainModelState.Mutable> FACTORY = new ModelStateFactoryImpl<>(TerrainModelStateImpl::new);

    private long flowBits;
    private int glowBits;

    @Override
    protected int intSize() {
        return super.intSize() + 3;
    }

    public void doRefreshFromWorld(XmBlockStateImpl xmState, BlockView world, BlockPos pos) {
        //TODO: restore super state retrieval and move whole thing to external helper
        //super.doRefreshFromWorld(xmState, world, pos);

        TerrainState.produceBitsFromWorldStatically(xmState.blockState, world, pos, (t, h) -> {
            this.flowBits = t;
            this.glowBits = h;
            return null;
        });
    }

    @Override
    public long getTerrainStateKey() {
        return flowBits;
    }

    @Override
    public int getTerrainHotness() {
        return glowBits;
    }

    @Override
    public TerrainModelState.Mutable setTerrainStateKey(long terrainStateKey) {
        flowBits = terrainStateKey;
        return this;
    }

    @Override
    public TerrainState getTerrainState() {
        return new TerrainState(flowBits, glowBits);
    }

    @Override
    public TerrainModelState.Mutable setTerrainState(TerrainState flowState) {
        flowBits = flowState.getStateKey();
        glowBits = flowState.getHotness();
        invalidateHashCode();
        return this;
    }

    @Override
    public TerrainModelState.Mutable geometricState() {
        TerrainModelStateImpl result = FACTORY.claimInner(this.primitive);
        result.flowBits = this.flowBits;
        return result;
    }

    @Override
    public void fromBytes(PacketByteBuf pBuff) {
        super.fromBytes(pBuff);
        flowBits = pBuff.readLong();
        glowBits = pBuff.readVarInt();
    }

    @Override
    public void toBytes(PacketByteBuf pBuff) {
        super.toBytes(pBuff);
        pBuff.writeLong(flowBits);
        pBuff.writeVarInt(glowBits);
    }

    @Override
    public ModelStateFactoryImpl<TerrainModelStateImpl, TerrainModelState, TerrainModelState.Mutable> factoryImpl() {
        return FACTORY;
    }

    @Override
    protected int maxSurfaces() {
        return 4;
    }
}
