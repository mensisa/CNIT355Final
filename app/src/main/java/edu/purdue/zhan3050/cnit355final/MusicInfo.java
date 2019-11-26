package edu.purdue.zhan3050.cnit355final;

import java.io.Serializable;

/**
 * A object that store music information
 */

public class MusicInfo implements Serializable {
    private int _id = -1;       //music id
    private int duration = -1;   //music duration
    private String artist = null;    //music artist
    private String musicName = null; // music name
    private String album = null;   // music album
    private String title = null;  // music title
    private int size;   //music size in byte
    private String data;  //path for music file
    private String album_id; //album picture for the music


    public String getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(String album_id) {
        this.album_id = album_id;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int get_id() {
        return _id;
    }

    public int getDuration() {
        return duration;
    }

    public String getArtist() {
        return artist;
    }

    public String getMusicName() {
        return musicName;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}
