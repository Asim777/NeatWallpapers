package us.asimgasimzade.android.neatwallpapers.favorites_db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import us.asimgasimzade.android.neatwallpapers.favorites_db.FavoritesDBContract.FavoritesEntry;

/**
 * Manages local Favorites Database
 */

public class FavoritesDBHelper extends SQLiteOpenHelper {


    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "favorites.db";
    private String SQL_DELETE_ENTRIES;

    //Default constructor
    public FavoritesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Create a table to hold favorite images. Image entry consists of ID, IMAGE_URL, IMAGE_NAME,
        //IMAGE_AUTHOR and IMAGE_LINK
         String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " +
                 FavoritesEntry.TABLE_NAME + " ( " +
                 FavoritesEntry._ID + " INTEGER PRIMARY KEY," +
                 FavoritesEntry.IMAGE_NAME + " TEXT NOT NULL, " +
                 FavoritesEntry.IMAGE_URL + " TEXT UNIQUE NOT NULL, " +
                 FavoritesEntry.IMAGE_AUTHOR + " TEXT NOT NULL, " +
                 FavoritesEntry.IMAGE_LINK + " TEXT NOT NULL " + ");";


        SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + FavoritesEntry.TABLE_NAME;

        //Execute SQL to create Favorites table
        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);

    }
}
