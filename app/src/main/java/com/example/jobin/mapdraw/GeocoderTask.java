package com.example.jobin.mapdraw;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

/**
 * Created by Sanu on 4/3/16.
 */
// An AsyncTask class for accessing the GeoCoding Web Service
public class GeocoderTask extends AsyncTask<String, Void, List<Address>> {
    private LatLng latLng;
    private MarkerOptions markerOptions;
    private GoogleMap mMap;
    private Context context;

    public GeocoderTask() {
        super();
    }

    public GeocoderTask(LatLng latLng, MarkerOptions markerOptions, GoogleMap mMap, Context context) {
        super();
        this.latLng = latLng;
        this.markerOptions = markerOptions;
        this.mMap = mMap;
        this.context = context;

    }

    @Override
    protected List<Address> doInBackground(String... locationName) {
        // Creating an instance of Geocoder class
        Geocoder geocoder = new Geocoder(context);
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
            Toast.makeText(context, "No Location found", Toast.LENGTH_SHORT).show();
        }

        // Adding Markers on Google Map for each matching address
        for (int i = 0; i < addresses.size(); i++) {

            Address address = addresses.get(i);

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
