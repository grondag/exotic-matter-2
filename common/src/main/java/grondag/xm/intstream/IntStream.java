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

package grondag.xm.intstream;

public interface IntStream {
	int get(int address);

	void set(int address, int value);

	default void setFloat(int address, float value) {
		set(address, Float.floatToRawIntBits(value));
	}

	default float getFloat(int address) {
		return Float.intBitsToFloat(get(address));
	}

	default void copyFrom(int targetAddress, IntStream source, int sourceAddress, int length) {
		for (int i = 0; i < length; i++) {
			set(targetAddress + i, source.get(sourceAddress + i));
		}
	}

	default void copyFrom(int targetAddress, int[] source, int sourceIndex, int length) {
		for (int i = 0; i < length; i++) {
			set(targetAddress + i, source[sourceIndex + i]);
		}
	}

	default void copyTo(int sourceAddress, int[] target, int targetAddress, int length) {
		for (int i = 0; i < length; i++) {
			target[targetAddress + i] = get(sourceAddress + i);
		}
	}

	default void release() {
	}

	/**
	 * Sets all ints in the stream to zero. Does not deallocate any storage.
	 */
	void clear();

	/**
	 * Releases unused storage and partial blocks, if underlying implementation uses
	 * blocks. May cause unpooled allocation and later garbage collection, so use
	 * only when going to keep the stream around a while. Will become uncompacted if
	 * data are added.
	 */
	void compact();

	/**
	 * For testing purposes only, the actual number of ints allocated by the stream.
	 */
	int capacity();
}
