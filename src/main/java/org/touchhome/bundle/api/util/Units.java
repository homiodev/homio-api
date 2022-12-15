package org.touchhome.bundle.api.util;

import tech.units.indriya.AbstractSystemOfUnits;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.format.SimpleUnitFormat;
import tech.units.indriya.function.ExpConverter;
import tech.units.indriya.function.LogConverter;
import tech.units.indriya.function.MultiplyConverter;
import tech.units.indriya.unit.AlternateUnit;
import tech.units.indriya.unit.ProductUnit;
import tech.units.indriya.unit.TransformedUnit;

import javax.measure.BinaryPrefix;
import javax.measure.MetricPrefix;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.*;
import javax.measure.spi.SystemOfUnits;
import java.math.BigInteger;

interface ArealDensity extends Quantity<ArealDensity> {
}

interface DataAmount extends Quantity<DataAmount> {
}

interface DataTransferRate extends Quantity<DataTransferRate> {
}

interface Density extends Quantity<Density> {
}

interface ElectricConductivity extends Quantity<ElectricConductivity> {
}

interface Intensity extends Quantity<Intensity> {
}

interface VolumetricFlowRate extends Quantity<VolumetricFlowRate> {
}

public final class Units extends AbstractSystemOfUnits {
    public static final Unit<Acceleration> METRE_PER_SQUARE_SECOND;
    public static final Unit<Acceleration> STANDARD_GRAVITY;
    public static final Unit<AmountOfSubstance> MOLE;
    public static final Unit<Volume> LITRE;
    public static final Unit<AmountOfSubstance> DEUTSCHE_HAERTE;
    public static final Unit<Angle> DEGREE_ANGLE;
    public static final Unit<Angle> RADIAN;
    public static final Unit<ArealDensity> DOBSON_UNIT;
    public static final Unit<CatalyticActivity> KATAL;
    public static final Unit<Density> KILOGRAM_PER_CUBICMETRE;
    public static final Unit<Density> MICROGRAM_PER_CUBICMETRE;
    public static final Unit<Dimensionless> ONE;
    public static final Unit<Dimensionless> PERCENT;
    public static final Unit<Dimensionless> PARTS_PER_BILLION;
    public static final Unit<Dimensionless> PARTS_PER_MILLION;
    public static final Unit<Dimensionless> DECIBEL;
    public static final Unit<ElectricCurrent> AMPERE;
    public static final Unit<ElectricCapacitance> FARAD;
    public static final Unit<ElectricCharge> COULOMB;
    public static final Unit<ElectricCharge> AMPERE_HOUR;
    public static final Unit<ElectricCharge> MILLIAMPERE_HOUR;
    public static final Unit<ElectricConductance> SIEMENS;
    public static final Unit<ElectricConductivity> SIEMENS_PER_METRE;
    public static final Unit<ElectricInductance> HENRY;
    public static final Unit<ElectricPotential> VOLT;
    public static final Unit<ElectricResistance> OHM;
    public static final Unit<Energy> JOULE;
    public static final Unit<Energy> WATT_SECOND;
    public static final Unit<Energy> WATT_HOUR;
    public static final Unit<Energy> KILOWATT_HOUR;
    public static final Unit<Energy> MEGAWATT_HOUR;
    public static final Unit<Power> VAR;
    public static final Unit<Power> KILOVAR;
    public static final Unit<Energy> VAR_HOUR;
    public static final Unit<Energy> KILOVAR_HOUR;
    public static final Unit<Power> VOLT_AMPERE;
    public static final Unit<Power> KILOVOLT_AMPERE;
    public static final Unit<Energy> VOLT_AMPERE_HOUR;
    public static final Unit<Force> NEWTON;
    public static final Unit<Frequency> HERTZ;
    public static final Unit<Intensity> IRRADIANCE;
    public static final Unit<Intensity> MICROWATT_PER_SQUARE_CENTIMETRE;
    public static final Unit<Illuminance> LUX;
    public static final Unit<LuminousFlux> LUMEN;
    public static final Unit<LuminousIntensity> CANDELA;
    public static final Unit<MagneticFlux> WEBER;
    public static final Unit<MagneticFluxDensity> TESLA;
    public static final Unit<Power> WATT;
    public static final Unit<Power> DECIBEL_MILLIWATTS;
    public static final Unit<Pressure> MILLIMETRE_OF_MERCURY;
    public static final Unit<Pressure> BAR;
    public static final Unit<Pressure> MILLIBAR;
    public static final Unit<Radioactivity> BECQUEREL;
    public static final Unit<Density> BECQUEREL_PER_CUBIC_METRE;
    public static final Unit<RadiationDoseAbsorbed> GRAY;
    public static final Unit<RadiationDoseEffective> SIEVERT;
    public static final Unit<Speed> MILLIMETRE_PER_HOUR;
    public static final Unit<Speed> METRE_PER_SECOND;
    public static final Unit<Speed> KNOT;
    public static final Unit<SolidAngle> STERADIAN;
    public static final Unit<Temperature> KELVIN;
    public static final Unit<Time> SECOND;
    public static final Unit<Time> MINUTE;
    public static final Unit<Time> HOUR;
    public static final Unit<Time> DAY;
    public static final Unit<Time> WEEK;
    public static final Unit<Time> YEAR;
    public static final Unit<VolumetricFlowRate> LITRE_PER_MINUTE;
    public static final Unit<VolumetricFlowRate> CUBICMETRE_PER_SECOND;
    public static final Unit<VolumetricFlowRate> CUBICMETRE_PER_MINUTE;
    public static final Unit<VolumetricFlowRate> CUBICMETRE_PER_HOUR;
    public static final Unit<VolumetricFlowRate> CUBICMETRE_PER_DAY;
    public static final Unit<DataAmount> BIT;
    public static final Unit<DataAmount> KILOBIT;
    public static final Unit<DataAmount> MEGABIT;
    public static final Unit<DataAmount> GIGABIT;
    public static final Unit<DataAmount> TERABIT;
    public static final Unit<DataAmount> PETABIT;
    public static final Unit<DataAmount> BYTE;
    public static final Unit<DataAmount> OCTET;
    public static final Unit<DataAmount> KILOBYTE;
    public static final Unit<DataAmount> MEGABYTE;
    public static final Unit<DataAmount> GIGABYTE;
    public static final Unit<DataAmount> TERABYTE;
    public static final Unit<DataAmount> PETABYTE;
    public static final Unit<DataAmount> KIBIBYTE;
    public static final Unit<DataAmount> MEBIBYTE;
    public static final Unit<DataAmount> GIBIBYTE;
    public static final Unit<DataAmount> TEBIBYTE;
    public static final Unit<DataAmount> PEBIBYTE;
    public static final Unit<DataAmount> KIBIOCTET;
    public static final Unit<DataAmount> MEBIOCTET;
    public static final Unit<DataAmount> GIBIOCTET;
    public static final Unit<DataAmount> TEBIOCTET;
    public static final Unit<DataAmount> PEBIOCTET;
    public static final Unit<DataTransferRate> BIT_PER_SECOND;
    public static final Unit<DataTransferRate> KILOBIT_PER_SECOND;
    public static final Unit<DataTransferRate> MEGABIT_PER_SECOND;
    public static final Unit<DataTransferRate> GIGABIT_PER_SECOND;
    public static final Unit<DataTransferRate> TERABIT_PER_SECOND;
    private static final Units INSTANCE = new Units();

    static {
        METRE_PER_SQUARE_SECOND = addUnit(tech.units.indriya.unit.Units.METRE_PER_SQUARE_SECOND);
        STANDARD_GRAVITY = addUnit(METRE_PER_SQUARE_SECOND.multiply(9.80665));
        MOLE = addUnit(tech.units.indriya.unit.Units.MOLE);
        LITRE = addUnit(tech.units.indriya.unit.Units.LITRE);
        DEUTSCHE_HAERTE = addUnit(new TransformedUnit("°dH", MetricPrefix.MILLI(MOLE).divide(LITRE), MultiplyConverter.of(5.6)));
        DEGREE_ANGLE = addUnit(new TransformedUnit("deg", tech.units.indriya.unit.Units.RADIAN,
                MultiplyConverter.ofPiExponent(1).concatenate(MultiplyConverter.ofRational(1L, 180L))));
        RADIAN = addUnit(tech.units.indriya.unit.Units.RADIAN);
        DOBSON_UNIT = addUnit(new ProductUnit(MetricPrefix.MILLI(tech.units.indriya.unit.Units.MOLE).multiply(0.4462)
                .divide(tech.units.indriya.unit.Units.SQUARE_METRE)));
        KATAL = addUnit(tech.units.indriya.unit.Units.KATAL);
        KILOGRAM_PER_CUBICMETRE = addUnit(new ProductUnit(
                tech.units.indriya.unit.Units.KILOGRAM.divide(tech.units.indriya.unit.Units.CUBIC_METRE)));
        MICROGRAM_PER_CUBICMETRE = addUnit(new TransformedUnit(KILOGRAM_PER_CUBICMETRE,
                MultiplyConverter.ofRational(BigInteger.ONE, BigInteger.valueOf(1000000000L))));
        ONE = addUnit(AbstractUnit.ONE);
        PERCENT = addUnit(tech.units.indriya.unit.Units.PERCENT);
        PARTS_PER_BILLION =
                addUnit(new TransformedUnit(ONE, MultiplyConverter.ofRational(BigInteger.ONE, BigInteger.valueOf(1000000000L))));
        PARTS_PER_MILLION =
                addUnit(new TransformedUnit(ONE, MultiplyConverter.ofRational(BigInteger.ONE, BigInteger.valueOf(1000000L))));
        DECIBEL = addUnit(ONE.transform(
                (new LogConverter(10.0)).inverse().concatenate(MultiplyConverter.ofRational(BigInteger.ONE, BigInteger.TEN))));
        AMPERE = addUnit(tech.units.indriya.unit.Units.AMPERE);
        FARAD = addUnit(tech.units.indriya.unit.Units.FARAD);
        COULOMB = addUnit(tech.units.indriya.unit.Units.COULOMB);
        AMPERE_HOUR = addUnit(tech.units.indriya.unit.Units.COULOMB.multiply(3600.0));
        MILLIAMPERE_HOUR = addUnit(MetricPrefix.MILLI(AMPERE_HOUR));
        SIEMENS = addUnit(tech.units.indriya.unit.Units.SIEMENS);
        SIEMENS_PER_METRE =
                addUnit(new ProductUnit(tech.units.indriya.unit.Units.SIEMENS.divide(tech.units.indriya.unit.Units.METRE)));
        HENRY = addUnit(tech.units.indriya.unit.Units.HENRY);
        VOLT = addUnit(tech.units.indriya.unit.Units.VOLT);
        OHM = addUnit(tech.units.indriya.unit.Units.OHM);
        JOULE = addUnit(tech.units.indriya.unit.Units.JOULE);
        WATT_SECOND = addUnit(new ProductUnit(tech.units.indriya.unit.Units.WATT.multiply(tech.units.indriya.unit.Units.SECOND)));
        WATT_HOUR = addUnit(new ProductUnit(tech.units.indriya.unit.Units.WATT.multiply(tech.units.indriya.unit.Units.HOUR)));
        KILOWATT_HOUR = addUnit(MetricPrefix.KILO(WATT_HOUR));
        MEGAWATT_HOUR = addUnit(MetricPrefix.MEGA(WATT_HOUR));
        VAR = addUnit(new AlternateUnit(tech.units.indriya.unit.Units.WATT, "var"));
        KILOVAR = addUnit(MetricPrefix.KILO(VAR));
        VAR_HOUR = addUnit(new ProductUnit(VAR.multiply(tech.units.indriya.unit.Units.HOUR)), Energy.class);
        KILOVAR_HOUR = addUnit(MetricPrefix.KILO(VAR_HOUR));
        VOLT_AMPERE = addUnit(new AlternateUnit(tech.units.indriya.unit.Units.WATT, "VA"));
        KILOVOLT_AMPERE = addUnit(MetricPrefix.KILO(VOLT_AMPERE));
        VOLT_AMPERE_HOUR = addUnit(new ProductUnit(VOLT_AMPERE.multiply(tech.units.indriya.unit.Units.HOUR)), Energy.class);
        NEWTON = addUnit(tech.units.indriya.unit.Units.NEWTON);
        HERTZ = addUnit(tech.units.indriya.unit.Units.HERTZ);
        IRRADIANCE =
                addUnit(new ProductUnit(tech.units.indriya.unit.Units.WATT.divide(tech.units.indriya.unit.Units.SQUARE_METRE)));
        MICROWATT_PER_SQUARE_CENTIMETRE =
                addUnit(new TransformedUnit(IRRADIANCE, MultiplyConverter.ofRational(BigInteger.ONE, BigInteger.valueOf(100L))));
        LUX = addUnit(tech.units.indriya.unit.Units.LUX);
        LUMEN = addUnit(tech.units.indriya.unit.Units.LUMEN);
        CANDELA = addUnit(tech.units.indriya.unit.Units.CANDELA);
        WEBER = addUnit(tech.units.indriya.unit.Units.WEBER);
        TESLA = addUnit(tech.units.indriya.unit.Units.TESLA);
        WATT = addUnit(tech.units.indriya.unit.Units.WATT);
        DECIBEL_MILLIWATTS = new TransformedUnit("dBm", MetricPrefix.MILLI(WATT),
                (new ExpConverter(10.0)).concatenate(MultiplyConverter.of(0.1)));
        MILLIMETRE_OF_MERCURY = addUnit(new TransformedUnit("mmHg", tech.units.indriya.unit.Units.PASCAL,
                MultiplyConverter.ofRational(BigInteger.valueOf(133322368L), BigInteger.valueOf(1000000L))));
        BAR = addUnit(new TransformedUnit("bar", tech.units.indriya.unit.Units.PASCAL,
                MultiplyConverter.ofRational(BigInteger.valueOf(100000L), BigInteger.ONE)));
        MILLIBAR = addUnit(MetricPrefix.MILLI(BAR));
        BECQUEREL = addUnit(tech.units.indriya.unit.Units.BECQUEREL);
        BECQUEREL_PER_CUBIC_METRE = addUnit(new ProductUnit(
                tech.units.indriya.unit.Units.BECQUEREL.divide(tech.units.indriya.unit.Units.CUBIC_METRE)));
        GRAY = addUnit(tech.units.indriya.unit.Units.GRAY);
        SIEVERT = addUnit(tech.units.indriya.unit.Units.SIEVERT);
        MILLIMETRE_PER_HOUR = addUnit(new TransformedUnit("mm/h", tech.units.indriya.unit.Units.KILOMETRE_PER_HOUR,
                MultiplyConverter.ofRational(BigInteger.ONE, BigInteger.valueOf(1000000L))));
        METRE_PER_SECOND = addUnit(tech.units.indriya.unit.Units.METRE_PER_SECOND);
        KNOT = addUnit(new TransformedUnit("kn", tech.units.indriya.unit.Units.KILOMETRE_PER_HOUR,
                MultiplyConverter.ofRational(BigInteger.valueOf(1852L), BigInteger.valueOf(1000L))));
        STERADIAN = addUnit(tech.units.indriya.unit.Units.STERADIAN);
        KELVIN = addUnit(tech.units.indriya.unit.Units.KELVIN);
        SECOND = addUnit(tech.units.indriya.unit.Units.SECOND);
        MINUTE = addUnit(tech.units.indriya.unit.Units.MINUTE);
        HOUR = addUnit(tech.units.indriya.unit.Units.HOUR);
        DAY = addUnit(tech.units.indriya.unit.Units.DAY);
        WEEK = addUnit(tech.units.indriya.unit.Units.WEEK);
        YEAR = addUnit(tech.units.indriya.unit.Units.YEAR);
        LITRE_PER_MINUTE =
                addUnit(new ProductUnit(tech.units.indriya.unit.Units.LITRE.divide(tech.units.indriya.unit.Units.MINUTE)));
        CUBICMETRE_PER_SECOND =
                addUnit(new ProductUnit(tech.units.indriya.unit.Units.CUBIC_METRE.divide(tech.units.indriya.unit.Units.SECOND)));
        CUBICMETRE_PER_MINUTE =
                addUnit(new ProductUnit(tech.units.indriya.unit.Units.CUBIC_METRE.divide(tech.units.indriya.unit.Units.MINUTE)));
        CUBICMETRE_PER_HOUR =
                addUnit(new ProductUnit(tech.units.indriya.unit.Units.CUBIC_METRE.divide(tech.units.indriya.unit.Units.HOUR)));
        CUBICMETRE_PER_DAY =
                addUnit(new ProductUnit(tech.units.indriya.unit.Units.CUBIC_METRE.divide(tech.units.indriya.unit.Units.DAY)));
        BIT = addUnit(new AlternateUnit(ONE, "bit"));
        KILOBIT = addUnit(MetricPrefix.KILO(BIT));
        MEGABIT = addUnit(MetricPrefix.MEGA(BIT));
        GIGABIT = addUnit(MetricPrefix.GIGA(BIT));
        TERABIT = addUnit(MetricPrefix.TERA(BIT));
        PETABIT = addUnit(MetricPrefix.PETA(BIT));
        BYTE = addUnit(BIT.multiply(8.0));
        OCTET = addUnit(BIT.multiply(8.0));
        KILOBYTE = addUnit(MetricPrefix.KILO(BYTE));
        MEGABYTE = addUnit(MetricPrefix.MEGA(BYTE));
        GIGABYTE = addUnit(MetricPrefix.GIGA(BYTE));
        TERABYTE = addUnit(MetricPrefix.TERA(BYTE));
        PETABYTE = addUnit(MetricPrefix.PETA(BYTE));
        KIBIBYTE = addUnit(BinaryPrefix.KIBI(BYTE));
        MEBIBYTE = addUnit(BinaryPrefix.MEBI(BYTE));
        GIBIBYTE = addUnit(BinaryPrefix.GIBI(BYTE));
        TEBIBYTE = addUnit(BinaryPrefix.TEBI(BYTE));
        PEBIBYTE = addUnit(BinaryPrefix.PEBI(BYTE));
        KIBIOCTET = addUnit(BinaryPrefix.KIBI(OCTET));
        MEBIOCTET = addUnit(BinaryPrefix.MEBI(OCTET));
        GIBIOCTET = addUnit(BinaryPrefix.GIBI(OCTET));
        TEBIOCTET = addUnit(BinaryPrefix.TEBI(OCTET));
        PEBIOCTET = addUnit(BinaryPrefix.PEBI(OCTET));
        BIT_PER_SECOND = addUnit(new ProductUnit(BIT.divide(tech.units.indriya.unit.Units.SECOND)));
        KILOBIT_PER_SECOND = addUnit(MetricPrefix.KILO(BIT_PER_SECOND));
        MEGABIT_PER_SECOND = addUnit(MetricPrefix.MEGA(BIT_PER_SECOND));
        GIGABIT_PER_SECOND = addUnit(MetricPrefix.GIGA(BIT_PER_SECOND));
        TERABIT_PER_SECOND = addUnit(MetricPrefix.TERA(BIT_PER_SECOND));
        SimpleUnitFormat.getInstance().label(AMPERE_HOUR, "Ah");
        SimpleUnitFormat.getInstance().label(BAR, BAR.getSymbol());
        SimpleUnitFormat.getInstance().label(BECQUEREL_PER_CUBIC_METRE, "Bq/m³");
        SimpleUnitFormat.getInstance().label(BIT, BIT.getSymbol());
        SimpleUnitFormat.getInstance().label(BIT_PER_SECOND, "bit/s");
        SimpleUnitFormat.getInstance().label(BYTE, "B");
        SimpleUnitFormat.getInstance().alias(BYTE, "o");
        SimpleUnitFormat.getInstance().label(CUBICMETRE_PER_DAY, "m³/d");
        SimpleUnitFormat.getInstance().label(CUBICMETRE_PER_HOUR, "m³/h");
        SimpleUnitFormat.getInstance().label(CUBICMETRE_PER_MINUTE, "m³/min");
        SimpleUnitFormat.getInstance().label(CUBICMETRE_PER_SECOND, "m³/s");
        SimpleUnitFormat.getInstance().label(DECIBEL, "dB");
        SimpleUnitFormat.getInstance().label(DECIBEL_MILLIWATTS, "dBm");
        SimpleUnitFormat.getInstance().label(DEGREE_ANGLE, "°");
        SimpleUnitFormat.getInstance().label(DEUTSCHE_HAERTE, "°dH");
        SimpleUnitFormat.getInstance().label(DOBSON_UNIT, "DU");
        SimpleUnitFormat.getInstance().label(GIGABYTE, "GB");
        SimpleUnitFormat.getInstance().label(GIBIBYTE, "GiB");
        SimpleUnitFormat.getInstance().alias(GIBIBYTE, "Gio");
        SimpleUnitFormat.getInstance().label(GIGABIT, "Gbit");
        SimpleUnitFormat.getInstance().label(GIGABIT_PER_SECOND, "Gbit/s");
        SimpleUnitFormat.getInstance().label(IRRADIANCE, "W/m²");
        SimpleUnitFormat.getInstance().label(KILOBYTE, "KB");
        SimpleUnitFormat.getInstance().label(KIBIBYTE, "KiB");
        SimpleUnitFormat.getInstance().alias(KIBIBYTE, "Kio");
        SimpleUnitFormat.getInstance().label(KILOBIT, "kbit");
        SimpleUnitFormat.getInstance().label(KILOBIT_PER_SECOND, "kbit/s");
        SimpleUnitFormat.getInstance().label(KILOVAR, "kvar");
        SimpleUnitFormat.getInstance().label(KILOVAR_HOUR, "kvarh");
        SimpleUnitFormat.getInstance().label(KILOVOLT_AMPERE, "kVA");
        SimpleUnitFormat.getInstance().label(KILOWATT_HOUR, "kWh");
        SimpleUnitFormat.getInstance().label(KNOT, KNOT.getSymbol());
        SimpleUnitFormat.getInstance().label(LITRE_PER_MINUTE, "l/min");
        SimpleUnitFormat.getInstance().label(MEGABYTE, "MB");
        SimpleUnitFormat.getInstance().label(MEBIBYTE, "MiB");
        SimpleUnitFormat.getInstance().alias(MEBIBYTE, "Mio");
        SimpleUnitFormat.getInstance().label(MEGABIT, "Mbit");
        SimpleUnitFormat.getInstance().label(MEGABIT_PER_SECOND, "Mbit/s");
        SimpleUnitFormat.getInstance().label(MEGAWATT_HOUR, "MWh");
        SimpleUnitFormat.getInstance().label(MICROGRAM_PER_CUBICMETRE, "µg/m³");
        SimpleUnitFormat.getInstance().label(MICROWATT_PER_SQUARE_CENTIMETRE, "µW/cm²");
        SimpleUnitFormat.getInstance().label(MILLIAMPERE_HOUR, "mAh");
        SimpleUnitFormat.getInstance().label(MILLIBAR, "mbar");
        SimpleUnitFormat.getInstance().label(MILLIMETRE_OF_MERCURY, MILLIMETRE_OF_MERCURY.getSymbol());
        SimpleUnitFormat.getInstance().label(PARTS_PER_BILLION, "ppb");
        SimpleUnitFormat.getInstance().label(PARTS_PER_MILLION, "ppm");
        SimpleUnitFormat.getInstance().label(PETABYTE, "PB");
        SimpleUnitFormat.getInstance().label(PEBIBYTE, "PiB");
        SimpleUnitFormat.getInstance().alias(PEBIBYTE, "Pio");
        SimpleUnitFormat.getInstance().label(PETABIT, "Pbit");
        SimpleUnitFormat.getInstance().label(STANDARD_GRAVITY, "gₙ");
        SimpleUnitFormat.getInstance().label(SIEMENS_PER_METRE, "S/m");
        SimpleUnitFormat.getInstance().label(TERABYTE, "TB");
        SimpleUnitFormat.getInstance().label(TEBIBYTE, "TiB");
        SimpleUnitFormat.getInstance().alias(TEBIBYTE, "Tio");
        SimpleUnitFormat.getInstance().label(TERABIT, "Tbit");
        SimpleUnitFormat.getInstance().label(TERABIT_PER_SECOND, "Tbit/s");
        SimpleUnitFormat.getInstance().label(VAR, "var");
        SimpleUnitFormat.getInstance().label(VAR_HOUR, "varh");
        SimpleUnitFormat.getInstance().label(VOLT_AMPERE, "VA");
        SimpleUnitFormat.getInstance().label(VOLT_AMPERE_HOUR, "VAh");
        SimpleUnitFormat.getInstance().label(WATT_HOUR, "Wh");
        SimpleUnitFormat.getInstance().label(WATT_SECOND, "Ws");
    }

    private Units() {
    }

    public static SystemOfUnits getInstance() {
        return INSTANCE;
    }

    private static <U extends Unit<?>> U addUnit(U unit) {
        INSTANCE.units.add(unit);
        return unit;
    }

    private static <U extends AbstractUnit<?>> U addUnit(U unit, Class<? extends Quantity<?>> type) {
        INSTANCE.units.add(unit);
        INSTANCE.quantityToUnit.put(type, unit);
        return unit;
    }

    @Override
    public String getName() {
        return "Units";
    }
}
