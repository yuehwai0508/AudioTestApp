package com.example.testapp2java;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Equalizer mEqualizer;
    private short[] mbLimit = new short[2];
    private MediaPlayer mediaPlayer;
    private AudioManager mAudioManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = 0;

        result = mAudioManager.requestAudioFocus((new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)).build());
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //create player
            mediaPlayer = MediaPlayer.create(this, R.raw.play_file);
            //start playing
            System.out.println("OnCreate method, OnCreate player created");
//            mediaPlayer.start();
            //listen for completion of playing
            mediaPlayer.setOnCompletionListener(mCompletionListener);
        }

        mEqualizer = new Equalizer(0 ,
                mediaPlayer.getAudioSessionId());
        mEqualizer.setEnabled(true);

        setUpEqualizerControls();
        // Volume Band 9
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBarBand16);
        TextView textView = (TextView) findViewById(R.id.textViewBand16);
        textView.setText("Volume");
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        seekBar.setMax(maxVolume);
        seekBar.setProgress(currVolume);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,i,AudioManager.FLAG_SHOW_UI);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do Nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do Nothing
            }
        });
        // Volume Band9
        Button buttonApply = findViewById(R.id.buttonApply);
        buttonApply.setOnClickListener(view -> applyEqualizerSettings());

        Button buttonPlay = findViewById(R.id.buttonPlay);
        buttonPlay.setOnClickListener(view -> pausePlay());

    }

    private void setUpEqualizerControls() {
        // Add Vertical SeekBars for each equalizer band
        int numBands = mEqualizer.getNumberOfBands();
        for (short band = 0; band < numBands; band++) {
            TextView textView = findViewById(getResources().getIdentifier(
                    "textViewBand" + (band + 1), "id", getPackageName()));
            SeekBar seekBar = findViewById(getResources().getIdentifier(
                    "seekBarBand" + (band + 1), "id", getPackageName()));

            // Set up SeekBar properties
            mbLimit = mEqualizer.getBandLevelRange();
            seekBar.setMin(mbLimit[0]);
            seekBar.setMax(mbLimit[1]);
            seekBar.setProgress(mEqualizer.getBandLevel(band));

            // Set up TextView labels
            textView.setText("Band " + (band + 1));

            // Set up SeekBar change listener
            short finalBand = band;
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                    mEqualizer.setBandLevel(finalBand, (short) (progress - 50));
//                    System.out.println("Band " + (finalBand + 1) + ": " + (progress));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    System.out.println("Band " + (finalBand + 1) + ": " + seekBar.getProgress() + " Touch Tracking Start");
                    // Not needed for this example
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    System.out.println("Band " + (finalBand + 1) + ": " + seekBar.getProgress() + " Touch Tracking STOP");
                    mEqualizer.setBandLevel(finalBand, (short) (seekBar.getProgress()));
                    // Not needed for this example
                }
            });
        }
    }

    private void applyEqualizerSettings() {
//         Apply Equalizer settings to your audio player
//         You need to connect this with your audio playback logic
//         Print the band levels here
        int numBands = mEqualizer.getNumberOfBands();
        short level = 0;
        for (short band = 0; band < numBands; band++) {
            level = mEqualizer.getBandLevel(band);
            System.out.println("Band " + (band + 1) + ": " + level);
            mEqualizer.setBandLevel(band, level);
        }
    }

    private void pausePlay() {
        if (mediaPlayer == null){
            System.out.println("mediaPlayer NULL");
            return;
        }
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        else {
            mediaPlayer.start();
        }
    }
    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
//            releaseMediaPlayer();
            mediaPlayer.start();
            System.out.println("Start over upon completion");
        }
    };
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                mediaPlayer.pause();
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                mediaPlayer.start();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
//                length = mediaPlayer.getCurrentPosition();
                System.out.println("loss focus, loss of focus");
                releaseMediaPlayer();
            }
        }
    };
    private void releaseMediaPlayer() {
        // If the media player is not null, then it may be currently playing a sound.
        if (mediaPlayer != null) {
            // Regardless of the current state of the media player, release its resources
            // because we no longer need it.
            mediaPlayer.release();
            // Set the media player back to null. For our code, we've decided that
            // setting the media player to null is an easy way to tell that the media player
            // is not configured to play an audio file at the moment.
            mediaPlayer = null;
            mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) mediaPlayer.release();
    }
}