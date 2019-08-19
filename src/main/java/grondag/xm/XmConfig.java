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
package grondag.xm;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

@API(status = INTERNAL)
public class XmConfig {
  
    ////////////////////////////////////////////////////
    // EXECUTION
    ////////////////////////////////////////////////////
//        @LangKey("config.execution")
//        @Comment("General settings for game logic execution.")
    public static ExecutionSettings EXECUTION = new ExecutionSettings();

    public static class ExecutionSettings {

//            @Comment({"Maximum number of queued 'operations' to be executed each server tick.",
//            " Operations are submitted by game logic that runs outside the main server thread.",
//            " The size of each operation varies, and some tasks consume more than one op. ",
//            " Try smaller values if seeing tick lag on the server. Some game actions or events may take ",
//            " longer to complete with small values.  Could have indirect effects on client if results in",
//            " large numbers of block update, for example."})
//            @RangeInt(min = 128, max = 1000000)
        public int maxQueuedWorldOperationsPerTick = 4096;
    }

    ////////////////////////////////////////////////////
    // BLOCKS
    ////////////////////////////////////////////////////
//    @LangKey("config.blocks")
//    @Comment("Settings for blocks.")
    public static BlockSettings BLOCKS = new BlockSettings();

    public static class BlockSettings {
//        @Comment("Allow user selection of hidden textures in SuperModel Block GUI. Generally only useful for testing.")
        public boolean showHiddenTextures = false;

//        @Comment("Controls how much detail should be shown if The One Probe is enabled.")
        public ProbeInfoLevel probeInfoLevel = ProbeInfoLevel.BASIC;

        public static enum ProbeInfoLevel {
            BASIC, EXTRA, DEBUG
        }

        // TODO: get rid of this and use assertions instead
//        @Comment("Set true to enable tracing output for block model state.  Can spam the log quite a bit, so leave false unless having problems.")
        public boolean debugModelState = false;

//        @Comment({"Maximum number of block states checked before placing virtual blocks.",
//            " Try smaller values if placing large multi-block regions is causing FPS problems.",
//            " With smaller values, species (connected textures) may not be selected properly ",
//        " for large multi-block placements."})
//        @RangeInt(min = 16, max = 4096)
        public int maxPlacementCheckCount = 512;

//        @Comment({"If true, terrain block quads will be randomly recolored to show tesselation.",
//            " Will only be apparent if block model hasn't already been constructed/cached.",
//            " May also require a chunk rebuild. Only use is for debug and testing."})
        public boolean enableTerrainQuadDebugRender = false;

//        @Comment({"If true, terrain block models will be generated with fewer quads for relatively flat regions.",
//            " Reduces memory consumption and may improve render performance but may also result in minor visual defects."})
        public boolean simplifyTerrainBlockGeometry = true;

//        @Comment({"Approximate maximum number of procedurally generated collision boxes for blocks that use then.",
//            " This is the budget for *optimal* boxes, which are generated in a separate-low priority thread.",
//            " This means there isn't much penalty for using more detailed collision boxes and allowing more ",
//            " detailed boxes can result in more accurate view tracing and projectile/entity collisions.",
//            " It is made configurable in case some future physics mod does collision processing and has ",
//            " performance issues with the larger numbers.  Smaller number are still pretty good. The generator",
//            " logic finds the best possible set of collision boxes (with 1/8 resolution) with the given budget.",
//            "",
//            "Very little (no) testing has been done with non-default values.  It is therefore STRONGLY recommended",
//            "to leave this value as-is unless you have a specific and worthwhile reason to change it."})
//        @RangeInt(min = 4, max = 64)
        public int collisionBoxBudget = 8;

    }

    ////////////////////////////////////////////////////
    // RENDERING
    ////////////////////////////////////////////////////
//    @LangKey("config.render")
//    @Comment("Settings for visual appearance.")

    public static Render RENDER = new Render();

    public static class Render {
//        @Comment("Maxiumum number of quads held in cache for reuse. Higher numbers may result is less memory consuption overall, up to a point.")
//        @RangeInt(min = 0xFFFF, max = 0xFFFFF)
        public int quadCacheSizeLimit = 524280;

//        @RequiresMcRestart
//        @Comment("Collect statistics on quad caching. Used for testing.")
        public boolean enableQuadCacheStatistics = false;

//        @RequiresMcRestart
//        @Comment("Enable animated textures. Set false if animation may be causing memory or performance problems.")
        public boolean enableAnimatedTextures = false;

//        @RequiresMcRestart
//        @Comment("Collect statistics on texture animation. Used for testing.")
        public boolean enableAnimationStatistics = false;

//        @RequiresMcRestart
//        @Comment({"Enable in-memroy texture compression of animated textures if your graphics card supports is.",
//        "Can reduce memory usage by 1GB or more."})
        public boolean enableAnimatedTextureCompression = false;

//        @RequiresMcRestart
//        @Comment("Seconds between output of client-side performance statistics to log, if any are enabled.")
//        @RangeInt(min = 10, max = 600)
        public int clientStatReportingInterval = 10;

//        @Comment({"If true, Dynamic flow block (volcanic lava and basalt) will not render faces occulded by adjacent flow blocks.",
//            " True is harder on CPU and easier on your graphics card/chip.  Experiment if you have FPS problems.",
//        " Probably won't matter on systems with both a fast CPU and fast graphics."})
        public boolean enableFaceCullingOnFlowBlocks = true;

//        @Comment("Debug Feature: draw block boundaries for non-cubic blocks.")
        public boolean debugDrawBlockBoundariesForNonCubicBlocks = false;

//        @Comment("Rendering for blocks about to be placed.")
        public PreviewMode previewSetting = PreviewMode.OUTLINE;

//        @Comment("Debug Feature: output generated font images that are uploaded to texture map.")
        public boolean outputFontTexturesForDebugging = false;

//        @Comment("Debug Feature: output generated color atlas in config folder to show possible hues.")
        public boolean debugOutputColorAtlas = false;

//        @Comment("Debug feature: draw quad outlines and vertex normals for the block currently being looked at.")
        public boolean debugDrawQuadsOutlinesAndNormals = false;

//        @Comment("Debug Feature: collision boxes drown without depth, colorized and inset. Used for seeing collision box generation problems.")
        public boolean debugCollisionBoxes;

        public static void recalcDerived() {
        }

        public static enum PreviewMode {
            NONE, OUTLINE
        }
    }

    ////////////////////////////////////////////////////
    // HYPERSTONE
    ////////////////////////////////////////////////////
//    @LangKey("config.hypermaterial")
//    @Comment("Settings for hyperdimensional building materials.")
    public static HyperStone HYPERSTONE = new HyperStone();

    public static class HyperStone {
//        @Comment("If false, mobs cannot spawn on hyper-dimensional blocks in darkness; similar to slabs.")
        public boolean allowMobSpawning = false;

//        @Comment("If false, normal fires directly above hyper-dimensional blocks are immediately extinguished.")
        public boolean allowFire = false;

//        @Comment("If false, players cannot harvest hyper-dimensional blocks without silk touch - they can be broken but drop rubble.")
        public boolean allowHarvest = false;

//        @Comment("If true, hyper-dimensional blocks can be harvested intact with silk touch. Only matters if allowHarvest is true.")
        public boolean allowSilkTouch = true;

//        @Comment("If true, hyper-dimensional blocks have a chance to lose durability due to damage from entities or explosions.")
        public boolean canBeDamaged;
    }

    public static void recalcDerived() {
        Render.recalcDerived();
    }

    // From Hard Science
    public static boolean logExcavationRenderTracking;
}
