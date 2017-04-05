package us.asimgasimzade.android.neatwallpapers;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;
import java.io.InputStream;

import us.asimgasimzade.android.neatwallpapers.data.User;
import us.asimgasimzade.android.neatwallpapers.utils.Utils;

import static us.asimgasimzade.android.neatwallpapers.utils.Utils.showToast;


/**
 * This activity displays account information of user and allows to edit it
 */

public class AccountActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_INTENT_KEY = 109;
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
    FirebaseUser authUser;
    FirebaseAuth.AuthStateListener authListener;
    String userId;
    DatabaseReference database;
    User currentUser;
    Bitmap profileImageBitmap;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.account_toolbar);
        setSupportActionBar(toolbar);

        //Enable up button
        final Drawable upArrow = ContextCompat.getDrawable(this, R.mipmap.ic_up);
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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



        //Getting FirebaseAuth object instance
        auth = FirebaseAuth.getInstance();
        //Getting FirebaseUser for current user
        authUser = auth.getCurrentUser();

        //Getting userId from authUser
        userId = authUser != null ? authUser.getUid() : null;
        //Get user data from FireBase database
        database = FirebaseDatabase.getInstance().getReference();






        // This event listener is triggered whenever there is a change in user profile data
        database.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                updateUI(currentUser);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Log.d("AsimTag", "Failed to read value.", databaseError.toException());
            }
        });

        // This listener will be called when there is change in FireBase user session
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser authUser = firebaseAuth.getCurrentUser();
                if (authUser == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(AccountActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };


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
                                        //Removing user data from FireBase database, if removing user
                                        // from FireBase authentication fails, we'll put this user
                                        // node back to database
                                        removeUser();
                                    }
                                })
                        .setNegativeButton(R.string.account_remove_dialog_negative_button, null)
                        .create()
                        .show();
            }
        });

        //Change picture button
        changeProfilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Starting image chooser
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");
                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");
                Intent chooserIntent = Intent.createChooser(pickIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
                startActivityForResult(chooserIntent, PICK_IMAGE_INTENT_KEY);
            }
        });


        //Save changes button
        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Getting entered text
                String newName = userNameEditText.getText().toString();
                String newEmail = userEmailEditText.getText().toString();

                //Updating data if entries are not empty
                if (!newEmail.isEmpty()) {
                    database.child("users").child(userId).child("email").setValue(newEmail);
                }
                if (!newName.isEmpty()) {
                    database.child("users").child(userId).child("fullname").setValue(newName);
                }
                //Toasting success message and clearing editTexts
                if (!(newEmail.isEmpty() & newName.isEmpty())) {
                    showToast(AccountActivity.this, "Your account data successfully changed", Toast.LENGTH_SHORT);
                }
                userEmailEditText.setText("");
                userNameEditText.setText("");

            }
        });

    }

    private void removeUser() {
        database.child("users").child(userId).removeValue();
        if (authUser != null) {
            authUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {

                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        showToast(AccountActivity.this.getBaseContext(), AccountActivity.this.getString
                                (R.string.profile_removed_success_message), Toast.LENGTH_LONG);

                    } else {
                        showToast(AccountActivity.this.getBaseContext(), AccountActivity.this.getString
                                (R.string.profile_removed_fail_message), Toast.LENGTH_LONG);
                        database.child("users").child(userId).setValue(currentUser);
                    }
                }
            });
        }
    }

    private void updateUI(User mCurrentUser) {
        if (mCurrentUser == null) return;
        //Update user full name
        userNameTextView.setText(mCurrentUser.getFullname());
        //Update user email
        userEmailTextView.setText(mCurrentUser.getEmail());
        //Update user profile picture
        String profileImageString = currentUser.getProfilePicture();
        Uri profileImageUri = Uri.parse(profileImageString);
        //Loading image with glide
        Glide.with(this.getApplicationContext()).
                load(profileImageUri).
                asBitmap().
                into(new SimpleTarget<Bitmap>(500,500) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        profileImageBitmap = resource;
                        Drawable circledProfileDrawable = Utils.getCircleImage(AccountActivity.this, profileImageBitmap);
                        profilePicture.setImageDrawable(circledProfileDrawable);
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //If user has chosen image, get this image as Bitmap and set to profilePicture imageView
            case PICK_IMAGE_INTENT_KEY:
                if (resultCode == RESULT_OK) {
                    try {
                        final Uri profileImageUri = data.getData();
                        final InputStream profileImageStream = getContentResolver().openInputStream(profileImageUri);
                        final Bitmap selectedProfileImage = BitmapFactory.decodeStream(profileImageStream);
                        Drawable circledDrawable = Utils.getCircleImage(this, selectedProfileImage);
                        profilePicture.setImageDrawable(circledDrawable);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        showToast(this, "There was a problem with picking image. Please try again", Toast.LENGTH_LONG);
                    }
                }
        }
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

    //Making emailAutoCompleteTextView to focus on passwordEditText when ACTION_NEXT pressed
    //And passwordEditText to click signInButton when ACTION_DONE pressed
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            if (userEmailEditText.hasFocus()) {
                // sends focus to passwordEditText field if user pressed "Next"
                saveChangesButton.performClick();
                return true;
            }
        }
        return false;
    }
}
