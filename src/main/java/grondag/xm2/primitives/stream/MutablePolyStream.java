package grondag.xm2.primitives.stream;

import grondag.xm2.primitives.polygon.IMutablePolygon;
import grondag.xm2.primitives.polygon.IPolygon;

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
