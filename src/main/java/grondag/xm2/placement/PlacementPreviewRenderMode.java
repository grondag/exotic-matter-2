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

package grondag.xm2.placement;

public enum PlacementPreviewRenderMode {
    SELECT(0x91BFBD), PLACE(0xA0FFFF), EXCAVATE(0xFC8D59), OBSTRUCTED(0xFFFFBF);

    public final float red;
    public final float green;
    public final float blue;

    private PlacementPreviewRenderMode(int rgbColor) {
        this.red = ((rgbColor >> 16) & 0xFF) / 255f;
        this.green = ((rgbColor >> 8) & 0xFF) / 255f;
        this.blue = (rgbColor & 0xFF) / 255f;
    }
}
