package grondag.brocade.model.texture;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.EXTBgra;
import org.lwjgl.opengl.EXTTextureCompressionS3TC;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Many of these methods are closely derived from TextureUtils
 * but labeled so that I can understand what is happening.
 * 
 * There are some differences and new methods to improve efficiency
 * and to enable texture compression for large animated textures.
 *
 */
@SideOnly(Side.CLIENT)
public class TextureHelper
{
    
    private static int blockGlTextureId;

    public static int blockGlTextureId()
    {
        return blockGlTextureId;
    }
    
    @SuppressWarnings("null")
    private static ITextureObject blockGlTextureObject = null;
    
    public static ITextureObject blockGlTextureObject()
    {
        return blockGlTextureObject;
    }
    
    public static void postStitch()
    {
        blockGlTextureId = Minecraft.getMinecraft().getTextureMapBlocks().getGlTextureId();
        
        blockGlTextureObject = Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
    }
    
    /**
     * Minecraft only allows up to 4 mimmap levels
     * but just in case this limit is exceeded we 
     * check when it matters because we used a fixed
     * number of buffers to handle compressed texture 
     * upload/download.
     */
    private static final int MAX_LOD_LEVELS = 5;
    
    /**
     * Our largest textures should be no more than 1024x1024
     */
    private static int BUFFER_SIZE = 0x100000;
    
    /**
     * Element 0 is used for operations that are not LOD-specific.
     * Whole array is used for compressed texture upload/download.
     */
    private static final ByteBuffer[] DATA_BUFFER = new ByteBuffer[MAX_LOD_LEVELS];
    
    static
    {
        for(int i = 0; i < MAX_LOD_LEVELS; i++)
        {
            DATA_BUFFER[i] = GLAllocation.createDirectByteBuffer((BUFFER_SIZE >> (i * 2)) * 4);
        }      
    }
    
    
    /**
     * Similar to vanilla version in TextureUtil
     * except that it accepts array of preloaded DMA buffers
     * which can be built via {@link #getBufferedTexture(int[][])}.
     */
    public static void uploadTextureMipmap(ByteBuffer[] imageData, final int width, final int height, final int originX, final int originY, final boolean blur, final boolean clamp)
    {
        for (int i = 0; i < imageData.length; ++i)
        {
            setTextureBlurMipmap(blur, (width >> i) > 1);
            setTextureClamped(clamp);
            if ((width >> i <= 0) || (height >> i <= 0)) break;
            GlStateManager.glTexSubImage2D(GL11.GL_TEXTURE_2D, i, originX >> i, originY >> i, width >> i, height >> i, EXTBgra.GL_BGRA_EXT, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, imageData[i].asIntBuffer());
        }
    }
    
    /**
     * Creates a new, compressed GL texture with given LOD and returns handle to it.
     */
    public static int getCompressedTexture(int[][] imageData, int imageWidth)
    {
        int lodCount = Math.min(imageData.length, MAX_LOD_LEVELS);
        int result = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, result);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

        for(int lod = 0; lod < lodCount; lod++)
        {
            int width = imageWidth >> lod;
            if(width == 0) break;
            setTextureBlurMipmap(false, width > 1);
            setTextureClamped(false);
            copyToBufferPos(imageData[lod], DATA_BUFFER[lod], 0, width * width);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, lod, EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT5_EXT, width, width, 0, EXTBgra.GL_BGRA_EXT, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, DATA_BUFFER[lod]);
        }
        
        return result;
    }
    
    /**
     * Loads previously created compressed texture from {@link #getCompressedTexture(int[][], int)}
     * to given position on the MC Block texture atlas.
     */
    public static void loadCompressedTextureFrame(int textureName, int lodCount, int originX, int originY, int imageWidth)
    {
        lodCount = Math.min(lodCount, MAX_LOD_LEVELS);
        
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureName);
        for(int lod = 0; lod < lodCount; lod++)
        {
            DATA_BUFFER[lod].clear();
            GL11.glGetTexImage(GL11.GL_TEXTURE_2D, lod, EXTBgra.GL_BGRA_EXT, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, DATA_BUFFER[lod]);
        }
        
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Minecraft.getMinecraft().getTextureMapBlocks().getGlTextureId());
        for(int lod = 0; lod < lodCount; lod++)
        {
            if (imageWidth >> lod <= 0) break;
            setTextureBlurMipmap(false, imageWidth >> lod > 1);
            setTextureClamped(false);
            GlStateManager.glTexSubImage2D(GL11.GL_TEXTURE_2D, lod, originX >> lod, originY >> lod, imageWidth >> lod, imageWidth >> lod, EXTBgra.GL_BGRA_EXT, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, DATA_BUFFER[lod].asIntBuffer());
        }
    }
    
    /**
     * Similar to vanilla version in TextureUtil
     */
    public static void setTextureBlurMipmap(final boolean enableBlur, final boolean isMoreThanOnePixel)
    {
        if (enableBlur)
        {
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, isMoreThanOnePixel ? GL11.GL_LINEAR_MIPMAP_LINEAR : GL11.GL_LINEAR);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        }
        else
        {
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, isMoreThanOnePixel ? GL11.GL_NEAREST_MIPMAP_LINEAR : GL11.GL_NEAREST);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }
    }
    
    /**
     * Similar to vanilla version in TextureUtil
     */
    public static void setTextureClamped(boolean isClamped)
    {
        if (isClamped)
        {
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        }
        else
        {
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        }
    }
    
    /**
     * Similar to vanilla version in TextureUtil
     */
    public static void copyToBufferPos(int[] sourceData, ByteBuffer target, int startPos, int length)
    {
        int[] aint = sourceData;

        if (Minecraft.getMinecraft().gameSettings.anaglyph)
        {
            aint = TextureUtil.updateAnaglyph(sourceData);
        }

        target.clear();
        target.asIntBuffer().put(aint, startPos, length);
        target.asIntBuffer().position(0).limit(length);
    }

    public static ByteBuffer[] getBufferedTexture(int[][] imageData)
    {
        ByteBuffer[] result = new ByteBuffer[imageData.length];
        
        for(int i = 0; i < imageData.length; i++)
        {
            result[i] = getBufferedTexture(imageData[i]);
        }
        
        return result;
    }
    
    public static ByteBuffer getBufferedTexture(int[] imageData)
    {
        ByteBuffer result = GLAllocation.createDirectByteBuffer(imageData.length * 4);
        copyToBufferPos(imageData, result, 0, imageData.length);
        return result;
    }
}
