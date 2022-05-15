package com.manish.weatherforecast.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.WindowManager;

import com.google.android.datatransport.runtime.scheduling.jobscheduling.SchedulerConfig;
import com.google.android.material.snackbar.Snackbar;
import com.manish.weatherforecast.R;
import com.manish.weatherforecast.base.BaseActivity;
import com.manish.weatherforecast.location_classes.MyLocationClass;

public class SplashActivity extends BaseActivity {
    int LOCATION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //check whether internet is on or not

        checkNetworkAndCall(this::requestLocation, true);
    }

        private void requestLocation() {
            checkPermissionAndCall(Manifest.permission.ACCESS_FINE_LOCATION, true, () -> new MyLocationClass(SplashActivity.this, this::onLocationSuccess, this::onLocationFailed, LOCATION_REQUEST_CODE));
        }

        void onLocationSuccess(Location myLocation) {
            Intent in = new Intent(SplashActivity.this, WeatherActivity.class);
            in.putExtra("myLocation", myLocation);
            startActivity(in);
            finish();
        }

        void onLocationFailed(String errorMessage) {
            showToast(errorMessage);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == LOCATION_REQUEST_CODE) {
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        requestLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        Snackbar.make(findViewById(android.R.id.content), "Location access required", Snackbar.LENGTH_INDEFINITE).setAction("Allow", view -> {
                            requestLocation();
                        }).show();
                        break;
                }
            }
    }
}