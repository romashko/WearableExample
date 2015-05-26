package com.app.testwear.wearabletest;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;


public class MainActivity extends Activity implements View.OnClickListener {

    private final static String TAG = MainActivity.class.getSimpleName();
    private final static String COUNT_KEY = "COUNT";

    private GoogleApiClient googleApiClient;

    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        count = 1;

        setContentView(R.layout.activity_main);
        findViewById(R.id.notification_button).setOnClickListener(this);
        findViewById(R.id.notification_action_button).setOnClickListener(this);
        findViewById(R.id.notification_page_button).setOnClickListener(this);
        findViewById(R.id.send_data).setOnClickListener(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "onConnected: " + connectionHint);
                        // Now you can use the Data Layer API
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(TAG, "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                    }
                })
                        // Request access only to the Wearable API
                .addApi(Wearable.API)
                .build();

    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        Notification notification;
        switch (v.getId()) {
            case R.id.notification_button:
                notification = getSimpleNotification();
                showNotification(notification);
                break;
            case R.id.notification_action_button:
                notification = getNotificationWithAction();
                showNotification(notification);
                break;
            case R.id.notification_page_button:
                notification = getNotificationWithPage();
                showNotification(notification);
                break;
            case R.id.send_data:
                PutDataMapRequest dataMap = PutDataMapRequest.create("/count");
                dataMap.getDataMap().putInt(COUNT_KEY, count++);
                PutDataRequest request = dataMap.asPutDataRequest();
                PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                        .putDataItem(googleApiClient, request);
                break;
            default:
                notification = getSimpleNotification();
        }
    }

    private void showNotification(Notification notification) {
        int notificationId = 001;

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notification);
    }

    private Notification getSimpleNotification() {
        // Build intent for notification content
        Intent viewIntent = new Intent(this, MainActivity.class);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(this, 0, viewIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                getDefaultBuilder(getString(R.string.notification_title), getString(R.string.notification_text))
                        .setContentIntent(viewPendingIntent);

        return notificationBuilder.build();

    }

    private NotificationCompat.Builder getDefaultBuilder(String title, String text) {
        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(text);
    }

    private Notification getNotificationWithAction() {
        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
        Uri geoUri = Uri.parse("geo:0,0?q=London");
        mapIntent.setData(geoUri);
        PendingIntent mapPendingIntent =
                PendingIntent.getActivity(this, 0, mapIntent, 0);

        Intent mailIntent = new Intent(Intent.ACTION_VIEW);
        Uri mailUri = Uri.parse("mailto:smb.roman@gmail.com");
        mailIntent.setData(mailUri);
        mapIntent.setData(geoUri);
        PendingIntent mailPendingIntent =
                PendingIntent.getActivity(this, 0, mailIntent, 0);

        NotificationCompat.Action action = new NotificationCompat.Action.Builder(android.R.drawable.ic_dialog_email, getString(R.string.send_mail), mailPendingIntent).build();

        Bitmap aBigBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.roman);

        NotificationCompat.Builder notificationBuilder =
                getDefaultBuilder(getString(R.string.notification_action_title), getString(R.string.notification_text))
                        .addAction(android.R.drawable.ic_dialog_map,
                                getString(R.string.ahow_map), mapPendingIntent)
                        .extend(new NotificationCompat.WearableExtender().addAction(action))
                        .setStyle(new NotificationCompat.BigPictureStyle()
                                .bigPicture(aBigBitmap));

        return notificationBuilder.build();
    }

    private Notification getNotificationWithPage() {

        // Create builder for the main notification
        NotificationCompat.Builder notificationBuilder =
                getDefaultBuilder("Page 1", "Short text");

        // Create a big text style for the second page
        NotificationCompat.BigTextStyle secondPageStyle = new NotificationCompat.BigTextStyle();
        secondPageStyle.setBigContentTitle("Page 2")
                .bigText("A lot of text...");

        // Create second page notification
        Notification secondPageNotification =
                new NotificationCompat.Builder(this)
                        .setStyle(secondPageStyle)
                        .build();

        return notificationBuilder
                .extend(new NotificationCompat.WearableExtender()
                        .addPage(secondPageNotification))
                .build();

    }
}
