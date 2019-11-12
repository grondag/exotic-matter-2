package grondag.xm.api.mesh.generator;

import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.PolyTransform;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.texture.TextureOrientation;
import net.minecraft.util.math.Direction;

public class StairMesh {
    public static void build(
            WritableMesh mesh,
            PolyTransform transform,
            boolean isCorner,
            boolean isInside,
            XmSurface SURFACE_BOTTOM,
            XmSurface SURFACE_TOP,
            XmSurface SURFACE_FRONT,
            XmSurface SURFACE_BACK,
            XmSurface SURFACE_LEFT,
            XmSurface SURFACE_RIGHT )
    {
        // Default geometry bottom/back against down/south faces. Corner is on right.

        // Sides are split into three quadrants vs one long strip plus one long quadrant
        // is necessary to avoid AO lighting artifacts. AO is done by vertex, and having
        // a T-junction tends to mess about with the results.

        final MutablePolygon quad = mesh.writer();

        quad.rotation(0, TextureOrientation.IDENTITY);
        quad.lockUV(0, true);
        quad.saveDefaults();

        // bottom is always the same
        quad.surface(SURFACE_BOTTOM);
        quad.nominalFace(Direction.DOWN);
        quad.setupFaceQuad(0, 0, 1, 1, 0, Direction.NORTH);
        transform.accept(quad);
        quad.append();


        // back is full except for outside corners
        if(isCorner && !isInside) {
            quad.surface(SURFACE_BACK);
            quad.setupFaceQuad(Direction.SOUTH, 0.0f, 0.0f, 0.5f, 0.5f, 0.0f, Direction.UP);
            transform.accept(quad);
            quad.append();

            quad.surface(SURFACE_BACK);
            quad.setupFaceQuad(Direction.SOUTH, 0.5f, 0.0f, 1.0f, 0.5f, 0.0f, Direction.UP);
            transform.accept(quad);
            quad.append();

            quad.surface(SURFACE_BACK);
            quad.setupFaceQuad(Direction.SOUTH, 0.0f, 0.5f, 0.5f, 1.0f, 0.0f, Direction.UP);
            transform.accept(quad);
            quad.append();
        } else {
            quad.surface(SURFACE_BACK);
            quad.nominalFace(Direction.SOUTH);
            quad.setupFaceQuad(0, 0, 1, 1, 0, Direction.UP);
            transform.accept(quad);
            quad.append();
        }

        if(isCorner) {
            if(isInside) {
                quad.surface(SURFACE_LEFT);
                quad.setupFaceQuad(Direction.EAST, 0.0f, 0.5f, 0.5f, 1.0f, 0.0f, Direction.UP);
                transform.accept(quad);
                quad.append();

                // Extra, inset top quadrant on inside corner

                // make cuts appear different from top/front face
                quad.textureSalt(1);
                quad.surface(SURFACE_LEFT);
                quad.setupFaceQuad(Direction.EAST, 0.5f, 0.5f, 1.0f, 1.0f, 0.5f, Direction.UP);
                transform.accept(quad);
                quad.append();

            } else {
                // Left side top quadrant is inset on an outside corner
                quad.textureSalt(1);
                quad.surface(SURFACE_LEFT);
                quad.setupFaceQuad(Direction.EAST, 0.0f, 0.5f, 0.5f, 1.0f, 0.5f, Direction.UP);
                transform.accept(quad);
                quad.append();
            }

        } else {
            quad.surface(SURFACE_LEFT);
            quad.setupFaceQuad(Direction.EAST, 0.0f, 0.5f, 0.5f, 1.0f, 0.0f, Direction.UP);
            transform.accept(quad);
            quad.append();
        }

        quad.surface(SURFACE_LEFT);
        quad.setupFaceQuad(Direction.EAST, 0.0f, 0.0f, 0.5f, 0.5f, 0.0f, Direction.UP);
        transform.accept(quad);
        quad.append();

        quad.surface(SURFACE_LEFT);
        quad.setupFaceQuad(Direction.EAST, 0.5f, 0.0f, 1.0f, 0.5f, 0.0f, Direction.UP);
        transform.accept(quad);
        quad.append();


        // right side is a full face on an inside corner
        if(isCorner && isInside) {
            quad.surface(SURFACE_RIGHT);
            quad.setupFaceQuad(Direction.WEST, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, Direction.UP);
            transform.accept(quad);
            quad.append();
        } else {
            quad.surface(SURFACE_RIGHT);
            quad.setupFaceQuad(Direction.WEST, 0.0f, 0.0f, 0.5f, 0.5f, 0.0f, Direction.UP);
            transform.accept(quad);
            quad.append();

            quad.surface(SURFACE_RIGHT);
            quad.setupFaceQuad(Direction.WEST, 0.5f, 0.0f, 1.0f, 0.5f, 0.0f, Direction.UP);
            transform.accept(quad);
            quad.append();

            quad.surface(SURFACE_RIGHT);
            quad.setupFaceQuad(Direction.WEST, 0.5f, 0.5f, 1.0f, 1.0f, 0.0f, Direction.UP);
            transform.accept(quad);
            quad.append();
        }

        // front
        if(isCorner) {
            if(isInside) {
                quad.surface(SURFACE_FRONT);
                quad.setupFaceQuad(Direction.NORTH, 0.0f, 0.0f, 0.5f, 0.5f, 0.0f, Direction.UP);
                transform.accept(quad);
                quad.append();

                quad.surface(SURFACE_FRONT);
                quad.setupFaceQuad(Direction.NORTH, 0.5f, 0.0f, 1.0f, 0.5f, 0.0f, Direction.UP);
                transform.accept(quad);
                quad.append();

                quad.surface(SURFACE_FRONT);
                quad.setupFaceQuad(Direction.NORTH, 0.5f, 0.5f, 1.0f, 1.0f, 0.0f, Direction.UP);
                transform.accept(quad);
                quad.append();

                quad.textureSalt(1);
                quad.surface(SURFACE_FRONT);
                quad.setupFaceQuad(Direction.NORTH, 0.0f, 0.5f, 0.5f, 1.0f, 0.5f, Direction.UP);
                transform.accept(quad);
                quad.append();
            } else {
                quad.surface(SURFACE_FRONT);
                quad.setupFaceQuad(Direction.NORTH, 0.0f, 0.0f, 1.0f, 0.5f, 0.0f, Direction.UP);
                transform.accept(quad);
                quad.append();

                quad.textureSalt(1);
                quad.surface(SURFACE_FRONT);
                quad.setupFaceQuad(Direction.NORTH, 0.5f, 0.5f, 1.0f, 1.0f, 0.5f, Direction.UP);
                transform.accept(quad);
                quad.append();
            }

        } else {
            quad.surface(SURFACE_FRONT);
            quad.setupFaceQuad(Direction.NORTH, 0.0f, 0.0f, 1.0f, 0.5f, 0.0f, Direction.UP);
            transform.accept(quad);
            quad.append();

            quad.textureSalt(1);
            quad.surface(SURFACE_FRONT);
            quad.setupFaceQuad(Direction.NORTH, 0.0f, 0.5f, 1.0f, 1.0f, 0.5f, Direction.UP);
            transform.accept(quad);
            quad.append();
        }

        // top
        if(isCorner) {
            if(isInside) {
                quad.surface(SURFACE_TOP);
                quad.setupFaceQuad(Direction.UP, 0.0f, 0.5f, 0.5f, 1.0f, 0.0f, Direction.SOUTH);
                transform.accept(quad);
                quad.append();

                quad.surface(SURFACE_TOP);
                quad.setupFaceQuad(Direction.UP, 0.5f, 0.5f, 1.0f, 1.0f, 0.0f, Direction.SOUTH);
                transform.accept(quad);
                quad.append();

                quad.surface(SURFACE_TOP);
                quad.setupFaceQuad(Direction.UP, 0.5f, 0.0f, 1.0f, 0.5f, 0.0f, Direction.SOUTH);
                transform.accept(quad);
                quad.append();

                quad.surface(SURFACE_TOP);
                quad.textureSalt(1);
                quad.setupFaceQuad(Direction.UP, 0.0f, 0.0f, 0.5f, 0.5f, 0.5f, Direction.SOUTH);
                transform.accept(quad);
                quad.append();
            } else {
                quad.surface(SURFACE_TOP);
                quad.setupFaceQuad(Direction.UP, 0.5f, 0.5f, 1.0f, 1.0f, 0.0f, Direction.SOUTH);
                transform.accept(quad);
                quad.append();

                quad.surface(SURFACE_TOP);
                quad.textureSalt(1);
                quad.setupFaceQuad(Direction.UP, 0.0f, 0.0f, 0.5f, 0.5f, 0.5f, Direction.SOUTH);
                transform.accept(quad);
                quad.append();

                quad.surface(SURFACE_TOP);
                quad.textureSalt(1);
                quad.setupFaceQuad(Direction.UP, 0.0f, 0.5f, 0.5f, 1.0f, 0.5f, Direction.SOUTH);
                transform.accept(quad);
                quad.append();

                quad.surface(SURFACE_TOP);
                quad.textureSalt(1);
                quad.setupFaceQuad(Direction.UP, 0.5f, 0.0f, 1.0f, 0.5f, 0.5f, Direction.SOUTH);
                transform.accept(quad);
                quad.append();
            }
        } else {
            quad.surface(SURFACE_TOP);
            quad.setupFaceQuad(Direction.UP, 0.0f, 0.5f, 1.0f, 1.0f, 0.0f, Direction.SOUTH);
            transform.accept(quad);
            quad.append();

            quad.surface(SURFACE_TOP);
            quad.textureSalt(1);
            quad.setupFaceQuad(Direction.UP, 0.0f, 0.0f, 1.0f, 0.5f, 0.5f, Direction.SOUTH);
            transform.accept(quad);
            quad.append();
        }
    }
}
