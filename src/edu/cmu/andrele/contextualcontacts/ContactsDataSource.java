package edu.cmu.andrele.contextualcontacts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Data source to manage the history tab list
 * @author andrele
 *
 */
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
	

	/**
	 * Adds a new Contact to the database and returns newly created Contact
	 * @param fullName Name of contact
	 * @param phoneNumber Phone number of contact
	 * @param emailAddress Email address of contact
	 * @param imageUri Image Uri of contact
	 * @param latitude Latitude where it was saved
	 * @param longitude Longitude where it was saved
	 * @param venues List of strings that represent nearby locations at the time
	 * @return
	 */
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
	
	/**
	 * Remove a contact from the database
	 * @param contact Contact to be removed
	 */
	public void deleteContact(CContact contact) {
		long id = contact.getId();
		System.out.println("Contact deleted with id: " + id);
		database.delete(MySQLiteHelper.TABLE_CONTACTS, MySQLiteHelper.COLUMN_ID + " = " + id, null);
	}
	
	/**
	 * Get a list of all contacts in the database
	 * @return List of CContact objects
	 */
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
	
	/**
	 * Gets a contact at a specified position
	 * @param position Index of contact
	 * @return CContact object at specified location
	 */
	public CContact getContact(int position) {
		List<CContact> allContacts = getAllContacts();
		int newPosition = allContacts.size() - 1 - position;
		CContact contact = allContacts.get(newPosition);
		if (contact != null) {
			return contact;
		}
		return null;
	}
	
	/**
	 * Builds a contact at the current cursor location
	 * @param cursor Current cursor position
	 * @return CContact object at current Cursor location
	 */
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
