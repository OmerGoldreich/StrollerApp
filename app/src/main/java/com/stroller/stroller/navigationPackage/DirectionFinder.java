package com.stroller.stroller.navigationPackage;

import android.os.AsyncTask;

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
import java.util.ArrayList;
import java.util.List;


public class DirectionFinder {
    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyDzAnCQaKoBFcu0L7w-OmQCdBWUx51DJKQ"; //this is stroller's
    private DirectionFinderListener listener;
    private String origin;
    private String destination;
    private String whatPageBroughtMeHere;

    public DirectionFinder(DirectionFinderListener listener, String origin, String destination,String faves_or_search) {
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
        this.whatPageBroughtMeHere = faves_or_search;
    }
    public void execute() throws UnsupportedEncodingException, JSONException {
        listener.onDirectionFinderStart();
        new DownloadRawData().execute(createUrl());
        //List<Route> parsedRoute = parseJSon(createUrl()); //new line 23.12
        //return parsedRoute;//new line 23.12
    }

    private String createUrl() throws UnsupportedEncodingException {
        //String urlOrigin = URLEncoder.encode(origin, "utf-8");
        //String urlDestination = URLEncoder.encode(destination, "utf-8");
        String urlSearch = "https://maps.googleapis.com/maps/api/directions/json?optimize=true&mode=walking&origin=Ob-La-Di,+54+Rue+de+Saintonge,+75003+Paris,+France&destination=Shakespeare+%26+Company,+Rue+de+la+B%C3%BBcherie,+Paris,+France&waypoints=Rue+Debelleyme,+75003+Paris,+France|Rue+des+Ecouffes,+75004+Paris,+France|Square+Ren%C3%A9+Viviani,+Quai+de+Montebello,+Paris,+France&key=AIzaSyAIEtqhx3HfArhy2nAlaY0x-HkXZz8v6Qw";
        String urlFaves = "https://maps.googleapis.com/maps/api/directions/json?origin=Arc+de+Triomphe,+Place+Charles+de+Gaulle,+75008+Paris,+France&destination=Eiffel+Tower,+Champ+de+Mars,+5+Avenue+Anatole+France,+75007+Paris,+France&waypoints=22+Av.+des+Champs-%C3%89lys%C3%A9es,+75008+Paris,+France&key=AIzaSyAIEtqhx3HfArhy2nAlaY0x-HkXZz8v6Qw";
        if(whatPageBroughtMeHere.equals("search")){
            return urlSearch;
        }
        if(whatPageBroughtMeHere.equals("faves")){
            return urlFaves;
        }
        return urlFaves;
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
        if(data==null)return;

        List<Route> routes = new ArrayList<Route>();
        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");
        for (int i = 0; i < jsonRoutes.length(); i++) {
            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
            Route route = new Route();

            JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
            JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
            JSONObject jsonStartingLeg = jsonLegs.getJSONObject(0);
            JSONObject jsonFinalLeg = jsonLegs.getJSONObject(jsonLegs.length()-1);
            JSONObject jsonDistance = jsonStartingLeg.getJSONObject("distance");
            JSONObject jsonDuration = jsonStartingLeg.getJSONObject("duration");
            JSONObject jsonEndLocation = jsonFinalLeg.getJSONObject("end_location");
            JSONObject jsonStartLocation = jsonStartingLeg.getJSONObject("start_location");

            route.distance = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
            route.duration = new Duration(jsonDuration.getString("text"), jsonDuration.getInt("value"));
            route.endAddress = jsonFinalLeg.getString("end_address");
            route.startAddress = jsonStartingLeg.getString("start_address");
            route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
            route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
            route.points = decodePolyLine(overview_polylineJson.getString("points"));

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