package org.homio.api.service;

import lombok.Getter;
import lombok.Setter;
import org.homio.api.entity.types.MiscEntity;
import org.homio.api.service.EntityService.ServiceInstance;
import org.homio.api.service.WeatherEntity.WeatherService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class WeatherEntity<T extends ServiceInstance & WeatherService> extends MiscEntity implements EntityService<T> {

  public enum WeatherInfoType {
    Temperature, WindSpeed, WindDegree, Visibility, FeelsLike, Humidity, Pressure, Clouds
  }

  public interface WeatherService {

    @NotNull WeatherInfo readWeather(@NotNull String city, @Nullable Long timestamp);
  }

  @Setter
  @Getter
  public static class WeatherInfo {

    private Long sunrise;
    private Long sunset;
    private Double temperature;
    private Double windSpeed;
    private Double windDegree;
    private Double visibility;
    private Double feelsLike;
    private Double humidity;
    private Double pressure;
    private Double clouds;
    private long dt;

    private Map<Long, HourWeatherInfo> hours;

    @Setter
    @Getter
    public static class HourWeatherInfo {

      private Double temperature;
      private Double feelsLike;
      private Double humidity;
      private Double pressure;
      private long dt;
    }
  }
}
