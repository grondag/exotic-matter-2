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

package grondag.xm.modelstate;

import org.jetbrains.annotations.ApiStatus.Internal;

import grondag.xm.api.modelstate.primitive.MutablePrimitiveState;
import grondag.xm.api.modelstate.primitive.PrimitiveState;

@Internal
public class SimpleModelStateImpl extends AbstractPrimitiveModelState<SimpleModelStateImpl, PrimitiveState, MutablePrimitiveState> implements MutablePrimitiveState {
	public static final int MAX_SURFACES = 8;

	public static final ModelStateFactoryImpl<SimpleModelStateImpl, PrimitiveState, MutablePrimitiveState> FACTORY = new ModelStateFactoryImpl<>(SimpleModelStateImpl::new);

	@Override
	public final ModelStateFactoryImpl<SimpleModelStateImpl, PrimitiveState, MutablePrimitiveState> factoryImpl() {
		return FACTORY;
	}

	@Override
	protected int maxSurfaces() {
		return MAX_SURFACES;
	}
}
