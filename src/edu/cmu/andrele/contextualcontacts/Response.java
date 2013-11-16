package edu.cmu.andrele.contextualcontacts;

import java.util.ArrayList;

public class Response {
	
	public ArrayList<Venue> venues;
	
	public String toString(){
		String s = "";
		for(int i = 0; i < venues.size(); i++){
			s += venues.get(i).toString();
		}
		return s;
	}
	
}
