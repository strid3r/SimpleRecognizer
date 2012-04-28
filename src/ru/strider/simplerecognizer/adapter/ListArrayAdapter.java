/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * Array Adapter List Class
 * By © strider 2012.
 */

package ru.strider.simplerecognizer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import ru.strider.simplerecognizer.R;
import ru.strider.widget.MainCheckedTextView;

/**
 * Array Adapter List Class.
 * 
 * @author strider
 */
public class ListArrayAdapter extends ArrayAdapter<String> {
	
	private Context mContext = null;
	
	private LayoutInflater mInflater = null;
	
	public ListArrayAdapter(Context context, List<String> listObject) {
		super(context, R.layout.list_item_activated_single_choice, listObject);
		
		mContext = context;
		
		mInflater = LayoutInflater.from(mContext);
	}
	
	public MainCheckedTextView getGenericView(ViewGroup root) {
		MainCheckedTextView textView = (MainCheckedTextView) mInflater.inflate(R.layout.list_item_activated_single_choice, root, false);
		
		return textView;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MainCheckedTextView textView = getGenericView(parent);
		textView.setText(getItem(position));
		
		return textView;
	}
	
}
