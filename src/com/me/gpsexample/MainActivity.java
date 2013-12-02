package com.me.gpsexample;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.bool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

	Button btnToggleLocation;
    ListView locList;
    // GPSTracker class
    GPSTracker gps;
    
    ArrayList<String> locArray;
    ArrayAdapter<String> adapter;
    
    double initDistanceLat;
    double initDistanceLon;
    
    final long postDelayed = 1000 * 10;
    String IMEI;
    
    JSONParser jsonParser = new JSONParser();
    
    // url to create new product
    private static String url_add_location = "http://192.168.1.6/gpstrack/add_location.php";
    
 // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCTS = "products";
    private static final String TAG_PID = "pid";
    private static final String TAG_NAME = "name";
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		btnToggleLocation = (Button) findViewById(R.id.btnToggleLocation);
		locList = (ListView) findViewById(R.id.locList);
		
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		IMEI = telephonyManager.getDeviceId();
		
		final Handler locHandler = new Handler();
		
	    final Runnable locRunnable = new Runnable()
	    {
	        private long time = 0;

	        @Override
	        public void run()
	        {
	        	// stuff
	        	
	        	logGPS();
	        	
	        	// do stuff then
	            // can call h again after work!
	            time += 1000;
	            Log.d("TimerExample", "Going for... " + time);
	            locHandler.postDelayed(this, postDelayed);
	            
	        }
	    };
	    
        // show location button click event
	    btnToggleLocation.setOnClickListener(new OnClickListener() {
	    	
	    	boolean toggleStatus = false;
	    	
			@Override
			public void onClick(View v) {
				
				if ( toggleStatus ) {
					locHandler.removeCallbacks(locRunnable);
					toggleStatus = false;
					btnToggleLocation.setText("Start Tracking");
				} else {
					
					logGPS();
					
					locHandler.postDelayed(locRunnable, postDelayed); // 1 second delay (takes millis)
					toggleStatus = true;
					btnToggleLocation.setText("Stop Tracking");
				}
			}
		});
        
		locArray = new ArrayList<String>();
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,  
				locArray);
		locList.setAdapter(adapter);
		
	}
	
	public void logGPS () {
		
		gps = new GPSTracker(MainActivity.this);
		 
        // check if GPS enabled     
        if(gps.canGetLocation()){
             
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
             
            // \n is for new line
            // Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();    
            
            if ( (int) initDistanceLat == 0 ) {
            	
            	initDistanceLat = latitude;
            	initDistanceLon = longitude;
            }
            
            //addLocs( "Lat: " + latitude + "\nLong: " + longitude );
            
            Calendar cal = Calendar.getInstance();
        	cal.getTime();
        	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        	
            double distance = distFrom(initDistanceLat, initDistanceLon, latitude, longitude);
            addLocs( sdf.format(cal.getTime()) + ": " + Double.toString( distance ) + " in miles" );
//            addLocs( sdf.format(cal.getTime()) + ": " + Double.toString( distance / 1609.34 ) + " in meters" );
            
//            addLocs(initDistanceLat + " " + initDistanceLon);
//            addLocs(latitude + " " + longitude);
            
            new AddLocation(latitude, longitude).execute();
            
        } else {
        	addLocs( "GPS not enabled!" ); 
        }
		
	}
	
	public void addLocs (String item) {
		
		this.locArray.add(item);
        this.adapter.notifyDataSetChanged();
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 3958.75;
		double dLat = Math.toRadians(lat2-lat1);
		double dLng = Math.toRadians(lng2-lng1);
		double sindLat = Math.sin(dLat / 2);
		double sindLng = Math.sin(dLng / 2);
		double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
		* Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double dist = earthRadius * c;

		return dist;
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
                    
                    // closing this screen
                    finish();
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
