package com.example.myapplication3;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

    public class MainActivity extends AppCompatActivity implements View.OnClickListener {

        String KEY_REPLY = "key_reply";
        public static final int NOTIFICATION_ID = 1;

        Button btnBasicInlineReply;
        TextView txtReplied;

        //this is so it works on api 26+
        private void createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name ="hello";
                String description = "help me";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel("CHANNEL_ID", name, importance);
                channel.setDescription(description);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
        }


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            createNotificationChannel();

            btnBasicInlineReply = (Button) findViewById(R.id.btn_basic_inline_reply);
            txtReplied = (TextView) findViewById(R.id.txt_inline_reply);
            btnBasicInlineReply.setOnClickListener(this);

            clearExistingNotifications();
        }

        @Override
        protected void onNewIntent(Intent intent) {
            super.onNewIntent(intent);
            processInlineReply(intent);

        }

        @Override
        public void onClick(View v) {

                    //Create notification builder
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL_ID")
                            .setSmallIcon(android.R.drawable.stat_notify_chat)
                            .setContentTitle("Inline Reply Notification");

                    String replyLabel = "Enter your reply here";

                    //Initialise RemoteInput
                    RemoteInput remoteInput = new RemoteInput.Builder(KEY_REPLY)
                            .setLabel(replyLabel)
                            .build();


                    int randomRequestCode = new Random().nextInt(54325);

                    //PendingIntent that restarts the current activity instance.
                    Intent resultIntent = new Intent(this, MainActivity.class);
                    //Set a unique request code for this pending intent
                    PendingIntent resultPendingIntent = PendingIntent.getActivity(this, randomRequestCode, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);


                    //Notification Action with RemoteInput instance added.
                    NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(
                            android.R.drawable.sym_action_chat, "REPLY", resultPendingIntent)
                            .addRemoteInput(remoteInput)
                            .setAllowGeneratedReplies(true)
                            .build();

                    //Notification.Action instance added to Notification Builder.
                    builder.addAction(replyAction);

                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("notificationId", NOTIFICATION_ID);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent dismissIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);


                    builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "DISMISS", dismissIntent);

                    //Create Notification.
                    NotificationManager notificationManager =
                            (NotificationManager)
                                    getSystemService(Context.NOTIFICATION_SERVICE);

                    notificationManager.notify(NOTIFICATION_ID,
                            builder.build());


        }

        private void clearExistingNotifications() {
            int notificationId = getIntent().getIntExtra("notificationId", 0);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(notificationId);
        }

        private void processInlineReply(Intent intent) {
            Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

            if (remoteInput != null) {
                String reply = remoteInput.getCharSequence(
                        KEY_REPLY).toString();

                //Set the inline reply text in the TextView
                txtReplied.setText("Reply is "+reply);


                //Update the notification to show that the reply was received.
                NotificationCompat.Builder repliedNotification =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(
                                        android.R.drawable.stat_notify_chat)
                                .setContentText("Inline Reply received");

                NotificationManager notificationManager =
                        (NotificationManager)
                                getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(NOTIFICATION_ID,
                        repliedNotification.build());

            }
        }
    }
