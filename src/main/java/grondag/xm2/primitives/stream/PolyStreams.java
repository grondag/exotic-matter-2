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

package grondag.xm2.primitives.stream;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import grondag.xm2.primitives.polygon.IMutablePolygon;
import grondag.xm2.primitives.polygon.IPolygon;

public class PolyStreams {
    public static final int FORMAT_TAGS = PolyStreamFormat.HAS_TAG_FLAG;
    public static final int FORMAT_LINKS = PolyStreamFormat.HAS_LINK_FLAG;

    private static final ArrayBlockingQueue<WritablePolyStream> writables = new ArrayBlockingQueue<>(256);
    private static final ArrayBlockingQueue<MutablePolyStream> mutables = new ArrayBlockingQueue<>(128);
    private static final ArrayBlockingQueue<CsgPolyStream> csgStreams = new ArrayBlockingQueue<>(128);
    private static final ArrayBlockingQueue<ReadOnlyPolyStream> readables = new ArrayBlockingQueue<>(256);
    private static final ArrayBlockingQueue<DispatchPolyStream> dispatches = new ArrayBlockingQueue<>(256);

    public static IWritablePolyStream claimWritable() {
        return claimWritable(0);
    }

    public static IWritablePolyStream claimWritable(int formatFlags) {
        WritablePolyStream result = writables.poll();
        if (result == null)
            result = new WritablePolyStream();
        result.prepare(formatFlags);
        return result;
    }

    static void release(WritablePolyStream freeStream) {
        writables.offer(freeStream);
    }

    public static IMutablePolyStream claimMutable(int formatFlags) {
        MutablePolyStream result = mutables.poll();
        if (result == null)
            result = new MutablePolyStream();
        result.prepare(formatFlags);
        return result;
    }

    static void release(MutablePolyStream freeStream) {
        mutables.offer(freeStream);
    }

    public static IReadOnlyPolyStream claimReadOnly(WritablePolyStream writablePolyStream, int formatFlags) {
        ReadOnlyPolyStream result = readables.poll();
        if (result == null)
            result = new ReadOnlyPolyStream();
        result.load(writablePolyStream, formatFlags);
        return result;
    }

    /**
     * Creates a stream with randomly recolored copies of the input stream.
     * <p>
     * 
     * Does not modify or release the input stream.
     */
    public static IReadOnlyPolyStream claimRecoloredCopy(IPolyStream input) {
        IWritablePolyStream result = claimWritable();
        if (input.origin()) {
            Random r = ThreadLocalRandom.current();

            IPolygon reader = input.reader();
            IMutablePolygon writer = result.writer();
            do {
                result.setVertexCount(reader.vertexCount());
                result.setLayerCount(reader.layerCount());
                writer.copyFrom(reader, true);
                writer.spriteColorAll(0, (r.nextInt(0x1000000) & 0xFFFFFF) | 0xFF000000);
                result.append();
            } while (input.next());
        }

        return result.releaseAndConvertToReader();
    }

    static void release(ReadOnlyPolyStream freeStream) {
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

    public static CsgPolyStream claimCSG(IPolyStream stream) {
        CsgPolyStream result = claimCSG();
        result.appendAll(stream);
        return result;
    }

    static void release(CsgPolyStream freeStream) {
        csgStreams.offer(freeStream);
    }
}
