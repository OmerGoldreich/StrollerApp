package com.stroller.stroller.navigationPackage;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class DirectionFinder {
    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json??optimize=true&mode=walking&";
    private static final String GOOGLE_API_KEY = "AIzaSyDzAnCQaKoBFcu0L7w-OmQCdBWUx51DJKQ"; //this is stroller's
    private DirectionFinderListener listener;
    private String origin;
    private String destination;
    private String whatPageBroughtMeHere;
    public static List<LatLng> decodedPolyline;
    public static List<LatLng> startInstructPoints;

    public DirectionFinder(DirectionFinderListener listener, String origin, String destination,String faves_or_search) {
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
        this.whatPageBroughtMeHere = faves_or_search;
    }
    public void execute() throws UnsupportedEncodingException, JSONException {
        listener.onDirectionFinderStart();
        new DownloadRawData().execute(createUrl());
    }


    private String createUrl() throws UnsupportedEncodingException {
        String urlOrigin = URLEncoder.encode(origin, "utf-8");
        String urlDestination = URLEncoder.encode(destination, "utf-8");
        //String waypoints = "&waypoints=Rue+Debelleyme,+75003+Paris,+France|Rue+des+Ecouffes,+75004+Paris,+France|Square+Ren%C3%A9+Viviani,+Quai+de+Montebello,+Paris,+France";
        String waypoints = "";
        //waypoints should be extracted from the URL Omer will provide
        return DIRECTION_URL_API + "origin=" + urlOrigin + "&destination=" + urlDestination + waypoints + "&key=" + GOOGLE_API_KEY;
    }

    private class DownloadRawData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String link = params[0];
            try {
                URL url = new URL(link);
                InputStream is = url.openConnection().getInputStream();
                StringBuffer buffer = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                parseJSon(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private void parseJSon(String data) throws JSONException {
        if(data==null)
            return;
        startInstructPoints = new ArrayList<>();
        List<Route> routes = new ArrayList<Route>();
        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");
        for (int i = 0; i < jsonRoutes.length(); i++) {
            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
            Route route = new Route();
            String instruct = "";

            JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
            JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
            JSONObject jsonStartingLeg = jsonLegs.getJSONObject(0);
            JSONObject jsonFinalLeg = jsonLegs.getJSONObject(jsonLegs.length()-1);
            JSONObject jsonDistance = jsonStartingLeg.getJSONObject("distance");
            JSONObject jsonEndLocation = jsonFinalLeg.getJSONObject("end_location");
            JSONObject jsonStartLocation = jsonStartingLeg.getJSONObject("start_location");

            route.distance = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
            route.endAddress = jsonFinalLeg.getString("end_address");
            route.startAddress = jsonStartingLeg.getString("start_address");
            route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
            route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
            route.points = decodePolyLine(overview_polylineJson.getString("points"));
            this.decodedPolyline = route.points;

            JSONObject jsonEndInstruct;
            LatLng endPoint;
            int minutes = 0;
            int hours = 0;
            //get navigation instructions
            for(int j = 0; j < jsonLegs.length(); j++){
                JSONObject jsonLeg = jsonLegs.getJSONObject(j);
                JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
                String durationString = jsonDuration.getString("text");
                Log.d("duration",durationString);
                String[] parseDuration = durationString.split(" ");
                if(parseDuration[1].contains("min")){
                    minutes += Integer.parseInt(parseDuration[0]);
                } else {
                    hours += Integer.parseInt(parseDuration[0]);
                    minutes += Integer.parseInt(parseDuration[2]);
                }
                JSONArray jsonSteps = jsonLeg.getJSONArray("steps");
                for(int k = 0; k < jsonSteps.length(); k++){
                    JSONObject step = jsonSteps.getJSONObject(k);
                    jsonEndInstruct = step.getJSONObject("start_location");
                    double lat = jsonEndInstruct.getDouble("lat");
                    double lng = jsonEndInstruct.getDouble("lng");
                    endPoint = new LatLng(lat,lng);
                    startInstructPoints.add(endPoint);
                    instruct = instruct.concat("- ");
                    instruct = instruct.concat(step.getString("html_instructions"));
                    instruct = instruct.concat("\n\n");
                }
            }
            if(minutes/60 > 0){
                hours += minutes / 60;
                minutes = minutes % 60;
            }
            String duration = Integer.toString(hours).concat("h\n").concat(Integer.toString(minutes)).concat("m");
            route.instructions = instruct;
            route.duration = new Duration(duration,1);
            route.instructionsPoints = startInstructPoints;
            routes.add(route);
        }

        listener.onDirectionFinderSuccess(routes);
    }

    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }


}