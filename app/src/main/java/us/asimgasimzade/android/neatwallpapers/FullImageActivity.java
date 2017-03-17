package us.asimgasimzade.android.neatwallpapers;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;

import java.io.File;

import uk.co.senab.photoview.PhotoViewAttacher;
import us.asimgasimzade.android.neatwallpapers.tasks.AddOrRemoveFavoriteAsyncTask;
import us.asimgasimzade.android.neatwallpapers.tasks.ImageIsFavoriteTask;
import us.asimgasimzade.android.neatwallpapers.utils.DownloadTargetInterface;
import us.asimgasimzade.android.neatwallpapers.utils.IsImageFavoriteResponseInterface;
import us.asimgasimzade.android.neatwallpapers.utils.SingleToast;
import us.asimgasimzade.android.neatwallpapers.utils.Utils;

import static us.asimgasimzade.android.neatwallpapers.utils.Utils.createTarget;
import static us.asimgasimzade.android.neatwallpapers.utils.Utils.downloadImage;
import static us.asimgasimzade.android.neatwallpapers.utils.Utils.downloadImageIfPermitted;
import static us.asimgasimzade.android.neatwallpapers.utils.Utils.fileExists;
import static us.asimgasimzade.android.neatwallpapers.utils.Utils.showMessageOKCancel;

/**
 * This activity opens when user clicks on image from SingleImageActivity to see the full
 * scrollable version of image
 */

public class FullImageActivity extends AppCompatActivity implements IsImageFavoriteResponseInterface,
        DownloadTargetInterface {

    private static final int REQUEST_PERMISSION_SETTING = 43;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10;
    boolean imageIsFavorite;
    String imageUrl;
    String imageAuthor;
    String imageLink;
    String imageName;
    MenuItem favoriteActionButton;
    SingleImageFragment.Operation operation;
    ProgressDialog downloadProgressDialog;
    ProgressBar loadingAnimationProgressBar;
    SimpleTarget<Bitmap> target;
    File imageFileForChecking;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        //Set action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Enable up button
        final Drawable upArrow = ContextCompat.getDrawable(this, R.mipmap.ic_up);
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Make the activity full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Get the imageview and wrap it in photoViewAttacher so that user can zoom, scroll
        // and double tap the imageview
        ImageView imageView = (ImageView) findViewById(R.id.fullImageView);
        PhotoViewAttacher photoViewAttacher;
        photoViewAttacher = new PhotoViewAttacher(imageView);
        photoViewAttacher.setMaximumScale(2);
        photoViewAttacher.update();

        //Get image attributes from the intent
        imageUrl = getIntent().getStringExtra("url");
        imageAuthor = getIntent().getStringExtra("author");
        imageLink = getIntent().getStringExtra("link");
        imageName = getIntent().getStringExtra("name");

        loadingAnimationProgressBar = (ProgressBar) findViewById(R.id.loading_progress_bar);
        loadingAnimationProgressBar.setVisibility(View.VISIBLE);

        Glide.with(getApplicationContext()).load(imageUrl).
                listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model,
                                               Target<GlideDrawable> target, boolean isFirstResource) {
                        loadingAnimationProgressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target,
                                                   boolean isFromMemoryCache, boolean isFirstResource) {
                        loadingAnimationProgressBar.setVisibility(View.GONE);
                        return false;
                    }
                }).into(imageView);

        //Get image is favorite boolean from intent
        imageIsFavorite = getIntent().getBooleanExtra("image_is_favorite", false);

        //We'll use this file to check if given image already exists on device and take corresponding
        //course of action depending on that
        //Specifying path to our app's directory
        File path = Environment.getExternalStoragePublicDirectory("NeatWallpapers");
        //Creating imageFile using path to our custom album
        imageFileForChecking = new File(path, "NEATWALLPAPERS_" + imageName + ".jpg");

        downloadProgressDialog = new ProgressDialog(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.full_image_menu, menu);

        favoriteActionButton = menu.findItem(R.id.action_favorite);
        if (imageIsFavorite) {
            favoriteActionButton.setIcon(ContextCompat.getDrawable(this, R.mipmap.ic_white_favorite_selected));
        } else {
            favoriteActionButton.setIcon(ContextCompat.getDrawable(this, R.mipmap.ic_white_favorite));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        int id = item.getItemId();
        switch (id) {

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_set:
                //Change the current Wallpaper:
                //if image doesn't exist then download it first
                operation = SingleImageFragment.Operation.SET_AS_WALLPAPER;
                //Creating target
                createTarget(this, this, operation, imageName, imageFileForChecking, downloadProgressDialog);
                if (!fileExists(imageFileForChecking)) {
                    downloadImageIfPermitted(this, imageUrl, target);
                } else {
                    //if it exists, just set it as wallpaper
                    Utils.setWallpaper(this, imageFileForChecking);
                }
                return true;

            case R.id.action_download:
                //Downloading image
                // if it already exists Toast message, saying that it does
                operation = SingleImageFragment.Operation.DOWNLOAD;

                //Creating target
                createTarget(this, this, operation, imageName, imageFileForChecking, downloadProgressDialog);
                if (fileExists(imageFileForChecking)) {
                    SingleToast.show(this.getApplicationContext(),
                            getString(R.string.image_already_exists_message), Toast.LENGTH_SHORT);
                } else {
                    //If it doesn't exist, download it, but first check if we have permission to do it
                    downloadImageIfPermitted(this, imageUrl, target);
                }
                return true;

            case R.id.action_favorite:

                new ImageIsFavoriteTask(this, imageIsFavorite, this, imageName).execute();
                //When favorite button inside FullImageActivity is clicked, we are adding this
                //image to Favorites database and changing background of a button
                new AddOrRemoveFavoriteAsyncTask(FullImageActivity.this, imageIsFavorite,
                        FullImageActivity.this, imageName, imageUrl, imageAuthor, imageLink).execute();
                //Setting delegate back to this instance of SingleImageFragment
                //Sending back return intent to FullImageActivity so that it calls callback
                // in FavoritesFragment to update it's GridView with new data
                Intent databaseIsChangedIntent = new Intent();
                FullImageActivity.this.setResult(Activity.RESULT_OK, databaseIsChangedIntent);
                return true;

            default: {
                return super.onOptionsItemSelected(item);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    SingleToast.show(getApplicationContext(), getString(R.string.permission_granted_message),
                            Toast.LENGTH_SHORT);
                    downloadImage(this, imageUrl, target);
                } else {
                    //Permission is not granted, but did the user also check "Never ask again"?
                    if (!showRationale) {
                        // user denied permission and also checked "Never ask again"
                        showMessageOKCancel(this, getString(R.string.permission_message),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                                    }
                                });
                    }
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }

    }

    @Override
    public void getTheTarget(SimpleTarget<Bitmap> response) {
        target = response;
    }

    @Override
    public void updateImageIsFavorite(boolean response) {
        imageIsFavorite = response;

        if (imageIsFavorite) {
            favoriteActionButton.setIcon(ContextCompat.getDrawable(this, R.mipmap.ic_white_favorite_selected));
        } else {
            favoriteActionButton.setIcon(ContextCompat.getDrawable(this, R.mipmap.ic_white_favorite));
        }
    }

}
