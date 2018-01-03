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


public class CustomDialog extends Dialog implements
        android.view.View.OnClickListener {

    public Activity activity;
    public Button back;
    private int activity_id;
    private TextView errorMessage;

    public CustomDialog(Activity actv, int actv_id) {
        super(actv);
        // TODO Auto-generated constructor stub
        this.activity = actv;
        this.activity_id = actv_id;
        this.errorMessage = activity.findViewById(R.id.errorMessage);
        Log.d("isNull", errorMessage == null ? "yes" : "no");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if(activity_id == 0){
            setContentView(R.layout.custom_dialog);
        } else {
            setContentView(R.layout.duration_dialog);
        }
        back = findViewById(R.id.back);
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(activity_id == 0){
            dismiss();
        } else {
            Intent intent = new Intent(activity, SearchActivity.class);
            activity.startActivity(intent);
            dismiss();
        }
    }
}