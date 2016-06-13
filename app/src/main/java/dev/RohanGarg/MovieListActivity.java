package dev.RohanGarg;

import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dev.RohanGarg.adapters.RecyclerAdapter;
import dev.RohanGarg.data.FavMoviesContract;
import dev.RohanGarg.data.FavMoviesProvider;
import dev.RohanGarg.models.Movie;
import dev.RohanGarg.utils.AppController;
import dev.RohanGarg.utils.GridAutoFitLayoutManager;

/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MovieListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    public static boolean mTwoPane;
    @BindString(R.string.apiBaseUrl)
    String URL;
    @BindString(R.string.TAG_JSON)
    String TAG_JSON;
    @BindString(R.string.POPULARITY)
    String sortBy;
    private ArrayList<Movie> movies;
    private String TAG = MovieListActivity.class.getSimpleName();
    private int page;
    private int selected;
    private GridAutoFitLayoutManager layoutManager;
    private RecyclerAdapter recyclerAdapter;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        ButterKnife.bind(this);

        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        movies = new ArrayList<>();

        View recyclerView = ButterKnife.findById(this, R.id.movie_list);
        assert recyclerView != null;

        if (savedInstanceState == null) {
            setupRecyclerView((RecyclerView) recyclerView);
            page = 1;
            selected = 0;
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    fetch();
                }
            });
        } else {
            page = savedInstanceState.getInt("page");
            selected = savedInstanceState.getInt("selected");
            sortBy = savedInstanceState.getString("sortBy");
            movies = savedInstanceState.getParcelableArrayList("movies");
            setupRecyclerView((RecyclerView) recyclerView);
        }

        if (ButterKnife.findById(this, R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        } else mTwoPane = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        outState.putInt("page", page);
        outState.putInt("selected", selected);
        outState.putString("sortBy", sortBy);
        outState.putParcelableArrayList("movies", movies);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (selected == 2) loadFavouriteMovies();
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        layoutManager = new GridAutoFitLayoutManager(this, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 152, getApplicationContext().getResources().getDisplayMetrics()));
        recyclerView.setLayoutManager(layoutManager);
        recyclerAdapter = new RecyclerAdapter(this, movies);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (layoutManager.findLastCompletelyVisibleItemPosition() == movies.size() - 1 && selected != 2) {
                    fetch();
                }
            }
        });
    }

    public void fetch() {
        String finalURL = URL + "sort_by=" + sortBy + "&page=" + page + "&api_key=" + getResources().getString(R.string.KEY);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(finalURL,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            JSONArray jsonArray = response.getJSONArray("results");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                movies.add(new Movie(
                                        jsonObject.getInt("id"),
                                        "http://image.tmdb.org/t/p/w342/" + jsonObject.getString("poster_path"),
                                        jsonObject.getString("overview"),
                                        jsonObject.getString("release_date"),
                                        jsonObject.getString("title"),
                                        "http://image.tmdb.org/t/p/w780/" + jsonObject.getString("backdrop_path"),
                                        jsonObject.getString("popularity"),
                                        jsonObject.getString("vote_count"),
                                        jsonObject.getString("vote_average")
                                ));
                            }
                            recyclerAdapter.notifyDataSetChanged();
                            //Log.d(TAG, "On page no: " + page);
                            page++;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.d(TAG, "Error: " + error.getMessage());
                Snackbar.make(findViewById(R.id.root), "Aw, Snap! Something went wrong", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, TAG_JSON);
    }

    public void loadFavouriteMovies() {
        movies.clear();
        recyclerAdapter.notifyDataSetChanged();

        // Retrieve movie records
        FavMoviesProvider provider = new FavMoviesProvider(this);

        Cursor c = provider.query(Uri.parse(FavMoviesContract.CONTENT_URI.toString()), null, null, null, "title");
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    movies.add(new Movie(
                            c.getInt(c.getColumnIndex("id")),
                            getValueFor(c, "posterImgURL"),
                            getValueFor(c, "overview"),
                            getValueFor(c, "releaseDate"),
                            getValueFor(c, "title"),
                            getValueFor(c, "backDropImgURL"),
                            getValueFor(c, "popularity"),
                            getValueFor(c, "voteCount"),
                            getValueFor(c, "rating")
                    ));
                } while (c.moveToNext());
            }
            c.close();
        }

        if (movies.size() > 0) {
            recyclerAdapter.notifyDataSetChanged();
        } else {
            Snackbar.make(ButterKnife.findById(this, R.id.root), "No favorite movies yet!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private String getValueFor(Cursor c, String column) {
        return c.getString(c.getColumnIndex(column));
    }

    @OnClick(R.id.sort)
    void displayDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle("Sort by")
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setSingleChoiceItems(R.array.options, selected,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, which + " selected");
                                if (which != selected) {
                                    selected = which;
                                    switch (selected) {
                                        case 0:
                                            sortBy = getResources().getString(R.string.POPULARITY);
                                            page = 1;
                                            movies.clear();
                                            fetch();
                                            break;
                                        case 1:
                                            sortBy = getResources().getString(R.string.RATING);
                                            page = 1;
                                            movies.clear();
                                            fetch();
                                            break;
                                        case 2:
                                            dialog.dismiss();
                                            loadFavouriteMovies();
                                            break;
                                        default:
                                            dialog.dismiss();
                                    }
                                }

                                dialog.dismiss();
                            }
                        })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            displayDialog();
        }
        return super.onOptionsItemSelected(item);
    }

}