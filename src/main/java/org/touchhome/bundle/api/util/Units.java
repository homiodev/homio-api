package org.touchhome.bundle.api.util;

import org.jetbrains.annotations.Nullable;
import tech.units.indriya.AbstractSystemOfUnits;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.format.SimpleUnitFormat;
import tech.units.indriya.function.LogConverter;
import tech.units.indriya.function.MultiplyConverter;
import tech.units.indriya.unit.*;

import javax.measure.MetricPrefix;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.*;
import javax.measure.spi.SystemOfUnits;
import java.math.BigInteger;
import java.util.Set;

import static tech.units.indriya.AbstractUnit.ONE;
import static tech.units.indriya.unit.Units.*;

interface DataAmount extends Quantity<DataAmount> {
}

interface DataTransferRate extends Quantity<DataTransferRate> {
}

interface Density extends Quantity<Density> {
}

interface VolumetricFlowRate extends Quantity<VolumetricFlowRate> {
}

public final class Units extends AbstractSystemOfUnits {
    public static final Unit<Acceleration> STANDARD_GRAVITY;
    public static final Unit<Angle> DEGREE_ANGLE;
    public static final Unit<Density> KILOGRAM_PER_CUBICMETRE;
    public static final Unit<Density> MICROGRAM_PER_CUBICMETRE;
    public static final Unit<Dimensionless> DECIBEL;
    public static final Unit<Dimensionless> LQI;
    public static final Unit<ElectricCharge> AMPERE_HOUR;
    public static final Unit<ElectricPotential> MILLI_VOLT;
    public static final Unit<Energy> WATT_SECOND;
    public static final Unit<Energy> WATT_HOUR;
    public static final Unit<Energy> KILOWATT_HOUR;
    public static final Unit<Power> VOLT_AMPERE;
    public static final Unit<Energy> VOLT_AMPERE_HOUR;
    public static final Unit<Pressure> BAR;
    public static final Unit<Speed> MILLIMETRE_PER_HOUR;
    public static final Unit<VolumetricFlowRate> LITRE_PER_MINUTE;
    public static final Unit<DataAmount> BIT;
    public static final Unit<DataAmount> BYTE;
    public static final Unit<DataAmount> OCTET;
    public static final Unit<DataTransferRate> BIT_PER_SECOND;
    private static final Units INSTANCE = new Units();

    static {
        MILLI_VOLT = addUnit(new TransformedUnit("mV", tech.units.indriya.unit.Units.VOLT, MultiplyConverter.of(1000)));
        STANDARD_GRAVITY = addUnit(METRE_PER_SQUARE_SECOND.multiply(9.80665));
        DEGREE_ANGLE = addUnit(new TransformedUnit("deg", tech.units.indriya.unit.Units.RADIAN,
                MultiplyConverter.ofPiExponent(1).concatenate(MultiplyConverter.ofRational(1L, 180L))));
        KILOGRAM_PER_CUBICMETRE = addUnit(new ProductUnit(
                tech.units.indriya.unit.Units.KILOGRAM.divide(tech.units.indriya.unit.Units.CUBIC_METRE)));
        MICROGRAM_PER_CUBICMETRE = addUnit(new TransformedUnit(KILOGRAM_PER_CUBICMETRE,
                MultiplyConverter.ofRational(BigInteger.ONE, BigInteger.valueOf(1000000000L))));
        LQI = addUnit(new BaseUnit<>("LQI", "LinkQuality", UnitDimension.LENGTH), Dimensionless.class);
        DECIBEL = addUnit(ONE.transform(
                (new LogConverter(10.0)).inverse().concatenate(MultiplyConverter.ofRational(BigInteger.ONE, BigInteger.TEN))));
        AMPERE_HOUR = addUnit(tech.units.indriya.unit.Units.COULOMB.multiply(3600.0));
        WATT_SECOND = addUnit(new ProductUnit(WATT.multiply(tech.units.indriya.unit.Units.SECOND)));
        WATT_HOUR = addUnit(new ProductUnit(WATT.multiply(tech.units.indriya.unit.Units.HOUR)));
        KILOWATT_HOUR = addUnit(MetricPrefix.KILO(WATT_HOUR));
        VOLT_AMPERE = addUnit(new AlternateUnit(WATT, "VA"));
        VOLT_AMPERE_HOUR = addUnit(new ProductUnit(VOLT_AMPERE.multiply(tech.units.indriya.unit.Units.HOUR)), Energy.class);
        BAR = addUnit(new TransformedUnit("bar", tech.units.indriya.unit.Units.PASCAL,
                MultiplyConverter.ofRational(BigInteger.valueOf(100000L), BigInteger.ONE)));
        MILLIMETRE_PER_HOUR = addUnit(new TransformedUnit("mm/h", tech.units.indriya.unit.Units.KILOMETRE_PER_HOUR,
                MultiplyConverter.ofRational(BigInteger.ONE, BigInteger.valueOf(1000000L))));
        LITRE_PER_MINUTE = addUnit(new ProductUnit(LITRE.divide(tech.units.indriya.unit.Units.MINUTE)));
        BIT = addUnit(new AlternateUnit(ONE, "bit"));
        BYTE = addUnit(BIT.multiply(8.0));
        OCTET = addUnit(BIT.multiply(8.0));
        BIT_PER_SECOND = addUnit(new ProductUnit(BIT.divide(tech.units.indriya.unit.Units.SECOND)));
        SimpleUnitFormat.getInstance().label(AMPERE_HOUR, "Ah");
        SimpleUnitFormat.getInstance().label(BAR, BAR.getSymbol());
        SimpleUnitFormat.getInstance().label(BIT, BIT.getSymbol());
        SimpleUnitFormat.getInstance().label(BIT_PER_SECOND, "bit/s");
        SimpleUnitFormat.getInstance().label(BYTE, "B");
        SimpleUnitFormat.getInstance().alias(BYTE, "o");
        SimpleUnitFormat.getInstance().label(DECIBEL, "dB");
        SimpleUnitFormat.getInstance().label(DEGREE_ANGLE, "°");
        SimpleUnitFormat.getInstance().label(LITRE_PER_MINUTE, "l/min");
        SimpleUnitFormat.getInstance().label(MICROGRAM_PER_CUBICMETRE, "µg/m³");
        SimpleUnitFormat.getInstance().label(STANDARD_GRAVITY, "gₙ");
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

    public static @Nullable Unit findUnit(String name) {
        Unit unit = findUnit(tech.units.indriya.unit.Units.getInstance().getUnits(), name);
        if (unit == null) {
            unit = findUnit((Set<Unit<?>>) Units.getInstance().getUnits(), name);
        }
        return unit;
    }

    private static Unit findUnit(Set<Unit<?>> units, String name) {
        return units.stream().filter((u) -> name.equalsIgnoreCase(u.toString())).findAny().orElse(null);
    }
}
