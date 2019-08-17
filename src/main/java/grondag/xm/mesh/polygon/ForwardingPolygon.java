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

package grondag.xm.mesh.polygon;

import grondag.fermion.spatial.Rotation;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.mesh.vertex.Vec3f;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.util.math.Direction;

public class ForwardingPolygon implements Polygon {
    @FunctionalInterface
    public static interface BooleanGetter {
        boolean getValue();
    }

    @FunctionalInterface
    public static interface BooleanSetter {
        void setValue(boolean value);
    }

    @FunctionalInterface
    public static interface IntGetter {
        int getValue();
    }

    @FunctionalInterface
    public static interface IntSetter {
        void setValue(int value);
    }

    public static final BooleanSetter DEFAULT_BOOL_SETTER = (b) -> {
        throw new UnsupportedOperationException();
    };
    public static final BooleanGetter DEFAULT_BOOL_GETTER = () -> false;

    public static final IntSetter DEFAULT_INT_SETTER = (i) -> {
        throw new UnsupportedOperationException();
    };
    public static final IntGetter DEFAULT_INT_GETTER = () -> Polygon.NO_LINK_OR_TAG;

    public Polygon wrapped;

    public BooleanSetter markSetter = DEFAULT_BOOL_SETTER;
    public BooleanGetter markGetter = DEFAULT_BOOL_GETTER;

    public BooleanSetter deletedSetter = DEFAULT_BOOL_SETTER;
    public BooleanGetter deletedGetter = DEFAULT_BOOL_GETTER;

    public IntSetter linkSetter = DEFAULT_INT_SETTER;
    public IntGetter linkGetter = DEFAULT_INT_GETTER;

    public IntSetter tagSetter = DEFAULT_INT_SETTER;
    public IntGetter tagGetter = DEFAULT_INT_GETTER;

    @Override
    public int vertexCount() {
        return wrapped.vertexCount();
    }

    @Override
    public Vec3f getPos(int index) {
        return wrapped.getPos(index);
    }

    @Override
    public Vec3f faceNormal() {
        return wrapped.faceNormal();
    }

    @Override
    public Direction nominalFace() {
        return wrapped.nominalFace();
    }

    @Override
    public Direction cullFace() {
        return wrapped.cullFace();
    }
    
    @Override
    public XmSurface surface() {
        return wrapped.surface();
    }

    @Override
    public Vec3f vertexNormal(int vertexIndex) {
        return wrapped.vertexNormal(vertexIndex);
    }

    @Override
    public boolean hasNormal(int vertexIndex) {
        return wrapped.hasNormal(vertexIndex);
    }

    @Override
    public float normalX(int vertexIndex) {
        return wrapped.normalX(vertexIndex);
    }

    @Override
    public float normalY(int vertexIndex) {
        return wrapped.normalY(vertexIndex);
    }

    @Override
    public float normalZ(int vertexIndex) {
        return wrapped.normalZ(vertexIndex);
    }

    @Override
    public float maxU(int spriteIndex) {
        return wrapped.maxU(spriteIndex);
    }

    @Override
    public float maxV(int spriteIndex) {
        return wrapped.maxV(spriteIndex);
    }

    @Override
    public float minU(int spriteIndex) {
        return wrapped.minU(spriteIndex);
    }

    @Override
    public float minV(int spriteIndex) {
        return wrapped.minV(spriteIndex);
    }

    @Override
    public float uvWrapDistance() {
        return wrapped.uvWrapDistance();
    }

    @Override
    public int spriteDepth() {
        return wrapped.spriteDepth();
    }

    @Override
    public String spriteName(int spriteIndex) {
        return wrapped.spriteName(spriteIndex);
    }

    @Override
    public boolean shouldContractUVs(int spriteIndex) {
        return wrapped.shouldContractUVs(spriteIndex);
    }

    @Override
    public Rotation rotation(int spriteIndex) {
        return wrapped.rotation(spriteIndex);
    }

    @Override
    public float x(int vertexIndex) {
        return wrapped.x(vertexIndex);
    }

    @Override
    public float y(int vertexIndex) {
        return wrapped.y(vertexIndex);
    }

    @Override
    public float z(int vertexIndex) {
        return wrapped.z(vertexIndex);
    }

    @Override
    public int spriteColor(int vertexIndex, int spriteIndex) {
        return wrapped.spriteColor(vertexIndex, spriteIndex);
    }

    @Override
    public int glow(int vertexIndex) {
        return wrapped.glow(vertexIndex);
    }

    @Override
    public float spriteU(int vertexIndex, int spriteIndex) {
        return wrapped.spriteU(vertexIndex, spriteIndex);
    }

    @Override
    public float spriteV(int vertexIndex, int spriteIndex) {
        return wrapped.spriteV(vertexIndex, spriteIndex);
    }

    @Override
    public int textureSalt() {
        return wrapped.textureSalt();
    }

    @Override
    public boolean lockUV(int spriteIndex) {
        return wrapped.lockUV(spriteIndex);
    }

    @Override
    public boolean hasRenderLayer(BlockRenderLayer layer) {
        return wrapped.hasRenderLayer(layer);
    }

    @Override
    public BlockRenderLayer blendMode(int spriteIndex) {
        return wrapped.blendMode(spriteIndex);
    }

    @Override
    public boolean emissive(int spriteIndex) {
        return wrapped.emissive(spriteIndex);
    }

    @Override
    public boolean disableAo(int spriteIndex) {
        return wrapped.disableAo(spriteIndex);
    }

    @Override
    public boolean disableDiffuse(int spriteIndex) {
        return wrapped.disableDiffuse(spriteIndex);
    }
    
    @Override
    public boolean isMarked() {
        return markGetter.getValue();
    }

    @Override
    public void setMark(boolean isMarked) {
        markSetter.setValue(isMarked);
    }

    @Override
    public void setDeleted() {
        deletedSetter.setValue(true);
    }

    @Override
    public boolean isDeleted() {
        return deletedGetter.getValue();
    }

    @Override
    public void setLink(int link) {
        linkSetter.setValue(link);
    }

    @Override
    public int getLink() {
        return linkGetter.getValue();
    }

    @Override
    public int tag() {
        return tagGetter.getValue();
    }
}
