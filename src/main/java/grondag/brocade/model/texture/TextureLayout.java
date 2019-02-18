package grondag.brocade.model.texture;

import static grondag.exotic_matter.model.state.ModelStateData.STATE_FLAG_NEEDS_CORNER_JOIN;
import static grondag.exotic_matter.model.state.ModelStateData.STATE_FLAG_NEEDS_MASONRY_JOIN;
import static grondag.exotic_matter.model.state.ModelStateData.STATE_FLAG_NEEDS_SPECIES;
import static grondag.exotic_matter.model.state.ModelStateData.STATE_FLAG_NONE;

import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum TextureLayout
{
    /**
     * Separate random tiles with naming convention base_j_i where i is 0-7 and j is 0 or more.
     */
    SPLIT_X_8 (STATE_FLAG_NONE)
    {
        @Override
        public final String buildTextureName(TexturePallette texture, int version, int index)
        {
            return buildTextureName_X_8(texture, index);
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public void prestitch(TexturePallette texture, Consumer<String> stitcher)
        {
            for (int i = 0; i < texture.textureVersionCount; i++)
            {
                stitcher.accept(buildTextureName_X_8(texture, i));
            }
        }  
    },
    
    /**
     * Single square file with optional versions. If more than one version, 
     * file names should have a 0-based _x suffix.
     */
    SIMPLE (STATE_FLAG_NONE),
    
    /**
     * Separate files with naming convention same as SPLIT_X_8 except only the start 13 textures
     * out of every 16 are used for borders.  Texture 14 contains the face that should be
     * rendered if the border is rendered in the solid render layer.  It is IMPORTANT that texture
     * 14 have a solid alpha channel - otherwise mipmap generation will be borked.  The solid face
     * won't be used at all if rendering in a non-solid layer. 
     * Files won't exist or will be blank for 14 and 15.
     */       
    BORDER_13 (STATE_FLAG_NEEDS_CORNER_JOIN | STATE_FLAG_NEEDS_SPECIES, 14)
    {
        @Override
        public final String buildTextureName(TexturePallette texture, int version, int index)
        {
            return buildTextureName_X_8(texture, version * this.blockTextureCount + index);
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public void prestitch(TexturePallette texture, Consumer<String> stitcher)
        {          
            // last texture (no border) only needed if indicated
            final int texCount = texture.renderNoBorderAsTile 
                ? this.textureCount
                : this.textureCount -1;
            
            for (int i = 0; i < texture.textureVersionCount; i++)
            {
                for(int j = 0; j < texCount; j++)
                  {
                    stitcher.accept(buildTextureName_X_8(texture, i * BORDER_13.blockTextureCount + j));
                  }
            }
        }
        
        @Override
        public String sampleTextureName(TexturePallette texture)
        {
            return this.buildTextureName(texture, 0, 4);
        }
    },
    
    /**
     * Separate files with naming convention same as SPLIT_X_8 except only the start 5 textures
     * out of every 8. Files won't exist or will be blank for 5-7.
     */ 
    MASONRY_5 (STATE_FLAG_NEEDS_CORNER_JOIN | STATE_FLAG_NEEDS_MASONRY_JOIN | STATE_FLAG_NEEDS_SPECIES, 5)
    {
        @Override
        public final String buildTextureName(TexturePallette texture, int version, int index)
        {
            return buildTextureName_X_8(texture, version * this.blockTextureCount + index);
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public void prestitch(TexturePallette texture, Consumer<String> stitcher)
        {
            for (int i = 0; i < texture.textureVersionCount; i++)
            {
                    for(int j = 0; j < this.textureCount; j++)
                    {
                        stitcher.accept(buildTextureName_X_8(texture, i * this.blockTextureCount + j));
                    }
            }
        }  
    },
    
    /**
     * Animated big textures stored as series of .jpg files
     */
    BIGTEX_ANIMATED (STATE_FLAG_NONE),
    
    /**
     * Compact border texture on format, typically with multiple variants.
     * Each quadrant of the texture represents one quadrant of a face that can be connected.
     * All are present on same image. Each quadrant must be able to connect
     * with other quadrants in any (connecting) rotation or texture variation.<p>
     * 
     * Follows same naming convention as {@link #SIMPLE}.
     */
    QUADRANT_CONNECTED (STATE_FLAG_NEEDS_CORNER_JOIN | STATE_FLAG_NEEDS_SPECIES);
    
    private static String buildTextureName_X_8(TexturePallette texture, int offset)
    {
        return texture.mod.modID() + ":blocks/" + texture.textureBaseName + "_" + (offset >> 3) + "_" + (offset & 7);
    }
    
    private TextureLayout( int stateFlags)
    {
        this(stateFlags, 1);
    }
    
    private TextureLayout( int stateFlags, int textureCount)
    {
        this.modelStateFlag = stateFlags;
        this.textureCount = textureCount;
        this.blockRowCount = (textureCount + 7) / 8;
        this.blockTextureCount = blockRowCount * 8;
    }
    
    /** identifies the world state needed to drive texture random rotation/selection */
    public final int modelStateFlag;
    
    /**
     * Textures per variant in this layout.
     */
    public final int textureCount;
    
    /**
     * If the texture is arranged as blocks with primary and secondary numbers, <br>
     * The number of distinct primary values per variant.<p>
     * 
     * Equivalently if the first number is row, the second number is column, and
     * this is the number of rows. 
     * 
     */
    public final int blockRowCount;
    
    /**
     * Count of texture positions per variant, assuming 8-column rows,
     * is simply {@link #blockRowCount} * 8.
     */
    public final int blockTextureCount;

    /**
     * Default implementation just prepends the folder. Suitable for single-file textures.
     * If textures have multiple versions, names should have a zero-based _x suffix
     */
    public String buildTextureName(TexturePallette texture, int version, int index)
    {
        return texture.mod.modID() + ":blocks/" + 
                (texture.textureVersionCount == 1
                    ? texture.textureBaseName
                    : (texture.textureBaseName + "_" + version));
    }

    @SideOnly(Side.CLIENT)
    public void prestitch(TexturePallette texture, Consumer<String> stitcher)
    {
        for (int i = 0; i < texture.textureVersionCount; i++)
        {
            stitcher.accept(this.buildTextureName(texture, i, 0));
        }
    }  

    public String sampleTextureName(TexturePallette texture)
    {
        return this.buildTextureName(texture, 0, 0);
    }

    /**
     * Note this currently doesn't set UV coordinates to give a to-scale, appropriate portion of the 
     * texture in the sample. It's always the whole texture. Doesn't matter in most cases, but
     * potentially a future enhancement.
     */
    @SideOnly(Side.CLIENT)
    public EnhancedSprite createSampleSprite(TexturePallette texture)
    {
        EnhancedSprite result = (EnhancedSprite)Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(sampleTextureName(texture));
        return result;
    }
}
