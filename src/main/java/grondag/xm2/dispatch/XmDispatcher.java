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

package grondag.xm2.dispatch;

import grondag.fermion.cache.ObjectSimpleLoadingCache;
import grondag.xm2.api.model.ImmutableModelState;
import grondag.xm2.api.model.ModelState;

// PERF: consider having keys cache their own output: set vs map
// Could this mean holders of keys could trade for an immutable copy
// with direct access to the result?

// custom loading cache is at least 2X faster than guava LoadingCache for our
// use case
public class XmDispatcher extends ObjectSimpleLoadingCache<ModelState, ImmutableModelState> {

    public static final XmDispatcher INSTANCE = new XmDispatcher(0xFFFF);

    public XmDispatcher(int maxSize) {
	super(k -> k.toImmutable(), maxSize);
    }
}
