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

package grondag.xm2.primitives.polygon;

/**
 * Subset of IPolygon that applies only to polys that are part of a polystream.
 * These operations all involve metadata, and therefore
 */
public interface IStreamPolygon {
    /**
     * Address of this poly within its stream.<br>
     * Will throw exception if not a stream-back poly.
     */
    public default int streamAddress() {
        throw new UnsupportedOperationException();
    }

    // TODO: remove
    @Deprecated
    default boolean isMarked() {
        return false;
    }

    // TODO: remove
    @Deprecated
    default void flipMark() {
        this.setMark(!this.isMarked());
    }

    // TODO: remove
    @Deprecated
    default void setMark(boolean isMarked) {
        throw new UnsupportedOperationException();
    }

    default boolean isDeleted() {
        return false;
    }

    default void setDeleted() {
        throw new UnsupportedOperationException();
    }

    /**
     * Improbable non-zero value that signifies no link set or link not supported.
     */
    public static final int NO_LINK_OR_TAG = Integer.MIN_VALUE;

    default int getLink() {
        return NO_LINK_OR_TAG;
    }

    default void setLink(int link) {
        throw new UnsupportedOperationException();
    }
}
