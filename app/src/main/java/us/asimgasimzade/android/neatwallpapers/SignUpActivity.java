package us.asimgasimzade.android.neatwallpapers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
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
    private Button signInButton, signUpButton, resetPasswordButton;
    private ProgressBar progressBar;
    private FirebaseUser authUser;
    String userId;
    private FirebaseAuth auth;
    SharedPreferences sharedPreferences;
    ArrayList<String> savedEmails;
    Gson gson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        signInButton = (Button) findViewById(R.id.sign_in_button);
        signUpButton = (Button) findViewById(R.id.sign_up_button);
        resetPasswordButton = (Button) findViewById(R.id.reset_password_button);
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

        resetPasswordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, ResetPasswordActivity.class));
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

                                    // Save emails to sharedpreferences for using in email
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
                                    //Getting FirebaseAuth object instance
                                    auth = FirebaseAuth.getInstance();
                                    //Getting FirebaseUser for current user
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

    //Making passwordEditText to click signUpButton when ACTION_DONE pressed on keyboard
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            if (passwordEditText.hasFocus()) {
                // user presses done button on keyboardand signUpButton gets clicked
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

