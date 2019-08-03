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
package grondag.xm.api.allocation;

public interface Reference {
    boolean isImmutable();

    <T extends Reference> T toImmutable();

    public static interface Mutable extends Reference {
        @Override
        default boolean isImmutable() {
            return false;
        }
    }

    public static interface Owned extends Mutable {
        void release();
    }

    public static interface Immutable extends Reference {
        @Override
        default boolean isImmutable() {
            return true;
        }

        @SuppressWarnings("unchecked")
        @Override
        default <T extends Reference> T toImmutable() {
            return (T) this;
        }
    }
}
