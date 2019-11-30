package edu.purdue.zhan3050.cnit355final;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;


/**
 * @author Yi Zhang
 * This class is used to handle audio control
 */

public class AudioControl extends RelativeLayout implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private Context mContext;
    private ImageView imagePlay;    // play or pause button
    private ImageView imageNext;    // next song button
    private ImageView imagePre;     // previous song button
    private TextView currentText;   //current time textView
    private TextView totalText;     //total time textView
    private SeekBar seekBar;        //seekBar
    private static final int DIP_80 = 80;
    private static final int DIP_10 = 10;
    private int layoutViewId = 0x7F24FF00;

    /*
     * Variables related to Audio Control
     */
    private MediaPlayer mediaPlayer;
    private int currentTime = 0;
    private int durationTime = 0;
    public static boolean isPause = false;
    public static boolean SEEK_BAR_STATE = true;

    /*
     * Default constructor
     */
    public AudioControl(Context context) {
        this(context, null);
    }

    public AudioControl(Context context, AttributeSet set) {
        super(context, set);
        mContext = context;
        init();
    }

    /*
     * use Java code to setup relative layout
     */
    private RelativeLayout.LayoutParams getParams() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(DIP_80, DIP_80);
        params.setMargins(10, 0, 0, 0);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        return params;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        RelativeLayout.LayoutParams params = getParams();
        imagePre = new ImageView(mContext);
        imagePre.setLayoutParams(params);
        imagePre.setId(layoutViewId + 1);
        imagePre.setOnClickListener(this);
        imagePre.setImageResource(R.mipmap.music_pre);

        RelativeLayout.LayoutParams params4 = getParams();
        imagePlay = new ImageView(mContext);
        params4.addRule(RelativeLayout.RIGHT_OF, imagePre.getId());
        imagePlay.setLayoutParams(params4);
        imagePlay.setId(layoutViewId);
        imagePlay.setOnClickListener(this);

        RelativeLayout.LayoutParams params5 = getParams();
        imageNext = new ImageView(mContext);
        params5.addRule(RelativeLayout.RIGHT_OF, imagePlay.getId());
        imageNext.setId(layoutViewId + 2);
        imageNext.setLayoutParams(params5);
        imageNext.setOnClickListener(this);
        imageNext.setImageResource(R.mipmap.music_next);

        currentText = newTextView(mContext, layoutViewId + 4);
        RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) currentText.getLayoutParams();
        params1.setMargins(40, 0, 30, 0);
        params1.addRule(RelativeLayout.RIGHT_OF, imageNext.getId());
        currentText.setLayoutParams(params1);

        totalText = newTextView(mContext, layoutViewId + 5);
        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) totalText.getLayoutParams();
        params2.setMargins(DIP_10, 0, DIP_10, 0);
        params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        totalText.setLayoutParams(params2);

        seekBar = new SeekBar(mContext);
        RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        params3.addRule(RelativeLayout.CENTER_IN_PARENT);
        params3.addRule(RelativeLayout.RIGHT_OF, currentText.getId());
        params3.addRule(RelativeLayout.LEFT_OF, totalText.getId());
        seekBar.setLayoutParams(params3);
        seekBar.setMax(100);
        seekBar.setFocusable(true);
        seekBar.setId(layoutViewId + 6);
        seekBar.setMinimumHeight(100);
        seekBar.setThumbOffset(0);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    seekBar.setFocusable(true);
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    seekBar.requestFocus();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    seekBar.setFocusable(false);
                }
                return false;
            }
        });
    }

    /*
     * customized TextView
     */
    private TextView newTextView(Context context, int id) {
        TextView textView = new TextView(context);
        textView.setId(id);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        textView.setLayoutParams(params);
        return textView;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // remove all views
        removeAllViews();
        // set icon for imagePlay based on the music playing status
        if (currentTime == 0 || isPause) {
            imagePlay.setImageResource(R.mipmap.music_play);
        } else {
            imagePlay.setImageResource(R.mipmap.music_pause);
        }
        /*
         * get and set the current time and total duration
         */
        try {
            currentTime = mediaPlayer.getCurrentPosition();
            durationTime = mediaPlayer.getDuration();
        } catch (Exception e) {
            currentTime = 0;
            durationTime = 0;
        }
        currentText.setText(timeToStr(currentTime));
        totalText.setText(timeToStr(durationTime));

        seekBar.setProgress((currentTime == 0) ? 0 : currentTime * 100 / durationTime);
        addView(imagePre);
        addView(imagePlay);
        addView(imageNext);
        addView(currentText);
        addView(seekBar);
        addView(totalText);
//        addView(imageSet);
        //System.out.println("ON AUDIO CONTROL ONLAYOUT");
    }

    /*
     * convert milliseconds to string MM:SS
     */
    private String timeToStr(int time) {
        String timeStr;
        int second = time / 1000;
        int minute = second / 60;
        second = second - minute * 60;
        if (minute > 9) {
            timeStr = String.valueOf(minute) + ":";
        } else {
            timeStr = "0" + String.valueOf(minute) + ":";
        }
        if (second > 9) {
            timeStr += String.valueOf(second);
        } else {
            timeStr += "0" + String.valueOf(second);
        }

        return timeStr;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public void setCurrentTime(int currentTime) {
        this.currentTime = currentTime;
        invalidate();
        requestLayout();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == imagePlay.getId()) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                imagePlay.setImageResource(R.mipmap.music_pause);
                isPause = true;
            } else {
                if (currentTime == 0) {
                    mediaPlayer.start();
                } else {
                    mediaPlayer.start();
                    mediaPlayer.seekTo(currentTime);
                }
                isPause = false;
                imagePlay.setImageResource(R.mipmap.music_play);
            }
            Intent intentPlay = new Intent("MUSCIPLAY_BROADCAST");
            intentPlay.putExtra("AudioControl", "AudioControl");
            mContext.sendBroadcast(intentPlay);

        }
        if (v.getId() == imagePre.getId()) {
            Intent intent = new Intent();
            intent.setAction("MUSCI_SWITCH");
            intent.putExtra("PRE", "PRE");
            intent.putExtra("NEXT", "");
            mContext.sendBroadcast(intent);

        }
        if (v.getId() == imageNext.getId()) {
            Intent intent = new Intent();
            intent.setAction("MUSCI_SWITCH");
            intent.putExtra("NEXT", "NEXT");
            intent.putExtra("PRE", "");
            mContext.sendBroadcast(intent);
        }

        invalidate();
        requestLayout();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if (fromUser && SEEK_BAR_STATE) {
            int time = seekBar.getProgress() * durationTime / 100;
            mediaPlayer.seekTo(time);
            invalidate();
            requestLayout();

        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

        SEEK_BAR_STATE = false;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        SEEK_BAR_STATE = true;
        onProgressChanged(seekBar, seekBar.getProgress(), true);
    }
}
