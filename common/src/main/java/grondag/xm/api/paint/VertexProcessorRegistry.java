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

package grondag.xm.api.paint;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.resources.ResourceLocation;

import grondag.xm.Xm;
import grondag.xm.paint.VertexProcessorRegistryImpl;

@Experimental
public interface VertexProcessorRegistry {
	ResourceLocation NONE_ID = new ResourceLocation(Xm.MODID, "none");

	VertexProcessorRegistry INSTANCE = VertexProcessorRegistryImpl.INSTANCE;

	VertexProcessor get(ResourceLocation id);

	VertexProcessor add(ResourceLocation id, VertexProcessor set);

	ResourceLocation getKey(VertexProcessor value);

	boolean containsKey(ResourceLocation id);
}
