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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.xm.mesh.CsgMeshBuilderImpl;

@API(status = Status.EXPERIMENTAL)
public interface CsgMeshBuilder {

    void push();

    void pop();

    boolean hasOutputStack();

    CsgMesh input();

    XmMesh build();

    /** must be the first operation */
    void union();

    void intersect();

    void difference();

    static CsgMeshBuilder threadLocal() {
        return CsgMeshBuilderImpl.threadLocal();
    }
}