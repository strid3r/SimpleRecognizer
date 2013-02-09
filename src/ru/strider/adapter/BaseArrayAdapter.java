/*
 * Copyright (C) 2012-2013 strider
 * 
 * Adapter
 * ArrayAdapter BaseArrayAdapter Class
 * By © strider 2012-2013.
 */

package ru.strider.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.Collection;
import java.util.List;

/**
 * ArrayAdapter BaseArrayAdapter Class.
 * 
 * @author strider
 */
public class BaseArrayAdapter<T> extends ArrayAdapter<T> {
	
	public BaseArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}
	
	public BaseArrayAdapter(Context context, int textViewResourceId, T[] arrayObject) {
		super(context, textViewResourceId, arrayObject);
	}
	
	public BaseArrayAdapter(Context context, int textViewResourceId, List<T> listObject) {
		super(context, textViewResourceId, listObject);
	}
	
	public BaseArrayAdapter(Context context, int resource, int textViewResourceId) {
		super(context, resource, textViewResourceId);
	}
	
	public BaseArrayAdapter(Context context, int resource, int textViewResourceId, T[] arrayObject) {
		super(context, resource, textViewResourceId, arrayObject);
	}
	
	public BaseArrayAdapter(Context context, int resource, int textViewResourceId, List<T> listObject) {
		super(context, resource, textViewResourceId, listObject);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void addData(Collection<? extends T> listObject) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			this.addAll(listObject);
		} else {
			for (T object : listObject) {
				this.add(object);
			}
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		
		view.setSelected(true);
		
		return view;
	}
	
}
