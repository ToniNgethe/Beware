package com.example.toni.beware;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener
{

    private GoogleMap mMap;
    private CardView card;
    private TextView loc;
    private GoogleApiClient googleApiClient;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private LocationRequest locationRequest;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 100;
    private Double myLongitude = null;
    private static final int MY_PERMISSIONS_FINE_LOCATION = 1;
    private Double myLatitude = null;
    private LatLng toCoordinated, fromCoordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton fb = (FloatingActionButton) findViewById(R.id.floatingActionButton2);
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, PostIssue.class));
            }
        });

        card = (CardView) findViewById(R.id.card_maps);
        loc = (TextView) findViewById(R.id.textView_maps);

        //google api client
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //location req
        locationRequest = new LocationRequest();
        locationRequest.setInterval(15 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                            .setTypeFilter(Place.TYPE_COUNTRY).setCountry("KE")
                            .build();


                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                    .setFilter(typeFilter)
                                    .build(MapsActivity.this);


                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);

                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error

                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.

                }

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                final Place place = PlaceAutocomplete.getPlace(this, data);
               // Log.i(LOG_TAG, "Place: " + place.getName())

                loc.setText(place.getName());

                //remove maker fast
                //add marrker to map
                Places.GeoDataApi.getPlaceById(googleApiClient, place.getId())
                        .setResultCallback(new ResultCallback<PlaceBuffer>() {
                            @Override
                            public void onResult(PlaceBuffer places) {
                                if (places.getStatus().isSuccess()) {
                                    final Place myPlace = places.get(0);

                                    fromCoordinates = myPlace.getLatLng();

                                    Log.v("Latitude is", "" + fromCoordinates.latitude);
                                    Log.v("Longitude is", "" + fromCoordinates.longitude);

                                    mMap.addMarker(new MarkerOptions().position(fromCoordinates).title(place.getAddress().toString()));
                                    // mMap.animateCamera(CameraUpdateFactory.newLatLng(queriedLocation));
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fromCoordinates, 15));
                                }
                                places.release();
                            }
                        });


            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {

                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.

            } else if (resultCode == RESULT_CANCELED) {

                // The user canceled the operation.
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in kenya and move the camera
        LatLng kenya = new LatLng(-0.023559, 37.90619300000003);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(kenya));

        mMap.addMarker(new MarkerOptions().position(kenya).title("Kenya"));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);

        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, MY_PERMISSIONS_FINE_LOCATION);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_FINE_LOCATION:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    Toast.makeText(MapsActivity.this, "This app requires location permissions to be granted", Toast.LENGTH_SHORT).show();
                    finish();

                }

                break;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (googleApiClient.isConnected()) {
            requestLocationUpdates();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        googleApiClient.disconnect();
    }

    @Override
    public void onLocationChanged(Location location) {

        myLatitude = location.getLatitude();
        myLongitude = location.getLongitude();

    }

    private void requestLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }

    }
}
