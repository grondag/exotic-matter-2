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

package grondag.xm2.mesh.helper;

/**
 * Used to express quads on a face (2D). By default u,v map directly to x, y on
 * the given face
 */
public class FaceVertex {
    public final float x;
    public final float y;
    public final float depth;
    protected final short glow;

    public FaceVertex(float x, float y, float depth) {
        this(x, y, depth, 0);
    }

    public FaceVertex(float x, float y, float depth, int glow) {
        this.x = x;
        this.y = y;
        this.depth = depth;
        this.glow = (short) (glow & 0xFF);
    }

    public int glow() {
        return this.glow;
    }

    @Override
    public FaceVertex clone() {
        assert false : "Why u clone immutable object?";
        return new FaceVertex(x, y, depth, this.glow);
    }

    public FaceVertex withXY(float x, float y) {
        return new FaceVertex(x, y, this.depth, this.glow);
    }

    public FaceVertex withDepth(float depth) {
        return new FaceVertex(this.x, this.y, depth, this.glow);
    }

    public FaceVertex withColor(int color) {
        return new FaceVertex.Colored(this.x, this.y, depth, color, this.glow);
    }

    public FaceVertex withGlow(int glow) {
        return new FaceVertex(this.x, this.y, this.depth, glow);
    }

    public FaceVertex withUV(float u, float v) {
        return new FaceVertex.UV(this.x, this.y, this.depth, u, v, this.glow);
    }

    public int color() {
        return 0xFFFFFFFF;
    }

    /**
     * This value is logical 0-1 within the texture for this face. NOT 0-16. And NOT
     * interpolated for the sprite. <br>
     * <br>
     * 
     * Note that the V orientation is flipped from the Y axis used for vertices.
     * Origin is at the top left for textures, vs. bottom left for vertex
     * coordinates. This means the default values for u, v will be x, 1-y. <br>
     * <br>
     * 
     * The bottom face is handled differently and RawQuad will flip it
     * automatically..
     */
    public float u() {
        return x;
    }

    /**
     * See {@link #u()}
     */
    public float v() {
        return 1 - y;
    }

    public static class Colored extends FaceVertex {
        private final int color;

        public Colored(float x, float y, float depth, int color, int glow) {
            super(x, y, depth, glow);
            this.color = color;
        }

        public Colored(float x, float y, float depth, float u, float v, int color, int glow) {
            super(x, y, depth, glow);
            this.color = color;
        }

        @Override
        public FaceVertex clone() {
            return new FaceVertex.Colored(x, y, depth, color, glow);
        }

        @Override
        public int color() {
            return color;
        }

        @Override
        public FaceVertex withXY(float x, float y) {
            return new FaceVertex.Colored(x, y, this.depth, this.color, this.glow);
        }

        @Override
        public FaceVertex withDepth(float depth) {
            return new FaceVertex.Colored(this.x, this.y, depth, this.color, this.glow);
        }

        @Override
        public FaceVertex withColor(int color) {
            return new FaceVertex.Colored(this.x, this.y, depth, color, this.glow);
        }

        @Override
        public FaceVertex withGlow(int glow) {
            return new FaceVertex.Colored(this.x, this.y, depth, this.color, glow);
        }

        @Override
        public FaceVertex withUV(float u, float v) {
            return new FaceVertex.UVColored(this.x, this.y, this.depth, u, v, this.color, this.glow);
        }
    }

    public static class UV extends FaceVertex {
        private final float u;
        private final float v;

        public UV(float x, float y, float depth, float u, float v) {
            this(x, y, depth, u, v, 0);
        }

        public UV(float x, float y, float depth, float u, float v, int glow) {
            super(x, y, depth, glow);
            this.u = u;
            this.v = v;
        }

        @Override
        public float u() {
            return this.u;
        }

        @Override
        public float v() {
            return this.v;
        }

        @Override
        public FaceVertex clone() {
            assert false : "Why u clone immutable object?";
            return new FaceVertex.UV(x, y, depth, u, v, glow);
        }

        @Override
        public FaceVertex withXY(float x, float y) {
            return new FaceVertex.UV(x, y, this.depth, this.u, this.v, this.glow);
        }

        @Override
        public FaceVertex withDepth(float depth) {
            return new FaceVertex.UV(this.x, this.y, depth, this.u, this.v, this.glow);
        }

        @Override
        public FaceVertex withColor(int color) {
            return new FaceVertex.UVColored(this.x, this.y, depth, this.u, this.v, color, this.glow);
        }

        @Override
        public FaceVertex withGlow(int glow) {
            return new FaceVertex.UV(this.x, this.y, depth, this.u, this.v, glow);
        }

        @Override
        public FaceVertex withUV(float u, float v) {
            return new FaceVertex.UV(this.x, this.y, this.depth, u, v, this.glow);
        }
    }

    public static class UVColored extends FaceVertex {
        private final float u;
        private final float v;
        private final int color;

        public UVColored(float x, float y, float depth, float u, float v, int color, int glow) {
            super(x, y, depth, glow & 0xFF);
            this.u = u;
            this.v = v;
            this.color = color;
        }

        @Override
        public int color() {
            return color;
        }

        @Override
        public float u() {
            return this.u;
        }

        @Override
        public float v() {
            return this.v;
        }

        @Override
        public FaceVertex clone() {
            assert false : "Why u clone immutable object?";
            return new FaceVertex.UVColored(x, y, depth, u, v, color, glow);
        }

        @Override
        public FaceVertex withXY(float x, float y) {
            return new FaceVertex.UVColored(x, y, this.depth, this.u, this.v, this.color, this.glow);
        }

        @Override
        public FaceVertex withDepth(float depth) {
            return new FaceVertex.UVColored(this.x, this.y, depth, this.u, this.v, this.color, this.glow);
        }

        @Override
        public FaceVertex withColor(int color) {
            return new FaceVertex.UVColored(this.x, this.y, this.depth, this.u, this.v, color, this.glow);
        }

        @Override
        public FaceVertex withGlow(int glow) {
            return new FaceVertex.UVColored(this.x, this.y, depth, this.u, this.v, this.color, glow);
        }

        @Override
        public FaceVertex withUV(float u, float v) {
            return new FaceVertex.UVColored(this.x, this.y, this.depth, u, v, this.color, this.glow);
        }
    }
}
