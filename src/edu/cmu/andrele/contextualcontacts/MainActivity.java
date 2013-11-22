package edu.cmu.andrele.contextualcontacts;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

public class MainActivity extends Activity implements LocationListener{
	
	private String url = "https://api.foursquare.com/v2/venues/search?ll=40.4457696,-79.9494519&client_id=TRFZGGZKZOOA0GWNFOCQTUHDVDJZCU1JQZSLKHYF3OUUKSE2&client_secret=OBETZ5VJ3QSYAJ5YEQAJU0JR54BX1V2XOIF55VQS3MGT5ARP&v=20121116";
	private SearchResponse lastResponse;
	private TextView locationTextView;
	
	// Location properties
	private LocationManager locationManager;
	LocationListener listenerCoarse;
	LocationListener listenerFine;
	Location location;
	String provider;
	
	private boolean isGpsEnabled;
	private boolean isLocationNetworkEnabled;

//	private void locationSetup() {
//		// set the location manager
//		try {
//			locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//			
//			// Initialize the fine criteria for location providers
//			Criteria fine = new Criteria();
//			fine.setAccuracy(Criteria.ACCURACY_FINE);
//			fine.setAltitudeRequired(false);
//			fine.setBearingRequired(false);
//			fine.setSpeedRequired(true);
//			fine.setCostAllowed(true);
//			fine.setPowerRequirement(Criteria.POWER_HIGH);
//			
//			// Initialize the coarse criteria for location providers
//			Criteria coarse = new Criteria();
//			coarse.setAccuracy(Criteria.ACCURACY_COARSE);
//			coarse.setPowerRequirement(Criteria.POWER_LOW);
//			
//			// Set gps update distance and time
//			int GPS_TIMEUPDATE = 1500;
//			int GPS_DISTANCEUPDATE = 7;
//			
//			// get last known location
//			String provider = locationManager.getBestProvider(coarse, true);
//			location = locationManager.getLastKnownLocation(provider);
//			
//			// setup listener
//			if (listenerFine == null || listenerCoarse == null) {
//				createLocationListeners();
//			}
//			
//			if (listenerFine != null) {
//				locationManager.requestLocationUpdates(locationManager.getBestProvider(fine, true), GPS_TIMEUPDATE, GPS_DISTANCEUPDATE, listenerFine);
//			}
//			
//			if (listenerCoarse != null) {
//				locationManager.requestLocationUpdates(locationManager.getBestProvider(coarse, true), GPS_TIMEUPDATE, GPS_DISTANCEUPDATE, listenerCoarse);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	private void createLocationListeners(){
//		
//		listenerFine = new LocationListener() {
//			public void onStatusChanged(String provider, int status, Bundle extras) {}
//			public void onProviderEnabled(String provider) {}
//			public void onProviderDisabled(String provider) {}
//			public void onLocationChanged(Location location) {
//				if (location.getAccuracy() > 500 && location.hasAccuracy()){
//					locationManager.removeUpdates(listenerFine);
//				} else {
//					// Do something else with location updates
//					Log.d("Andre", "Fine location updated!");
//					if (isNetworkAvailable()) {
//						new FoursquareChecker().execute(url);
//					}
//				}
//			}
//		};
//		
//		listenerCoarse = new LocationListener() {
//			public void onStatusChanged(String provider, int status, Bundle extras) {}
//			public void onProviderEnabled(String provider) {}
//			public void onProviderDisabled(String provider) {}
//			public void onLocationChanged(Location location) {
//				if (location.getAccuracy() < 500 && location.hasAccuracy()){
//					locationManager.removeUpdates(listenerCoarse);
//				} else {
//					// Do something else with location updates
//					Log.d("Andre", "Coarse location updated!");
//					if (isNetworkAvailable()) {
//						new FoursquareChecker().execute(url);
//					}
//				}
//			}
//		};
//	}
//	
//	private void stopListening() {
//		locationManager.removeUpdates(this);
//	}
//	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove the title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		locationTextView = (TextView)findViewById(R.id.textView1);
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(provider);
		
		if (location != null) {
			Log.d("Andre", "Provider " + provider + " has been selected.");
			onLocationChanged(location);
		} else {
//			locationTextView.setText("Searching nearby...");
		}
		
		// Set up GPS and network checks
//		isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		isLocationNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
				

		setContentView(R.layout.activity_main);

		
		TabHost tabHost=(TabHost)findViewById(android.R.id.tabhost);
		tabHost.setup();

		TabSpec spec1=tabHost.newTabSpec("Add Contact");
		spec1.setContent(R.id.tab1);
		spec1.setIndicator("Add Contact");

		TabSpec spec2=tabHost.newTabSpec("History");
		spec2.setIndicator("History");
		spec2.setContent(R.id.tab2);

		tabHost.addTab(spec1);
		tabHost.addTab(spec2);
		
		lastResponse = null;

		Log.d("andre", "OnCreate called");
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		locationManager.requestLocationUpdates(provider, 400, 1, this);
		Log.d("Andre", "onResume called.");
		locationTextView = (TextView)findViewById(R.id.textView1);
//        android.location.Location lastLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        if (lastLoc != null) {
//    		url = "https://api.foursquare.com/v2/venues/search?ll="+lastLoc.getLatitude()+","+lastLoc.getLongitude()+"&client_id=TRFZGGZKZOOA0GWNFOCQTUHDVDJZCU1JQZSLKHYF3OUUKSE2&client_secret=OBETZ5VJ3QSYAJ5YEQAJU0JR54BX1V2XOIF55VQS3MGT5ARP&v=20121116";
//    		if (isNetworkAvailable()) {
//    			new FoursquareChecker().execute(url);
//    		}
//        }

	}
	
	@Override
	protected void onPause(){
		super.onPause();
		locationManager.removeUpdates(this);
	}
	
	@Override
	public void onLocationChanged(Location location) {
		Log.d("Andre", "Lat: " + location.getLatitude() + " Long: " + location.getLongitude());
		url = "https://api.foursquare.com/v2/venues/search?ll="+location.getLatitude()+","+location.getLongitude()+"&client_id=TRFZGGZKZOOA0GWNFOCQTUHDVDJZCU1JQZSLKHYF3OUUKSE2&client_secret=OBETZ5VJ3QSYAJ5YEQAJU0JR54BX1V2XOIF55VQS3MGT5ARP&v=20121116";
		if (isNetworkAvailable()) {
			new FoursquareChecker().execute(url);
		}
	}
	
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
	    Toast.makeText(this, "Enabled new provider " + provider,
	            Toast.LENGTH_SHORT).show();
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	    Toast.makeText(this, "Enabled new provider " + provider,
	            Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onStop(){
		super.onStop();
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private class FoursquareChecker extends AsyncTask<String, Integer, SearchResponse> {
		protected SearchResponse doInBackground(String... urls) {

			InputStream source = retreiveStream(urls[0]);
			Log.d("MINE", "The source is " + source);
			Gson gson = new Gson();
			
			Reader reader = new InputStreamReader(source);
			
			SearchResponse searchResponse = gson.fromJson(reader, SearchResponse.class);
			
			Log.d("MINE", "SR responses is: " + searchResponse.response);
			
			return searchResponse;
			
		}
		
		protected void onProgressUpdate(Integer... progress) {
			
		}
		
		protected void onPostExecute(SearchResponse result) {
			if (result != null) {
				if (!result.response.venues.isEmpty()) {
					lastResponse = result;
					Log.d("MINE", "Result: " + result.response.venues.get(0).name);
					if (locationTextView != null) {
						locationTextView.setText(result.response.venues.get(0).name);
					}
				} else {
					if (locationTextView != null) {
						locationTextView.setText("No nearby results found");
					}
				}
			}
		}
		
		private InputStream retreiveStream(String url) {
			DefaultHttpClient client = new DefaultHttpClient();
			
			HttpGet getRequest = new HttpGet(url);
			
			try {
				HttpResponse getResponse = client.execute(getRequest);
				final int statusCode = getResponse.getStatusLine().getStatusCode();
				
				if (statusCode != HttpStatus.SC_OK) {
					Log.w(getClass().getSimpleName(), "Error " + statusCode + " for URL " + url);
					return null;
				}
				
				HttpEntity getResponseEntity = getResponse.getEntity();
				return getResponseEntity.getContent();

			} catch (IOException e) {
				getRequest.abort();
				Log.w("MINE", e.getMessage());
				Log.w(getClass().getSimpleName(), "Error for URL " + url, e);
			}
			
			return null;
		}
	}




}
