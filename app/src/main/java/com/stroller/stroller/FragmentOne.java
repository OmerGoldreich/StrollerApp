package com.stroller.stroller;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.stroller.stroller.navigationPackage.DirectionFinderGoogleMap;
import com.stroller.stroller.navigationPackage.DirectionFinderListener;
import com.stroller.stroller.navigationPackage.Highlight;
import com.stroller.stroller.navigationPackage.Route;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class FragmentOne extends Fragment implements DirectionFinderListener {
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE2 = 2;
    int PLACE_PICKER_REQUEST = 1;
    int PLACE_PICKER_REQUEST2 = 2;
    String origin = "";
    String dest = "";
    Double lat;
    Double lng;



    public FragmentOne() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_one, container, false);
        EditText originEditText = v.findViewById(R.id.editText4);
        originEditText.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);

                } catch (GooglePlayServicesRepairableException e) {

                } catch (GooglePlayServicesNotAvailableException e) {

                }
            }
        });
        EditText destEditText = v.findViewById(R.id.editText3);
        destEditText.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST2);

                } catch (GooglePlayServicesRepairableException e) {

                } catch (GooglePlayServicesNotAvailableException e) {

                }
            }
        });

        Button stroll = v.findViewById(R.id.btn2);
        stroll.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(origin.equals("") || dest.equals("")){
                    CustomDialog dialog = new CustomDialog(getActivity(), 0);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();
                    return;
                }
                try {
                    new DirectionFinderGoogleMap(FragmentOne.this, origin, dest,getActivity(),true).execute();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                EditText txt = getView().findViewById(R.id.editText4);
                CharSequence primaryAddress = place.getName();
                if(isdigit(primaryAddress.charAt(0)))txt.setText(place.getAddress());
                else{txt.setText(String.format("%s, %s", primaryAddress, place.getAddress()));}
                LatLng originll = place.getLatLng();
                lat = originll.latitude;
                lng = originll.longitude;
                origin = lat.toString() + "," + lng.toString();
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                Log.d("error", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        } else if(requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE2) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                EditText txt2 = getView().findViewById(R.id.editText3);
                CharSequence primaryAddress = place.getName();
                if(isdigit(primaryAddress.charAt(0)))txt2.setText(place.getAddress());
                else{txt2.setText(String.format("%s, %s", primaryAddress, place.getAddress()));}
                LatLng destll = place.getLatLng();
                lat = destll.latitude;
                lng = destll.longitude;
                dest = lat.toString() + "," + lng.toString();
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                Log.d("error", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private boolean isdigit(char c) {
        return c >= 48 && c <= 57;
    }

    @Override
    public void onDirectionFinderStart() {

    }

    @Override
    public void onDirectionFinderSuccess(List<Route> route, List<Highlight> highlights) {
    }
}