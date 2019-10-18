package com.richez.ladylyn.buscommuterapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.ebanx.swipebtn.OnStateChangeListener;
import com.ebanx.swipebtn.SwipeButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.richez.ladylyn.buscommuterapp.Common.Common;
import com.richez.ladylyn.buscommuterapp.Model.User;

public class ShowReminderActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    MediaPlayer mediaPlayer;
    CountDownTimer countDownTimer;
    private int counter = 15;
    TextView countdowm;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public static final String MyPreferences = "MyPreferences";
    Ringtone ringtone;

    Handler handler, handler2;
    Runnable runnable2;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_show_reminder2);
        sharedPreferences = this.getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        mediaPlayer = MediaPlayer.create(this, R.raw.voicealertcom);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(1.0f, 1.0f);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        handler = new Handler();
        handler2 = new Handler();

        runnable2 = new Runnable() {
            @Override
            public void run() {
                vibrator.vibrate(500);

                handler2.postDelayed(this, 4000);
            }
        };
        SwipeButton swipeButton = (SwipeButton) findViewById(R.id.swipe_btn);
        swipeButton.setOnStateChangeListener(new OnStateChangeListener() {
            @Override
            public void onStateChange(boolean active) {

                finish();
            }
        });
        countdowm = (TextView) findViewById(R.id.countdown);
        countDownTimer = new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                countdowm.setText("Dialog will close in " + String.valueOf(counter) + " seconds");
                counter--;
            }

            @Override
            public void onFinish() {
                finish();
            }
        }.start();
    }

    public boolean isVoiceReminderEnabled() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getBoolean("check_voice", true);
    }

    public boolean isVibrateEnabled() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getBoolean("check_vibrate", true);
    }

    public boolean isRingtoneEnabled() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getBoolean("check_ringtone", true);
    }

    @Override
    protected void onPause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }

        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }

        handler2.removeCallbacks(runnable2);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }

        handler2.removeCallbacks(runnable2);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isRingtoneEnabled()) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String path = preferences.getString("ringtone_sound", "");
            if (!path.isEmpty()) {
                ringtone = RingtoneManager.getRingtone(this, Uri.parse(path));
                ringtone.play();
            }
        }
        if (isVoiceReminderEnabled()) {

            if (mediaPlayer != null) {
                mediaPlayer.start();
            }
        }
        if (isVibrateEnabled()) {
            runnable2.run();

        }
    }
}
