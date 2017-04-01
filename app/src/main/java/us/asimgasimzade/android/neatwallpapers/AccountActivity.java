package us.asimgasimzade.android.neatwallpapers;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This activity displays account information of user and allows to edit it
 */

public class AccountActivity extends AppCompatActivity {
    ImageView profilePicture;
    TextView userNameTextView;
    TextView userEmailTextView;
    EditText userNameEditText;
    EditText userEmailEditText;
    Button logOutButton;
    Button changeProfilePictureButton;
    Button saveChangesButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.account_toolbar);
        setSupportActionBar(toolbar);

        //Enable up button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //Getting references to views
        profilePicture = (ImageView) findViewById(R.id.profile_picture_imageView);
        userNameEditText = (EditText) findViewById(R.id.account_name_editText);
        userEmailEditText = (EditText) findViewById(R.id.account_email_editText);
        userNameTextView = (TextView) findViewById(R.id.account_name_textView);
        userEmailTextView = (TextView) findViewById(R.id.account_email_textView);
        logOutButton = (Button) findViewById(R.id.account_logout_button);
        changeProfilePictureButton = (Button) findViewById(R.id.account_change_picture_button);
        saveChangesButton = (Button) findViewById(R.id.account_save_changes_button);

        int[][] states = new int[][] {
                new int[] { android.R.attr.state_pressed}, // pressed
                new int[] { android.R.attr.state_focused}, // focused
                new int[] {}
        };
        int[] colors = new int[] {
                ContextCompat.getColor(this, R.color.white), // white
                ContextCompat.getColor(this, R.color.white), // white
                ContextCompat.getColor(this, R.color.colorAccent), // pink

        };
        ColorStateList list = new ColorStateList(states, colors);
        logOutButton.setTextColor(list);
        changeProfilePictureButton.setTextColor(list);

        //Get user data from FireBase database and populate views


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.account_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(AccountActivity.this, SettingsActivity.class));
                return true;
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            default:
                return super.onContextItemSelected(item);
        }
    }


    //TODO: Move it to Utils.java if it works
    public Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
        int targetWidth = 50;
        int targetHeight = 50;
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                targetHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2,
                ((float) targetHeight - 1) / 2,
                (Math.min(((float) targetWidth),
                        ((float) targetHeight)) / 2),
                Path.Direction.CCW);

        canvas.clipPath(path);
        Bitmap sourceBitmap = scaleBitmapImage;
        canvas.drawBitmap(sourceBitmap,
                new Rect(0, 0, sourceBitmap.getWidth(),
                        sourceBitmap.getHeight()),
                new Rect(0, 0, targetWidth, targetHeight), null);
        return targetBitmap;
    }
}
