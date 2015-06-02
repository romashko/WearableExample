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
import android.support.v4.app.RemoteInput;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

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
    public static final String EXTRA_VOICE_REPLY = "extra_voice_reply";
    public static final int NOTIFICATION_ID = 1;
    public static final String GEO_LINK = "geo:0,0?q=London";
    public static final String MAIL_LINK = "mailto:smb.roman@gmail.com";

    private GoogleApiClient googleApiClient;

    private int count;

    private TextView notificationResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        count = 1;

        setContentView(R.layout.activity_main);
        findViewById(R.id.notification_button).setOnClickListener(this);
        findViewById(R.id.notification_big_button).setOnClickListener(this);
        findViewById(R.id.notification_action_button).setOnClickListener(this);
        findViewById(R.id.notification_page_button).setOnClickListener(this);
        findViewById(R.id.notification_voice).setOnClickListener(this);
        findViewById(R.id.send_data).setOnClickListener(this);
        notificationResult = (TextView) findViewById(R.id.notification_result);

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
    protected void onNewIntent(Intent intent) {
        notificationResult.setText(getMessageText(intent));
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
            case R.id.notification_big_button:
                notification = getBigStyleNotification();
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
            case R.id.notification_voice:
                notification = getNotificationWithVoice();
                showNotification(notification);
                break;
            case R.id.send_data:
                PutDataMapRequest dataMap = PutDataMapRequest.create("/count");
                dataMap.getDataMap().putInt(COUNT_KEY, count++);
                PutDataRequest request = dataMap.asPutDataRequest();
                PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                        .putDataItem(googleApiClient, request);
                break;
        }
    }

    private CharSequence getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(EXTRA_VOICE_REPLY);
        }
        return null;
    }

    private void showNotification(Notification notification) {
        int notificationId = NOTIFICATION_ID;

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
        Uri geoUri = Uri.parse(GEO_LINK);
        mapIntent.setData(geoUri);
        PendingIntent mapPendingIntent =
                PendingIntent.getActivity(this, 0, mapIntent, 0);

        Intent mailIntent = new Intent(Intent.ACTION_VIEW);
        Uri mailUri = Uri.parse(MAIL_LINK);
        mailIntent.setData(mailUri);
        mapIntent.setData(geoUri);
        PendingIntent mailPendingIntent =
                PendingIntent.getActivity(this, 0, mailIntent, 0);

        NotificationCompat.Action action = new NotificationCompat.Action.Builder(android.R.drawable.ic_dialog_email, getString(R.string.send_mail), mailPendingIntent).build();

        Bitmap aBigBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.me);

        NotificationCompat.Builder notificationBuilder =
                getDefaultBuilder(getString(R.string.notification_action_title), getString(R.string.notification_text))
                        .addAction(android.R.drawable.ic_dialog_map,
                                getString(R.string.ahow_map), mapPendingIntent)
                        .extend(new NotificationCompat.WearableExtender()
                                .addAction(action)
                                .setGravity(Gravity.BOTTOM)
                                .setHintHideIcon(true))
                        .setStyle(new NotificationCompat.BigPictureStyle()
                                .bigPicture(aBigBitmap));

        return notificationBuilder.build();
    }

    private Notification getBigStyleNotification() {

        // Create builder for the main notification
        NotificationCompat.Builder notificationBuilder =
                getDefaultBuilder(getString(R.string.notification_title), getString(R.string.notification_text));

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(getString(R.string.notification_title))
                .bigText(getString(R.string.a_lot_of_text));

        notificationBuilder.setStyle(bigTextStyle)
                        .build();

        return notificationBuilder.build();

    }


    private Notification getNotificationWithPage() {

        // Create builder for the main notification
        NotificationCompat.Builder notificationBuilder =
                getDefaultBuilder(getString(R.string.notification_title_page_1), getString(R.string.notification_text));

        // Create a big text style for the second page
        NotificationCompat.BigTextStyle secondPageStyle = new NotificationCompat.BigTextStyle();
        secondPageStyle.setBigContentTitle(getString(R.string.notification_title_page2))
                .bigText(getString(R.string.a_lot_of_text));

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

    private Notification getNotificationWithVoice() {
        String replyLabel = getResources().getString(R.string.reply_label);
        String[] replyChoices = getResources().getStringArray(R.array.reply_choices);

        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
                .setLabel(replyLabel)
                .setChoices(replyChoices)
                .build();

// Create an intent for the reply action
        Intent replyIntent = new Intent(this, MainActivity.class);
        PendingIntent replyPendingIntent =
                PendingIntent.getActivity(this, 0, replyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

// Create the reply action and add the remote input
        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(android.R.drawable.ic_input_add,
                        getString(R.string.reply_label), replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

// Build the notification and add the action via WearableExtender
        return getDefaultBuilder(getString(R.string.notification_reply_title), getString(R.string.notification_reply_text))
                        .extend(new NotificationCompat.WearableExtender()
                                .addAction(action))
                        .build();
    }


}
