package org.homio.api.state;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Log4j2
@Accessors(chain = true)
public class DecimalType implements State, Comparable<DecimalType> {

  public static final DecimalType TRUE = new DecimalType(1);
  public static final DecimalType FALSE = new DecimalType(0);

  public static final DecimalType ZERO = new DecimalType(0);
  public static final DecimalType HUNDRED = new DecimalType(100);

  private final @NotNull BigDecimal value;

  @Getter @Setter private @Nullable String unit;

  @Getter @Setter private @Nullable BigDecimal oldValue;

  public DecimalType(Number value) {
    this(new BigDecimal(value.toString()));
  }

  public DecimalType(@NotNull BigDecimal value) {
    this(value, Math.max(value.scale(), 2));
  }

  public DecimalType(@NotNull BigDecimal value, @Nullable Integer scale) {
    if (scale != null) {
      this.value = value.setScale(scale, RoundingMode.HALF_UP);
    } else {
      this.value = value;
    }
  }

  public DecimalType(@NotNull BigDecimal value, @Nullable BigDecimal oldValue) {
    this.value = value;
    this.oldValue = oldValue;
  }

  public DecimalType(int value, int oldValue) {
    this.value = BigDecimal.valueOf(value);
    this.oldValue = BigDecimal.valueOf(oldValue);
  }

  public DecimalType(float value, @Nullable Float oldValue) {
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

  public DecimalType(float value, @Nullable Integer scale) {
    this(BigDecimal.valueOf(value), scale);
  }

  public DecimalType(String value) {
    this(new BigDecimal(value));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

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
  public BigDecimal rawValue() {
    return value;
  }

  @Override
  public boolean boolValue() {
    return value.intValue() != 0;
  }

  @Override
  public void setAsNode(ObjectNode node, String key) {
    node.put(key, value);
  }

  @Override
  public String stringValue() {
    return value.toPlainString();
  }

  @Override
  public long longValue() {
    return value.longValue();
  }

  public @NotNull BigDecimal toBigDecimal() {
    return value;
  }

  @Override
  public String toString() {
    return value.toPlainString();
  }

  public @NotNull String toString(int maxPrecision) {
    int precision = value.precision();
    if (precision <= maxPrecision) {
      return value.toPlainString();
    }
    String formatString = "%." + maxPrecision + "f";
    return String.format(formatString, value);
  }

  public @NotNull String toUIString() {
    return this + StringUtils.trimToEmpty(unit);
  }

  public @NotNull String toUIString(int scale) {
    return toString(scale) + StringUtils.trimToEmpty(unit);
  }

  public int getScale() {
    return value.scale();
  }
}
