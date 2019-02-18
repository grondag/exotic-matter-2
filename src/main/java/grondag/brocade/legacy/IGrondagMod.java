package grondag.brocade.legacy;

import java.util.ArrayList;

import org.apache.logging.log4j.Logger;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

public interface IGrondagMod
{
    public String modID();
    
    /**
     * Puts mod ID and . in front of whatever is passed in
     */
    public default String prefixName(String name)
    {
        return String.format("%s.%s", this.modID(), name.toLowerCase());
    }
    
    public default String prefixResource(String name)
    {
        return String.format("%s:%s", this.modID(), name.toLowerCase());
    }
    
    public default ResourceLocation resource(String name)
    {
        return new ResourceLocation(prefixResource(name));
    }
    
    public default void addRecipe(ItemStack output, int index, String recipe, String... inputs)
    {
        String[] lines = new String[3];
        lines[0] = recipe.substring(0, 3);
        lines[1] = recipe.substring(3, 6);
        lines[2] = recipe.substring(6, 9);
        
        final char[] symbols = "ABCDEFGHI".toCharArray();
        int i = 0;
        ArrayList<Object> params = new ArrayList<Object>();
        
        params.add(lines);
        
        for(String s : inputs)
        {
            params.add((Character)symbols[i]);
            params.add(ForgeRegistries.ITEMS.getValue(resource(s)));
            i++;
        }
        
        GameRegistry.addShapedRecipe(
                resource(output.getItem().getRegistryName().toString() + index), 
                resource(this.modID()),
                output,
                params.toArray());
    }
    

    public Logger getLog();
    
    public default void error(String message, Object o1, Object o2)
    {
        this.getLog().error(message, o1, o2);
    }

    public default void error(String message, Throwable t)
    {
        this.getLog().error(message, t);
    }

    public default void error(String message)
    {
        this.getLog().error(message);
    }

    public default void debug(String message, Object...args)
    {
        this.getLog().debug(String.format(message, args));
    }

    public default void debug(String message)
    {
        this.getLog().debug(message);
    }

    public default void info(String message, Object...args)
    {
        this.getLog().info(String.format(message, args));
    }

    public default void info(String message)
    {
       this.getLog().info(message);
    }

    public default void warn(String message, Object...args)
    {
        this.getLog().warn(String.format(message, args));
    }

    public default void warn(String message)
    {
        this.getLog().warn(message);
    }
}
