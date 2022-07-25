package org.touchhome.bundle.api.ui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@AllArgsConstructor
public enum TimePeriod {
    Minute(1, TimeUnit.MINUTES, "-1m"),
    FiveMinute(5, TimeUnit.MINUTES, "-5m"),
    FifteenMinute(15, TimeUnit.MINUTES, "-15m"),
    Hour(1, TimeUnit.HOURS, "-1h"),
    Day(1, TimeUnit.DAYS, "-1d"),
    Week(7, TimeUnit.DAYS, "-7d"),
    Month(30, TimeUnit.DAYS, "-30d"),
    Year(365, TimeUnit.DAYS, "-365d"),
    All(null, null, "0");

    public static TimePeriod fromValue(String value) {
        return Stream.of(TimePeriod.values()).filter(e -> e.name().equals(value)).findFirst().orElse(null);
    }

    private final Integer sub;
    private final TimeUnit timeUnit;
    private final String dateFromNow;

    public TimePeriodImpl getTimePeriod() {
        return new TimePeriodImpl() {
            @Override
            public Pair<Date, Date> getDateRange() {
                return Pair.of(sub == null ? new Date(0) : new Date(System.currentTimeMillis() - timeUnit.toMillis(sub)),
                        new Date());
            }

            @Override
            public String getDateFromNow() {
                return dateFromNow;
            }

            @Override
            public List<Date> evaluateDateRange() {
                Date minDate;
                List<Date> dates = new ArrayList<>();
                switch (TimePeriod.this) {
                    case Minute:
                        minDate = DateUtils.addMinutes(DateUtils.truncate(new Date(), Calendar.SECOND), -1);
                        IntStream.rangeClosed(0, 60).forEach(i -> dates.add(DateUtils.addSeconds(minDate, i)));
                        return dates;
                    case FiveMinute:
                        minDate = DateUtils.addMinutes(DateUtils.truncate(new Date(), Calendar.SECOND), -5);
                        IntStream.rangeClosed(0, 15).forEach(i -> dates.add(DateUtils.addSeconds(minDate, i * 20)));
                        return dates;
                    case FifteenMinute:
                        minDate = DateUtils.addMinutes(DateUtils.truncate(new Date(), Calendar.SECOND), -15);
                        IntStream.rangeClosed(0, 15).forEach(i -> dates.add(DateUtils.addMinutes(minDate, i)));
                        return dates;
                    case Hour:
                        minDate = DateUtils.addMinutes(DateUtils.truncate(new Date(), Calendar.SECOND), -60);
                        IntStream.rangeClosed(0, 60).forEach(i -> dates.add(DateUtils.addMinutes(minDate, i)));
                        return dates;
                    case Day:
                        minDate = DateUtils.addHours(DateUtils.truncate(new Date(), Calendar.SECOND), -24);
                        IntStream.rangeClosed(0, 23).forEach(i -> dates.add(DateUtils.addHours(minDate, i)));
                        return dates;
                    case Week:
                        minDate = DateUtils.truncate(DateUtils.addWeeks(new Date(), -1), Calendar.DATE);
                        IntStream.rangeClosed(0, 7).forEach(i -> {
                            dates.add(DateUtils.addDays(minDate, i));
                        });
                        return dates;
                    case Month:
                        minDate = DateUtils.truncate(DateUtils.addMonths(new Date(), -1), Calendar.DATE);
                        IntStream.rangeClosed(1, 31).forEach(i -> {
                            dates.add(DateUtils.addDays(minDate, i));
                        });
                        return dates;
                    case Year:
                        minDate = DateUtils.truncate(DateUtils.addYears(new Date(), -1), Calendar.DATE);
                        IntStream.rangeClosed(0, 11).forEach(i -> {
                            dates.add(DateUtils.addMonths(minDate, i));
                        });
                        return dates;
                    default:
                        return null; // evaluate from min/max
                }
            }
        };
    }

    public interface TimePeriodImpl {
        Pair<Date, Date> getDateRange();

        String getDateFromNow();

        List<Date> evaluateDateRange();
    }
}
