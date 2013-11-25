package edu.cmu.andrele.contextualcontacts;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactDetailView extends Activity {
	private ImageView imageView;
	private TextView nameText, phoneText, emailText, dateText, locationText, cityText;
	private Button sendText, sendEmail;
	
	private CContact currentContact;
	
	private ContactsDataSource dataSource;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_detail_view);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Open the datasource
		dataSource = new ContactsDataSource(this);
		dataSource.open();
		
		// Get all views
		imageView = (ImageView)findViewById(R.id.detailImage);
		nameText = (TextView)findViewById(R.id.detailName);
		emailText = (TextView)findViewById(R.id.detailEmail);
		phoneText = (TextView)findViewById(R.id.detailPhone);
		locationText = (TextView)findViewById(R.id.detailLocation);
		cityText = (TextView)findViewById(R.id.detailCity);
		sendText = (Button)findViewById(R.id.btnSendText);
		sendEmail = (Button)findViewById(R.id.btnSendEmail);
		dateText = (TextView)findViewById(R.id.detailDate);
		currentContact = new CContact();
		
		
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		
		Log.d(MainActivity.APPTAG, "Intent: " + intent + " Action: " + action + " Type: " + type);
		
		if (MainActivity.INTENT_ACTION_SHOW_DETAIL.equals(action)) {
			updateContactDetails(intent.getIntExtra("position", -1));
		}
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
//            NavUtils.navigateUpFromSameTask(this);
        	this.finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
	@Override
	protected void onResume() {
		dataSource.open();
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		dataSource.close();
		super.onPause();
	}
	
	
	private void updateContactDetails(int position) {
		if (position == -1) {
			Log.d(MainActivity.APPTAG, "Error fetching contact details. Positon -1");
		} else {
			CContact contact = dataSource.getContact(position);
			if (contact != null)
				currentContact = contact;
			if (!contact.imageUri.toString().isEmpty() || contact.imageUri != null) {
				try {
					imageView.setImageBitmap(photoWithOrientation(contact.imageUri));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (!contact.fullName.isEmpty() && contact.fullName != null) {
				nameText.setText(contact.fullName);
			} else {
				nameText.setText("");
			}
			
			if (!contact.emailAddress.isEmpty() && contact.emailAddress != null) {
				emailText.setText(contact.emailAddress);
			} else {
				emailText.setText("");
			}
			
			if (!contact.phoneNumber.isEmpty() && contact.phoneNumber != null) {
				phoneText.setText(contact.phoneNumber);
			} else {
				phoneText.setText("");
			}
			
			if (contact.date != null) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
				dateText.setText(dateFormat.format(contact.date));
			}
			
			if (!contact.venues.isEmpty() && contact.venues != null) {
				locationText.setText(contact.venues.get(0));
			} else {
				locationText.setText("Unknown Location");
			}
		}
	}
	
	public static float getOrientation(Context context, Uri uri) {
	    if (uri.getScheme().equals("content")) {
	        String[] projection = { Images.ImageColumns.ORIENTATION };
	        Cursor c = context.getContentResolver().query(
	                uri, projection, null, null, null);
	        if (c.moveToFirst()) {
	            return c.getInt(0);
	        }
	    } else if (uri.getScheme().equals("file")) {
	        try {
	            ExifInterface exif = new ExifInterface(uri.getPath());
	            int rotation = (int)exifOrientationToDegrees(
	                    exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
	                            ExifInterface.ORIENTATION_NORMAL));
	            return rotation;
	        } catch (IOException e) {
	            Log.e(MainActivity.APPTAG, "Error checking exif", e);
	        }
	    }
	        return 0f;
	    }
	
	    private static float exifOrientationToDegrees(int exifOrientation) {
	    if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
	        return 90;
	    } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
	        return 180;
	    } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
	        return 270;
	    }
	    return 0;
    }

    
	public Bitmap photoWithOrientation(Uri photoUri) throws FileNotFoundException {
		ContentResolver cr = getContentResolver();
		InputStream in = cr.openInputStream(photoUri);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize=4;
		options.inScaled=true;
		Bitmap thumb = BitmapFactory.decodeStream(in, null, options);
		Matrix matrix = new Matrix();
		float rotation = getOrientation(this, photoUri);
		if (rotation != 0f) {
			matrix.preRotate(rotation);
			Bitmap rotatedBitmap = Bitmap.createBitmap(thumb, 0, 0, thumb.getWidth(), thumb.getHeight(), matrix, true);
			return rotatedBitmap;
		} else {
			return thumb;
		}
	}
}
