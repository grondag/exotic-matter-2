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

package grondag.xm2.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import grondag.fermion.color.BlockColorMapProvider;
import grondag.fermion.color.Chroma;
import grondag.fermion.color.Hue;
import grondag.fermion.color.Luminance;
import grondag.fermion.serialization.NBTDictionary;
import grondag.fermion.varia.ILocalized;
import grondag.xm2.Xm;
import grondag.xm2.init.SubstanceConfig;
import grondag.fermion.structures.NullHandler;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.block.Material;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.ai.pathing.PathNodeType;

/**
 * Similar to Minecraft Material. Didn't want to tie to that implementation.
 * Determines Minecraft material and other physical properties.
 */
public class BlockSubstance implements ILocalized {
    private static final String NBT_SUBSTANCE = NBTDictionary.claim("substance");

    /**
     * Finite number of substances defined to facilitate bit-wise serialization to
     * client GUIs
     */
    public static final int MAX_SUBSTANCES = 4096;

    private static final HashMap<String, BlockSubstance> allByName = new HashMap<>();
    private static final ArrayList<BlockSubstance> allByOrdinal = new ArrayList<>();
    private static final List<BlockSubstance> allReadOnly = Collections.unmodifiableList(allByOrdinal);

    private static int nextOrdinal = 0;

    public static final BlockSubstance DEFAULT = create("default",
            new SubstanceConfig(1, BlockHarvestTool.ANY, 0, 10, 1.0), Material.EARTH, BlockSoundGroup.WOOL,
            BlockColorMapProvider.INSTANCE.getColorMap(Hue.AZURE, Chroma.WHITE, Luminance.MEDIUM_LIGHT).ordinal);

    public static BlockSubstance deserializeNBT(CompoundTag tag) {
        return NullHandler.defaultIfNull(allByName.get(tag.getString(NBT_SUBSTANCE)), BlockSubstance.DEFAULT);
    }

    public static BlockSubstance fromBytes(PacketByteBuf pBuff) {
        int ordinal = pBuff.readByte();
        return ordinal >= 0 && ordinal < allByOrdinal.size() ? allByOrdinal.get(ordinal) : null;
    }

    
    public static BlockSubstance get(String systemName) {
        return allByName.get(systemName);
    }

    
    public static BlockSubstance get(int ordinal) {
        return ordinal < 0 || ordinal >= allByOrdinal.size() ? null : allByOrdinal.get(ordinal);
    }

    public static BlockSubstance create(String systemName, SubstanceConfig config, Material material, BlockSoundGroup sound,
            int defaultColorMapID, boolean isHyperMaterial) {
        BlockSubstance existing = get(systemName);
        if (existing != null) {
            assert false : "Duplicate substance name";
            Xm.LOG.warn(
                    String.format("Block substance with duplicate name %s not created.  Existing substance with that name be used instead.",
                    systemName));
            return existing;
        }

        return new BlockSubstance(systemName, config, material, sound, defaultColorMapID, isHyperMaterial);
    }

    public static BlockSubstance create(String systemName, SubstanceConfig config, Material material, BlockSoundGroup sound,
            int defaultColorMapID) {
        return create(systemName, config, material, sound, defaultColorMapID, false);
    }

    public static BlockSubstance createHypermatter(String systemName, SubstanceConfig config, Material material,
            BlockSoundGroup sound, int defaultColorMapID) {
        return create(systemName, config, material, sound, defaultColorMapID, true);
    }

    public final Material material;
    public final BlockSoundGroup soundType;

    public final String systemName;
    public final int ordinal;
    public final int hardness;
    public final int resistance;
    public final BlockHarvestTool harvestTool;
    public final int harvestLevel;
    public final int defaultColorMapID;
    public final boolean isHyperMaterial;
    public final boolean isTranslucent;
    public final double walkSpeedFactor;
    public final int flammability;
    public final boolean isBurning;
    public final PathNodeType pathNodeType;

    private BlockSubstance(String systemName, SubstanceConfig substance, Material material, BlockSoundGroup sound,
            int defaultColorMapID, boolean isHyperMaterial) {
        this.systemName = systemName;
        this.ordinal = nextOrdinal++;
        this.material = material;
        this.isHyperMaterial = isHyperMaterial;
        soundType = sound;
        this.defaultColorMapID = defaultColorMapID;
        this.isTranslucent = this.material == Material.GLASS;

        this.hardness = substance.hardness;
        this.resistance = substance.resistance;
        this.harvestTool = substance.harvestTool;
        this.harvestLevel = substance.harvestLevel;
        this.walkSpeedFactor = substance.walkSpeedFactor;
        this.flammability = substance.flammability;
        this.isBurning = substance.isBurning;
        this.pathNodeType = substance.pathNodeType;

        if (this.ordinal < MAX_SUBSTANCES) {
            allByName.put(systemName, this);
            allByOrdinal.add(this);
        } else {
            Xm.LOG.warn(String.format("Block substance limit of %d exceeded.  Substance %s will not be usable.",
                    MAX_SUBSTANCES, systemName));
        }

    }

    @Override
    public String localizedName() {
        return I18n.translate("material." + this.systemName.toLowerCase());
    }

    public void serializeNBT(CompoundTag tag) {
        tag.putString(NBT_SUBSTANCE, this.systemName);
    }

    public void toBytes(PacketByteBuf pBuff) {
        pBuff.writeByte(this.ordinal);
    }

    public static List<BlockSubstance> all() {
        return allReadOnly;
    }
}
