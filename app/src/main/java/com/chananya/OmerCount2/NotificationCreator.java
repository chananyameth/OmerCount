package com.chananya.OmerCount2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

public class NotificationCreator
{
	public static final String ACTION_SNOOZE = "ACTION_SNOOZE";
	public static final String ACTION_COUNTED = "ACTION_COUNTED";
	public static final String ACTION_NOTIFY = "ACTION_NOTIFY";
	private String CHANNEL_ID = "NotificationCreatorId";
	int Id;		// unique int for each notification - we have only one
	private Context context;

	Intent contentIntent;
	Intent snoozeIntent;
	Intent countedIntent;
	PendingIntent contentPendingIntent;
	PendingIntent snoozePendingIntent;
	PendingIntent countedPendingIntent;
	NotificationManagerCompat notificationManager;

	public NotificationCreator(Context con)
	{
		context = con;
		createNotificationChannel();

		// clicking on the notification
		contentIntent = new Intent(context, MainActivity.class);
		contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		contentPendingIntent = PendingIntent.getActivity(context, 0, contentIntent, 0);

		notificationManager = NotificationManagerCompat.from(context);
		Id = 8945; // random


		// clicking on 'later' button
		snoozeIntent = new Intent(context, ReminderReceiver.class);
		snoozeIntent.setAction(ACTION_SNOOZE);
		//snoozeIntent.putExtra("EXTRA_NOTIFICATION_ID", Id);
		snoozePendingIntent = PendingIntent.getBroadcast(context, 0, snoozeIntent, 0);

		// clicking on 'counted' button
		countedIntent = new Intent(context, ReminderReceiver.class);
		countedIntent.setAction(ACTION_COUNTED);
		//countedIntent.putExtra("EXTRA_NOTIFICATION_ID", Id);
		countedPendingIntent = PendingIntent.getBroadcast(context, 0, countedIntent, 0);
	}

	public void create(String title, String text)
	{
		create(title, text, text);
	}

	public void create(String title, String text, String bigtext)
	{
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
				.setSmallIcon(R.drawable.app_icon)
				.setContentTitle(title)
				.setContentText(text)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(bigtext))
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setContentIntent(contentPendingIntent) // clicking on the notification
				.setAutoCancel(false)
				.addAction(R.drawable.default_image, context.getString(R.string.later), snoozePendingIntent)		// clicking on 'later' button
				.addAction(R.drawable.default_image, context.getString(R.string.mark_as_counted), snoozePendingIntent);		// clicking on 'later' button


		notificationManager.notify(Id, builder.build());
	}

	private void createNotificationChannel()
	{
		// Create the NotificationChannel, but only on API 26+ because
		// the NotificationChannel class is new and not in the support library
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = "omerNotification";
			String description = "omer Notification";
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
			channel.setDescription(description);
			// Register the channel with the system; you can't change the importance
			// or other notification behaviors after this
			NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
		}
	}
}
