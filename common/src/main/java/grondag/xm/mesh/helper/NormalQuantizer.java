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

package grondag.xm.mesh.helper;

import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class NormalQuantizer {
	private static int packComponent(float n) {
		return Math.round(n * 256) + 256;
	}

	private static float unpackComponent(int q) {
		return (q - 256) / 256f;
	}

	public static int pack(float x, float y, float z) {
		final int qx = packComponent(x);
		final int qy = packComponent(y);
		final int qz = packComponent(z);
		return qx | (qy << 10) | (qz << 20);
	}

	public static float unpackX(int q) {
		return unpackComponent(q & 0x3FF);
	}

	public static float unpackY(int q) {
		return unpackComponent((q >> 10) & 0x3FF);
	}

	public static float unpackZ(int q) {
		return unpackComponent((q >> 20) & 0x3FF);
	}
}
