package us.asimgasimzade.android.neatwallpapers.FavoritesDB;

import android.provider.BaseColumns;

/**
 * Defines table and column names for the Favorites database.
 */

 public class FavoritesDBContract {

    //Private constructor to prevent someone from accidentally instantiating the contract class
    private FavoritesDBContract() {

    }

    /* Inner class that defines the table contents of the Favorites table */
     public static final class FavoritesEntry implements BaseColumns {

        //Setting our table's name
         public static final String TABLE_NAME = "favorites";

        //Column for storing image name
        public static final String IMAGE_NAME = "image_name";

        //Column for storing image url
         public static final String IMAGE_URL = "image_url";

        //Column for storing image author
        public static final String IMAGE_AUTHOR = "image_author";

        //Column for storing image link
        public static final String IMAGE_LINK = "image_link";

    }
}
