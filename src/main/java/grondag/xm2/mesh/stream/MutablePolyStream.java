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
import grondag.xm2.mesh.polygon.IPolygon;

public class MutablePolyStream extends WritablePolyStream implements IMutablePolyStream {
    protected final StreamBackedMutablePolygon editor = new StreamBackedMutablePolygon();

    @Override
    protected void prepare(int formatFlags) {
        super.prepare(formatFlags);
        editor.stream = stream;
    }

    @Override
    public void clear() {
        super.clear();
        editor.invalidate();
    }

    @Override
    protected void doRelease() {
        super.doRelease();
        editor.stream = null;
    }

    @Override
    protected void returnToPool() {
        PolyStreams.release(this);
    }

    @Override
    public IMutablePolygon editor() {
        return editor;
    }

    @Override
    public boolean editorOrigin() {
        if (isEmpty()) {
            editor.invalidate();
            return false;
        } else {
            editor.moveTo(originAddress);
            if (editor.isDeleted())
                editorNext();
            return editorHasValue();
        }
    }

    @Override
    public boolean editorNext() {
        return moveReaderToNext(this.editor);
    }

    @Override
    public boolean editorHasValue() {
        return isValidAddress(editor.baseAddress) && !editor.isDeleted();
    }

    @Override
    public void moveEditor(int address) {
        validateAddress(address);
        editor.moveTo(address);
    }

    @Override
    public IMutablePolygon editor(int address) {
        moveEditor(address);
        return editor;
    }

    @Override
    public int editorAddress() {
        return editor.baseAddress;
    }

    @Override
    protected void appendCopy(IPolygon polyIn, int withFormat) {
        final boolean needReaderLoad = reader.baseAddress == writeAddress;

        // formatFlags for writer poly should already include mutable
        assert PolyStreamFormat.isMutable(withFormat);

        int newFormat = PolyStreamFormat.setLayerCount(withFormat, polyIn.layerCount());
        newFormat = PolyStreamFormat.setVertexCount(newFormat, polyIn.vertexCount());
        stream.set(writeAddress, newFormat);
        internal.moveTo(writeAddress);
        internal.copyFrom(polyIn, true);
        writeAddress += PolyStreamFormat.polyStride(newFormat, true);

        if (needReaderLoad)
            reader.loadFormat();
    }
}
