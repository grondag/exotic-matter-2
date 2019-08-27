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

import grondag.xm.Xm;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

@API(status = Status.EXPERIMENTAL)
public class CsgHelper {
    private static ThreadLocal<CsgHelper> THREADLOCAL = ThreadLocal.withInitial(CsgHelper::new);
    
    public static CsgHelper threadLocal() {
        return THREADLOCAL.get();
    }
    
    private final ObjectArrayList<CsgMesh> outputStack = new ObjectArrayList<>();
    private CsgMesh input = XmMeshes.claimCsg();
    private CsgMesh temp = XmMeshes.claimCsg();
    private CsgMesh output = XmMeshes.claimCsg();
    private boolean hasOutput = false;
    
    public void push() {
        if(hasOutput) {
            outputStack.push(output);
            output = XmMeshes.claimCsg();
            input.clear();
            temp.clear();
            hasOutput = false;
        } else {
            throw new UnsupportedOperationException("Csg push without output.");
        }
    }
    
    public void pop() {
        if(outputStack.isEmpty()) {
            throw new IllegalStateException("Output stack is empty");
        }
        if(hasOutput) {
            input.release();
            input = outputStack.pop();
        } else {
            output.release();
            output = outputStack.pop();
        }
    }
    
    public boolean hasOutputStack() {
        return !outputStack.isEmpty();
    }
    
    public CsgMesh input() {
        return hasOutput ? input : output;
    }
    
    public XmMesh clearToReader() {
        if(!outputStack.isEmpty()) {
            Xm.LOG.warn("CsgHelper clear with non-empty stack.  This is unexpected and probably incorrect usage.");
            while (!outputStack.isEmpty()) {
                outputStack.pop().release();
            }
        }
        input.clear();
        temp.clear();
        
        // TODO: make config option?
//        final Random r = ThreadLocalRandom.current();
//        MutablePolygon editor = output.editor();
//        if(editor.origin()) {
//            do {
//                editor.colorAll(0, (r.nextInt(0x1000000) & 0xFFFFFF) | 0xFF000000);
//            } while (editor.next());
//        }
        
        output.splitAsNeeded();
        XmMesh result = output.toReader();
        output.clear();
        hasOutput = false;
        return result;
    }
    
    /** must be the first operation */
    public void union() {
        if(hasOutput) {
            CSG.union(input, output, temp);
            postOp();
        } else {
            hasOutput = true;
        }
    }
    
    public void intersect() {
        if(hasOutput) {
            CSG.intersect(input, output, temp);
            postOp();
        } else {
            throw new UnsupportedOperationException("First operation must be union.");
        }
    }
    
    public void difference() {
        if(hasOutput) {
            CSG.difference(input, output, temp);
            postOp();
        } else {
            throw new UnsupportedOperationException("First operation must be union.");
        }
    }
    
    private void postOp() {
        CsgMesh swap = output;
        output = temp;
        temp = swap;
        swap.clear();
        input.clear();
    }
}
