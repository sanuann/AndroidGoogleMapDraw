package com.example.jobin.mapdraw;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback, OnMapClickListener, OnMapLongClickListener {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private ArrayList<LatLng> arrayPoints = null;
    PolylineOptions polylineOptions;
    MarkerOptions markerOptions;
    LatLng latLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        arrayPoints = new ArrayList<LatLng>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map); // get handle to the fragment and pass it to resource id of the <fragment> element
        mapFragment.getMapAsync(this); // set the callback on the fragment

        // Getting reference to editText of the layout activity_maps
        final EditText etLocation = (EditText) findViewById(R.id.et_location);


        // Defining editText search event listener for the editText
        OnEditorActionListener findClickListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // Getting user input location
                    String location = etLocation.getText().toString();

                    if (location != null && !location.equals("")) {
                        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        new GeocoderTask().execute(location);
                    }
                    return true;
                }
                return false;
            }
        };

        // Setting editText search click event listener for the keyboard search icon
        etLocation.setOnEditorActionListener(findClickListener);

        Button place_button = (Button) findViewById(R.id.place_icon);
        place_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create(); //Read Update

                class alertDialogOnClickListener implements DialogInterface.OnClickListener {
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //<do something>;
                                Toast.makeText(getApplicationContext(), "you have pressed Default", Toast.LENGTH_SHORT).show();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //<do something>;
                                Toast.makeText(getApplicationContext(), "you have pressed Custom", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Default Icon", new alertDialogOnClickListener());
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Custom Icon", new alertDialogOnClickListener());
                alertDialog.show();
            }

        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (checkPlayServices()) {
            mMap = googleMap;
            // Sets the map type to be "hybrid"
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            googleMap.setIndoorEnabled(true);
            googleMap.setTrafficEnabled(true);
            // 3D building
            googleMap.setBuildingsEnabled(true);
            // Get zoom button
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.setOnMapClickListener(this);
            mMap.setOnMapLongClickListener(this);
        }
    }

    @Override
    public void onMapClick(final LatLng point) {
        final MarkerOptions marker = new MarkerOptions();
        AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create(); //Read Update

        class alertDialogOnClickListener implements DialogInterface.OnClickListener {
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //add marker

                        marker.position(point)
                                .draggable(true);
                        mMap.addMarker(marker);
                        Toast.makeText(getApplicationContext(), "you have pressed Default", Toast.LENGTH_SHORT).show();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //add marker
                        BitmapDescriptor custom_icon = BitmapDescriptorFactory.fromResource(R.drawable.map_marker_flag_icon);
                        marker.position(point)
                                .icon(custom_icon)
                                .draggable(true);
                        mMap.addMarker(marker);
                        Toast.makeText(getApplicationContext(), "you have pressed Custom", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Default Icon", new alertDialogOnClickListener());
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Custom Icon", new alertDialogOnClickListener());
        alertDialog.show();


        // setting polyline in the map
        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        polylineOptions.width(5);
        arrayPoints.add(point);
        polylineOptions.addAll(arrayPoints);
        mMap.addPolyline(polylineOptions);
    }

    @Override
    public void onMapLongClick(LatLng point) {
        mMap.clear();
        arrayPoints.clear();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Normal_Map:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.Hybrid_Map:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.Satellite_Map:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.Terrain_Map:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            case R.id.None_Map:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    // An AsyncTask class for accessing the GeoCoding Web Service
    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;

            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {

            if (addresses == null || addresses.size() == 0) {
                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
            }

            // Adding Markers on Google Map for each matching address
            for (int i = 0; i < addresses.size(); i++) {

                Address address = (Address) addresses.get(i);

                // Creating an instance of GeoPoint, to display in Google Map
                latLng = new LatLng(address.getLatitude(), address.getLongitude());

                String addressText = String.format("%s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getCountryName());

                markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(addressText);

                mMap.addMarker(markerOptions);

                // Locate the first location
                if (i == 0) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                }
            }
        }
    }
}
