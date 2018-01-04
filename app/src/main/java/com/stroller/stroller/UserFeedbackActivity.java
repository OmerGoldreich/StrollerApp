package com.stroller.stroller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class UserFeedbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_feedback);
        Button like = findViewById(R.id.likeButton);
        like.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(UserFeedbackActivity.this,SearchActivity.class);
                startActivity(intent);
            }
        });
        Button dislike = findViewById(R.id.dislikeButton);
        dislike.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(UserFeedbackActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
    }
}
