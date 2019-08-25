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
package grondag.xm.api.mesh;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.Polygon;

/**
 * Stream that allows appending to end in wip area but is immutable for polygons
 * already created.
 */
@API(status = EXPERIMENTAL)
public interface WritableMesh extends XmMesh {

    /**
     * Current setting for packed normals. See {@link #setPackedNormals(boolean)}
     */
    default boolean usePackedNormals() {
        return false;
    }

    /**
     * Set to true to compress face and vertex normals.
     * <p>
     * 
     * Packed normals are less precise, so should only use for streams that will be
     * used for rendering and not CSG or other operations.
     * <p>
     * 
     * False by default. Not all stream writers support. If unsupported, stream will
     * simply ignore.
     */
    default void setPackedNormals(boolean usePacked) {
        // NOOP
    }

    /**
     * Holds WIP poly data that will be appended by next call to {@link #append()}.
     * Is reset to defaults when append is called.
     * <p>
     * 
     * DO NOT HOLD A NON-LOCAL REFERENCE TO THIS.
     */
    MutablePolygon writer();

    /**
     * Address that will be used for next appended polygon when append is
     * called.<br>
     * Cannot be used with move... methods until writer is appended.
     */
    int writerAddress();

    /**
     * Appends WIP as new poly and resets WIP to default values. Increases size of
     * stream by 1.
     * @deprecated Use append on poly instead.
     */
    //TODO: deprecated
    @Deprecated
    @API(status = Status.DEPRECATED)
    void append();

    /**
     * Current poly settings will be used to initialize WIP after append.
     */
    void saveDefaults();

    /**
     * Undoes effects of {@link #saveDefaults()} so that defaults are for a new poly
     * stream.
     */
    void clearDefaults();

    /**
     * Loads default values into WIP.
     */
    void loadDefaults();

    /**
     * Releases this stream and returns an immutable reader stream. The reader strip
     * will use non-pooled heap memory and thus should only be used for streams with
     * a significant lifetime to prevent needless garbage collection.
     * <p>
     * 
     * The reader stream will not include deleted polygons, and will only include
     * tag, link or bounds metadata if those flags are specified.
     */
    ReadOnlyMesh releaseToReader();

    /** Creates a read-only copy without releasing allocation */
    ReadOnlyMesh toReader();
    
    /**
     * Sets vertex count for current writer. Value can be saved as part of defaults.
     */
    void setVertexCount(int vertexCount);

    /**
     * Sets layer count for current writer. Value can be saved as part of defaults.
     */
    void setLayerCount(int layerCount);

    /**
     * Makes no change to writer state, except address.
     */
    void appendCopy(Polygon poly);

    default void appendAll(XmMesh stream) {
        final Polygon reader = stream.reader();
        if (reader.origin()) {
            do {
                assert !reader.isDeleted();
                this.appendCopy(reader);
            } while (reader.next());
        }
    }

    /**
     * If the poly at the given address is a tri or a convex quad, does nothing and
     * returns IStreamPolygon#NO_ADDRESS.
     * <p>
     * 
     * If the poly is a concave quad or higher-order polygon, appends new polys
     * split from this one at end of the stream, marks the poly at the given address
     * as deleted, and returns the address of the first split output.<p>
     * 
     * @deprecated Handled during render output - can only cause trouble before
     */
    //TODO: deprecated
    @Deprecated
    @API(status = Status.DEPRECATED)
    public int splitIfNeeded(int targetAddress);

    public void clear();
}
