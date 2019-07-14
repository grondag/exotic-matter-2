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

package grondag.xm2.mesh.stream;

import grondag.xm2.mesh.polygon.IMutablePolygon;

/**
 * Polygons in this stream can be edited after appending via an editor cursor.
 * To allow for editing, polygons in this type of stream consume more memory.
 * <p>
 * 
 * The number of layers can be changed after appending, but number of vertices
 * cannot.
 */
public interface IMutablePolyStream extends IWritablePolyStream {
    IMutablePolygon editor();

    /**
     * Moves editor to the first polygon. Returns true if moved editor has a value.
     * (Stream not empty and not all polys deleted.)
     */
    boolean editorOrigin();

    /**
     * True if moves editor to next non-deleted poly. False if at the end of the
     * stream.
     */
    boolean editorNext();

    /**
     * Moves editor to given address.
     */
    void moveEditor(int address);

    /**
     * Combo of {@link #moveEditor(int)} and {@link #editor()}.<br>
     * Moves editor to given address and returns the editor cursor for concision.
     */
    IMutablePolygon editor(int address);

    boolean editorHasValue();

    int editorAddress();
}
