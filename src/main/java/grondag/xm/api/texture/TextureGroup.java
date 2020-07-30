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
package grondag.xm.api.texture;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

@API(status = EXPERIMENTAL)
public enum TextureGroup {
	STATIC_TILES,
	STATIC_BORDERS,
	STATIC_DETAILS,
	DYNAMIC_TILES,
	DYNAMIC_BORDERS,
	DYNAMIC_DETAILS,
	HIDDEN_TILES,
	HIDDEN_BORDERS,
	HIDDEN_DETAILS,
	ALWAYS_HIDDEN;

	/** used as a fast way to filter textures from a list */
	public final int bitFlag;

	private TextureGroup() {
		bitFlag = (1 << ordinal());
	}

	public static int makeTextureGroupFlags(TextureGroup... groups) {
		int flags = 0;
		for (final TextureGroup group : groups) {
			flags |= group.bitFlag;
		}
		return flags;
	}

	public static int HIDDEN = HIDDEN_TILES.bitFlag | HIDDEN_BORDERS.bitFlag | HIDDEN_DETAILS.bitFlag | ALWAYS_HIDDEN.bitFlag;
}
