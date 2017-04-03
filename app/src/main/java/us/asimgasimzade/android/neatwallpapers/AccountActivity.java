package us.asimgasimzade.android.neatwallpapers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.R.id.message;
import static us.asimgasimzade.android.neatwallpapers.utils.Utils.showToast;

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
    Button removeAccountButton;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseAuth.AuthStateListener authListener;


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
        removeAccountButton = (Button) findViewById(R.id.account_remove_account_button);


        //Getting FirebaseAuth object instance
        auth = FirebaseAuth.getInstance();
        //Getting FirebaseUser for current user
        user = auth.getCurrentUser();
        // this listener will be called when there is change in firebase user session
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(AccountActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };


        //Dynamically change logOut and changeProfilePicture button textColors
        // when focused and pressed
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_pressed}, // pressed
                new int[]{android.R.attr.state_focused}, // focused
                new int[]{}
        };
        int[] colors = new int[]{
                ContextCompat.getColor(this, R.color.white), // white
                ContextCompat.getColor(this, R.color.white), // white
                ContextCompat.getColor(this, R.color.colorAccent), // pink

        };
        ColorStateList list = new ColorStateList(states, colors);
        logOutButton.setTextColor(list);
        changeProfilePictureButton.setTextColor(list);


        //Log Out button
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
            }
        });

        //Remove Account button
        removeAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder removeAccountDialog =
                        new AlertDialog.Builder(AccountActivity.this, R.style.AppCompatAlertDialogStyle);
                removeAccountDialog.setMessage(R.string.account_remove_account_dialog_message)
                        .setTitle(R.string.account_remove_account_dialog_title)
                        .setPositiveButton(R.string.account_remove_dialog_positive_button,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (user != null) {
                                            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        showToast(getBaseContext(),
                                                                getString(R.string.profile_removed_success_message),
                                                                Toast.LENGTH_LONG);
                                                    } else {
                                                        showToast(getBaseContext(),
                                                                getString(R.string.profile_removed_fail_message),
                                                                Toast.LENGTH_LONG);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                })
                        .setNegativeButton(R.string.account_remove_dialog_negative_button, null)
                        .create()
                        .show();
            }
        });


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
   /* public Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
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
    }*/

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
}
