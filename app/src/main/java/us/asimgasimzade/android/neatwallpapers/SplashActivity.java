package us.asimgasimzade.android.neatwallpapers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Splash Screen Activity showing when launch takes long
 */

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

        //To make FireBase database persist state offline
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        finish();
    }
}
