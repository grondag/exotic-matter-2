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

package grondag.xm.collision;

import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
interface Functions {
	@FunctionalInterface interface Int3Consumer {
		void accept(int x, int y, int z);
	}

	@FunctionalInterface interface Float3Test {
		boolean apply(float x, float y, float z);
	}

	@FunctionalInterface interface Float3Consumer {
		void accept(float x, float y, float z);
	}
}
