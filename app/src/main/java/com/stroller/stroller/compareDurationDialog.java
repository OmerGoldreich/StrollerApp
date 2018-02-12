package com.stroller.stroller;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by tala on 09-Feb-18.
 */

public class compareDurationDialog extends Dialog implements
        android.view.View.OnClickListener{

    private int strollerDuration;
    private int originalDurtion;
    public Activity activity;
    public Button cool;
    private int activity_id;
    private TextView errorMessage;

    compareDurationDialog(Activity actv, int originalDuration,int strollerDuration) {
        super(actv);
        this.activity = actv;
        this.originalDurtion=originalDuration;
        this.strollerDuration=strollerDuration;
     }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.compare_duration_dialog);
        TextView msg = findViewById(R.id.infomsg);
        int diff = this.strollerDuration - this.originalDurtion;
        msg.setText("Strolling this route will take "+ diff +" minutes more than the shortest one, but hey! you're gonna have way more fun!");
        cool = findViewById(R.id.cool);
        cool.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
      dismiss();
    }
}