package grondag.hard_science.gui.control;

import grondag.exotic_matter.block.BlockSubstance;
import grondag.exotic_matter.varia.HorizontalAlignment;
import grondag.exotic_matter.varia.VerticalAlignment;
import grondag.hard_science.gui.GuiUtil;
import grondag.hard_science.gui.IGuiRenderContext;
import grondag.hard_science.init.ModSubstances;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MaterialPicker extends GuiControl<MaterialPicker>
{
    /** dimensions are material and toughness */
    private static BlockSubstance[][]substances = new BlockSubstance[3][3];
    
    @SuppressWarnings("deprecation")
    private final String textMaterial = I18n.translateToLocal("label.material");
    @SuppressWarnings("deprecation")
    private final String textToughness = I18n.translateToLocal("label.toughness");

    private double boxSize;
    private double spacing;
    
    private int materialIndex = NO_SELECTION;
    private int toughnessIndex = NO_SELECTION;
    
    private BlockSubstance substance = BlockSubstance.DEFAULT;

    static
    {
        substances[0][0] = ModSubstances.FLEXSTONE;
        substances[0][1] = ModSubstances.DURASTONE;
        substances[0][2] = ModSubstances.HYPERSTONE;

        substances[1][0] = ModSubstances.FLEXIGLASS;
        substances[1][1] = ModSubstances.DURAGLASS;
        substances[1][2] = ModSubstances.HYPERGLASS;
        
        substances[2][0] = ModSubstances.FLEXWOOD;
        substances[2][1] = ModSubstances.DURAWOOD;
        substances[2][2] = ModSubstances.HYPERWOOD;
    }
    
    public MaterialPicker()
    {
        this.setAspectRatio(2.0 / 7.0);
    }
    
    public void setSubstance(BlockSubstance substance)
    {
        this.materialIndex = NO_SELECTION;
        this.toughnessIndex = NO_SELECTION;
        this.substance = substance;
        
        for(int i = 0; i < 3; i++)
        {
            for(int j = 0; j < 3; j++)
            {
                if(substance == substances[i][j])
                {
                    this.materialIndex = i;
                    this.toughnessIndex = j;
                    return;
                }
            }
        }
    }
    
    public BlockSubstance getSubstance()
    {
        if(this.materialIndex == NO_SELECTION || this.toughnessIndex == NO_SELECTION) return this.substance;
        return substances[this.materialIndex][this.toughnessIndex];
    }
    
    private int getMouseIndex(int mouseX, int mouseY)
    {
        if(mouseX < this.left || mouseX > this.right || mouseY < (this.top + this.height / 2) || mouseY > this.bottom) return NO_SELECTION;
        
        int x = (int) (this.left + boxSize);
        if( mouseX < x) return 0;
        
        x += this.spacing;
        if(mouseX < x) return NO_SELECTION;
        
        x += this.boxSize;
        if(mouseX < x) return 1;
        
        x += this.spacing;
        if(mouseX < x) return NO_SELECTION;
        
        x += this.boxSize;
        if(mouseX < x) return 2;
        
        x += this.spacing + this.spacing;
        if(mouseX < x) return NO_SELECTION;
        
        x += this.boxSize;
        if(mouseX < x) return 3;
        
        x += this.spacing;
        if(mouseX < x) return NO_SELECTION;
        
        x += this.boxSize;
        if(mouseX < x) return 4;
        
        x += this.spacing;
        if(mouseX < x) return NO_SELECTION;
        
        return 5;
    }
    
    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        double halfWidth = this.width / 2;
        double halfHeight = this.height / 2;
        double xMiddle = this.left + halfWidth;
        double yMiddle = this.top + halfHeight;
        
        GuiUtil.drawAlignedStringNoShadow(renderContext.fontRenderer(), textMaterial, (float) this.left, (float) this.top, 
                (float) (halfWidth - this.spacing), (float) halfHeight, TEXT_COLOR_LABEL, HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE);
        
        GuiUtil.drawAlignedStringNoShadow(renderContext.fontRenderer(), textToughness, (float) (xMiddle + spacing), (float) this.top, 
                (float) (halfWidth - this.spacing), (float) halfHeight, TEXT_COLOR_LABEL, HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE);
        
        int mouseIndex = this.getMouseIndex(mouseX, mouseY);
        if(mouseIndex != NO_SELECTION)
        {
            double highlightX = this.left + mouseIndex * (this.boxSize + this.spacing);
            if(mouseIndex > 2) highlightX += this.spacing;
            
            GuiUtil.drawRect(highlightX + 1, yMiddle + 1, highlightX + this.boxSize - 1, this.bottom - 1, GuiControl.BUTTON_COLOR_FOCUS);
        }
        
        if(this.materialIndex != NO_SELECTION)
        {
            GuiUtil.drawBoxWidthHeight(this.left + this.materialIndex * (this.boxSize + this.spacing), yMiddle, boxSize, boxSize, 1, GuiControl.BUTTON_COLOR_ACTIVE);
        }
        
        if(this.toughnessIndex != NO_SELECTION)
        {
            GuiUtil.drawBoxWidthHeight(xMiddle + this.spacing + this.toughnessIndex * (this.boxSize + this.spacing), yMiddle, boxSize, boxSize, 1, GuiControl.BUTTON_COLOR_ACTIVE);
        }

        
        renderContext.minecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        
        double x = this.left + 1;
        double y = yMiddle + 1;
        double size = this.boxSize - 2;
        
        GuiUtil.renderItemAndEffectIntoGui(renderContext, new ItemStack(Item.getItemFromBlock(Blocks.STONE)), x , y, size);
        x += (this.boxSize + this.spacing);
        
        GuiUtil.renderItemAndEffectIntoGui(renderContext, new ItemStack(Item.getItemFromBlock(Blocks.GLASS)), x , y, size);
        x += (this.boxSize + this.spacing);

        GuiUtil.renderItemAndEffectIntoGui(renderContext, new ItemStack(Item.getItemFromBlock(Blocks.LOG)), x , y, size);
        x += (this.boxSize + this.spacing + this.spacing);

        GuiUtil.renderItemAndEffectIntoGui(renderContext, new ItemStack(Items.STONE_PICKAXE), x , y, size);
        x += (this.boxSize + this.spacing);
        
        GuiUtil.renderItemAndEffectIntoGui(renderContext, new ItemStack(Items.IRON_PICKAXE), x , y, size);
        x += (this.boxSize + this.spacing);

        GuiUtil.renderItemAndEffectIntoGui(renderContext, new ItemStack(Items.DIAMOND_PICKAXE), x , y, size);
    }

    @Override
    protected void handleCoordinateUpdate()
    {
        this.boxSize = this.height / 2;
        this.spacing = (this.width - (boxSize * 6)) / 6;
        
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        int mouseIndex = this.getMouseIndex(mouseX, mouseY);
        if(mouseIndex == NO_SELECTION) return;
        
        if(mouseIndex < 3)
        {
            this.materialIndex = mouseIndex;
        }
        else
        {
            this.toughnessIndex = mouseIndex - 3;
        }
    }

    @Override
    protected void handleMouseDrag(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        this.handleMouseClick(mc, mouseX, mouseY, clickedMouseButton);
    }

    @Override
    protected void handleMouseScroll(int mouseX, int mouseY, int scrollDelta)
    {
        // ignore
    }

    @Override
    public void drawToolTip(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        // TODO Auto-generated method stub
        
    }

}
