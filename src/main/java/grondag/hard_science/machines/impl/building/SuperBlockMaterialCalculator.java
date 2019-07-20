package grondag.hard_science.machines.impl.building;

import grondag.exotic_matter.block.BlockSubstance;
import grondag.exotic_matter.model.painting.PaintLayer;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.varia.ColorHelper;
import grondag.exotic_matter.varia.ColorHelper.CMY;
import grondag.exotic_matter.varia.Useful;
import grondag.hard_science.init.ModSubstances;
import grondag.hard_science.matter.MatterUnits;
import grondag.hard_science.matter.VolumeUnits;
import net.minecraft.util.math.BlockPos;

public class SuperBlockMaterialCalculator
{
    public final long resinA_nL;
    public final long resinB_nL;
    public final long filler_nL;
    public final long nanoLights_nL;
    public final long cyan_nL;
    public final long magenta_nL;
    public final long yellow_nL;
    public final long TiO2_nL;
    public final BlockSubstance actualSubtance;
    
    public SuperBlockMaterialCalculator(ISuperModelState modelState, BlockSubstance requestedSubstance, int lightValue)
    {
        // TODO: require minimal nanolights if has glow rendering but no lights
        this.nanoLights_nL = lightValue > 0 ? MatterUnits.nL_NANO_LIGHTS_PER_BLOCK : 0;
        
        final long volume = (long) (Useful.volumeAABB(modelState.collisionBoxes(BlockPos.ORIGIN)) * MatterUnits.nL_ONE_BLOCK);
        
        if(requestedSubstance == ModSubstances.DURAWOOD
               || requestedSubstance == ModSubstances.FLEXWOOD
               || requestedSubstance == ModSubstances.HYPERWOOD)
        {
            this.actualSubtance = ModSubstances.FLEXWOOD;
            this.filler_nL = 0;
            final long halfResinVolume = (long) ((volume - this.nanoLights_nL) * MatterUnits.RESIN_WOOD_FRACTION_BY_VOLUME / 2);
            this.resinA_nL = halfResinVolume;
            this.resinB_nL = halfResinVolume;
        }
        else if(requestedSubstance == ModSubstances.DURAGLASS
                || requestedSubstance == ModSubstances.FLEXIGLASS
                || requestedSubstance == ModSubstances.HYPERGLASS)
         {
            final long halfResinVolume = (long) ((volume - this.nanoLights_nL) / 2);
            this.actualSubtance = ModSubstances.FLEXIGLASS;
            this.filler_nL = 0;
            this.resinA_nL = halfResinVolume;
            this.resinB_nL = halfResinVolume;
         }
        else
        {
            this.actualSubtance = ModSubstances.FLEXSTONE;
            this.filler_nL = volume;
            final long halfResinVolume = (long) ((volume - this.nanoLights_nL) * MatterUnits.FILLER_VOID_RATIO / 2);
            this.resinA_nL = halfResinVolume;
            this.resinB_nL = halfResinVolume;
        }
       
        
        CMY cmy = ColorHelper.cmy(modelState.getColorARGB(PaintLayer.BASE));
        
        int basis = 5;
        float cyan = cmy.cyan * basis;
        float magenta = cmy.magenta * basis;
        float yellow = cmy.yellow * basis;
        
        if(modelState.isLayerEnabled(PaintLayer.MIDDLE))
        {
            basis++;
            cmy = ColorHelper.cmy(modelState.getColorARGB(PaintLayer.MIDDLE));
            if(cmy.cyan != 0) cyan += cmy.cyan;
            if(cmy.magenta != 0) magenta += cmy.magenta;
            if(cmy.yellow != 0) yellow += cmy.yellow;
        }
        
        if(modelState.isLayerEnabled(PaintLayer.OUTER))
        {
            basis++;
            cmy = ColorHelper.cmy(modelState.getColorARGB(PaintLayer.OUTER));
            if(cmy.cyan != 0) cyan += cmy.cyan;
            if(cmy.magenta != 0) magenta += cmy.magenta;
            if(cmy.yellow != 0) yellow += cmy.yellow;
        }

        if(modelState.hasLampSurface())
        {
            basis++;
            cmy = ColorHelper.cmy(modelState.getColorARGB(PaintLayer.LAMP));
            if(cmy.cyan != 0) cyan += cmy.cyan;
            if(cmy.magenta != 0) magenta += cmy.magenta;
            if(cmy.yellow != 0) yellow += cmy.yellow;
        }

        cyan = cyan / basis;
        magenta = magenta / basis;
        yellow = yellow / basis;
        
        // Dye consumption should by driven by surface area instead of volume.
        // We assume the shape is a cube for this purpose - some shapes could have higher surface areas
        double surfaceArea_M2 = Useful.squared(Math.cbrt((double) volume / MatterUnits.nL_ONE_BLOCK)) * 6;
        
        double pigmentVolume_nL =  surfaceArea_M2 / MatterUnits.M2_PIGMENT_COVERAGE_SQUARE_METERS_PER_LITER * VolumeUnits.LITER.nL;
        double pigmentVolumePerComponent_nL = pigmentVolume_nL / 3;

        
        this.cyan_nL = cyan > 0 ? (long) (cyan * pigmentVolumePerComponent_nL) : 0;
        this.magenta_nL = magenta > 0 ? (long) (magenta * pigmentVolumePerComponent_nL) : 0;
        this.yellow_nL = yellow > 0 ? (long) (yellow * pigmentVolumePerComponent_nL) : 0;
        
        // Perfect white would be all TiO2 and black would be none
        // In real world would not use CMY pigments to get block, 
        // but in game assuming this is cost-effective.
        this.TiO2_nL = (long) pigmentVolume_nL - this.cyan_nL - this.magenta_nL - this.yellow_nL;
    }
}