package us.asimgasimzade.android.neatwallpapers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

import us.asimgasimzade.android.neatwallpapers.data.User;

import static us.asimgasimzade.android.neatwallpapers.utils.Utils.showToast;

/**
 * Login screen that offers log in via email/password or using google/facebook account
 */

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 116;
    private EditText passwordEditText;
    private AutoCompleteTextView emailAutoCompleteTextView;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private static ArrayList<String> savedEmails;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    Button signInButton;
    GoogleApiClient mGoogleApiClient;
    CallbackManager facebookCallbackManager;
    ProfileTracker mProfileTracker;
    Profile currentProfile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        //Initialize Facebook CallbackManager
        facebookCallbackManager = CallbackManager.Factory.create();

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
        Button resetPasswordButton = (Button) findViewById(R.id.reset_password_button);
        Button googleSignInButton = (Button) findViewById(R.id.sign_in_with_google_button);
        LoginButton facebookSignInButton = (LoginButton) findViewById(R.id.sign_in_with_facebook);
        facebookSignInButton.setReadPermissions(Arrays.asList("public_profile", "email"));

        emailAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.email_editText);
        passwordEditText = (EditText) findViewById(R.id.password_editText);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);


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
            ArrayAdapter<String> savedEmailsAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, savedEmails);
            emailAutoCompleteTextView.setAdapter(savedEmailsAdapter);
        }


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.firebase_web_client_id))
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


        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //User is signed in

                } else {
                    // User is signed out
                }
            }
        };


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
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });

        //Google sign in button
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();

            }
        });

        //Facebook sign in button
/*        facebookSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithFacebook();
            }
        });*/

        facebookSignInButton.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                String token = loginResult.getAccessToken().getToken();
                AccessToken accessToken = loginResult.getAccessToken();
                signInWithFacebook(token, accessToken);

            }

            @Override
            public void onCancel() {
                showToast(LoginActivity.this.getApplicationContext(), getString(R.string.facebook_sign_in_cancelled_message), Toast.LENGTH_SHORT);
            }

            @Override
            public void onError(FacebookException error) {
                showToast(LoginActivity.this.getApplicationContext(), getString(R.string.facebook_sign_in_fail_message), Toast.LENGTH_SHORT);
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

                progressBar.setVisibility(View.VISIBLE);

                //authenticate the user
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                progressBar.setVisibility(View.GONE);
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
                                    goToMainActivity();


                                }
                            }
                        });
            }
        });
    }

    private void createNewUserFromFacebook(AuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this.getApplicationContext(), "Facebook sign in failed",
                                    Toast.LENGTH_SHORT).show();
                        }

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

    private void signInWithFacebook(String uToken, AccessToken accessToken) {

        final AuthCredential credential = FacebookAuthProvider.getCredential(uToken);

        GraphRequest request = GraphRequest.newMeRequest(accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.v("LoginActivity", response.toString());

                        // Application code
                        try {
                            String fbEmail = response.getJSONObject().getString("email");
                            String fbFullName = response.getJSONObject().getString("name");

                            //Getting facebook profile

                            if (Profile.getCurrentProfile() == null) {
                                mProfileTracker = new ProfileTracker() {
                                    @Override
                                    protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                                        // profile2 is the new profile
                                        mProfileTracker.stopTracking();
                                        currentProfile = profile2;
                                    }
                                };
                                // no need to call startTracking() on mProfileTracker
                                // because it is called by its constructor, internally.
                            } else {
                                currentProfile = Profile.getCurrentProfile();
                            }

                            //Getting profile picture
                            String fbProfilePicture = "";
                            //Getting facebook profile picture
                            if (currentProfile != null) {
                                Uri fbProfilePictureUri = currentProfile.getProfilePictureUri(200, 200);
                                fbProfilePicture = fbProfilePictureUri.toString();
                            }

                            //Create new user
                            createNewUserFromFacebook(credential);
                            //Adding new user to the database
                            addNewUserInfoToDatabase(fbFullName, fbEmail, fbProfilePicture);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }


                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender,birthday");
        request.setParameters(parameters);
        request.executeAsync();

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...)
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }

        // Result returned from facebook login
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);

    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
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
                Log.d("AsimTag", "google sign in account is null");
            }
        } else {
            //Google sign in failed
            showToast(LoginActivity.this, getString(R.string.google_sign_in_fail_message), Toast.LENGTH_SHORT);
            Log.d("AsimTag", "google sign in result is false");
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
                    Log.d("AsimTag", "google sign in task is unsuccessful");
                } else {
                    //Google sign in was successful
                    //By default google gives us 96x96 photo for profile picture, we'll change the uri
                    // a bit to get a better one
                    String profilePictureUriString = "";
                    if (googleSignInAccount.getPhotoUrl() != null) {
                        profilePictureUriString = googleSignInAccount.getPhotoUrl().toString();
                    }
                    String profilePictureUriStringHD = profilePictureUriString.replace("s96-c", "s500-c");
                    addNewUserInfoToDatabase(googleSignInAccount.getDisplayName(),
                            googleSignInAccount.getEmail(), profilePictureUriStringHD);
                    goToMainActivity();
                }
                //If the call to signInWithCredential succeeds, the AuthStateListener runs the
                // onAuthStateChanged callback
            }
        });
    }

    private void addNewUserInfoToDatabase(String mUserName, String mUserEmail, String mUserImage) {
        // Add new user to FirebaseDatabase under the "users" node
        // Creating new user node, which returns the unique key value
        // new user node would be /users/$userid/

        // Creating new user object
        User user = new User(mUserName, mUserEmail, mUserImage);

        //Get FireBase database reference instance
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        //Getting FirebaseUser for current user
        FirebaseUser authUser = auth.getCurrentUser();
        String userId = authUser != null ? authUser.getUid() : null;
        // pushing user to 'users' node using the userId
        if (userId != null) {
            database.child("users").child(userId).setValue(user);
        }
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
        progressBar.setVisibility(View.GONE);
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

}
