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

import grondag.fermion.intstream.IntStreams;
import grondag.xm.mesh.polygon.IPolygon;

public class ReadOnlyPolyStream extends AbstractPolyStream implements IReadOnlyPolyStream {
    void load(WritablePolyStream streamIn, int formatFlags) {
        prepare(IntStreams.claim(streamIn.stream.capacity()));

        if (!streamIn.isEmpty()) {
            streamIn.origin();
            IPolygon reader = streamIn.reader();
            do
                this.appendCopy(reader, formatFlags);
            while (streamIn.next());
        }

        this.stream.compact();
    }

    @Override
    protected void doRelease() {
        super.doRelease();
    }

    @Override
    protected void returnToPool() {
        PolyStreams.release(this);
    }
}
