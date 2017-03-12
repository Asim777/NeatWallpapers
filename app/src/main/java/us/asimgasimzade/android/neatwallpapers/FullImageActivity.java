package us.asimgasimzade.android.neatwallpapers;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * This activity opens when user clicks on image from SingleImageActivity to see the full
 * scrollable version of image
 */

public class FullImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        Toolbar toolbar = (Toolbar) findViewById(R.id.full_image_toolbar);
        setSupportActionBar(toolbar);

        //Enable up button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (toolbar != null) {
            //Get status bar height
            int statusBarHeight = 0;
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            }

            //Apply top padding to a toolbar to fix content going under transparent status bar issue
            AppBarLayout.LayoutParams toolbarParams = new AppBarLayout.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            toolbarParams.setMargins(0, statusBarHeight, 0, 0);
            toolbar.setLayoutParams(toolbarParams);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        //Removing title from action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        ImageView imageView = (ImageView) findViewById(R.id.fullImageView);
        PhotoViewAttacher photoViewAttacher;
        photoViewAttacher = new PhotoViewAttacher(imageView);
        photoViewAttacher.update();

        //Get image URL from the intent
        String imageUrl = getIntent().getStringExtra("url");
        Glide.with(getApplicationContext()).load(imageUrl).into(imageView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.full_image_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.favorite_button: {
                Toast.makeText(this, "Favorite pressed", Toast.LENGTH_LONG);
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
