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

package grondag.xm.api.paint;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

/**
 * Server-safe copy of BlendMode used in rendering.
 */
@API(status = EXPERIMENTAL)
public enum PaintBlendMode {
	/**
	 * Emulate blending behavior associated with the block.
	 */
	DEFAULT,

	/**
	 * Fully opaque with depth test, no blending. Used for most normal blocks.
	 */
	SOLID,

	/**
	 * Pixels with alpha > 0.5 are rendered as if {@code SOLID}. Other pixels are not rendered.
	 * Texture mip-map enabled.  Used for leaves.
	 */
	CUTOUT_MIPPED,

	/**
	 * Pixels with alpha > 0.5 are rendered as if {@code SOLID}. Other pixels are not rendered.
	 * Texture mip-map disabled.  Used for iron bars, glass and other cutout sprites with hard edges.
	 */
	CUTOUT,

	/**
	 * Pixels are blended with the background according to alpha color values. Some performance cost,
	 * use in moderation. Texture mip-map enabled.  Used for stained glass.
	 */
	TRANSLUCENT;


}
