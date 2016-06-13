package dev.RohanGarg.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
    public String posterImgURL, overview, releaseDate, title, backDropImgURL, popularity, voteCount, rating;
    public int id;

    public Movie(int id, String posterImgURL, String overview, String releaseDate, String title, String backDropImgURL, String popularity, String voteCount, String rating) {

        this.id = id;
        this.title = title;
        this.overview = overview;
        this.backDropImgURL = backDropImgURL;
        this.rating = rating;
        this.posterImgURL = posterImgURL;
        this.releaseDate = releaseDate;
        this.popularity = popularity;
        this.voteCount = voteCount;

    }

    protected Movie(Parcel in) {
        posterImgURL = in.readString();
        overview = in.readString();
        releaseDate = in.readString();
        title = in.readString();
        backDropImgURL = in.readString();
        popularity = in.readString();
        voteCount = in.readString();
        rating = in.readString();
        id = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(posterImgURL);
        dest.writeString(overview);
        dest.writeString(releaseDate);
        dest.writeString(title);
        dest.writeString(backDropImgURL);
        dest.writeString(popularity);
        dest.writeString(voteCount);
        dest.writeString(rating);
        dest.writeInt(id);
    }
}