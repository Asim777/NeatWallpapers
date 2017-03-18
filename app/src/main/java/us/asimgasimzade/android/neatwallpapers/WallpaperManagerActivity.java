package us.asimgasimzade.android.neatwallpapers;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * This activity allows user to choose how to set the wallpaper: full width, standart or free size;
 * cut and resize it before setting
 */

public class WallpaperManagerActivity extends AppCompatActivity {
    Drawable drawable;
    TextView standardTextView;
    TextView entireTextView;
    TextView freeTextView;
    Activity thisActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_manager);
        thisActivity = this;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.set_as_wallpaper);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        Uri imageUri = intent.getData();

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            drawable = Drawable.createFromStream(inputStream, imageUri.toString());
        } catch (FileNotFoundException e) {
            //TODO: put proper default image here
            drawable = ContextCompat.getDrawable(this, R.drawable.animals);
        }

        ImageView imageView = (ImageView) findViewById(R.id.wallpaper_manager_image_view);
        imageView.setImageDrawable(drawable);


        standardTextView = (TextView) findViewById(R.id.option_standard);
        entireTextView = (TextView) findViewById(R.id.option_entire);
        freeTextView = (TextView) findViewById(R.id.option_free);

        //Standard button
        standardTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToDefaultBackground(entireTextView, freeTextView);
                standardTextView.setBackgroundColor(ContextCompat.getColor(thisActivity,
                        R.color.colorPrimary));
            }
        });

        //Entire button
        entireTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToDefaultBackground(standardTextView, freeTextView);
                entireTextView.setBackgroundColor(ContextCompat.getColor(WallpaperManagerActivity.this,
                        R.color.colorPrimary));
            }
        });

        //Free button
        freeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToDefaultBackground(standardTextView, entireTextView);
                freeTextView.setBackgroundColor(ContextCompat.getColor(WallpaperManagerActivity.this,
                        R.color.colorPrimary));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.wallpaper_manager_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void changeToDefaultBackground(TextView... textViews) {
        int color;
        Drawable backgroundDrawable;
        for (TextView textView : textViews) {
            backgroundDrawable = textView.getBackground();
            if (backgroundDrawable instanceof ColorDrawable) {
                color = ((ColorDrawable) backgroundDrawable).getColor();
                if (color != ContextCompat.getColor(thisActivity, R.color.white))
                    textView.setBackgroundColor(ContextCompat.getColor(thisActivity, R.color.white));
            }
        }

    }
}

