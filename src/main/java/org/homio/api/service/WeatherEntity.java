package org.homio.api.service;

import lombok.*;
import org.homio.api.entity.device.DeviceBaseEntity;
import org.homio.api.service.EntityService.ServiceInstance;
import org.homio.api.service.WeatherEntity.WeatherService;
import org.homio.api.ui.route.UIRouteMisc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@UIRouteMisc
public abstract class WeatherEntity<T extends ServiceInstance & WeatherService> extends DeviceBaseEntity implements EntityService<T> {

    public enum WeatherInfoType {
        Temperature, WindSpeed, WindDegree, Visibility, FeelsLike, Humidity, Pressure, Clouds
    }

    public interface WeatherService {

        @NotNull
        WeatherInfo readWeather(@NotNull String city, @Nullable Long timestamp);
    }

    @Setter
    @Getter
    public static class WeatherInfo {

        private String city;
        private String icon;
        private String condition;
        private Double rainSpeed;
        private Long sunrise;
        private Long sunset;
        private Double temperature;
        private Double minTemperature;
        private Double maxTemperature;
        private Double windSpeed;
        private Double windDegree;
        private Double visibility;
        private Double feelsLike;
        private Double humidity;
        private Double pressure;
        private Double clouds;
        private long dt;

        private Map<Long, HourWeatherInfo> hours;
        private List<DailyForecast> forecast;

        @Setter
        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DailyForecast {
            private long dt;
            private String name;
            private String icon;
            private double minTemp;
            private double maxTemp;
            private String condition;
            private Map<Long, HourWeatherInfo> hours;
        }

        @Setter
        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class HourWeatherInfo {

            private Double minTemp;
            private Double maxTemp;
            private Double temperature;
            private Double feelsLike;
            private Double humidity;
            private Double pressure;
            private Double windSpeed;
            private Double windDegree;
            private String icon;
            private String condition;
            private long dt;
        }
    }
}
