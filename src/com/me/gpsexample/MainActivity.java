package com.me.gpsexample;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends Activity {

	Button btnToggleLocation;
    ListView locList;
    // GPSTracker class
    GPSTracker gps;
    
    ArrayList<String> locArray;
    ArrayAdapter<String> adapter;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		btnToggleLocation = (Button) findViewById(R.id.btnToggleLocation);
		locList = (ListView) findViewById(R.id.locList);
		
	    
        // show location button click event
	    btnToggleLocation.setOnClickListener(new OnClickListener() {
	    	
			@Override
			public void onClick(View v) {
				
				boolean toggleStatus = AddLocationService.isInstanceCreated();
				
				if ( toggleStatus ) {
					
					// stop service
					stopService(new Intent(MainActivity.this, AddLocationService.class));
					
					toggleStatus = false;
					btnToggleLocation.setText("Start Tracking");
				} else {
					
					// start service
					startService(new Intent(MainActivity.this, AddLocationService.class));
					
					
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
	
}
