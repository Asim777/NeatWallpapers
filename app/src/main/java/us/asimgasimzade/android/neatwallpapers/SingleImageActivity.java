package us.asimgasimzade.android.neatwallpapers;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

/**
 * This Activity holds single image ViewPager
 */

public class SingleImageActivity extends AppCompatActivity {

    int imageNumber;
    String imageSource;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_image);

        //Getting extras from intent
        imageNumber = getIntent().getIntExtra("number", 0);
        imageSource = getIntent().getStringExtra("imageSource");

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        SingleImageHolderFragment singleImageHolderFragment = new SingleImageHolderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("image_number", imageNumber);
        bundle.putString("image_source", imageSource);
        singleImageHolderFragment.setArguments(bundle);
        fragmentTransaction.add(R.id.single_image_holder_fragment_holder, singleImageHolderFragment).commit();


    }
}