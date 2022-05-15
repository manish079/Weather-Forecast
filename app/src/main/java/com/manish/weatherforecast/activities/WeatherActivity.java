package com.manish.weatherforecast.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.manish.weatherforecast.R;
import com.manish.weatherforecast.base.BaseActivity;
import com.manish.weatherforecast.location_classes.MyLocationClass;
import com.manish.weatherforecast.pojo.MyWeather;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeatherActivity extends BaseActivity implements ActionBar.TabListener, ViewPager.OnPageChangeListener, AdapterView.OnItemClickListener {
    Location myLocation;
    ViewPager viewPager;
    ActionBar actionBar;
    ImageView ivIcon;
    TextView tvMain, tvCity, tvTemperature, tvPressure, tvHumidity, tvWindSpeed, tvClouds, tvSunrise, tvSunset, tvLast, tvDescription;
    ListView lvForecast;
    String city;
    int date;
    FloatingActionButton fabMyLoc;
    Spinner spLocations;
    SQLiteDatabase sqLiteDatabase;
    ArrayList<Object> placeNames = new ArrayList<>();
    ArrayList<Object> placeLats = new ArrayList<>();
    ArrayList<Object> placeLons = new ArrayList<>();
    List<MyWeather.Daily> dailyList;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLocations();
    }

    private void getLocations() {

        placeNames = new ArrayList<>();
        placeLats = new ArrayList<>();
        placeLons = new ArrayList<>();
        placeNames.add("My Location");
        placeLats.add(myLocation.getLatitude());
        placeLons.add(myLocation.getLongitude());

        Cursor cursor = sqLiteDatabase.rawQuery("Select * from tbname", null);
        while (cursor.moveToNext()) {
            placeNames.add(cursor.getString(0));
            placeLats.add(cursor.getString(1));
            placeLons.add(cursor.getString(2));
        }
        cursor.close();

        ArrayAdapter<Object> adapter = new ArrayAdapter<>(WeatherActivity.this, android.R.layout.simple_list_item_1, placeNames);
        spLocations.setAdapter(adapter);
        spLocations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                double lat = Double.parseDouble(placeLats.get(i).toString());
                double lon = Double.parseDouble(placeLons.get(i).toString());
                getWeather(lat, lon);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.locations)
            moveIntent(LocationActivity.class, false);  //moveIntent method in BaseActivity()
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        sqLiteDatabase = openOrCreateDatabase("Database", MODE_PRIVATE, null);
        sqLiteDatabase.execSQL("Create table if not exists tbname (PlaceName varchar,Lattitude varchar, Longitude varchar);");
        myLocation = getIntent().getParcelableExtra("myLocation");
        getAllViewsIds();
        setViewPager();
    }

    private void getAllViewsIds() {
        spLocations = findViewById(R.id.spLocations);
        viewPager = findViewById(R.id.viewPager);
        ivIcon = findViewById(R.id.ivIcon);
        tvMain = findViewById(R.id.tvMain);
        tvCity = findViewById(R.id.tvCity);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvPressure = findViewById(R.id.tvPressure);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWindSpeed = findViewById(R.id.tvWindSpeed);
        tvClouds = findViewById(R.id.tvClouds);
        tvSunrise = findViewById(R.id.tvSunrise);
        tvSunset = findViewById(R.id.tvSunset);
        tvLast = findViewById(R.id.tvLast);
        lvForecast = findViewById(R.id.lvForecast);
        tvDescription = findViewById(R.id.tvDescription);
        fabMyLoc = findViewById(R.id.fabMyLoc);


        //lambda function
        fabMyLoc.setOnClickListener(view -> {
            if (spLocations.getSelectedItemPosition() != 0) {  //Set default weather on weatherActivity() && Atleast one place name must be exist in spinner list
                spLocations.setSelection(0, true);
                getWeather(myLocation.getLatitude(), myLocation.getLongitude());
            }

            //for update weather wen ever we want
            getLocations();


        });
    }

    private void getWeather(double latitude, double longitude) {
        String url = "https://api.openweathermap.org/data/2.5/onecall?lat=" + latitude + "&lon=" + longitude + "&exclude=minutely,hourly,alerts&appid=" + getString(R.string.open_weather_api_key);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> { //lambda function used bcoz Response.Listener<JSONObject>() has only one method  onResponse() so we use lambda
            dismissProgressDialog();
            setData(response.toString());
        }, error -> {
            dismissProgressDialog();
            showToast(getVolleyError(error));
        });
        checkNetworkAndCall(() -> {
            showProgressDialog();
            Volley.newRequestQueue(this).add(jsonObjectRequest);
        }, true);
    }

    private String getVolleyError(VolleyError error) {
        String err = error.getMessage();
        if (error instanceof TimeoutError) {
            err = "Connection timeout";
        } else if (error instanceof NoConnectionError) {
            err = "No connection";
        } else if (error instanceof AuthFailureError) {
            err = "Authentication failure";
        } else if (error instanceof ServerError) {
            err = "Server error";
        } else if (error instanceof NetworkError) {
            err = "Network error";
        } else if (error instanceof ParseError) {
            err = "Server's response could not be parsed";
        }
        return err;
    }

    private void setData(String response) {

      /*
            Gson (also known as Google Gson) is a small
            Java-based library for parsing and creating JSON objects. It’s a serialization/deserialization
            library to convert Java Objects into JSON and back.*/

        MyWeather myWeather = new Gson().fromJson(response, MyWeather.class); //convert java object into json
        String temperature = kelvinToCelsius(myWeather.current.temp) + "\u00b0C" + " / " + kelvinToFahren(myWeather.current.temp) + "\u00b0F";
        String pressure = myWeather.current.pressure + " hPa";
        String humidity = myWeather.current.humidity + "%";
        city = getPlaceName(myWeather.lat, myWeather.lon);
        String weather = myWeather.current.weather.get(0).main;
        String description = myWeather.current.weather.get(0).description;
        String windspeed = myWeather.current.windSpeed + " m/sec";
        String clouds = myWeather.current.clouds + "%";
        int sunrise = myWeather.current.sunrise;
        int sunset = myWeather.current.sunset;
        date = myWeather.current.dt;
        String icon = myWeather.current.weather.get(0).icon;
        String icon_url = "https://openweathermap.org/img/w/" + icon + ".png";
        Glide.with(WeatherActivity.this).asBitmap().load(icon_url).into(ivIcon);
        tvTemperature.setText(temperature);
        tvPressure.setText(pressure);
        tvHumidity.setText(humidity);
        tvCity.setText(city);
        tvMain.setText(weather);
        tvDescription.setText(description);
        tvWindSpeed.setText(windspeed);
        tvClouds.setText(clouds);
        tvSunrise.setText(convertDate1(sunrise));
        tvSunset.setText(convertDate1(sunset));
        tvLast.setText("Last Updated at " + convertDate1(date));

        dailyList = myWeather.daily;
        ListViewAdapter listViewAdapter = new ListViewAdapter(WeatherActivity.this, R.layout.row_forecast, new String[dailyList.size()]);
        lvForecast.setAdapter(listViewAdapter);
        lvForecast.setOnItemClickListener(WeatherActivity.this);
    }

    private void setViewPager() {
        viewPager.setOffscreenPageLimit(viewPager.getChildCount() - 1);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter();
        viewPager.setAdapter(viewPagerAdapter);
        actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.addTab(actionBar.newTab().setText("Current Weather").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText("Weather Forecast").setTabListener(this));
        viewPager.setOnPageChangeListener(this);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        actionBar.setSelectedNavigationItem(i);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    //Work on list view dialog in weather forecast viewpager
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.dialog);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        //Set data to forecast dialog
        TextView tvDCity = alertDialog.findViewById(R.id.tvDCity);
        TextView tvDDateTime = alertDialog.findViewById(R.id.tvDDateTime);
        ImageView tvDIcon = alertDialog.findViewById(R.id.tvDIcon);
        TextView tvDmain = alertDialog.findViewById(R.id.tvDmain);
        TextView tvDTemp = alertDialog.findViewById(R.id.tvDTemp);
        TextView tvDPressure = alertDialog.findViewById(R.id.tvDPressure);
        TextView tvDHumidity = alertDialog.findViewById(R.id.tvDHumidity);
        TextView tvDWindSpeed = alertDialog.findViewById(R.id.tvDWindSpeed);
        TextView tvDCloudiness = alertDialog.findViewById(R.id.tvDCloudiness);
        TextView tvDupdate = alertDialog.findViewById(R.id.tvDupdate);
        TextView tvMorning = alertDialog.findViewById(R.id.tvMorning);
        TextView tvDay = alertDialog.findViewById(R.id.tvDay);
        TextView tvEvening = alertDialog.findViewById(R.id.tvEvening);
        TextView tvNight = alertDialog.findViewById(R.id.tvNight);
        TextView tvminTemp = alertDialog.findViewById(R.id.tvDMinTempForecast);
        tvMorning.setText(kelvinToCelsius(dailyList.get(position).temp.morn) + "\u00b0C");
        tvDay.setText(kelvinToCelsius(dailyList.get(position).temp.day) + "\u00b0C");
        tvEvening.setText(kelvinToCelsius(dailyList.get(position).temp.eve) + "\u00b0C");
        tvNight.setText(kelvinToCelsius(dailyList.get(position).temp.night) + "\u00b0C");
        tvDDateTime.setText(convertDate3(dailyList.get(position).dt));
        tvDPressure.setText(dailyList.get(position).pressure.toString() + " hPa");
        tvDHumidity.setText(dailyList.get(position).humidity.toString() + "%");
        tvDCloudiness.setText(dailyList.get(position).clouds.toString() + "%");
        tvDWindSpeed.setText(dailyList.get(position).windSpeed.toString() + " m/sec");
        Glide.with(WeatherActivity.this).load("https://openweathermap.org/img/w/" + dailyList.get(position).weather.get(0).icon + ".png").into(tvDIcon);
        tvDmain.setText(dailyList.get(position).weather.get(0).description);
//        tvDTemp.setText(kelvinToCelsius((dailyList.get(position).temp.min + dailyList.get(position).temp.max) / 2) + "\u00b0C");
        tvDTemp.setText(kelvinToCelsius(dailyList.get(position).temp.max) + "\u00b0C");  //max temp
        tvminTemp.setText(kelvinToCelsius(dailyList.get(position).temp.min)+"\u00b0C");  //min temp
        tvDCity.setText(city);
        tvDupdate.setText("Last Updated at " + convertDate1(date)); //set last update time
    }

    String convertDate1(int longsec) {
        Date d = new Date(longsec * 1000L);
        SimpleDateFormat df = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return df.format(d);
    }

    String convertDate2(int longsec) {
        Date d = new Date(longsec * 1000L);
        SimpleDateFormat df = new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault());
        return df.format(d);
    }

    String convertDate3(int longsec) {
        Date d = new Date(longsec * 1000L);
        SimpleDateFormat df = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
        return df.format(d);
    }

    public String kelvinToCelsius(double kelvin) {
        double dCelsius = kelvin - 273.15f;
        return String.format(Locale.getDefault(), "%.0f", dCelsius);
    }

    public String kelvinToFahren(double kelvin) {
        double dCelsius = kelvin - 273.15f;
        double dFahren = (dCelsius * 9 / 5) + 32;
        return String.format(Locale.getDefault(), "%.0f", dFahren);
    }

    public String getPlaceName(double lat, double lon) {
        String placename = "";
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> ads = geocoder.getFromLocation(lat, lon, 1);
            if (ads != null && ads.size() > 0) {
                Address address = ads.get(0);
                placename = address.getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return placename;
    }

    private class ViewPagerAdapter extends PagerAdapter {

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            int[] ids = {R.id.tabCurrent, R.id.tabForecast};
            return findViewById(ids[position]);
        }

        @Override
        public int getCount() {
            return viewPager.getChildCount();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }
    }


    //set data to row_layout of weather forecasr
    private class ListViewAdapter extends ArrayAdapter {
        public ListViewAdapter(Context context, int resource, Object[] objects) {
            super(context, resource, objects);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = getLayoutInflater().inflate(R.layout.row_forecast, null);
            TextView tvDateTime = v.findViewById(R.id.tvDateTime);
            TextView tvMainForecast = v.findViewById(R.id.tvMainForecast);
            ImageView ivForecast = v.findViewById(R.id.ivForecast);
            TextView tvTempForecast = v.findViewById(R.id.tvTempForecast);
            TextView tvMinTemp=v.findViewById(R.id.tvMinTempForecast);

//            tvTempForecast.setText(kelvinToCelsius((dailyList.get(position).temp.min + dailyList.get(position).temp.max) / 2) + "\u00b0C");  // show on row forecast average day temperature
            tvTempForecast.setText(kelvinToCelsius(dailyList.get(position).temp.max)+ "\u00b0C");  // show on row forecast average day temperature
            tvMinTemp.setText(kelvinToCelsius(dailyList.get(position).temp.min)+ "\u00b0C");

            tvDateTime.setText(convertDate2(dailyList.get(position).dt));
            tvMainForecast.setText(dailyList.get(position).weather.get(0).description);
            Glide.with(WeatherActivity.this).asBitmap().load("https://openweathermap.org/img/w/" + dailyList.get(position).weather.get(0).icon + ".png").into(ivForecast);
            return v;
        }
    }
}
