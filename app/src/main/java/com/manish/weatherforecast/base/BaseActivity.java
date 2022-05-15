package com.manish.weatherforecast.base;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.manish.weatherforecast.receiver.NetworkChangeReceiver;

public class BaseActivity extends AppCompatActivity {

    public ProgressDialog progressDialog;
    Runnable networkCallback, permCallback;
    Snackbar networkSnackbar;
    boolean shouldRetryPermission;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        IntentFilter intentFilter = new IntentFilter();

        //Broadcast receiver set
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new NetworkChangeReceiver() {
            @Override
            public void onNetworkChange() {
                checkNetworkStatus();       //check network active or not
                if (networkCallback != null)
                    checkNetworkAndCall(networkCallback, true);
            }
        }, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNetworkStatus();
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
        return networkCapabilities != null && (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
    }

    public void checkNetworkStatus() {
        if (!isNetworkConnected())
            showToast("Network Disconnected!");
    }

    public void showProgressDialog() {
        dismissProgressDialog();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public void showSnackBar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    public void showSnackBarWithAction(String message, String actionText, Runnable action) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).setAction(actionText, view -> action.run()).show();
    }

    public void moveIntent(Class<?> clas, boolean isFinish) {
        Intent intent = new Intent(this, clas);
        startActivity(intent);
        if (isFinish)
            finish();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            getCurrentFocus().clearFocus();
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    public void checkNetworkAndCall(Runnable callback, boolean shouldRetry) {
        networkCallback = null;
        if (networkSnackbar != null)
            networkSnackbar.dismiss();

        if (shouldRetry)
            networkSnackbar = Snackbar.make(findViewById(android.R.id.content), "No Internet Connection", Snackbar.LENGTH_INDEFINITE).setAction("Retry", view -> checkNetworkAndCall(callback, true));
        else
            networkSnackbar = Snackbar.make(findViewById(android.R.id.content), "No Internet Connection", Snackbar.LENGTH_SHORT);

        if (isNetworkConnected())
            callback.run();
        else {
            if (shouldRetry)
                networkCallback = callback;
            networkSnackbar.show();
        }
    }

    public void checkPermissionAndCall(String permission, boolean shouldRetry, Runnable callback) {
        shouldRetryPermission = shouldRetry;
        if (callback == null)
            callback = permCallback;
        permCallback = null;
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            if (callback != null)
                callback.run();
        } else {
            permCallback = callback;
            requestPermissions(new String[]{permission}, 12345);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                checkPermissionAndCall(permissions[0], shouldRetryPermission, permCallback);
            else {
                int snackBarLength = Snackbar.LENGTH_LONG;
                if (shouldRetryPermission)
                    snackBarLength = Snackbar.LENGTH_INDEFINITE;

                if (shouldShowRequestPermissionRationale(permissions[0]))
                    Snackbar.make(findViewById(android.R.id.content), permissions[0].substring(19) + " permission required", snackBarLength).setAction("Allow", view -> checkPermissionAndCall(permissions[0], shouldRetryPermission, permCallback)).show();
                else
                    Snackbar.make(findViewById(android.R.id.content), "Enable " + permissions[0].substring(19) + " permission in settings", snackBarLength).setAction("Settings", view -> {
                        checkPermissionAndCall(permissions[0], shouldRetryPermission, permCallback);
                        goToSettings();
                    }).show();
            }
        }
    }

    public void goToSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(myAppSettings);
    }
}
