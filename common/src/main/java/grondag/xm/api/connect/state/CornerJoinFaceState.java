/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.xm.api.connect.state;

import net.minecraft.core.Direction;

import grondag.xm.orientation.api.FaceCorner;
import grondag.xm.orientation.api.FaceEdge;

/**
 * Describes the connected-texture state of a block face for a face within a
 * {@link CornerJoinState}. Result interpretation may vary depending on the test
 * used to derive the state.
 *
 * <p>Can also be applied to shapes if the shape has some sort of meaningful face
 * with an appearance that varies based on connections.
 *
 * <p>All information is <em>relative</em> to the block face for which this state
 * was returned. You must obtain and retain that information separately - it is
 * not part of this object.
 */
public interface CornerJoinFaceState {
	int ordinal();

	boolean isJoined(FaceEdge side);

	boolean isJoined(Direction toFace, Direction onFace);

	/**
	 * True if connected-texture/shape blocks need to render corner due to
	 * missing/covered block in adjacent corner.
	 */
	boolean needsCorner(FaceCorner corner);

	/**
	 * True if connected-texture/shape blocks need to render corner due to
	 * missing/covered block in adjacent corner.
	 *
	 * <p>Note that to use this version you must know which block face this state is
	 * on.
	 */
	boolean needsCorner(Direction face1, Direction face2, Direction onFace);
}
