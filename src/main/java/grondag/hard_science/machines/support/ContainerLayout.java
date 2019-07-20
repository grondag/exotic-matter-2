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
package grondag.hard_science.machines.support;

public class ContainerLayout {

    /**
     * Number of pixels between each slot corner for slots. MC default is 18, which
     * leaves 2px border between slots.
     */
    public int slotSpacing = 18;

    public int externalMargin = 6;

    public int expectedTextHeight = 12;

    public int playerInventoryWidth = slotSpacing * 8 + 16;

    public int dialogWidth = externalMargin * 2 + playerInventoryWidth;

    public int dialogHeight = externalMargin * 3 + slotSpacing * 10 + expectedTextHeight * 2;

    /** distance from edge of dialog to start of player inventory area */
    public int playerInventoryLeft = externalMargin;

    /** distance from top of dialog to start of player inventory area */
    public int playerInventoryTop = dialogHeight - externalMargin - slotSpacing * 4;

}
