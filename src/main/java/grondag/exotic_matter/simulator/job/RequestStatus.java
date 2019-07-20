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
package grondag.exotic_matter.simulator.job;

public enum RequestStatus {
    /** Request is being constructed, scheduled, etc. Status not yet meaningful. */
    NEW(false),

    /**
     * Request is waiting for planned external dependencies (schedule, bulkResource
     * availability, target capacity) to be met
     */
    WAITING(false),

    /**
     * Request dependencies have been met and it is ready to be worked.
     */
    READY(false),

    /**
     * Request has been claimed by a worker and is being actively worked.
     */
    ACTIVE(false),

    /**
     * Request is done. Ain't gonna do any more stuff. Move along now.
     */
    COMPLETE(true),

    /**
     * Request was cancelled and clean up completed. No more activity will happen.
     */
    CANCELLED(true),

    /**
     * Sometimes bad things happen to good Requests...
     */
    ABEND(true);

    /** true if this status should result in dependent requests being notified */
    public final boolean isTerminated;

    private RequestStatus(boolean notifyDependents) {
        this.isTerminated = notifyDependents;
    }

    /**
     * True if this request is complete and dependent requests can start.
     */
    public boolean isComplete() {
        return this == COMPLETE;
    }

    /**
     * True if this request has stopped but did not complete normally.
     */
    public boolean didEndWithoutCompleting() {
        return this.isTerminated && this != COMPLETE;
    }
}
