package com.me.gpsexample;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class AddLocationService extends Service {
	
	// GPSTracker class
    GPSTracker gps;
	
	final long postDelayed = 1000 * 10;
    String IMEI;
    
    JSONParser jsonParser = new JSONParser();
    
    // url to create new product
    private static String url_add_location = "http://192.168.1.6/gpstrack/add_location.php";
    
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    
	
	private static final String TAG = "AddLocationService";
	
	final Handler locHandler = new Handler();
	Runnable locRunnable;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	private static AddLocationService instance = null;
	
	public static boolean isInstanceCreated() { 
		return instance != null; 
	}//met
   
	@Override
	public void onCreate() {

		Toast.makeText(this, "Congrats! MyService Created", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onCreate");
		
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		IMEI = telephonyManager.getDeviceId();
		
		instance = this;
		
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onStart");	
		
	    locRunnable = new Runnable()
	    {
	        @Override
	        public void run()
	        {
	        	// stuff
	        	
	        	logGPS();
	        	
	        	locHandler.postDelayed(this, postDelayed);
	            
	        }
	    };
	    
	    logGPS();
	    
	    locHandler.postDelayed(locRunnable, postDelayed);
		
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "MyService Stopped", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");
		
		locHandler.removeCallbacks(locRunnable);
		
		instance = null;
		
	}
	
	// Custom Methods
	
	public void logGPS () {
		
		gps = new GPSTracker(this);
		 
        // check if GPS enabled     
        if(gps.canGetLocation()){
             
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
             
            new AddLocation(latitude, longitude).execute();
            
        } else {
        	String message = "GPS not enabled!";
        	
        	Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        	Log.d(TAG, message);
        }
		
	}
	
	/**
     * Background Async Task to Create new product
     * */
    class AddLocation extends AsyncTask<String, String, String> {
    	
    	double latitude;
    	double longitude;
    	
    	public AddLocation(double _latitude, double _longitude) {
    		latitude = _latitude;
    		longitude = _longitude;
    	}
    	
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
 
        /**
         * Creating product
         * */
        protected String doInBackground(String... args) {
            String sLatitude = Double.toString( latitude );
            String sLongitude = Double.toString( longitude );
            
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("latitude", sLatitude));
            params.add(new BasicNameValuePair("longitude", sLongitude));
            params.add(new BasicNameValuePair("imei", IMEI));
            
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_add_location,
                    "POST", params);
 
            // check log cat fro response
            Log.d("Create Response", json.toString());
 
            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);
 
                if (success == 1) {
                    // successfully created product
                    
                } else {
                    // failed to create product
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
 
            return null;
        }
 
        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
        }
 
    }
	
}
