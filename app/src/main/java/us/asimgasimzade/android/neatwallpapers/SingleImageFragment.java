package us.asimgasimzade.android.neatwallpapers;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import java.io.File;
import java.util.ArrayList;

import us.asimgasimzade.android.neatwallpapers.data.GridItem;
import us.asimgasimzade.android.neatwallpapers.data.ImagesDataClass;
import us.asimgasimzade.android.neatwallpapers.tasks.AddOrRemoveFavoriteAsyncTask;
import us.asimgasimzade.android.neatwallpapers.tasks.DownloadImageAsyncTask;
import us.asimgasimzade.android.neatwallpapers.tasks.ImageIsFavoriteTask;
import us.asimgasimzade.android.neatwallpapers.utils.IsImageFavoriteResponseInterface;

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

public class SingleImageFragment extends Fragment implements IsImageFavoriteResponseInterface {

    private static final int REQUEST_PERMISSION_SETTING = 43;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10;
    private static final int FULL_IMAGE_REQUEST_CODE = 105;
    int currentPosition;

    boolean imageIsFavorite;
    boolean fragmentKilled;
    String currentImageUrl;
    String currentAuthorInfo;
    String currentImageLink;
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
                currentImagesList = ImagesDataClass.imageslist;
            }
        }

        if (currentImagesList.isEmpty()) {
            fragmentKilled = true;
            getActivity().finish();
        } else {
            getImageAttributes();
        }
    }

    public void getImageAttributes() {
        currentItem = currentImagesList.get(currentPosition);
        currentImageUrl = currentItem.getImage();
        currentAuthorInfo = currentItem.getAuthor();
        currentImageLink = currentItem.getLink();
        currentImageName = currentItem.getName();
    }

    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (!fragmentKilled) {
            // Inflate the layout for this fragment
            rootView = inflater.inflate(R.layout.fragment_single_image, container, false);

            //Downloading and setting image
            ImageView imageView = (ImageView) rootView.findViewById(R.id.single_image_view);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent fullImageIntent = new Intent(getActivity(), FullImageActivity.class).
                            putExtra("url", currentImageUrl).
                            putExtra("author", currentAuthorInfo).
                            putExtra("link", currentImageLink).
                            putExtra("name", currentImageName).
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
            authorInfoTextView.setText(String.format(getResources().getString(R.string.author_info), currentAuthorInfo));

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
            new ImageIsFavoriteTask(this, imageIsFavorite, getActivity(), currentImageName).execute();

            favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //When favorite button inside SingleImageActivity is clicked, we are adding this
                    //image to Favorites database and changing background of a button
                    new AddOrRemoveFavoriteAsyncTask(fragmentInstance, imageIsFavorite, getActivity(),
                            currentImageName, currentImageUrl, currentAuthorInfo, currentImageLink).execute();
                    //Setting delegate back to this instance of SingleImageFragment
                    sendFavoritesOKResult(Activity.RESULT_OK);
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


        }
        return rootView;
    }

    private void createProgressDialog() {
        //Creating progress dialog
        downloadProgressDialog = new ProgressDialog(activityInstance, R.style.AppCompatAlertDialogStyle);
        downloadProgressDialog.setMessage(activityInstance.getString(R.string.message_downloading_image));
        downloadProgressDialog.setCancelable(true);
        downloadProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                //Cancel the task if user presses back while mDownloadProgressDialog
                // is shown
                if(downloadImageTask != null) {
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
                        if(downloadImageTask != null) {
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

    public void sendFavoritesOKResult(int result) {
        //Sending back return intent to FavoritesFragment to update it's GridView with new data
        Intent databaseIsChangedIntent = new Intent();
        getActivity().setResult(result, databaseIsChangedIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean showRationale = shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
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
                        showMessageOKCancel(activityInstance, getString(R.string.permission_message),
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

        if (downloadProgressDialog != null) {
            downloadProgressDialog.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        new ImageIsFavoriteTask(this, imageIsFavorite, getActivity(), currentImageName).execute();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == FULL_IMAGE_REQUEST_CODE) {
            sendFavoritesOKResult(Activity.RESULT_OK);
        }
    }

    @Override
    public void updateImageIsFavorite(boolean response) {
        imageIsFavorite = response;

        if (imageIsFavorite) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                favoriteButton.setBackground(ContextCompat.getDrawable(getActivity(), R.mipmap.ic_favorite_selected));
            } else {
                //noinspection deprecation
                favoriteButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.ic_favorite_selected));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                favoriteButton.setBackground(ContextCompat.getDrawable(getActivity(), R.mipmap.ic_favorite));
            } else {
                //noinspection deprecation
                favoriteButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.ic_favorite));
            }
        }
    }

    public SimpleTarget<Bitmap> createTarget() {

        return new SimpleTarget<Bitmap>() {

            @Override
            public void onResourceReady(final Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                if(!downloadImageTaskCancelled){
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

