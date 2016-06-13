package dev.RohanGarg;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import dev.RohanGarg.data.FavMoviesContract;
import dev.RohanGarg.models.Movie;
import dev.RohanGarg.models.Video;
import dev.RohanGarg.utils.AppController;
import dev.RohanGarg.utils.Utils;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */

    private static String TAG = MovieDetailFragment.class.getSimpleName();
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.overview)
    TextView overview;
    @Bind(R.id.date)
    TextView date;
    @Bind(R.id.rating)
    TextView rating;
    @Bind(R.id.popularity)
    TextView popularity;
    @BindString(R.string.movieQueryBaseUrl)
    String movieQueryBaseUrl;
    @BindString(R.string.KEY)
    String KEY;
    @BindString(R.string.TAG_JSON)
    String TAG_JSON;
    boolean isReviewFetched;
    boolean isVideoFetched;
    Movie movie;
    Activity activity;
    View rootView;
    ListView videosView;
    String reviews;
    ArrayList<Video> videos;
    ShareActionProvider mShareActionProvider;

    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this.getActivity();
        setHasOptionsMenu(true);
        movie = getArguments().getParcelable("movie");
        if (savedInstanceState != null) {
            isReviewFetched = savedInstanceState.getBoolean("isReviewFetched");
            isVideoFetched = savedInstanceState.getBoolean("isVideoFetched");
            videos = savedInstanceState.getParcelableArrayList("videos");
        } else
            videos = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.content_movie_detail, container, false);
        ButterKnife.bind(this, rootView);
        if (activity != null && movie != null) {
            title.setText(movie.title);
            overview.setText(movie.overview);
            date.setText(movie.releaseDate);
            String r = String.format("%.1f", Float.parseFloat(movie.rating));
            String p = String.format("%.0f", Float.parseFloat(movie.popularity)) + "%";
            rating.setText(r);
            popularity.setText(p);

            try {
                Picasso.with(activity).load(movie.backDropImgURL).placeholder(R.drawable.ui_shadow).error(R.drawable.ui_shadow).into((ImageView) activity.findViewById(R.id.backDrop));
                Picasso.with(activity).load(movie.posterImgURL).placeholder(R.drawable.ui_shadow).error(R.drawable.ui_shadow).into((ImageView) activity.findViewById(R.id.poster));
                FloatingActionButton favourite = (FloatingActionButton) activity.findViewById(R.id.favourite);
                favourite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setFavOrUnFav();
                    }
                });
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if (!isVideoFetched) {
                        fetchVideos();
                    } else {
                        displayVideos();
                    }
                }
            });

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if (!isReviewFetched) {
                        fetchReviews();
                    }
                }
            });

        }

        return rootView;
    }

    void displayVideos() {
        videosView = ButterKnife.findById(rootView, R.id.videos);
        ArrayAdapter<Video> adapter = new ArrayAdapter<Video>(activity, R.layout.simple_list_item_1, android.R.id.text1, videos) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                text1.setText(videos.get(position).name);
                return view;
            }

        };
        videosView.setAdapter(adapter);
        Utils.setListViewHeightBasedOnChildren(videosView);
        videosView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                watchYoutubeVideo(videos.get(position).id);
            }
        });
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_detail, menu);
        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        // If onLoadFinished happens before this, we can go ahead and set the share intent now.

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        if (videos.size() > 0)
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Checkout this video for the movie " + movie.title + " :\n\n" + videos.get(0).name + "\n" +
                            "http://www.youtube.com/watch?v=" +
                            videos.get(0).id
            );
        else
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Sharing is caring :) ");
        return shareIntent;
    }

    void displayReviews() {
        TextView reviewsContent = ButterKnife.findById(rootView, R.id.reviewsContent);
        reviewsContent.setText(reviews);
    }

    void setFavOrUnFav() {
        Cursor cursor = activity.getContentResolver().query(FavMoviesContract.CONTENT_URI,
                null, "id =" + movie.id,
                null, null);

        //If our cursor is not null and the count is 0 it's a new favorite, else removed it.
        if (cursor != null) {
            if (cursor.getCount() == 0) {

                ContentValues values = new ContentValues();
                values.put("id", movie.id);
                values.put("title", movie.title);
                values.put("overview", movie.overview);
                values.put("backDropImgURL", movie.backDropImgURL);
                values.put("rating", movie.rating);
                values.put("posterImgURL", movie.posterImgURL);
                values.put("releaseDate", movie.releaseDate);
                values.put("popularity", movie.popularity);
                values.put("voteCount", movie.voteCount);

                activity.getContentResolver().update(
                        FavMoviesContract.CONTENT_URI, values,
                        "id =" + movie.id, null);
                Snackbar.make(ButterKnife.findById(activity, R.id.root), "Added to favourites", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                activity.getContentResolver().delete(
                        FavMoviesContract.CONTENT_URI,
                        "id =" + movie.id, null);
                Snackbar.make(ButterKnife.findById(activity, R.id.root), "Removed from favourites", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            cursor.close();
        }
    }

    void watchYoutubeVideo(String id) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=" + id));
            startActivity(intent);
        }
    }

    private void fetchVideos() {
        String finalURL = movieQueryBaseUrl + movie.id + "/videos?api_key=" + KEY;
        //Log.d(TAG, finalURL);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(finalURL,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            JSONArray jsonArray = response.getJSONArray("results");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                videos.add(new Video(jsonObject.getString("name"), jsonObject.getString("key")));
                            }
                            displayVideos();
                            isVideoFetched = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.d(TAG, "Error: " + error.getMessage());
                Snackbar.make(ButterKnife.findById(activity, R.id.root), "Aw, Snap! Something went wrong", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, TAG_JSON);
    }

    private void fetchReviews() {
        String finalURL = movieQueryBaseUrl + movie.id + "/reviews?api_key=" + KEY;
        //Log.d(TAG, finalURL);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(finalURL,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            JSONArray jsonArray = response.getJSONArray("results");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                reviews += jsonObject.getString("author") + " :\n\n" + jsonObject.getString("content") + "\n\n\n";
                            }
                            if (reviews == null)
                                reviews = "No reviews yet!";
                            displayReviews();
                            isReviewFetched = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.d(TAG, "Error: " + error.getMessage());
                String reviews = "Error loading reviews!";
                TextView reviewsContent = ButterKnife.findById(rootView, R.id.reviewsContent);
                reviewsContent.setText(reviews);
            }
        });
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, TAG_JSON);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isReviewFetched", isReviewFetched);
        outState.putBoolean("isVideoFetched", isVideoFetched);
        outState.putParcelableArrayList("videos", videos);
    }
}