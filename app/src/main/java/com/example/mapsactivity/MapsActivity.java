package com.example.mapsactivity;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.mapsactivity.R.id;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import kotlin.jvm.internal.Intrinsics;
import kotlin.TypeCastException;

public final class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, OnMarkerClickListener {

    private static GoogleMap map;
    private static NetworkController networkController;
    private static FusedLocationProviderClient fusedLocationClient;
    private static String inputtext = null;
    private static Location lastLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_maps);
        networkController = new NetworkController();
        Fragment fragment = this.getSupportFragmentManager().findFragmentById(R.id.map);
        if (fragment == null) {
            throw new TypeCastException("null cannot be cast to non-null type com.google.android.gms.maps.SupportMapFragment");
        } else {
            SupportMapFragment mapFragment = (SupportMapFragment)fragment;
            mapFragment.getMapAsync((OnMapReadyCallback)this);
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            Intrinsics.checkExpressionValueIsNotNull(fusedLocationClient, "LocationServices.getFuse…ationProviderClient(this)");
            Button searchbtn = (Button)this.findViewById(R.id.btn_search);
            searchbtn.setOnClickListener((OnClickListener)(new OnClickListener() {
                public final void onClick(View view) {
                    System.out.println("************************************");
                    EditText entertext = (EditText)view.findViewById(id.entertext);
                    inputtext = entertext.getText().toString();
                    System.out.println("************************************" + inputtext);

                    Location searchedLocation = null;
                    try {
                        searchedLocation = networkController.fetchGeocoding(inputtext);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    System.out.println("fetchGeocoding 성...공?"+ searchedLocation.getLatitude() +" "+ searchedLocation.getLongitude());
                }
            }));
        }
    }

    private void setUpMap() {
        if (ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 1);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setOnMarkerClickListener(this);
        setUpMap();
        map.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location lastLocation) {
                if(lastLocation != null){
                    LatLng currentLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f));
                }
                if(lastLocation == null) {
                    System.out.println("lastLocation is null");
                } else {
                    System.out.println("lastLocation is what");
                    placeMarkerOnMap(networkController.fetchStore(lastLocation));
                }
            }
        });
    }

    private final BitmapDescriptor bitmapDescriptorFromVector(Runnable context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable((Context) context, vectorResId);
        if (vectorDrawable == null) {
            Intrinsics.throwNpe();
        }
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        BitmapDescriptor var10000 = BitmapDescriptorFactory.fromBitmap(bitmap);
        Intrinsics.checkExpressionValueIsNotNull(var10000, "BitmapDescriptorFactory.fromBitmap(bitmap)");
        return var10000;
    }

    private void placeMarkerOnMap(List<Store> storesByGeo) {
        if (storesByGeo != null) {
            System.out.println("-------------------");
            System.out.println("placeMarkerOnMap");
            System.out.println("-------------------");
            for (final Store store : storesByGeo) {
                final LatLng pinLocation = new LatLng(store.getLat(), store.getLng());
                final String remain = store.getRemain_stat();
                this.runOnUiThread((Runnable) (new Runnable() {
                    public final void run() {
                        switch (remain) {
                            case "plenty":
                                map.addMarker(new MarkerOptions()   //MarkerOptions의 매개변수에 color를 넣어야함
                                        .position(pinLocation)
                                        .title(store.getName())
                                        .icon(bitmapDescriptorFromVector(this, R.drawable.ic_green)));
                                break;
                            case "some":
                                map.addMarker(new MarkerOptions()   //MarkerOptions의 매개변수에 color를 넣어야함
                                        .position(pinLocation)
                                        .title(store.getName())
                                        .icon(bitmapDescriptorFromVector(this, R.drawable.ic_yellow)));
                                break;
                            case "few":
                                map.addMarker(new MarkerOptions()   //MarkerOptions의 매개변수에 color를 넣어야함
                                        .position(pinLocation)
                                        .title(store.getName())
                                        .icon(bitmapDescriptorFromVector(this, R.drawable.ic_red)));
                                break;
                            default:
                                map.addMarker(new MarkerOptions()   //MarkerOptions의 매개변수에 color를 넣어야함
                                        .position(pinLocation)
                                        .title(store.getName())
                                        .icon(bitmapDescriptorFromVector(this, R.drawable.ic_gray)));
                                break;
                        }
                    }
                }));
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}
