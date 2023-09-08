package ai.hellodoctor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private int notiId = 0;

    /* 토큰이 새로 만들어질때나 refresh 될때  */
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.e("로그:onNewToken", s);
        /* DB서버로 새토큰을 업데이트시킬수 있는 부분 */
    }

    /* 메세지를 새롭게 받을때 */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d("로그:onMessageReceived", "From: " + remoteMessage.getFrom() + "/" + remoteMessage);

        /*if (remoteMessage.getNotification() != null) {
            Log.d("로그", "From: " + remoteMessage.getFrom());
            Log.d("로그", "Message Notification Body: " + remoteMessage.getNotification().getBody());
            String messageBody = remoteMessage.getNotification().getBody();
            String messageTitle = remoteMessage.getNotification().getTitle();
            sendNotification(remoteMessage);
        }*/

        sendNotification(remoteMessage);
    }

    private void sendNotification(RemoteMessage remote) {
        String messageBody = remote.getData().get("message");
        String subject = remote.getData().get("subject");
        String goUrl = remote.getData().get("goUrl");
        String gubun = remote.getData().get("gubun");
        String channelId = "helloDr";
        int notiId = createID();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.putExtra("goUrl", goUrl);

        if (TextUtils.isEmpty(goUrl)) goUrl = String.valueOf(R.string.intro);
        Log.d("로그:sendNotification", goUrl);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, notiId /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        //Bitmap BigPictureStyle = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        long vibrate[] = {500, 0, 500, 0};

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(subject)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setVibrate(vibrate)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody));

        Notification notification = notificationBuilder.build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // 오레오 이상 (안드로이드8)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();

            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);

            notificationManager.createNotificationChannel(channel);

        }
        //notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        notificationManager.notify(notiId, notificationBuilder.build());
    }

    private int createID() {
        int id = 0;
        Random rd = new Random();
        id = rd.nextInt(99999);

        //Log.d("createID()", String.valueOf(id));
        return id;
    }
}
