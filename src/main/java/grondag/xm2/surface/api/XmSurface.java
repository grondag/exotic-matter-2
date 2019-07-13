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

package grondag.xm2.surface.api;

import grondag.xm2.painting.SurfaceTopology;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;

public interface XmSurface {
	int FLAG_ALLOW_BORDERS = 1;
	int FLAG_IGNORE_DEPTH_FOR_RADOMIZATION = 2;
	int FLAG_LAMP_GRADIENT = 4;
	
	int ordinal();
	
	String nameKey();
	
	@Environment(EnvType.CLIENT)
	default String name() {
		return I18n.translate(nameKey());
	}
	
    SurfaceTopology topology();
    
    float uvWrapDistance();
    
    int flags();

    default boolean ignoreDepthForRandomization() {
    	return (flags() & FLAG_IGNORE_DEPTH_FOR_RADOMIZATION) == FLAG_IGNORE_DEPTH_FOR_RADOMIZATION;
    }
    
    default boolean allowBorders() {
    	return (flags() & FLAG_ALLOW_BORDERS) == FLAG_ALLOW_BORDERS;
    }
    
    default boolean isLampGradient() {
    	return (flags() & FLAG_LAMP_GRADIENT) == FLAG_LAMP_GRADIENT;
    }
}
