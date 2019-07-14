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

package grondag.xm2.api.connect.world;

import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;

/**
 * Implement to define when a block neighbor should be considered "present and matching" for purposes
 * of computing a join state.  Can also be used for more general purposes to retrieve values with
 * caching and lazy evaluation via {@link BlockNeighbors#result(grondag.xm2.connect.api.model.BlockCorner)}
 * and its variance. <p>
 * 
 * The "from" values will always be derived from the central block. The "to" values will represent
 * a neighboring block.  All inputs benefit from the lazy evaluation and caching provided by {@link BlockNeighbors}.<p>
 * 
 * The model state values will be non-null if {@link BlockNeighbors} was given a state function when it
 * was retrieved. (And if the function returns a non-null value.)  See {@link ModelStateFunction}
 */
@API(status = STABLE)
@FunctionalInterface
public interface BlockTest {
    boolean apply(BlockTestContext context);
}
