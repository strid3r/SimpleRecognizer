/*
 * Copyright (C) 2013 strider
 * 
 * Application
 * Lifecycle ActivityLifecycle Interface
 * By © strider 2013.
 */

package ru.strider.app;

import android.app.Activity;

/**
 * Lifecycle ActivityLifecycle Interface.
 * 
 * @author strider
 */
public interface ActivityLifecycle {
	
	Activity getActivity();
	
	boolean isAlive();
	
	boolean isSaveInstanceState();
	
	boolean isRestoreInstanceState();
	
}
