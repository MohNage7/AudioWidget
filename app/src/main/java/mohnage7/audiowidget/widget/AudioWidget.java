package mohnage7.audiowidget.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.View;
import android.widget.RemoteViews;

import mohnage7.audiowidget.R;
import mohnage7.audiowidget.data.Audio;

import static mohnage7.audiowidget.widget.AudioService.ACTION_PAUSE;
import static mohnage7.audiowidget.widget.AudioService.ACTION_PLAY;
import static mohnage7.audiowidget.widget.AudioService.ACTION_SHUFFLE;
import static mohnage7.audiowidget.widget.AudioService.ACTION_STOP;

/**
 * Implementation of App Widget functionality.
 */
public class AudioWidget extends AppWidgetProvider {

    private static final int PLAY_REQUEST_CODE = 0;
    private static final int PAUSE_REQUEST_CODE = 1;
    private static final int STOP_REQUEST_CODE = 2;
    private static final int SHUFFLE_REQUEST_CODE = 3;
    private static final String FILE_EXTENSION = ".mp3";

    private static void setPendingIntent(Context context, int appWidgetId, RemoteViews remoteViews, String actionPlay) {
        Intent intent = new Intent(context, AudioService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetId);
        intent.putExtra(AudioService.ACTION, actionPlay);
        PendingIntent pendingIntent;
        switch (actionPlay) {
            case ACTION_PLAY:
                pendingIntent = PendingIntent.getService(context, PLAY_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.playBtn, pendingIntent);
                break;
            case ACTION_PAUSE:
                pendingIntent = PendingIntent.getService(context, PAUSE_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.pauseBtn, pendingIntent);
                break;
            case ACTION_SHUFFLE:
                pendingIntent = PendingIntent.getService(context, SHUFFLE_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.shuffleBtn, pendingIntent);
                break;
            case ACTION_STOP:
                pendingIntent = PendingIntent.getService(context, STOP_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.stopBtn, pendingIntent);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + actionPlay);
        }
    }

    public static void updateAppWidgets(Context context, AppWidgetManager appWidgetManager,
                                        Audio audio, String action, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.audio_widget);

            // set pending intent to be fired when user interacts with the widget control buttons.
            setPendingIntent(context, appWidgetId, remoteViews, ACTION_PLAY);
            setPendingIntent(context, appWidgetId, remoteViews, ACTION_PAUSE);
            setPendingIntent(context, appWidgetId, remoteViews, ACTION_SHUFFLE);
            setPendingIntent(context, appWidgetId, remoteViews, ACTION_STOP);

            // update views
            fillViewsWithData(context, audio, remoteViews);
            updateControlViewsAccordingToAction(action, remoteViews);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }


    private static void fillViewsWithData(Context context, Audio audio, RemoteViews remoteViews) {
        if (audio != null) {
            remoteViews.setTextViewText(R.id.audioNameTv, audio.getName().replace(FILE_EXTENSION, ""));
            remoteViews.setTextViewText(R.id.extraInfoTv, String.format("%s - %s", audio.getArtist(), audio.getAlbum()));
            setAudioAlbumArt(audio.getPath(), remoteViews);
        } else {
            remoteViews.setImageViewResource(R.id.audioIv, R.drawable.ic_audiotrack);
            remoteViews.setTextViewText(R.id.audioNameTv, context.getString(R.string.app_name));
            remoteViews.setTextViewText(R.id.extraInfoTv, context.getString(R.string.author));
        }
    }

    private static void updateControlViewsAccordingToAction(String action, RemoteViews remoteViews) {
        if (action != null) {
            switch (action) {
                case ACTION_PLAY:
                case AudioService.ACTION_SHUFFLE:
                    remoteViews.setViewVisibility(R.id.playBtn, View.GONE);
                    remoteViews.setViewVisibility(R.id.pauseBtn, View.VISIBLE);
                    break;
                case ACTION_PAUSE:
                case AudioService.ACTION_STOP:
                    remoteViews.setViewVisibility(R.id.playBtn, View.VISIBLE);
                    remoteViews.setViewVisibility(R.id.pauseBtn, View.GONE);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + action);
            }
        }
    }

    /**
     * this method retrieves album's art and sets it to @audioIv or sets default icon.
     *
     * @param remoteViews the target view to be updated.
     */
    private static void setAudioAlbumArt(String path, RemoteViews remoteViews) {
        Bitmap bitmap = getAlbumImage(path);
        if (bitmap!=null) {
            remoteViews.setBitmap(R.id.audioIv, "setImageBitmap", bitmap);
        }else {
            remoteViews.setImageViewResource(R.id.audioIv, R.drawable.ic_audiotrack);
        }
    }

    /**
     * retrieves album's art by image's path.
     *
     * @param path of the audio file that we want to retrieve it's art.
     * @return bitmap with the targeted art.
     */
    private static Bitmap getAlbumImage(String path) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        byte[] data = mediaMetadataRetriever.getEmbeddedPicture();
        // convert the byte array to a bitmap
        if (data != null) {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return null;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
            updateAppWidgets(context, appWidgetManager, null, null, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        stopService(context);
    }

    private void stopService(Context context) {
        Intent playIntent = new Intent(context, AudioService.class);
        context.stopService(playIntent);
    }
}

