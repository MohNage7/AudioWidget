package mohnage7.audiowidget.widget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mohnage7.audiowidget.data.Audio;

public class AudioService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {

    public static final String ACTION_PLAY = "play";
    public static final String ACTION_PAUSE = "pause";
    public static final String ACTION_STOP = "stop";
    public static final String ACTION_SHUFFLE = "shuffle";
    public static final String ACTION = "action";
    private static final int DEFAULT_POSITION = 0;
    private static final String TAG = AudioService.class.getSimpleName();
    Random random = new Random();
    private MediaPlayer mediaPlayer;
    private int resumePosition = DEFAULT_POSITION;
    private AudioManager audioManager;
    private List<Audio> audioList;
    private Audio audio;

    @Override
    public void onCreate() {
        super.onCreate();
        audioList = getAudioList();
        initMediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra(ACTION) && audioList != null && !audioList.isEmpty()) {
            //Request audio focus
            if (!requestAudioFocus()) {
                //Could not gain focus
                stopSelf();
            }
            String action = intent.getStringExtra(ACTION);
            Log.d(TAG, action);
            switch (action) {
                case ACTION_PLAY:
                    if (resumePosition == DEFAULT_POSITION) {
                        audio = getRandomAudio(audioList);
                        prepareMediaPlayerWith(audio);
                    } else {
                        resumeMedia();
                    }
                    break;
                case ACTION_PAUSE:
                    pauseMedia();
                    break;
                case ACTION_STOP:
                    audio = null;
                    stopMedia();
                    stopSelf();
                    break;
                case ACTION_SHUFFLE:
                    stopMedia();
                    audio = getRandomAudio(audioList);
                    prepareMediaPlayerWith(audio);
                    break;
                default:
                    stopSelf();
            }
            updateWidget(audio, action);
        } else {
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }


    private Audio getRandomAudio(List<Audio> audioList) {
        return audioList.get(random.nextInt(audioList.size()));
    }

    private List<Audio> getAudioList() {
        final List<Audio> tempAudioList = new ArrayList<>();
        try {
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String[] projection = {MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.ALBUM, MediaStore.Audio.ArtistColumns.ARTIST,};
            String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
            String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
            Cursor c = getApplicationContext().getContentResolver().query(uri, projection, selection, null, sortOrder);

            if (c != null) {
                while (c.moveToNext()) {
                    Audio audioModel = createAudioModel(c);
                    tempAudioList.add(audioModel);
                }
                c.close();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Please enable storage permission from AudioActivity or settings.");
            Log.e(TAG, e.getLocalizedMessage());
        }
        return tempAudioList;
    }

    private Audio createAudioModel(Cursor c) {
        String path = c.getString(0);
        String album = c.getString(1);
        String artist = c.getString(2);
        String name = path.substring(path.lastIndexOf("/") + 1);
        Audio audioModel = new Audio();
        audioModel.setName(name);
        audioModel.setAlbum(album);
        audioModel.setArtist(artist);
        audioModel.setPath(path);
        return audioModel;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        // audio track is finished so execute the following
        stopMedia();
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        stopSelf();
        return false;
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        playMedia();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        //Invoked when the audio focus of the system is updated.
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mediaPlayer == null) {
                    initMediaPlayer();
                } else if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
                updateWidget(null, ACTION_STOP);
                stopSelf();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time and resume later.
                if (mediaPlayer.isPlaying()) {
                    pauseMedia();
                    updateWidget(null, ACTION_PAUSE);
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Short period interruption like "Notification" ,
                // it's ok to keep playing but with low volume
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.setVolume(0.1f, 0.1f);
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + focusChange);
        }

    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        //Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    private void updateWidget(Audio audio, String action) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, AudioWidget.class));
        //Now update all widgets, We also can update our widget via Broadcast.
        AudioWidget.updateAppWidgets(this, appWidgetManager, audio,action, appWidgetIds);
    }

    private void prepareMediaPlayerWith(Audio audio) {
        String mediaFile = audio.getPath();
        if (mediaFile != null && !mediaFile.isEmpty()) {
            try {
                //Reset so that the MediaPlayer is not pointing to another data source
                mediaPlayer.reset();
                // Set the data source to the mediaFile location
                mediaPlayer.setDataSource(mediaFile);
            } catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage());
                stopSelf();
            }
            mediaPlayer.prepareAsync();
        } else {
            stopSelf();
        }
    }

    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    private void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void removeAudioFocus() {
        if (audioManager != null)
            audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        removeAudioFocus();
    }
}
