package com.manish.weatherforecast.activities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.manish.weatherforecast.R;
import com.manish.weatherforecast.base.BaseActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocationActivity extends BaseActivity {
    FloatingActionButton fabAdd;
    ListView lvLocations;
    SQLiteDatabase sqLiteDatabase;
    ArrayList<String> placeNames = new ArrayList<>();
    TextView tvPlaceholder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        fabAdd = findViewById(R.id.fabAdd);
        lvLocations = findViewById(R.id.lvLocation);
        tvPlaceholder = findViewById(R.id.tvPlaceholder);

        getSupportActionBar().setTitle("Locations");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sqLiteDatabase = openOrCreateDatabase("Database", MODE_PRIVATE, null);
        sqLiteDatabase.execSQL("Create table if not exists tbname (PlaceName varchar,Lattitude varchar, Longitude varchar);");
        setList();

        fabAdd.setOnClickListener(view -> checkNetworkAndCall(this::showAddPlaceDialog, false)); //Check network connection enable or not
    }

    private void showAddPlaceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LocationActivity.this);
        builder.setTitle("Search Place");
        builder.setView(R.layout.add_location_dialog);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        final EditText etPlaceName = alertDialog.findViewById(R.id.etPlaceName);
        Button btSearch = alertDialog.findViewById(R.id.btSearch);
        final ListView lvLocations = alertDialog.findViewById(R.id.lvLocations);
        btSearch.setOnClickListener(view1 -> {
            etPlaceName.setError(null);
            String placeName = etPlaceName.getText().toString();
            if (placeName.trim().equals("")) {
                etPlaceName.setError("Place Name Required");
                etPlaceName.requestFocus();
                return;
            }
            showProgressDialog();
            Geocoder geocoder = new Geocoder(LocationActivity.this);  //Geocoder class helps in getting city name and longitude, latitude
            try {
                List<Address> addresses = geocoder.getFromLocationName(placeName, 10); //get address of placeName  into address
                if (addresses.size() == 0) {  //If city not found or address not contain any value
                    etPlaceName.setError("Place Not Found");
                    etPlaceName.requestFocus();
                    dismissProgressDialog();
                    return;
                }
                final ArrayList<String> placeNames1 = new ArrayList<>();
                final ArrayList<Double> placeLats1 = new ArrayList<>();
                final ArrayList<Double> placeLons1 = new ArrayList<>();
                for (int p = 0; p < addresses.size(); p++) {
                    Address address = addresses.get(p);
                    String placeAddress = address.getLocality() + ", " + address.getAdminArea() + ", " + address.getCountryName();  //getAdminArear() used for getting state name and getAdminArea() used for getting local search place name
                    double placeLat = address.getLatitude();
                    double placeLon = address.getLongitude();

                    //added place and their latiude and longitude into list so we can get thier wether detail
                    placeNames1.add(placeAddress);
                    placeLats1.add(placeLat);
                    placeLons1.add(placeLon);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(LocationActivity.this, android.R.layout.simple_list_item_1, placeNames1);
                lvLocations.setAdapter(adapter);

                progressDialog.dismiss();
                lvLocations.setOnItemClickListener((adapterView, view11, i, l) -> {
                    sqLiteDatabase.execSQL("Insert into tbname values ('" + placeNames1.get(i) + "' , '" + placeLats1.get(i).toString() + "','" + placeLons1.get(i).toString() + "');");
                    alertDialog.dismiss();
                    setList();
                });

            } catch (IOException e) {
                Toast.makeText(LocationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setList() {
        Cursor cursor = sqLiteDatabase.rawQuery("Select * from tbname", null);
        if (cursor.getCount() == 0)
            tvPlaceholder.setVisibility(View.VISIBLE);
        else
            tvPlaceholder.setVisibility(View.GONE);
        placeNames = new ArrayList<>();
        while (cursor.moveToNext()) {
            placeNames.add(cursor.getString(0));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(LocationActivity.this, android.R.layout.simple_list_item_1, placeNames);
        lvLocations.setAdapter(adapter);
        registerForContextMenu(lvLocations);
        cursor.close();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add("Delete");
    }

    //delete place name from location list
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        sqLiteDatabase.execSQL("Delete from tbname where PlaceName = '" + placeNames.get(menuInfo.position) + "'");
        setList();
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
