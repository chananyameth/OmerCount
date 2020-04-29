package com.chananya.OmerCount2;

import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;

import java.util.Calendar;
import java.util.Date;

import static java.lang.StrictMath.abs;

public class JewishTimes
{
	private SettingsManager sm;
	private ComplexZmanimCalendar czc;
	private JewishCalendar jc;
	private JewishDate jd;
	private HebrewDateFormatter hdf;

	public JewishTimes()
	{
		sm = SettingsManager.getInstance();
		czc = new ComplexZmanimCalendar(sm.getLocation());
		jc = new JewishCalendar(nowD());
		jd = new JewishDate(nowD());
		hdf = new HebrewDateFormatter();
		hdf.setHebrewFormat(true);
	}

	private ComplexZmanimCalendar getCzc()
	{
		czc.setCalendar(Calendar.getInstance(sm.getLocation().getTimeZone()));
		return czc;
	}

	public Date nowD()
	{
		return Calendar.getInstance(sm.getLocation().getTimeZone()).getTime();
	}

	public Calendar nowC()
	{
		return Calendar.getInstance(sm.getLocation().getTimeZone());
	}

	public Calendar tommorowOf(Calendar c)
	{
		Calendar temp = (Calendar) c.clone();
		temp.add(Calendar.DAY_OF_MONTH, 1);
		return temp;
	}

	public boolean isNowCloseToTzais(int deltaDifferenceMillis)
	{
		return abs(getTzais().getTime() - nowD().getTime())
				< deltaDifferenceMillis;
	}

	public Date getTzais()
	{
		return getCzc().getTzaisGeonim6Point45Degrees();
	}

	public boolean isNowAfterTzais()
	{
		return getTzais().compareTo(nowD()) < 0;
	}

	public int getNowOmerCount(boolean afterTzais)
	{
		return getOmerCount(nowD(), afterTzais); // returns -1 if it isn't day of omer
	}

	public int getOmerCount(Date d, boolean afterSunset)
	{
		jc.setDate(d);
		int omer = jc.getDayOfOmer() + (afterSunset ? 1 : 0);
		if (omer < 1 || 49 < omer)
			omer = -1;
		return omer;
	}

	public String formatDayOfWeek(Calendar c)
	{
		return formatDayOfWeek(c.getTime());
	}

	public String formatDayOfWeek(Date d)
	{
		jd.setDate(d);
		return hdf.formatDayOfWeek(jd);
	}

	public String format(Calendar c)
	{
		return format(c.getTime());
	}

	public String format(Date d)
	{
		jd.setDate(d);
		return hdf.format(jd);
	}
}
