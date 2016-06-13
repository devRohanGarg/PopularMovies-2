package dev.RohanGarg.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import dev.RohanGarg.MovieDetailActivity;
import dev.RohanGarg.MovieDetailFragment;
import dev.RohanGarg.MovieListActivity;
import dev.RohanGarg.R;
import dev.RohanGarg.models.Movie;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    public ArrayList<Movie> movies;
    MovieListActivity mContext;

    public RecyclerAdapter(MovieListActivity context, ArrayList<Movie> movies) {
        this.mContext = context;
        this.movies = movies;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.title.setText(movies.get(position).title);
        String r = String.format("%.1f", Float.parseFloat(movies.get(position).rating));
        String p = String.format("%.0f", Float.parseFloat(movies.get(position).popularity)) + "%";
        holder.rating.setText(r);
        holder.popularity.setText(p);
        Picasso.with(mContext).load(movies.get(position).posterImgURL).placeholder(R.drawable.ui_shadow).error(R.drawable.ui_shadow).into(holder.imageView);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MovieListActivity.mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putParcelable("movie", movies.get(position));
                    MovieDetailFragment fragment = new MovieDetailFragment();
                    fragment.setArguments(arguments);
                    mContext.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.movie_detail_container, fragment)
                            .commit();
                } else {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, MovieDetailActivity.class);
                    intent.putExtra("movie", movies.get(position));
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        @Bind(R.id.cardTitle)
        TextView title;
        @Bind(R.id.cardRating)
        TextView rating;
        @Bind(R.id.cardPopularity)
        TextView popularity;
        @Bind(R.id.cardImage)
        ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            ButterKnife.bind(this, view);
        }

    }
}