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

package grondag.brocade.connect.impl;

import static org.apiguardian.api.API.Status.INTERNAL;
import static grondag.brocade.connect.api.model.FaceCorner.*;
import static grondag.brocade.connect.api.model.FaceEdge.*;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.apiguardian.api.API;

import grondag.brocade.connect.api.model.BlockEdge;
import grondag.brocade.connect.api.model.FaceCorner;
import grondag.brocade.connect.api.model.FaceEdge;
import grondag.brocade.connect.api.state.CornerJoinFaceState;
import grondag.brocade.connect.api.world.BlockNeighbors;
import grondag.brocade.connect.impl.helper.FaceEdgeHelper;
import net.minecraft.util.math.Direction;

/**
 * Corner bits indicate that a corner is needed, not that the corner is present.
 * (These are normally inverse.)
 */
@API(status = INTERNAL)
public enum CornerJoinFaceStateImpl implements CornerJoinFaceState {
    NO_FACE(0, 0), 
    NONE(0, 0), // must be after NO_FACE, overwrites NO_FACE in lookup table, should never be checked by lookup
    TOP(TOP_EDGE.ordinalBit, 0), 
    BOTTOM(BOTTOM_EDGE.ordinalBit, 0), 
    LEFT(LEFT_EDGE.ordinalBit, 0),
    RIGHT(RIGHT_EDGE.ordinalBit, 0),
    
    TOP_BOTTOM(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit, 0),
    LEFT_RIGHT(LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, 0),

    TOP_BOTTOM_RIGHT_NO_CORNERS(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, 0,
            TOP_RIGHT, BOTTOM_RIGHT),
    TOP_BOTTOM_RIGHT_TR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            TOP_RIGHT.ordinalBit),
    TOP_BOTTOM_RIGHT_BR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            BOTTOM_RIGHT.ordinalBit),
    TOP_BOTTOM_RIGHT_TR_BR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            TOP_RIGHT.ordinalBit | BOTTOM_RIGHT.ordinalBit),

    TOP_BOTTOM_LEFT_NO_CORNERS(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit, 0,
            TOP_LEFT, BOTTOM_LEFT),
    TOP_BOTTOM_LEFT_TL(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit,
            TOP_LEFT.ordinalBit),
    TOP_BOTTOM_LEFT_BL(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit,
            BOTTOM_LEFT.ordinalBit),
    TOP_BOTTOM_LEFT_TL_BL(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit,
            TOP_LEFT.ordinalBit | BOTTOM_LEFT.ordinalBit),

    TOP_LEFT_RIGHT_NO_CORNERS(TOP_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, 0,
            TOP_LEFT, TOP_RIGHT),
    TOP_LEFT_RIGHT_TL(TOP_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            TOP_LEFT.ordinalBit),
    TOP_LEFT_RIGHT_TR(TOP_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            TOP_RIGHT.ordinalBit),
    TOP_LEFT_RIGHT_TL_TR(TOP_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            TOP_LEFT.ordinalBit | TOP_RIGHT.ordinalBit),

    BOTTOM_LEFT_RIGHT_NO_CORNERS(BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, 0,
            BOTTOM_LEFT, BOTTOM_RIGHT),
    BOTTOM_LEFT_RIGHT_BL(BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            BOTTOM_LEFT.ordinalBit),
    BOTTOM_LEFT_RIGHT_BR(BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            BOTTOM_RIGHT.ordinalBit),
    BOTTOM_LEFT_RIGHT_BL_BR(BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            BOTTOM_LEFT.ordinalBit | BOTTOM_RIGHT.ordinalBit),

    TOP_LEFT_NO_CORNER(TOP_EDGE.ordinalBit | LEFT_EDGE.ordinalBit, 0, TOP_LEFT),
    TOP_LEFT_TL(TOP_EDGE.ordinalBit | LEFT_EDGE.ordinalBit, TOP_LEFT.ordinalBit),

    TOP_RIGHT_NO_CORNER(TOP_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, 0, TOP_RIGHT),
    TOP_RIGHT_TR(TOP_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, TOP_RIGHT.ordinalBit),

    BOTTOM_LEFT_NO_CORNER(BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit, 0, BOTTOM_LEFT),
    BOTTOM_LEFT_BL(BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit, BOTTOM_LEFT.ordinalBit),

    BOTTOM_RIGHT_NO_CORNER(BOTTOM_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, 0, BOTTOM_RIGHT),
    BOTTOM_RIGHT_BR(BOTTOM_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, BOTTOM_RIGHT.ordinalBit),

    ALL_NO_CORNERS(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, 0,
            TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT),
    ALL_TL(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            TOP_LEFT.ordinalBit),
    ALL_TR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            TOP_RIGHT.ordinalBit),
    ALL_TL_TR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            TOP_RIGHT.ordinalBit | TOP_LEFT.ordinalBit),
    ALL_BL(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            BOTTOM_LEFT.ordinalBit),
    ALL_TL_BL(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            BOTTOM_LEFT.ordinalBit | TOP_LEFT.ordinalBit),
    ALL_TR_BL(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            BOTTOM_LEFT.ordinalBit | TOP_RIGHT.ordinalBit),
    ALL_TL_TR_BL(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            BOTTOM_LEFT.ordinalBit | TOP_RIGHT.ordinalBit | TOP_LEFT.ordinalBit),
    ALL_BR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            BOTTOM_RIGHT.ordinalBit),
    ALL_TL_BR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            BOTTOM_RIGHT.ordinalBit | TOP_LEFT.ordinalBit),
    ALL_TR_BR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            BOTTOM_RIGHT.ordinalBit | TOP_RIGHT.ordinalBit),
    ALL_TL_TR_BR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            BOTTOM_RIGHT.ordinalBit | TOP_RIGHT.ordinalBit | TOP_LEFT.ordinalBit),
    ALL_BL_BR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            BOTTOM_RIGHT.ordinalBit | BOTTOM_LEFT.ordinalBit),
    ALL_TL_BL_BR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            BOTTOM_RIGHT.ordinalBit | BOTTOM_LEFT.ordinalBit | TOP_LEFT.ordinalBit),
    ALL_TR_BL_BR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            BOTTOM_RIGHT.ordinalBit | BOTTOM_LEFT.ordinalBit | TOP_RIGHT.ordinalBit),
    ALL_TL_TR_BL_BR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
            BOTTOM_RIGHT.ordinalBit | BOTTOM_LEFT.ordinalBit | TOP_RIGHT.ordinalBit | TOP_LEFT.ordinalBit);


    private final int bitFlags;
    private final FaceCorner[] cornerTests;
    private CornerJoinFaceStateImpl[] subStates;

    private CornerJoinFaceStateImpl(int faceBits, int cornerBits, FaceCorner... cornerTests) {
        this.bitFlags = faceBits | (cornerBits << 4);
        this.cornerTests = cornerTests;
    }
    
    private boolean hasCornerTests() {
        return (cornerTests != null && cornerTests.length > 0);
    }

    private FaceCorner[] cornerTests() {
        return cornerTests;
    }

    CornerJoinFaceStateImpl[] subStates() {
        return subStates;
    }

    @Override
    public boolean isJoined(FaceEdge side) {
        return (this.bitFlags & side.ordinalBit) == side.ordinalBit;
    }

    @Override
    public boolean isJoined(Direction toFace, Direction onFace) {
        FaceEdge side = FaceEdge.fromWorld(toFace, onFace);
        return side == null ? false : this.isJoined(side);
    }

    /**
     * True if connected-texture/shape blocks need to render corner due to
     * missing/covered block in adjacent corner.
     */
    @Override
    public boolean needsCorner(FaceCorner corner) {
        return ((this.bitFlags >> 4) & corner.ordinalBit) == corner.ordinalBit;
    }

    @Override
    public boolean needsCorner(Direction face1, Direction face2, Direction onFace) {
        FaceEdge side1 = FaceEdge.fromWorld(face1, onFace);
        FaceEdge side2 = FaceEdge.fromWorld(face2, onFace);
        return side1 == null || side2 == null ? false : this.needsCorner(FaceCorner.find(side1, side2));
    }
    
    private static final CornerJoinFaceStateImpl[] VALUES = CornerJoinFaceStateImpl.values();
    public static final int COUNT = VALUES.length;
    
    /**
     * Sparsely populated - only meaningful states are non-null. For example, cannot
     * also have corners on side with a border.
     */
    private static final CornerJoinFaceStateImpl[] LOOKUP = new CornerJoinFaceStateImpl[256];
    
    static {
        for (CornerJoinFaceStateImpl state : CornerJoinFaceStateImpl.values()) {
            LOOKUP[state.bitFlags] = state;

            ArrayList<CornerJoinFaceStateImpl> subStateList = new ArrayList<CornerJoinFaceStateImpl>();

            if (state == NO_FACE) {
                subStateList.add(NO_FACE);
            } else {
                for (CornerJoinFaceStateImpl subState : CornerJoinFaceStateImpl.values()) {
                    if (subState != NO_FACE && (subState.bitFlags & state.bitFlags & 15) == (subState.bitFlags & 15)) {
                        subStateList.add(subState);
                    }
                }
            }
            state.subStates = subStateList.toArray(new CornerJoinFaceStateImpl[subStateList.size()]);
        }
    }

    private static CornerJoinFaceStateImpl find(int faceBits, int cornerBits) {
        return LOOKUP[(faceBits & 15) | ((cornerBits & 15) << 4)];
    }

    public static CornerJoinFaceStateImpl find(Direction face, SimpleJoinStateImpl join) {
        int faceFlags = 0;

        CornerJoinFaceStateImpl fjs;

        if (join.isJoined(face)) {
            fjs = CornerJoinFaceStateImpl.NO_FACE;
        } else {
            for (int i = 0; i < FaceEdgeHelper.COUNT; i++) {
                final FaceEdge fside = FaceEdgeHelper.fromOrdinal(i);
                if (join.isJoined(fside.toWorld(face))) {
                    faceFlags |= fside.ordinalBit;
                }
            }

            fjs = CornerJoinFaceStateImpl.find(faceFlags, 0);
        }
        return fjs;
    }

    public static CornerJoinFaceStateImpl find(Direction face, BlockNeighbors tests) {
        int faceFlags = 0;
        int cornerFlags = 0;

        CornerJoinFaceStateImpl fjs;

        if (tests.result(face)) {
            fjs = CornerJoinFaceStateImpl.NO_FACE;
        } else {
            for (int i = 0; i < FaceEdgeHelper.COUNT; i++) {
                final FaceEdge fside = FaceEdgeHelper.fromOrdinal(i);
                final Direction joinFace = fside.toWorld(face);
                if (tests.result(joinFace) && !tests.result(BlockEdge.find(face, joinFace))) {
                    faceFlags |= fside.ordinalBit;
                }
            }

            fjs = CornerJoinFaceStateImpl.find(faceFlags, cornerFlags);

            if (fjs.hasCornerTests()) {
                for (FaceCorner corner : fjs.cornerTests()) {
                    if (!tests.result(corner.leftSide.toWorld(face), corner.rightSide.toWorld(face))
                            || tests.result(corner.leftSide.toWorld(face),
                                    corner.rightSide.toWorld(face), face)) {
                        cornerFlags |= corner.ordinalBit;
                    }
                }

                fjs = CornerJoinFaceStateImpl.find(faceFlags, cornerFlags);
            }
        }
        return fjs;
    }
    
    public static CornerJoinFaceStateImpl fromOrdinal(int ordinal) {
        return VALUES[ordinal];
    }
    
    public static void forEach(Consumer<CornerJoinFaceState> consumer) {
        for(CornerJoinFaceStateImpl val: VALUES) {
            consumer.accept(val);
        }
    }
}
