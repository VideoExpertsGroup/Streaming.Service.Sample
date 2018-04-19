/*
 *
  * Copyright (c) 2011-2017 VXG Inc.
 *
 */


package com.vxg.cloudsdk.streamland_streamer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.vxg.cloudsdk.CloudSDK;
import com.vxg.cloudsdk.CloudStreamerSDK;
import com.vxg.cloudsdk.Interfaces.ICloudStreamerCallback;
import com.vxg.cloudsdk.Objects.CloudCamera;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;

import veg.mediacapture.sdk.MediaCapture;
import veg.mediacapture.sdk.MediaCaptureCallback;
import veg.mediacapture.sdk.MediaCaptureConfig;

public class MainActivity extends Activity implements OnClickListener, MediaCaptureCallback
{

	private static final String TAG 	 = "streamland_streamer";

	public  static AutoCompleteTextView	edtId;
	private Button						btnConnect;
	private Button button_settings;

	CloudStreamerSDK mCloudStreamer;
	boolean     mStreamerStarted = false;
	CloudCamera mCloudCamera;
	MediaCapture capturer;
	boolean misSurfaceCreated = false;
	private ImageView 			led;
	private TextView 			captureStatusText = null;
	private TextView 			captureStatusText2 = null;
	private TextView			captureStatusStat = null;
	String rtmp_url = "";
	private static final boolean USE_PORTRAIT_MODE = false;

	private MulticastLock multicastLock = null;

	//SET Channel
	//String msAccessToken = "";//"eyJ0b2tlbiI6InNoYXJlLmV5SnphU0k2SURFd05IMC41YTM4ZWVjMXQxMmNmZjc4MC44RTN1cC0waFItVjJxVm9GWGc3SXJVdG1sczgiLCJjYW1pZCI6MTMwOTEwLCJjbW5ncmlkIjoxMzEyNjAsImFjY2VzcyI6InN0cmVhbWluZyJ9";
	String msAccessToken=""; //get from TestServer
	boolean USE_TEST_SERVER = true;
	TestServerConnection testServerConnection = new TestServerConnection();

	SharedPreferences settings=null;

	public boolean isRec(){
		return ( capturer != null && capturer.getState() == MediaCapture.CaptureState.Started );
	}

	// Event handler
	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			MediaCapture.CaptureNotifyCodes status = (MediaCapture.CaptureNotifyCodes) msg.obj;
			//Log.i(TAG, "=Status handleMessage status="+status);

			String strText = null;
			int rtmp_status = capturer.getRTMPStatus();

			switch (status)
			{
				case CAP_OPENED:
					strText = "Opened";
					break;
				case CAP_SURFACE_CREATED:
					strText = "Camera surface created surfaceView="+capturer.getSurfaceView();
					misSurfaceCreated = true;
					break;
				case CAP_SURFACE_DESTROYED:
					strText = "Camera surface destroyed";
					misSurfaceCreated = false;
					break;
				case CAP_STARTED:
					strText = "Started";
					break;
				case CAP_STOPPED:
					strText = "Stopped";
					if (rtmp_status == (-999)) {
						String sss = "Streaming stopped. DEMO VERSION limitation";
						captureStatusText.setText(sss);
					}else {
						captureStatusText.setText("");
					}
					captureStatusStat.setText("");
					break;
				case CAP_CLOSED:
					strText = "Closed";
					break;
				case CAP_ERROR:
					strText = "Error";
					//break;
				case CAP_TIME:
					if(isRec()) {
						int dur = (int) (long) capturer.getDuration() / 1000;
						int v_cnt = capturer.getVideoPackets();
						int a_cnt = capturer.getAudioPackets();
						long v_pts = capturer.getLastVideoPTS();
						long a_pts = capturer.getLastAudioPTS();
						int nreconnects = capturer.getStatReconnectCount();

						String sss = "";
						String sss2 = "";
						int min = dur / 60;
						int sec = dur - (min * 60);
						sss = String.format("%02d:%02d", min, sec);
						if (rtmp_status == (-999)) {
							sss = "Streaming stopped. DEMO VERSION limitation";
							capturer.Stop();
							led.setImageResource(R.drawable.led_green);
							//mbuttonRec.setImageResource(R.drawable.ic_fiber_manual_record_red);
						} else if (rtmp_status != (-1)) {

							if (capturer.USE_RTSP_SERVER) {
								sss += ". RTSP ON (" + capturer.getRTSPAddr() + ")";
								sss2 += "v:" + v_cnt + " a:" + a_cnt + " rcc:" + nreconnects;
							} else {
								sss += ". RTMP " + ((rtmp_status == 0) ? "ON ( " + rtmp_url + " )" : "Err:" + rtmp_status);
								//sss += ". RTMP "+ ((rtmp_status == 0)?"ON ":"Err:"+rtmp_status);
								if (rtmp_status == (-5)) {
									sss += " Server not connected ( " + rtmp_url + " )";
								} else if (rtmp_status == (-12)) {
									sss += " Out of memory";
								}
								sss2 += "v:" + v_cnt + " a:" + a_cnt + " rcc:" + nreconnects;
								sss2 += "\nv_pts: " + v_pts + " a_pts: " + a_pts + " delta: " + (v_pts - a_pts);
							}

						} else {
							// rtmp_status == (-1)
							if (rtmp_status == (-999)) {
								sss = "Streaming stopped. DEMO VERSION limitation";
							}
						}

						captureStatusText.setText(sss);
						captureStatusStat.setText(sss2);
					}else{
						if (rtmp_status == (-999)) {
							String sss = "Streaming stopped. DEMO VERSION limitation";
							captureStatusText.setText(sss);
						}else {
							captureStatusText.setText("");
						}
						captureStatusStat.setText("");
					}
					break;

				default:
					break;
			}
			if(strText != null){
				Log.i(TAG, "=Status handleMessage str="+strText);
			}
		}
	};

	// All event are sent to event handlers
	@Override
	public int OnCaptureStatus(int arg) {
		MediaCapture.CaptureNotifyCodes status = MediaCapture.CaptureNotifyCodes.forValue(arg);
		if (handler == null || status == null)
			return 0;

		//Log.v(TAG, "=OnCaptureStatus status=" + arg);
		switch (MediaCapture.CaptureNotifyCodes.forValue(arg))
		{
			default:
				Message msg = new Message();
				msg.obj = status;
				handler.sendMessage(msg);
		}

		return 0;
	}

	// callback from Native Capturer
	@Override
	public int OnCaptureReceiveData(ByteBuffer buffer, int type, int size,
									long pts) {

		return 0;
	}

	@Override
    public void onCreate(Bundle savedInstanceState)
	{
		setTitle(R.string.app_name);
		super.onCreate(savedInstanceState);

		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		multicastLock = wifi.createMulticastLock("multicastLock");
		multicastLock.setReferenceCounted(true);
		multicastLock.acquire();

		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

		setContentView(R.layout.activity_streamer);

		settings = PreferenceManager.getDefaultSharedPreferences(this);

		if(USE_PORTRAIT_MODE){
			//set portrait mode
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}else{
			//set landscape mode
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}


		CloudSDK.setContext(this);
		CloudSDK.setLogEnable(true);
		CloudSDK.setLogLevel(2);
		Toast.makeText(getApplicationContext(), "CloudSDK ver="+CloudSDK.getLibVersion(), Toast.LENGTH_LONG).show();

		led = (ImageView)findViewById(R.id.led);
		led.setImageResource(R.drawable.led_green);

		captureStatusText = (TextView)findViewById(R.id.statusRec);
		captureStatusStat = (TextView)findViewById(R.id.statusStat);
		captureStatusStat.setText("");

		captureStatusText2 = (TextView)findViewById(R.id.statusRec2);
		captureStatusText2.setText("");

		capturer = (MediaCapture)findViewById(R.id.captureView);
		capturer.RequestPermission(this);

		if(USE_PORTRAIT_MODE){
			capturer.getConfig().setvideoOrientation(90); //portrait
		}else{
			capturer.getConfig().setvideoOrientation(0); //landscape
		}

		//audio
		capturer.getConfig().setAudioFormat(MediaCaptureConfig.TYPE_AUDIO_AAC);
		capturer.getConfig().setAudioBitrate(128);
		capturer.getConfig().setAudioSamplingRate(44100);
		capturer.getConfig().setAudioChannels(2);

		//video
		capturer.getConfig().setSecVideoFramerate(30);
		capturer.getConfig().setVideoBitrate(800);
		capturer.getConfig().setVideoResolution(MediaCaptureConfig.CaptureVideoResolution.VR_640x480);

		if (capturer.getConfig().getCaptureSource() != MediaCaptureConfig.CaptureSources.PP_MODE_VIRTUAL_DISPLAY.val())
			capturer.Open(null, this);

		mCloudStreamer = new CloudStreamerSDK(new ICloudStreamerCallback() {
			@Override
			public void onStarted(String surl) {
				Log.v(TAG, "=>onStarted surl="+surl);
				rtmp_url = surl;
				capturer.getConfig().setUrl(surl);
				capturer.StartStreaming();
			}

			@Override
			public void onStopped() {
				Log.v(TAG, "<=onStopeed");
				capturer.StopStreaming();

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						captureStatusText.setText("No Clients");
						captureStatusStat.setText("");
					}
				});
			}

			@Override
			public void onError(final int result) {
				Log.v(TAG, "=onError");

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getApplicationContext(), "Error="+result, Toast.LENGTH_LONG).show();
						stopStreamer();
					}
				});
			}

			@Override
			public void onCameraConnected() {
				mCloudCamera = (CloudCamera)mCloudStreamer.getCamera();
				if(mStreamerStarted)
					mCloudStreamer.Start();
			}
		});
		if( !(msAccessToken == null || msAccessToken.length()<1) )
			mCloudStreamer.setSource(msAccessToken); //see onStatus()

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

		btnConnect = (Button)findViewById(R.id.button_connect);
        btnConnect.setOnClickListener(this);
		btnConnect.setText("Start");

		button_settings = (Button)findViewById(R.id.button_settings);
		button_settings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
				startActivity(intent);
			}
		});


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

					JSONArray objects = null;
					try {
						objects = jsonObject.getJSONArray("objects");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					int i=0;
					int cnt = (objects!=null?objects.length():0);
					for (i=0; i<cnt; i++) {
						try {

							JSONObject channel = objects.getJSONObject(i);
							if (!channel.has("access_token_all") || channel.isNull("access_token_all"))
								continue;
							msAccessToken = channel.getString("access_token_all");
							if(msAccessToken != null)
								break;
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}

				final String sresult = ""+(result != 200?"Connection error="+result:"No channels");

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(msAccessToken != null){
							edtId.setText(msAccessToken);
							startStreamer();
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
		if(mStreamerStarted) {
			stopStreamer();
			return;
		}

		get_access_token();

		if(!check_access_token())
			return ;

		startStreamer();
    }

	void startStreamer(){
		if(msAccessToken == null || msAccessToken.length()<1)
			return;

		if(!misSurfaceCreated){
			Toast.makeText(getApplicationContext(), "Camera not ready yet", Toast.LENGTH_LONG).show();
			return;
		}

		mCloudStreamer.setSource(msAccessToken);
		if(mCloudCamera != null)
			mCloudStreamer.Start();
		btnConnect.setText("Stop");
		captureStatusText.setText("No clients");
		captureStatusStat.setText("");
		mStreamerStarted = true;
	}

	void stopStreamer(){
		if(!mStreamerStarted) {
			return;
		}

		capturer.StopStreaming();
		mCloudStreamer.Stop();
		btnConnect.setText("Start");
		mStreamerStarted = false;

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

		stopStreamer();
	}

  	@Override
  	protected void onDestroy() 
  	{
  		Log.e(TAG, "onDestroy()");

		mCloudStreamer.Stop();
		capturer.StopStreaming();
		
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
		btnConnect.setText("Connect");
	}

}
