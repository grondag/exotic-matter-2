/*
 * Copyright © Original Authors
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
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.api.modelstate.base;

import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.core.BlockPos;

import grondag.xm.api.connect.state.CornerJoinState;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.surface.XmSurface;

@Experimental
public interface MutableBaseModelState<R extends BaseModelState<R, W>, W extends MutableBaseModelState<R, W>> extends BaseModelState<R, W>, MutableModelState {
	@Override
	W copyFrom(ModelState template);

	@Override
	R releaseToImmutable();

	@Override
	W setStatic(boolean isStatic);

	W paint(int surfaceIndex, XmPaint paint);

	W paint(XmSurface surface, XmPaint paint);

	W paintAll(XmPaint paint);

	W posX(int index);

	W posY(int index);

	W posZ(int index);

	W pos(BlockPos pos);

	W species(int species);

	W orientationIndex(int index);

	W cornerJoin(CornerJoinState join);

	W simpleJoin(SimpleJoinState join);

	W alternateJoin(SimpleJoinState join);

	W alternateJoinBits(int joinBits);

	W primitiveBits(int bits);

	<T> T applyAndRelease(Function<ModelState, T> func);

	W apply(Consumer<W> consumer);
}
