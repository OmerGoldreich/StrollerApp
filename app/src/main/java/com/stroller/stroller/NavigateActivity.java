package com.stroller.stroller;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Arrays;
import java.util.List;

public class NavigateActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final PatternItem DOT = new Dot();
    private static final int PATTERN_GAP_LENGTH_PX = 10;
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    // Create a stroke pattern of a gap followed by a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        String htmlInstruct = getIntent().getStringExtra("instruct");
        String destInstruct = htmlInstruct.replaceAll("<div.*?>","\n\n- ");
        String instruct = destInstruct.replaceAll("<.*?>", "");
        TextView box = findViewById(R.id.instructions);
        box.setText(instruct);
        box.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        PolylineOptions options = MapsActivity.getLineOptions();
        List<LatLng> points = options.getPoints();
        LatLng startLoc = points.get(0);
        LatLng endLoc = points.get(points.size()-1);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLoc, 20));

        mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.locc))
                .title("Start")
                .position(startLoc));

        mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.endlocc))
                .title("End")
                .position(endLoc));


        Polyline currentLine = mMap.addPolyline(options);
        currentLine.setPattern(PATTERN_POLYLINE_DOTTED);
    }
}