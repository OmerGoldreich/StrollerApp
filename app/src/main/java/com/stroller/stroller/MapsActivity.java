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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.stroller.stroller.navigationPackage.DirectionFinder;
import com.stroller.stroller.navigationPackage.DirectionFinderListener;
import com.stroller.stroller.navigationPackage.Highlight;
import com.stroller.stroller.navigationPackage.LatLon;
import com.stroller.stroller.navigationPackage.Route;

import com.google.android.gms.maps.model.MapStyleOptions;

import org.json.JSONException;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,ViewTreeObserver.OnGlobalLayoutListener {
    private static final PatternItem DOT = new Dot();
    private static final int PATTERN_GAP_LENGTH_PX = 10;
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    // Create a stroke pattern of a gap followed by a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

    private GoogleMap mMap;
    private static PolylineOptions polylineOptions;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Highlight> highlights=new ArrayList<>();
    private HashMap<LatLng,String> interestingPointsOnTheWay = new HashMap<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    public Map<String,String> descDict = new HashMap<String, String>();
    public Map<String,Integer> imgDict = new HashMap<String,Integer>();
    String[] descriptions={"Prepare your duck face! Cathédrale Notre-Dame is on your way","Encounter some delicacies on Rue des Rosiers","Square René Viviani is one of the most beloved spots in town","Shop till you drop at Rue Vieille du Temple"};
    Integer[] imgIds={R.drawable.eyeheart,R.drawable.menucafe,R.drawable.tree2,R.drawable.bagshop};
    public String instruct = "";
    private View mapView;
    private boolean isMapReady=false;
    private boolean isViewReady=false;
    public static String duration = "";
    private static List<LatLng> decodedPolylineMaps;
    public static List<LatLng> route_instruc_strt_pnts;
    public static String instructions="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Intent intent = this.getIntent();
        for (int i=0;i<intent.getIntExtra("highlightsSize",1);i++){
            highlights.add((Highlight) intent.getSerializableExtra("highlights"+i));
        }
        updateDictionaries();
        fixImagesTexts();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();
        mapView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        Button stroll = findViewById(R.id.button2);
        stroll.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, NavigateActivity.class);
                intent.putExtra("instruct",instruct);
                startActivity(intent);
            }
        });

    }
    private void updateDictionaries(){
        descDict.put("sightseeing","Prepare your duck face! {name} is on your way");
        descDict.put("food","Encounter some delicacies on {name}");
        descDict.put("parks","{name} is one of the most beloved spots in town");
        descDict.put("shops","Shop till you drop at {name}");
        descDict.put("attractions","Prepare your duck face! {name} is on your way");
        descDict.put("cafes","Prepare your duck face! {name} is on your way");
        descDict.put("culturalactivities","Prepare your duck face! {name} is on your way");
        descDict.put("foodshops","Prepare your duck face! {name} is on your way");
        descDict.put("nightlife","Prepare your duck face! {name} is on your way");
        imgDict.put("sightseeing",R.drawable.eyeheart);
        imgDict.put("food",R.drawable.menucafe);
        imgDict.put("parks",R.drawable.tree2);
        imgDict.put("shops",R.drawable.bagshop);
        imgDict.put("attractions",R.drawable.bagshop);
        imgDict.put("cafes",R.drawable.bagshop);
        imgDict.put("culturalactivities",R.drawable.bagshop);
        imgDict.put("foodshops",R.drawable.bagshop);
        imgDict.put("nightlife",R.drawable.bagshop);
    }
    private void fixImagesTexts(){
        ImageView[] imageViews={findViewById(R.id.imageView1),findViewById(R.id.imageView2),findViewById(R.id.imageView3),findViewById(R.id.imageView4)};
        TextView[] textViews={findViewById(R.id.textView1),findViewById(R.id.textView2),findViewById(R.id.textView3),findViewById(R.id.textView4)};
        for(int i=0;i<highlights.size();i++){
            imageViews[i].setImageResource(imgDict.get(highlights.get(i).category));
            String currentText=descDict.get(highlights.get(i).category);
            currentText=currentText.replace("{name}",highlights.get(i).name);
            textViews[i].setText(currentText);
        }
    }
    public void drawRouteOnMap(List<Route> routes) {
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
                bounds = new LatLngBounds(new LatLng(lowerLat, route.endLocation.longitude),new LatLng(upperLat, route.startLocation.longitude));
            } else {
                upperLat = route.endLocation.latitude + 0.01;
                lowerLat = route.startLocation.latitude - 0.005;
                bounds = new LatLngBounds(new LatLng(lowerLat, route.startLocation.longitude),new LatLng(upperLat, route.endLocation.longitude));
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("place1",80,80)))
                    .title(route.startAddress)
                    .position(new LatLng(route.startLocation.latitude,route.startLocation.longitude))));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("place2",80,80)))
                    .title(route.endAddress)
                    .position(new LatLng(route.endLocation.latitude,route.endLocation.longitude))));

            for(LatLng location : interestingPointsOnTheWay.keySet()){
                String val = interestingPointsOnTheWay.get(location);
                if(val.equals("food")){
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("menulined",100,100)))
                            .position(location));
                } else if(val.equals("sightseeing")){
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("eyelined",100,100)))
                            .position(location));

                } else if(val.equals("parks")){
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("treelined",100,100)))
                            .position(location));

                } else if(val.equals("shops")){
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("baglined",100,100)))
                            .position(location));

                } else if(val.equals("attractions")){
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("baglined",100,100)))
                            .position(location));

                }  else if(val.equals("cafes")){
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("baglined",100,100)))
                            .position(location));

                }  else if(val.equals("culturalactivities")){
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("baglined",100,100)))
                            .position(location));

                }  else if(val.equals("foodshops")){
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("baglined",100,100)))
                            .position(location));

                }  else if(val.equals("nightlife")){
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
                polylineOptions.add(new LatLng(route.points.get(i).latitude,route.points.get(i).longitude));
            Polyline currentLine=mMap.addPolyline(polylineOptions);
            currentLine.setPattern(PATTERN_POLYLINE_DOTTED);
            polylinePaths.add(currentLine);

            instruct = route.instructions;
            instructions=instruct;
            duration = route.duration.text;
        }
        this.decodedPolylineMaps=new ArrayList<>();
        for (LatLon latLon : routes.get(0).points) {
            this.decodedPolylineMaps.add(new LatLng(latLon.latitude,latLon.longitude));
        }
        this.route_instruc_strt_pnts=new ArrayList<>();
        for (LatLon latLon : routes.get(0).instructionsPoints) {
            this.route_instruc_strt_pnts.add(new LatLng(latLon.latitude,latLon.longitude));
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
        Intent intent = this.getIntent();
        ArrayList<Route> routeList=new ArrayList<>();
        for (int i=0;i<intent.getIntExtra("routesListSize",1);i++){
            routeList.add((Route)intent.getSerializableExtra("routesList"+i));
        }
        for (Highlight highlight : highlights){
            interestingPointsOnTheWay.put(new LatLng(highlight.latitude, highlight.longitude),highlight.category);
        }
        drawRouteOnMap(routeList);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        /*googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_json));*/
        Log.i("MapsActivity","before permission check");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return;
        }
        Log.i("MapsActivity","after permission check");
        //mMap.setMyLocationEnabled(true);
        isMapReady=true;
        if(isMapReady&&isViewReady) {
            sendRequest();
        }

        final ImageButton AddtoFavesButton = findViewById(R.id.imageButton);
        AddtoFavesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ViewDialog alert = new ViewDialog(MapsActivity.this, "", 0,decodedPolylineMaps);
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alert.show();
            }
        });
    }
    @Override
    public void onGlobalLayout() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        } else {
            mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
        isViewReady=true;
        if(isMapReady&&isViewReady) {
            sendRequest();
        }

    }
    public Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }
/*
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
    }*/

    public static PolylineOptions getLineOptions(){
        return polylineOptions;
    }
}