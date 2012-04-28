/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * ExpandableList Adapter Two Level Class
 * By © strider 2012.
 */

package ru.strider.simplerecognizer.adapter;

import android.content.Context;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.List;

import ru.strider.simplerecognizer.R;
import ru.strider.widget.CheckedLinearLayout;
import ru.strider.widget.MainCheckedTextView;
import ru.strider.widget.RealImageView;

/**
 * ExpandableList Adapter Two Level Class.
 * 
 * @author strider
 */
public class TwoLevelExpandableListAdapter extends BaseExpandableListAdapter implements OnCreateContextMenuListener,
		ExpandableListView.OnChildClickListener, ExpandableListView.OnGroupCollapseListener, ExpandableListView.OnGroupExpandListener {
	
	private static final float PADDING_NON_LEFT_DP = 8.0f;
	
	private Context mContext = null;
	
	private LayoutInflater mInflater = null;
	
	private float mDensityScale = 0;
	
	private int mPaddingNonLeft = 0;
	
	private List<String> mListGroup = null;
	private List<List<String>> mListChild = null;
	
	public TwoLevelExpandableListAdapter(Context context, List<String> listGroup, List<List<String>> listChild) {
		mContext = context;
		
		mInflater = LayoutInflater.from(mContext);
		
		mDensityScale = mContext.getResources().getDisplayMetrics().density;
		
		mPaddingNonLeft = (int) (PADDING_NON_LEFT_DP * mDensityScale + 0.5f);
		
		mListGroup = listGroup;
		mListChild = listChild;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		//
	}
	
	public MainCheckedTextView getGenericView(ViewGroup root, int paddingLeft) {
		MainCheckedTextView textView = (MainCheckedTextView) mInflater.inflate(R.layout.list_item_activated_single_choice, root, false);
		
		textView.setPadding(paddingLeft, mPaddingNonLeft, mPaddingNonLeft, mPaddingNonLeft);
		
		return textView;
	}
	
	@Override
	public Object getGroup(int groupPosition) {
		return mListGroup.get(groupPosition);
	}
	
	@Override
	public int getGroupCount() {
		return mListGroup.size();
	}
	
	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}
	
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		CheckedLinearLayout linearLayout = (CheckedLinearLayout) mInflater.inflate(R.layout.expandablelist_item_activated_single_choice, parent, false);
		
		RealImageView imageView = (RealImageView) linearLayout.findViewById(R.id.imageViewArrow);
		
		if ((mListChild.get(groupPosition) == null) || (mListChild.get(groupPosition).isEmpty())) {
			imageView.setImageResource(R.color.transparent);
		} else if (isExpanded) {
			imageView.setImageResource(R.drawable.indicator_expandablelist_maximized);
		}
		
		MainCheckedTextView textView = (MainCheckedTextView) linearLayout.findViewById(R.id.checkedTextViewItem);
		
		textView.setPadding(0, mPaddingNonLeft, mPaddingNonLeft, mPaddingNonLeft);
		textView.setText(getGroup(groupPosition).toString());
		
		return linearLayout;
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mListChild.get(groupPosition).get(childPosition);
	}
	
	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return (groupPosition + childPosition + 1);
	}
	
	@Override
	public int getChildrenCount(int groupPosition) {
		return mListChild.get(groupPosition).size();
	}
	
	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		TextView textView = getGenericView(parent, (int) (60.0f * mDensityScale + 0.5f));
		textView.setText(getChild(groupPosition, childPosition).toString());
		
		return textView;
	}
	
	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	@Override
	public boolean hasStableIds() {
		return true;
	}
	
	@Override
	public void onGroupExpand(int groupPosition) {
		//
    }
	
	@Override
	public void onGroupCollapse(int groupPosition) {
		//
	}
	
	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		//
		return false;
	}
	
}
