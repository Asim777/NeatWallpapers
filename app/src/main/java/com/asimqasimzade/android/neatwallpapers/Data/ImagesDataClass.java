package com.asimqasimzade.android.neatwallpapers.Data;

import com.google.gson.Gson;

import java.util.ArrayList;


/**
 * This class is used to save GridItems' information retrieved by LoadImagesAsyncTask to
 * use in SingleImageActivity when loading new images while scrolling images
 */

public class ImagesDataClass {

    public static ArrayList<GridItem> imageslist = new ArrayList<>();
    public static ArrayList<GridItem> popularImagesList = new ArrayList<>();
    public static ArrayList<GridItem> recentImagesList = new ArrayList<>();
    public static ArrayList<GridItem> favoriteImagesList = new ArrayList<>();

}
