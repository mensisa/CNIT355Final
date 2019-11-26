package edu.purdue.zhan3050.cnit355final;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Music Player
 * Read all music from external storage and show all information of the audio file
 */
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    // store the results that fetched from system database
    private Cursor mCursor;
    // store the information in mCursor into list with a hashMap
    private List<Map<String, String>> List_map;
    // ListView in layout
    private ListView MusicListView;
    // A ContentResolver that access to system database
    private ContentResolver contentResolver;
    // A list to store MusicInfo object
    private List<MusicInfo> musicInfos;
    // SimpleAdapter
    private SimpleAdapter simpleAdapter;
    // requestCode for permission
    private final static int STORGE_REQUEST = 1;
    // check if the music is playing
    private boolean isPlyer = false;
    // A intent to start new activity
    private Intent intent;

    @Override
    protected void onResume() {
        super.onResume();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MusicListView = (ListView) findViewById(R.id.MusicListView);
        int check = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (check != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORGE_REQUEST);
        } else {
            init();
        }

        // start intent
        intent = new Intent();
        intent.setClass(MainActivity.this, MusicPlayerService.class);

        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // when onDestroy stop the intent
        stopService(intent);
    }

    /*
     * initiate ListView
     */
    private void init() {
        // use ContentResolver connect to system database
        contentResolver = getContentResolver();

        // fetch music information from external storage using cursor
        mCursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID}, null, null, null);

        List_map = new ArrayList<Map<String, String>>();
        musicInfos = new ArrayList<>();
        for (int i = 0; i < mCursor.getCount(); i++) {
            Map<String, String> map = new HashMap<>();
            MusicInfo musicInfo = new MusicInfo();

            // move the cursor to the next
            mCursor.moveToNext();

            // store musicInfo into List<MusicInfo>中
            musicInfo.set_id(mCursor.getInt(0));
            musicInfo.setTitle(mCursor.getString(1));
            musicInfo.setAlbum(mCursor.getString(2));
            musicInfo.setArtist(mCursor.getString(3));
            musicInfo.setDuration(mCursor.getInt(4));
            musicInfo.setMusicName(mCursor.getString(5));
            musicInfo.setSize(mCursor.getInt(6));
            musicInfo.setData(mCursor.getString(7));
            // store data into List<Map<String ,String>>
            // get the music image
            String MusicImage = getAlbumImage(mCursor.getInt(8));
            // check if the music has a image
            if (MusicImage == null) {
                // if null, use a default image
                map.put("image", String.valueOf(R.mipmap.timg));
                musicInfo.setAlbum_id(String.valueOf(R.mipmap.timg));
            } else {
                // if not null set the music
                map.put("image", MusicImage);
                musicInfo.setAlbum_id(MusicImage);
            }
            // musicInfo.setAlbum_id(mCursor.getInt(8));
            musicInfos.add(musicInfo);


            map.put("name", mCursor.getString(5));
            // convert the size of the music from byte to MB
            Float size = (float) (mCursor.getInt(6) * 1.0 / 1024 / 1024);
            BigDecimal b = new BigDecimal(size);
            Float f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
            map.put("size", f1.toString() + "MB");
            List_map.add(map);


        }
        // instantiate SimpleAdapter
        simpleAdapter = new SimpleAdapter(this, List_map, R.layout.music_adapte_view,
                new String[]{"image", "name", "size"}, new int[]{R.id.MusicImage,
                R.id.MusicName, R.id.MusicSize});
        // set adapter for ListView
        MusicListView.setAdapter(simpleAdapter);
        // set onClick listener for MusicList item
        MusicListView.setOnItemClickListener(this);
    }

    /*
     * get the image of the music
     */
    private String getAlbumImage(int album_id) {
        String UriAlbum = "content://media/external/audio/albums";
        String projection[] = new String[]{"album_art"};
        // use cursor to fetch album image
        Cursor cursor = contentResolver.query(Uri.parse(UriAlbum + File.separator + Integer.toString(album_id)),
                projection, null, null, null);
        String album = null;
        if (cursor.getCount() > 0 && cursor.getColumnCount() > 0) {
            cursor.moveToNext();
            album = cursor.getString(0);
        }
        // close cursor
        cursor.close();
        return album;
    }

    /**
     * result for permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORGE_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // initiate the app
                    init();
                    System.out.println("Get permissions");
                } else {
                    System.out.println("Didn't get permissions");
                }
                break;
        }

    }

    /**
     * perform actions when item clicked
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String MusicData = musicInfos.get(position).getData();
        System.out.println("THE MUSIC DATA IS " + MusicData);

        // send the position of the clicked item to MusicPlay, get MusicInfo and play music
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putSerializable("musicinfo", (Serializable) getMusicInfos());
        Intent intent = new Intent();
        intent.putExtras(bundle);
        intent.setClass(this, MusicPlay.class);
        startActivity(intent);
    }

    // Method to get MusicInfo
    public List<MusicInfo> getMusicInfos() {
        return musicInfos;
    }
}
