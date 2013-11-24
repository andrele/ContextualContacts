package edu.cmu.andrele.contextualcontacts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.gson.Gson;

public class MainActivity extends ListActivity implements LocationListener, OnClickListener, GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener {
	
    // Debugging tag for the application
    public static final String APPTAG = "ContextualContaxts";
	
	private String url = "https://api.foursquare.com/v2/venues/search?ll=40.4457696,-79.9494519&client_id=TRFZGGZKZOOA0GWNFOCQTUHDVDJZCU1JQZSLKHYF3OUUKSE2&client_secret=OBETZ5VJ3QSYAJ5YEQAJU0JR54BX1V2XOIF55VQS3MGT5ARP&v=20121116";
	private SearchResponse lastResponse;
	private TextView locationTextView;
	
	// Location properties
//	private LocationManager locationManager;
//	LocationListener listenerCoarse;
//	LocationListener listenerFine;
	Location currentLocation;
	String provider;
	private LocationClient mLocationClient;
	private LocationRequest mLocationRequest;
    boolean mUpdatesRequested = false;


    public static final String SHARED_PREFERENCES = "edu.cmu.andrele.contextualcontacts.SHARED_PREFERENCES";

    // Key for storing the "updates requested" flag in shared preferences
    public static final String KEY_UPDATES_REQUESTED = "edu.cmu.andrele.contextualcontacts.KEY_UPDATES_REQUESTED";

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*
     * Constants for location update parameters
     */
    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int UPDATE_INTERVAL_IN_SECONDS = 300;
    public static final int FAST_CEILING_IN_SECONDS = 1;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;
    
	
	// Camera properties
	public static final int MEDIA_TYPE_IMAGE = 1;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private Uri fileUri;
	public ImageView imageButtonAvatar;
	
	// Other UI elements
	public Button saveButton;
	public Button clearButton;
	public EditText fullName;
	public EditText phoneNumber;
	public EditText emailAddress;
	
	private boolean isGpsEnabled;
	private boolean isLocationNetworkEnabled;

	// List view
	private ContactsDataSource datasource;
	
    // Handle to SharedPreferences for this app
    SharedPreferences mPrefs;

    // Handle to a SharedPreferences editor
    SharedPreferences.Editor mEditor;
	
	public static class ErrorDialogFragment extends DialogFragment {
		private Dialog mDialog;
		
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}
		
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
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
		
		// Setup list view
		datasource = new ContactsDataSource(this);
		datasource.open();
		List<CContact> values = datasource.getAllContacts();
		
		// Use SimpleCursorAdapter to show the elements in ListView
//		ArrayAdapter<CContact> adapter = new ArrayAdapter<CContact>(this, android.R.layout.simple_list_item_1, values);
		ContactArrayAdapter adapter = new ContactArrayAdapter(this, values);
		setListAdapter(adapter);
		
		
		// Setup location services
		locationTextView = (TextView)findViewById(R.id.listItemName);
//		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//		Criteria criteria = new Criteria();
//		provider = locationManager.getBestProvider(criteria, false);
//		Location location = locationManager.getLastKnownLocation(provider);
		
		mUpdatesRequested = true;
		mLocationRequest = LocationRequest.create();
		mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
		mLocationClient = new LocationClient(this, this, this);
		mLocationClient.connect();
		
		// Open shared preferences
		mPrefs = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
		mEditor = mPrefs.edit();
		
		// Handles to buttons
		imageButtonAvatar = (ImageView)findViewById(R.id.imageButtonAvatar);
		imageButtonAvatar.setOnClickListener(this);
		saveButton = (Button)findViewById(R.id.btnSave);
		saveButton.setOnClickListener(this);
		clearButton = (Button)findViewById(R.id.btnClear);
		clearButton.setOnClickListener(this);
		fullName = (EditText)findViewById(R.id.editTextName);
		phoneNumber = (EditText)findViewById(R.id.editTextPhone);
		emailAddress = (EditText)findViewById(R.id.editTextEmail);
		
		
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
	public void onClick(View view) {
		ContactArrayAdapter adapter = (ContactArrayAdapter) getListAdapter();
		CContact contact = null;
		switch (view.getId()) {
		case R.id.btnSave:
			if (fullName.getText().toString().matches("")) {
				Toast.makeText(this, "Please enter a name before saving", Toast.LENGTH_LONG).show();
			} else {
				ArrayList<String> venueStrings = new ArrayList<String>();
				venueStrings.add(locationTextView.getText().toString());
				Uri newUri = null;
				if (fileUri == null || fileUri.toString().isEmpty()) {
					newUri = Uri.parse("");
				} else {
					newUri = fileUri;
				}
				contact = datasource.createContact(fullName.getText().toString(), phoneNumber.getText().toString(), emailAddress.getText().toString(), newUri, 10.0f, 10.0f, venueStrings);
				if (contact != null) {
					addToContacts(contact);
					adapter.add(contact);
					adapter.notifyDataSetChanged();
					Toast.makeText(this, contact.fullName + " was saved in your contacts", Toast.LENGTH_LONG).show();
				}
			}
			break;
		case R.id.btnClear:
			clearFields();
			break;
		case R.id.imageButtonAvatar:
			dispatchImageCaptureIntent();
			break;
		}
	}
	
	public void clearFields() {
		fullName.setText("");		
		phoneNumber.setText("");
		emailAddress.setText("");
		fullName.requestFocus();
	}
	
	public static float getOrientation(Context context, Uri uri) {
	        if (uri.getScheme().equals("content")) {
	        String[] projection = { Images.ImageColumns.ORIENTATION };
	        Cursor c = context.getContentResolver().query(
	                uri, projection, null, null, null);
	        if (c.moveToFirst()) {
	            return c.getInt(0);
	        }
	    } else if (uri.getScheme().equals("file")) {
	        try {
	            ExifInterface exif = new ExifInterface(uri.getPath());
	            int rotation = (int)exifOrientationToDegrees(
	                    exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
	                            ExifInterface.ORIENTATION_NORMAL));
	            return rotation;
	        } catch (IOException e) {
	            Log.e(APPTAG, "Error checking exif", e);
	        }
	    }
	        return 0f;
	    }
	
	    private static float exifOrientationToDegrees(int exifOrientation) {
	    if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
	        return 90;
	    } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
	        return 180;
	    } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
	        return 270;
	    }
	    return 0;
    }
	
	    
	public Bitmap photoWithOrientation(Uri photoUri) throws FileNotFoundException {
		ContentResolver cr = getContentResolver();
		InputStream in = cr.openInputStream(photoUri);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize=4;
		options.inScaled=true;
		Bitmap thumb = BitmapFactory.decodeStream(in, null, options);
		Matrix matrix = new Matrix();
		float rotation = getOrientation(this, photoUri);
		if (rotation != 0f) {
			matrix.preRotate(rotation);
			Bitmap rotatedBitmap = Bitmap.createBitmap(thumb, 0, 0, thumb.getWidth(), thumb.getHeight(), matrix, true);
			return rotatedBitmap;
		} else {
			return thumb;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
				if (resultCode != RESULT_CANCELED) {
					  // Image captured and saved to fileUri specified in the Intent
					Toast.makeText(this, "Image saved", Toast.LENGTH_LONG).show();
					
					try {
						imageButtonAvatar.setImageBitmap(photoWithOrientation(fileUri));
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
				break;
			case CONNECTION_FAILURE_RESOLUTION_REQUEST:
				switch (resultCode) {
					case Activity.RESULT_OK:
						// Try request again
						Log.d("Google Play", "Connected. Resolved problem");
						break;
					default:
						Log.d("Google Play", "Disconnected. No resolution.");
						break;
				}
				break;
		}
	}
	
	// Check to see if GooglePlay services is available
	private boolean servicesConnected() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (ConnectionResult.SUCCESS == resultCode) {
			Log.d("Location Updates", "Google Play services is available.");
			return true;
		} else {
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
			
			if (errorDialog != null) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(errorDialog);
				errorFragment.show(getFragmentManager(), "Location Updates");
			}
			return false;
		}
	}
	
	@Override
	protected void onStart(){		
		super.onStart();
        mLocationClient.connect();

	}
	
	@Override
	protected void onResume(){
		datasource.open();
		super.onResume();
//		locationManager.requestLocationUpdates(provider, 400, 1, this);
        // If the app already has a setting for getting location updates, get it
        if (mPrefs.contains(KEY_UPDATES_REQUESTED)) {
            mUpdatesRequested = mPrefs.getBoolean(KEY_UPDATES_REQUESTED, false);

        // Otherwise, turn off location updates until requested
        } else {
            mEditor.putBoolean(KEY_UPDATES_REQUESTED, false);
            mEditor.commit();
        }

		Log.d("Andre", "onResume called.");
		locationTextView = (TextView)findViewById(R.id.locationText);
	}
	
	@Override
	protected void onPause(){
		datasource.close();
//		mUpdatesRequested = false;
        mEditor.putBoolean(KEY_UPDATES_REQUESTED, mUpdatesRequested);
        mEditor.commit();
		super.onPause();
//		locationManager.removeUpdates(this);
	}
	
	@Override
	protected void onStop(){
        // If the client is connected
        if (mLocationClient.isConnected()) {
            stopPeriodicUpdates();
        }

        // After disconnect() is called, the client is considered "dead".
        mLocationClient.disconnect();
		super.onStop();
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
	}
	
	@Override
	public void onLocationChanged(Location location) {
		currentLocation = location;
		Log.d("Andre", "Location Changed - Lat: " + location.getLatitude() + " Long: " + location.getLongitude());
		url = "https://api.foursquare.com/v2/venues/search?ll="+location.getLatitude()+","+location.getLongitude()+"&client_id=TRFZGGZKZOOA0GWNFOCQTUHDVDJZCU1JQZSLKHYF3OUUKSE2&client_secret=OBETZ5VJ3QSYAJ5YEQAJU0JR54BX1V2XOIF55VQS3MGT5ARP&v=20121116";
		if (isNetworkAvailable()) {
			new FoursquareChecker().execute(url);
		}
	}
	
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderEnabled(String provider) {
	    Toast.makeText(this, "Enabled new provider " + provider,
	            Toast.LENGTH_SHORT).show();
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	    Toast.makeText(this, "Enabled new provider " + provider,
	            Toast.LENGTH_SHORT).show();
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
					Log.d("MINE", "First result: " + result.response.venues.get(0).name);
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
	
	private void addToContacts(CContact contact) {
		String DisplayName = contact.fullName;
		 String MobileNumber = contact.phoneNumber;
		 String HomeNumber = "";
		 String WorkNumber = "";
		 String emailID = contact.emailAddress;
		 String company = "";
		 String jobTitle = "";
		 SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd,yyyy");
		 String notes = "";
		 
		 try {
			notes = "Met at " + contact.venues.get(0) + " on " + dateFormat.format(contact.date);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		 ArrayList < ContentProviderOperation > ops = new ArrayList < ContentProviderOperation > ();

		 ops.add(ContentProviderOperation.newInsert(
		 ContactsContract.RawContacts.CONTENT_URI)
		     .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
		     .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
		     .build());

		 //------------------------------------------------------ Names
		 if (DisplayName != null) {
		     ops.add(ContentProviderOperation.newInsert(
		     ContactsContract.Data.CONTENT_URI)
		         .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
		         .withValue(ContactsContract.Data.MIMETYPE,
		     ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
		         .withValue(
		     ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
		     DisplayName).build());
		 }
		 
		 //------------------------------------------------------ Notes
		 if (notes != null) {
			 ops.add(ContentProviderOperation.newInsert(
			 ContactsContract.Data.CONTENT_URI)
			 	.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
			 	.withValue(ContactsContract.Data.MIMETYPE,
			 ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
				.withValue(
			ContactsContract.CommonDataKinds.Note.NOTE, notes).build());
		 }

		 //------------------------------------------------------ Mobile Number                     
		 if (MobileNumber != null) {
		     ops.add(ContentProviderOperation.
		     newInsert(ContactsContract.Data.CONTENT_URI)
		         .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
		         .withValue(ContactsContract.Data.MIMETYPE,
		     ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
		         .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, MobileNumber)
		         .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
		     ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
		         .build());
		 }

		 //------------------------------------------------------ Email
		 if (emailID != null) {
		     ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
		         .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
		         .withValue(ContactsContract.Data.MIMETYPE,
		     ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
		         .withValue(ContactsContract.CommonDataKinds.Email.DATA, emailID)
		         .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
		         .build());
		 }

		 //------------------------------------------------------ Organization
		 if (!company.equals("") && !jobTitle.equals("")) {
		     ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
		         .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
		         .withValue(ContactsContract.Data.MIMETYPE,
		     ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
		         .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, company)
		         .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
		         .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, jobTitle)
		         .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
		         .build());
		 }

		 // Asking the Contact provider to create a new contact                 
		 try {
		     getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
		 } catch (Exception e) {
		     e.printStackTrace();
		     Toast.makeText(this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
		 }
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */

            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
            }
        } else {

            // If no resolution is available, display a dialog to the user with the error.
            showErrorDialog(connectionResult.getErrorCode());
        }

	}

	@Override
	public void onConnected(Bundle bundle) {
		Log.d(APPTAG, "GooglePlay Connected");
        startPeriodicUpdates();
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
	
    /**
     * In response to a request to start updates, send a request
     * to Location Services
     */
    private void startPeriodicUpdates() {
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    /**
     * In response to a request to stop updates, send a request to
     * Location Services
     */
    private void stopPeriodicUpdates() {
    	Log.d(APPTAG, "Stopping periodic updates");
        mLocationClient.removeLocationUpdates(this);
    }
	
    private void showErrorDialog(int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
            errorCode,
            this,
            CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(getFragmentManager(), APPTAG);
        }
    }

}
