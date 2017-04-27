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
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import us.asimgasimzade.android.neatwallpapers.data.User;

import static us.asimgasimzade.android.neatwallpapers.utils.Utils.showToast;

/**
 * A sig nup screen that offers signing up via email/password.
 */
public class SignUpActivity extends AppCompatActivity {

    private EditText fullnameEditText, emailEditText, passwordEditText;
    private Button signUpButton;
    private ProgressBar progressBar;
    private FirebaseUser authUser;
    String userId;
    private FirebaseAuth auth;
    SharedPreferences sharedPreferences;
    ArrayList<String> savedEmails;
    Gson gson;
    private String resetPasswordEmail;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Get FireBase auth instance
        auth = FirebaseAuth.getInstance();

        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signUpButton = (Button) findViewById(R.id.sign_up_button);
        Button resetPasswordButton = (Button) findViewById(R.id.reset_password_button);
        fullnameEditText = (EditText) findViewById(R.id.name_editText);
        emailEditText = (EditText) findViewById(R.id.email_editText);
        passwordEditText = (EditText) findViewById(R.id.password_editText);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //Get shared preferences instance
        sharedPreferences = getSharedPreferences("EMAILS_SP", Context.MODE_PRIVATE);
        gson = new Gson();

        //Getting saved emails list from shared preferences and showing them as
        // autocomplete in emailAutoCompleteTextView
        String savedEmailsJson = sharedPreferences.getString("SavedEmailsList", "");
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        savedEmails = new ArrayList<>();
        ArrayList<String> savedEmailsFromJson = gson.fromJson(savedEmailsJson, listType);
        if(savedEmailsFromJson != null){
            savedEmails = savedEmailsFromJson;
        }

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder forgotPasswordDialog = new AlertDialog.Builder(SignUpActivity.this);

                forgotPasswordDialog.setMessage(R.string.forgot_password_msg);
                //Create container view to set margins
                FrameLayout container = new FrameLayout(SignUpActivity.this);
                // Set up the input
                final AutoCompleteTextView emailEditText = new AutoCompleteTextView(SignUpActivity.this);
                emailEditText.setSingleLine();
                // Specify the type of input expected;
                FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = 40;
                params.rightMargin = 40;
                params.topMargin = 40;
                emailEditText.setLayoutParams(params);
                container.addView(emailEditText);
                emailEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                emailEditText.requestFocus();

                forgotPasswordDialog.setView(container);
                // Set positive button
                forgotPasswordDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetPasswordEmail = emailEditText.getText().toString();
                        if(resetPasswordEmail.isEmpty()){
                            showToast(SignUpActivity.this.getApplicationContext(),
                                    getString(R.string.reset_password_empty_email_message), Toast.LENGTH_LONG);
                        } else {

                            showProgressDialog(getString(R.string.message_reset_password));
                            auth.sendPasswordResetEmail(resetPasswordEmail).
                                    addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                showToast(SignUpActivity.this.getApplicationContext(),
                                                        getString(R.string.reset_password_success_message), Toast.LENGTH_LONG);
                                            } else {
                                                showToast(SignUpActivity.this.getApplicationContext(),
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

        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        signUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    showToast(getApplicationContext(), "Please, enter email address!", Toast.LENGTH_SHORT);
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    showToast(getApplicationContext(), "Please, enter password!", Toast.LENGTH_SHORT);
                    return;
                }

                if (password.length() < 6) {
                    showToast(getApplicationContext(), "Password is too short, please enter at least 6 characters!",
                            Toast.LENGTH_SHORT);
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //Create a user
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignUpActivity.this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign up fails, display a message to the user. If sign up succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed up user can be handled in the listener. We also create new entry
                                // in FireBase database for this user if sign up succeeds
                                if (!task.isSuccessful()) {
                                    showToast(SignUpActivity.this, "Sign up failed", Toast.LENGTH_SHORT);
                                } else {
                                    // Sign up successful
                                    // Creating new user object
                                    User user = new User(fullnameEditText.getText().toString(),
                                            emailEditText.getText().toString(), "");

                                    // Save emails to SharedPreferences for using in email
                                    // AutoCompleteTextView in future
                                    if(auth.getCurrentUser() != null){
                                        String currentEmail = auth.getCurrentUser().getEmail();
                                        if(!savedEmails.contains(currentEmail)) {
                                            savedEmails.add(currentEmail);
                                        }
                                    }
                                    SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                                    String savedEmailsListJson = gson.toJson(savedEmails);
                                    sharedPreferencesEditor.putString("SavedEmailsList", savedEmailsListJson);
                                    sharedPreferencesEditor.apply();


                                    // Add new user to FirebaseDatabase under the "users" node
                                    // Creating new user node, which returns the unique key value
                                    // new user node would be /users/$userid/

                                    //Get FireBase database reference instance
                                    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                                    //Getting FireBaseAuth object instance
                                    auth = FirebaseAuth.getInstance();
                                    //Getting FireBaseUser for current user
                                    authUser = auth.getCurrentUser();
                                    userId = authUser != null ? authUser.getUid() : null;
                                    // pushing user to 'users' node using the userId
                                    database.child("users").child(userId).setValue(user);

                                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                    finish();
                                }
                            }
                        });

            }
        });

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

    //Making passwordEditText to click signUpButton when ACTION_DONE pressed on keyboard
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            if (passwordEditText.hasFocus()) {
                // user presses done button on keyboard and signUpButton gets clicked
                signUpButton.callOnClick();
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
}

