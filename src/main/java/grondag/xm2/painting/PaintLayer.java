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

package grondag.xm2.painting;

import grondag.fermion.structures.BinaryEnumSet;
import net.minecraft.client.resource.language.I18n;

/**
 * Primitive models can have up to five paint layers. These are vaguely akin to
 * shaders or materials in that each layer will have a texture and other
 * appearance-defining attributes.
 * <p>
 * 
 * The layers have names that describe typical use but they can be used for
 * anything, provided the mesh generator and the model state agree on which
 * surfaces should get which paint.
 * <p>
 * 
 * z-position of layers is per enum order.
 * 
 */
public enum PaintLayer {
    /**
     * Typically used as the undecorated appearance the primary surface, often the
     * only paint layer. Lowest (unmodified) z position.
     */
    BASE(0),

    /**
     * Typically used to render sides or bottoms, or the cut surfaces of CSG
     * outputs.
     */
    CUT(0),

    /**
     * Typically used to render a secondary surface within a model.
     */
    LAMP(0),

    /**
     * Typically used to decorate the primary surface.
     */
    MIDDLE(1),

    /**
     * Typically used to decorate the primary surface.
     */
    OUTER(2);

    /** Convenience for values().length */
    public static final int SIZE;

    /** Convenience for values() */
    public static final PaintLayer VALUES[];

    static {
        SIZE = values().length;
        VALUES = values();
    }

    public static BinaryEnumSet<PaintLayer> BENUMSET = new BinaryEnumSet<>(PaintLayer.class);

    private PaintLayer(int textureLayerIndex) {
        this.textureLayerIndex = textureLayerIndex;
    }

    public final int textureLayerIndex;

    public String localizedName() {
        return I18n.translate("paintlayer." + this.name().toLowerCase());
    }

}
