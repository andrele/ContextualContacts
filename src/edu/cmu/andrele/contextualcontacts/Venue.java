package edu.cmu.andrele.contextualcontacts;

import java.util.ArrayList;

public class Venue {
	public String id;
	public String name;
	public Location location;
	public ArrayList<Category> categories;
	
	public String toString(){
		return name;
	}
	
}
