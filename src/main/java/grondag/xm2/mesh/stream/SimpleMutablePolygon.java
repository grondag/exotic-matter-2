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

package grondag.xm2.mesh.stream;

import static grondag.xm2.mesh.stream.PolyStreamFormat.MUTABLE_FLAG;
import static grondag.xm2.mesh.stream.PolyStreamFormat.isMutable;

import grondag.fermion.intstream.IntStreams;

public class SimpleMutablePolygon extends StreamBackedMutablePolygon {
    public SimpleMutablePolygon() {
	this(1, 4);
    }

    public SimpleMutablePolygon(int layerCount, int vertexCount) {
	stream = IntStreams.claim();
	baseAddress = 0;
	prepare(layerCount, vertexCount);
    }

    public void prepare(int layerCount, int vertexCount) {
	stream.clear();
	int format = PolyStreamFormat.setLayerCount(MUTABLE_FLAG, layerCount);
	assert isMutable(format);
	format = PolyStreamFormat.setVertexCount(format, vertexCount);
	this.setFormat(format);
	loadStandardDefaults();
    }

    @Override
    public void release() {
	super.release();
	stream.release();
	stream = null;
    }
}
