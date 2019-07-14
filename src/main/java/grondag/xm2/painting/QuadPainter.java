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

import grondag.fermion.varia.Useful;
import grondag.fermion.world.Rotation;
import grondag.xm2.api.paint.XmPaint;
import grondag.xm2.api.surface.XmSurface;
import grondag.xm2.api.texture.TextureRotation;
import grondag.xm2.api.texture.TextureScale;
import grondag.xm2.api.texture.TextureSet;
import grondag.xm2.mesh.polygon.IMutablePolygon;
import grondag.xm2.mesh.stream.IMutablePolyStream;
import grondag.xm2.api.model.MutableModelState;
import grondag.xm2.api.model.ModelWorldState;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

public abstract class QuadPainter {
    @FunctionalInterface
    public static interface IPaintMethod {
	/**
	 * Assigns specific texture and texture rotation based on model state and
	 * information in the polygon and surface. Also handles texture UV mapping.
	 * <p>
	 * 
	 * Implementations can and should assume locked UV coordinates are assigned
	 * before this is called if UV locking is enabled for the quad
	 * <p>
	 * 
	 * Implementation should claim and use first render layer with a null texture
	 * name.<br>
	 * (Claim by assigning a non-null texture name.)
	 * 
	 * Any polys in the input stream that are split should be deleted and new polys
	 * appended to the stream.
	 * <p>
	 * 
	 * Implementation may assume stream is non-empty and stream editor is at origin.
	 * <p>
	 * 
	 */
	void paintQuads(IMutablePolyStream stream, MutableModelState modelState, XmSurface surface, XmPaint paint,
		int textureDepth);
    }

    /**
     * Call from paint quad in sub classes to return results. Handles item scaling,
     * then adds to the output list.
     */
    // UGLY: change arg order to match others
    protected static void commonPostPaint(IMutablePolygon editor, int textureIndex, MutableModelState modelState,
	    XmSurface surface, XmPaint paint) {
	editor.setRenderLayer(textureIndex, paint.blendMode(textureIndex));
	editor.setEmissive(textureIndex, paint.emissive(textureIndex));

	paint.vertexProcessor(textureIndex).process(editor, textureIndex, modelState, surface, paint);

	// FIXME: not going to work with new primitives w/ shared geometry
	// move this to baking if still needed
//        if(isItem)
//        {
//            switch(this.paintLayer)
//            {
//            case MIDDLE:
//                inputQuad.scaleFromBlockCenter(1.01f);
//                break;
//
//            case OUTER:
//                inputQuad.scaleFromBlockCenter(1.02f);
//                break;
//
//            default:
//                break;
//            }
//        }
    }

    /**
     * Transform input vector so that x & y correspond with u / v on the given face,
     * with u,v origin at upper left and z is depth, where positive values represent
     * distance into the face (away from viewer). <br>
     * <br>
     * 
     * Coordinates are start masked to the scale of the texture being used and when
     * we reverse an orthogonalAxis, we use the texture's sliceMask as the basis so
     * that we remain within the frame of the texture scale we are using. <br>
     * <br>
     * 
     * Note that the x, y components are for determining min/max UV values. They
     * should NOT be used to set vertex UV coordinates directly. All bigtex models
     * should have lockUV = true, which means that uv coordinates will be derived at
     * time of quad bake by projecting each vertex onto the plane of the quad's
     * nominal face. Setting UV coordinates on a quad with lockUV=true has no
     * effect.
     */
    protected static Vec3i getSurfaceVector(int blockX, int blockY, int blockZ, Direction face, TextureScale scale) {
	// PERF: reuse instances?

	int sliceCountMask = scale.sliceCountMask;
	int x = blockX & sliceCountMask;
	int y = blockY & sliceCountMask;
	int z = blockZ & sliceCountMask;

	switch (face) {
	case EAST:
	    return new Vec3i(sliceCountMask - z, sliceCountMask - y, -blockX);

	case WEST:
	    return new Vec3i(z, sliceCountMask - y, blockX);

	case NORTH:
	    return new Vec3i(sliceCountMask - x, sliceCountMask - y, blockZ);

	case SOUTH:
	    return new Vec3i(x, sliceCountMask - y, -blockZ);

	case DOWN:
	    return new Vec3i(x, sliceCountMask - z, blockY);

	case UP:
	default:
	    return new Vec3i(x, z, -blockY);
	}
    }

    /**
     * Rotates given surface vector around the center of the texture by the given
     * degree.
     * 
     */
    protected static Vec3i rotateFacePerspective(Vec3i vec, Rotation rotation, TextureScale scale) {
	// PERF - reuse instances?
	switch (rotation) {
	case ROTATE_90:
	    return new Vec3i(vec.getY(), scale.sliceCountMask - vec.getX(), vec.getZ());

	case ROTATE_180:
	    return new Vec3i(scale.sliceCountMask - vec.getX(), scale.sliceCountMask - vec.getY(), vec.getZ());

	case ROTATE_270:
	    return new Vec3i(scale.sliceCountMask - vec.getY(), vec.getX(), vec.getZ());

	case ROTATE_NONE:
	default:
	    return vec;

	}
    }

    protected static int textureVersionForFace(Direction face, TextureSet tex, MutableModelState modelState) {
	if (tex.versionCount() == 0)
	    return 0;
	return textureHashForFace(face, tex, modelState) & tex.versionMask();
    }

    protected static int textureHashForFace(Direction face, TextureSet tex, MutableModelState modelState) {
	final int species = modelState.hasSpecies() ? modelState.worldState().species() : 0;
	final int speciesBits = species << 16;
	final int shift = tex.scale().power;
	final ModelWorldState worldState = modelState.worldState();
	
	switch (face) {
	case DOWN:
	case UP: {
	    final int yBits = (((worldState.posX() >> shift) & 0xFF) << 8) | ((worldState.posZ() >> shift) & 0xFF)
		    | speciesBits;
	    return HashCommon.mix(yBits);
	}

	case EAST:
	case WEST: {
	    final int xBits = (((worldState.posY() >> shift) & 0xFF) << 8) | ((worldState.posZ() >> shift) & 0xFF)
		    | speciesBits;
	    return HashCommon.mix(xBits);
	}

	case NORTH:
	case SOUTH: {
	    final int zBits = (((worldState.posX() >> shift) & 0xFF) << 8) | ((worldState.posY() >> shift) & 0xFF)
		    | speciesBits;
	    return HashCommon.mix(zBits);
	}

	default:
	    return 0;
	}
    }

    /**
     * Gives randomized (if applicable) texture rotation for the given face. If
     * texture rotation type is FIXED, gives the textures default rotation. If
     * texture rotation type is CONSISTENT, is based on species only. If texture
     * rotation type is RANDOM, is based on position (chunked by texture size) and
     * species (if applies).
     */
    protected static Rotation textureRotationForFace(Direction face, TextureSet tex, MutableModelState modelState) {
	final int species = modelState.hasSpecies() ? modelState.worldState().species() : 0;
	if (tex.rotation() == TextureRotation.ROTATE_RANDOM) {
	    if (tex.scale() == TextureScale.SINGLE) {
		return Useful.offsetEnumValue(Rotation.ROTATE_NONE,
			(textureHashForFace(face, tex, modelState) >> 8) & 3);
	    } else {
		return species == 0 ? Rotation.ROTATE_NONE
			: Useful.offsetEnumValue(Rotation.ROTATE_NONE, HashCommon.mix(species) & 3);
	    }
	} else {
	    return tex.rotation().rotation;
	}
    }
}
