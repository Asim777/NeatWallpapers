package us.asimgasimzade.android.neatwallpapers.utils;

import android.graphics.Bitmap;

import com.bumptech.glide.request.target.SimpleTarget;

public interface DownloadTargetInterface {

    void getTheTarget(SimpleTarget<Bitmap> response);

}
