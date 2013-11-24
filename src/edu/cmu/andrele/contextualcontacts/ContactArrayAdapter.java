package edu.cmu.andrele.contextualcontacts;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat")
public class ContactArrayAdapter extends ArrayAdapter<CContact>{
	private final Context context;
	private final List<CContact> values;
	
	public ContactArrayAdapter(Context context, List<CContact> values) {
		super(context, R.layout.contact_list_item, values);
		this.context = context;
		this.values = values;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.contact_list_item, parent, false);
		TextView name = (TextView) rowView.findViewById(R.id.listItemName);
		TextView details = (TextView) rowView.findViewById(R.id.listItemDetails);
		ImageView avatar = (ImageView) rowView.findViewById(R.id.listItemAvatar);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd,yyyy");
		
		name.setText(values.get(position).fullName);
		
		String detailsString = "Met at " + values.get(position).venues.get(0) + " on " + dateFormat.format(values.get(position).date);
		details.setText(detailsString);
		
		if (values.get(position).imageUri != null ) {
	        try {
	        	
	    		ContentResolver cr = context.getContentResolver();
	    		InputStream in = cr.openInputStream(values.get(position).imageUri);
	    		BitmapFactory.Options options = new BitmapFactory.Options();
	    		options.inSampleSize=8;
	    		options.inScaled=true;
	    		Bitmap thumb = BitmapFactory.decodeStream(in, null, options);
	    		Matrix matrix = new Matrix();
	    		float rotation = MainActivity.getOrientation(context, values.get(position).imageUri);
	    		if (rotation != 0f) {
	    			matrix.preRotate(rotation);
	    			Bitmap rotatedBitmap = Bitmap.createBitmap(thumb, 0, 0, thumb.getWidth(), thumb.getHeight(), matrix, true);
					avatar.setImageBitmap(rotatedBitmap);
	    		} else {
	    			avatar.setImageBitmap(thumb);
	    		}        	
//				Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), values.get(position).imageUri);
//				avatar.setImageBitmap(bitmap);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
			}
		}
		
		return rowView;
	}
}
