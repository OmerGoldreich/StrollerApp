package com.stroller.stroller;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.stroller.stroller.navigationPackage.DirectionFinder;

import java.util.Arrays;
import java.util.List;

public class NavigateActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final PatternItem DOT = new Dot();
    private static final int PATTERN_GAP_LENGTH_PX = 10;
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    // Create a stroke pattern of a gap followed by a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);
    private static double currLocationLAT;
    private static double currLocationLNG;
    private GoogleMap mMap;
    public String[] instructions;
    public List<LatLng> endPoints;
    public TextView box;
    public int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        String htmlInstruct = getIntent().getStringExtra("instruct");
        htmlInstruct = htmlInstruct.replaceAll("&nbsp;", "");
        htmlInstruct = htmlInstruct.replaceAll("<div.*?>", "\n\n- ");
        String instruct = htmlInstruct.replaceAll("<.*?>", "");

        instructions = instruct.split("\n\n");

        endPoints = DirectionFinder.startInstructPoints;

        box = findViewById(R.id.instructions);
        box.setText(instructions[0]);

        ImageButton stop = findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(NavigateActivity.this, UserFeedbackActivity.class);
                startActivity(intent);
                finish();
            }
        });

        String duration = MapsActivity.duration;
        duration = duration.replaceAll(" ", "");
        duration = duration.replaceAll("mins", "m");
        duration = duration.replaceAll("hours", "h\n");
        duration = duration.replaceAll("hour", "h\n");
        if (duration.indexOf("h") == -1) {
            duration = "0h\n".concat(duration);
        }

        TextView display = findViewById(R.id.time);
        display.setText(duration);
    }

    public Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        PolylineOptions options = MapsActivity.getLineOptions();
        List<LatLng> points = options.getPoints();
        LatLng startLoc = points.get(0);
        LatLng endLoc = points.get(points.size() - 1);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLoc, 20));

        mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("place1",80,80)))
                .title("Start")
                .position(startLoc));

        mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("place2",80,80)))
                .title("End")
                .position(endLoc));


        Polyline currentLine = mMap.addPolyline(options);
        currentLine.setPattern(PATTERN_POLYLINE_DOTTED);

        mMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
        mMap.setOnMyLocationClickListener(onMyLocationClickListener);
        enableMyLocationIfPermitted();

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMinZoomPreference(11);
        LocationManager myLocManager;
        myLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        myLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double currLat = location.getLatitude();
                double currLng = location.getLongitude();
                currLocationLAT = currLat;
                currLocationLNG = currLng;
                Log.d("curr location ",currLocationLAT+" "+currLocationLNG);
                if(index < endPoints.size()) {
                    if (Math.pow((currLat - endPoints.get(index).latitude), 2)
                            + Math.pow((currLng - endPoints.get(index).longitude), 2) <= Math.pow(0.0007, 2)) {
                        index++;
                        box.setText(instructions[index]);
                        box.setMovementMethod(new ScrollingMovementMethod());
                    }
                }
            }

            @Override
            public void onProviderDisabled(String provider) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProviderEnabled(String provider) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
                // TODO Auto-generated method stub
            }
        });

    }

    private void enableMyLocationIfPermitted() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocationIfPermitted();
                } else {
                }
                return;
            }

        }
    }
    private GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener =
            new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    mMap.setMinZoomPreference(15);
                    return false;
                }
            };
    private GoogleMap.OnMyLocationClickListener onMyLocationClickListener =
            new GoogleMap.OnMyLocationClickListener() {
                @Override
                public void onMyLocationClick(@NonNull Location location) {

                    mMap.setMinZoomPreference(12);

                    CircleOptions circleOptions = new CircleOptions();
                    circleOptions.center(new LatLng(location.getLatitude(),
                            location.getLongitude()));

                    circleOptions.radius(200);
                    circleOptions.fillColor(Color.RED);
                    circleOptions.strokeWidth(6);

                    mMap.addCircle(circleOptions);
                }
            };

}