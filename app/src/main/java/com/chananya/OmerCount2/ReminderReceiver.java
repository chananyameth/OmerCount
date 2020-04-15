package com.chananya.OmerCount2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		if(intent.getAction() == NotificationCreator.ACTION_SNOOZE)
		{

		}
		else if(intent.getAction() == NotificationCreator.ACTION_COUNTED)
		{

		}
		else if(intent.getAction() == NotificationCreator.ACTION_NOTIFY)
		{

		}

		// TODO: This method is called when the BroadcastReceiver is receiving
		// an Intent broadcast.
		//throw new UnsupportedOperationException("Not yet implemented");
	}
}
