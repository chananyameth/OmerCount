package com.chananya.OmerCount2;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Toast;

import net.sourceforge.zmanim.util.GeoLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

enum Nusakh
{
	SEFARAD,
	ASHKENAZ,
	EDOT_HAMIZRAKH,
	METH
}

public class SettingsActivity extends Activity
{
	// settings vars: initialize to -1 or "", and on the first access load from SharedPreferences.
	// when activity stops: save all settings vars to SharedPreferences.

	private SharedPreferences sp;

	// vars:
	private Nusakh nusakh;
	private boolean isAlarmActive;
	private int alarmMode;

	// constants:
	public static final int ALARM_MODE_HOUR = 0;
	public static final int ALARM_MODE_RELATIVE = 1;

	// views:
	private AppCompatSpinner nusakh_sp;
	private CheckBox activate;
	private LinearLayoutCompat linearAlarms;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		sp = getSharedPreferences("settings", Activity.MODE_PRIVATE);
		setSpinner();
		activate = findViewById(R.id.activate_alarm);
		linearAlarms = findViewById(R.id.linearAlarms);
	}

	private void setSpinner()
	{
		nusakh_sp = findViewById(R.id.nusakh);

		List<String> nusakh_list = new ArrayList<String>();
		nusakh_list.add(getString(R.string.sefarad));
		nusakh_list.add(getString(R.string.ashkecaz));
		nusakh_list.add(getString(R.string.edot_hmizrakh));
		nusakh_list.add(getString(R.string.meth));

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, nusakh_list);

		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		nusakh_sp.setAdapter(dataAdapter);
	}

	@Override
	protected void onStop()
	{
		try {
			sp.edit().putInt("version", getPackageManager().getPackageInfo(getPackageName(), 0).versionCode); //TODO: change version
			toast(String.valueOf(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode));
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		sp.edit().putString("nusakh", nusakh_sp.getSelectedItem().toString());
		toast(nusakh_sp.getSelectedItem().toString());

		sp.edit().putBoolean("activate alarm", activate.isChecked());

		//TODO: save alarm settings anyway

		sp.edit().commit();
		super.onStop();
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

	public void activate_alarmClicked(View view)
	{
		CheckBox c = (CheckBox) view;
		if (c.isChecked())
			linearAlarms.setClickable(true);
		else
			linearAlarms.setClickable(false);
	}

	public void onRadioButtonClicked(View view)
	{
	}


	private void toast(String s)
	{
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}
}
