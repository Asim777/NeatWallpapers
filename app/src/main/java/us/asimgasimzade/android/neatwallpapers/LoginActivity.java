package us.asimgasimzade.android.neatwallpapers;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import us.asimgasimzade.android.neatwallpapers.data.User;

import static us.asimgasimzade.android.neatwallpapers.utils.Utils.showToast;

/**
 * Login screen that offers log in via email/password or using google account
 */

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 116;
    private EditText passwordEditText;
    private AutoCompleteTextView emailAutoCompleteTextView;
    private FirebaseAuth auth;
    private static ArrayList<String> savedEmails;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    Button signInButton;
    GoogleApiClient mGoogleApiClient;
    String userName;
    String userEmail;
    String userProfilePicture;
    User user;
    DatabaseReference database;
    String userId;
    private ProgressDialog progressDialog;
    String resetPasswordEmail;
    ArrayAdapter<String> savedEmailsAdapter;
    private ValueEventListener userListener;
    private DatabaseReference userReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        //If user is currently signed in, just start the MainActivity
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        //Setting view after checking user signed in
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Getting references to views
        signInButton = (Button) findViewById(R.id.sign_in_button);
        Button signUpButton = (Button) findViewById(R.id.sign_up_button);
        final Button resetPasswordButton = (Button) findViewById(R.id.reset_password_button);
        Button googleSignInButton = (Button) findViewById(R.id.sign_in_with_google_button);

        emailAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.email_editText);
        passwordEditText = (EditText) findViewById(R.id.password_editText);


        //Get shared preferences instance
        sharedPreferences = getSharedPreferences("EMAILS_SP", Context.MODE_PRIVATE);
        gson = new Gson();
        //Getting saved emails list from shared preferences and showing them as
        // autocomplete in emailAutoCompleteTextView
        String savedEmailsJson = sharedPreferences.getString("SavedEmailsList", "");
        Type listType = new TypeToken<ArrayList<String>>() {
        }.getType();
        savedEmails = new ArrayList<>();
        ArrayList<String> savedEmailsFromJson = gson.fromJson(savedEmailsJson, listType);
        if (savedEmailsFromJson != null) {
            savedEmails = savedEmailsFromJson;
        }
        if (savedEmails.size() > 0) {
            savedEmailsAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, savedEmails);
            emailAutoCompleteTextView.setAdapter(savedEmailsAdapter);
        }


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        showToast(getApplicationContext(), "Google sign in unsuccessful!", Toast.LENGTH_LONG);
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        //Sign up button
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });

        //Reset password button
        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder forgotPasswordDialog = new AlertDialog.Builder(LoginActivity.this);

                forgotPasswordDialog.setMessage(R.string.forgot_password_msg);
                //Create container view to set margins
                FrameLayout container = new FrameLayout(LoginActivity.this);
                // Set up the input
                final AutoCompleteTextView emailEditText = new AutoCompleteTextView(LoginActivity.this);
                emailEditText.setSingleLine();
                // Specify the type of input expected;
                FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = 40;
                params.rightMargin = 40;
                params.topMargin = 40;
                emailEditText.setLayoutParams(params);
                container.addView(emailEditText);
                emailEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                emailEditText.setAdapter(savedEmailsAdapter);
                emailEditText.requestFocus();

                forgotPasswordDialog.setView(container);
                // Set positive button
                forgotPasswordDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetPasswordEmail = emailEditText.getText().toString();
                        if(resetPasswordEmail.isEmpty()){
                            showToast(LoginActivity.this.getApplicationContext(),
                                    getString(R.string.reset_password_empty_email_message), Toast.LENGTH_LONG);
                        } else {
                            showProgressDialog(getString(R.string.message_reset_password));
                            auth.sendPasswordResetEmail(resetPasswordEmail).
                                    addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        showToast(LoginActivity.this.getApplicationContext(),
                                                getString(R.string.reset_password_success_message), Toast.LENGTH_LONG);
                                    } else {
                                        showToast(LoginActivity.this.getApplicationContext(),
                                                getString(R.string.reset_password_fail_message), Toast.LENGTH_LONG);
                                    }
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    }
                });
                //Set negative button
                forgotPasswordDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = forgotPasswordDialog.create();
                if (alertDialog.getWindow() != null){
                    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                }
                alertDialog.show();
            }
        });

        //Google sign in button
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog(getString(R.string.message_login));
                signInWithGoogle();

            }
        });


        //Sign in button
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailAutoCompleteTextView.getText().toString();
                final String password = passwordEditText.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    emailAutoCompleteTextView.setError(getString(R.string.empty_email_error_message));
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    emailAutoCompleteTextView.setError(getString(R.string.empty_password_error_message));
                    return;
                }

                showProgressDialog(getString(R.string.message_login));

                //authenticate the user
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                progressDialog.dismiss();
                                if (!task.isSuccessful()) {
                                    //Login task failed
                                    if (password.length() < 6) {
                                        passwordEditText.setError(getString(R.string.minimum_password));
                                    } else {
                                        showToast(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG);
                                    }


                                } else {
                                    //Login successful
                                    //Save emails to SharedPreferences for using in email AutoCompleteTextView in future
                                    if (auth.getCurrentUser() != null) {
                                        String currentEmail = auth.getCurrentUser().getEmail();
                                        if (!savedEmails.contains(currentEmail)) {
                                            savedEmails.add(currentEmail);
                                        }
                                    }
                                    SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                                    String savedEmailsListJson = gson.toJson(savedEmails);
                                    sharedPreferencesEditor.putString("SavedEmailsList", savedEmailsListJson);
                                    sharedPreferencesEditor.apply();
                                    progressDialog.dismiss();
                                    goToMainActivity();


                                }
                            }
                        });
            }
        });
    }

    private void goToMainActivity() {
        //Go to MainActivity
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void signInWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...)
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }

    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.d("AsimTag", "Sign in attempt status is " + result.getStatus());
        progressDialog.dismiss();
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount googleSignInAccount = result.getSignInAccount();
            if (googleSignInAccount != null) {
                firebaseAuthWithGoogle(googleSignInAccount);
                // Save email to sharedpreferences for using in email
                // AutoCompleteTextView in future
                if (auth.getCurrentUser() != null) {
                    String currentEmail = auth.getCurrentUser().getEmail();
                    if (!savedEmails.contains(currentEmail)) {
                        savedEmails.add(currentEmail);
                    }
                }
                SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                String savedEmailsListJson = gson.toJson(savedEmails);
                sharedPreferencesEditor.putString("SavedEmailsList", savedEmailsListJson);
                sharedPreferencesEditor.apply();
            } else {
                //Google signInAccount object is null. Sign in failed
                showToast(LoginActivity.this, getString(R.string.google_sign_in_fail_message), Toast.LENGTH_SHORT);
            }
        } else {
            //Google sign in failed
            showToast(LoginActivity.this, getString(R.string.google_sign_in_fail_message), Toast.LENGTH_SHORT);
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount googleSignInAccount) {
        //get an ID token from the GoogleSignInAccount object, exchange it for a Firebase credential,
        // and authenticate with Firebase using the Firebase credential
        AuthCredential credential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {
                    //Google sign in failed
                    showToast(LoginActivity.this, getString(R.string.google_sign_in_fail_message), Toast.LENGTH_SHORT);
                } else {
                    //Google sign in was successful
                    //By default google gives us 96x96 photo for profile picture, we'll change the uri
                    // a bit to get a better one
                    String profilePictureUriString = "";
                    if (googleSignInAccount.getPhotoUrl() != null) {
                        profilePictureUriString = googleSignInAccount.getPhotoUrl().toString();
                    }
                    String profilePictureUriStringHD = profilePictureUriString.replace("s96-c", "s500-c");
                    userName = googleSignInAccount.getDisplayName();
                    userEmail = googleSignInAccount.getEmail();
                    userProfilePicture = profilePictureUriStringHD;
                    //Add new user to database
                    addNewUserInfoToDatabase(userName, userEmail, userProfilePicture);


                }
                //If the call to signInWithCredential succeeds, the AuthStateListener runs the
                // onAuthStateChanged callback
            }
        });
    }

    private void addNewUserInfoToDatabase(final String googleUserName,
                                          final String googleUserEmail, final String googleUserProfilePicture) {
        // Add new user to FirebaseDatabase under the "users" node
        // Creating new user node, which returns the unique key value
        // new user node would be /users/$userid/

        //Get FireBase database reference instance
        database = FirebaseDatabase.getInstance().getReference();

        //Getting FirebaseUser for current user
        FirebaseUser authUser = auth.getCurrentUser();
        userId = authUser != null ? authUser.getUid() : null;
        userReference = database.child("users").child(userId);
        // This event listener is triggered whenever there is a change in user profile data
        userListener = userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    user = new User(googleUserName, googleUserEmail, googleUserProfilePicture);

                    // adding user to 'users' node using the userId
                    if (userId != null) {
                        database.child("users").child(userId).setValue(user);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
            }
        });

        //Go to main activity
        goToMainActivity();
    }

    private void showProgressDialog(String message) {
        //Creating progress dialog
         progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.show();
    }


    //Making emailAutoCompleteTextView to focus on passwordEditText when ACTION_NEXT pressed
    //And passwordEditText to click signInButton when ACTION_DONE pressed
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            if (emailAutoCompleteTextView.hasFocus()) {
                // sends focus to passwordEditText field if user pressed "Next"
                passwordEditText.requestFocus();
                return true;
            } else if (passwordEditText.hasFocus()) {
                // presses sign in button
                signInButton.performClick();
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if( userListener != null && userReference != null){
            userReference.removeEventListener(userListener);
        }
    }
}
