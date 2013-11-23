package edu.cmu.andrele.contextualcontacts;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ContactsDataSource {
	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_CONTACT };
	
	public ContactsDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}
	
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		dbHelper.close();
	}
	
	public CContact createContact(String fullName) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_CONTACT, fullName);
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
		return contacts;
	}
	
	private CContact cursorToContact(Cursor cursor) {
		CContact contact = new CContact();
		contact.setId(cursor.getLong(0));
		contact.setContact(cursor.getString(1));
		return contact;
	}
}
