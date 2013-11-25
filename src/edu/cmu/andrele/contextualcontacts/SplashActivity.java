package edu.cmu.andrele.contextualcontacts;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity {
	private long splashDelay = 1500;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_layout);
		
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				finish();
				Intent mainIntent = new Intent().setClass(SplashActivity.this, MainActivity.class);
				startActivity(mainIntent);
				SplashActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
			}
		};
		
		Timer timer = new Timer();
		timer.schedule(task, splashDelay);
	}
}
