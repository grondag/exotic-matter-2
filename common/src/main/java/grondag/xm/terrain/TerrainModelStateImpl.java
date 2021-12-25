/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.xm.terrain;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.BlockGetter;

import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.paint.PaintIndex;
import grondag.xm.api.terrain.TerrainModelState;
import grondag.xm.modelstate.AbstractPrimitiveModelState;

@Internal
public class TerrainModelStateImpl extends AbstractPrimitiveModelState<TerrainModelStateImpl, TerrainModelState, TerrainModelState.Mutable> implements TerrainModelState.Mutable {
	public static final ModelStateFactoryImpl<TerrainModelStateImpl, TerrainModelState, TerrainModelState.Mutable> FACTORY = new ModelStateFactoryImpl<>(TerrainModelStateImpl::new);

	private long flowBits;
	private int glowBits;

	public void doRefreshFromWorld(XmBlockState xmState, BlockGetter world, BlockPos pos) {
		//TODO: restore super state retrieval and move whole thing to external helper
		//super.doRefreshFromWorld(xmState, world, pos);

		TerrainState.produceBitsFromWorldStatically(xmState.blockState(), world, pos, (t, h) -> {
			flowBits = t;
			glowBits = h;
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
		final TerrainModelStateImpl result = FACTORY.claimInner(primitive);
		result.flowBits = flowBits;
		return result;
	}

	@Override
	public void fromBytes(FriendlyByteBuf pBuff, PaintIndex paintIndex) {
		super.fromBytes(pBuff, paintIndex);
		flowBits = pBuff.readLong();
		glowBits = pBuff.readVarInt();
	}

	@Override
	public void toBytes(FriendlyByteBuf pBuff) {
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
