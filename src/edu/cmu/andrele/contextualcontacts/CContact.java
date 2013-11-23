package edu.cmu.andrele.contextualcontacts;

import java.util.ArrayList;

import android.net.Uri;

public class CContact {
	public int id;
	public String fullName;
	public String phoneNumber;
	public String emailAddress;
	public Uri imageUri;
	public float latitude, longitude;
	public ArrayList<String> venues;
	
	// Contact constructor
	public CContact (int id, String fullName, String phoneNumber, String emailAddress, Uri imageUri, float latitude, float longitude, ArrayList<String> venues) {
		super();
		this.id = id;
		this.fullName = fullName;
		this.phoneNumber = phoneNumber;
		this.emailAddress = emailAddress;
		this.imageUri = imageUri;
		this.latitude = latitude;
		this.longitude = longitude;
		this.venues = venues;
	}
	
	public int getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return fullName + " Phone: " + phoneNumber + " Email: " + emailAddress;
	}
}
