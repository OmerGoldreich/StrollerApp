package com.stroller.stroller;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.stroller.stroller.navigationPackage.DirectionFinder;
import com.stroller.stroller.navigationPackage.DirectionFinderListener;
import com.stroller.stroller.navigationPackage.Route;

import org.json.JSONException;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener {
    private static final PatternItem DOT = new Dot();
    private static final int PATTERN_GAP_LENGTH_PX = 10;
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    // Create a stroke pattern of a gap followed by a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

    private GoogleMap mMap;
    private static PolylineOptions polylineOptions;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private HashMap<LatLng,String> interestingPointsOnTheWay = new HashMap<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    String[] descriptions={"Prepare your duck face! Cathédrale Notre-Dame is on your way","Encounter some delicacies on Rue des Rosiers","Square René Viviani is one of the most beloved spots in town","Shop till you drop at Rue Vieille du Temple"};
    Integer[] imgIds={R.drawable.eyeheart,R.drawable.menucafe,R.drawable.tree2,R.drawable.bagshop};
    public String instruct = "";
    public static String duration = "";
    private static List<LatLng> decodedPolylineMaps;
    public static List<LatLng> route_instruc_strt_pnts;


    public static String instructions="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        fixImagesTexts();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button stroll = findViewById(R.id.button2);
        stroll.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, NavigateActivity.class);
                intent.putExtra("instruct",instruct);
                startActivity(intent);
            }
        });

    }
    private void fixImagesTexts(){
        ImageView[] imageViews={findViewById(R.id.imageView1),findViewById(R.id.imageView2),findViewById(R.id.imageView3),findViewById(R.id.imageView4)};
        TextView[] textViews={findViewById(R.id.textView1),findViewById(R.id.textView2),findViewById(R.id.textView3),findViewById(R.id.textView4)};
        for(int i=0;i<4;i++){
            imageViews[i].setImageResource(imgIds[i]);
            textViews[i].setText(descriptions[i]);
        }
    }

    private void sendRequest() {
        String from_faves_or_search = getIntent().getStringExtra("FAVES_OR_SEARCH");
        String duration_from_faves = FragmentTwo.duration_from_faves;
        String instructions_from_faves = FragmentTwo.instructions_from_faves;

        if(from_faves_or_search.equals("faves")){
            List<LatLng> points=FragmentTwo.faves_polyline;
            decodedPolylineMaps = points;
            route_instruc_strt_pnts = FragmentTwo.faves_instruct_pnts;
            LatLng startLoc = points.get(0);
            LatLng endLoc = points.get(points.size() - 1);
            duration = duration_from_faves;
            instruct = instructions_from_faves;

            if(mMap!=null){

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLoc, 14));

                mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("place1",80,80)))
                        .title("Start")
                        .position(startLoc));

                mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("place2",80,80)))
                        .title("End")
                        .position(endLoc));


                polylineOptions = new PolylineOptions().
                        geodesic(true).
                        color(Color.parseColor("#FF8765")).
                        width(15);
                for(LatLng point:points){
                    polylineOptions.add(point);
                }
                Polyline currentLine = mMap.addPolyline(polylineOptions);
                currentLine.setPattern(PATTERN_POLYLINE_DOTTED);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.setMinZoomPreference(11);
               }
            return;
        }

        String origin = getIntent().getStringExtra("origin");
        String destination = getIntent().getStringExtra("dest");
        try {
            new DirectionFinder(this, origin, destination,from_faves_or_search).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        interestingPointsOnTheWay.put(new LatLng(48.857514, 2.358788),"restaurant");
        interestingPointsOnTheWay.put(new LatLng(48.852329, 2.347787),"park");
        interestingPointsOnTheWay.put(new LatLng(48.853054, 2.349910),"selfie");
        interestingPointsOnTheWay.put(new LatLng(48.859611, 2.359974),"shopping");

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        /*googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_json));*/

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        sendRequest();

        final ImageButton AddtoFavesButton = findViewById(R.id.imageButton);
        AddtoFavesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ViewDialog alert = new ViewDialog(MapsActivity.this, "", 0,decodedPolylineMaps);
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alert.show();
            }
        });
    }

    public Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    @Override
    public void onDirectionFinderStart() {

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            if(route.duration.value > 10800){
                CustomDialog dialog = new CustomDialog(MapsActivity.this, 1);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
                return;
            }

            double upperLat;
            double lowerLat;
            LatLngBounds bounds;
            if(route.startLocation.latitude > route.endLocation.latitude){
                upperLat = route.startLocation.latitude + 0.01;
                lowerLat = route.endLocation.latitude - 0.005;
                Log.d("lower",Double.toString(lowerLat));
                Log.d("upper",Double.toString(upperLat));
                bounds = new LatLngBounds(new LatLng(lowerLat, route.endLocation.longitude),new LatLng(upperLat, route.startLocation.longitude));
            } else {
                upperLat = route.endLocation.latitude + 0.01;
                lowerLat = route.startLocation.latitude - 0.005;
                Log.d("lower",Double.toString(lowerLat));
                Log.d("upper",Double.toString(upperLat));
                bounds = new LatLngBounds(new LatLng(lowerLat, route.startLocation.longitude),new LatLng(upperLat, route.endLocation.longitude));
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("place1",80,80)))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("place2",80,80)))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            for(LatLng location : interestingPointsOnTheWay.keySet()){
                String val = interestingPointsOnTheWay.get(location);
                if(val.equals("restaurant")){
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("menulined",100,100)))
                            .position(location));
                } else if(val.equals("selfie")){
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("eyelined",100,100)))
                            .position(location));

                } else if(val.equals("park")){
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("treelined",100,100)))
                            .position(location));

                } else if(val.equals("shopping")){
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("baglined",100,100)))
                            .position(location));

                }
            }

            polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.parseColor("#FF8765")).
                    width(15);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));
            Polyline currentLine=mMap.addPolyline(polylineOptions);
            currentLine.setPattern(PATTERN_POLYLINE_DOTTED);
            polylinePaths.add(currentLine);

            instruct = route.instructions;
            instructions=instruct;
            duration = route.duration.text;
        }
        this.decodedPolylineMaps = routes.get(0).points;
        route_instruc_strt_pnts = routes.get(0).instructionsPoints;
    }

    public static PolylineOptions getLineOptions(){
        return polylineOptions;
    }
}