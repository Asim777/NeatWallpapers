package us.asimgasimzade.android.neatwallpapers;


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
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import uk.co.senab.photoview.PhotoViewAttacher;
import us.asimgasimzade.android.neatwallpapers.data.GridItem;
import us.asimgasimzade.android.neatwallpapers.tasks.DownloadImageAsyncTask;
import us.asimgasimzade.android.neatwallpapers.utils.Utils;

import static us.asimgasimzade.android.neatwallpapers.utils.Utils.downloadImage;
import static us.asimgasimzade.android.neatwallpapers.utils.Utils.downloadImageIfPermitted;
import static us.asimgasimzade.android.neatwallpapers.utils.Utils.fileExists;
import static us.asimgasimzade.android.neatwallpapers.utils.Utils.showMessageOKCancel;
import static us.asimgasimzade.android.neatwallpapers.utils.Utils.showToast;

/**
 * This activity opens when user clicks on image from SingleImageActivity to see the full
 * scrollable version of image
 */

public class FullImageActivity extends AppCompatActivity  {

    private static final int REQUEST_PERMISSION_SETTING = 43;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10;
    boolean imageIsFavorite;
    String currentImageUrl;
    String currentImageAuthor;
    String currentImageLink;
    String currentImageThumbnail;
    String currentImageName;
    MenuItem favoriteActionButton;
    SingleImageFragment.Operation operation;
    ProgressDialog downloadProgressDialog;
    ProgressBar loadingAnimationProgressBar;
    SimpleTarget<Bitmap> target;
    File imageFileForChecking;
    DownloadImageAsyncTask downloadImageTask;
    private boolean downloadImageTaskCancelled;
    private DatabaseReference favoritesReference;
    private ValueEventListener favoritesListener;

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
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Make the activity full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Get the imageView and wrap it in photoViewAttacher so that user can zoom, scroll
        // and double tap the imageView
        ImageView imageView = (ImageView) findViewById(R.id.fullImageView);
        PhotoViewAttacher photoViewAttacher;
        photoViewAttacher = new PhotoViewAttacher(imageView);
        photoViewAttacher.setMaximumScale(2);
        photoViewAttacher.update();

        //Get image attributes from the intent
        Intent intent = getIntent();
        currentImageUrl = intent.getStringExtra("url");
        currentImageAuthor = intent.getStringExtra("author");
        currentImageLink = intent.getStringExtra("link");
        currentImageThumbnail = intent.getStringExtra("thumbnail");
        currentImageName = intent.getStringExtra("name");

        loadingAnimationProgressBar = (ProgressBar) findViewById(R.id.loading_progress_bar);
        loadingAnimationProgressBar.setVisibility(View.VISIBLE);

        Glide.with(getApplicationContext()).load(currentImageUrl).
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
        imageFileForChecking = new File(path, "NEATWALLPAPERS_" + currentImageName + ".jpg");

        //Initiating FireBase database and getting reference to favorites
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser authUser = auth.getCurrentUser();
        String userId = authUser != null ? authUser.getUid() : null;
        //Get instance of FireBase database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //Get reference to current user's favorites node
        if (userId != null) {
            favoritesReference = database.getReference("users").child(userId).child("favorites");
        }


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

                if (!fileExists(imageFileForChecking)) {

                    //Creating target
                    target = createTarget();
                    //Creating progress dialog
                    createProgressDialog();
                    //Show downloading progress dialog
                    downloadProgressDialog.show();

                    downloadImageIfPermitted(this, currentImageUrl, target);
                } else {
                    //if it exists, just set it as wallpaper
                    Utils.setWallpaper(this, imageFileForChecking);
                }
                return true;

            case R.id.action_download:
                //Downloading image
                // if it already exists Toast message, saying that it does
                operation = SingleImageFragment.Operation.DOWNLOAD;

                if (fileExists(imageFileForChecking)) {
                    showToast(this.getApplicationContext(),
                            getString(R.string.image_already_exists_message), Toast.LENGTH_SHORT);
                } else {
                    //Creating target
                    target = createTarget();
                    //Creating progress dialog
                    createProgressDialog();
                    //Show downloading progress dialog
                    downloadProgressDialog.show();

                    //If it doesn't exist, download it, but first check if we have permission to do it
                    downloadImageIfPermitted(this, currentImageUrl, target);
                }
                return true;

            case R.id.action_favorite:

                //When favorite button inside FullImageActivity is clicked, we are adding this
                //image to Favorites database and changing background of a button
                if (imageIsFavorite) {
                    //Image is favorite, we are removing it from database
                    favoritesReference.child(currentImageName).removeValue();
                    showToast(getApplicationContext(), getResources().getString(R.string.image_removed_from_favorites_message),Toast.LENGTH_SHORT);
                } else {
                    //Create new favorite image and put into FireBase database
                    GridItem currentItem = new GridItem(currentImageUrl, currentImageName,
                            currentImageAuthor, currentImageLink, currentImageThumbnail);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy-hhmmss", Locale.US);
                    String timestamp = simpleDateFormat.format(new Date());

                    favoritesReference.child(currentImageName).setValue(currentItem, timestamp);
                    showToast(getApplicationContext(), this.getResources().getString(R.string.image_added_to_favorites_message),Toast.LENGTH_SHORT);
                }

                return true;

            default: {
                return super.onOptionsItemSelected(item);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        favoritesListener = favoritesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Find out if this image exists in favorites node
                imageIsFavorite = dataSnapshot.child(currentImageName).exists();
                //We need to check favoriteActionButton not null, because if user clicks favorite button
                //in SingleImageActivity and then immediately enters FullImageActivity, onDataChange
                //is called before onCreateOptionsMenu where favoriteActionButton is instantiated and
                //it causes nullPointerException in updateImageIsFavorite() method
                if(favoriteActionButton != null){
                    updateImageIsFavorite(imageIsFavorite);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
                    showToast(getApplicationContext(), getString(R.string.permission_granted_message),
                            Toast.LENGTH_SHORT);
                    downloadImage(this, currentImageUrl, target);
                } else {
                    //Permission is not granted, but did the user also check "Never ask again"?
                    if (!showRationale) {
                        // user denied permission and also checked "Never ask again"
                        showMessageOKCancel(this, getString(R.string.download_permission_message),
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

    private void createProgressDialog() {
        //Creating progress dialog
        downloadProgressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
        downloadProgressDialog.setMessage(getString(R.string.message_downloading_image));
        downloadProgressDialog.setCancelable(true);
        downloadProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                //Cancel the task if user presses back while mDownloadProgressDialog
                // is shown
                if (downloadImageTask != null) {
                    downloadImageTask.cancel(true);
                } else {
                    downloadImageTaskCancelled = true;
                }
            }
        });
        downloadProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadProgressDialog.dismiss();
                        //If downloadImageTask exists, cancel it, if it doesn't exist yet, update
                        //the boolean so that it never gets started from Target's onResourceReady
                        if (downloadImageTask != null) {
                            downloadImageTask.cancel(true);
                        } else {
                            downloadImageTaskCancelled = true;
                        }
                    }
                });
        downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        downloadProgressDialog.setIndeterminate(false);
        downloadProgressDialog.setMax(100);
    }

    public SimpleTarget<Bitmap> createTarget() {

        return new SimpleTarget<Bitmap>() {

            @Override
            public void onResourceReady(final Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                if (!downloadImageTaskCancelled) {
                    downloadImageTask = new DownloadImageAsyncTask(FullImageActivity.this, operation, currentImageName,
                            imageFileForChecking, bitmap, downloadProgressDialog);
                    downloadImageTask.execute();
                } else {
                    downloadImageTaskCancelled = false;
                }
            }

        };
    }


    @Override
    public void onPause() {
        super.onPause();
        //Remove download progress dialog when fragment goes to background
        if (downloadProgressDialog != null) {
            downloadProgressDialog.dismiss();
        }
        if( favoritesListener != null && favoritesReference != null){
            favoritesReference.removeEventListener(favoritesListener);
        }
    }

    public void updateImageIsFavorite(boolean response) {
        imageIsFavorite = response;

        if (imageIsFavorite) {
            favoriteActionButton.setIcon(ContextCompat.getDrawable(this, R.mipmap.ic_white_favorite_selected));
        } else {
            favoriteActionButton.setIcon(ContextCompat.getDrawable(this, R.mipmap.ic_white_favorite));
        }
    }

}
