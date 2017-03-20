package us.asimgasimzade.android.neatwallpapers;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import us.asimgasimzade.android.neatwallpapers.utils.SingleToast;
import us.asimgasimzade.android.neatwallpapers.utils.Utils;

import static us.asimgasimzade.android.neatwallpapers.utils.Utils.getBitmapFromUri;

/**
 * This activity allows user to choose how to set the wallpaper: full width, standart or free size;
 * cut and resize it before setting
 */

public class WallpaperManagerActivity extends AppCompatActivity {
    Bitmap currentBitmap;
    Bitmap defaultBitmap;
    Bitmap resultBitmap;
    double currentBitmapWidth;
    double currentBitmapHeight;
    int aspectRatioWidth;
    int aspectRatioHeight;
    TextView standardTextView;
    TextView entireTextView;
    TextView freeTextView;
    Activity thisActivity;
    CropImageView cropImageView;
    Intent intent;
    Uri imageUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_manager);
        //Getting instance of current Activity to use in inner classes instead of WallpaperManagerActivity.this
        thisActivity = this;

        //Setting title of ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.set_as_wallpaper);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Getting image's Uri from intent
        intent = getIntent();
        imageUri = intent.getData();

        //Getting our bitmap from imageUri we got from intent
        //We need it in order to get image's aspect ratio so that we can set it when user selects
        //"Entire" aspect ratio option
        try {
            currentBitmap = getBitmapFromUri(thisActivity, imageUri);
            if (currentBitmap != null) {
                //Getting width and height of image
                currentBitmapWidth = currentBitmap.getWidth();
                currentBitmapHeight = currentBitmap.getHeight();
                //Dividing width to height
                double initialAspectRatio = currentBitmapWidth / currentBitmapHeight;
                //Getting fractional part, rounding it up to 2 digits after comma and
                // multiplying it by 100 to get 2 digits, casting it to int and now we have our
                // aspect ratio height
                aspectRatioHeight = (int) (new BigDecimal(initialAspectRatio).remainder(BigDecimal.ONE)
                        .setScale(2, RoundingMode.HALF_UP).doubleValue() * 100);
                //Multiplying aspect ratio height by result of dividing inage width to height and
                //getting our aspect ratio width
                aspectRatioWidth = (int) (aspectRatioHeight * initialAspectRatio);
            }
        } catch (IOException e) {
            e.printStackTrace();
            //TODO: put proper default placeholder image here - defaultBitmap
            SingleToast.show(getApplicationContext(),
                    "There was a problem while downloading the image. Please try again", Toast.LENGTH_LONG);
        }

        //Getting reference to CropImageView and feeding our imageUri to it
        cropImageView = (CropImageView) findViewById(R.id.wallpaper_manager_cropImage_view);
        cropImageView.setImageUriAsync(imageUri);
        //Disabling auto-zooming when selected small portion of image
        cropImageView.setAutoZoomEnabled(false);
        //Set initial aspect ratio to standard
        cropImageView.setAspectRatio(1, 1);

        //Getting reference to our crop options button textviews
        standardTextView = (TextView) findViewById(R.id.option_standard);
        entireTextView = (TextView) findViewById(R.id.option_entire);
        freeTextView = (TextView) findViewById(R.id.option_free);

        //Standard button
        standardTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToDefaultBackground(entireTextView, freeTextView);
                standardTextView.setBackgroundColor(ContextCompat.getColor(thisActivity, R.color.colorPrimary));
                cropImageView.setAspectRatio(1, 1);
            }
        });

        //Entire button
        entireTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToDefaultBackground(standardTextView, freeTextView);
                entireTextView.setBackgroundColor(ContextCompat.getColor(thisActivity, R.color.colorPrimary));
                cropImageView.setAspectRatio(aspectRatioWidth, aspectRatioHeight);
            }
        });

        //Free button
        freeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToDefaultBackground(standardTextView, entireTextView);
                freeTextView.setBackgroundColor(ContextCompat.getColor(thisActivity, R.color.colorPrimary));
                cropImageView.clearAspectRatio();
            }
        });

        cropImageView.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
            @Override
            public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {

                //Get wallpaper manager and set resultBitmap as wallpaper
                WallpaperManager wallpaperManager
                        = WallpaperManager.getInstance(getApplicationContext());
                try {
                    resultBitmap = result.getBitmap();
                    wallpaperManager.setBitmap(resultBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

            case R.id.action_set:
                //Set current image as Wallpaper:
                cropImageView.getCroppedImageAsync();
                new SuccessDialogFragment().show(getSupportFragmentManager(), "Success");

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

