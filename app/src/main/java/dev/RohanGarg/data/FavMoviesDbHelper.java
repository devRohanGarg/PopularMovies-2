package dev.RohanGarg.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Rohan Garg on 07-02-2016.
 */
public class FavMoviesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FavouriteMovies";
    private static final int DATABASE_VERSION = 1;

    public FavMoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FavMoviesContract.CREATE_DB_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
        onCreate(db);
    }
}