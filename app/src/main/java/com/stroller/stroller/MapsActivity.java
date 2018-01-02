package com.stroller.stroller;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
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

import com.google.android.gms.maps.model.MapStyleOptions;

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
    private ProgressDialog progressDialog;
    String[] descriptions={"Prepare your duck face! Cathédrale Notre-Dame is on your way","Encounter some delicacies on Rue des Rosiers","Square René Viviani is one of the most beloved spots in town","Shop till you drop at Rue Vieille du Temple"};
    Integer[] imgIds={R.drawable.attractive,R.drawable.menu,R.drawable.tree,R.drawable.shoppingbag};
    public String instruct = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        fixImagesTexts();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        sendRequest();

        //added by Tala 19.12.17
        final ImageButton AddtoFavesButton = findViewById(R.id.imageButton);
        AddtoFavesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ViewDialog alert = new ViewDialog();
                alert.showDialog((Activity) v.getContext(), "Give this route a name");
            }
        });

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
    }


    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait...",
                "Stroller is working its magic", true);

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
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            if(route.duration.value > 10800){
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setTitle("Hold On")
                        .setMessage("Strolling this route will take more than three hours!")
                        .setNegativeButton("Change Route", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                                Intent intent = new Intent(MapsActivity.this, SearchActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return;
            }
            double newLat=(route.startLocation.latitude+route.endLocation.latitude)/2;
            double newLon=(route.startLocation.longitude+route.endLocation.longitude)/2;
            LatLng originLoc = new LatLng(newLat,newLon);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(originLoc, 14));

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.locc))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.endlocc))
                    .title(route.endAddress)
                    .position(route.endLocation)));


            /*for(LatLng location : interestingPointsOnTheWay.keySet()){
                String val = interestingPointsOnTheWay.get(location);
                if(val.equals("restaurant")){
                    mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin24))
                            .position(location));
                }
                if(val.equals("selfie")){
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin24))
                            .position(location));
                }
                if(val.equals("park")){
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin24))
                            .position(location));
                }
                if(val.equals("shopping")){
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin24))
                            .position(location));
                }
            }*/

            for(LatLng location : interestingPointsOnTheWay.keySet()){
                mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin24))
                .position(location));
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
        }
    }

    public static PolylineOptions getLineOptions(){
        return polylineOptions;
    }
}