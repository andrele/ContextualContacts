package edu.cmu.andrele.contextualcontacts;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * Activity that shows detailed information on the contact selected from the History list
 * @author andrele
 *
 */
public class ContactDetailView extends Activity implements OnClickListener {
	private ImageView imageView;
	private TextView nameText, phoneText, emailText, dateText, locationText;
	private Button sendText, sendEmail;
	
	private CContact currentContact;
	
	private ContactsDataSource dataSource;
	
	@Override
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
		dateText = (TextView)findViewById(R.id.detailDate);
		sendText = (Button)findViewById(R.id.btnSendText);
		sendText.setOnClickListener(this);
		sendEmail = (Button)findViewById(R.id.btnSendEmail);
		sendEmail.setOnClickListener(this);
		
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
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btnSendEmail:
			sendGreetingEmail(currentContact);
			break;
		case R.id.btnSendText:
			sendGreetingText(currentContact);
			break;
		}
	}
	
	/**
	 * Build a greeting email intent and start activity. Based on the available information in the CContact object, 
	 * it will attempt to phrase the greeting appropriately
	 * @param contact Contact to send a email to
	 */
	private void sendGreetingEmail(CContact contact) {
		String toField = contact.emailAddress;
		String subjectField = "";
		String bodyField = "";
		if (!contact.venues.get(0).toString().contentEquals("Unknown Location")) {
			subjectField = "Great meeting you at " + contact.venues.get(0);
			bodyField = "Hello " + contact.fullName + ",\nIt was great meeting you at " + contact.venues.get(0) + ". Please keep in touch!\n\n";
		} else {
			subjectField = "Great to meet you, " + contact.fullName;
			SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM dd", Locale.US);
			bodyField = "Hello " + contact.fullName + ",\nIt was great meeting you on " + dateFormatter.format(contact.date) + ". Please keep in touch!\n\n";
		}
		
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("message/rfc822");
		
		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {toField});
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subjectField);
		emailIntent.putExtra(Intent.EXTRA_TEXT, bodyField);
		
		startActivity(Intent.createChooser(emailIntent, "Send your greeting with:"));
	}
	
	/**
	 * Build a greeting text message intent and start activity. Based on the available information in the CContact object, 
	 * it will attempt to phrase the greeting appropriately
	 * @param contact Contact to send a text greeting to
	 */
	private void sendGreetingText(CContact contact) {
		Uri smsUri = Uri.parse("sms:" + contact.phoneNumber);
		Intent intent = new Intent(Intent.ACTION_VIEW, smsUri);
		
		String bodyField = "";
		if (!contact.venues.get(0).toString().contentEquals("Unknown Location")) {
			bodyField = "Hello " + contact.fullName + "! It was great meeting you at " + contact.venues.get(0) + ". Please keep in touch!";
		} else {
			SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM dd", Locale.US);
			bodyField = "Hello " + contact.fullName + "! It was great meeting you on " + dateFormatter.format(contact.date) + ". Please keep in touch!";
		}
		
		intent.putExtra("sms_body", bodyField);
		startActivity(intent);
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
	
	/**
	 * Populates the view with Contact information.
	 * @param position Row on the list that was pressed
	 */
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
				nameText.setVisibility(View.GONE);
			}
			
			if (!contact.emailAddress.isEmpty() && contact.emailAddress != null) {
				emailText.setText(contact.emailAddress);
			} else {
				emailText.setText("");
				emailText.setVisibility(View.GONE);
				sendEmail.setVisibility(View.INVISIBLE);
			}
			
			if (!contact.phoneNumber.isEmpty() && contact.phoneNumber != null) {
				phoneText.setText(contact.phoneNumber);
			} else {
				phoneText.setText("");
				phoneText.setVisibility(View.GONE);
				sendText.setVisibility(View.INVISIBLE);
			}
			
			if (contact.date != null) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
				dateText.setText(dateFormat.format(contact.date));
			}
			
			if (!contact.venues.isEmpty() && contact.venues != null) {
				locationText.setText(contact.venues.get(0));
			} else {
				locationText.setText("Unknown Location");
			}
		}
	}
	
	/**
	 * Gets the orientation of a photo given a Uri
	 * @param context Current application context
	 * @param uri Uri of a photo
	 * @return Rotation in degrees
	 */
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

    /**
     * Loads a Bitmap from a Uri and rotates it before returning it
     * @param photoUri Uri of the Bitmap to be returned
     * @return Bitmap of the rotated photo
     * @throws FileNotFoundException
     */
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
