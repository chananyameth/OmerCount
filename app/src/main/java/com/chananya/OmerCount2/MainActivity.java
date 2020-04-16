package com.chananya.OmerCount2;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;
import net.sourceforge.zmanim.util.GeoLocation;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends Activity
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

	//Other classes
	private JewishTimes times;
	private OmerTexts texts;

	//navigation mode
	private boolean isNavMode;
	private Calendar navDate;

	private AlertDialog.Builder dialog;


	//-----------------------------remove vars from here down------------

	//variables
	private int dayToDisplay = 0;
	private Calendar DateToDisplay;
	private boolean afterTzais; //after tzais, before chatzos. so add 1 to DayToDisplay

	//zmanim
	private ComplexZmanimCalendar czc;
	private JewishCalendar jc;
	private HebrewDateFormatter hdf;
	private Date tzais; //tzais haKochavim by rav Tucazinsky
	private boolean date_nav_mode; //navigating between date manually, so doesn't have 'or le..'
	private boolean skipSettingTzais;

	//components
	private AlertDialog.Builder d;
	private boolean is_d_open = false;
	AlarmManager alarmManager;
	private PendingIntent pendingIntent;
	private static MainActivity inst;
	private NotificationCreator nc;

	public static MainActivity instance()
	{
		return inst;
	}

	@Override
	protected void onCreate(Bundle _savedInstanceState)
	{
		super.onCreate(_savedInstanceState);
		getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
		setContentView(R.layout.main);
		inst = this;

		//initializeTimes();
		initialize(_savedInstanceState);
		//initializeLogic();

		//dayChangedUpdateView();
		determineShowDialog();
	}

	/*private void initializeTimes()
	{
		jc = new JewishCalendar();
		hdf = new HebrewDateFormatter();
		hdf.setHebrewFormat(true);

		String locationName = "Jerusalem, IL";
		double latitude = 31.771959; // Jerusalem, IL
		double longitude = 35.217018; // Jerusalem, IL
		double elevation = 798; // optional elevation correction in Meters
		TimeZone timeZone = TimeZone.getTimeZone("Israel");
		GeoLocation location = new GeoLocation(locationName, latitude, longitude, elevation, timeZone);
		czc = new ComplexZmanimCalendar(location); //default to today's date
		tzais = czc.getTzaisGeonim6Point45Degrees();

		DateToDisplay = Calendar.getInstance(timeZone);

		// set broadcastReceiver & notification
		//Calendar calendar = Calendar.getInstance();
		//calendar.set(Calendar.HOUR_OF_DAY, tzais.getHours());
		//calendar.set(Calendar.MINUTE, tzais.getMinutes());
		//Intent myIntent = new Intent(MainActivity.this, NotifyReceiver.class);
		//pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, myIntent, 0);
		//alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
	}*/

	private void initialize(Bundle _savedInstanceState)
	{
		texts = new OmerTexts(getApplicationContext(), dayToDisplay);
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
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
					{
						navDate.set(Calendar.YEAR, year);
						navDate.set(Calendar.MONTH, monthOfYear);
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
				if (dayToDisplay == 49) {
					toast(getString(R.string.leap));
					navDate.add(Calendar.DAY_OF_MONTH, -48); //subtract 48 days - back to start
				} else {
					navDate.add(Calendar.DAY_OF_MONTH, 1); //add a day
				}
				isNavMode = true;
				updateView(false);
			}
		});

		/*d = new AlertDialog.Builder(this);
		d.setCancelable(false)
				.setMessage("כעת אנו קרובים למעבר בין הימים. להציג את היום העובר, או את היום החדש?")
				.setPositiveButton("היום העובר", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dlg, int id)
					{
						//previous day
						is_d_open = false;
						skipSettingTzais = true;

						afterTzais = false;
						Toast.makeText(getApplicationContext(), "היום העובר", Toast.LENGTH_SHORT).show();
						dayChangedUpdateView();
					}
				})
				.setNegativeButton("היום החדש", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dlg, int id)
					{
						//new day
						is_d_open = false;
						skipSettingTzais = true;

						afterTzais = true;
						Toast.makeText(getApplicationContext(), "היום החדש", Toast.LENGTH_SHORT).show();
						dayChangedUpdateView();
					}
				})
				.create();*/

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

	/*private void initializeLogic()
	{
		date_nav_mode = false;
		skipSettingTzais = false;
		is_d_open = false;
		afterTzais = false;
	}*/

	/*private void dayChangedUpdateView()
	{
		Date now = new Date();

		if (!skipSettingTzais) {
			afterTzais = (tzais.compareTo(now) < 0);
			//now let the user choose to change afterTzais or leave it as it is
			if (!date_nav_mode && !is_d_open && (tzais.getTime() - now.getTime()) < 3600000 && (tzais.getTime() - now.getTime()) > 0)    // 1000*60*60 = 3,600,000 -> within 1 hour before tzais
			{
				is_d_open = true;
				d.show();
				//Toast.makeText(getApplicationContext(), "Test Toast - should appear after dialog closed" , Toast.LENGTH_SHORT).show();
			}
		}

		if (!date_nav_mode && afterTzais) //adjusting DateToDisplay to be the right hebrew date
		{
			DateToDisplay.setTime(now);
			DateToDisplay.add(Calendar.DAY_OF_MONTH, 1);
		}

		jc.setDate(DateToDisplay);
		int day = jc.getDayOfOmer();

		JewishDate jdt = new JewishDate(DateToDisplay);
		if (!date_nav_mode && afterTzais)
			date_tv.setText("אור ליום " + hdf.formatDayOfWeek(jdt) + " " + hdf.format(jdt));
		else
			date_tv.setText("יום " + hdf.formatDayOfWeek(jdt) + " " + hdf.format(jdt));

		if (0 < day && day < 50) {
			noOmer.setVisibility(View.GONE);
			texts_vs.setVisibility(View.VISIBLE);
			dayToDisplay = day;
			texts.setDay(day);

			leshem_yichud_tv.setText(texts.getLeshem_yichud());
			beracha_tv.setText(texts.getBeracha());
			hayom_tv.setText(texts.getHayom());
			harachaman_tv.setText(texts.getHarachaman());
			lamnatzeach_tv.setText(texts.getLamnatzeach());
			ana_bekoach_tv.setText(texts.getAna_bekoach());
			ribono_tv.setText(texts.getRibono());
		} else {
			texts_vs.setVisibility(View.GONE);
			noOmer.setVisibility(View.VISIBLE);
		}
		skipSettingTzais = false;

		//Calendar calendar = Calendar.getInstance();
		//Intent myIntent = new Intent(MainActivity.this, NotifyReceiver.class);
		//pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, myIntent, 0);
		//alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
	}*/


	// ----------------------new--------------
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
			if (times.isNowCloseToTzais(1000 * 60 * 30)) // half hour before or after Tzais
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
