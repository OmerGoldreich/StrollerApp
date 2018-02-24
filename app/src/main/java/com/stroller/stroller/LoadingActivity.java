package com.stroller.stroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.stroller.stroller.navigationPackage.DirectionFinder;
import com.stroller.stroller.navigationPackage.DirectionFinderListener;
import com.stroller.stroller.navigationPackage.Highlight;
import com.stroller.stroller.navigationPackage.Route;

import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class LoadingActivity extends Activity implements DirectionFinderListener {

    boolean timeout = false;
    boolean done = false;
    boolean cancel = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        TextView txt = findViewById(R.id.appname);
        txt.setTextSize(25);
        txt.setText("S T R O L L E R\nI S  W O R K I N G  I T S  M A G I C");
        final ImageView[] images = {findViewById(R.id.icon5), findViewById(R.id.icon15), findViewById(R.id.icon11), findViewById(R.id.icon23), findViewById(R.id.icon8), findViewById(R.id.icon17), findViewById(R.id.icon26), findViewById(R.id.icon9), findViewById(R.id.icon3), findViewById(R.id.icon21), findViewById(R.id.icon4), findViewById(R.id.icon14), findViewById(R.id.icon27), findViewById(R.id.icon20), findViewById(R.id.icon7), findViewById(R.id.icon18), findViewById(R.id.icon1), findViewById(R.id.icon22), findViewById(R.id.icon12), findViewById(R.id.icon24), findViewById(R.id.icon25), findViewById(R.id.icon19), findViewById(R.id.icon10), findViewById(R.id.icon2), findViewById(R.id.icon28), findViewById(R.id.icon13), findViewById(R.id.icon16), findViewById(R.id.icon30), findViewById(R.id.icon29), findViewById(R.id.icon6)};
            String origin = getIntent().getStringExtra("origin");
            String destination = getIntent().getStringExtra("dest");
            try {
                new DirectionFinder(this, origin, destination).execute();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        Animation[] animations = new Animation[30];
        for (int i = 0; i < animations.length; i++) {
            animations[i] = new AlphaAnimation(1, 0);
            animations[i].setInterpolator(new AccelerateInterpolator());
            animations[i].setDuration(600);
            animations[i].setStartOffset((i + 1) * 200);
            animations[i].setRepeatMode(Animation.REVERSE);
            animations[i].setRepeatCount(1);
            images[i].startAnimation(animations[i]);
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            @Override
            public void run(){
                Animation[] animations = new Animation[30];
                for (int i = 0; i < animations.length; i++) {
                    animations[i] = new AlphaAnimation(1, 0);
                    animations[i].setInterpolator(new AccelerateInterpolator());
                    animations[i].setDuration(600);
                    animations[i].setStartOffset((i + 1) * 200);
                    animations[i].setRepeatMode(Animation.REVERSE);
                    animations[i].setRepeatCount(1);
                    images[i].startAnimation(animations[i]);
                }
            }
        }, 12500);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!done && !cancel) {
                    timeout = true;
                    Intent intent = new Intent(LoadingActivity.this, SearchActivity.class);
                    intent.putExtra("loading", "timeout");
                    startActivity(intent);
                }
            }
        },25000);
    }

    @Override
    public void onDirectionFinderStart() {

    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes, List<Highlight> highlights) {
        if(!timeout && !cancel){
            done = true;
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

    @Override
    public void onBackPressed() {
        timeout = true;
        cancel = true;
        finish();
        super.onBackPressed();
    }
}
