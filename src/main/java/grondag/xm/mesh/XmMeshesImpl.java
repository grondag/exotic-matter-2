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
package grondag.xm.mesh;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import grondag.xm.api.mesh.CsgMesh;
import grondag.xm.api.mesh.MutableMesh;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.ReadOnlyMesh;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.Polygon;

public class XmMeshesImpl {
    static final int FORMAT_TAGS = MeshFormat.HAS_TAG_FLAG;
    static final int FORMAT_LINKS = MeshFormat.HAS_LINK_FLAG;

    private static final ArrayBlockingQueue<WritableMeshImpl> writables = new ArrayBlockingQueue<>(256);
    private static final ArrayBlockingQueue<MutableMeshImpl> mutables = new ArrayBlockingQueue<>(128);
    private static final ArrayBlockingQueue<CsgMeshhImpl> csgStreams = new ArrayBlockingQueue<>(128);
    private static final ArrayBlockingQueue<ReadOnlyMeshImpl> readables = new ArrayBlockingQueue<>(256);

    public static WritableMesh claimWritable() {
        return claimWritable(0);
    }

    static WritableMesh claimWritable(int formatFlags) {
        WritableMeshImpl result = writables.poll();
        if (result == null)
            result = new WritableMeshImpl();
        result.prepare(formatFlags);
        return result;
    }

    static void release(WritableMeshImpl freeStream) {
        writables.offer(freeStream);
    }

    public static MutableMesh claimMutable() {
        return claimMutable(0);
    }
    
    static MutableMesh claimMutable(int formatFlags) {
        MutableMeshImpl result = mutables.poll();
        if (result == null)
            result = new MutableMeshImpl();
        result.prepare(formatFlags);
        return result;
    }

    static void release(MutableMeshImpl freeStream) {
        mutables.offer(freeStream);
    }

    static ReadOnlyMesh claimReadOnly(WritableMeshImpl writablePolyStream, int formatFlags) {
        ReadOnlyMeshImpl result = readables.poll();
        if (result == null)
            result = new ReadOnlyMeshImpl();
        result.load(writablePolyStream, formatFlags);
        return result;
    }

    /**
     * Creates a stream with randomly recolored copies of the input stream.
     * <p>
     * 
     * Does not modify or release the input stream.
     */
    public static ReadOnlyMesh claimRecoloredCopy(XmMesh input) {
        WritableMesh result = claimWritable();
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

        return result.releaseToReader();
    }

    static void release(ReadOnlyMeshImpl freeStream) {
        readables.offer(freeStream);
    }

    public static CsgMeshhImpl claimCsg() {
        CsgMeshhImpl result = csgStreams.poll();
        if (result == null)
            result = new CsgMeshhImpl();
        result.prepare();
        return result;
    }

    public static CsgMesh claimCsg(XmMesh stream) {
        CsgMeshhImpl result = claimCsg();
        result.appendAll(stream);
        return result;
    }

    static void release(CsgMeshhImpl freeStream) {
        csgStreams.offer(freeStream);
    }
}
