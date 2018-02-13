package com.stroller.stroller;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;


public class CustomDialog extends Dialog implements
        android.view.View.OnClickListener {

    public Activity activity;
    public Button back;
    private int activity_id;
    private int diff;

    CustomDialog(Activity actv, int actv_id) {
        super(actv);
        this.activity = actv;
        this.activity_id = actv_id;
    }

    CustomDialog(Activity actv, int actv_id, int original_duration, int stroller_duration) {
        super(actv);
        this.activity = actv;
        this.activity_id = actv_id;
        this.diff = stroller_duration - original_duration;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.duration_dialog);
        back = findViewById(R.id.back);
        back.setOnClickListener(this);
        TextView msg = findViewById(R.id.errorMessage);
        if(activity_id == 0){
            msg.setText(R.string.orig_dest);
        } else if (activity_id == 2){
            msg.setText("This route takes " + diff + " additional minutes, but contains many hidden gems!");
            TextView title = findViewById(R.id.hold);
            title.setText("Route Info");
        }
    }

    @Override
    public void onClick(View v) {
        if(activity_id == 0 || activity_id == 2){
            dismiss();
        } else if(activity_id == 1) {
            Intent intent = new Intent(activity, SearchActivity.class);
            activity.startActivity(intent);
            dismiss();
        }
    }
}