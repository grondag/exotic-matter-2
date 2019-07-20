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
package grondag.hard_science.gui.control;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VisibilityPanel extends Panel {

    private final ArrayList<ArrayList<GuiControl<?>>> groups = new ArrayList<ArrayList<GuiControl<?>>>();

    private final ArrayList<String> labels = new ArrayList<String>();

    private int visiblityIndex = VisiblitySelector.NO_SELECTION;

    public VisibilityPanel(boolean isVertical) {
        super(isVertical);
    }

    public int getVisiblityIndex() {
        return visiblityIndex;
    }

    public void setVisiblityIndex(int visiblityIndex) {
        this.visiblityIndex = visiblityIndex;
        this.children = groups.get(visiblityIndex);
        this.isDirty = true;
        this.refreshContentCoordinatesIfNeeded();
    }

    /**
     * Creates a new visibility group with the given caption and returns its index.
     * Must call this before adding controls using the index.
     */
    public int createVisiblityGroup(String label) {
        this.labels.add(label);
        this.groups.add(new ArrayList<GuiControl<?>>());
        return this.labels.size() - 1;
    }

    public VisibilityPanel addAll(int visibilityIndex, GuiControl<?>... controls) {
        this.groups.get(visibilityIndex).addAll(Arrays.asList(controls));
        this.isDirty = true;
        return this;
    }

    public VisibilityPanel add(int visibilityIndex, GuiControl<?> control) {
        this.groups.get(visibilityIndex).add(control);
        this.isDirty = true;
        return this;
    }

    public VisibilityPanel remove(int visibilityIndex, int controlindex) {
        this.groups.get(visibilityIndex).remove(controlindex);
        this.isDirty = true;
        return this;
    }

    public String getLabel(int visiblityIndex) {
        return labels.get(visiblityIndex);
    }

    public int size() {
        return labels.size();
    }
}
