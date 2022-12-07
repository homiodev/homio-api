package org.touchhome.bundle.api.state;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Objects;

@Log4j2
public class DecimalType extends State implements Comparable<DecimalType> {

    public static final DecimalType TRUE = new DecimalType(1);
    public static final DecimalType FALSE = new DecimalType(0);

    public static final DecimalType ZERO = new DecimalType(0);
    public static final DecimalType HUNDRED = new DecimalType(100);

    @Getter
    private final @NotNull BigDecimal value;

    @Getter
    @Setter
    private BigDecimal oldValue;

    public DecimalType(BigDecimal value) {
        this.value = value;
    }

    public DecimalType(BigDecimal value, BigDecimal oldValue) {
        this.value = value;
        this.oldValue = oldValue;
    }

    public DecimalType(int value, int oldValue) {
        this.value = BigDecimal.valueOf(value);
        this.oldValue = BigDecimal.valueOf(oldValue);
    }

    public DecimalType(float value, Float oldValue) {
        this(BigDecimal.valueOf(value), oldValue == null ? null : BigDecimal.valueOf(oldValue));
    }

    public DecimalType(long value) {
        this(BigDecimal.valueOf(value));
    }

    public DecimalType(double value) {
        this(BigDecimal.valueOf(value));
    }

    public DecimalType(float value) {
        this(BigDecimal.valueOf(value));
    }

    public DecimalType(String value) {
        this(new BigDecimal(value));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DecimalType that = (DecimalType) o;

        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equalToOldValue() {
        return Objects.equals(value, oldValue);
    }

    @Override
    public int compareTo(DecimalType o) {
        return value.compareTo(o.value);
    }

    public double doubleValue() {
        return value.doubleValue();
    }

    @Override
    public float floatValue() {
        return value.floatValue();
    }

    @Override
    public int intValue() {
        return value.intValue();
    }

    @Override
    public boolean boolValue() {
        throw new IllegalStateException("Unable to fetch boolean value from DecimalType");
    }

    @Override
    public String stringValue() {
        return value.toPlainString();
    }

    @Override
    public long longValue() {
        return value.longValue();
    }

    public BigDecimal toBigDecimal() {
        return value;
    }

    @Override
    public String toString() {
        return toFullString();
    }

    public String toFullString() {
        return value.toPlainString();
    }
}
