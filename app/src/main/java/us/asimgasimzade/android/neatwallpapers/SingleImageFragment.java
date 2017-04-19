package us.asimgasimzade.android.neatwallpapers;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import us.asimgasimzade.android.neatwallpapers.data.GridItem;
import us.asimgasimzade.android.neatwallpapers.data.ImagesDataClass;
import us.asimgasimzade.android.neatwallpapers.tasks.DownloadImageAsyncTask;

import static us.asimgasimzade.android.neatwallpapers.utils.Utils.checkNetworkConnection;
import static us.asimgasimzade.android.neatwallpapers.utils.Utils.fileExists;
import static us.asimgasimzade.android.neatwallpapers.utils.Utils.showMessageOKCancel;
import static us.asimgasimzade.android.neatwallpapers.utils.Utils.showToast;

/**
 * Fragment that holds single image page in SingleImage view pager
 */

public class SingleImageFragment extends Fragment {

    private static final int REQUEST_PERMISSION_SETTING = 43;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10;
    private static final int FULL_IMAGE_REQUEST_CODE = 105;
    int currentPosition;
    boolean imageIsFavorite;
    String currentImageUrl;
    String currentImageAuthor;
    String currentImageLink;
    String currentImageThumbnail;
    String currentImageName;
    String source;
    String url;
    File imageFileForChecking;
    SimpleTarget<Bitmap> target;
    ArrayList<GridItem> currentImagesList;
    Operation operation;
    //Downloading progress dialog
    ProgressDialog downloadProgressDialog;
    DownloadImageAsyncTask downloadImageTask;
    private boolean downloadImageTaskCancelled;
    SharedPreferences sharedPreferences;
    private DatabaseReference favoritesReference;
    WeakReference<FragmentActivity> activityWeakReference;
    private boolean permission;
    private Drawable favoriteDrawable;
    private Drawable favoriteSelectedDrawable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("AsimTag", "SingleImageFragment onCreate() called");
        activityWeakReference = new WeakReference<>(getActivity());
        //Getting url of current selected image from ImagesDataClass using imageNumber from
        // intent and ViewPager page position
        Bundle bundle = getArguments();
        currentPosition = bundle.getInt("image_number");
        source = bundle.getString("image_source");
        url = bundle.getString("current_url");
        //Get instance of SharedPreferences
        sharedPreferences = activityWeakReference.get().getSharedPreferences("SINGLE_IMAGE_SP", Context.MODE_PRIVATE);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser authUser = auth.getCurrentUser();
        String userId = authUser != null ? authUser.getUid() : null;
        //Get instance of FireBase database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //Get reference to current user's favorites node
        if (userId != null) {
            favoritesReference = database.getReference("users").child(userId).child("favorites");
        }
        //Checking if permission to WRITE_EXTERNAL_STORAGE is granted by user
        permission = isPermissionWriteToExternalStorageGranted();

    }

    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d("AsimTag", "SingleImageFragment onCreateView() called");
        assert source != null;
        switch (source) {
            case "popular": {
                currentImagesList = ImagesDataClass.popularImagesList;
            }
            break;
            case "recent": {
                currentImagesList = ImagesDataClass.recentImagesList;
            }
            break;
            case "favorites": {
                currentImagesList = ImagesDataClass.favoriteImagesList;
            }
            break;
            case "search": {
                currentImagesList = ImagesDataClass.searchResultImagesList;
            }
            break;
            case "default": {
                currentImagesList = ImagesDataClass.defaultImagesList;
            }
        }

        favoriteDrawable = ContextCompat.getDrawable(activityWeakReference.get(), R.mipmap.ic_favorite);
        favoriteSelectedDrawable = ContextCompat.getDrawable(activityWeakReference.get(), R.mipmap.ic_favorite_selected);

        final GridItem currentItem = currentImagesList.get(currentPosition);
        currentImageName = currentItem.getName();
        currentImageAuthor = currentItem.getAuthor();
        currentImageUrl = currentItem.getImage();
        currentImageThumbnail = currentItem.getThumbnail();
        currentImageLink = currentItem.getLink();

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_single_image, container, false);

        //Downloading and setting image
        ImageView imageView = (ImageView) rootView.findViewById(R.id.single_image_view);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fullImageIntent = new Intent(activityWeakReference.get(), FullImageActivity.class).
                        putExtra("url", currentImageUrl).
                        putExtra("author", currentImageAuthor).
                        putExtra("link", currentImageLink).
                        putExtra("name", currentImageName).
                        putExtra("thumbnail", currentImageThumbnail).
                        putExtra("image_is_favorite", imageIsFavorite);
                startActivityForResult(fullImageIntent, FULL_IMAGE_REQUEST_CODE);
            }
        });

        // Loading animation progress bar
        final ProgressBar loadingAnimationProgressBar = (ProgressBar) rootView.findViewById(R.id.loading_progress_bar);
        loadingAnimationProgressBar.setVisibility(View.VISIBLE);

        Glide.with(activityWeakReference.get().getApplicationContext()).load(currentImageUrl).
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

        //Setting author info
        TextView authorInfoTextView = (TextView) rootView.findViewById(R.id.author_info_text_view);
        authorInfoTextView.setText(String.format(getResources().getString(R.string.author_info), currentImageAuthor));

        //Setting image link
        TextView imageLinkTextView = (TextView) rootView.findViewById(R.id.image_link_text_view);
        imageLinkTextView.setPaintFlags(imageLinkTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        imageLinkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openImageLinkIntent = new Intent(Intent.ACTION_VIEW);
                openImageLinkIntent.setData(Uri.parse("https://pixabay.com/goto/" + currentImageName + "/"));
                startActivity(openImageLinkIntent);
            }
        });

        //We'll use this file to check if given image already exists on device and take corresponding
        //course of action depending on that
        //Specifying path to our app's directory
        File path = Environment.getExternalStoragePublicDirectory("NeatWallpapers");
        //Creating imageFile using path to our custom album
        imageFileForChecking = new File(path, "NEATWALLPAPERS_" + currentImageName + ".jpg");


        //-----------------------------------------------------------------------------------------
        //Back button
        //-----------------------------------------------------------------------------------------
        Button backButton = (Button) rootView.findViewById(R.id.single_image_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //When back button inside SingleImageActivity is clicked, we are initiating
                //back button pressed and automatically going back to the previous activity
                activityWeakReference.get().onBackPressed();
            }
        });

        //-----------------------------------------------------------------------------------------
        // Favorite button
        //-----------------------------------------------------------------------------------------
        final Button favoriteButton = (Button) rootView.findViewById(R.id.single_image_favorite_button);

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //When favorite button inside SingleImageActivity is clicked, we are adding this
                //image to Favorites database and changing background of a button
                if (imageIsFavorite) {
                    //Image is favorite, we are removing it from database
                    //We are doing it in parallel thread so that user sees toast message immediately.
                    // We are also changing favorite button background from here because addValueEventListener
                    // might take some time to update it. If it's not removed successfully, favorite
                    //button will be changed back anyway, when onDataChange in listener is triggered
                    new RemoveFavoriteTask(favoritesReference, currentImageName).start();

                    updateImageIsFavorite(false, favoriteButton);
                    showToast(activityWeakReference.get().getApplicationContext(), activityWeakReference.get().getResources().getString(R.string.image_removed_from_favorites_message),Toast.LENGTH_SHORT);
                } else {
                    //Create new favorite image and put into FireBase database
                    //Image is favorite, we are removing it from database
                    //We are doing it in parallel thread so that user sees toast message immediately.
                    // We are also changing favorite button background from here because addValueEventListener
                    // might take some time to update it. If it's not added successfully, favorite
                    //button will be changed back anyway, when onDataChange in listener is triggered
                    new AddFavoriteTask(favoritesReference, currentItem).start();

                    updateImageIsFavorite(true, favoriteButton);
                    showToast(activityWeakReference.get().getApplicationContext(), activityWeakReference.get().getResources().getString(R.string.image_added_to_favorites_message),Toast.LENGTH_SHORT);

                }
            }

        });

        //-----------------------------------------------------------------------------------------
        // Set as wallpaper button
        //-----------------------------------------------------------------------------------------

        Button setAsWallpaperButton = (Button) rootView.findViewById(R.id.set_as_wallpaper_button);

        setAsWallpaperButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Check if network is available
                if (checkNetworkConnection(activityWeakReference.get().getApplicationContext())) {
                    //Change the current Wallpaper, if image doesn't exist then download it first
                    operation = Operation.SET_AS_WALLPAPER;

                    if (!fileExists(imageFileForChecking)) {

                        //Creating target
                        target = createTarget();
                        //Creating progress dialog
                        createProgressDialog();
                        //Show downloading progress dialog
                        downloadProgressDialog.show();

                        downloadImageIfPermitted(permission, currentImageUrl, target);
                    } else {
                        //if it exists, just set it as wallpaper
                        setWallpaper(imageFileForChecking);
                    }
                }
            }
        });


        //-----------------------------------------------------------------------------------------
        // Download button
        //-----------------------------------------------------------------------------------------

        Button downloadButton = (Button) rootView.findViewById(R.id.download_button);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Check if network is available
                if (checkNetworkConnection(activityWeakReference.get().getApplicationContext())) {


                    //Downloading image
                    // if it already exists Toast message, saying that it does
                    operation = Operation.DOWNLOAD;

                    if (fileExists(imageFileForChecking)) {
                        showToast(activityWeakReference.get().getApplicationContext().getApplicationContext(),
                                getString(R.string.image_already_exists_message), Toast.LENGTH_SHORT);
                    } else {
                        //Creating target
                        target = createTarget();
                        //Create Progress dialog
                        createProgressDialog();
                        //Show downloading progress dialog
                        downloadProgressDialog.show();
                        //If it doesn't exist, download it, but first check if we have permission to do it
                        downloadImageIfPermitted(permission, currentImageUrl, target);
                    }
                }
            }
        });


        favoritesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Find out if this image exists in favorites node
                imageIsFavorite = dataSnapshot.child(currentImageName).exists();
                updateImageIsFavorite(imageIsFavorite, favoriteButton);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return rootView;
    }

    private void createProgressDialog() {
        //Creating progress dialog
        downloadProgressDialog = new ProgressDialog(activityWeakReference.get(), R.style.AppCompatAlertDialogStyle);
        downloadProgressDialog.setTitle(activityWeakReference.get().getString(R.string.title_downloading_image));
        downloadProgressDialog.setMessage(activityWeakReference.get().getString(R.string.message_downloading_image));
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean showRationale = shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is not granted, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    showToast(activityWeakReference.get().getApplicationContext(), getString(R.string.permission_granted_message),
                            Toast.LENGTH_SHORT);
                    downloadImage(currentImageUrl, target);
                } else {
                    //Permission is not granted, but did the user also check "Never ask again"?
                    if (!showRationale) {
                        // user denied permission and also checked "Never ask again"
                        showMessageOKCancel(activityWeakReference.get(), getString(R.string.download_permission_message),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", activityWeakReference.get().getPackageName(), null);
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
    public void onPause() {
        super.onPause();
        //Remove download progress dialog when fragment goes to background
        if (downloadProgressDialog != null) {
            downloadProgressDialog.dismiss();
        }
    }

    public void updateImageIsFavorite(boolean response, Button favoriteButton) {
        imageIsFavorite = response;

        if (imageIsFavorite) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                favoriteButton.setBackground(favoriteSelectedDrawable);
            } else {
                //noinspection deprecation
                favoriteButton.setBackgroundDrawable(favoriteSelectedDrawable);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                favoriteButton.setBackground(favoriteDrawable);
            } else {
                //noinspection deprecation
                favoriteButton.setBackgroundDrawable(favoriteDrawable);
            }
        }
    }

    private boolean isPermissionWriteToExternalStorageGranted() {
        return (ActivityCompat.checkSelfPermission(activityWeakReference.get(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * This method checks if user granted a permission to download the image by calling
     * isPermissionWriteToExternalStorageGranted, and if yes, it calls dowloadImage() method,
     * if not, it shows Request Permission dialog
     */
    public void downloadImageIfPermitted(boolean isPermissionGranted, String currentImageUrl,
                                                SimpleTarget<Bitmap> target) {
        //Checking if permission to WRITE_EXTERNAL_STORAGE is granted by user
        if (isPermissionGranted) {
            //If it's granted, just download the image
            downloadImage(currentImageUrl, target);
        } else {
            //If it's not granted, request it
            SingleImageFragment.this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    public SimpleTarget<Bitmap> createTarget() {

        return new SimpleTarget<Bitmap>() {

            @Override
            public void onResourceReady(final Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                if (!downloadImageTaskCancelled) {
                    downloadImageTask = new DownloadImageAsyncTask(activityWeakReference.get(), operation, currentImageName,
                            imageFileForChecking, bitmap, downloadProgressDialog);
                    downloadImageTask.execute();
                } else {
                    downloadImageTaskCancelled = false;
                }
            }
        };

    }

    private void downloadImage(String currentImageUrl, SimpleTarget<Bitmap> target) {
        //We have permission, so we can download the image
        Glide.with(activityWeakReference.get()).load(currentImageUrl).asBitmap().into(target);
    }

    private void setWallpaper(File imageFile) {
        Intent setAsIntent = new Intent(activityWeakReference.get(), WallpaperManagerActivity.class);
        Uri imageUri = Uri.fromFile(imageFile);
        setAsIntent.setDataAndType(imageUri, "image/*");
        setAsIntent.putExtra("mimeType", "image/*");
        activityWeakReference.get().startActivity(setAsIntent);
    }

    public enum Operation {
        DOWNLOAD, SET_AS_WALLPAPER
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("AsimTag", "SingleImageFragment onDetach() called, variables nulled");
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
        Log.d("AsimTag", "SingleImageFragment onAttachFragment() called");
    }

    private static class RemoveFavoriteTask extends Thread {

        DatabaseReference mFavoritesReference;
        String mCurrentImageName;

        RemoveFavoriteTask(DatabaseReference favoritesReference, String currentImageName){
            mFavoritesReference = favoritesReference;
            mCurrentImageName = currentImageName;
        }

        @Override
        public void run() {
            mFavoritesReference.child(mCurrentImageName).removeValue();
        }
    }

    private static class AddFavoriteTask extends Thread {

        DatabaseReference mFavoritesReference;
        GridItem mCurrentItem;

        AddFavoriteTask(DatabaseReference favoritesReference, GridItem currentItem){
            mFavoritesReference = favoritesReference;
            mCurrentItem = currentItem;
        }

        @Override
        public void run() {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy-hhmmss", Locale.US);
            String timestamp = simpleDateFormat.format(new Date());
            //If image doesn't exist in database, add it
            if(mFavoritesReference.child(mCurrentItem.getName()) != null){
                mFavoritesReference.child(mCurrentItem.getName()).setValue(mCurrentItem, timestamp);
            }
        }
    }
}