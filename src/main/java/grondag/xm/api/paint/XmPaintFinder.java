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

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.util.Identifier;

import grondag.xm.api.texture.TextureSet;

@Experimental
public interface XmPaintFinder {
	XmPaint find();

	XmPaintFinder clear();

	XmPaintFinder copy(XmPaint paint);

	XmPaintFinder textureDepth(int depth);

	XmPaintFinder textureColor(int textureIndex, int colorARBG);

	XmPaintFinder texture(int textureIndex, TextureSet sprite);

	@Deprecated
	XmPaintFinder blendMode(int textureIndex, PaintBlendMode blendMode);

	XmPaintFinder blendMode(PaintBlendMode blendMode);

	XmPaintFinder disableColorIndex(int textureIndex, boolean disable);

	XmPaintFinder disableDiffuse(int textureIndex, boolean disable);

	XmPaintFinder disableAo(int textureIndex, boolean disable);

	XmPaintFinder emissive(int textureIndex, boolean isEmissive);

	XmPaintFinder shader(Identifier shader);

	XmPaintFinder condition(Identifier condition);

	XmPaintFinder vertexProcessor(int textureIndex, VertexProcessor vp);
}
