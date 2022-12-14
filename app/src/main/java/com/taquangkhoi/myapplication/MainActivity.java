package com.taquangkhoi.myapplication;

import static java.util.Arrays.*;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    GoogleMap mMap;
    SupportMapFragment mapFragment;
    Spinner spinner;
    Geocoder geocoder;
    SearchView searchView;
    ImageButton btnExit, ibtnSchool;
    ImageView ibtnSearch;

    private FirebaseAuth mAuth;
    private static int AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final String TAG = "MainActivity";

    Place place;

    private final OkHttpClient client = new OkHttpClient();

    // FusedLocationProviderClient - Main class for receiving location updates.
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    Location currentLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        // D??ng try catch ????? tr??nh b??? crash app
        try {
            requestLocationPermission();
        } catch (Exception e) {
            e.printStackTrace();
        }

        requestLocation();

        addControls();
        addEvents();

        // Hi???n map tr??n m??n h??nh
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest.Builder(TimeUnit.SECONDS.toMillis(60))
                .build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }

            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult.getLastLocation() != null) {
                    //currentLocation = locationResult.getLocations()[0];
                    double latitude = currentLocation.getLatitude();
                    double longitude = currentLocation.getLongitude();
                    Log.i(TAG, "onLocationResult: " + latitude + " " + longitude);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            Log.i(TAG, "onSuccess getLastLocation: " + location.getLatitude() + " " + location.getLongitude());
                            currentLocation = location;
                            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            List<Address> addresses = null;
                            try {
                                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String cityName = addresses.get(0).getLocality();
//                            textCityName1.setText(cityName);
                            String stateName = addresses.get(0).getAddressLine(0);
                            String countryName = addresses.get(0).getCountryName();

                            Log.i(TAG, "onSuccess: " + cityName + ", " + stateName + ", " + countryName);
                        } else {
                            Log.i(TAG, "onSuccess getLastLocation: null");
                        }
                    }
                });


    }

    private void addEvents() {
        // S??? ki???n khi ch???n item trong spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String type = spinner.getSelectedItem().toString();
                switch (type) {
                    case "Roadmap":
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case "Satellite":
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                    case "Terrain":
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                    case "Hybrid":
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Log.i("TAG", "onClick to Logout: " + mAuth.getCurrentUser());
                if (mAuth.getCurrentUser() == null) {
                    Toast.makeText(MainActivity.this, "????ng xu???t th??nh c??ng", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        ibtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSearchCalled();
            }
        });

        ibtnSchool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    searchSchool();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Intent intent = new Intent(MainActivity.this, SchoolActivity.class);
                //startActivity(intent);
            }
        });
    }

    private void addControls() {
        spinner = findViewById(R.id.Spinner);
        btnExit = findViewById(R.id.ibtnExit);
        ibtnSearch = findViewById(R.id.ibtnSearch);
        ibtnSchool = findViewById(R.id.ibtnSchool);

        // T???o m???ng ????? l??u d??? li???u
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Roadmap");
        arrayList.add("Satellite");
        arrayList.add("Hybrid");
        arrayList.add("Terrain");

        // ArrayAdapter d??ng ????? hi???n th??? d??? li???u l??n Spinner
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayList);
        spinner.setAdapter(arrayAdapter);

        geocoder = new Geocoder(this);

    }

    // H??m ch???y khi Map ???? s???n s??ng
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Th??m n??t Thu nh??? ph??ng to
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Th??m c??? ch??? thu nh???, ph??ng to - Zoom
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        mMap.setOnMarkerClickListener(this);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.API_KEY);
        }

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        // Th??m My Location Button
        addMyLocationButton();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

            }
        });
    }

    public void onSearchCalled() {
        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .setCountry("VN")
                .setTypeFilter(TypeFilter.CITIES)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + ", " + place.getAddress());
                Toast.makeText(MainActivity.this, "ID: " + place.getId() + "address:" + place.getAddress() +
                        "Name:" + place.getName() + " latlong: " + place.getLatLng(), Toast.LENGTH_LONG).show();
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
                String address = place.getAddress();
                // do query with address

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(MainActivity.this, "Error: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    // Event T??m ki???m ?????a ??i???m c???a SearchView
    private void addSearchViewEvents() {

    }

    // Y??u c???u quy???n truy c???p v??? tr??
    private void requestLocationPermission() {
        // Ki???m tra SDK
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        // Ki???m tra quy???n ???? ???????c ch???p thu???n ch??a
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            addMyLocationButton();
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            // N???u ch??a ???????c ch???p thu???n th?? y??u c???u ch???p thu???n
            String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permissions, 1);
        }
    }

    // H??m ????? th??m n??t MyLocationButton
    private void addMyLocationButton() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != getPackageManager().PERMISSION_GRANTED

                &&

                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != getPackageManager().PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    // Ki???m tra k???t qu??? Permission ???? cho cho ph??o hay ch??a
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addMyLocationButton();
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method y??u c???u b???t GPS
    private void requestLocation() {
        // LocationRequest Builder
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY)
                .setIntervalMillis(1000)
                .build();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        // L???y setting t??? m??y, ????? bi???t GPS ???? b???t ch??a
        SettingsClient client = LocationServices.getSettingsClient(this);

        // Task d??ng ????? m??? lu???ng (thread) m???i ????? ki???m tra GPS
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        // N???u GPS ch??a b???t th?? m??? c???a s??? y??u c???u b???t GPS
        // th??m listener ????? b???t s??? ki???n khi ng?????i d??ng ch??a b???t GPS (v?? Task fail)
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                51);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Log.i("Marker", "Marker Clicked");
        if (place != null) {
            String placeId = place.getId();
            try {
                showBottomSheetDialog(placeId);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private void showBottomSheetDialog(String placeId) throws InterruptedException {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.location_info);

        TextView tvwAdress, tvwOpenHours, tvwWebsite, tvwPhone;
        tvwAdress = bottomSheetDialog.findViewById(R.id.tvw_address);
        tvwOpenHours = bottomSheetDialog.findViewById(R.id.tvw_open_hours);
        tvwWebsite = bottomSheetDialog.findViewById(R.id.tvw_website);
        tvwPhone = bottomSheetDialog.findViewById(R.id.tvw_email);

        String urlGetPlaceDetail = "https://maps.googleapis.com/maps/api/place/details/json" +
                "?place_id=" + placeId +
                "&key=" + BuildConfig.API_KEY;

        Log.i(TAG, "URL " + urlGetPlaceDetail);

        Request request = new Request.Builder()
                .url(urlGetPlaceDetail)
                .build();

        final String[] response = {null};
        Bundle bundle = new Bundle();

        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    response[0] = runTest(urlGetPlaceDetail);

                    // parse json
                    JSONObject obj = new JSONObject(response[0]);
                    Log.i(TAG, "showBottomSheetDialog run: " + obj.toString());

                    bundle.putString("address", obj.getJSONObject("result").getString("formatted_address"));
                    if (obj.getJSONObject("result").has("opening_hours")) {
                        bundle.putString("open_hours", "??ang m???");
                    } else {
                        bundle.putString("open_hours", "Kh??ng c?? th??ng tin");
                    }
                    if (obj.getJSONObject("result").has("website")) {
                        bundle.putString("website", obj.getJSONObject("result").getString("website"));
                    } else {
                        bundle.putString("website", "Kh??ng c?? th??ng tin");
                    }
                    if (obj.getJSONObject("result").has("international_phone_number")) {
                        bundle.putString("phone", obj.getJSONObject("result").getString("international_phone_number"));
                    } else {
                        bundle.putString("phone", "Kh??ng c?? th??ng tin");
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        thread.join();

        tvwAdress.setText(bundle.getString("address"));
        tvwOpenHours.setText(bundle.getString("open_hours"));
        tvwWebsite.setText(bundle.getString("website"));
        tvwPhone.setText(bundle.getString("phone"));

        bottomSheetDialog.show();
    }

    private String runTest(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public void searchSchool() throws InterruptedException {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        String urlNearbySearchSchool = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?key=" + BuildConfig.API_KEY +
                "&location=" + currentLocation.getLatitude() +
                "%2C" + currentLocation.getLongitude() +
                "&radius=500" +
                "&type=school";

        Log.i(TAG, "searchSchool URL " + urlNearbySearchSchool);

        Request request = new Request.Builder()
                .url(urlNearbySearchSchool)
                .build();

        final String[] response = {null};
        List<School> schools = new ArrayList<>();

        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    response[0] = runTest(urlNearbySearchSchool);

                    // parse json
                    JSONObject obj = new JSONObject(response[0]);
                    Log.i(TAG, "searchSchool run: " + obj);

                    JSONArray results = obj.getJSONArray("results");

                    for (int i = 0; i < (results.length() < 10 ? results.length() : 10); i++) {
                        Log.i(TAG, "searchSchool run: " + i + " " + results.getJSONObject(i).getString("name")
                                + " " + results.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lat")
                                + " " + results.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lng")
                                + " " + results.getJSONObject(i).getString("vicinity")
                        );
                        schools.add(new School(
                                results.getJSONObject(i).getString("name"),
                                results.getJSONObject(i).getString("vicinity"),
                                results.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lat"),
                                results.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lng"),
                                results.getJSONObject(i).getString("place_id")
                        ));
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        thread.join();

        // get current LatLng
        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

        for (School school : schools) {
            mMap.addMarker(new MarkerOptions()
                    .position(school.getLatLng())
                    .title(school.getName())
                    .snippet(school.getAddress())
                    );
        }

    }
}