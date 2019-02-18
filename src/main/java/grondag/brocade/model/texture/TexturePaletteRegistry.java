package grondag.brocade.model.texture;

import static grondag.exotic_matter.model.texture.TextureRotationType.FIXED;
import static grondag.exotic_matter.model.texture.TextureRotationType.RANDOM;
import static grondag.exotic_matter.world.Rotation.ROTATE_NONE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.ConfigXM;
import grondag.exotic_matter.ExoticMatter;
import grondag.exotic_matter.IGrondagMod;
import grondag.exotic_matter.init.SubstanceConfig;
import grondag.exotic_matter.model.painting.PaintLayer;
import grondag.exotic_matter.varia.structures.NullHandler;

public class TexturePaletteRegistry implements Iterable<ITexturePalette>
{
    /**
     * Max number of texture palettes that can be registered, loaded and represented in model state.
     */
    public static final int MAX_PALETTES = 4096;
    
    static int nextOrdinal = 0;
    private static final HashMap<String, ITexturePalette> allByName = new HashMap<>();
    private static final ArrayList<ITexturePalette> allByOrdinal = new ArrayList<ITexturePalette>();
    private static final List<ITexturePalette> allReadOnly = Collections.unmodifiableList(allByOrdinal);
    
    /**
     * Important that we have at least one texture and should have ordinal zero so that it is the default value returned by modelState.
     * Is not meant for user selection. For CUT paint layer means should use same texture as base layer.
     * For DETAIL and OVERLAY layers, indicates those layers are disabled. 
     */
     public static final ITexturePalette NONE = addTexturePallette("none", "noise_moderate", 
            new TexturePaletteSpec(ExoticMatter.INSTANCE).withVersionCount(4).withScale(TextureScale.SINGLE).withLayout(TextureLayout.SPLIT_X_8)
            .withRotation(RANDOM.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.BASE_ONLY).withGroups(TextureGroup.ALWAYS_HIDDEN));
    
    
    public static List<ITexturePalette> all()
    {
        return allReadOnly;
    }

    public static ITexturePalette get(String systemName)
    {
        return NullHandler.defaultIfNull(allByName.get(systemName), NONE);
    }
    
    public static ITexturePalette get(int ordinal)
    {
        return ordinal < 0 || ordinal >= allByOrdinal.size() ? NONE : NullHandler.defaultIfNull(allByOrdinal.get(ordinal), NONE);
    }
    
    public static ITexturePalette addTexturePallette(String systemName, String textureBaseName, TexturePaletteSpec info)
    {
        ITexturePalette result = new TexturePallette(systemName, textureBaseName, info);
        addToCollections(result);
        return result;
    }
    
    public static ITexturePalette addZoomedPallete(ITexturePalette source)
    {
        ITexturePalette result = new TexturePallette(
                source.systemName() + ".zoom", 
                source.textureBaseName(),
                new TexturePaletteSpec(source)
                    .withZoomLevel(source.zoomLevel() + 1)
                    .withScale(source.textureScale().zoom()));
        addToCollections(result);
        return result;
    }

    private static void addToCollections(ITexturePalette palette)
    {
        allByOrdinal.add(palette);
        allByName.put(palette.systemName(), palette);
    }
    public int size() { return allByOrdinal.size(); }

    public boolean isEmpty() { return allByOrdinal.isEmpty(); }

    public boolean contains(Object o) { return allByOrdinal.contains(o); }
   
    @Override
    public Iterator<ITexturePalette> iterator() { return allByOrdinal.iterator(); }
   
    public static List<ITexturePalette> getTexturesForSubstanceAndPaintLayer(SubstanceConfig substance, PaintLayer layer)
    {
        int searchFlags = 0;
        switch(layer)
        {
        case BASE:
        case CUT:
        case LAMP:
            searchFlags = TextureGroup.STATIC_TILES.bitFlag | TextureGroup.DYNAMIC_TILES.bitFlag;
            if(ConfigXM.BLOCKS.showHiddenTextures) searchFlags |= TextureGroup.HIDDEN_TILES.bitFlag;
            break;
    
        case MIDDLE:
        case OUTER:
            searchFlags = TextureGroup.STATIC_DETAILS.bitFlag | TextureGroup.DYNAMIC_DETAILS.bitFlag
                         | TextureGroup.STATIC_BORDERS.bitFlag | TextureGroup.DYNAMIC_BORDERS.bitFlag;
            
            if(ConfigXM.BLOCKS.showHiddenTextures) 
                    searchFlags |= (TextureGroup.HIDDEN_DETAILS.bitFlag | TextureGroup.HIDDEN_BORDERS.bitFlag);
            
            break;
            
        default:
            break;
        
        }
        
        ImmutableList.Builder<ITexturePalette> builder = ImmutableList.builder();
        for(ITexturePalette t : allByOrdinal)
        {
            if((t.textureGroupFlags() & searchFlags) != 0)
            {
                builder.add(t);
            }
        }
        
        return builder.build();
    }

    public static ITexturePalette addBorderRandom(IGrondagMod mod, String textureName, boolean allowTile, boolean renderNoBorderAsTile)
    {
        return addTexturePallette(textureName, textureName, 
                new TexturePaletteSpec(mod).withVersionCount(4).withScale(TextureScale.SINGLE).withLayout(TextureLayout.BORDER_13)
                .withRotation(FIXED.with(ROTATE_NONE))
                .withRenderIntent(allowTile ? TextureRenderIntent.BASE_OR_OVERLAY_NO_CUTOUT : TextureRenderIntent.OVERLAY_ONLY)
                .withGroups( allowTile ? TextureGroup.STATIC_BORDERS : TextureGroup.STATIC_TILES, TextureGroup.STATIC_BORDERS)
                .withRenderNoBorderAsTile(renderNoBorderAsTile));
    }

    public static ITexturePalette addBorderSingle(IGrondagMod mod, String textureName)
    {
        return addTexturePallette(textureName, textureName, 
                new TexturePaletteSpec(mod).withVersionCount(1).withScale(TextureScale.SINGLE).withLayout(TextureLayout.BORDER_13)
                .withRotation(FIXED.with(ROTATE_NONE)).withRenderIntent(TextureRenderIntent.OVERLAY_ONLY).withGroups(TextureGroup.STATIC_BORDERS));
    }
}
