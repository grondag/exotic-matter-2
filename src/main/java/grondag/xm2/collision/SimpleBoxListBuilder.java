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

package grondag.xm2.collision;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Makes no attempt to combine boxes.<br>
 * Used when boxes are already known to be optimal.
 */
public class SimpleBoxListBuilder implements ICollisionBoxListBuilder {
    final IntArrayList boxes = new IntArrayList();

    @Override
    public void clear() {
        boxes.clear();
    }

    @Override
    public IntCollection boxes() {
        return boxes;
    }

    @Override
    public void add(int boxKey) {
        boxes.add(boxKey);
    }
}
