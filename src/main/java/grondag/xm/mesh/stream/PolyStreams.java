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

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import grondag.xm.mesh.polygon.MutablePolygon;
import grondag.xm.mesh.polygon.Polygon;

public class PolyStreams {
    public static final int FORMAT_TAGS = PolyStreamFormat.HAS_TAG_FLAG;
    public static final int FORMAT_LINKS = PolyStreamFormat.HAS_LINK_FLAG;

    private static final ArrayBlockingQueue<WritablePolyStreamImpl> writables = new ArrayBlockingQueue<>(256);
    private static final ArrayBlockingQueue<MutablePolyStreamImpl> mutables = new ArrayBlockingQueue<>(128);
    private static final ArrayBlockingQueue<CsgPolyStream> csgStreams = new ArrayBlockingQueue<>(128);
    private static final ArrayBlockingQueue<ReadOnlyPolyStreamImpl> readables = new ArrayBlockingQueue<>(256);
    private static final ArrayBlockingQueue<DispatchPolyStream> dispatches = new ArrayBlockingQueue<>(256);

    public static WritablePolyStream claimWritable() {
        return claimWritable(0);
    }

    public static WritablePolyStream claimWritable(int formatFlags) {
        WritablePolyStreamImpl result = writables.poll();
        if (result == null)
            result = new WritablePolyStreamImpl();
        result.prepare(formatFlags);
        return result;
    }

    static void release(WritablePolyStreamImpl freeStream) {
        writables.offer(freeStream);
    }

    public static MutablePolyStream claimMutable(int formatFlags) {
        MutablePolyStreamImpl result = mutables.poll();
        if (result == null)
            result = new MutablePolyStreamImpl();
        result.prepare(formatFlags);
        return result;
    }

    static void release(MutablePolyStreamImpl freeStream) {
        mutables.offer(freeStream);
    }

    public static ReadOnlyPolyStream claimReadOnly(WritablePolyStreamImpl writablePolyStream, int formatFlags) {
        ReadOnlyPolyStreamImpl result = readables.poll();
        if (result == null)
            result = new ReadOnlyPolyStreamImpl();
        result.load(writablePolyStream, formatFlags);
        return result;
    }

    /**
     * Creates a stream with randomly recolored copies of the input stream.
     * <p>
     * 
     * Does not modify or release the input stream.
     */
    public static ReadOnlyPolyStream claimRecoloredCopy(PolyStream input) {
        WritablePolyStream result = claimWritable();
        if (input.origin()) {
            Random r = ThreadLocalRandom.current();

            Polygon reader = input.reader();
            MutablePolygon writer = result.writer();
            do {
                result.setVertexCount(reader.vertexCount());
                result.setLayerCount(reader.spriteDepth());
                writer.copyFrom(reader, true);
                writer.colorAll(0, (r.nextInt(0x1000000) & 0xFFFFFF) | 0xFF000000);
                result.append();
            } while (input.next());
        }

        return result.releaseAndConvertToReader();
    }

    static void release(ReadOnlyPolyStreamImpl freeStream) {
        readables.offer(freeStream);
    }

    public static DispatchPolyStream claimDispatch() {
        DispatchPolyStream result = dispatches.poll();
        if (result == null)
            result = new DispatchPolyStream();
        result.prepare();
        return result;
    }

    static void release(DispatchPolyStream freeStream) {
        dispatches.offer(freeStream);
    }

    public static CsgPolyStream claimCSG() {
        CsgPolyStream result = csgStreams.poll();
        if (result == null)
            result = new CsgPolyStream();
        result.prepare();
        return result;
    }

    public static CsgPolyStream claimCSG(PolyStream stream) {
        CsgPolyStream result = claimCSG();
        result.appendAll(stream);
        return result;
    }

    static void release(CsgPolyStream freeStream) {
        csgStreams.offer(freeStream);
    }
}
