package us.asimgasimzade.android.neatwallpapers;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import us.asimgasimzade.android.neatwallpapers.data.User;
import us.asimgasimzade.android.neatwallpapers.utils.Utils;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.view.View.GONE;
import static us.asimgasimzade.android.neatwallpapers.utils.Utils.showToast;


/**
 * This activity displays account information of user and allows to edit it
 */

public class AccountActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_INTENT_KEY = 109;
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 5893;
    private static final int PERMISSION_SETTING_REQUEST_CODE = 9083;
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
    StorageReference storageRef;
    String userId;
    DatabaseReference database;
    User currentUser;
    Bitmap profileImageBitmap;
    Uri profilePictureDownloadUrl;
    String newName;
    String newEmail;
    Drawable newCircledDrawable;
    boolean emailChangeSuccessful;
    boolean nameChangeSuccessful;
    boolean profileImageChangeSuccessful;
    private Intent uploadIntent;
    private ValueEventListener eventListener;
    private DatabaseReference userReference;


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

        //We need this to make action bar visible after adding app:elevation="0dp" to it in xml
        findViewById(R.id.account_appBarLayout).bringToFront();

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
        //Get FireBase database instance reference
        database = FirebaseDatabase.getInstance().getReference();
        //Get FireBase storage instance
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        storageRef = storage.getReference();
        //Check if user is signed in with google account, if yes, we won't be showing editText
        // to edit email address
        isGoogleUser();

        // This event listener is triggered whenever there is a change in user profile data
        userReference = database.child("users").child(userId);
        eventListener = userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                updateUI(currentUser);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
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
                newName = userNameEditText.getText().toString();
                newEmail = userEmailEditText.getText().toString();

                //Updating data if entries are not empty
                if (!newEmail.isEmpty()) {
                    authUser.updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                database.child("users").child(userId).child("email").setValue(newEmail);
                                emailChangeSuccessful = true;
                            }
                        }
                    });
                }
                if (!newName.isEmpty()) {
                    database.child("users").child(userId).child("fullname").setValue(newName);
                    nameChangeSuccessful = true;
                }
                //Create reference to our profile image
                StorageReference profilePictureStorageReference = storageRef.child(userId).child("profile.jpg");
                // Get the data from profilePicture ImageView as bytes
                profilePicture.setDrawingCacheEnabled(true);
                profilePicture.buildDrawingCache();

                final Bitmap profilePictureBitmap = profilePicture.getDrawingCache();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                profilePictureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = profilePictureStorageReference.putBytes(data);
                profilePicture.setDrawingCacheEnabled(false);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast(AccountActivity.this.getApplicationContext(),
                                getString(R.string.profile_picture_upload_fail_message), Toast.LENGTH_SHORT);
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        //noinspection VisibleForTests
                        profilePictureDownloadUrl = taskSnapshot.getDownloadUrl();
                        if (profilePictureDownloadUrl != null) {
                            //Putting profile picture's url to database
                            database.child("users").child(userId).child("profilePicture").setValue(profilePictureDownloadUrl.toString());
                        }

                    }
                }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        //Toasting success message and clearing editTexts
                        if (emailChangeSuccessful | nameChangeSuccessful | profileImageChangeSuccessful) {
                            showToast(AccountActivity.this.getApplicationContext(),
                                    getString(R.string.account_data_changed_success_message), Toast.LENGTH_SHORT);
                        } else {
                            showToast(AccountActivity.this.getApplicationContext(),
                                    getString(R.string.account_data_changed_fail_message), Toast.LENGTH_SHORT);
                        }
                        userEmailEditText.setText("");
                        userNameEditText.setText("");
                        nameChangeSuccessful = false;
                        emailChangeSuccessful = false;
                        profileImageChangeSuccessful = false;
                    }
                });
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
                into(new SimpleTarget<Bitmap>(500, 500) {
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
        uploadIntent = data;
        switch (requestCode) {
            //If user has chosen image, get this image as Bitmap and set to profilePicture imageView
            case PICK_IMAGE_INTENT_KEY:
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        uploadIfPermissionGranted();
                    }

                }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void uploadIfPermissionGranted() {


        //Checking if permission to READ_EXTERNAL_STORAGE is granted by user
        if (isPermissionReadFromExternalStorageGranted()) {
            //If it's granted, just upload the image
            try {
                final Uri profileImageUri = uploadIntent.getData();
                final InputStream profileImageStream = getContentResolver().openInputStream(profileImageUri);
                final Bitmap selectedProfileImage = BitmapFactory.decodeStream(profileImageStream);
                newCircledDrawable = Utils.getCircleImage(this, selectedProfileImage);
                profilePicture.setImageDrawable(newCircledDrawable);
                profileImageChangeSuccessful = true;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                showToast(this, getString(R.string.change_image_error_message), Toast.LENGTH_LONG);
            }
        } else {
            //If it's not granted, request it
            ActivityCompat.requestPermissions(
                    this, new String[]{WRITE_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_REQUEST_CODE);
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

    public void isGoogleUser() {
        for (UserInfo user : authUser.getProviderData()) {
            if (user.getProviderId().equals("google.com")) {
                //User signed in with google, we don't want him to change email
                // Disable userEmailEditText
                TextInputLayout emailTextInputLayout = (TextInputLayout) findViewById(R.id.email_textinputlayout);
                emailTextInputLayout.setVisibility(GONE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (eventListener != null) {
            userReference.removeEventListener(eventListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean showRationale = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            showRationale = shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        switch (requestCode) {

            case READ_EXTERNAL_STORAGE_REQUEST_CODE:
                // If request is not granted, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    showToast(this.getApplicationContext(), getString(R.string.permission_granted_message),
                            Toast.LENGTH_SHORT);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        uploadIfPermissionGranted();
                    }
                    break;
                } else {
                    //Permission is not granted, but did the user also check "Never ask again"?
                    if (!showRationale) {
                        // user denied permission and also checked "Never ask again"
                        Utils.showMessageOKCancel(this, getString(R.string.upload_permission_message),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", AccountActivity.this.getPackageName(), null);
                                        intent.setData(uri);
                                        startActivityForResult(intent, PERMISSION_SETTING_REQUEST_CODE);
                                    }
                                });
                    }
                }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public boolean isPermissionReadFromExternalStorageGranted() {
        return (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)

                == PackageManager.PERMISSION_GRANTED);
    }
}
