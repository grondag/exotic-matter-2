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
package grondag.xm.modelstate;

import org.jetbrains.annotations.ApiStatus.Internal;

import grondag.fermion.varia.NBTDictionary;

@Internal
public abstract class ModelStateTagHelper {
	private ModelStateTagHelper() {
	}

	public static final String NBT_SHAPE = NBTDictionary.GLOBAL.claim("xms");
	static final String NBT_SHAPE_BITS = NBTDictionary.GLOBAL.claim("xsb");
	static final String NBT_WORLD_BITS = NBTDictionary.GLOBAL.claim("xwb");
	static final String NBT_PAINTS = NBTDictionary.GLOBAL.claim("xmp");



}
