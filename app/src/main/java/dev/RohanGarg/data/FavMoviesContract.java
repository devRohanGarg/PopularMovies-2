package dev.RohanGarg.data;

import android.net.Uri;

import java.util.HashMap;

/**
 * Created by Rohan Garg on 07-02-2016.
 */
public class FavMoviesContract {

    public static final String PROVIDER_NAME = "dev.RohanGarg.FavoriteMovies";
    public static final String PROVIDER_URL = "content://" + PROVIDER_NAME + "/movies";
    public static final Uri CONTENT_URI = Uri.parse(PROVIDER_URL);
    public static final String MOVIES_TABLE_NAME = "movies";
    public static final String CREATE_DB_TABLE =
            " CREATE TABLE " + MOVIES_TABLE_NAME +
                    " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " id INTEGER NOT NULL, " +
                    " title TEXT NOT NULL, " +
                    " overview TEXT NOT NULL, " +
                    " backDropImgURL TEXT, " +
                    " rating TEXT, " +
                    " posterImgURL TEXT, " +
                    " releaseDate TEXT, " +
                    " popularity TEXT, " +
                    " voteCount TEXT);";
    static final int MOVIES = 1;
    static final int MOVIE_ID = 2;
    public static HashMap<String, String> MOVIES_PROJECTION_MAP;
}