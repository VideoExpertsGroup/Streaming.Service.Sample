/*
 *
  * Copyright (c) 2011-2017 VXG Inc.
 *
 */


package com.vxg.cloudsdk.streamland_player;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.vxg.cloudsdk.CloudPlayerSDK;
import com.vxg.cloudsdk.CloudSDK;
import com.vxg.cloudsdk.Enums.CloudPlayerEvent;
import com.vxg.cloudsdk.Interfaces.ICloudObject;
import com.vxg.cloudsdk.Interfaces.ICloudPlayerCallback;
import com.vxg.cloudsdk.Objects.CloudPlayerConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import veg.mediaplayer.sdk.MediaPlayerConfig;

public class MainActivity extends Activity implements OnClickListener
{

	public final String TAG = MainActivity.class.getSimpleName();

	public  static AutoCompleteTextView	edtId;
	private Button button_connect;
	private Button button_settings;
	private ProgressBar player_progress;

	private FrameLayout					player_view;
	private CloudPlayerSDK 				player;

	private PlayerPanelControlVisibleTask 	playerPanelControlTask 	= null;

	private MulticastLock multicastLock = null;

	//SET Channel
	//String msAccessToken = "eyJ0b2tlbiI6InNoYXJlLmV5SnphU0k2SURJd2ZRLjVhMjkzMzgxdDEyY2ZmNzgwLmIyS3lsRzZSNVg3MDNWb2sydnlTbFc1Q3B3OCIsImNhbWlkIjoxMjk2NTAsImFjY2VzcyI6IndhdGNoIn0=";
	String msAccessToken=""; //get from TestServer
	boolean USE_TEST_SERVER = true;
	TestServerConnection testServerConnection = new TestServerConnection();

	SharedPreferences settings=null;


    @Override
    public void onCreate(Bundle savedInstanceState)
	{
		setTitle(R.string.app_name);
		super.onCreate(savedInstanceState);

		settings = PreferenceManager.getDefaultSharedPreferences(this);

		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		multicastLock = wifi.createMulticastLock("multicastLock");
		multicastLock.setReferenceCounted(true);
		multicastLock.acquire();

		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

		setContentView(R.layout.view_camera_buy_id);

		CloudSDK.setContext(this);
		CloudSDK.setLogEnable(true);
		CloudSDK.setLogLevel(2);
		Toast.makeText(getApplicationContext(), "CloudSDK ver="+CloudSDK.getLibVersion(), Toast.LENGTH_LONG).show();

		player_view = (FrameLayout) findViewById(R.id.playerView);
		player = new CloudPlayerSDK(player_view, new CloudPlayerConfig(), new ICloudPlayerCallback(){

			@Override
			public void onStatus(final CloudPlayerEvent player_event, final ICloudObject p) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						switch(player_event){
							case SOURCE_CHANGED:
								player.play();
								break;
							case CONNECTING:
								break;
							case CONNECTED:
								break;
							case STARTED:
							case PAUSED:
								if(playerPanelControlTask == null) {
									playerPanelControlTask = new PlayerPanelControlVisibleTask();
									executeAsyncTask(playerPanelControlTask, "");
								}
								player_progress.setVisibility(View.INVISIBLE);
								break;
							case CLOSED:
								setUIDisconnected();
								break;
							case EOS:
								break;
							case SEEK_COMPLETED:
								break;
							case ERROR:
								break;
							case TRIAL_VERSION:
								Toast.makeText(getApplicationContext(), "TRIAL_VERSION", Toast.LENGTH_LONG).show();
								setUIDisconnected();
								break;
						}
					}
				});
			}
		});

		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		edtId = (AutoCompleteTextView)findViewById(R.id.edit_id);
		edtId.setText(msAccessToken ==null?"": msAccessToken);
		//edtId.setVisibility(View.INVISIBLE);

		edtId.setOnEditorActionListener(new OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId,	KeyEvent event)
			{
				if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
				{
					InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					in.hideSoftInputFromWindow(edtId.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					return true;

				}
				return false;
			}
		});

		button_connect = (Button)findViewById(R.id.button_connect);
        button_connect.setOnClickListener(this);

		button_settings = (Button)findViewById(R.id.button_settings);
		button_settings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
				startActivity(intent);
			}
		});

		player_progress = (ProgressBar) findViewById(R.id.player_progress);

		RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_view);
        layout.setOnTouchListener(new OnTouchListener() 
		{
			public boolean onTouch(View v, MotionEvent event) 
			{
				InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if (getWindow() != null && getWindow().getCurrentFocus() != null && getWindow().getCurrentFocus().getWindowToken() != null)
					inputManager.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
				return true;
			}
		});
    }

    public int get_access_token(){
    	if(!USE_TEST_SERVER)
    		return 0;

		int server_changed = settings.getInt("server_changed", 1);
		if(server_changed != 1 && msAccessToken != null && msAccessToken.length()>0){
			return 0;
		}
		msAccessToken = null;

		SharedPreferences.Editor ed = settings.edit();
		ed.putInt("server_changed", 0);
		ed.apply();

    	final String server_addr = settings.getString("server", "10.20.16.10");
		final String login = settings.getString("login", "");
		final String password = settings.getString("password", "");

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				int result = testServerConnection.do_login(server_addr, login, password);
				JSONObject jsonObject = testServerConnection.get_channels();
				if(jsonObject != null){
					try {
						JSONArray objects = jsonObject.getJSONArray("objects");

						// Get First channel from back end
						JSONObject channel = objects.getJSONObject(0);
						msAccessToken = channel.getString("access_token_watch");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				final String sresult = ""+(result != 200?"Connection error="+result:"No channels");

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(msAccessToken != null){
							edtId.setText(msAccessToken);
							startPlayer();
						}else{
							Toast.makeText(getApplicationContext(), sresult, Toast.LENGTH_LONG).show();
						}
					}
				});
			}
		});
		t.start();

		return 0;
	}

	public boolean check_access_token(){
    	if(USE_TEST_SERVER)
    		return true;

    	String ch = edtId.getText().toString();
    	if(ch.length() > 0){
			msAccessToken = ch;
		}
		if(msAccessToken == null || msAccessToken.length()<1){

					new AlertDialog.Builder(this)
									.setTitle("Access token")
									.setMessage("Please set \'Access token\' STRING into msAccessToken variable ")
									.setNeutralButton("OK", null)
									.show();

					return false;
			}
		return true;
	}

	public void onClick(View v)
	{
		try{
			get_access_token();
			if(!check_access_token())
				return ;
			startPlayer();
		} catch (Exception e){
			Log.e(TAG,e.toString());
			return;
		}
    }

    void startPlayer(){
		if(msAccessToken == null || msAccessToken.length()<1)
			return;
		player_progress.setVisibility(View.VISIBLE);
		CloudPlayerConfig conf  = player.getCloneConfig();
		conf.setLatencyControl(3);
		conf.setMinLatency();
		player.setConfig(conf);
		player.setSource(msAccessToken);
		player.play();
	}
 
	protected void onPause()
	{
		Log.e(TAG, "onPause()");
		super.onPause();
	}

	@Override
  	protected void onResume() 
	{
		Log.e(TAG, "onResume()");
		super.onResume();
  	}

  	@Override
	protected void onStart() 
  	{
      	Log.e(TAG, "onStart()");
		super.onStart();
	}

  	@Override
	protected void onStop() 
  	{
  		Log.e(TAG, "onStop()");
		super.onStop();
	}

  	@Override
  	protected void onDestroy() 
  	{
  		Log.e(TAG, "onDestroy()");

		player.close();
		
		System.gc();
		
		if (multicastLock != null) {
		    multicastLock.release();
		    multicastLock = null;
		}		
		super.onDestroy();
   	}	

	protected void setUIDisconnected()
	{
		setTitle(R.string.app_name);
		button_connect.setText("Connect");
		if (playerPanelControlTask != null) {
			playerPanelControlTask.cancel(true);
			playerPanelControlTask = null;
		}
	}



	private class PlayerPanelControlVisibleTask extends AsyncTask<String, Void, Boolean>
	{
		private int time_delay = 500;

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params)
		{
			Runnable uiRunnable = null;
			uiRunnable = new Runnable()
			{
				public void run()
				{
					synchronized(this) { this.notify(); }
				}
			};

			boolean stop = false;
			try
			{
				do
				{
					synchronized ( uiRunnable )
					{
						runOnUiThread(uiRunnable);
						try
						{
							uiRunnable.wait();
						}
						catch ( InterruptedException e ) { stop = true; }
					}

					if (stop) break;

					Thread.sleep(time_delay);
				}
				while(!isCancelled());
			}
			catch (Exception e)
			{
			}

			synchronized ( uiRunnable )
			{
				runOnUiThread(uiRunnable);
				try
				{
					uiRunnable.wait();
				}
				catch ( InterruptedException e ) { stop = true; }
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			super.onPostExecute(result);
			playerPanelControlTask = null;
		}
		@Override
		protected void onCancelled()
		{
			super.onCancelled();
			playerPanelControlTask = null;
		}
	}


	static public <T> void executeAsyncTask(AsyncTask<T, ?, ?> task, T... params)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		}
		else
		{
			task.execute(params);
		}
	}

}
