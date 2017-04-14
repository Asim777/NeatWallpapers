package us.asimgasimzade.android.neatwallpapers;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import us.asimgasimzade.android.neatwallpapers.data.GridItem;
import us.asimgasimzade.android.neatwallpapers.data.ImagesDataClass;
import us.asimgasimzade.android.neatwallpapers.tasks.DownloadImageAsyncTask;

import static us.asimgasimzade.android.neatwallpapers.utils.Utils.checkNetworkConnection;
import static us.asimgasimzade.android.neatwallpapers.utils.Utils.downloadImage;
import static us.asimgasimzade.android.neatwallpapers.utils.Utils.downloadImageIfPermitted;
import static us.asimgasimzade.android.neatwallpapers.utils.Utils.fileExists;
import static us.asimgasimzade.android.neatwallpapers.utils.Utils.setWallpaper;
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
    Button favoriteButton;
    Button setAsWallpaperButton;
    Button downloadButton;
    Button backButton;
    SimpleTarget<Bitmap> target;
    GridItem currentItem;
    ArrayList<GridItem> currentImagesList;
    View rootView;
    Operation operation;
    SingleImageFragment fragmentInstance;
    Activity activityInstance;
    //Downloading progress dialog
    ProgressDialog downloadProgressDialog;
    // Loading animation progress bar
    private ProgressBar loadingAnimationProgressBar;
    DownloadImageAsyncTask downloadImageTask;
    private boolean downloadImageTaskCancelled;
    SharedPreferences sharedPreferences;
    private DatabaseReference favoritesReference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentInstance = this;
        activityInstance = getActivity();

        //Getting url of current selected image from ImagesDataClass using imageNumber from
        // intent and ViewPager page position
        Bundle bundle = getArguments();
        currentPosition = bundle.getInt("image_number");
        source = bundle.getString("image_source");
        url = bundle.getString("current_url");
        //Get instance of SharedPreferences
        sharedPreferences = activityInstance.getSharedPreferences("SINGLE_IMAGE_SP", Context.MODE_PRIVATE);

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

    public void getImageAttributes() {
        currentItem = currentImagesList.get(currentPosition);
        currentImageName = currentItem.getName();
        currentImageAuthor = currentItem.getAuthor();
        currentImageUrl = currentItem.getImage();
        currentImageThumbnail = currentItem.getThumbnail();
        currentImageLink = currentItem.getLink();
    }

    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

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

        getImageAttributes();

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_single_image, container, false);

        //Downloading and setting image
        ImageView imageView = (ImageView) rootView.findViewById(R.id.single_image_view);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fullImageIntent = new Intent(getActivity(), FullImageActivity.class).
                        putExtra("url", currentImageUrl).
                        putExtra("author", currentImageAuthor).
                        putExtra("link", currentImageLink).
                        putExtra("name", currentImageName).
                        putExtra("thumbnail", currentImageThumbnail).
                        putExtra("image_is_favorite", imageIsFavorite);
                startActivityForResult(fullImageIntent, FULL_IMAGE_REQUEST_CODE);
            }
        });

        loadingAnimationProgressBar = (ProgressBar) rootView.findViewById(R.id.loading_progress_bar);
        loadingAnimationProgressBar.setVisibility(View.VISIBLE);
        Glide.with(getActivity().getApplicationContext()).load(currentImageUrl).
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
        backButton = (Button) rootView.findViewById(R.id.single_image_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //When back button inside SingleImageActivity is clicked, we are initiating
                //back button pressed and automatically going back to the previous activity
                getActivity().onBackPressed();
            }
        });

        //-----------------------------------------------------------------------------------------
        // Favorite button
        //-----------------------------------------------------------------------------------------

        favoriteButton = (Button) rootView.findViewById(R.id.single_image_favorite_button);

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
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            favoritesReference.child(currentImageName).removeValue();
                        }
                    }).start();
                    updateImageIsFavorite(false);
                    showToast(getActivity(), getActivity().getResources().getString(R.string.image_removed_from_favorites_message),Toast.LENGTH_SHORT);
                } else {
                    //Create new favorite image and put into FireBase database
                    //Image is favorite, we are removing it from database
                    //We are doing it in parallel thread so that user sees toast message immediately.
                    // We are also changing favorite button background from here because addValueEventListener
                    // might take some time to update it. If it's not added successfully, favorite
                    //button will be changed back anyway, when onDataChange in listener is triggered
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy-hhmmss", Locale.US);
                            String timestamp = simpleDateFormat.format(new Date());
                            //If image doesn't exist in database, add it
                            if(favoritesReference.child(currentImageName) != null){
                                favoritesReference.child(currentImageName).setValue(currentItem, timestamp);
                            }
                        }
                    }).start();
                    updateImageIsFavorite(true);
                    showToast(getActivity(), getActivity().getResources().getString(R.string.image_added_to_favorites_message),Toast.LENGTH_SHORT);

                }
            }

        });

        //-----------------------------------------------------------------------------------------
        // Set as wallpaper button
        //-----------------------------------------------------------------------------------------

        setAsWallpaperButton = (Button) rootView.findViewById(R.id.set_as_wallpaper_button);

        setAsWallpaperButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Check if network is available
                if (checkNetworkConnection(activityInstance)) {
                    //Change the current Wallpaper, if image doesn't exist then download it first
                    operation = Operation.SET_AS_WALLPAPER;

                    if (!fileExists(imageFileForChecking)) {

                        //Creating target
                        target = createTarget();
                        //Creating progress dialog
                        createProgressDialog();
                        //Show downloading progress dialog
                        downloadProgressDialog.show();

                        downloadImageIfPermitted(activityInstance, fragmentInstance, currentImageUrl, target);
                    } else {
                        //if it exists, just set it as wallpaper
                        setWallpaper(activityInstance, imageFileForChecking);
                    }
                }
            }
        });


        //-----------------------------------------------------------------------------------------
        // Download button
        //-----------------------------------------------------------------------------------------

        downloadButton = (Button) rootView.findViewById(R.id.download_button);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Check if network is available
                if (checkNetworkConnection(activityInstance)) {


                    //Downloading image
                    // if it already exists Toast message, saying that it does
                    operation = Operation.DOWNLOAD;

                    if (fileExists(imageFileForChecking)) {
                        showToast(getActivity().getApplicationContext(),
                                getString(R.string.image_already_exists_message), Toast.LENGTH_SHORT);
                    } else {
                        //Creating target
                        target = createTarget();
                        //Create Progress dialog
                        createProgressDialog();
                        //Show downloading progress dialog
                        downloadProgressDialog.show();
                        //If it doesn't exist, download it, but first check if we have permission to do it
                        downloadImageIfPermitted(activityInstance, fragmentInstance, currentImageUrl, target);
                    }
                }
            }
        });


        favoritesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Find out if this image exists in favorites node
                imageIsFavorite = dataSnapshot.child(currentImageName).exists();
                updateImageIsFavorite(imageIsFavorite);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return rootView;
    }

    private void createProgressDialog() {
        //Creating progress dialog
        downloadProgressDialog = new ProgressDialog(activityInstance, R.style.AppCompatAlertDialogStyle);
        downloadProgressDialog.setTitle(activityInstance.getString(R.string.title_downloading_image));
        downloadProgressDialog.setMessage(activityInstance.getString(R.string.message_downloading_image));
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
                    showToast(getActivity().getApplicationContext(), getString(R.string.permission_granted_message),
                            Toast.LENGTH_SHORT);
                    downloadImage(activityInstance, currentImageUrl, target);
                } else {
                    //Permission is not granted, but did the user also check "Never ask again"?
                    if (!showRationale) {
                        // user denied permission and also checked "Never ask again"
                        showMessageOKCancel(activityInstance, getString(R.string.download_permission_message),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
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

    public void updateImageIsFavorite(boolean response) {
        imageIsFavorite = response;

        if (imageIsFavorite) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                favoriteButton.setBackground(ContextCompat.getDrawable(activityInstance, R.mipmap.ic_favorite_selected));
            } else {
                //noinspection deprecation
                favoriteButton.setBackgroundDrawable(ContextCompat.getDrawable(activityInstance, R.mipmap.ic_favorite_selected));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                favoriteButton.setBackground(ContextCompat.getDrawable(activityInstance, R.mipmap.ic_favorite));
            } else {
                //noinspection deprecation
                favoriteButton.setBackgroundDrawable(ContextCompat.getDrawable(activityInstance, R.mipmap.ic_favorite));
            }
        }
    }

    public SimpleTarget<Bitmap> createTarget() {

        return new SimpleTarget<Bitmap>() {

            @Override
            public void onResourceReady(final Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                if (!downloadImageTaskCancelled) {
                    downloadImageTask = new DownloadImageAsyncTask(activityInstance, operation, currentImageName,
                            imageFileForChecking, bitmap, downloadProgressDialog);
                    downloadImageTask.execute();
                } else {
                    downloadImageTaskCancelled = false;
                }
            }
        };

    }

    public enum Operation {
        DOWNLOAD, SET_AS_WALLPAPER
    }

}

