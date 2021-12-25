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

package grondag.xm.api.util;

public class ColorUtil {
	public static int interpolate(int from, int to, float toWeight) {
		final int a1 = (from >> 24) & 0xFF;
		final int r1 = (from >> 16) & 0xFF;
		final int g1 = (from >> 8) & 0xFF;
		final int b1 = from & 0xFF;

		final int a2 = (to >> 24) & 0xFF;
		final int r2 = (to >> 16) & 0xFF;
		final int g2 = (to >> 8) & 0xFF;
		final int b2 = to & 0xFF;

		final int a = (int) (a1 + 0.49f + (a2 - a1) * toWeight) & 0xFF;
		final int r = (int) (r1 + 0.49f + (r2 - r1) * toWeight) & 0xFF;
		final int g = (int) (g1 + 0.49f + (g2 - g1) * toWeight) & 0xFF;
		final int b = (int) (b1 + 0.49f + (b2 - b1) * toWeight) & 0xFF;

		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	public static int swapRedBlue(int color) {
		return (color & 0xFF00FF00) | ((color & 0x00FF0000) >> 16) | ((color & 0xFF) << 16);
	}

	/** Arguments are assumed to be ARGB. */
	public static int multiplyColor(int color1, int color2) {
		final int red = ((color1 >> 16) & 0xFF) * ((color2 >> 16) & 0xFF) / 0xFF;
		final int green = ((color1 >> 8) & 0xFF) * ((color2 >> 8) & 0xFF) / 0xFF;
		final int blue = (color1 & 0xFF) * (color2 & 0xFF) / 0xFF;
		final int alpha = ((color1 >> 24) & 0xFF) * ((color2 >> 24) & 0xFF) / 0xFF;

		return (alpha << 24) | (red << 16) | (green << 8) | blue;
	}

	/** Arguments are assumed to be ARGB - does not modify alpha. */
	public static int multiplyRGB(int color, float shade) {
		final int red = (int) (((color >> 16) & 0xFF) * shade);
		final int green = (int) (((color >> 8) & 0xFF) * shade);
		final int blue = (int) ((color & 0xFF) * shade);
		final int alpha = ((color >> 24) & 0xFF);

		return (alpha << 24) | (red << 16) | (green << 8) | blue;
	}
}
