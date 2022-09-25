package com.taquangkhoi.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    GoogleMap mMap;
    SupportMapFragment mapFragment;
    Spinner spinner;
    Geocoder geocoder;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addControls();
        addEvents();

        //
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void addEvents() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String type = spinner.getSelectedItem().toString();
                switch (type) {
                    case "Normal":
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
        SearchViewEvents();
    }

    private void addControls() {
        spinner = findViewById(R.id.Spinner);
        searchView = findViewById(R.id.searchView);

        // Tạo mảng để lưu dữ liệu
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Roadmap");
        arrayList.add("Satellite");
        arrayList.add("Hybrid");
        arrayList.add("Terrain");

        // ArrayAdapter dùng để hiển thị dữ liệu lên Spinner
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayList);
        spinner.setAdapter(arrayAdapter);

        geocoder = new Geocoder(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Thêm nút Thu nhỏ phóng to
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Thêm cử chỉ thu nhỏ, phóng to - Zoom
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        // Thêm My Location Button
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != getPackageManager().PERMISSION_GRANTED

                &&

                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != getPackageManager().PERMISSION_GRANTED)
        {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    // Tìm kiếm địa điểm
    private void SearchViewEvents() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                try {
                    String location = searchView.getQuery().toString();
                    List<Address> addresses = geocoder.getFromLocationName(location, 1);
                    Address address = addresses.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 80));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }
}