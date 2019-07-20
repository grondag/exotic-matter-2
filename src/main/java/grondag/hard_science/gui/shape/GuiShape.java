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

import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.hard_science.gui.control.Panel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiShape extends Panel {

    public GuiShape(boolean isVertical) {
        super(isVertical);
    }

    /** called before control is displayed and whenever modelstate changes */
    public abstract void loadSettings(ISuperModelState modelState);

    /** called to detect user changes - return true if model state was changed */
    public abstract boolean saveSettings(ISuperModelState modelState);

}
