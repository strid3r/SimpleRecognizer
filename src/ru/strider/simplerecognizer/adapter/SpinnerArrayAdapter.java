/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * Array Adapter Spinner Class
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
import ru.strider.widget.MainTextView;

/**
 * Array Adapter Spinner Class.
 * 
 * @author strider
 */
public class SpinnerArrayAdapter extends ArrayAdapter<String> {
	
	private Context mContext = null;
	
	private LayoutInflater mInflater = null;
	
	public SpinnerArrayAdapter(Context context, List<String> listObject) {
		super(context, R.layout.spinner_item_activated, listObject);
		
		mContext = context;
		
		mInflater = LayoutInflater.from(mContext);
		
		this.setDropDownViewResource(R.layout.list_item_activated_single_choice);
	}
	
	public MainTextView getGenericView(ViewGroup root) {
		MainTextView textView = (MainTextView) mInflater.inflate(R.layout.spinner_item_activated, root, false);
		
		return textView;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MainTextView textView = getGenericView(parent);
		textView.setText(getItem(position));
		
		return textView;
	}
	
}
