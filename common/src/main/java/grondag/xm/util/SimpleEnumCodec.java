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

package grondag.xm.util;

import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class SimpleEnumCodec<T extends Enum<?>> {
	private final T[] values;
	private final Object2ObjectOpenHashMap<String, T> map = new Object2ObjectOpenHashMap<>();

	public final int count;

	public SimpleEnumCodec(Class<T> clazz) {
		this.values = clazz.getEnumConstants();
		count = values.length;

		for (final var val : values) {
			map.put(val.name(), val);
		}
	}

	public final T fromOrdinal(int ordinal) {
		return values[ordinal];
	}

	public final T fromName(String name) {
		return map.get(name);
	}

	public void forEach(Consumer<T> consumer) {
		for (final T val : values) {
			consumer.accept(val);
		}
	}
}
