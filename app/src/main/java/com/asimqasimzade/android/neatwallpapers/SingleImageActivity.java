package com.asimqasimzade.android.neatwallpapers;

import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;


public class SingleImageActivity extends AppCompatActivity {
    private static final String LOG_TAG = SingleImageActivity.class.getSimpleName();
    int favoritePressedTimes = 0;
    Button favoriteButton;
    Button backButton;
    Button setAsWallpaperButton;
    Button downloadButton;
    String image;
    String name;
    SimpleTarget<Bitmap> target;
    File imageFile;
    FileOutputStream outputStream;
    boolean downloadIsSuccessfull;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_image);

        //Loading image
        image = getIntent().getStringExtra("image");
        name = getIntent().getStringExtra("name");
        ImageView singleImageView = (ImageView) findViewById(R.id.single_image_view);
        Glide.with(this).load(image).into(singleImageView);

        //Back button
        backButton = (Button) findViewById(R.id.single_image_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //When back button inside SingleImageActivity is clicked, we are finishing this
                //activity and automatically going back to the previous activity
                finish();
            }
        });

        //Favorite button
        favoriteButton = (Button) findViewById(R.id.single_image_favorite_button);
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //When favorite button inside SingleImageActivity is clicked, we are adding this
                //image to Favorites database and changing background of a button
                favoritePressedTimes++;
                //TODO: Substitute it with code that checks if image is in favorites
                if (favoritePressedTimes % 2 != 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        favoriteButton.setBackground(ContextCompat.getDrawable(SingleImageActivity.this, R.mipmap.ic_favorite_selected));
                    } else {
                        favoriteButton.setBackgroundDrawable(ContextCompat.getDrawable(SingleImageActivity.this, R.mipmap.ic_favorite_selected));
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        favoriteButton.setBackground(ContextCompat.getDrawable(SingleImageActivity.this, R.mipmap.ic_favorite));
                    } else {
                        favoriteButton.setBackgroundDrawable(ContextCompat.getDrawable(SingleImageActivity.this, R.mipmap.ic_favorite));
                    }
                }
                //TODO: Add code to add image to Favorite Database
            }
        });

        //Set as wallpaper button
        setAsWallpaperButton = (Button) findViewById(R.id.set_as_wallpaper_button);


        setAsWallpaperButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });


        //Download button

        target = new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(final Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                new AsyncTask<Void, Void, Boolean>() {

                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        //Specifying path to our app's directory
                        File path = Environment.getExternalStoragePublicDirectory("NeatWallpapers");
                        //Creating imageFile using path to our custom album
                        File imageFile = new File(path, "NEATWALLPAPERS_" + name + ".jpg");

                        //Creating our custom album directory, if it's not created, logging error message
                        if (!path.mkdirs()) {
                            Log.e(LOG_TAG, "Directory not created");
                        }

                        //We are checking if there is ExternalStorage mounted on device and is it
                        //readable
                        if (isExternalStorageWritable()) {
                            try {
                                outputStream = new FileOutputStream(imageFile);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                                if (imageFile.exists()) {
                                    Log.e(LOG_TAG, "Image successfully downloaded");
                                    downloadIsSuccessfull = true;
                                } else {
                                    Log.e(LOG_TAG, "Problem while downloading image");
                                    downloadIsSuccessfull = false;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    outputStream.flush();
                                    outputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            // Tell the media scanner about the new file so that it is
                            // immediately available to the user.
                            MediaScannerConnection.scanFile(getApplicationContext(),
                                    new String[]{imageFile.getAbsolutePath()},
                                    null,
                                    new MediaScannerConnection.OnScanCompletedListener() {
                                        public void onScanCompleted(String path, Uri uri) {
                                            Log.i("ExternalStorage", "Scanned " + path + ":");
                                            Log.i("ExternalStorage", "-> uri=" + uri);
                                        }
                                    }
                            );
                        } else {
                            Log.e(LOG_TAG, "External memory is not available to write");
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        //Checking the result and giving feedback to user about success
                        checkResult();
                    }
                }.execute();
            }
        };

        downloadButton = (Button) findViewById(R.id.download_button);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Glide.with(getApplicationContext()).load(image).asBitmap().into(target);
            }
        });


    }

    private void checkResult(){
        if (downloadIsSuccessfull) {
            Toast.makeText(getApplicationContext(), "Image is successfully saved",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(SingleImageActivity.this,
                    "Problem while downloading image, please try again",
                    Toast.LENGTH_LONG).show();
        }
    }
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
}
