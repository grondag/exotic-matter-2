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
package grondag.hard_science.gui.shape;

import grondag.exotic_matter.init.ModShapes;
import grondag.exotic_matter.model.mesh.ModelShape;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiShapeFinder {
    public static GuiShape findGuiForShape(ModelShape<?> shape, Minecraft mc) {
        if (shape == ModShapes.COLUMN_SQUARE)
            return new GuiSquareColumn(mc);
        else if (shape == ModShapes.STACKED_PLATES)
            return new GuiStackedPlates(mc);
        else
            return new GuiSimpleShape(true);
    }
}
