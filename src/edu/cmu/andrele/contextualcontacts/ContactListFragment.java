package edu.cmu.andrele.contextualcontacts;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;

public class ContactListFragment extends ListFragment {
	// ListView properties
	private ContactsDataSource datasource;
	private static final String TAG = "ContactListFragment";
	private ContactArrayAdapter mAdapter;
	
	public ContactListFragment() {
		super();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
		
		datasource = new ContactsDataSource(getActivity());
		datasource.open();
		
		if (mAdapter == null) {
			List<CContact> values = datasource.getAllContacts();
			mAdapter = new ContactArrayAdapter(getActivity(), values);
		}
		getListView().setAdapter(mAdapter);
	}
}
