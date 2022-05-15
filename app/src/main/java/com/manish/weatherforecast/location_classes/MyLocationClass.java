package com.manish.weatherforecast.location_classes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MyLocationClass {
    Consumer<Location> onLocationSuccess;
    Consumer<String> onLocationFailed;
    Context context;
    int REQUEST_CODE;

    /*

     */
    private FusedLocationProviderClient fusedLocationClient; //Get last location of the user
    private LocationCallback locationCallback;  //Used for receiving notifications from the FusedLocationProviderApi when the device location has changed or can no longer
                                                // be determined. The methods are called if the LocationCallback has been registered with the location client
                                                //using the requestLocationUpdates(GoogleApiClient, LocationRequest, LocationCallback, Looper) method.

    public MyLocationClass(Context context, Consumer<Location> onLocationSuccess, Consumer<String> onLocationFailed, int REQUEST_CODE) {
        this.onLocationSuccess = onLocationSuccess;
        this.onLocationFailed = onLocationFailed;
        this.context = context;
        this.REQUEST_CODE = REQUEST_CODE;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        locationCallback = new LocationCallback() {
            /*
                public void onLocationResult (LocationResult result)
                    Called when device location information is available.
                ->he most recent location returned by getLastLocation() is not guaranteed to be immediately fresh, but will be reasonably up to date given the
                hints specified by the active LocationRequests.
                Parameters
                    result	The latest location result available.
             */
            @Override
            public void onLocationResult(LocationResult locationResult) {
                fusedLocationClient.removeLocationUpdates(locationCallback);
                if (locationResult == null)
                    onLocationFailed.accept("Location not found");
                else
                    onLocationSuccess.accept(locationResult.getLastLocation());  //location available
            }
        };
        getLastLocation();
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!=null)
                    onLocationSuccess.accept(location);
                else
                    createLocationRequest();
            }
        });
        fusedLocationClient.getLastLocation().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                createLocationRequest();
            }
        });
    }

    @SuppressLint("MissingPermission")
    protected void createLocationRequest() {
        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        SettingsClient client = LocationServices.getSettingsClient(context);

        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(locationSettingsResponse -> fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper()));

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                    if(e instanceof ResolvableApiException){
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult((Activity) context, REQUEST_CODE);
                        } catch (IntentSender.SendIntentException sendEx) {
                            onLocationFailed.accept(sendEx.getLocalizedMessage());
                        }
                }
            }
        });
    }

}
