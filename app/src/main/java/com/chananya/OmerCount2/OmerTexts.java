package com.chananya.OmerCount2;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

public class OmerTexts
{
	private String leshem_yichud;
	private String beracha;
	//private String hayom;
	private String harachaman;
	private String pre_lamnatzeach;
	private String lamnatzeach;
	private String ana_bekoach;
	private String ribono_1;
	private String sheba;
	private String ribono_2;

	private int day; // 1 to 49
	private String[] sefirot;
	private Context context;

	public OmerTexts(Context _cont, int _day)
	{
		setDay(_day);
		context = _cont;
		sefirot = new String[7];

		readFile();
	}

	private String loadJSONFromAsset()
	{
		String json;
		try {
			InputStream is = context.getAssets().open("Texts.json");
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			json = new String(buffer, StandardCharsets.UTF_8);
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		return json;
	}

	private void readFile()
	{
		try {
			JSONObject obj = new JSONObject(loadJSONFromAsset());
			String our_nusach_string = obj.getString("our_nusach");

			JSONObject jo_our_nusach = new JSONObject(our_nusach_string);

			leshem_yichud = jo_our_nusach.getString("leshem_yichud");
			beracha = jo_our_nusach.getString("beracha");
			harachaman = jo_our_nusach.getString("harachaman");
			pre_lamnatzeach = jo_our_nusach.getString("pre_lamnatzeach");
			lamnatzeach = jo_our_nusach.getString("lamnatzeach");
			ana_bekoach = jo_our_nusach.getString("ana_bekoach");
			ribono_1 = jo_our_nusach.getString("ribono_1");
			sheba = jo_our_nusach.getString("sheba");
			ribono_2 = jo_our_nusach.getString("ribono_2");

			JSONArray ja_sefirot = jo_our_nusach.getJSONArray("sefirot");
			for (int i = 0; i < ja_sefirot.length(); ++i) {
				JSONObject jo_inside = ja_sefirot.getJSONObject(i);
				sefirot[i] = jo_inside.getString("sefira");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	void setDay(int _day)
	{
		if (_day < 1 || 49 < _day)
			_day = 1;
		day = _day;
	}

	private String hayom()
	{
		String str = "";
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

		return str;
	}

	private String sheba()
	{
		if (day < 1 || day > 49)
			day = 1;
		return sefirot[(day - 1) % 7] + sheba + sefirot[(day - 1) / 7];
	}

	private SpannableStringBuilder lamnatzeach()
	{
		ArrayList<String> separated = new ArrayList(Arrays.asList(lamnatzeach.split(" ")));
		String beginning = "";
		String today_word = "";
		String end = "";

		int word_beggining = 0;
		int word_end = -1;

		// split by word
		for (int i = 0; i < separated.size(); i++) {
			if (i < day - 1)
				beginning = beginning.concat(separated.get(i).concat(" "));
			else if (i > day - 1)
				end = end.concat(separated.get(i).concat(" "));
			else
				today_word = separated.get(i).concat(" ");
		}

		SpannableStringBuilder builder = new SpannableStringBuilder();

		ForegroundColorSpan fcs_black = new ForegroundColorSpan(Color.BLACK);
		ForegroundColorSpan fcs_red = new ForegroundColorSpan(Color.RED);
		ForegroundColorSpan fcs_green = new ForegroundColorSpan(Color.GREEN);
		BackgroundColorSpan bcs_green = new BackgroundColorSpan(Color.GREEN);

		SpannableString str1 = new SpannableString(beginning);
		str1.setSpan(fcs_black, 0, str1.length(), 0);
		builder.append(str1);

		SpannableString str2 = new SpannableString(today_word);
		str2.setSpan(fcs_red, 0, str2.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
		builder.append(str2);

		SpannableString str3 = new SpannableString(end);
		str3.setSpan(fcs_black, 0, str3.length(), 0);
		builder.append(str3);

		// ----- now 'builder' has all the text, with today's word highlighted -----

		int i = 0;
		int letter_beggining = -1;
		int letter_end = -1;
		// helper: counts non-hebrew letters (like nikkud, parenthesis and spaces)
		// plus all hebrew letters (without spaces) from the start of pasuk B
		// (doesn't include pasuk A yet) till pasuk E (the one of the letter-per-day)
		int helper = 81; // 81 hebrew letters before Yismechu Viranenu

		for (; i < lamnatzeach.length(); i++) {
			int temp = lamnatzeach.charAt(i);
			//showMessage(String.valueOf((int)lamnatzeach.charAt((int)index)));
			if ((temp < 1488) || (1514 < temp)) { // 1488=alef, 1514=tav
				// if lamnatzeach.charAt(i) is *not* a hebrew letter
				helper++;
			}
			if ((i == day + helper) && (letter_beggining == -1)) {
				letter_beggining = i;
			}
			if (i == day + helper + 1) {
				break;
			}
		}
		letter_end = i; // i now is the index of the *end* of today_letter (plus nikud)

		if ((day == 21) || (day == 22)) { // those 2 days' letters include unnecessary parenthesis: Tishp(o)t
			letter_end--;
		}

		builder.setSpan(fcs_green, letter_beggining, letter_end, 0);

		// add the first pasuk
		builder.insert(0, pre_lamnatzeach, 0, pre_lamnatzeach.length());
		builder.setSpan(fcs_black, 0, pre_lamnatzeach.length(), 0);

		return builder;
	}

	private SpannableStringBuilder ana_bekoach()
	{
		ArrayList<String> separated = new ArrayList(Arrays.asList(ana_bekoach.split(" ")));
		String beggining = "";
		String today = "";
		String end = "";

		for (int i = 0; i < separated.size(); i++) {
			if (i < day - 1)
				beggining = beggining.concat(separated.get(i).concat(" "));
			else if (i > day - 1)
				end = end.concat(separated.get(i).concat(" "));
			else //matches today
				today = separated.get(i).concat(" ");
		}

		SpannableStringBuilder builder = new SpannableStringBuilder();

		ForegroundColorSpan fcs_black = new ForegroundColorSpan(Color.BLACK);
		ForegroundColorSpan fcs_red = new ForegroundColorSpan(Color.RED);

		SpannableString str1 = new SpannableString(beggining);
		str1.setSpan(fcs_black, 0, str1.length(), 0);
		builder.append(str1);

		SpannableString str2 = new SpannableString(today);
		str2.setSpan(fcs_red, 0, str2.length(), 0);
		builder.append(str2);

		SpannableString str3 = new SpannableString(end);
		str3.setSpan(fcs_black, 0, str3.length(), 0);
		builder.append(str3);

		return builder;
	}


	public String getLeshem_yichud()
	{
		return leshem_yichud;
	}

	public String getBeracha()
	{
		return beracha;
	}

	public String getHayom()
	{
		return hayom();
	}

	public String getHarachaman()
	{
		return harachaman;
	}

	public SpannableStringBuilder getLamnatzeach()
	{
		return lamnatzeach();
	}

	public SpannableStringBuilder getAna_bekoach()
	{
		return ana_bekoach();
	}

	public String getRibono()
	{
		return ribono_1 + " " + sheba() + " " + ribono_2;
	}
}
