package com.asimqasimzade.android.neatwallpapers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.asimqasimzade.android.neatwallpapers.Data.ImagesDataClass;
import com.asimqasimzade.android.neatwallpapers.FavoritesDB.FavoritesDBContract;
import com.asimqasimzade.android.neatwallpapers.FavoritesDB.FavoritesDBHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static java.lang.Thread.sleep;

/**
 * Fragment that holds single image page in SingleImage view pager
 */

public class SingleImageFragment extends Fragment {

    Button favoriteButton;
    Button setAsWallpaperButton;
    Button downloadButton;
    Button backButton;
    SimpleTarget<Bitmap> target;
    File imageFile;
    File imageFileForChecking;
    FileOutputStream outputStream;
    String currentImageUrl;
    String currentAuthorInfo;
    String currentImageLink;
    String currentImageName;
    String source;
    int currentPosition;
    // Progress Dialog
    private ProgressDialog progressDialog;
    View rootView;
    Cursor cursor;

    public enum Operation {
        DOWNLOAD, SET_AS_WALLPAPER
    }

    Operation operation;
    boolean imageIsFavorite;

    private static final String LOG_TAG = "asim" /*SingleImageFragment.class.getSimpleName()*/;
    private static final int REQUEST_ID_SET_AS_WALLPAPER = 100;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Getting url of current selected image from ImagesDataClass using imageNumber from
        // intent and ViewPager page position
        Bundle bundle = getArguments();
        currentPosition = bundle.getInt("image_number");
        source = bundle.getString("image_source");
        //We need to make this check so that ViewPager doesn't create next Fragment with
        // currentPosition = 600 when we enter 599th, because when it does, there is no 600th
        // element in ImageDataClass.imagelist and it causes IndexOutOfBoundsException. That's why,
        // when we are at second to last page, and swipe write, activity closes. And we can't enter
        // the last item from the list either.

        switch (source) {
            case "popular": {
                currentImageUrl = ImagesDataClass.popularImagesList.get(currentPosition).getImage();
                currentAuthorInfo = ImagesDataClass.popularImagesList.get(currentPosition).getAuthor();
                currentImageLink = ImagesDataClass.popularImagesList.get(currentPosition).getLink();
                currentImageName = ImagesDataClass.popularImagesList.get(currentPosition).getName();
            }
            break;
            case "recent": {
                currentImageUrl = ImagesDataClass.recentImagesList.get(currentPosition).getImage();
                currentAuthorInfo = ImagesDataClass.recentImagesList.get(currentPosition).getAuthor();
                currentImageLink = ImagesDataClass.recentImagesList.get(currentPosition).getLink();
                currentImageName = ImagesDataClass.recentImagesList.get(currentPosition).getName();
            }
            break;
            case "favorites": {
                currentImageUrl = ImagesDataClass.favoriteImagesList.get(currentPosition).getImage();
                currentAuthorInfo = ImagesDataClass.favoriteImagesList.get(currentPosition).getAuthor();
                currentImageLink = ImagesDataClass.favoriteImagesList.get(currentPosition).getLink();
                currentImageName = ImagesDataClass.favoriteImagesList.get(currentPosition).getName();
            }
            break;
            case "default": {
                currentImageUrl = ImagesDataClass.imageslist.get(currentPosition).getImage();
                currentAuthorInfo = ImagesDataClass.imageslist.get(currentPosition).getAuthor();
                currentImageLink = ImagesDataClass.imageslist.get(currentPosition).getLink();
                currentImageName = ImagesDataClass.imageslist.get(currentPosition).getName();
            }
        }

    }

    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {



            // Inflate the layout for this fragment
            rootView = inflater.inflate(R.layout.fragment_single_image, container, false);

            //Downloading and setting image
            ImageView imageView = (ImageView) rootView.findViewById(R.id.single_image_view);
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
                    openImageLinkIntent.setData(Uri.parse(currentImageLink));
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
            new SingleImageFragment.ImageIsFavoriteTask().execute();

            favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //When favorite button inside SingleImageActivity is clicked, we are adding this
                    //image to Favorites database and changing background of a button
                    new SingleImageFragment.AddOrRemoveFavoriteAsyncTask().execute();
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
                        Glide.with(getActivity().getApplicationContext()).load(currentImageUrl).asBitmap().into(target);
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
                        Toast.makeText(getActivity().getApplicationContext(), "Image already exists. Check your Gallery.", Toast.LENGTH_SHORT).show();
                    } else {
                        //if it doesn't exist, download it
                        Glide.with(getActivity().getApplicationContext()).load(currentImageUrl).asBitmap().into(target);
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
                                Log.e(LOG_TAG, "Directory not created");
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
                                            sleep(100);
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
                                        outputStream.flush();
                                        outputStream.close();
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
                        protected void onProgressUpdate(Integer... values) {
                            super.onProgressUpdate(values);
                            //Setting progress value
                            /*progressDialog.setMax(100);*/
                            progressDialog.setProgress(values[0]);
                        }

                        @Override
                        protected void onPostExecute(Boolean aBoolean) {
                            //Dismiss the progress dialog
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }

                            //Checking the result and giving feedback to user about success
                            if (operation == Operation.DOWNLOAD) {
                                if (fileExists(imageFile)) {
                                    Log.e(LOG_TAG, "Image successfully saved");
                                    Toast.makeText(getActivity().getApplicationContext(), "Image is successfully saved!",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e(LOG_TAG, "Problem while downloading image");
                                    Toast.makeText(getActivity(),
                                            "Problem while downloading image, please try again",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                //Checking the result and giving feedback to user about success
                                if (fileExists(imageFile)) {
                                    setWallpaper(imageFile);
                                    Log.e(LOG_TAG, "Wallpaper Set successfully");
                                } else {
                                    Log.e(LOG_TAG, "Problem while setting wallpaper");
                                }
                            }
                        }
                    }.execute();
                }
            };

        return rootView;
    }

    class ImageIsFavoriteTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            FavoritesDBHelper dbHelper = new FavoritesDBHelper(getActivity());
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String selectString = "SELECT * FROM " + FavoritesDBContract.FavoritesEntry.TABLE_NAME
                    + " WHERE " + FavoritesDBContract.FavoritesEntry.IMAGE_NAME + " =?";

            try {
                cursor = db.rawQuery(selectString, new String[]{currentImageName});
                imageIsFavorite = cursor.moveToFirst();
            } finally {
                cursor.close();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
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
    }

    public class AddOrRemoveFavoriteAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            FavoritesDBHelper dbHelper = new FavoritesDBHelper(getActivity());
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            // Define 'where' part of query
            String selection = FavoritesDBContract.FavoritesEntry.IMAGE_NAME + " = ?";

            if (imageIsFavorite) {
                // Issue SQL statement
                db.delete(FavoritesDBContract.FavoritesEntry.TABLE_NAME, selection, new String[]{currentImageName});
                return null;
            } else {
                //Create content values for new database entry
                ContentValues values = new ContentValues();
                values.put(FavoritesDBContract.FavoritesEntry.IMAGE_NAME, currentImageName);
                values.put(FavoritesDBContract.FavoritesEntry.IMAGE_URL, currentImageUrl);
                values.put(FavoritesDBContract.FavoritesEntry.IMAGE_AUTHOR, currentAuthorInfo);
                values.put(FavoritesDBContract.FavoritesEntry.IMAGE_LINK, currentImageLink);

                // Insert the new row using our values
                db.insert(FavoritesDBContract.FavoritesEntry.TABLE_NAME, null, values);
                return null;
            }

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!imageIsFavorite) {
                Toast.makeText(getActivity(), "Image is added to Favorites", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Image is removed from Favorites", Toast.LENGTH_SHORT).show();
            }
            new ImageIsFavoriteTask().execute();
        }
    }

    protected void showProgressDialog() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Downloading Image");
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
}
