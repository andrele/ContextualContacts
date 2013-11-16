package edu.cmu.andrele.contextualcontacts;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.*;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove the title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
