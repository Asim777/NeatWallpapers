package us.asimgasimzade.android.neatwallpapers;


import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import uk.co.senab.photoview.PhotoViewAttacher;
import us.asimgasimzade.android.neatwallpapers.tasks.AddOrRemoveFavoriteAsyncTask;
import us.asimgasimzade.android.neatwallpapers.tasks.ImageIsFavoriteTask;

/**
 * This activity opens when user clicks on image from SingleImageActivity to see the full
 * scrollable version of image
 */

public class FullImageActivityInterface extends AppCompatActivity implements IsImageFavoriteAsyncResponseInterface {

    boolean imageIsFavorite;
    String imageUrl;
    String imageAuthor;
    String imageLink;
    String imageName;
    MenuItem favoriteActionButton;
    Operation operation;

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
        Glide.with(getApplicationContext()).load(imageUrl).into(imageView);

        //Get image is favorite boolean from intent
        imageIsFavorite = getIntent().getBooleanExtra("image_is_favorite", false);

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

           /* case R.id.action_set:
                //Change the current Wallpaper:
                //if image doesn't exist then download it first
                operation = Operation.SET_AS_WALLPAPER;
                if (!fileExists(imageFileForChecking)) {
                    downloadImageIfPermitted();
                } else {
                    //if it exists, just set it as wallpaper
                    setWallpaper(imageFileForChecking);
                }
                return true;*/

            case R.id.action_download:
                return true;

            case R.id.action_favorite:

                new ImageIsFavoriteTask(this, imageIsFavorite, this, imageName).execute();
                //When favorite button inside FullImageActivityInterface is clicked, we are adding this
                //image to Favorites database and changing background of a button
                new AddOrRemoveFavoriteAsyncTask(FullImageActivityInterface.this, imageIsFavorite,
                        FullImageActivityInterface.this, imageName, imageUrl, imageAuthor, imageLink).execute();
                //Setting delegate back to this instance of SingleImageFragment
                //Sending back return intent to FullImageActivityInterface so that it calls callback
                // in FavoritesFragment to update it's GridView with new data
                Intent databaseIsChangedIntent = new Intent();
                FullImageActivityInterface.this.setResult(Activity.RESULT_OK, databaseIsChangedIntent);
                return true;

            default: {
                return super.onOptionsItemSelected(item);
            }
        }

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

    @Override
    protected void onResume() {
        super.onResume();

    }

    private enum Operation {
        DOWNLOAD, SET_AS_WALLPAPER
    }
}
