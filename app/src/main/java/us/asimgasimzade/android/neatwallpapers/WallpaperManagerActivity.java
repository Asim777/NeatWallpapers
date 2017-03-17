package us.asimgasimzade.android.neatwallpapers;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static us.asimgasimzade.android.neatwallpapers.utils.Utils.REQUEST_ID_SET_AS_WALLPAPER;

/**
 * This activity allows user to choose how to set the wallpaper: full width, standart or free size;
 * cut and resize it before setting
 */

public class WallpaperManagerActivity extends AppCompatActivity {
    Drawable drawable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_manager);

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
            drawable = ContextCompat.getDrawable(this, R.drawable.animals);
        }

        ImageView imageView = (ImageView) findViewById(R.id.wallpaper_manager_image_view);
        imageView.setImageDrawable(drawable);
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
}
