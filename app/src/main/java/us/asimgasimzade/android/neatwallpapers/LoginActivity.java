package us.asimgasimzade.android.neatwallpapers;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import us.asimgasimzade.android.neatwallpapers.data.GridItem;

import static android.os.Build.VERSION_CODES.M;
import static us.asimgasimzade.android.neatwallpapers.utils.Utils.showToast;

/**
 * Login screen that offers log in via email/password or using google/facebook account
 */

public class LoginActivity extends AppCompatActivity {
    private EditText passwordEditText;
    private AutoCompleteTextView emailAutoCompleteTextView;
    private Button signInButton, signUpButton, resetPasswordButton;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private static ArrayList<String> savedEmails;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        //If user is currently signed in, just start the MainActivity
        if (auth.getCurrentUser() != null){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        //Setting view after checking user signed in
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //Getting references to views
        signInButton = (Button) findViewById(R.id.sign_in_button);
        signUpButton = (Button) findViewById(R.id.sign_up_button);
        resetPasswordButton = (Button) findViewById(R.id.reset_password_button);
        emailAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.email_editText);
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

        if(savedEmails.size() > 0){
            ArrayAdapter<String> savedEmailsAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, savedEmails);
            emailAutoCompleteTextView.setAdapter(savedEmailsAdapter);
        }


        //Get Firebase auth instance (again?)
        auth = FirebaseAuth.getInstance();

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailAutoCompleteTextView.getText().toString();
                final String password = passwordEditText.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    showToast(getApplicationContext(), "Please, enter email address!", Toast.LENGTH_SHORT);
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    showToast(getApplicationContext(), "Please, enter password!", Toast.LENGTH_SHORT);
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
                        if (!task.isSuccessful()){
                            //Login task failed
                            if (password.length() < 6){
                                passwordEditText.setError(getString(R.string.minimum_password));
                            } else {
                                showToast(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG);
                            }


                        } else {
                            //Login successful
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

                            //Go to MainActivity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {



        super.onSaveInstanceState(outState);
    }
}
