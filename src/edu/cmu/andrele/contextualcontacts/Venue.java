package edu.cmu.andrele.contextualcontacts;

import java.util.ArrayList;

/**
 * Data model for parsing Venue JSON data from Foursquare API
 * @author andrele
 *
 */
public class Venue {
	public String id;
	public String name;
	public Location location;
	public ArrayList<Category> categories;
	
	public String toString(){
		return name;
	}
	
}
