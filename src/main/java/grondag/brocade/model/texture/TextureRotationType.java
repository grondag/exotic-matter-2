package grondag.brocade.model.texture;

import grondag.fermion.world.Rotation;

public enum TextureRotationType
{
    /** should not be rotated in any way except for a given preset */
    FIXED,
    
    /** Can be rotated if done so consistently within a plane */
    CONSISTENT, 
    
    /** shoudl be rotated randomly */
    RANDOM;
    
    public TextureRotationSetting with(Rotation rotation)
    {
        return new TextureRotationSetting(rotation);
    }
    
    public class TextureRotationSetting
    {
        public final Rotation rotation;
        
        private TextureRotationSetting(Rotation rotation)
        {
            this.rotation = rotation;
        }
        
        public TextureRotationType rotationType()
        {
            return TextureRotationType.this;
        }
    }
}
