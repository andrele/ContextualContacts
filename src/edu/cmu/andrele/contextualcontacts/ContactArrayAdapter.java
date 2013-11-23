package edu.cmu.andrele.contextualcontacts;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
		
		name.setText(values.get(position).fullName);
		
		String detailsString = values.get(position).venues.get(0) + " on <TestDate>";
		details.setText(detailsString);
		
		if (values.get(position).imageUri != null ) {
	        try {
				Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), values.get(position).imageUri);
				avatar.setImageBitmap(bitmap);
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
