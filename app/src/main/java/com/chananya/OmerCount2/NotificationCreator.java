package com.chananya.OmerCount2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class NotificationCreator
{
	private String CHANNEL_ID = "NotificationCreatorId";
	private Context context;

	public NotificationCreator(Context con)
	{
		context = con;
		createNotificationChannel();
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
				.setStyle(new NotificationCompat.BigTextStyle()
						.bigText(bigtext))
				.setPriority(NotificationCompat.PRIORITY_DEFAULT);
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
