/*
 * Copyright (C) 2012-2013 strider
 * 
 * Simple Recognizer
 * BaseExpandableListAdapter CourseAdapter Class
 * By © strider 2012-2013.
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
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.strider.simplerecognizer.R;
import ru.strider.simplerecognizer.model.Course;

/**
 * BaseExpandableListAdapter CourseAdapter Class.
 * 
 * @author strider
 */
public class CourseAdapter extends BaseExpandableListAdapter
		implements OnCreateContextMenuListener,
		ExpandableListView.OnGroupExpandListener, ExpandableListView.OnGroupCollapseListener,
		ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener {
	
	private LayoutInflater mInflater = null;
	
	private List<Object> mListGroup = null;
	private List<List<Object>> mListChild = null;
	private List<List<Object>> mListCount = null;
	
	private boolean mIsNotifyOnChange = true;
	
	public CourseAdapter(Context context) {
		doInit(context);
	}
	
	public CourseAdapter(Context context, List<?> listGroup, List<? extends List<?>> listChild,
			List<? extends List<?>> listCount) {
		doInit(context);
		
		addData(listGroup, listChild, listCount);
	}
	
	private void doInit(Context context) {
		mInflater = LayoutInflater.from(context.getApplicationContext());
		
		mListGroup = new ArrayList<Object>();
		mListChild = new ArrayList<List<Object>>();
		mListCount = new ArrayList<List<Object>>();
	}
	
	public void clear() {
		mListGroup.clear();
		mListChild.clear();
		mListCount.clear();
		
		if (mIsNotifyOnChange) {
			this.notifyDataSetChanged();
		}
	}
	
	public void addData(List<?> listGroup, List<? extends List<?>> listChild, List<? extends List<?>> listCount) {
		if ((listGroup.size() != listChild.size()) || (listChild.size() != listCount.size())) {
			throw new IllegalArgumentException("Group, Child and Count data sizes must be equal.");
		}
		
		for (int i = 0; i < listGroup.size(); i++) {
			addChildren(addGroup(listGroup.get(i)), listChild.get(i), listCount.get(i));
		}
	}
	
	public void setNotifyOnChange(boolean isNotifyOnChange) {
		mIsNotifyOnChange = isNotifyOnChange;
	}
	
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		
		mIsNotifyOnChange = true;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		//
	}
	
	@Override
	public Object getGroup(int groupPosition) {
		return mListGroup.get(groupPosition);
	}
	
	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}
	
	public int addGroup(Object group) {
		mListGroup.add(group);
		mListChild.add(new ArrayList<Object>());
		mListCount.add(new ArrayList<Object>());
		
		if (mIsNotifyOnChange) {
			notifyDataSetChanged();
		}
		
		return (getGroupCount() - 1);
	}
	
	@Override
	public int getGroupCount() {
		return mListGroup.size();
	}
	
	public int indexOfGroup(Object object) {
		return mListGroup.indexOf(object);
	}
	
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		View view = convertView;
		
		GroupViewHolder groupViewHolder = null;
		
		if (view == null) {
			view = mInflater.inflate(R.layout.expandablelist_group_single_choice_activated, parent, false);
			
			groupViewHolder = new GroupViewHolder();
			groupViewHolder.mArrow = (ImageView) view.findViewById(R.id.imageViewGroupArrow);
			groupViewHolder.mGroup = (CheckedTextView) view.findViewById(R.id.checkedTextViewGroup);
			
			view.setTag(groupViewHolder);
		} else {
			groupViewHolder = (GroupViewHolder) view.getTag();
		}
		
		// Arrow
		groupViewHolder.mArrow.setImageResource((getChildrenCount(groupPosition) > 0)
				? (isExpanded
						? R.drawable.indicator_expandablelist_maximized
						: R.drawable.indicator_expandablelist_minimized
					)
				: R.color.transparent
			);
		
		// Category
		Object category = getGroup(groupPosition);
		
		groupViewHolder.mGroup.setText(
				(category instanceof CharSequence) ? (CharSequence) category : category.toString()
			);
		
		return view;
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mListChild.get(groupPosition).get(childPosition);
	}
	
	public int getCount(int groupPosition, int childPosition) {
		Object count = mListCount.get(groupPosition).get(childPosition);
		
		if (count instanceof Number) {
			return ((Number) count).intValue();
		} else {
			try {
				return Integer.parseInt(count.toString());
			} catch (NumberFormatException e) {
				return -1;
			}
		}
	}
	
	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return (groupPosition + childPosition + 1);
	}
	
	public void addChildren(int groupPosition, List<?> listChild, List<?> listCount) {
		if (groupPosition < getGroupCount()) {
			mListChild.get(groupPosition).addAll(listChild);
			mListCount.get(groupPosition).addAll(listCount);
			
			if (mIsNotifyOnChange) {
				notifyDataSetChanged();
			}
		}
	}
	
	@Override
	public int getChildrenCount(int groupPosition) {
		return mListChild.get(groupPosition).size();
	}
	
	public int indexOfChild(Object object) {
		return mListChild.indexOf(object);
	}
	
	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
			View convertView, ViewGroup parent) {
		View view = convertView;
		
		ChildViewHolder childViewHolder = null;
		
		if (view == null) {
			view = mInflater.inflate(R.layout.expandablelist_child_single_choice_activated, parent, false);
			
			childViewHolder = new ChildViewHolder();
			childViewHolder.mCount = (TextView) view.findViewById(R.id.textViewCount);
			childViewHolder.mChild = (CheckedTextView) view.findViewById(R.id.checkedTextViewChild);
			
			view.setTag(childViewHolder);
		} else {
			childViewHolder = (ChildViewHolder) view.getTag();
		}
		
		// Item Count
		int count = getCount(groupPosition, childPosition);
		
		childViewHolder.mCount.setText((count > 0) ? ("[" + Integer.toString(count) + "]") : null);
		
		// Course
		Object course = getChild(groupPosition, childPosition);
		
		childViewHolder.mChild.setText(
				(course instanceof Course)
						? ((Course) course).getTitle()
						: ((course instanceof CharSequence)
								? (CharSequence) course
								: course.toString())
			);
		
		return view;
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
	public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition,
			long id) {
		return false;
	}
	
	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
			int childPosition, long id) {
		return false;
	}
	
	/**
	 * Car GroupViewHolder Class.
	 * 
	 * @author strider
	 */
	private static class GroupViewHolder {
		
		private ImageView mArrow = null;
		private CheckedTextView mGroup = null;
		
	}
	
	/**
	 * Car ChildViewHolder Class.
	 * 
	 * @author strider
	 */
	private static class ChildViewHolder {
		
		private TextView mCount = null;
		private CheckedTextView mChild = null;
		
	}
	
}
