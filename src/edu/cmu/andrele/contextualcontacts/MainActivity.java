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

public class MainActivity extends Activity {
	
	private String url = "https://api.foursquare.com/v2/venues/search?ll=40.4457696,-79.9494519&client_id=TRFZGGZKZOOA0GWNFOCQTUHDVDJZCU1JQZSLKHYF3OUUKSE2&client_secret=OBETZ5VJ3QSYAJ5YEQAJU0JR54BX1V2XOIF55VQS3MGT5ARP&v=20121116";
	private SearchResponse lastResponse;
	private TextView locationTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove the title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		locationTextView = (TextView)findViewById(R.id.textView1);
		
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
		new FoursquareChecker().execute(url);
		Log.d("andre", "OnCreate called");
	}
	
	protected void onResume(Bundle savedInstanceState){

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
			lastResponse = result;
			Log.d("MINE", "Result: " + result);
			locationTextView.setText(result.response.venues.get(0).name);
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
