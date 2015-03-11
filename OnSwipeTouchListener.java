package com.goldfish.registration;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * Detects left and right swipes across a view.
 */
public class OnSwipeTouchListener implements OnTouchListener {

    private final GestureDetector gestureDetector;
    private Context cxt;
    private int curPosition;
    private static int counter = 0;
    private IRegistrationFragments curFragment;

    public OnSwipeTouchListener(Context context,int currentPosition,IRegistrationFragments currentFragment) {
    	cxt = context;
    	curPosition = currentPosition;
    	curFragment = currentFragment;
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public void onSwipeLeft() {
    	if(curPosition>0)
    		((RegistrationMainActivity)cxt).setFragment(curPosition-1);
    	((RegistrationMainActivity)cxt).setIndicatorProp(((RegistrationMainActivity)cxt).getCounter());
    }

    public void onSwipeRight() {
    	if(curPosition <5 && curFragment.validateAllFields()){
    		if(((RegistrationMainActivity)cxt).getCounter() <= curPosition){
    			counter = ((RegistrationMainActivity)cxt).getCounter();
    			((RegistrationMainActivity)cxt).setIndicatorProp(curPosition+1);
    			counter ++;
    			((RegistrationMainActivity)cxt).setCounter(counter);
    		}
    		((RegistrationMainActivity)cxt).setFragment(curPosition+1);
    	}
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_DISTANCE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        	float distanceX = 0;
        	float distanceY = 0;
        	if(null != e1 && null != e2){
        		distanceX = e2.getX() - e1.getX();
                distanceY = e2.getY() - e1.getY();
        	}
            
            if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (distanceX > 0)
                    onSwipeLeft();
                else
                    onSwipeRight();
                return true;
            }
            return false;
        }

	@Override
		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}
    }
    
    private class CustomGestureDetector extends GestureDetector{

    	OnGestureListener listen;
    	VelocityTracker velocity = VelocityTracker.obtain();
    	
		public CustomGestureDetector(Context context, OnGestureListener listener) {
			super(context, listener);
			listen = listener;
			
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent ev) {
			velocity.addMovement(ev);
			listen.onFling(ev, ev, velocity.getXVelocity(),velocity.getYVelocity());
			return super.onTouchEvent(ev);
		}
    	
    }

}