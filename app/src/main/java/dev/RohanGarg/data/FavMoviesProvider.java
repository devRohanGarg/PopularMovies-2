package dev.RohanGarg.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by Rohan Garg on 07-02-2016.
 */
public class FavMoviesProvider extends ContentProvider {

    static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(FavMoviesContract.PROVIDER_NAME, "movies", FavMoviesContract.MOVIES);
        uriMatcher.addURI(FavMoviesContract.PROVIDER_NAME, "movies/#", FavMoviesContract.MOVIE_ID);
    }

    private SQLiteDatabase db;

    public FavMoviesProvider(Context context) {
        FavMoviesDbHelper dbHelper = new FavMoviesDbHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public FavMoviesProvider() {
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        FavMoviesDbHelper dbHelper = new FavMoviesDbHelper(context);
        db = dbHelper.getWritableDatabase();
        return (db != null);
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(FavMoviesContract.MOVIES_TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case FavMoviesContract.MOVIES:
                qb.setProjectionMap(FavMoviesContract.MOVIES_PROJECTION_MAP);
                break;

            case FavMoviesContract.MOVIE_ID:
                qb.appendWhere("_id =" + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        new FavMoviesDbHelper(getContext());

        if (sortOrder == null || sortOrder == "") {
            /**
             * By default sort on movie name
             */
            sortOrder = "title";
        }

        return qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            /**
             * Get all movie records
             */
            case FavMoviesContract.MOVIES:
                return "vnd.android.cursor.dir/vnd.RohanGarg.movies";

            /**
             * Get a movie record
             */
            case FavMoviesContract.MOVIE_ID:
                return "vnd.android.cursor.item/vnd.RohanGarg.movies";

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /**
         * Add a new movie record
         */
        long rowID = db.insert(FavMoviesContract.MOVIES_TABLE_NAME, "", values);

        /**
         * If record is added successfully
         */

        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(FavMoviesContract.CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case FavMoviesContract.MOVIES:
                count = db.delete(FavMoviesContract.MOVIES_TABLE_NAME, selection, selectionArgs);
                break;

            case FavMoviesContract.MOVIE_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete(FavMoviesContract.MOVIES_TABLE_NAME, "_id = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case FavMoviesContract.MOVIES:
                count = db.update(FavMoviesContract.MOVIES_TABLE_NAME, values, selection, selectionArgs);
                break;

            case FavMoviesContract.MOVIE_ID:
                count = db.update(FavMoviesContract.MOVIES_TABLE_NAME, values, "_id = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (count == 0) {
            insert(uri, values);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}