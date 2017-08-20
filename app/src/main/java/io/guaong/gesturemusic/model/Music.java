package io.guaong.gesturemusic.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by 关桐 on 2017/8/1.
 *
 */

public class Music implements Parcelable{

    private String title;

    private String artist;

    private long size;

    private Uri uri;

    private int id;

    private long duration;

    private String time;

    public Music(){}

    public Music(Parcel in) {
        title = in.readString();
        artist = in.readString();
        size = in.readLong();
        uri = in.readParcelable(Uri.class.getClassLoader());
        id = in.readInt();
        duration = in.readLong();
        time = in.readString();
    }

    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel in) {
            return new Music(in);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = Uri.parse(uri);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeLong(size);
        dest.writeParcelable(uri, flags);
        dest.writeInt(id);
        dest.writeLong(duration);
        dest.writeString(time);
    }
}
