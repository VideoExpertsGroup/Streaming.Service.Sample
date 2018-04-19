
package com.vxg.cloudsdk.streamland_streamer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity {
	static final public String TAG = "SettingsActivity";

	public static SettingsActivity sThis;
	SharedPreferences settings=null;
	Preference chooser = null;
	
	void set_server_changed(){
		if(settings == null)
			return;
		
		Editor ed = settings.edit();
		ed.putInt("server_changed", 1);
		ed.apply();
	}
	

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		sThis = this;

		addPreferencesFromResource(R.xml.preferences);

		settings = PreferenceManager.getDefaultSharedPreferences(this);

		final Preference server_login = findPreference("login");
		String sLogin = settings.getString("login", "");
		server_login.setSummary(sLogin);

		final Preference server_password = findPreference("password");

		final Preference server = findPreference("server");
		String sserver = settings.getString("server", "10.20.16.10");
		server.setSummary(sserver);

		final Preference test_server = findPreference("test_server");
		test_server.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				//check connection
				test_server.setSummary("Connecting...");

				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						TestServerConnection connection = new TestServerConnection();
						final int ret = connection.do_login(settings.getString("server", "10.20.16.10"), settings.getString("login", ""), settings.getString("password", ""));
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if(ret == 0 || ret == 200){
									test_server.setSummary("Connected");
								}else{
									test_server.setSummary("Connection failed, err="+ret);
								}
							}
						});
					}
				});
				t.start();
				return true;
			}
		});

		server_login.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary((String)newValue);
				set_server_changed();
				return true;
			}
		});

		server_password.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				//preference.setSummary((String)newValue);
				set_server_changed();
				return true;
			}
		});
		
		
		server.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary((String)newValue);
				set_server_changed();
				return true;
			}
		});


	}

	@Override
	public void onPause() {
		super.onPause();
	}


	@Override
	public void onBackPressed() {

		finish();
		
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
	}
	

}
