package com.chananya.OmerCount2;

import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;
import net.sourceforge.zmanim.util.GeoLocation;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static java.lang.StrictMath.abs;

public class JewishTimes
{
	private ComplexZmanimCalendar czc;
	private JewishCalendar jc;
	private JewishDate jd;
	private HebrewDateFormatter hdf;

	private Date dateToDisplay;

	public JewishTimes()
	{
		czc = new ComplexZmanimCalendar(SettingsActivity.getLocation());
		jc = new JewishCalendar(now());
		jd = new JewishDate(now());
		hdf = new HebrewDateFormatter();
		hdf.setHebrewFormat(true);

		// TODO: move to settings class - make static functions
		{
			String locationName = "Jerusalem, IL";
			double latitude = 31.771959; // Jerusalem, IL
			double longitude = 35.217018; // Jerusalem, IL
			double elevation = 798; // optional elevation correction in Meters
			TimeZone timeZone = TimeZone.getTimeZone("Israel");
			GeoLocation location = new GeoLocation(locationName, latitude, longitude, elevation, timeZone);
		}
	}

	private ComplexZmanimCalendar getCzc()
	{
		czc.setCalendar(Calendar.getInstance(SettingsActivity.getLocation().getTimeZone()));
		return czc;
	}
	private Date now()
	{
		return Calendar.getInstance(SettingsActivity.getLocation().getTimeZone()).getTime();
	}

	public boolean isNowCloseToTzais(int deltaDifferenceMillis)
	{
		return abs(getTzais().getTime() - now().getTime())
				< deltaDifferenceMillis;
	}
	public Date getTzais()
	{
		return getCzc().getTzaisGeonim6Point45Degrees();
	}
	public boolean isNowAfterTzais()
	{
		return getTzais().compareTo(now()) > 0;
	}
	public int getNowOmerCount()
	{
		jc.setDate(now());
		return jc.getDayOfOmer(); // returns -1 if it isn't day of omer
	}
	public int getOmerCount(Date d, boolean afterSunset)
	{
		jc.setDate(d);
		int omer = jc.getDayOfOmer() + (afterSunset ? 1 : 0);
		if(omer < 1 || 49 < omer)
			omer = -1;
		return omer;
	}

	public String formatDayOfWeek(Date d)
	{
		jd.setDate(d);
		return hdf.formatDayOfWeek(jd);
	}
	public String format(Date d)
	{
		jd.setDate(d);
		return hdf.format(jd);
	}
}
