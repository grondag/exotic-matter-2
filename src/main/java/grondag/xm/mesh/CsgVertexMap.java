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

package grondag.xm.mesh;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

import grondag.xm.api.mesh.polygon.PolyHelper;
import grondag.xm.api.mesh.polygon.Polygon;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongComparators;

@API(status = INTERNAL)
/**
 * Groups proximate vertices for face recombination.
 */
class CsgVertexMap {

    /**
     * Holds vertices as sequential tuples of x, y, z values
     */
    private final FloatArrayList clusters = new FloatArrayList();
    
    private final IntArrayList vertices = new IntArrayList();
    
    private static final int VERTEX_POLY_ID = 0;
    private static final int VERTEX_INDEX = 1;
    
    /**
     * Holds cluster : vertex map with raw cluster index in high bytes 
     * and raw vertex index in low bytes.  Sorting the list groups by cluster.
     */
    private final LongArrayList clusterMap = new LongArrayList();
    
    private static final long VERTEX_MASK = 0xFFFFFFFFL;
    private static final long CLUSTER_MASK = VERTEX_MASK << 32;
    
    private boolean isClusterMapDirty = false;
    
    private static final int NONE = -1;

    /**
     * Current iteration position within cluster map
     */
    private int clusterSearchIndex = NONE;
    
    /** 
     * Set to cluster map size at start of iteration
     * to avoid incorrect results due to unsorted addtionas
     * at end of map.
     */
    private int clusterSearchLimit = 0;
    
    /**
     * Current iteration position within matches.
     */
    private int matchIndex = NONE;
    
    private final IntArrayList matches = new IntArrayList();
    private final IntArrayList matchBuilder = new IntArrayList();
    
    private static final int MATCH_POLY_A = 0;
    private static final int MATCH_VERTEX_A = 1;
    private static final int MATCH_POLY_B = 2;
    private static final int MATCH_VERTEX_B = 3;
    
    void add(int polyId, Polygon poly) {
        final IntArrayList vertices = this.vertices;
        final LongArrayList clusterMap = this.clusterMap;
        
        final int limit = poly.vertexCount();
        for(int i = 0; i < limit; i++) {
            final int vertexAddress = vertices.size();
            final int cluster = findOrCreateCluster(poly.x(i), poly.y(i), poly.z(i));
            vertices.add(polyId);
            vertices.add(i);
            clusterMap.add(((long)cluster << 32) | vertexAddress);
        }
        
        isClusterMapDirty = true;
    }
    
    private int findOrCreateCluster(float x, float y, float z) {
        final FloatArrayList clusters = this.clusters;
        final int limit = clusters.size();
        int i  = 0;
        while (i < limit) {
            final float dx = x - clusters.getFloat(i++);
            final float dy = y - clusters.getFloat(i++);
            final float dz = z - clusters.getFloat(i++);
            if(dx * dx + dy * dy + dz * dz < PolyHelper.EPSILON) {
                return i - 3;
            }
        }
        clusters.add(x);
        clusters.add(y);
        clusters.add(z);
        return i;
    }
    
    void clear() {
        clusters.clear();
        vertices.clear();
        clusterMap.clear();
        isClusterMapDirty = false;
        clusterSearchIndex = NONE;
        matchIndex = NONE;
        matches.clear();
    }
    
    private void sortClusterMap() {
        if(isClusterMapDirty) {
            clusterMap.sort(LongComparators.NATURAL_COMPARATOR);
            int i = clusterMap.size() - 1;
            // trim deleted values from end to speed searches
            while(i >= 0 && clusterMap.getLong(i) == Long.MAX_VALUE) {
                clusterMap.rem(i--);
            }
            isClusterMapDirty = false;
            clusterSearchLimit = clusterMap.size();
        }
    }
    
    private void populateMatches() {
        matchIndex = NONE;
        while(matchIndex == NONE && clusterSearchIndex != NONE) {
            populateMatchesInner();
        }
    }
    
    private void populateMatchesInner() {
        final IntArrayList matchBuilder = this.matchBuilder;
        matchBuilder.clear();
        
        final LongArrayList clusterMap = this.clusterMap;
        final int limit = clusterSearchLimit;
        if(clusterSearchIndex >= limit) {
            clusterSearchIndex = NONE;
            return;
        }
        
        // position at first non-deleted value
        while(clusterSearchIndex < limit && clusterMap.getLong(clusterSearchIndex) == Long.MAX_VALUE) {
            clusterSearchIndex++;
        }
        
        if(clusterSearchIndex >= limit) {
            clusterSearchIndex = NONE;
            return;
        }
        
//        final int startIndex = clusterSearchIndex;
        long pair = clusterMap.getLong(clusterSearchIndex++);
        final long cluster = pair & CLUSTER_MASK;
        matchBuilder.add((int)(pair & VERTEX_MASK));
        
        while (clusterSearchIndex < limit) {
            pair = clusterMap.getLong(clusterSearchIndex);
            if((pair & CLUSTER_MASK) == cluster) {
                matchBuilder.add((int)(pair & VERTEX_MASK));
                clusterSearchIndex++;
            } else if (pair == Long.MAX_VALUE) {
                clusterSearchIndex++;
            } else {
                break;
            }
        }
        
        final int matchCount = matchBuilder.size();
        if(matchCount < 2) {
            matchIndex = NONE;
            if(matchCount == 0) {
                assert clusterSearchIndex == limit : "CsgVertexMap: abnormal termination";
                clusterSearchIndex = NONE;
            } else {
                assert matchCount == 1 : "CsgVertexMap: negative match count";
                // Having this causes failed matches, not sure why
                // remove unmatched cluster - nothing can come of it
//                clusterMap.set(startIndex, Long.MAX_VALUE);
            }
        } else {
            matchIndex = 0;
            final IntArrayList matches = this.matches;
            matches.clear();
            
            // PERF: could be better to use pairs directly vs building combinations 
            for(int i = 0; i < matchCount; i++) {
                final int matchA = matchBuilder.getInt(i);
                final int idA = vertices.getInt(matchA + VERTEX_POLY_ID);
                final int vertexA = vertices.getInt(matchA + VERTEX_INDEX);
                for(int j = i + 1; j < matchCount; j++) {
                    matches.add(idA);
                    matches.add(vertexA);
                    final int matchB = matchBuilder.getInt(j);
                    matches.add(vertices.getInt(matchB + VERTEX_POLY_ID));
                    matches.add(vertices.getInt(matchB + VERTEX_INDEX));
                }
            }
        }
    }
    
    /**
     * Moves cursor to first potential match.
     * @return true if any potential matches remain
     */
    boolean first() {
        sortClusterMap();
        clusterSearchIndex = 0;
        matchIndex = NONE;
        populateMatches();
        return hasValue();
    }
    
    /**
     * Moves cursor to next potential match.
     * @return false if at end of potential matches
     */
    boolean next() {
        if(hasValue()) {
            matchIndex += 4;
            if(matchIndex >= matches.size()) {
                populateMatches();
                return hasValue();
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
    
    /** removes polys that are part of current match */
    void remove() {
        assert hasValue() : "CsgVertexMap: removal without current value";
        final int a = idA();
        final int b = idB();
        removeInner(a);
        removeInner(b);
    }
    
    private void removeInner(int polyId) {
        final IntArrayList vertices = this.vertices;
        final LongArrayList clusterMap = this.clusterMap;
        
        final int clusterLimit = clusterMap.size();
        for(int i = 0; i < clusterLimit; i++) {
            final int vertexAddress = (int)(clusterMap.getLong(i) & VERTEX_MASK);
            // may encounter negative values due to removals elsewhen
            if(vertexAddress >= 0 && vertices.getInt(vertexAddress) == polyId) {
                clusterMap.set(i, Long.MAX_VALUE);
            }
        }
    }
    
    /**
     * 
     * @return False if cursor methods do not point to a valid entry.
     */
    boolean hasValue() {
        return matchIndex != NONE;
    }
    
    /**
     * @return poly ID of 1st poly in potential match, -1 if no value.
     */
    int idA() {
        final int matchIndex = this.matchIndex;
        return matchIndex == NONE ? NONE : matches.getInt(matchIndex + MATCH_POLY_A);
    }
    
    /**
     * @return Vertex index of 1st poly in potential match, -1 if no value.
     */
    int vertexA() {
        final int matchIndex = this.matchIndex;
        return matchIndex == NONE ? NONE : matches.getInt(matchIndex + MATCH_VERTEX_A);
    }
    
    /**
     * @return poly ID of 2nd poly in potential match, -1 if no value.
     */
    int idB() {
        final int matchIndex = this.matchIndex;
        return matchIndex == NONE ? NONE : matches.getInt(matchIndex + MATCH_POLY_B);
    }
    
    /**
     * @return Vertex index of 2nd poly in potential match, -1 if no value.
     */
    int vertexB() {
        final int matchIndex = this.matchIndex;
        return matchIndex == NONE ? NONE : matches.getInt(matchIndex + MATCH_VERTEX_B);
    }
    
    int bucketSize() {
        return matchBuilder.size();
    }
}
