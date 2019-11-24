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
package grondag.xm.mesh.helper;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

@API(status = INTERNAL)
public class NormalQuantizer {
	private static int packComponent(float n) {
		return Math.round(n * 256) + 256;
	}

	private static float unpackComponent(int q) {
		return (q - 256) / 256f;
	}

	public static int pack(float x, float y, float z) {
		final int qx = packComponent(x);
		final int qy = packComponent(y);
		final int qz = packComponent(z);
		return qx | (qy << 10) | (qz << 20);
	}

	public static float unpackX(int q) {
		return unpackComponent(q & 0x3FF);
	}

	public static float unpackY(int q) {
		return unpackComponent((q >> 10) & 0x3FF);
	}

	public static float unpackZ(int q) {
		return unpackComponent((q >> 20) & 0x3FF);
	}
}
