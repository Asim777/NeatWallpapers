package us.asimgasimzade.android.neatwallpapers;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import us.asimgasimzade.android.neatwallpapers.data.GridItem;
import us.asimgasimzade.android.neatwallpapers.data.ImagesDataClass;
import us.asimgasimzade.android.neatwallpapers.favorites_db.FavoritesDBContract;
import us.asimgasimzade.android.neatwallpapers.favorites_db.FavoritesDBHelper;
import us.asimgasimzade.android.neatwallpapers.tasks.AddOrRemoveFavoriteAsyncTask;
import us.asimgasimzade.android.neatwallpapers.tasks.ImageIsFavoriteTask;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;
import static java.lang.Thread.sleep;

/**
 * Fragment that holds single image page in SingleImage view pager
 */

public class SingleImageFragment extends Fragment implements IsImageFavoriteAsyncResponse {

    private static final int REQUEST_ID_SET_AS_WALLPAPER = 100;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10;
    private static final int REQUEST_PERMISSION_SETTING = 43;
    Button favoriteButton;
    Button setAsWallpaperButton;
    Button downloadButton;
    Button backButton;
    SimpleTarget<Bitmap> target;
    File imageFile;
    File imageFileForChecking;
    FileOutputStream outputStream;
    GridItem currentItem;
    ArrayList<GridItem> currentImagesList;
    String currentImageUrl;
    String currentAuthorInfo;
    String currentImageLink;
    String currentImageName;
    String source;
    String url;
    boolean fragmentKilled;
    int currentPosition;
    boolean directoryNotCreated;
    View rootView;
    Operation operation;
    boolean imageIsFavorite;
    // Progress Dialog
    private ProgressDialog progressDialog;
    SingleImageFragment fragmentInstance;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentInstance = this;

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
                            putExtra("url", currentImageUrl);
                    startActivity(fullImageIntent);
                }
            });
            Glide.with(getActivity().getApplicationContext()).load(currentImageUrl).into(imageView);

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
                    //When back button inside SingleImageActivity is clicked, we are finishing this
                    //activity and automatically going back to the previous activity
                    getActivity().finish();
                }
            });

            //-----------------------------------------------------------------------------------------
            // Favorite button
            //-----------------------------------------------------------------------------------------

            favoriteButton = (Button) rootView.findViewById(R.id.single_image_favorite_button);
            new ImageIsFavoriteTask(this, imageIsFavorite, getActivity(), currentImageName, favoriteButton).execute();

            favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //When favorite button inside SingleImageActivity is clicked, we are adding this
                    //image to Favorites database and changing background of a button
                    new AddOrRemoveFavoriteAsyncTask(fragmentInstance, imageIsFavorite, getActivity(),
                            currentImageName, favoriteButton).execute();
                    //Setting delegate back to this instance of SingleImageFragment

                    //Sending back return intent to FavoritesFragment to update it's GridView with new data
                    Intent databaseIsChangedIntent = new Intent();
                    getActivity().setResult(Activity.RESULT_OK, databaseIsChangedIntent);
                }
            });

            //-----------------------------------------------------------------------------------------
            // Set as wallpaper button
            //-----------------------------------------------------------------------------------------

            setAsWallpaperButton = (Button) rootView.findViewById(R.id.set_as_wallpaper_button);

            setAsWallpaperButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Change the current Wallpaper:
                    //if image doesn't exist then download it first
                    operation = Operation.SET_AS_WALLPAPER;
                    if (!fileExists(imageFileForChecking)) {
                        downloadImageIfPermitted();
                    } else {
                        //if it exists, just set it as wallpaper
                        setWallpaper(imageFileForChecking);
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
                    //Downloading image
                    // if it already exists Toast message, saying that it does
                    operation = Operation.DOWNLOAD;
                    if (fileExists(imageFileForChecking)) {
                        Toast.makeText(getActivity().getApplicationContext(),
                                R.string.image_already_exists_message, Toast.LENGTH_SHORT).show();
                    } else {
                        //If it doesn't exist, download it, but first check if we have permission to do it
                        downloadImageIfPermitted();
                    }
                }
            });


            target = new SimpleTarget<Bitmap>() {

                @Override
                public void onResourceReady(final Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {

                    new AsyncTask<Void, Integer, Boolean>() {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            showProgressDialog();
                        }

                        @Override
                        protected Boolean doInBackground(Void... voids) {

                            final int totalProgressTime = 100;
                            //Specifying path to our app's directory
                            File path = Environment.getExternalStoragePublicDirectory("NeatWallpapers");
                            //Creating imageFile using path to our custom album
                            imageFile = new File(path, "NEATWALLPAPERS_" + currentImageName + ".jpg");

                            //Creating our custom album directory, if it's not created, logging error message
                            if (!path.mkdirs()) {
                                directoryNotCreated = true;
                            }

                            //We are checking if there is ExternalStorage mounted on device and is it
                            //readable
                            if (isExternalStorageWritable()) {
                                int jumpTime = 5;
                                try {
                                    outputStream = new FileOutputStream(imageFile);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                                    while (jumpTime < totalProgressTime) {
                                        try {
                                            sleep(70);
                                            //publishing progress
                                            //after that onProgressUpdate will be called
                                            publishProgress(jumpTime);
                                            jumpTime += 5;

                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    try {
                                        if (outputStream != null) {
                                            outputStream.flush();
                                            outputStream.close();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                // Tell the media scanner about the new file so that it is
                                // immediately available to the user.
                                MediaScannerConnection.scanFile(getActivity().getApplicationContext(),
                                        new String[]{imageFile.getAbsolutePath()},
                                        null,
                                        new MediaScannerConnection.OnScanCompletedListener() {
                                            public void onScanCompleted(String path, Uri uri) {

                                            }
                                        }
                                );
                            }
                            return null;
                        }

                        @Override
                        protected void onProgressUpdate(Integer... values) {
                            super.onProgressUpdate(values);
                            //Setting progress value
                            /*progressDialog.setMax(100);*/
                            progressDialog.setProgress(values[0]);
                        }

                        @Override
                        protected void onPostExecute(Boolean aBoolean) {
                            //Dismiss the progress dialog
                            if (progressDialog != null) {
                                progressDialog.dismiss();
                            }

                            if (operation == Operation.DOWNLOAD) {
                                if (fileExists(imageFile)) {
                                    Toast.makeText(getActivity().getApplicationContext(), R.string.image_successfully_saved_message,
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(),
                                            R.string.problem_downloading_image_message,
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                //Checking the result and giving feedback to user about success
                                if (fileExists(imageFile)) {
                                    setWallpaper(imageFile);
                                    Toast.makeText(getActivity().getApplicationContext(),
                                            R.string.setting_wallpaper_message, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity().getApplicationContext(),
                                            R.string.problem_while_setting_wallpaper_message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }.execute();
                }
            };
        }
        return rootView;
    }

    private void downloadImageIfPermitted() {
        //Checking if permission to WRITE_EXTERNAL_STORAGE is granted by user
        if (isPermissionWriteToExternalStorageGranted()) {
            //If it's granted, just download the image
            downloadImage();
        } else {
            //If it's not granted, request it
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    private void downloadImage() {
        //We have permission, so we can download the image
        Glide.with(getActivity().getApplicationContext()).load(currentImageUrl).asBitmap().into(target);
    }

    private boolean isPermissionWriteToExternalStorageGranted() {
        return (checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED);
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
                    Toast.makeText(getActivity().getApplicationContext(), R.string.permission_granted_message,
                            Toast.LENGTH_SHORT).show();
                    downloadImage();
                } else {
                    //Permission is not granted, but did the user also check "Never ask again"?
                    if (!showRationale) {
                        // user denied permission and also checked "Never ask again"
                        showMessageOKCancel(getString(R.string.permission_message),
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

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setPositiveButton("Go to settings", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    protected void showProgressDialog() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.message_downloading_image));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.setProgress(0);
        progressDialog.show();
    }

    private void setWallpaper(File imageFile) {
        Intent setAsIntent = new Intent(Intent.ACTION_ATTACH_DATA);
        Uri imageUri = Uri.fromFile(imageFile);
        setAsIntent.setDataAndType(imageUri, "image/*");
        setAsIntent.putExtra("jpg", "image/*");
        startActivityForResult(Intent.createChooser(setAsIntent, getString(R.string.set_as)), REQUEST_ID_SET_AS_WALLPAPER);
    }

    /**
     * *This method checks if downloading image was successful so we can show according feedback to
     * the user
     * Returns boolean - true if successful and false if unsuccessful
     */
    private boolean fileExists(File image) {
        boolean result;
        result = image.exists() && image.isFile();
        return result;
    }

    /**
     * This method checks if there is External Storage currently mounted in device. It returns boolean
     *
     * @return boolean, true if External Storage is mounted, false if not
     */
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }


    private enum Operation {
        DOWNLOAD, SET_AS_WALLPAPER;
    }

    @Override
    public void updateImageIsFavorite(boolean response) {
        imageIsFavorite = response;
    }
}

