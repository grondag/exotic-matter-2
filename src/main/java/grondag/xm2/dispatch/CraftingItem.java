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

package grondag.xm2.dispatch;

import grondag.fermion.color.BlockColorMapProvider;
import grondag.fermion.color.ColorMap.EnumColorMap;
import grondag.xm2.api.paint.XmPaint;
import grondag.xm2.init.XmTextures;
import grondag.xm2.api.model.MutableModelState;
import net.minecraft.item.Item;

/**
 * Generic item class with a SuperModel render and state. Be sure to set
 * creative tab for mod that uses it.
 */
public class CraftingItem extends Item {
    public final MutableModelState modelState;

    // TODO: pretty sure this doesn't work after big refactor - is even stil needed?
    public CraftingItem(Settings settings, MutableModelState modelState) {
	super(settings);
	this.modelState = modelState;
	final int colorIndex = this.hashCode() % BlockColorMapProvider.INSTANCE.getColorMapCount();
	XmPaint paint = XmPaint.finder().texture(0, XmTextures.WHITE)
		.textureColor(0, BlockColorMapProvider.INSTANCE.getColorMap(colorIndex).getColor(EnumColorMap.BASE))
		.find();
	this.modelState.paintAll(paint);
    }
}
