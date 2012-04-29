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
	
	private static final float PADDING_DP = 14.0f;
	
	private Context mContext = null;
	
	private LayoutInflater mInflater = null;
	
	private float mDensityScale = 0;
	
	private int mPadding = 0;
	
	public ListArrayAdapter(Context context, List<String> listObject) {
		super(context, R.layout.list_item_activated_single_choice, listObject);
		
		mContext = context;
		
		mInflater = LayoutInflater.from(mContext);
		
		mDensityScale = mContext.getResources().getDisplayMetrics().density;
		
		mPadding = (int) (PADDING_DP * mDensityScale + 0.5f);
	}
	
	public MainCheckedTextView getGenericView(ViewGroup root) {
		MainCheckedTextView textView = (MainCheckedTextView) mInflater.inflate(R.layout.list_item_activated_single_choice, root, false);
		
		textView.setPadding(mPadding, mPadding, mPadding, mPadding);
		
		return textView;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MainCheckedTextView textView = getGenericView(parent);
		textView.setText(getItem(position));
		
		return textView;
	}
	
}
