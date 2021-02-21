package org.touchhome.bundle.api.state;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.touchhome.bundle.api.model.KeyValueEnum;

import java.util.function.BiPredicate;

@AllArgsConstructor
public enum CompareType implements KeyValueEnum {
    GREATER(">", (a, b) -> a > b),
    LESS("<", (a, b) -> a < b),
    GREATER_EQUAL(">=", (a, b) -> a >= b),
    LESS_EQUAL("<=", (a, b) -> a <= b),
    EQUAL("=", (a, b) -> Double.compare(a, b) == 0);

    @Getter
    private final String shortName;

    private final BiPredicate<Double, Double> matchFn;

    public boolean match(double a, double b) {
        return matchFn.test(a, b);
    }

    public boolean match(int a, int b) {
        return matchFn.test((double) a, (double) b);
    }

    @Override
    public String toString() {
        return shortName;
    }
}
