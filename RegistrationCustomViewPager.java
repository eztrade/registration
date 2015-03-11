package com.goldfish.registration;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class RegistrationCustomViewPager extends ViewPager {
	private boolean swipe_enabled;
	
    public boolean isSwipe_enabled() {
		return swipe_enabled;
	}

	public void setSwipe_enabled(boolean swipe_enabled) {
		this.swipe_enabled = swipe_enabled;
	}
	
    public RegistrationCustomViewPager(Context context) {
        super(context);
    }

    public RegistrationCustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
    	if(isSwipe_enabled()){
    		return super.onInterceptTouchEvent(event);
    	}
    	return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if(isSwipe_enabled()){
    		return super.onTouchEvent(event);
    	}
    	 return false;       
    }
    
}