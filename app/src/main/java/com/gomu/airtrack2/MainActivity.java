package com.gomu.airtrack2;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationRequest;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    Marker currentMarker;

    private MapView mapView;
    private GoogleMap googleMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int PERMISSIONS_REQUEST_LOCATION = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    String coordinates = "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude();
                    //locationTextView.setText("Location: " + coordinates);
                    System.out.println("period location " + coordinates);

                    String cell_info;
                    int cell_id = getBaseStationInfo();
                    getNetType();

                    cell_info = "CellID : " + Integer.toString(cell_id);
                    LatLng latlng = new LatLng( location.getLatitude(), location.getLongitude());
                    googleMap.addMarker(new MarkerOptions().position(latlng).title(cell_info));

                    // Move the camera to the marker's position and zoom in
                    googleMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(latlng, 12));



                }
            }
        };
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        this.googleMap = googleMap;

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            showCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Add a marker at a specific location
        LatLng location = new LatLng(37.7749, -122.4194); // San Francisco coordinates
        googleMap.addMarker(new MarkerOptions().position(location).title("Marker in San Francisco"));

        // Move the camera to the marker's position and zoom in
        googleMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(location, 12));
    }

    private void showCurrentLocation() {
        // Get the user's current location
        // You would typically use a LocationProvider here (e.g., FusedLocationProviderClient)

        // Example: Assuming you have the user's location in 'location'
        Location location = new Location("dummy");
        location.setLatitude(37.7749);
        location.setLongitude(-122.4194);

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.addMarker(new MarkerOptions().position(currentLatLng).title("Current Location"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
        System.out.println("현재 lat : " + currentLatLng.latitude + " longi : " + currentLatLng.longitude);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onMapReady(googleMap);
            }
        }
    }


    private void getNetType() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            int type = networkInfo.getType();

//            int type, int subtype, String typeName, String subtypeName
            System.out.println("getNetType: networkInfo.type -- " + networkInfo.getType());
            System.out.println("getNetType: networkInfo.subtype -- " + networkInfo.getSubtype());
            System.out.println("getNetType: networkInfo.typeName -- " + networkInfo.getTypeName());
            System.out.println("getNetType: networkInfo.subtypeName -- " + networkInfo.getSubtypeName());
            System.out.println("getNetType: networkInfo.reason -- " + networkInfo.getReason());
            System.out.println("getNetType: networkInfo.extraInfo -- " + networkInfo.getExtraInfo());

            if (type == ConnectivityManager.TYPE_WIFI) {
                System.out.println("getNetType: wifi 유형");
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                System.out.println("getNetType: 데이터유형");
            }
        } else {
            if (networkInfo == null) {
                System.out.println("getNetType: networkInfo为null");
            }
            if (networkInfo != null && !networkInfo.isConnected()) {
                System.out.println("getNetType: networkInfo not connected network");
            }

        }
    }

    private int getBaseStationInfo() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,}, 1);
            }
        }

//        int type = telephonyManager.getNetworkType();
//        System.out.println("getBaseStationInfo: getNetworkType -- "+type);

        String networkOperator = telephonyManager.getNetworkOperator();
        System.out.println("getBaseStationInfo: getNetworkOperator -- " + networkOperator);
        int mcc = Integer.parseInt(networkOperator.substring(0, 3));
        int mnc = Integer.parseInt(networkOperator.substring(3, 5));
        System.out.println("getBaseStationInfo: mcc -- " + mcc);
        System.out.println("getBaseStationInfo: mnc -- " + mnc);

        int cell_id=0;
        List<CellInfo> cellList = telephonyManager.getAllCellInfo();
        //Log.i(TAG, "getBaseStationInfo: getAllCellInfo -- " + cellList);
        if (cellList != null && cellList.size() > 0) {
            for (CellInfo info : cellList) {
                if (info instanceof CellInfoLte) {
                    CellIdentityLte lte = ((CellInfoLte) info).getCellIdentity();
                    if(lte.getCi() != 0) cell_id = lte.getCi();
                }
                else if(info instanceof CellInfoWcdma) {
                    CellIdentityWcdma wcdma = ((CellInfoWcdma) info).getCellIdentity();
                    if(wcdma.getCid() != 0)  cell_id = wcdma.getCid();
                }
                else if(info instanceof CellInfoCdma) {
                    CellIdentityCdma cdma = ((CellInfoCdma) info).getCellIdentity();
                    if(cdma.getSystemId() != 0) cell_id = cdma.getSystemId();
                }
                System.out.println("getBaseStationInfo: info -- " + info);
            }
        }
        return cell_id;
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_LOCATION);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // Update interval in milliseconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (checkLocationPermission()) {
            startLocationUpdates();
        } else {
            requestLocationPermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}