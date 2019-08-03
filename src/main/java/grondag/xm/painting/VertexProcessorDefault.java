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

package grondag.xm.painting;

import grondag.fermion.color.ColorHelper;
import grondag.xm.api.model.ModelState;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.surface.XmSurface;
import grondag.xm.mesh.polygon.IMutablePolygon;

public class VertexProcessorDefault extends VertexProcessor {
    public final static VertexProcessor INSTANCE = new VertexProcessorDefault();

    static {
        VertexProcessors.register(VertexProcessorDefault.INSTANCE);
    }

    VertexProcessorDefault() {
        super("default", 0);
    }

    @Override
    public final void process(IMutablePolygon poly, int textureIndex, ModelState modelState, XmSurface surface, XmPaint paint) {
        int color = paint.textureColor(textureIndex);

        // TODO: remove? Was causing problems when acuity is enabled because renderpass
        // will be solid
//        if(modelState.getRenderPass(paintLayer) != BlockRenderLayer.TRANSLUCENT)
//            color =  0xFF000000 | color;

        // If surface is a lamp gradient then glow bits are used
        // to blend the lamp color/brighness with the nominal color/brightness.
        // This does not apply with the lamp paint layer itself (makes no sense).
        // (Generally gradient surfaces should not be painted by lamp color)
        // TODO: put back lamp gradient processing
//        if (surface.isLampGradient()) {
//            int lampColor = .getColorARGB(PaintLayer.LAMP);
//            int lampBrightness = modelState.isEmissive(PaintLayer.LAMP) ? 255 : 0;
//
//            // keep target surface alpha
//            int alpha = color & 0xFF000000;
//
//            for (int i = 0; i < poly.vertexCount(); i++) {
//                final float w = poly.getVertexGlow(i) / 255f;
//                int b = Math.round(lampBrightness * w);
//                int c = ColorHelper.interpolate(color, lampColor, w) & 0xFFFFFF;
//                poly.spriteColor(i, layerIndex, c | alpha);
//                poly.setVertexGlow(i, b);
//            }
//        } else {
        // normal shaded surface - tint existing colors, usually WHITE to start with
        for (int i = 0; i < poly.vertexCount(); i++) {
            final int c = ColorHelper.multiplyColor(color, poly.spriteColor(i, textureIndex));
            poly.spriteColor(i, textureIndex, c);
        }
//        }
    }

}
