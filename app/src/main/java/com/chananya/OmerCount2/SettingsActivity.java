package com.chananya.OmerCount2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity
{
	// settings vars: initialize to -1 or "", and on the first access load from SharedPreferences.
	// when activity stops: save all settings vars to SharedPreferences.

	private SettingsManager sm;

	// constants:
	public static final int ALARM_MODE_HOUR = 0;
	public static final int ALARM_MODE_RELATIVE = 1;

	// views:
	private Spinner nusakh_sp;
	private CheckBox activate_alarm_cb;
	private LinearLayout linearAlarms;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		sm = SettingsManager.getInstance();

		setSpinner();
		activate_alarm_cb = findViewById(R.id.activate_alarm);
		linearAlarms = findViewById(R.id.linearAlarms);

		applySettingsFromSM();
	}

	private void applySettingsFromSM()
	{
		nusakh_sp.setSelection(sm.nusakh);
		activate_alarm_cb.setChecked(sm.isAlarmActive);
		enableDisableView(linearAlarms, sm.isAlarmActive);

		//TODO: set alarm settings
	}

	private void setSpinner()
	{
		nusakh_sp = findViewById(R.id.nusakh);
		nusakh_sp.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, SettingsManager.nusakhList));
		nusakh_sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
			{
				//TODO
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView)
			{
			}
		});
	}

	@Override
	protected void onStop()
	{
		sm.nusakh = nusakh_sp.getSelectedItemPosition();
		sm.isAlarmActive = activate_alarm_cb.isChecked();
		//TODO: save alarm mode

		sm.saveSettings();
		super.onStop();
	}


	public void activate_alarmClicked(View view)
	{
		enableDisableView(linearAlarms, ((CheckBox) view).isChecked());
	}

	private void enableDisableView(View view, boolean enabled)
	{
		view.setEnabled(enabled);

		if (view instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) view;

			for (int idx = 0; idx < group.getChildCount(); idx++)
				enableDisableView(group.getChildAt(idx), enabled);
		}
	}

	public void onRadioButtonClicked(View view)
	{
	}


	private void toast(String s)
	{
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}
}
