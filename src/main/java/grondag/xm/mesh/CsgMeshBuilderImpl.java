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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.xm.Xm;
import grondag.xm.api.mesh.Csg;
import grondag.xm.api.mesh.CsgMeshBuilder;
import grondag.xm.api.mesh.CsgMesh;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.XmMeshes;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

@API(status = Status.EXPERIMENTAL)
public class CsgMeshBuilderImpl implements CsgMeshBuilder {
    private static ThreadLocal<CsgMeshBuilderImpl> THREADLOCAL = ThreadLocal.withInitial(CsgMeshBuilderImpl::new);
    
    private static final int NO_OP = 0;
    private static final int UNION = 1;
    private static final int DIFFERENCE = 2;
    private static final int INTERSECT = 3;
    
    public static CsgMeshBuilder threadLocal() {
        return THREADLOCAL.get();
    }
    
    private final ObjectArrayList<CsgMesh> outputStack = new ObjectArrayList<>();
    private CsgMesh input = XmMeshes.claimCsg();
    private CsgMesh temp = XmMeshes.claimCsg();
    private CsgMesh output = XmMeshes.claimCsg();
    private boolean hasOutput = false;
    private int pendingOp = NO_OP;
    
    @Override
    public void push() {
        if(hasOutput) {
            applyPendingOp(temp);
            outputStack.push(output);
            output = XmMeshes.claimCsg();
            input.clear();
            temp.clear();
            hasOutput = false;
        } else {
            throw new UnsupportedOperationException("Csg push without output.");
        }
    }
    
    @Override
    public void pop() {
        if(outputStack.isEmpty()) {
            throw new IllegalStateException("Output stack is empty");
        }
        if (hasOutput) {
            applyPendingOp(temp);
            input.release();
            input = outputStack.pop();
        } else {
            output.release();
            output = outputStack.pop();
        }
    }
    
    @Override
    public boolean hasOutputStack() {
        return !outputStack.isEmpty();
    }
    
    @Override
    public CsgMesh input() {
        if (hasOutput) {
            applyPendingOp(temp);
            return input;
        } else {
            return output;
        }
    }
    
    @Override
    public XmMesh build() {
        if(!outputStack.isEmpty()) {
            Xm.LOG.warn("CsgMeshBuilder build with non-empty stack.  This is unexpected and probably incorrect usage.");
            while (!outputStack.isEmpty()) {
                outputStack.pop().release();
            }
        }
        
        WritableMesh target = XmMeshes.claimWritable();

        if(pendingOp == NO_OP) {
            output.outputRecombinedQuads(target);
        } else {
            applyPendingOp(target);
        }
        
        input.clear();
        temp.clear();
        output.clear();
        pendingOp = NO_OP;
        
        target.splitAsNeeded();
        XmMesh result = target.releaseToReader();
        hasOutput = false;
        return result;
    }
    
    /** must be the first operation */
    @Override
    public void union() {
        if(hasOutput) {
            pendingOp = UNION;
        } else {
            hasOutput = true;
        }
    }
    
    @Override
    public void intersect() {
        if(hasOutput) {
            pendingOp = INTERSECT;
        } else {
            throw new UnsupportedOperationException("First operation must be union.");
        }
    }
    
    @Override
    public void difference() {
        if(hasOutput) {
            pendingOp = DIFFERENCE;
        } else {
            throw new UnsupportedOperationException("First operation must be union.");
        }
    }
    
    private void applyPendingOp(WritableMesh target) {
        switch(pendingOp) {
            case UNION:
                Csg.union(input, output, target);
                break;
            case DIFFERENCE:
                Csg.difference(input, output, target);
                break;
            case INTERSECT:
                Csg.intersect(input, output, target);
                break;
            default:
            case NO_OP:
                return;
        }
        
        pendingOp = NO_OP;
        
        if(target == temp) {
            CsgMesh swap = output;
            output = temp;
            temp = swap;
            swap.clear();
            input.clear();
        }
    }
}
