package com.chananya.OmerCount2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity
{
	//UI elements
	private TextView date_tv;
	private TextView noOmer;
	private ScrollView texts_vs;
	private TextView leshem_yichud_tv;
	private TextView beracha_tv;
	private TextView hayom_tv;
	private TextView harachaman_tv;
	private TextView lamnatzeach_tv;
	private TextView ana_bekoach_tv;
	private TextView ribono_tv;
	private CheckBox checkbox1;
	private TextView aleynu_tv;

	//navigation mode
	private boolean isNavMode;
	private Calendar navDate;

	//Other classes
	private JewishTimes times;
	private OmerTexts texts;
	private NotificationCreator nc;
	private AlertDialog.Builder dialog;

	public static Context applicationContext;

	@Override
	protected void onCreate(Bundle _savedInstanceState)
	{
		super.onCreate(_savedInstanceState);
		//getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
		setContentView(R.layout.main);
		applicationContext = getApplicationContext();

		firstTimeSetSettings();
		initialize(_savedInstanceState);
		determineShowDialog();
	}

	private void firstTimeSetSettings()
	{
		SharedPreferences sp = getApplicationContext().getSharedPreferences("settings", Activity.MODE_PRIVATE);

		if (!sp.contains("version")) {
			toast(getString(R.string.first_time_settings));
			Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
			startActivity(intent);
		}
	}

	private void initialize(Bundle _savedInstanceState)
	{
		texts = new OmerTexts(getApplicationContext(), 1); // _day=1 just for initialization. any way gets set to the right date before using
		times = new JewishTimes();

		// views
		date_tv = (TextView) findViewById(R.id.date);
		noOmer = (TextView) findViewById(R.id.noOmer);
		texts_vs = (ScrollView) findViewById(R.id.texts_vs);

		leshem_yichud_tv = (TextView) findViewById(R.id.leshem_yichud_tv);
		beracha_tv = (TextView) findViewById(R.id.beracha_tv);
		hayom_tv = (TextView) findViewById(R.id.count_tv);
		harachaman_tv = (TextView) findViewById(R.id.harachaman);
		lamnatzeach_tv = (TextView) findViewById(R.id.lamnatzeach_tv);
		ana_bekoach_tv = (TextView) findViewById(R.id.ana_bekoach_tv);
		ribono_tv = (TextView) findViewById(R.id.ribono_tv);
		aleynu_tv = (TextView) findViewById(R.id.aleynu_tv);

		checkbox1 = (CheckBox) findViewById(R.id.checkbox1);

		nc = new NotificationCreator(this.getApplicationContext());

		isNavMode = false;
		navDate = times.nowC();

		// listeners
		date_tv.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View _view)
			{
				DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener()
				{
					@Override
					public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
					{
						navDate.set(Calendar.YEAR, year);
						navDate.set(Calendar.MONTH, month);
						navDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
						isNavMode = true;
						updateView(false);
					}
				}, navDate.get(Calendar.YEAR), navDate.get(Calendar.MONTH), navDate.get(Calendar.DAY_OF_MONTH));
				datePickerDialog.show();
			}
		});

		hayom_tv.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View _view)
			{
				if (times.getOmerCount(navDate.getTime(), false) == 49) {
					toast(getString(R.string.leap));
					navDate.add(Calendar.DAY_OF_MONTH, -48); //subtract 48 days - back to start
				} else {
					navDate.add(Calendar.DAY_OF_MONTH, 1); //add a day
				}
				isNavMode = true;
				updateView(false);
			}
		});

		dialog = new AlertDialog.Builder(this);
		dialog.setCancelable(false)
				.setMessage(getString(R.string.tzais_warning))
				.setPositiveButton(getString(R.string.past_day), new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dlg, int id)
					{
						toast(getString(R.string.past_day));
						updateView(false);
					}
				})
				.setNegativeButton(getString(R.string.new_day), new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dlg, int id)
					{
						toast(getString(R.string.new_day));
						updateView(true);
					}
				})
				.create();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.settings:
				Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
				startActivity(intent);
				return true;
			case R.id.help:
				//showHelp();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void Button1Click(View view)
	{
		//nc.create("Hello", "world! this is a very very very very very long long long text, and more text.......");
		nc.create("Hello", "world! this is a short text");
	}

	private void determineShowDialog()
	{
		if (isNavMode) {
			updateView(false);
		} else {
			if (times.isNowCloseToTzais(1000 * 60 * 30)) // half an hour before or after Tzais
				dialog.show();
			else
				updateView(times.isNowAfterTzais());
		}
	}

	private void updateView(boolean afterTzais)
	{
		int day;

		if (isNavMode)
			day = times.getOmerCount(navDate.getTime(), afterTzais);
		else
			day = times.getNowOmerCount(afterTzais);

		if (afterTzais)
			date_tv.setText("אור ליום " + times.formatDayOfWeek(times.tommorowOf(navDate)) + " " + times.format(times.tommorowOf(navDate)));
		else
			date_tv.setText("יום " + times.formatDayOfWeek(navDate) + " " + times.format(navDate));

		if (0 < day && day < 50) {
			noOmer.setVisibility(View.GONE);
			texts_vs.setVisibility(View.VISIBLE);

			texts.setDay(day);

			leshem_yichud_tv.setText(texts.getLeshem_yichud());
			beracha_tv.setText(texts.getBeracha());
			hayom_tv.setText(texts.getHayom());
			harachaman_tv.setText(texts.getHarachaman());
			lamnatzeach_tv.setText(texts.getLamnatzeach());
			ana_bekoach_tv.setText(texts.getAna_bekoach());
			ribono_tv.setText(texts.getRibono());
			aleynu_tv.setText(texts.getAleynu());
		} else {
			texts_vs.setVisibility(View.GONE);
			noOmer.setVisibility(View.VISIBLE);
		}
	}

	private void toast(String s)
	{
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}
}
