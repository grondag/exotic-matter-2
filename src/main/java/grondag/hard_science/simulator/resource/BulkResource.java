package grondag.hard_science.simulator.resource;


import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import grondag.hard_science.matter.Gas;
import grondag.hard_science.matter.IComposition;
import grondag.hard_science.matter.MatterPhase;
import grondag.hard_science.matter.Temperature;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeBulk;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

/**
 * Resources used within device processing or system accounting.
 * Bulk resources have no storage manager and no logistics service. 
 * They do not publish any events and cannot be transported.
 * They CAN be stored in isolated containers.
 */
//public class BulkResource extends IForgeRegistryEntry.Impl<BulkResource> implements IResource<StorageTypeBulk>
public class BulkResource implements IResource<StorageTypeBulk>
{
    private static Map<Fluid, BulkResource> fluidLookup = new HashMap<Fluid, BulkResource>();

    public static BulkResource fromFluid(Fluid fluid)
    {
        return fluidLookup.get(fluid);
    }
    
    private final String systemName;
    public final int color;
//    public final String label;
    private final IComposition composition;
    private final double tempK;
    private final double pressureP;
    private final MatterPhase phase;
    
    /**
     * g/cm3, water ~1.0
     */
    private final double density;
    
    private Fluid fluid;
    private FluidResource resource;
    
    public BulkResource(
            String systemName,
            int color,
//            String label,
            IComposition composition,
            double tempCelsius,
            double pressureAtm,
            MatterPhase phase,
            double density
            )
    {
//        this.setRegistryName(systemName);
        this.systemName = systemName;
        this.color = color;
//        this.label = label;
        this.composition = composition;
        this.tempK = Temperature.celsiusToKelvin(tempCelsius);
        this.pressureP = Gas.atmToPascals(pressureAtm);
        this.phase = phase;
        this.density = density;
    }
    
    /**
     * Use this form for gasses that can
     * have density estimated as an ideal gas.
     */
    public BulkResource(
            String systemName,
            int color,
            IComposition molecule,
            double tempCelsius,
            double pressureAtm
            )
    {
        this(systemName, color, molecule, tempCelsius, pressureAtm, MatterPhase.GAS, 
                Gas.idealGasDensityCA(molecule, tempCelsius, pressureAtm));

    }
    
    public String systemName()
    {
        return this.systemName;
    }
    
    @Override
    public String displayName()
    {
        return I18n.translateToLocal("matter." + this.systemName).trim();
    }

    @Override
    public BulkResourceWithQuantity withQuantity(long quantity)
    {
        return new BulkResourceWithQuantity(this, quantity);
    }
    
    public BulkResourceWithQuantity withLiters(double liters)
    {
        return new BulkResourceWithQuantity(this, (long) (liters * VolumeUnits.LITER.nL));
    }
    
    public BulkResourceWithQuantity withKilograms(double kg)
    {
        return new BulkResourceWithQuantity(this, (long) (kg / this.density * VolumeUnits.LITER.nL));
    }
    
    @Override
    public String toString()
    {
        return this.displayName();
    }

//    @Override
//    public int hashCode()
//    {
//        return this.getRegistryName().hashCode();
//    }
  
    @Override
    public boolean isResourceEqual(@Nullable IResource<?> other)
    {
        return this.equals(other);
    }

    @Override
    public boolean equals(@Nullable Object other)
    {
        if(other == this) return true;
        if(other == null) return false;
        if(other instanceof BulkResource)
        {
            return ((BulkResource)other).systemName().equals(this.systemName());
//            return ((BulkResource)other).getRegistryName().equals(this.getRegistryName());
        }
        return false;
    }
    
    @Override
    public StorageTypeBulk storageType()
    {
        return StorageType.PRIVATE;
    }
    
    /**
     * Null if not a managed resource.
     */
    @Nullable
    public Fluid fluid()
    {
        return this.fluid;
    }

    /**
     * Null if not a managed resource.
     */
    @Nullable
    public FluidResource fluidResource()
    {
        return this.resource;
    }
    
    public MatterPhase phase()
    {
        return this.phase;
    }
    
    public IComposition composition()
    {
        return this.composition;
    }
    
    public double temperatureK()
    {
        return this.tempK;
    }
    
    public double temperatureC()
    {
        return Temperature.kelvinToCelsius(this.tempK);
    }
    
    public double pressureAtm()
    {
        return Gas.pascalsToAtm(this.pressureP);
    }
    
    public double pressurePascals()
    {
        return this.pressureP;
    }
    
    /**
     * g/cm3, water ~1.0
     */
    public double density()
    {
        return this.density;
    }
    
    public double gPerMol()
    {
        return this.composition.weight();
    }
    
    public double molsPerLiter()
    {
        
        // d = kg/l
        // d = (g / 1000)/l
        // g/l = 1000d
        // w = g/m
        // m/g = 1/w
        // m/l == m/g * g/l = 1000d / w
        return this.density * 1000 / this.gPerMol();
    }
    
    public double molsPerKL()
    {
        return this.molsPerLiter() * 1000;
    }
    
    public double litersPerMol()
    {
        // d = kg/l = g/1000/l
        // l/g = 1/1000/d
        // g/m * l/g = l/m = g/m / 1000 / d
        return this.gPerMol() / 1000 / this.density;
    }
    
    public double nlPerMol()
    {
        return this.gPerMol() / this.density * VolumeUnits.MILLILITER.nL;
    }
    
    public double kLPerMol()
    {
        return this.litersPerMol() / 1000;
    }
    
    public BulkResourceWithQuantity defaultStack()
    {
        return this.phase == MatterPhase.SOLID
               ? this.withKilograms(1)
               : this.withLiters(1);
    }
    
    public double kgPerBlock()
    {
        return this.density * 1000;
    }
    
    public double tonnesPerBlock()
    {
        return this.density;
    }
    
    public void registerFluid()
    {
        this.fluid = FluidRegistry.getFluid(systemName);
        if(this.fluid == null)
        {
            this.fluid = new Fluid(this.systemName, this.phase.iconResource, this.phase.iconResource, this.color);
            if(this.phase == MatterPhase.GAS) this.fluid.setGaseous(true);
            this.fluid.setDensity((int) this.density());
            this.fluid.setTemperature((int) this.temperatureK());
            FluidRegistry.registerFluid(this.fluid);
        }
        this.resource = new FluidResource(this.fluid, null);
        fluidLookup.put(this.fluid, this);
    }
}
