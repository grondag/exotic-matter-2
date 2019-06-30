package grondag.xm2.primitives.stream;

import grondag.xm2.primitives.polygon.IMutablePolygon;

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
