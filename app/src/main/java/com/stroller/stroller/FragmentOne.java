package com.stroller.stroller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class FragmentOne extends Fragment {
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE2 = 2;
    int PLACE_PICKER_REQUEST = 1;
    int PLACE_PICKER_REQUEST2 = 2;
    AlertDialog dialog;
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
                try {/*
                    //newly added 28.12
                    AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                           // .setTypeFilter(AutocompleteFilter.)
                            .build();

                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .setFilter(typeFilter)
                                    .build(getActivity());
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);

*/
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
/*
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(getActivity());
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE2);
*/


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
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
                    alertBuilder.setTitle("Hold On")
                            .setMessage("You need to choose an origin and a destination")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    closeDialog();
                            }
                    });
                    dialog = alertBuilder.show();
                    return;
                }
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                intent.putExtra("FAVES_OR_SEARCH","search");
                intent.putExtra("origin",origin);
                intent.putExtra("dest",dest);
                startActivity(intent);
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
                txt.setText(place.getName());
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
                txt2.setText(place.getName());
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

    private void closeDialog(){
        if(dialog != null){
            dialog.dismiss();
        }
    }
}