package com.asimqasimzade.android.neatwallpapers.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.asimqasimzade.android.neatwallpapers.Data.ImagesDataClass;
import com.asimqasimzade.android.neatwallpapers.R;
import com.asimqasimzade.android.neatwallpapers.SingleImageActivity;
import com.asimqasimzade.android.neatwallpapers.SingleImageFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static java.lang.Thread.sleep;

/**
 * Created by Asim on 2/1/2017.
 */

public class SingleImageViewPagerAdapter extends FragmentStatePagerAdapter {

    Context mContext;
    int mImageNumber;
    final String IMAGE_NUMBER = "image_number";


    public SingleImageViewPagerAdapter (FragmentManager fm, Context context, int imageNumber) {
        super(fm);
        mContext = context;
        mImageNumber = imageNumber;

    }

    @Override
    public Fragment getItem(int position) {

        //Getting current position of image and sending it in bundle to newly created fragment
        int currentPosition = mImageNumber + position;
        Bundle b = new Bundle();
        b.putInt(IMAGE_NUMBER, currentPosition);
        SingleImageFragment singleImageFragment = new SingleImageFragment();
        singleImageFragment.setArguments(b);

        return singleImageFragment;
    }

    @Override
    public int getCount() {
        return 200;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }
}
