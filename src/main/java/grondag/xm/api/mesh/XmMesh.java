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

import java.util.function.Consumer;

import grondag.xm.api.mesh.polygon.Polygon;

public interface XmMesh {
    boolean isEmpty();

    /**
     * Reference to poly at current read address.<br>
     * When stream first created will point to the first poly in the stream.<br>
     * Returns null if at end or first poly has not been written.<br>
     * 
     * THIS READ IS NOT THREAD-SAFE and should only be used the allocating thread.
     */
    Polygon reader();

    /**
     * Moves reader and returns it.
     */
    Polygon reader(int address);

    /**
     * Claims a reader appropriate for concurrent access. Will fail for streams that
     * are mutable.
     * <p>
     * 
     * Reader must be released after use or stream can never be recycled to the pool
     * when released.
     */
    default MeshReaderPolygon claimThreadSafeReader() {
        throw new UnsupportedOperationException();
    }

    /**
     * Moves reader to start of stream.<br>
     * Note that WIP is not considered part of stream.
     * <p>
     * 
     * Returns true if reader has a value, meaning stream not empty & not all polys
     * are deleted.
     */
    boolean origin();

    /**
     * Moves reader to next poly in this stream. Returns false if already at
     * end.<br>
     * Note that WIP is not considered part of stream.
     */
    boolean next();

    /**
     * False if reader at end of stream in build order. <br>
     * By extension, also false if stream is empty.<br>
     * Note that WIP is not considered part of stream.
     */
    boolean hasValue();

    /**
     * Address of current reader location. Can be used with {@link #moveTo(int)} to
     * return. Addresses are immutable within this stream instance.
     * <p>
     */
    int getAddress();

    /**
     * Moves reader to location specified by address. Address should be from
     * {@link #getAddress()}.
     * <p>
     * 
     * Will throw exception if address points to WIP on streams still being written.
     */
    void moveTo(int address);

    void release();

    /**
     * Virtual read-only reference to an existing poly in this stream. Use for
     * interpolation and other poly-poly operations. Does not affect and not
     * affected by read/write/update cursors.
     * <p>
     * 
     * DO NOT STORE A REFERENCE. Meant only for use in a local operation. Calls to
     * {@link #movePolyA(int)} will produce new values.
     */
    Polygon polyA();

    /**
     * Sets address for {@link #polyA()} and returns same as convenience.
     */
    void movePolyA(int address);

    /**
     * Sets address for {@link #polyA()} and returns same as convenience.
     */
    Polygon polyA(int address);

    /**
     * Secondary instance of {@link #polyA()}. For interpolation.
     */
    Polygon polyB();

    /**
     * Secondary instance of {@link #movePolyA(int)}. For interpolation.
     */
    void movePolyB(int address);

    /**
     * Secondary instance of {@link #polyA(int)}. For interpolation.
     */
    Polygon polyB(int address);

    /**
     * Mark this poly or clear the mark for the poly at the read cursor. Meaning of
     * mark is up to user.
     * <p>
     * 
     * This, along with {@link #setLink(int)} are the only mutating options allowed
     * on a stream reader.
     * <p>
     * 
     * Not meant for concurrent use.
     */
    default void setTag(int tag) {
        throw new UnsupportedOperationException();
    }

    /**
     * Absolute address version of {@link #setMark(boolean)}
     */
    default void setTag(int address, int tag) {
        throw new UnsupportedOperationException();
    }

    default int getTag() {
        return Polygon.NO_LINK_OR_TAG;
    }

    default int getTag(int address) {
        return Polygon.NO_LINK_OR_TAG;
    }

    /**
     * True if poly at reader position is deleted.
     */
    default boolean isDeleted() {
        return false;
    }

    /**
     * Marks the poly at reader position for deletion.<br>
     * See {@link #setDeleted(int)}
     */
    default void setDeleted() {
        throw new UnsupportedOperationException();
    }

    /**
     * True if poly at given address is deleted.
     */
    default boolean isDeleted(int address) {
        return false;
    }

    /**
     * Marks poly at given address as deleted. Poly remains in collection but will
     * be skipped in next... methods. Streams that implement this will typically
     * also have IDeletable polygons that will reflect this change.
     */
    default void setDeleted(int address) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns link value for poly at given address.
     */
    default int getLink(int address) {
        return Polygon.NO_LINK_OR_TAG;
    }

    /**
     * Sets link for this poly at the current reader. Used to create sublists within
     * a stream.
     * <p>
     * 
     * This, along with {@link #setMark(boolean)} are the only mutating options
     * allowed on a stream reader.
     * <p>
     * 
     * Not meant for concurrent use.
     */
    default void setLink(int linkAddress) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets link for the poly at the target address. See {@link #setLink(int)}
     */
    default void setLink(int targetAddress, int linkAddress) {
        throw new UnsupportedOperationException();
    }

    /**
     * True if reader at end of link chain or not in a link chain.
     */
    default boolean hasLink() {
        return false;
    }

    /**
     * Clears any link set by {@link #setLink(int)}
     */
    default void clearLink() {
        throw new UnsupportedOperationException();
    }

    /**
     * Moves reader to next linked poly, if a link exists. Returns false if already
     * at end of link chain.
     */
    default boolean nextLink() {
        throw new UnsupportedOperationException();
    }

    /**
     * Mark this poly or clear the mark for the poly at the read cursor. Meaning of
     * mark is up to user.
     * <p>
     * 
     * This, along with {@link #setLink(int)} are the only mutating options allowed
     * on a stream reader.
     * <p>
     * 
     * Not meant for concurrent use.
     */
    default void setMark(boolean isMarked) {
        throw new UnsupportedOperationException();
    }

    /**
     * Absolute address version of {@link #setMark(boolean)}
     */
    default void setMark(int address, boolean isMarked) {
        throw new UnsupportedOperationException();
    }

    default boolean isMarked() {
        return false;
    }

    default boolean isMarked(int address) {
        return false;
    }

    default void flipMark() {
        setMark(!isMarked());
    }

    default void flipMark(int address) {
        setMark(!isMarked(address));
    }
    
    /** Moves reader! */
    default void forEach(Consumer<Polygon> target) {
        if (origin()) {
            Polygon reader = reader();

            do
                target.accept(reader);
            while (next());
        }
    }
    
    static XmMesh EMPTY = XmMeshes.claimWritable().releaseToReader();
}
