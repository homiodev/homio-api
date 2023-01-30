package org.touchhome.bundle.api.state;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.quantity.Quantities;

import javax.measure.*;
import java.math.BigDecimal;

/**
 * Value with Unit
 */
@Log4j2
public class QuantityType implements State, Comparable<QuantityType> {

    @Getter
    private final @NotNull Quantity quantity;

    public QuantityType(Number value, Unit unit) {
        // Avoid scientific notation for double
        BigDecimal bd = new BigDecimal(value.toString());
        quantity = Quantities.getQuantity(bd, unit);
    }

    @Override
    public int compareTo(QuantityType o) {
        if (quantity.getUnit().isCompatible(o.quantity.getUnit())) {
            QuantityType v1 = this.toUnit(getUnit().getSystemUnit());
            QuantityType v2 = o.toUnit(o.getUnit().getSystemUnit());
            if (v1 != null && v2 != null) {
                return Double.compare(v1.doubleValue(), v2.doubleValue());
            } else {
                throw new IllegalArgumentException("Unable to convert to system unit during compare.");
            }
        } else {
            throw new IllegalArgumentException("Can not compare incompatible units.");
        }
    }

    public QuantityType toUnit(Unit<?> targetUnit) {
        if (!targetUnit.equals(getUnit())) {
            try {
                UnitConverter uc = getUnit().getConverterToAny(targetUnit);
                Quantity<?> result = Quantities.getQuantity(uc.convert(quantity.getValue()), targetUnit);

                return new QuantityType(result.getValue(), (Unit) targetUnit);
            } catch (UnconvertibleException | IncommensurableException e) {
                log.debug("Unable to convert unit from {} to {}", getUnit(), targetUnit);
                return null;
            }
        }
        return this;
    }

    private Unit getUnit() {
        return quantity.getUnit();
    }

    @Override
    public int intValue() {
        return quantity.getValue().intValue();
    }

    @Override
    public boolean boolValue() {
        throw new IllegalStateException("Unable to fetch boolean value from Quantity type");
    }

    @Override
    public String stringValue() {
        return quantity.getValue().toString();
    }

    @Override
    public long longValue() {
        return quantity.getValue().longValue();
    }

    @Override
    public float floatValue() {
        return quantity.getValue().floatValue();
    }

    public double doubleValue() {
        return quantity.getValue().doubleValue();
    }

    @Override
    public String toString() {
        return toFullString();
    }

    public String toFullString() {
        if (quantity.getUnit() == AbstractUnit.ONE) {
            return quantity.getValue().toString();
        } else {
            return quantity.toString();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuantityType that = (QuantityType) o;

        return quantity.equals(that.quantity);
    }

    @Override
    public int hashCode() {
        return quantity.hashCode();
    }
}
