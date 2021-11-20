/*
 * Copyright © Original Authors
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
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.api.mesh;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.xm.mesh.XmMeshesImpl;

@Experimental
public class XmMeshes {
	private XmMeshes() { }

	public static WritableMesh claimWritable() {
		return XmMeshesImpl.claimWritable();
	}

	public static MutableMesh claimMutable() {
		return XmMeshesImpl.claimMutable();
	}

	/**
	 * Creates a mesh with randomly recolored copies of the input mesh polygons.
	 *
	 * <p>Does not modify or release the input mesh.
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
