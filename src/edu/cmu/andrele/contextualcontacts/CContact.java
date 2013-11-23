package edu.cmu.andrele.contextualcontacts;

import java.util.ArrayList;

import android.net.Uri;

public class CContact {
	public long id;
	public String fullName;
	public String phoneNumber;
	public String emailAddress;
	public Uri imageUri;
	public float latitude, longitude;
	public ArrayList<String> venues;
	
	// Contact constructors
	public CContact() {
		super();
	}
	
	public CContact (long id, String fullName, String phoneNumber, String emailAddress, Uri imageUri, float latitude, float longitude, ArrayList<String> venues) {
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
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public void setName(String name) {
		this.fullName = name;
	}
	
	public void setPhone(String phone) {
		this.phoneNumber = phone;
	}
	
	public void setEmail(String email) {
		this.emailAddress = email;
	}
	
	public void setImageURI(String uri) {
		this.imageUri = Uri.parse(uri);
	}
	
	public void setLat(float lat) {
		this.latitude = lat;
	}
	
	public void setLong(float longitude) {
		this.longitude = longitude;
	}
	
	public void setVenuesFromString(String string) {
		// Parse venues as CSV into ArrayList
		String[] parts = string.split(", ");
	}
	
	@Override
	public String toString() {
		return fullName + " Phone: " + phoneNumber + " Email: " + emailAddress;
	}
}
