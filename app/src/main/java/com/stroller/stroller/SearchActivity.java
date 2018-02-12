package com.stroller.stroller;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.TabLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.firebase.auth.FirebaseAuth;


public class SearchActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ViewPager viewPager = findViewById(R.id.pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Add Fragments to adapter one by one
        adapter.addFragment(new FragmentOne(), "Search");
        adapter.addFragment(new FragmentTwo(), "Favorites");
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            String firstName= extras.getString("firstName");
            if(firstName==null){
                firstName=FirebaseAuth.getInstance().getCurrentUser().getDisplayName().split(" ")[0];
            }
            TextView username = findViewById(R.id.txt);
            username.setText(String.format("Hello, %s", firstName));
            ImageView imgProfilePic = (ImageView)findViewById(R.id.user);
            String ProfilePicURL = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();
       //     String ProfilePicURL="https://media.licdn.com/mpr/mpr/shrinknp_200_200/AAEAAQAAAAAAAAyqAAAAJGFlNWJiODA3LWZkNzctNGNhOC1iYTZkLTc5NzFlNjFmMmU0Ng.jpg";
            // show The Image
            new DownloadImageTask(imgProfilePic)
                    .execute(ProfilePicURL);
        }
    }
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
    }

    //moved to FragmentOne
    /*public void strollButtonAction(View v) {
        Intent intent = new Intent(SearchActivity.this, MapsActivity.class);
        intent.putExtra("FAVES_OR_SEARCH","search");
        startActivity(intent);
    }*/

    public void logOutButtonAction(View v) {
        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
        intent.putExtra("disconnect",1);
        startActivity(intent);
        finish();
    }



    // Adapter for the viewpager using FragmentPagerAdapter
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }
}
