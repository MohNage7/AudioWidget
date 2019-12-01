package mohnage7.audiowidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class AudioWidget extends AppWidgetProvider {

    private static final int PLAY_REQUEST_CODE = 0;
    private static final int STOP_REQUEST_CODE = 1;
    private static final int SHUFFLE_REQUEST_CODE = 2;

    private static void setPendingIntent(Context context, int appWidgetId, RemoteViews remoteViews, String actionPlay, int i, int p) {
        Intent playIntent = new Intent(context, AudioService.class);
        playIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetId);
        playIntent.putExtra(AudioService.ACTION, actionPlay);
        PendingIntent playPendingIntent = PendingIntent.getService(context, i, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(p, playPendingIntent);
    }

    public static void updateAppWidgets(Context context, AppWidgetManager appWidgetManager,
                                        Audio audio, String action, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.audio_widget);

            setPendingIntent(context, appWidgetId, remoteViews, AudioService.ACTION_PLAY, PLAY_REQUEST_CODE, R.id.playBtn);
            setPendingIntent(context, appWidgetId, remoteViews, AudioService.ACTION_SHUFFLE, SHUFFLE_REQUEST_CODE, R.id.shuffleBtn);
            setPendingIntent(context, appWidgetId, remoteViews, AudioService.ACTION_STOP, STOP_REQUEST_CODE, R.id.stopBtn);

            if (audio != null) {
                remoteViews.setTextViewText(R.id.audioNameTv, audio.getName().replace(".mp3", ""));
                remoteViews.setTextViewText(R.id.extraInfoTv, String.format("%s - %s", audio.getArtist(), audio.getAlbum()));
                setAudioAlbumArt(audio, remoteViews);
            }
            if (action!=null){
                switch (action){
                    case AudioService.ACTION_PLAY:
                        remoteViews.setViewVisibility(R.id.playBtn, View.GONE);
                        remoteViews.setViewVisibility(R.id.pauseBtn, View.VISIBLE);
                        break;
                    case AudioService.ACTION_STOP:
                        remoteViews.setViewVisibility(R.id.playBtn, View.VISIBLE);
                        remoteViews.setViewVisibility(R.id.pauseBtn, View.GONE);
                        break;
                    case AudioService.ACTION_SHUFFLE:
                        remoteViews.setViewVisibility(R.id.playBtn, View.GONE);
                        remoteViews.setViewVisibility(R.id.pauseBtn, View.VISIBLE);
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + action);
                }
            }
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    private static void setAudioAlbumArt(Audio audio, RemoteViews remoteViews) {
        Bitmap bitmap = getAlbumImage(audio.getPath());
        if (bitmap!=null) {
            remoteViews.setBitmap(R.id.audioIv, "setImageBitmap", bitmap);
        }else {
            remoteViews.setImageViewResource(R.id.audioIv, R.drawable.ic_audiotrack);
        }
    }

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

}

