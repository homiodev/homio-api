package org.touchhome.bundle.api.entity.widget;

import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public enum AggregationType {
    First(true),
    Last(true),
    Min(false),
    Max(false),
    Sum(false),
    Count(false),
    Average(false),
    Median(false);

    @Getter private final boolean requireSorting;

    /** Stream must be already sorted for First, Last */
    public float evaluate(@NotNull Stream<Float> stream) {
        switch (this) {
            case First:
                return stream.findFirst().orElse(0F);
            case Last:
                return stream.reduce((first, second) -> second).orElse(0F);
            case Min:
                return stream.min(Float::compare).orElse(0F);
            case Max:
                return stream.max((o1, o2) -> Float.compare(o2, o1)).orElse(0F);
            case Sum:
                return stream.reduce(0F, Float::sum);
            case Count:
                return (float) stream.count();
            case Average:
                return stream.collect(Collectors.averagingDouble(value -> value)).floatValue();
            case Median:
                long size = stream.count();
                DoubleStream sortedAges = stream.mapToDouble(h -> h).sorted();
                if (size % 2 == 0) {
                    return (float) sortedAges.skip(size / 2 - 1).limit(2).average().getAsDouble();
                }
                return (float) sortedAges.skip(size / 2).findFirst().getAsDouble();
        }
        throw new IllegalStateException("Unable to evaluate unknown Aggregation type");
    }
}
