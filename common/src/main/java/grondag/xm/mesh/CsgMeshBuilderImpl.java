/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.xm.mesh;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.xm.Xm;
import grondag.xm.api.mesh.Csg;
import grondag.xm.api.mesh.CsgMesh;
import grondag.xm.api.mesh.CsgMeshBuilder;
import grondag.xm.api.mesh.MutableMesh;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.XmMeshes;

@Experimental
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
		if (hasOutput) {
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
		if (outputStack.isEmpty()) {
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
		return buildMutable().releaseToReader();
	}

	@Override
	public MutableMesh buildMutable() {
		if (!outputStack.isEmpty()) {
			Xm.LOG.warn("CsgMeshBuilder build with non-empty stack.  This is unexpected and probably incorrect usage.");

			while (!outputStack.isEmpty()) {
				outputStack.pop().release();
			}
		}

		final MutableMesh target = XmMeshes.claimMutable();

		if (pendingOp == NO_OP) {
			output.outputRecombinedQuads(target);
		} else {
			applyPendingOp(target);
		}

		input.clear();
		temp.clear();
		output.clear();
		pendingOp = NO_OP;

		target.splitAsNeeded();
		hasOutput = false;
		return target;
	}

	/** Must be the first operation. */
	@Override
	public void union() {
		if (hasOutput) {
			pendingOp = UNION;
		} else {
			hasOutput = true;
		}
	}

	@Override
	public void intersect() {
		if (hasOutput) {
			pendingOp = INTERSECT;
		} else {
			throw new UnsupportedOperationException("First operation must be union.");
		}
	}

	@Override
	public void difference() {
		if (hasOutput) {
			pendingOp = DIFFERENCE;
		} else {
			throw new UnsupportedOperationException("First operation must be union.");
		}
	}

	private void applyPendingOp(WritableMesh target) {
		switch (pendingOp) {
			case UNION:
				Csg.union(output, input, target);
				break;
			case DIFFERENCE:
				Csg.difference(output, input, target);
				break;
			case INTERSECT:
				Csg.intersect(output, input, target);
				break;
			case NO_OP:
			default:
				return;
		}

		pendingOp = NO_OP;

		if (target == temp) {
			final CsgMesh swap = output;
			output = temp;
			temp = swap;
			swap.clear();
			input.clear();
		}
	}
}
