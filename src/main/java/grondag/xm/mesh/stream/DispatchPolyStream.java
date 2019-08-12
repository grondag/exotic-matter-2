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

package grondag.xm.mesh.stream;

import java.util.function.Consumer;

import grondag.fermion.intstream.IntStreams;
import grondag.xm.dispatch.QuadListKeyBuilder;
import grondag.xm.mesh.polygon.Polygon;
import grondag.xm.mesh.polygon.StreamReaderPolygon;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.util.math.Direction;

//TODO: can probably remove this whole thing
/**
 * Read-only stream that builds linked lists of quads based on actual face. Used
 * for space-efficient block render dispatch.
 * <p>
 *
 * Will have a format with links no matter how claimed and will ignore any links
 * in the stream used to create it.
 */
public class DispatchPolyStream extends AbstractPolyStream implements ReadOnlyPolyStream, Consumer<Polygon> {
    protected boolean isBuilt = false;
    protected int lastAppendAddress = Polygon.NO_LINK_OR_TAG;

    public final int getOcclusionHash(Direction face) {
        assert isBuilt;

        if (face == null)
            return 0;

        int result = stream.get(face.ordinal());
        if (result == -1) {
            result = computeOcclusionHash(face);
            stream.set(face.ordinal(), result);
        }
        return result;
    }

    private int computeOcclusionHash(Direction face) {
        assert isBuilt;

        int address = firstAddress(BlockRenderLayer.SOLID, face);
        if (address == Polygon.NO_LINK_OR_TAG)
            return 0;

        QuadListKeyBuilder keyBuilder = QuadListKeyBuilder.prepareThreadLocal(face);
        StreamReaderPolygon r = claimThreadSafeReader();

        while (address != Polygon.NO_LINK_OR_TAG) {
            r.moveTo(address);
            keyBuilder.accept(r);
            address = r.getLink();
        }

        r.release();
        return keyBuilder.getQuadListKey();
    }

    private int listHeadAddress(BlockRenderLayer layer, Direction actualFace) {
        int faceIndex = actualFace == null ? 6 : actualFace.ordinal();
        return writeAddress + layer.ordinal() * 7 + faceIndex;
    }

    /**
     * Returns IPolygon#NO_LINK_OR_TAG if no polys with given layer and actual face
     * are present.
     */
    public final int firstAddress(BlockRenderLayer layer, Direction actualFace) {
        assert isBuilt;
        return stream.get(listHeadAddress(layer, actualFace));
    }

    /**
     * First six ints store occlusion hashes once computed.
     */
    @Override
    protected int newOrigin() {
        return 6;
    }

    void prepare() {
        super.prepare(IntStreams.claim());

        // FIXME: needs to happen during clear, or maybe no longer needed - perhaps
        // store in mesh or via tags
        isBuilt = false;
        lastAppendAddress = Polygon.NO_LINK_OR_TAG;

        // clear occlusion hash data
        stream.set(0, -1);
        stream.set(1, -1);
        stream.set(2, -1);
        stream.set(3, -1);
        stream.set(4, -1);
        stream.set(5, -1);
    }

    @Override
    public void accept(Polygon p) {
        assert !isBuilt;

        if (p.isDeleted())
            return;

        // As polygons are accepted, link them back to previous.
        // This will let us traverse the list in reverse order,
        // which in turns means we can build linked lists by
        // inserting at the head of list without additional metadata
        // and still maintain original arrival order in the final linked list.
        int appendAddress = this.writeAddress;
        this.appendCopy(p, PolyStreamFormat.HAS_LINK_FLAG);
        setLink(appendAddress, lastAppendAddress);
        lastAppendAddress = appendAddress;
    }

    /**
     * Call after all polys added to set up lists and reduce memory use.
     */
    public void build() {
        // populate list starts with value indicating empty list
        final int limit = writeAddress + 28;
        for (int i = writeAddress; i < limit; i++)
            stream.set(i, Polygon.NO_LINK_OR_TAG);

        int currentAddress = lastAppendAddress;
        final StreamBackedPolygon p = this.internal;

        while (currentAddress != Polygon.NO_LINK_OR_TAG) {
            p.moveTo(currentAddress);

            // save our next address before we overwrite it
            final int nextAddress = p.getLink();

            // figure out what list it belongs in
            final int listAddress = listHeadAddress(p.blendMode(0), p.cullFace());

            // find current first address in the list, which could be "none"
            final int currentHead = stream.get(listAddress);

            // link the current poly to the current head of the list
            p.setLink(currentHead);

            // make the current poly the head of the list
            stream.set(listAddress, currentAddress);

            // move (backwards) in the stream
            currentAddress = nextAddress;
        }

        p.invalidate();

        // housekeeping
        stream.compact();
        this.isBuilt = true;
    }

    @Override
    protected void doRelease() {
        super.doRelease();
    }

    @Override
    protected void returnToPool() {
        PolyStreams.release(this);
    }

    @Override
    public final StreamReaderPolygon claimThreadSafeReader() {
        assert isBuilt;
        return super.claimThreadSafeReaderImpl();
    }

}
