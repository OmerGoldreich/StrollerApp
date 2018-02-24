package com.stroller.stroller;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class CustomDialog extends Dialog implements
        android.view.View.OnClickListener {

    public Activity activity;
    public Button back;
    private int activity_id;
    private int diff = 0;
    private String instructions = "";

    public CustomDialog(Activity actv, int actv_id) {
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

    CustomDialog(Activity actv, int actv_id, String instructions){
        super(actv);
        this.activity = actv;
        this.activity_id = actv_id;
        this.instructions = instructions;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if(activity_id == 3){
            setContentView(R.layout.instruction_dialog);
        } else if (activity_id == 5) {
            setContentView(R.layout.enable_location_dialog);
        } else {
            setContentView(R.layout.duration_dialog);
        }
        back = findViewById(R.id.back);
        back.setOnClickListener(this);
        TextView msg = findViewById(R.id.errorMessage);
        TextView title = findViewById(R.id.hold);
        if(activity_id == 0){
            msg.setText(R.string.orig_dest);
        } else if (activity_id == 2){
            title.setText(R.string.info);
            msg.setText(String.format("This route takes %d additional minutes, but contains many hidden gems!", diff));
        } else if (activity_id == 3){
            title.setText(R.string.full_instruct);
            msg.setText(instructions);
            msg.setMovementMethod(new ScrollingMovementMethod());
        } else if (activity_id == 4){
            ImageView img = findViewById(R.id.missing);
            img.setImageResource(R.drawable.broken);
            title.setText(R.string.sorry);
            msg.setText(R.string.sorry_body);
        }
    }

    @Override
    public void onClick(View v) {
        if(activity_id == 5){
            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            activity.startActivity(myIntent);
            dismiss();
        } else {
            dismiss();
        }
    }
}