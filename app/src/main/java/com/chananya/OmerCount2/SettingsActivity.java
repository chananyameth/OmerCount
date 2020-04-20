package com.chananya.OmerCount2;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import net.sourceforge.zmanim.util.GeoLocation;

import java.util.TimeZone;

public class SettingsActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
	}

	public static GeoLocation getLocation()
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

	public void onRadioButtonClicked(View view)
	{
	}
}
