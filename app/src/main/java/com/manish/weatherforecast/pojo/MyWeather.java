package com.manish.weatherforecast.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MyWeather {
    @SerializedName("lat")
    @Expose
    public Double lat;
    @SerializedName("lon")
    @Expose
    public Double lon;
    @SerializedName("timezone")
    @Expose
    public String timezone;
    @SerializedName("timezone_offset")
    @Expose
    public Integer timezoneOffset;
    @SerializedName("current")
    @Expose
    public Current current;
    @SerializedName("daily")
    @Expose
    public List<Daily> daily = null;

    public class Current {

        @SerializedName("dt")
        @Expose
        public Integer dt;
        @SerializedName("sunrise")
        @Expose
        public Integer sunrise;
        @SerializedName("sunset")
        @Expose
        public Integer sunset;
        @SerializedName("temp")
        @Expose
        public Double temp;
        @SerializedName("feels_like")
        @Expose
        public Double feelsLike;
        @SerializedName("pressure")
        @Expose
        public Integer pressure;
        @SerializedName("humidity")
        @Expose
        public Integer humidity;
        @SerializedName("dew_point")
        @Expose
        public Double dewPoint;
        @SerializedName("uvi")
        @Expose
        public Double uvi;
        @SerializedName("clouds")
        @Expose
        public Integer clouds;
        @SerializedName("visibility")
        @Expose
        public Integer visibility;
        @SerializedName("wind_speed")
        @Expose
        public Double windSpeed;
        @SerializedName("wind_deg")
        @Expose
        public Integer windDeg;
        @SerializedName("weather")
        @Expose
        public List<Weather> weather = null;
    }

    public class Daily {

        @SerializedName("dt")
        @Expose
        public Integer dt;
        @SerializedName("sunrise")
        @Expose
        public Integer sunrise;
        @SerializedName("sunset")
        @Expose
        public Integer sunset;
        @SerializedName("temp")
        @Expose
        public Temp temp;
        @SerializedName("feels_like")
        @Expose
        public FeelsLike feelsLike;
        @SerializedName("pressure")
        @Expose
        public Integer pressure;
        @SerializedName("humidity")
        @Expose
        public Integer humidity;
        @SerializedName("dew_point")
        @Expose
        public Double dewPoint;
        @SerializedName("wind_speed")
        @Expose
        public Double windSpeed;
        @SerializedName("wind_deg")
        @Expose
        public Integer windDeg;
        @SerializedName("weather")
        @Expose
        public List<Weather> weather = null;
        @SerializedName("clouds")
        @Expose
        public Integer clouds;
        @SerializedName("pop")
        @Expose
        public Double pop;
        @SerializedName("uvi")
        @Expose
        public Double uvi;

    }

    public class FeelsLike {

        @SerializedName("day")
        @Expose
        public Double day;
        @SerializedName("night")
        @Expose
        public Double night;
        @SerializedName("eve")
        @Expose
        public Double eve;
        @SerializedName("morn")
        @Expose
        public Double morn;

    }


    public class Temp {

        @SerializedName("day")
        @Expose
        public Double day;
        @SerializedName("min")
        @Expose
        public Double min;
        @SerializedName("max")
        @Expose
        public Double max;
        @SerializedName("night")
        @Expose
        public Double night;
        @SerializedName("eve")
        @Expose
        public Double eve;
        @SerializedName("morn")
        @Expose
        public Double morn;

    }

    public class Weather {

        @SerializedName("id")
        @Expose
        public Integer id;
        @SerializedName("main")
        @Expose
        public String main;
        @SerializedName("description")
        @Expose
        public String description;
        @SerializedName("icon")
        @Expose
        public String icon;

    }
}
