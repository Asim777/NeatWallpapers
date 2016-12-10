package com.asimqasimzade.android.neatwallpapers;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    SimpleTarget<Bitmap> targetDownload;
    SimpleTarget<Bitmap> targetSetAsWallpaper;
    File imageFile;
    File imageFileForChecking;
    FileOutputStream outputStream;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_image);

        //We'll use this file to check if given image already exists on device and take corresponding
        //course of action depending on that
        //Specifying path to our app's directory
        File path = Environment.getExternalStoragePublicDirectory("NeatWallpapers");
        //Creating imageFile using path to our custom album
        imageFileForChecking = new File(path, "NEATWALLPAPERS_" + name + ".jpg");

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

        targetSetAsWallpaper = new SimpleTarget<Bitmap>() {

            @Override
            public void onResourceReady(final Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        //Specifying path to our app's directory
                        File path = Environment.getExternalStoragePublicDirectory("NeatWallpapers");
                        //Creating imageFile using path to our custom album
                        imageFile = new File(path, "NEATWALLPAPERS_" + name + ".jpg");

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
                        if (doesFileExist(imageFile)) {
                            setWallpaper(imageFile);
                            Log.e(LOG_TAG, "Wallpaper Set successfully");
                            Toast.makeText(getApplicationContext(), "Wallpaper Set successfully",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Log.e(LOG_TAG, "Problem while setting wallpaper");
                            Toast.makeText(SingleImageActivity.this,
                                    "Problem while setting wallpaper, please try again",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }.execute();
            }
        };

        setAsWallpaperButton = (Button) findViewById(R.id.set_as_wallpaper_button);

        setAsWallpaperButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Change the current Wallpaper:
                //if image doesn't exist then download it first
                if (!doesFileExist(imageFileForChecking)) {
                    Glide.with(getApplicationContext()).load(image).asBitmap().into(targetSetAsWallpaper);
                } else {
                    //if it exists, just set it as wallpaper
                    setWallpaper(imageFileForChecking);
                }
            }
        });


        //Download button

        targetDownload = new SimpleTarget<Bitmap>() {

            @Override
            public void onResourceReady(final Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        //Specifying path to our app's directory
                        File path = Environment.getExternalStoragePublicDirectory("NeatWallpapers");
                        //Creating imageFile using path to our custom album
                        imageFile = new File(path, "NEATWALLPAPERS_" + name + ".jpg");

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
                        if (doesFileExist(imageFile)) {
                            Log.e(LOG_TAG, "Image successfully downloaded");
                            Toast.makeText(getApplicationContext(), "Image is successfully saved",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Log.e(LOG_TAG, "Problem while downloading image");
                            Toast.makeText(SingleImageActivity.this,
                                    "Problem while downloading image, please try again",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }.execute();
            }
        };

        downloadButton = (Button) findViewById(R.id.download_button);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Downlading image
                // if it already exists Toast message, saying that it does
                if (doesFileExist(imageFileForChecking)) {
                    Toast.makeText(getApplicationContext(), "Image already exists. Check your Gallery.", Toast.LENGTH_LONG).show();
                } else {
                    //if it doesn't exist, download it
                    Glide.with(getApplicationContext()).load(image).asBitmap().into(targetDownload);
                }
            }
        });


    }

    private void setWallpaper(File imageFile) {
        /*Intent setAsIntent = new Intent(Intent.ACTION_ATTACH_DATA);
        intent.setDataAndType(imageFile.getAbsolutePath(), "image*//*")*/
        try {
            //Retrieve a WallpaperManager
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(SingleImageActivity.this);

            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            wallpaperManager.setBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(SingleImageActivity.this, "Wallpaper isn't set. Please try again", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * *This method checks if downloading image was successful so we can show according feedback to
     * the user
     * Returns boolean - true if successful and false if unsuccessful
     */
    private boolean doesFileExist(File image) {
        boolean result;
        if (image.exists() && image.isFile()) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    /**
     * This method cheks if there is External Storage currently mounted in device. It returns boolean
     *
     * @return boolean, true if External Storage is mounted, false if not
     */
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * This method cheks if there is External Storage currently mounted or in Read Only Mode. It returns boolean
     *
     * @return boolean, true if External Storage is mounted or in Read Only Mode, false if not
     */
    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
}
