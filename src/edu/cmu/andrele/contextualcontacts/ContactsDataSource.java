package edu.cmu.andrele.contextualcontacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class ContactsDataSource {
	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_NAME, MySQLiteHelper.COLUMN_PHONE, MySQLiteHelper.COLUMN_EMAIL, MySQLiteHelper.COLUMN_IMAGEURI, MySQLiteHelper.COLUMN_LATITUDE, MySQLiteHelper.COLUMN_LONGITUDE, MySQLiteHelper.COLUMN_VENUES, MySQLiteHelper.COLUMN_DATE };
	
	public ContactsDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}
	
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		dbHelper.close();
	}
	
	public String arrayToCSV(ArrayList<String> array) {
		String csvString = "";
		if (array != null && !array.isEmpty()) {
			for (String item : array) {
				csvString += item + ", ";
			}
		}
		return csvString;
	}
	
	public CContact createContact( String fullName, String phoneNumber, String emailAddress, Uri imageUri, float latitude, float longitude, ArrayList<String> venues) {
		Date now = new Date();
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_NAME, fullName);
		values.put(MySQLiteHelper.COLUMN_PHONE, phoneNumber);
		values.put(MySQLiteHelper.COLUMN_EMAIL, emailAddress);
		values.put(MySQLiteHelper.COLUMN_IMAGEURI, imageUri.toString());
		values.put(MySQLiteHelper.COLUMN_LATITUDE, latitude);
		values.put(MySQLiteHelper.COLUMN_LONGITUDE, longitude);
		values.put(MySQLiteHelper.COLUMN_VENUES, arrayToCSV(venues));
		values.put(MySQLiteHelper.COLUMN_DATE, now.getTime());
				
		long insertId = database.insert(MySQLiteHelper.TABLE_CONTACTS, null, values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_CONTACTS, allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null, null, null, null);
		cursor.moveToFirst();
		CContact newContact = cursorToContact(cursor);
		cursor.close();
		return newContact;
	}
	
	public void deleteContact(CContact contact) {
		long id = contact.getId();
		System.out.println("Contact deleted with id: " + id);
		database.delete(MySQLiteHelper.TABLE_CONTACTS, MySQLiteHelper.COLUMN_ID + " = " + id, null);
	}
	
	public List<CContact> getAllContacts() {
		List<CContact> contacts = new ArrayList<CContact>();
		
		Cursor cursor = database.query(MySQLiteHelper.TABLE_CONTACTS, allColumns, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			CContact contact = cursorToContact(cursor);
			contacts.add(contact);
			cursor.moveToNext();
		}
		
		cursor.close();
		
		// Reverse the order of the list
//		Collections.reverse(contacts);
		
		return contacts;
	}
	
	private CContact cursorToContact(Cursor cursor) {
		CContact contact = new CContact();
		contact.setId(cursor.getLong(0));
		contact.setName(cursor.getString(1));
		contact.setPhone(cursor.getString(2));
		contact.setEmail(cursor.getString(3));
		contact.setImageURI(cursor.getString(4));
		contact.setLat(cursor.getFloat(5));
		contact.setLong(cursor.getFloat(6));
		contact.setVenuesFromString(cursor.getString(7));
		contact.date.setTime(cursor.getLong(8));
		return contact;
	}
}
