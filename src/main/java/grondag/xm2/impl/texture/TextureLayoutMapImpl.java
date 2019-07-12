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

package grondag.xm2.impl.texture;

import java.util.function.Consumer;

import grondag.xm2.api.texture.TextureLayout;
import grondag.xm2.api.texture.TextureLayoutMap;
import grondag.xm2.api.texture.TextureNameFunction;
import grondag.xm2.api.texture.TextureSet;
import net.minecraft.util.Identifier;

public class TextureLayoutMapImpl implements TextureLayoutMap {
	public static TextureLayoutMapImpl create(TextureLayout layout, TextureNameFunction nameFunc) {
		return new TextureLayoutMapImpl(layout, nameFunc);
	}
	
    public final TextureLayout layout;
    
    public final TextureNameFunction nameFunc;
    
    private TextureLayoutMapImpl(TextureLayout layout, TextureNameFunction nameFunc) {
    	this.layout = layout;
    	this.nameFunc = nameFunc;
    };
    
    @Override
    public TextureLayout layout() {
    	return layout;
    }
    
    public final void prestitch(TextureSet texture, Consumer<Identifier> stitcher) {
        for (int i = 0; i < texture.versionCount(); i++) {
        	for(int j = 0; j < layout.textureCount; j++) {
        		stitcher.accept(new Identifier(nameFunc.apply(texture.baseTextureName(), i, j)));
        	}
        }
    }
    
    public final String buildTextureName(TextureSet texture, int version, int index) {
    	return nameFunc.apply(texture.baseTextureName(), version, index);
    }

    public final String sampleTextureName(TextureSet texture) {
        return nameFunc.apply(texture.baseTextureName(), 0, 0);
    }
}
