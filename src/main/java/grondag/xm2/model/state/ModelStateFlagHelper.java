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

package grondag.xm2.model.state;

import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_DISABLE_BLOCK_ONLY;
import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_HAS_SOLID_RENDER;
import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_HAS_TRANSLUCENT_GEOMETRY;
import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_HAS_TRANSLUCENT_RENDER;
import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_IS_POPULATED;

import grondag.xm2.api.model.ModelPrimitive;
import grondag.xm2.api.paint.XmPaint;
import grondag.xm2.model.primitive.AbstractModelPrimitive;
import net.minecraft.block.BlockRenderLayer;

/**
 * Populates state flags for a given model state.
 * 
 * Results are returns as STATE_FLAG_XXXX values from ModelState for easy
 * persistence and usage within that class.
 */
public class ModelStateFlagHelper {
    public static final int getFlags(ModelState state) {
	final ModelPrimitive mesh = state.getShape();

	int flags = STATE_FLAG_IS_POPULATED | mesh.stateFlags(state);

	final int surfCount = mesh.surfaces().size();
	for (int i = 0; i < surfCount; i++) {
	    XmPaint p = state.paint(i);
	    final int texDepth = p.textureDepth();

	    for (int j = 0; j < texDepth; j++) {
		if (p.blendMode(j) == BlockRenderLayer.SOLID) {
		    flags |= STATE_FLAG_HAS_SOLID_RENDER;
		} else {
		    flags |= STATE_FLAG_HAS_TRANSLUCENT_RENDER;
		    if (j == 0) {
			flags |= STATE_FLAG_HAS_TRANSLUCENT_GEOMETRY;
		    }
		}

		flags |= p.texture(j).stateFlags();
	    }
	}

	// turn off this.stateFlags that don't apply to non-block formats if we aren't
	// one
	if (((AbstractModelPrimitive) mesh).stateFormat != StateFormat.BLOCK)
	    flags &= STATE_FLAG_DISABLE_BLOCK_ONLY;

	return flags;
    }
}
