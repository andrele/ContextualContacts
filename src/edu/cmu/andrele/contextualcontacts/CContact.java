package edu.cmu.andrele.contextualcontacts;

import java.util.ArrayList;
import java.util.Date;

import android.net.Uri;
import android.util.Log;

public class CContact {
	public long id;
	public String fullName;
	public String phoneNumber;
	public String emailAddress;
	public Uri imageUri;
	public float latitude, longitude;
	public ArrayList<String> venues;
	public Date date;
	
	// Contact constructors
	public CContact() {
		super();
		this.venues = new ArrayList<String>();
		this.date = new Date();
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
		this.venues = new ArrayList<String>();
		if (venues != null && !venues.isEmpty()) {
			for (String venue : venues) {
				this.venues.add(venue);
			}
		}
		this.date = new Date();
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
		if (string != null) {
			// Parse venues as CSV into ArrayList
			String[] parts = string.split(", ");
			this.venues.clear();
			for (int i = 0; i<parts.length;i++) {
				venues.add(parts[i]);
			}
		}
	}
	
	@Override
	public String toString() {
		return fullName + " Phone: " + phoneNumber + " Email: " + emailAddress;
	}
	
	
}
