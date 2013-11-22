package edu.cmu.andrele.contextualcontacts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
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
	
	// Camera properties
	public static final int MEDIA_TYPE_IMAGE = 1;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private Uri fileUri;
	public ImageButton imageButtonAvatar;
	
	private boolean isGpsEnabled;
	private boolean isLocationNetworkEnabled;

	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "ContextualContacts");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("MyCameraApp", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else {
	        return null;
	    }

	    return mediaFile;
	}
	
	private void dispatchImageCaptureIntent() {
		// Setup camera intent
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove the title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		// Set up GPS and network checks
//		isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//		isLocationNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		
		// Setup location services
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
		
		imageButtonAvatar = (ImageButton)findViewById(R.id.imageButtonAvatar);
		
		imageButtonAvatar.setOnClickListener(new ImageButton.OnClickListener() {
			@Override
			public void onClick(View v) {
				dispatchImageCaptureIntent();
			}
		});
		
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			  // Image captured and saved to fileUri specified in the Intent
			Toast.makeText(this, "Image saved", Toast.LENGTH_LONG).show();
			
			try {
				ContentResolver cr = getContentResolver();
				InputStream in = cr.openInputStream(fileUri);
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize=4;
				options.inScaled=true;
				Bitmap thumb = BitmapFactory.decodeStream(in, null, options);
				imageButtonAvatar.setImageBitmap(thumb);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
        } else if (resultCode == RESULT_CANCELED) {
            // User cancelled the image capture
            Toast.makeText(this, "Image capture cancelled.", Toast.LENGTH_LONG).show();
        } else {
            // Image capture failed, advise user
            Toast.makeText(this, "Image capture failed.", Toast.LENGTH_LONG).show();

        }
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
