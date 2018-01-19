package com.stroller.stroller;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.stroller.stroller.navigationPackage.DirectionFinder;
import com.stroller.stroller.navigationPackage.DirectionFinderListener;
import com.stroller.stroller.navigationPackage.Highlight;
import com.stroller.stroller.navigationPackage.Route;

import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class LoadingActivity extends AppCompatActivity implements DirectionFinderListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        TextView txt = findViewById(R.id.appname);
        txt.setTextSize(25);
        txt.setText("S T R O L L E R\nI S  W O R K I N G  I T S  M A G I C");
        ImageView[] images = {findViewById(R.id.icon5), findViewById(R.id.icon15), findViewById(R.id.icon11), findViewById(R.id.icon23), findViewById(R.id.icon8), findViewById(R.id.icon17), findViewById(R.id.icon26), findViewById(R.id.icon9), findViewById(R.id.icon3), findViewById(R.id.icon21), findViewById(R.id.icon4), findViewById(R.id.icon14), findViewById(R.id.icon27), findViewById(R.id.icon20), findViewById(R.id.icon7), findViewById(R.id.icon18), findViewById(R.id.icon1), findViewById(R.id.icon22), findViewById(R.id.icon12), findViewById(R.id.icon24), findViewById(R.id.icon25), findViewById(R.id.icon19), findViewById(R.id.icon10), findViewById(R.id.icon2), findViewById(R.id.icon28), findViewById(R.id.icon13), findViewById(R.id.icon16), findViewById(R.id.icon30), findViewById(R.id.icon29), findViewById(R.id.icon6)};
        final String favesOrSearch=getIntent().getStringExtra("FAVES_OR_SEARCH");
        if (favesOrSearch.equals("faves")){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(LoadingActivity.this, MapsActivity.class);
                    intent.putExtra("FAVES_OR_SEARCH", favesOrSearch);
                    intent.putExtra("origin", getIntent().getStringExtra("origin"));
                    intent.putExtra("dest", getIntent().getStringExtra("dest"));
                    startActivity(intent);
                    finish();
                }
            }, 5000);
        }
        else{
            String origin = getIntent().getStringExtra("origin");
            String destination = getIntent().getStringExtra("dest");
            try {
                new DirectionFinder(this, origin, destination,favesOrSearch).execute();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        Animation[] animations = new Animation[30];
        for (int i = 0; i < animations.length; i++) {
            animations[i] = new AlphaAnimation(1, 0);
            animations[i].setInterpolator(new AccelerateInterpolator());
            animations[i].setDuration(500);
            animations[i].setStartOffset((i + 1) * 200);
            animations[i].setRepeatMode(Animation.REVERSE);
            animations[i].setRepeatCount(1);
            images[i].startAnimation(animations[i]);
        }
    }

    @Override
    public void onDirectionFinderStart() {

    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes, List<Highlight> highlights) {
        Intent intent = new Intent(LoadingActivity.this, MapsActivity.class);
        intent.putExtra("FAVES_OR_SEARCH", getIntent().getStringExtra("FAVES_OR_SEARCH"));
        intent.putExtra("origin", getIntent().getStringExtra("origin"));
        intent.putExtra("dest", getIntent().getStringExtra("dest"));
        for (int i=0;i<routes.size();i++) {
            intent.putExtra("routesList"+i,routes.get(i));
        }
        intent.putExtra("routesListSize", routes.size());


        for (int i=0;i<highlights.size();i++) {
            intent.putExtra("highlights"+i,highlights.get(i));
        }
        intent.putExtra("highlightsSize", highlights.size());
        startActivity(intent);
        finish();
    }
}
