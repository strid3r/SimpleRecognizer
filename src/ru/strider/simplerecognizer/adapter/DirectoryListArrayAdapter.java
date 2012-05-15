/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * Array Adapter Directory List Class
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
 * Array Adapter Directory List Class.
 * 
 * @author strider
 */
public class DirectoryListArrayAdapter extends ArrayAdapter<String> {
	
	private Context mContext = null;
	
	private LayoutInflater mInflater = null;
	
	public DirectoryListArrayAdapter(Context context, List<String> listObject) {
		super(context, R.layout.list_item_activated, listObject);
		
		mContext = context;
		
		mInflater = LayoutInflater.from(mContext);
	}
	
	public void initData(List<String> listObject) {
		this.clear();
		
		this.addAll(listObject);
		
		this.notifyDataSetChanged();
	}
	
	public MainTextView getGenericView(ViewGroup root) {
		MainTextView textView = (MainTextView) mInflater.inflate(R.layout.list_item_activated, root, false);
		
		return textView;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MainTextView textView = getGenericView(parent);
		textView.setText(getItem(position));
		
		return textView;
	}
	
}
