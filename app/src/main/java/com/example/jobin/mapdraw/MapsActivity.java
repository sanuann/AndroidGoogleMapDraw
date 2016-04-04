package com.example.jobin.mapdraw;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback, OnMapClickListener, OnMapLongClickListener {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MapsActivity";
    private boolean placeMarkerFlag;
    private GoogleMap mMap;
    private ArrayList<LatLng> arrayPoints;
    private PolylineOptions polylineOptions;
    private MarkerOptions markerOptions;
    private LatLng latLng;


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
                        InputMethodManager imm = (InputMethodManager) v.getContext()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        new GeocoderTask(latLng, markerOptions, mMap, v.getContext()).execute
                                (location);
                    }
                    return true;
                }
                return false;
            }
        };
        // Setting editText search click event listener for the keyboard search icon
        etLocation.setOnEditorActionListener(findClickListener);


        // Getting reference to RadioGroup of the layout activity_maps and set map type selected
        RadioGroup rgViews = (RadioGroup) findViewById(R.id.rg_views);
        rgViews.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_normal) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                } else if (checkedId == R.id.rb_hybrid) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                } else if (checkedId == R.id.rb_satellite) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else if (checkedId == R.id.rb_terrain) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                }
            }
        });

        // Getting reference to place marker and draw line button and set corresponding flag
        Button place_marker = (Button) findViewById(R.id.btn_marker);
        place_marker.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                placeMarkerFlag = true;
            }

        });

        Button draw_line = (Button) findViewById(R.id.btn_line);
        draw_line.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                placeMarkerFlag = false;
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
            // Sets default map type to be "hybrid"
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            //mMap.setIndoorEnabled(true);
            //mMap.setTrafficEnabled(true);
            // 3D building
            //mMap.setBuildingsEnabled(true);
            // Get zoom button
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.setOnMapClickListener(this);
            mMap.setOnMapLongClickListener(this);
        }
    }

    @Override
    public void onMapClick(final LatLng point) {

        if (placeMarkerFlag) { // place marker only
            final MarkerOptions marker = new MarkerOptions();
            AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create(); //Read Update

            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Default Icon", new
                    AlertDialogOnClickListener(point, marker, mMap));
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Custom Icon", new
                    AlertDialogOnClickListener(point, marker, mMap));
            alertDialog.show();
        }

        // draw lines to connect the points on map
        else {
            // setting polyline in the map
            polylineOptions = new PolylineOptions();
            polylineOptions.color(Color.RED);
            polylineOptions.width(5);
            arrayPoints.add(point);
            polylineOptions.addAll(arrayPoints);
            mMap.addPolyline(polylineOptions);
        }

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

}
