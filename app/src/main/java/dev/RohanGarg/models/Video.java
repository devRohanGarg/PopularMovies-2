package dev.RohanGarg.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Rohan Garg on 12-02-2016.
 */
public class Video implements Parcelable {

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };
    public String name, id;

    public Video(String name, String id) {
        this.name = name;
        this.id = id;
    }

    protected Video(Parcel in) {
        name = in.readString();
        id = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(id);
    }
}