package io.guaong.gesturemusic.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

import io.guaong.gesturemusic.model.Music;

/**
 * Created by 关桐 on 2017/8/1.
 *
 */

public class MusicUtil {

    public static ArrayList<Music> getMusicList(Context context){
        final ArrayList<Music> musicList = new ArrayList<>();
        final ContentResolver contentResolver= context.getContentResolver();
        final Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor != null && cursor.moveToFirst()){
            String title;
            String artist;
            long size;
            String uri;
            int id;
            long duration;
            String time;
            while (!cursor.isAfterLast()){
                title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                int flag = cutIndex(title);
                if (flag != -1){
                    artist = title.substring(0, flag);
                    title = title.substring(flag + 2, title.length());
                }
                size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                uri = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                String s = (int)((duration / 1000) % 60) + "";
                if ((int)((duration / 1000) % 60) < 10){
                    s = 0 + s;
                }
                time = (int)(duration / 60000) + ":" + s ;
                if (isLegal(title, artist, size, duration)){
                    Music music = new Music();
                    music.setTitle(title);
                    music.setArtist(artist);
                    music.setUri(uri);
                    music.setDuration(duration);
                    music.setId(id);
                    music.setSize(size);
                    music.setTime(time);
                    musicList.add(music);
                }
                cursor.moveToNext();

            }
            cursor.close();
        }
        return musicList;
    }

    public static boolean haveMusic(List<Music> musicList){
        return musicList != null;
    }

    private static int cutIndex(String str){
        return str.indexOf("-");
    }

    private static boolean isLegal(String title, String artist, long size, long duration){
        final boolean isLegal;
        isLegal = (!"".contains(title))
                && (size > 1024)
                && (!"".contains(artist))
                && (duration > 3600);
        return isLegal;
    }
}
