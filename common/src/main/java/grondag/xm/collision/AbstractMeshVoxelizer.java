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

package grondag.xm.collision;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Internal;

import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.mesh.polygon.Vec3f;

@Internal
abstract class AbstractMeshVoxelizer implements Consumer<Polygon> {
	// diameters
	static final float D1 = 0.5f;
	static final float D2 = D1 * 0.5f;
	static final float D3 = D2 * 0.5f;
	static final float D4 = D3 * 0.5f;

	// radii
	static final float R1 = D1 * 0.5f;
	static final float R2 = D2 * 0.5f;
	static final float R3 = D3 * 0.5f;
	static final float R4 = D4 * 0.5f;

	// center offsets, low and high
	static final float CLOW1 = 0.25f;
	static final float CHIGH1 = CLOW1 + D1;

	static final float CLOW2 = CLOW1 * 0.5f;
	static final float CHIGH2 = CLOW2 + D2;

	static final float CLOW3 = CLOW2 * 0.5f;
	static final float CHIGH3 = CLOW3 + D3;

	static final float CLOW4 = CLOW3 * 0.5f;
	static final float CHIGH4 = CLOW4 + D4;

	@Override
	public final void accept(Polygon poly) {
		acceptTriangle(poly.getPos(0), poly.getPos(1), poly.getPos(2));

		if (poly.vertexCount() == 4) {
			acceptTriangle(poly.getPos(0), poly.getPos(2), poly.getPos(3));
		}

		assert poly.vertexCount() < 5;
	}

	protected abstract void acceptTriangle(Vec3f v0, Vec3f v1, Vec3f v2);
}
