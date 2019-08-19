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

import grondag.xm.mesh.XmMeshesImpl;

@API(status = EXPERIMENTAL)
public class XmMeshes {
    private XmMeshes() {}
    
    public static WritableMesh claimWritable() {
        return XmMeshesImpl.claimWritable();
    }

    public static MutableMesh claimMutable() {
        return XmMeshesImpl.claimMutable();
    }

    /**
     * Creates a mesh with randomly recolored copies of the input mesh polygons.<p>
     * 
     * Does not modify or release the input mesh.
     */
    public static ReadOnlyMesh claimRecoloredCopy(XmMesh mesh) {
        return XmMeshesImpl.claimRecoloredCopy(mesh);
    }

    public static CsgMesh claimCsg() {
        return XmMeshesImpl.claimCsg();
    }

    public static CsgMesh claimCsg(XmMesh mesh) {
        return XmMeshesImpl.claimCsg(mesh);
    }

}
