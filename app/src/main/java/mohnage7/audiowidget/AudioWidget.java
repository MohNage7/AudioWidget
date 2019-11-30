package mohnage7.audiowidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
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

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                       Audio audio, int appWidgetId) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.audio_widget);

        setPendingIntent(context, appWidgetId, remoteViews, AudioService.ACTION_PLAY, PLAY_REQUEST_CODE, R.id.playBtn);
        setPendingIntent(context, appWidgetId, remoteViews, AudioService.ACTION_SHUFFLE, SHUFFLE_REQUEST_CODE, R.id.shuffleBtn);
        setPendingIntent(context, appWidgetId, remoteViews, AudioService.ACTION_STOP, STOP_REQUEST_CODE, R.id.stopBtn);

        if (audio != null) {
            Log.e("Widget", audio.getName());
        }


        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, null, appWidgetId);
        }
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

