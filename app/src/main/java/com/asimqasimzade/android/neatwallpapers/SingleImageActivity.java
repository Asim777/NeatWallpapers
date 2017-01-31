package com.asimqasimzade.android.neatwallpapers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
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
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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


public class SingleImageActivity extends AppCompatActivity {
    private static final String LOG_TAG = SingleImageActivity.class.getSimpleName();
    private static final int REQUEST_ID_SET_AS_WALLPAPER = 100;
    Button favoriteButton;
    Button backButton;
    Button setAsWallpaperButton;
    Button downloadButton;
    String imageUrl;
    String imageName;
    String authorInfo;
    String imageLink;
    int imageNumber;
    SimpleTarget<Bitmap> target;
    File imageFile;
    File imageFileForChecking;
    FileOutputStream outputStream;
    enum Operation {
        DOWNLOAD, SET_AS_WALLPAPER
    }
    Cursor cursor;
    Operation operation;
    boolean imageIsFavorite;
    // Progress Dialog
    private ProgressDialog progressDialog;

    //Experimental scrolling
    ViewPager singleImageViewPager;
    SingleImageViewPagerAdapter singleImageViewPagerAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_image);

        //Loading image
        imageUrl = getIntent().getStringExtra("image");
        imageName = getIntent().getStringExtra("name");
        authorInfo = getIntent().getStringExtra("author");
        imageLink = getIntent().getStringExtra("link");
        imageNumber = getIntent().getIntExtra("number", 0);

        singleImageViewPagerAdapter = new SingleImageViewPagerAdapter(this);
        singleImageViewPager = (ViewPager) findViewById(R.id.single_image_viewpager);
        singleImageViewPager.setAdapter(singleImageViewPagerAdapter);
        // how many images to load into memory from the either side of current page
        singleImageViewPager.setOffscreenPageLimit(5);

        //We'll use this file to check if given image already exists on device and take corresponding
        //course of action depending on that
        //Specifying path to our app's directory
        File path = Environment.getExternalStoragePublicDirectory("NeatWallpapers");
        //Creating imageFile using path to our custom album
        imageFileForChecking = new File(path, "NEATWALLPAPERS_" + imageName + ".jpg");

/*        ImageView singleImageView = (ImageView) findViewById(R.id.single_image_view);
        Glide.with(this).load(imageUrl).into(singleImageView);*/

        //Setting author info
        TextView authorInfoTextView = (TextView) findViewById(R.id.author_info_text_view);
        authorInfoTextView.setText(String.format(getResources().getString(R.string.author_info), authorInfo));

        //Setting image link
        TextView imageLinkTextView = (TextView) findViewById(R.id.image_link_text_view);
        imageLinkTextView.setPaintFlags(imageLinkTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        imageLinkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openImageLinkIntent = new Intent(Intent.ACTION_VIEW);
                openImageLinkIntent.setData(Uri.parse(imageLink));
                startActivity(openImageLinkIntent);
            }
        });
        //-----------------------------------------------------------------------------------------
        //Back button
        //-----------------------------------------------------------------------------------------
        backButton = (Button) findViewById(R.id.single_image_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //When back button inside SingleImageActivity is clicked, we are finishing this
                //activity and automatically going back to the previous activity
                finish();
            }
        });


        //-----------------------------------------------------------------------------------------
        // Favorite button
        //-----------------------------------------------------------------------------------------

        favoriteButton = (Button) findViewById(R.id.single_image_favorite_button);
        new ImageIsFavoriteTask().execute();

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //When favorite button inside SingleImageActivity is clicked, we are adding this
                //image to Favorites database and changing background of a button
                new AddOrRemoveFavoriteAsyncTask().execute();
                //Sending back return intent to FavoritesFragment to update it's GridView with new data
                Intent databaseIsChangedIntent = new Intent();
                setResult(Activity.RESULT_OK, databaseIsChangedIntent);
            }
        });


        //-----------------------------------------------------------------------------------------
        // Set as wallpaper button
        //-----------------------------------------------------------------------------------------

        setAsWallpaperButton = (Button) findViewById(R.id.set_as_wallpaper_button);

        setAsWallpaperButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Change the current Wallpaper:
                //if image doesn't exist then download it first
                operation = Operation.SET_AS_WALLPAPER;
                if (!fileExists(imageFileForChecking)) {
                    Glide.with(getApplicationContext()).load(imageUrl).asBitmap().into(target);
                } else {
                    //if it exists, just set it as wallpaper
                    setWallpaper(imageFileForChecking);
                }
            }
        });


        //-----------------------------------------------------------------------------------------
        // Download button
        //-----------------------------------------------------------------------------------------

        downloadButton = (Button) findViewById(R.id.download_button);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Downloading image
                // if it already exists Toast message, saying that it does
                operation = Operation.DOWNLOAD;
                if (fileExists(imageFileForChecking)) {
                    Toast.makeText(getApplicationContext(), "Image already exists. Check your Gallery.", Toast.LENGTH_SHORT).show();
                } else {
                    //if it doesn't exist, download it
                    Glide.with(getApplicationContext()).load(imageUrl).asBitmap().into(target);
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
                        imageFile = new File(path, "NEATWALLPAPERS_" + imageName + ".jpg");

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

                                while(jumpTime < totalProgressTime) {
                                    try {
                                        sleep(100);
                                        //publishing progress
                                        //after that onProgressUpdate will be called
                                        publishProgress(jumpTime);
                                        jumpTime += 5;

                                    }  catch (InterruptedException e){
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
                        if(operation == Operation.DOWNLOAD) {
                            if (fileExists(imageFile)) {
                                Log.e(LOG_TAG, "Image successfully saved");
                                Toast.makeText(getApplicationContext(), "Image is successfully saved!",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e(LOG_TAG, "Problem while downloading image");
                                Toast.makeText(SingleImageActivity.this,
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

    }

    class SingleImageViewPagerAdapter extends PagerAdapter {

        Context mContext;
        LayoutInflater mInflater;

        public SingleImageViewPagerAdapter(Context context){
            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            //TODO: put search results from JSON here
            return 200;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            //Geting url of current selected image from ImagesDataClass using imageNumber from
            // intent and ViewPager page position
            String currentImageUrl = ImagesDataClass.imageslist.get(imageNumber+position).getImage();
            View itemView = mInflater.inflate(R.layout.single_image_viewpager_item, container, false);
            container.addView(itemView);
            final ImageView imageView = (ImageView) itemView.findViewById(R.id.single_image_view);
            Glide.with(mContext).load(currentImageUrl).into(imageView);
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }
    }

    protected void showProgressDialog(){
        progressDialog = new ProgressDialog(this);
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


    public class AddOrRemoveFavoriteAsyncTask extends  AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... voids) {

            FavoritesDBHelper dbHelper = new FavoritesDBHelper(SingleImageActivity.this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            // Define 'where' part of query
            String selection = FavoritesDBContract.FavoritesEntry.IMAGE_NAME + " = ?";

            if(imageIsFavorite){
                // Issue SQL statement
                db.delete(FavoritesDBContract.FavoritesEntry.TABLE_NAME, selection, new String[] {imageName});
                return null;
            } else {
                //Create content values for new database entry
                ContentValues values = new ContentValues();
                values.put(FavoritesDBContract.FavoritesEntry.IMAGE_NAME, imageName);
                values.put(FavoritesDBContract.FavoritesEntry.IMAGE_URL, imageUrl);

                // Insert the new row using our values
                db.insert(FavoritesDBContract.FavoritesEntry.TABLE_NAME, null, values);
                return null;
            }

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(!imageIsFavorite){
                Toast.makeText(SingleImageActivity.this, "Image is added to Favorites", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SingleImageActivity.this, "Image is removed from Favorites", Toast.LENGTH_SHORT).show();
            }
            new ImageIsFavoriteTask().execute();
        }
    }

    public class ImageIsFavoriteTask extends  AsyncTask<Void, Void, Void>  {
        @Override
        protected Void doInBackground(Void... voids) {
            //Check if this entry exists in database
            FavoritesDBHelper dbHelper = new FavoritesDBHelper(SingleImageActivity.this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String selectString = "SELECT * FROM " + FavoritesDBContract.FavoritesEntry.TABLE_NAME
                    + " WHERE " + FavoritesDBContract.FavoritesEntry.IMAGE_NAME + " =?";

            try {
                cursor = db.rawQuery(selectString, new String[] {imageName});
                imageIsFavorite = cursor.moveToFirst();
            } finally {
                cursor.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(imageIsFavorite){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    favoriteButton.setBackground(ContextCompat.getDrawable(SingleImageActivity.this, R.mipmap.ic_favorite_selected));
                } else {
                    //noinspection deprecation
                    favoriteButton.setBackgroundDrawable(ContextCompat.getDrawable(SingleImageActivity.this, R.mipmap.ic_favorite_selected));
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    favoriteButton.setBackground(ContextCompat.getDrawable(SingleImageActivity.this, R.mipmap.ic_favorite));
                } else {
                    //noinspection deprecation
                    favoriteButton.setBackgroundDrawable(ContextCompat.getDrawable(SingleImageActivity.this, R.mipmap.ic_favorite));
                }
            }
        }
    }

}
