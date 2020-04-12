package com.chananya.OmerCount2;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
	//variables
	private int dayToDisplay = 0;
	private Calendar DateToDisplay;
	private boolean afterTzais; //after tzais, before chatzos. so add 1 to DayToDisplay
	private OmerTexts texts;

	//UI elements
	private TextView date_tv;
	private TextView noOmer;
	private ScrollView vscroll1;
	private TextView leshem_yichud_tv;
	private TextView beracha_tv;
	private TextView hayom_tv;
	private TextView harachaman_tv;
	private TextView lamnatzeach_tv;
	private TextView ana_bekoach_tv;
	private TextView ribono_tv;
	private CheckBox checkbox1;

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

		initializeTimes();
		initialize(_savedInstanceState);
		initializeLogic();

		dayChangedUpdateView();
	}

	private void initializeTimes()
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

		//set service & notification
		/*Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, tzais.getHours());
		calendar.set(Calendar.MINUTE, tzais.getMinutes());
		Intent myIntent = new Intent(MainActivity.this, NotifyReceiver.class);
		pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, myIntent, 0);
		alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);*/
	}

	private void initialize(Bundle _savedInstanceState)
	{
		texts = new OmerTexts(getApplicationContext(), dayToDisplay);

		// views
		date_tv = (TextView) findViewById(R.id.date);
		noOmer = (TextView) findViewById(R.id.noOmer);
		vscroll1 = (ScrollView) findViewById(R.id.vscroll1);

		leshem_yichud_tv = (TextView) findViewById(R.id.leshem_yichud_tv);
		beracha_tv = (TextView) findViewById(R.id.beracha_tv);
		hayom_tv = (TextView) findViewById(R.id.count_tv);
		harachaman_tv = (TextView) findViewById(R.id.harachaman);
		lamnatzeach_tv = (TextView) findViewById(R.id.lamnatzeach_tv);
		ana_bekoach_tv = (TextView) findViewById(R.id.ana_bekoach_tv);
		ribono_tv = (TextView) findViewById(R.id.ribono_tv);

		checkbox1 = (CheckBox) findViewById(R.id.checkbox1);

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
						DateToDisplay.set(Calendar.YEAR, year);
						DateToDisplay.set(Calendar.MONTH, monthOfYear);
						DateToDisplay.set(Calendar.DAY_OF_MONTH, dayOfMonth);

						date_nav_mode = true;
						dayChangedUpdateView();
					}
				}, DateToDisplay.get(Calendar.YEAR), DateToDisplay.get(Calendar.MONTH), DateToDisplay.get(Calendar.DAY_OF_MONTH));
				datePickerDialog.show();
			}
		});
		hayom_tv.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View _view)
			{
				if (dayToDisplay == 49) {
					Toast.makeText(getApplicationContext(), "leap", Toast.LENGTH_SHORT);
					DateToDisplay.add(Calendar.DAY_OF_MONTH, -48); //subtract 48 days - back to start
				} else {
					DateToDisplay.add(Calendar.DAY_OF_MONTH, 1); //add a day
				}
				date_nav_mode = true;
				dayChangedUpdateView();
			}
		});

		d = new AlertDialog.Builder(this);
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
				.create();
	}

	private void initializeLogic()
	{
/*		leshem_yichud = "לְשֵׁם יִחוּד קּוּדְשָׁא בְּרִיךְ הוּא וּשְׁכִינְתֵּיהּ בִּדְחִילוּ וּרְחִימוּ לְיַחֵד שֵׁם י\"ה בו\"ה בְּיִחוּדָא שְׁלִים בְּשֵׁם כָּל יִשְׂרָאֵל. הִנְּנִי מוּכָן וּמְזֻמָּן לְקַיֵּם מִצְוַת עֲשֵׂה שֶׁל סְפִירַת הָעֹמֶר כְּמוֹ שֶׁכָּתוּב בַּתּוֹרָה: וּסְפַרְתֶּם לָכֶם מִמָּחֳרַת הַשַּׁבָּת מִיּוֹם הֲבִיאֲכֶם אֶת עֹמֶר הַתְּנוּפָה שֶׁבַע שַׁבָּתוֹת תְּמִימוֹת תִּהְיֶינָה: עַד מִמָּחֳרָת הַשַּׁבָּת הַשְּׁבִיעִית תִּסְפְּרוּ חֲמִשִּׁים יוֹם.";
		pre_lamnatzeach = "לַמְנַצֵּח בִּנְגִינֹת מִזְמוֹר שִׁיר. ";
		lamnatzeach = "אֱלֹהִים יְחָנֵּנוּ וִיבָרְכֵנוּ יָאֵר פָּנָיו אִתָּנוּ סֶלָה. לָדַעַת בָּאָרֶץ דַּרְכֶּךָ בְּכָל גּוֹיִם יְשׁוּעָתֶךָ. יוֹדוּךָ עַמִּים אֱלֹהִים יוֹדוּךָ עַמִּים כֻּלָּם. יִשְׂמְחוּ וִירַנְּנוּ לְאֻמִּים כִּי תִשְׁפּ(וֹ)ט עַמִּים מִישׁוֹר וּלְאֻמִּים בָּאָרֶץ תַּנְחֵם סֶלָה. יוֹדוּךָ עַמִּים אֱלֹהִים יוֹדוּךָ עַמִּים כֻּלָּם. אֶרֶץ נָתְנָה יְבוּלָהּ יְבָרְכֵנוּ אֱלֹהִים אֱלֹהֵינוּ. יְבָרְכֵנוּ אֱלֹהִים וְיִירְאוּ אֹתוֹ כָּל אַפְסֵי אָרֶץ.";
		ana_bekoach = "אָנָּא בְּכֹחַ גְּדֻלַּת יְמִינְךָ תַּתִּיר צְרוּרָה (אב\"ג ית\"ץ) \nקַבֵּל רִנַּת עַמְּךָ שַׂגְּבֵנוּ טַהֲרֵנוּ נוֹרָא (קר\"ע שט\"ן) \nנָא גִבּוֹר דּוֹרְשֵׁי יִחוּדְךָ כְּבָבַת שָׁמְרֵם (נג\"ד יכ\"ש) \nבָּרְכֵם טַהֲרֵם רַחֲמֵי צִדְקָתְךָ תָּמִיד גָּמְלֵם (בט\"ר צת\"ג) \nחֲסִין קָדוֹשׁ בְּרוֹב טוּבְךָ נַהֵל עֲדָתֶךָ (חק\"ב טנ\"ע) \nיָחִיד גֵּאֶה לְעַמְּךָ פְּנֵה זוֹכְרֵי קְדֻשָּׁתֶךָ (יג\"ל פז\"ק) \nשַׁוְעָתֵנוּ קַבֵּל וּשְׁמַע צַעֲקָתֵנוּ יוֹדֵעַ תַּעֲלוּמוֹת (שק\"ו צי\"ת) \nבָּרוּךְ שֵׁם כְּבוֹד מַלְכוּתוֹ לְעוֹלָם וָעֶד.";
		ribono_1 = "רִבּוֹנוֹ שֶׁל עוֹלָם, אַתָּה צִוִּיתָנוּ עַל יְדֵי מֹשֶׁה עַבְדֶּךָ לִסְפֹּר סְפִירַת הָעֹמֶר כְּדֵי לְטַהֲרֵנוּ מִקְּלִפּוֹתֵינוּ וּמִטֻּמְאוֹתֵינוּ, כְּמוֹ שֶּׁכָּתַבְתָּ בְּתוֹרָתֶךָ: וּסְפַרְתֶּם לָכֶם מִמָּחֳרָת הַשַּׁבָּת מִיּוֹם הֲבִיאֲכֶם אֶת עֹמֶר הַתְּנוּפָה שֶׁבַע שַׁבָּתוֹת תְּמִימוֹת תִּהְיֶינָה: עַד מִמָּחֳרָת הַשַּׁבָּת הַשְּׁבִיעִית תִּסְפְּרוּ חֲמִשִּׁים יוֹם, כְּדֵי שֶׁיִּטָּהֲרוּ נַפְשׁוֹת עַמְּךָ יִשְׂרָאֵל מִזֻּהֲמָתָם. וּבְכֵן יְהִי רָצוֹן מִלְּפָנֶיךָ ה' אֱלֹהֵינוּ וֵאלֹהֵי אֲבוֹתֵינוּ, שֶׁבִּזְכוּת סְפִירַת הָעֹמֶר שֶׁסָּפַרְתִּי הַיּוֹם, יְתֻקַּן מַה שֶׁפָּגַמְתִּי בִּסְפִירָה:";

		sefirot.clear();
		sefirot.add("חֶסֶד");
		sefirot.add("גְּבוּרָה");
		sefirot.add("תִּפְאֶרֶת");
		sefirot.add("נֵּצַח");
		sefirot.add("הוֹד");
		sefirot.add("יְסוֹד");
		sefirot.add("מַלְכוּת");

		//fonts
		leshem_yichud_tv.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/font1.ttf"), Typeface.NORMAL);
		beracha_tv.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/font1.ttf"), Typeface.NORMAL);
		count_tv.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/font1.ttf"), Typeface.NORMAL);
		harachaman.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/font1.ttf"), Typeface.NORMAL);
		lamnatzeach_tv.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/font1.ttf"), Typeface.NORMAL);
		ana_bekoach_tv.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/font1.ttf"), Typeface.NORMAL);
		ribono_1_tv.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/font1.ttf"), Typeface.NORMAL);
		sefira_tv.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/font1.ttf"), Typeface.NORMAL);
		ribono_2_tv.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/font1.ttf"), Typeface.NORMAL);

		leshem_yichud_tv.setText(leshem_yichud);
		ana_bekoach_tv.setText(ana_bekoach);
		ribono_1_tv.setText(ribono_1);
*/
		date_nav_mode = false;
		skipSettingTzais = false;
		is_d_open = false;
		afterTzais = false;
	}

	/*private void _change_style(final TextView _tv, final String _start, final String _middle, final String _end)
	{
		SpannableStringBuilder builder = new SpannableStringBuilder();

		android.text.style.ForegroundColorSpan fcs = new android.text.style.ForegroundColorSpan(Color.BLACK);
		android.text.style.ForegroundColorSpan fcs1 = new android.text.style.ForegroundColorSpan(Color.RED);

		SpannableString str1 = new SpannableString(_start);
		str1.setSpan(fcs, 0, str1.length(), 0);
		builder.append(str1);

		SpannableString str2 = new SpannableString(_middle);
		str2.setSpan(fcs1, 0, str2.length(), 0);
		builder.append(str2);

		SpannableString str3 = new SpannableString(_end);
		str3.setSpan(fcs, 0, str3.length(), 0);
		builder.append(str3);

		_tv.setText(builder, TextView.BufferType.SPANNABLE);
	}*/

	private void dayChangedUpdateView()
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
			vscroll1.setVisibility(View.VISIBLE);
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
			vscroll1.setVisibility(View.GONE);
			noOmer.setVisibility(View.VISIBLE);
		}
		skipSettingTzais = false;

		/*Calendar calendar = Calendar.getInstance();
		Intent myIntent = new Intent(MainActivity.this, NotifyReceiver.class);
		pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, myIntent, 0);
		alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);*/
	}

	//---------- HELPERS - SETTING UP & DECORATING TEXT ----------
/*
	private void _a_01_sefira()
	{
		str = "";
		int day = (int) DayToDisplay;
		String[] d = {"", "אחד ", "שני ", "שלושה ", "ארבעה ", "חמישה ", "שישה ", "שבעה ", "שמונה ", "תשעה ", "עשר ", "עשרים ", "שלושים ", "ארבעים "};
		str += "היום ";
		if (day == 1) str += "יום אחד ";
		else if (day % 10 != 0) {
			str += d[day % 10];
			if (day > 10) {
				if (day % 10 == 2) {
					str = str.substring(0, str.length() - 1);
					str += "ם ";
				}
				if (day > 20) str += "ו";
			}
		} else if (day == 10) str += "עשרה ";

		if (1 < day && day < 11) str += "ימים ";
		else if (10 < day) str += d[9 + day / 10] + "יום ";

		if (6 < day) {
			str += "שהם ";
			if (day / 7 == 1) str += "שבוע אחד ";
			else str += d[day / 7] + "שבועות ";

			if (day % 7 != 0) {
				str += "ו";
				if (day % 7 == 1) str += "יום אחד ";
				else str += d[day % 7] + "ימים ";
			}
		}
		str += "לעומר";
		count_tv.setText(str);
	}


	private void _a_02_lamnatzeach()
	{
		separated = new ArrayList(Arrays.asList(lamnatzeach.split(" ")));
		index = 0;
		start = "";
		end = "";
		for (int _repeat14 = 0; _repeat14 < (int) (separated.size()); _repeat14++) {
			if (index < (DayToDisplay - 1)) {
				start = start.concat(separated.get((int) (index)).concat(" "));
			} else {
				if (index > (DayToDisplay - 1)) {
					end = end.concat(separated.get((int) (index)).concat(" "));
				} else {
					middle = separated.get((int) (index)).concat(" ");
				}
			}
			index++;
		}
		//----------------------------172---------81-------------------
		SpannableStringBuilder builder = new SpannableStringBuilder();

		android.text.style.ForegroundColorSpan fcs = new android.text.style.ForegroundColorSpan(Color.BLACK);
		android.text.style.ForegroundColorSpan fcs1 = new android.text.style.ForegroundColorSpan(Color.RED);

		SpannableString str1 = new SpannableString(start);
		str1.setSpan(fcs, 0, str1.length(), 0);
		builder.append(str1);

		SpannableString str2 = new SpannableString(middle);
		str2.setSpan(fcs1, 0, str2.length(), 0);
		builder.append(str2);

		SpannableString str3 = new SpannableString(end);
		str3.setSpan(fcs, 0, str3.length(), 0);
		builder.append(str3);
		index = 0;
		first_letter = -1;
		helper = 81;
		for (int _repeat64 = 0; _repeat64 < (int) (lamnatzeach.length()); _repeat64++) {
			temp = (int) lamnatzeach.charAt((int) index);
			//showMessage(String.valueOf((int)lamnatzeach.charAt((int)index)));
			if ((temp < 1488) || (1514 < temp)) {
				helper++;
			}
			if ((index == (DayToDisplay + helper)) && (first_letter == -1)) {
				first_letter = index;
			}
			if (index == ((DayToDisplay + helper) + 1)) {
				break;
			}
			index++;
		}
		if ((DayToDisplay == 21) || (DayToDisplay == 22)) {
			index--;
		}
		SpannableStringBuilder builder1 = new SpannableStringBuilder();

		android.text.style.BackgroundColorSpan fcs2 = new android.text.style.BackgroundColorSpan(Color.GREEN);
		android.text.style.ForegroundColorSpan fcs3 = new android.text.style.ForegroundColorSpan(Color.GREEN);

		SpannableString str4 = new SpannableString(pre_lamnatzeach);
		str4.setSpan(fcs, 0, str4.length(), 0);
		builder1.append(str4);

		SpannableString str5 = new SpannableString(builder);
		str5.setSpan(fcs3, (int) first_letter, (int) index, 0);
		builder1.append(str5);

		lamnatzeach_tv.setText(builder1, TextView.BufferType.SPANNABLE);
	}


	private void _a_03_ana_bekoach()
	{
		separated = new ArrayList(Arrays.asList(ana_bekoach.split(" ")));
		index = 0;
		start = "";
		end = "";
		for (int _repeat15 = 0; _repeat15 < (int) (separated.size()); _repeat15++) {
			if (index < (DayToDisplay - 1)) {
				start = start.concat(separated.get((int) (index)).concat(" "));
			} else {
				if (index > (DayToDisplay - 1)) {
					end = end.concat(separated.get((int) (index)).concat(" "));
				} else {
					middle = separated.get((int) (index)).concat(" ");
				}
			}
			index++;
		}
		_change_style(ana_bekoach_tv, start, middle, end);
	}


	private void _a_04_kabala()
	{
		sefira_tv.setText(sefirot.get((int) ((DayToDisplay - 1) % 7)).concat(" שֶׁבַּ".concat(sefirot.get((int) ((DayToDisplay - 1) / 7)))));
	}
	*/
}
