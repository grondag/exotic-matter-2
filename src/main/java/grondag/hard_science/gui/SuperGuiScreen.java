package grondag.hard_science.gui;


import static grondag.hard_science.gui.control.GuiControl.CONTROL_EXTERNAL_MARGIN;
import static grondag.hard_science.gui.control.GuiControl.CONTROL_INTERNAL_MARGIN;

import java.io.IOException;
import java.util.ArrayList;

import javax.annotation.Nonnull;

import org.lwjgl.input.Mouse;

import grondag.exotic_matter.block.SuperBlockStackHelper;
import grondag.exotic_matter.model.color.BlockColorMapProvider;
import grondag.exotic_matter.model.color.ColorMap;
import grondag.exotic_matter.model.color.ColorMap.EnumColorMap;
import grondag.exotic_matter.model.painting.PaintLayer;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.model.texture.ITexturePalette;
import grondag.exotic_matter.model.texture.TexturePaletteRegistry;
import grondag.exotic_matter.network.PacketHandler;
import grondag.exotic_matter.placement.SuperItemBlock;
import grondag.exotic_matter.varia.ColorHelper;
import grondag.hard_science.Configurator;
import grondag.hard_science.gui.control.BrightnessSlider;
import grondag.hard_science.gui.control.Button;
import grondag.hard_science.gui.control.ColorPicker;
import grondag.hard_science.gui.control.GuiControl;
import grondag.hard_science.gui.control.ItemPreview;
import grondag.hard_science.gui.control.MaterialPicker;
import grondag.hard_science.gui.control.Panel;
import grondag.hard_science.gui.control.ShapePicker;
import grondag.hard_science.gui.control.TexturePicker;
import grondag.hard_science.gui.control.Toggle;
import grondag.hard_science.gui.control.TranslucencyPicker;
import grondag.hard_science.gui.control.VisibilityPanel;
import grondag.hard_science.gui.control.VisiblitySelector;
import grondag.hard_science.gui.shape.GuiShape;
import grondag.hard_science.gui.shape.GuiShapeFinder;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.network.client_to_server.PacketConfigurePlacementItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SuperGuiScreen extends GuiScreen implements IGuiRenderContext
{

    private static final int BUTTON_ID_CANCEL = 0;
    private static final int BUTTON_ID_ACCEPT = 1;

    @SuppressWarnings("deprecation")
    private final String STR_ACCEPT = I18n.translateToLocal("label.accept");
    @SuppressWarnings("deprecation")
    private final String STR_CANCEL = I18n.translateToLocal("label.cancel");

    private int xStart;
    private int yStart;
    private int xSize;
    private int ySize;

    private MaterialPicker materialPicker;
    private TranslucencyPicker translucencyPicker;
    private ColorPicker[] colorPicker;
    private TexturePicker[] textureTabBar;
    private ShapePicker shapePicker;
    private Toggle[] fullBrightToggle;
    private Toggle outerToggle;
    private Toggle middleToggle;
    private Toggle baseTranslucentToggle;
    private Toggle lampTranslucentToggle;
    private BrightnessSlider brightnessSlider;
    private GuiShape shapeGui;

    private ItemPreview itemPreview;

    private ISuperModelState modelState = null;

    private boolean hasUpdates = false;

    private int buttonWidth;
    private int buttonHeight;

    private Panel mainPanel;
    private int group_base;
    private int group_outer;
    private int group_middle;
    private int group_lamp;
    private int group_shape;
    private int group_material;
    private VisibilityPanel rightPanel;
    
    private GuiControl<?> hoverControl;

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    private void updateItemPreviewState()
    {
        // abort on strangeness
        if(modelState == null)
        {
            return;
        }

        if(shapePicker.getSelected() != modelState.getShape())
        {
            modelState.setShape(shapePicker.getSelected());
            rightPanel.remove(group_shape, 1);
            shapeGui = GuiShapeFinder.findGuiForShape(modelState.getShape(), mc);
            rightPanel.add(group_shape, shapeGui.setVerticalWeight(2));
            // display shape defaults if any
            shapeGui.loadSettings(modelState);
            hasUpdates = true;
        }
        else
        {
            //shape is the same, so can check for shape-specific updates
            hasUpdates = shapeGui.saveSettings(modelState) || hasUpdates;
        }

        if(brightnessSlider.getBrightness() != SuperBlockStackHelper.getStackLightValue(itemPreview.previewItem))
        {
            SuperBlockStackHelper.setStackLightValue(itemPreview.previewItem, brightnessSlider.getBrightness());
            hasUpdates = true;
        }

        if(materialPicker.getSubstance() != SuperBlockStackHelper.getStackSubstance(itemPreview.previewItem))
        {
            SuperBlockStackHelper.setStackSubstance(itemPreview.previewItem, materialPicker.getSubstance());
            baseTranslucentToggle.setVisible(materialPicker.getSubstance().isTranslucent);
            lampTranslucentToggle.setVisible(materialPicker.getSubstance().isTranslucent);
            translucencyPicker.setVisible(materialPicker.getSubstance().isTranslucent);
            hasUpdates = true;
        }

        //FIXME: needs redone now that each paint layer has own alpha
//        Translucency newTrans = materialPicker.getSubstance().isTranslucent
//                ? translucencyPicker.getTranslucency()
//                        : Translucency.CLEAR;
//        if(newTrans == null)
//        {
//            newTrans = Translucency.CLEAR;
//        }
//        if(newTrans != modelState.getTranslucency() )
//        {
//            modelState.setTranslucency(newTrans);
//            hasUpdates = true;
//        }
        
        if(!outerToggle.isOn() && modelState.isLayerEnabled(PaintLayer.OUTER))
        {
            modelState.disableLayer(PaintLayer.OUTER);
            hasUpdates = true;
            textureTabBar[PaintLayer.OUTER.ordinal()].setSelected(null);
        }
        
        if(!middleToggle.isOn() && modelState.isLayerEnabled(PaintLayer.MIDDLE))
        {
            modelState.disableLayer(PaintLayer.MIDDLE);
            hasUpdates = true;
            textureTabBar[PaintLayer.MIDDLE.ordinal()].setSelected(null);
        }

        // needs to happen before toggle checks because it turns on
        // middle/outer toggles when a texture is selected
        for(PaintLayer layer : PaintLayer.VALUES)
        {
            updateItemPreviewSub(layer);
        }

        if(baseTranslucentToggle.isOn() != modelState.isTranslucent(PaintLayer.BASE))
        {
            modelState.setTranslucent(PaintLayer.BASE, baseTranslucentToggle.isOn());
            hasUpdates = true;
        }

        if(lampTranslucentToggle.isOn() != modelState.isTranslucent(PaintLayer.LAMP))
        {
            modelState.setTranslucent(PaintLayer.LAMP, lampTranslucentToggle.isOn());
            hasUpdates = true;
        }
       
        if(hasUpdates)
        {
            this.itemPreview.previewItem.setItemDamage(this.modelState.getMetaData());
            SuperBlockStackHelper.setStackModelState(itemPreview.previewItem, modelState);
        }
    }

    /** Hack to deal with removing color maps from model state */
    @Deprecated
    private int[] lastColorMapID = {-1, -1, -1, -1, -1};
    
    private void updateItemPreviewSub(PaintLayer layer)
    {
        if(lastColorMapID[layer.ordinal()] != colorPicker[layer.ordinal()].getColorMapID())
        {
            updateColors(layer);
            hasUpdates = true;
        }

        ITexturePalette tex = textureTabBar[layer.ordinal()].getSelected();
        if(tex != null && modelState.getTexture(layer) != tex)
        {
            modelState.setTexture(layer, tex);
            hasUpdates = true;
            
            //enable layer if user selected a texture
            if(layer == PaintLayer.OUTER && !outerToggle.isOn())
            {
                outerToggle.setOn(true);
            }
            else if(layer == PaintLayer.MIDDLE && !middleToggle.isOn())
            {
                middleToggle.setOn(true);
            }
        }

        if(((modelState.isEmissive(layer)) != fullBrightToggle[layer.ordinal()].isOn()))
        {
            modelState.setEmissive(layer, fullBrightToggle[layer.ordinal()].isOn());
            updateColors(layer);
            this.colorPicker[layer.ordinal()].showLampColors = modelState.isEmissive(layer);
            hasUpdates = true;
        }
    }

    private void updateColors(PaintLayer layer)
    {
        lastColorMapID[layer.ordinal()] = colorPicker[layer.ordinal()].getColorMapID();
        ColorMap map = BlockColorMapProvider.INSTANCE.getColorMap(lastColorMapID[layer.ordinal()]);
        modelState.setColorMap(layer, map);
        textureTabBar[layer.ordinal()].borderColor = BlockColorMapProvider.INSTANCE
                .getColorMap(colorPicker[layer.ordinal()].getColorMapID())
                .getColor(modelState.isEmissive(layer) ? EnumColorMap.LAMP: EnumColorMap.BASE);
        
        if(layer == PaintLayer.BASE)
        {
            // refresh base color on overlay layers if it has changed
            int baseColor = modelState.isEmissive(PaintLayer.BASE)
                    ? map.getColor(EnumColorMap.LAMP)
                    : map.getColor(EnumColorMap.BASE);

            textureTabBar[PaintLayer.MIDDLE.ordinal()].baseColor = baseColor;
            textureTabBar[PaintLayer.OUTER.ordinal()].baseColor = baseColor;
            textureTabBar[PaintLayer.LAMP.ordinal()].baseColor = baseColor;
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int clickedMouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, clickedMouseButton);
        mainPanel.mouseClick(mc, mouseX, mouseY, clickedMouseButton);
        //        colorPicker.mouseClick(this.mc, mouseX, mouseY);
        //        this.textureTabBar.mouseClick(this.mc, mouseX, mouseY);
        updateItemPreviewState();
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        mainPanel.mouseDrag(mc, mouseX, mouseY, clickedMouseButton);
        //        colorPicker.mouseDrag(this.mc, mouseX, mouseY);
        //        this.textureTabBar.mouseDrag(this.mc, mouseX, mouseY);
        updateItemPreviewState();
    }

    //    @Override
    //    protected void mouseReleased(int mouseX, int mouseY, int state)
    //    {
    //        super.mouseReleased(mouseX, mouseY, state);
    //
    //        updateItemPreviewState();
    //    }

    @Override
    protected void actionPerformed(@Nonnull GuiButton button) throws IOException
    {
        if(hasUpdates && button.id == BUTTON_ID_ACCEPT)
        {
            PacketHandler.CHANNEL.sendToServer(new PacketConfigurePlacementItem(itemPreview.previewItem));
            hasUpdates = false;
        }
        mc.displayGuiScreen((GuiScreen)null);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initGui()
    {
        super.initGui();

        ySize = MathHelper.clamp(height * 4 / 5, fontRenderer.FONT_HEIGHT * 28, height);
        yStart = (height - ySize) / 2;
        xSize = (int) (ySize * GuiUtil.GOLDEN_RATIO);
        xStart = (width - xSize) / 2;

        buttonWidth = Math.max(fontRenderer.getStringWidth(STR_ACCEPT), fontRenderer.getStringWidth(STR_CANCEL)) + CONTROL_INTERNAL_MARGIN + CONTROL_INTERNAL_MARGIN;
        buttonHeight = fontRenderer.FONT_HEIGHT + CONTROL_INTERNAL_MARGIN + CONTROL_INTERNAL_MARGIN;

        int buttonTop = yStart + ySize - buttonHeight - CONTROL_EXTERNAL_MARGIN;
        int buttonLeft = xStart + xSize - CONTROL_EXTERNAL_MARGIN * 2 - buttonWidth * 2;

        // buttons are cleared by super each time
        this.addButton(new Button(BUTTON_ID_ACCEPT, buttonLeft, buttonTop, buttonWidth, buttonHeight, STR_ACCEPT));
        this.addButton(new Button(BUTTON_ID_CANCEL, buttonLeft + CONTROL_EXTERNAL_MARGIN + buttonWidth, buttonTop, buttonWidth, buttonHeight, STR_CANCEL));

        if(itemPreview == null)
        {
            itemPreview = new ItemPreview();
            itemPreview.previewItem = mc.player.getHeldItem(EnumHand.MAIN_HAND).copy();

            if (itemPreview.previewItem == null || !(itemPreview.previewItem.getItem() instanceof SuperItemBlock))
            {
                // Abort on strangeness
                return;
            }
            //            this.meta = this.itemPreview.previewItem.getMetadata();
            modelState = SuperBlockStackHelper.getStackModelState(itemPreview.previewItem);
        }

        // abort on strangeness
        if(modelState == null)
        {
            return;
        }

        if(textureTabBar == null)
        {
            materialPicker = new MaterialPicker();
            shapePicker = new ShapePicker();
            translucencyPicker = new TranslucencyPicker();
            textureTabBar = new TexturePicker[PaintLayer.SIZE];
            colorPicker = new ColorPicker[PaintLayer.SIZE];

            outerToggle = new Toggle().setLabel("Enabled");
            middleToggle = new Toggle().setLabel("Enabled");
            baseTranslucentToggle = new Toggle().setLabel("Translucent");
            lampTranslucentToggle = new Toggle().setLabel("Translucent");
            fullBrightToggle = new Toggle[PaintLayer.SIZE];
            brightnessSlider = new BrightnessSlider(mc);

            for(int i = 0; i < PaintLayer.SIZE; i++)
            {
                TexturePicker t = (TexturePicker) new TexturePicker(new ArrayList<ITexturePalette>(), xStart + CONTROL_EXTERNAL_MARGIN, yStart + 100).setVerticalWeight(5);
                // only render textures with alpha for layers that will render that way in world
                t.renderAlpha = PaintLayer.VALUES[i] == PaintLayer.MIDDLE || PaintLayer.VALUES[i] == PaintLayer.OUTER;
                textureTabBar[i] = t;

                colorPicker[i] = (ColorPicker) new ColorPicker().setHorizontalWeight(5);

                fullBrightToggle[i] = new Toggle().setLabel("Glowing");
            }
        }

        if(mainPanel == null)
        {

            rightPanel = (VisibilityPanel) new VisibilityPanel(true)
                    .setOuterMarginWidth(CONTROL_EXTERNAL_MARGIN)
                    .setInnerMarginWidth(CONTROL_EXTERNAL_MARGIN)
                    .setHorizontalWeight(5)
                    .setBackgroundColor(GuiControl.CONTROL_BACKGROUND);

            group_base = rightPanel.createVisiblityGroup(PaintLayer.BASE.localizedName());
            Panel tempV = new Panel(true).addAll(fullBrightToggle[PaintLayer.BASE.ordinal()], baseTranslucentToggle)
                    .setHorizontalWeight(2);
            Panel tempH = new Panel(false).addAll(tempV, colorPicker[PaintLayer.BASE.ordinal()]).setVerticalWeight(2);
            rightPanel.addAll(group_base, tempH, textureTabBar[PaintLayer.BASE.ordinal()]);
            rightPanel.setVisiblityIndex(group_base);

            group_middle = rightPanel.createVisiblityGroup(PaintLayer.MIDDLE.localizedName());
            tempV = new Panel(true).addAll(middleToggle, fullBrightToggle[PaintLayer.MIDDLE.ordinal()])
                    .setHorizontalWeight(2);
            tempH = new Panel(false).addAll(tempV, colorPicker[PaintLayer.MIDDLE.ordinal()]).setVerticalWeight(2);
            rightPanel.addAll(group_middle, tempH, textureTabBar[PaintLayer.MIDDLE.ordinal()]);

            group_outer = rightPanel.createVisiblityGroup(PaintLayer.OUTER.localizedName());
            tempV = new Panel(true).addAll(outerToggle, fullBrightToggle[PaintLayer.OUTER.ordinal()])
                    .setHorizontalWeight(2);
            tempH = new Panel(false).addAll(tempV, colorPicker[PaintLayer.OUTER.ordinal()]).setVerticalWeight(2);
            rightPanel.addAll(group_outer, tempH,textureTabBar[PaintLayer.OUTER.ordinal()]);
            
            group_lamp = rightPanel.createVisiblityGroup(PaintLayer.LAMP.localizedName());
            tempV = new Panel(true).addAll(fullBrightToggle[PaintLayer.LAMP.ordinal()], lampTranslucentToggle)
                    .setHorizontalWeight(2);
            tempH = new Panel(false).addAll(tempV, colorPicker[PaintLayer.LAMP.ordinal()]).setVerticalWeight(2);
            rightPanel.addAll(group_lamp, tempH, textureTabBar[PaintLayer.LAMP.ordinal()]);

            group_shape = rightPanel.createVisiblityGroup(I18n.translateToLocal("label.shape"));
            rightPanel.add(group_shape, shapePicker.setVerticalWeight(5));
            shapeGui = GuiShapeFinder.findGuiForShape(modelState.getShape(), mc);
            rightPanel.add(group_shape, shapeGui.setVerticalWeight(2));

            group_material = rightPanel.createVisiblityGroup(I18n.translateToLocal("label.material"));
            rightPanel.add(group_material, materialPicker.setVerticalLayout(Layout.PROPORTIONAL));
            rightPanel.add(group_material, translucencyPicker.setVerticalLayout(Layout.PROPORTIONAL));
            rightPanel.add(group_material, brightnessSlider);
            rightPanel.setInnerMarginWidth(GuiControl.CONTROL_INTERNAL_MARGIN * 4);

            VisiblitySelector selector = new VisiblitySelector(rightPanel);

            Panel leftPanel = (Panel) new Panel(true)
                    .setInnerMarginWidth(CONTROL_EXTERNAL_MARGIN)
                    .add(new Panel(true)
                            .setOuterMarginWidth(CONTROL_EXTERNAL_MARGIN)
                            .add(itemPreview)
                            .setBackgroundColor(GuiControl.CONTROL_BACKGROUND)
                            .setVerticalWeight(1))
                    .add(new Panel(true)
                            .add(selector)
                            .setBackgroundColor(GuiControl.CONTROL_BACKGROUND)
                            .setVerticalWeight(3))
                    .setWidth(100)
                    .setHorizontalLayout(Layout.FIXED)
                    .resize(0, 0, (xSize - CONTROL_EXTERNAL_MARGIN) * 2.0 / 7.0, 1);

            mainPanel = (Panel) new Panel(false)
                    .setOuterMarginWidth(0)
                    .setInnerMarginWidth(CONTROL_EXTERNAL_MARGIN)
                    .resize(xStart + CONTROL_EXTERNAL_MARGIN, yStart + CONTROL_EXTERNAL_MARGIN, xSize - CONTROL_EXTERNAL_MARGIN * 2, ySize - CONTROL_EXTERNAL_MARGIN * 3 - buttonHeight);

            mainPanel.addAll(leftPanel, rightPanel);

            loadControlValuesFromModelState();

        }
        else
        {
            ((Panel)mainPanel.get(0)).resize( 0, 0, (xSize - CONTROL_EXTERNAL_MARGIN) * 2.0 / 7.0, 1);
            mainPanel.resize(xStart + CONTROL_EXTERNAL_MARGIN, yStart + CONTROL_EXTERNAL_MARGIN, xSize - CONTROL_EXTERNAL_MARGIN * 2, ySize - CONTROL_EXTERNAL_MARGIN * 3 - buttonHeight);
        }
    }

    private void loadControlValuesFromModelState()
    {
        materialPicker.setSubstance(SuperBlockStackHelper.getStackSubstance(itemPreview.previewItem));
        shapePicker.setSelected(modelState.getShape());
        brightnessSlider.setBrightness(SuperBlockStackHelper.getStackLightValue(itemPreview.previewItem));
        outerToggle.setOn(modelState.isLayerEnabled(PaintLayer.OUTER));
        middleToggle.setOn(modelState.isLayerEnabled(PaintLayer.MIDDLE));
        baseTranslucentToggle.setOn(modelState.isTranslucent(PaintLayer.BASE));
        lampTranslucentToggle.setOn(modelState.isTranslucent(PaintLayer.LAMP));

        baseTranslucentToggle.setVisible(materialPicker.getSubstance().isTranslucent);
        lampTranslucentToggle.setVisible(materialPicker.getSubstance().isTranslucent);

        translucencyPicker.setVisible(materialPicker.getSubstance().isTranslucent);
        //FIXME: needs redone now that each layer has alpha
//        translucencyPicker.setTranslucency(modelState.getTranslucency());

        shapeGui.loadSettings(modelState);
        
        for(PaintLayer layer : PaintLayer.VALUES)
        {
            TexturePicker t = textureTabBar[layer.ordinal()];

            t.clear();
            t.addAll(TexturePaletteRegistry.getTexturesForSubstanceAndPaintLayer(Configurator.SUBSTANCES.flexstone, layer));
            ITexturePalette tex = modelState.getTexture(layer);
            t.setSelected(tex == TexturePaletteRegistry.NONE ? null : modelState.getTexture(layer));
            t.showSelected();
            t.borderColor = modelState.isEmissive(layer)
                    ? ColorHelper.lampColor(modelState.getColorARGB(layer))
                    : modelState.getColorARGB(layer);
            t.baseColor = modelState.isEmissive(PaintLayer.BASE)
                    ? ColorHelper.lampColor(modelState.getColorARGB(PaintLayer.BASE))
                    : modelState.getColorARGB(PaintLayer.BASE);

            ColorPicker c = colorPicker[layer.ordinal()];
            
            //FIXME: not going to work - commented to allow compile after removing color map from model state
            //c.setColorMapID(modelState.getColorMap(layer).ordinal);
            
            c.showLampColors = modelState.isEmissive(layer);
            fullBrightToggle[layer.ordinal()].setOn(modelState.isEmissive(layer));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.hoverControl = null;
        super.drawDefaultBackground();
        drawGradientRect(xStart, yStart, xStart + xSize, yStart + ySize, -1072689136, -804253680);
        mainPanel.drawControl(this, mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
        if(this.hoverControl != null) hoverControl.drawToolTip(this, mouseX, mouseY, partialTicks);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        int i = Mouse.getEventX() * width / mc.displayWidth;
        int j = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        int scrollAmount = Mouse.getEventDWheel();
        if(scrollAmount != 0)
        {
            mainPanel.mouseScroll(i, j, scrollAmount);
            updateItemPreviewState();
        }

    }
    
    @Override
    public Minecraft minecraft()
    {
        return this.mc;
    }

    @Override
    public RenderItem renderItem()
    {
        return this.itemRender;
    }

    @Override
    public GuiScreen screen()
    {
        return this;
    }

    @Override
    public FontRenderer fontRenderer()
    {
        return this.fontRenderer;
    }
    
    @Override
    public void drawToolTip(ItemStack hoverStack, int mouseX, int mouseY)
    {
        this.renderToolTip(hoverStack, mouseX, mouseY);
    }

    @Override
    public void setHoverControl(GuiControl<?> control)
    {
        this.hoverControl = control;
    }

    @Override
    public int mainPanelLeft()
    {
        // Not used for this one
        return 0;
    }

    @Override
    public int mainPanelTop()
    {
        // Not used for this one
        return 0;
    }

    @Override
    public int mainPanelSize()
    {
        // Not used for this one
        return 0;
    }

    @Override
    public void addControls(Panel mainPanel, MachineTileEntity tileEntity)
    {
        // Not used for this one
    }
}
