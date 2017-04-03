package us.asimgasimzade.android.neatwallpapers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static us.asimgasimzade.android.neatwallpapers.utils.Utils.showToast;

/**
 * A sig nup screen that offers signing up via email/password.
 */
public class SignUpActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button signInButton, signUpButton, resetPasswordButton;
    private ProgressBar progressBar;
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
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    showToast(SignUpActivity.this, "Authentication failed", Toast.LENGTH_SHORT);
                                } else {
                                    //Sign up successful
                                    //Save emails to sharedpreferences for using in email AutoCompleteTextView in future
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

                                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                    finish();
                                }
                            }
                        });

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
}

