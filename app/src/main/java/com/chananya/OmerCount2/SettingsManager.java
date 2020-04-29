package com.chananya.OmerCount2;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import net.sourceforge.zmanim.util.GeoLocation;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

public class SettingsManager
{
	private SharedPreferences sp;

	// settings
	public int nusakh; // position in nusakhList
	public boolean isAlarmActive;
	public int alarmMode;


	// singleton
	private static Context context;

	private static class SingletonHolder
	{
		private final static SettingsManager INSTANCE = new SettingsManager();
	}

	public static SettingsManager getInstance()
	{
		context = MainActivity.applicationContext;
		return SingletonHolder.INSTANCE;
	}

	private SettingsManager()
	{
		sp = context.getSharedPreferences("settings", Activity.MODE_PRIVATE);
		nusakhList = Arrays.asList(
				context.getString(R.string.sefarad),
				context.getString(R.string.ashkecaz),
				context.getString(R.string.edot_hamizrakh),
				context.getString(R.string.meth));

		loadSettings();
	}

	private void loadSettings()
	{
		nusakh = sp.getInt("nusakh", 0);
		isAlarmActive = sp.getBoolean("activate alarm", true);
		alarmMode = sp.getInt("alarm mode", 0);
	}

	public void saveSettings()
	{
		try {
			sp.edit().putInt("version", context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode).commit(); //TODO: change version
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		sp.edit().putInt("nusakh", nusakh).commit();
		sp.edit().putBoolean("activate alarm", isAlarmActive).commit();
		sp.edit().putInt("alarm mode", alarmMode).commit();
	}

	// statics
	public static List<String> nusakhList;

	public GeoLocation getLocation()
	{
		// TODO: add more places
		String locationName = "Jerusalem, IL";
		double latitude = 31.771959; // Jerusalem, IL
		double longitude = 35.217018; // Jerusalem, IL
		double elevation = 798; // optional elevation correction in Meters
		TimeZone timeZone = TimeZone.getTimeZone("Israel");
		GeoLocation location = new GeoLocation(locationName, latitude, longitude, elevation, timeZone);
		return location;
	}

}
