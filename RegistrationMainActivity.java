package com.goldfish.registration;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;

import com.goldfish.R;
import com.goldfish.analytics.AnalyticsManager;
import com.goldfish.login.LoginActivity;
import com.goldfish.registration.RegistrationDataManager.RegistrationKey;
import com.goldfish.viewpagerindicator.CirclePageIndicatorView;
import com.goldfish.widgets.CustomFontButtonView;

public class RegistrationMainActivity extends FragmentActivity {
	private CirclePageIndicatorView cIndicator;
	private RegistrationCustomViewPager viewPager;
	private CustomFontButtonView cancelBtn;
	private static boolean validateIdentity;
	private static int counter;

	private RegistrationDataManager rDataManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.v4_registration_main);
		counter = 0;
		rDataManager = new RegistrationDataManager();
		cancelBtn = (CustomFontButtonView) findViewById(R.id.cancelORback);
		cancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popFragment();
			}
		});

		viewPager = (RegistrationCustomViewPager) findViewById(R.id.registrationPager);

		final MyPagerAdapter pAdapter = new MyPagerAdapter(
				getSupportFragmentManager());
		viewPager.setAdapter(pAdapter);
		viewPager.setSwipe_enabled(false);
		cIndicator = (CirclePageIndicatorView) findViewById(R.id.reg_indicator);
		cIndicator.setmConditionalFill(true);
		final float density = getResources().getDisplayMetrics().density;
		cIndicator.setRadius(5 * density);
		cIndicator.setPageColor(0xFF888888);
		cIndicator.setFillColor(getResources().getColor(R.color.barclay_blue));
		cIndicator.setStrokeWidth(1f);
		cIndicator.setAsRegistrationScreen(true);
		cIndicator
				.setStrokeColor(getResources().getColor(R.color.barclay_blue));
		cIndicator.setViewPager(viewPager);

		cIndicator.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int pos) {
				viewPager.setCurrentItem(pos);
				Fragment fragment = (Fragment) pAdapter.instantiateItem(
						viewPager, pos);
				((IRegistrationFragments) fragment).onFragmentDisplay();
			}
		});
	}

	@Override
	public void onBackPressed() {
		int currentItem = viewPager.getCurrentItem();
		if (currentItem == 0) {
			AnalyticsManager.getInstance().trackRegIdentityCancel();
			validateIdentity = false;
			finish();
		} else {
			setFragment(currentItem - 1);
		}

	}

	private class MyPagerAdapter extends FragmentPagerAdapter {

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Object instantiateItem(View v, int position) {
			viewPager.setCurrentItem(position);
			return v;
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
		}

		@Override
		public Fragment getItem(int pos) {
			switch (pos) {
			case 0:
				return VerifyIdentityFragment
						.newInstance(RegistrationMainActivity.this);		
			case 1:
				return CreateCredentialsFragment
						.newInstance(RegistrationMainActivity.this);
			case 2:
				return ChooseSecurityOptionsFragment
						.newInstance(RegistrationMainActivity.this);
			case 3:
				return ChooseSecurityQuestionsFragment
						.newInstance(RegistrationMainActivity.this);
			case 4:
				return EnterEmailFragment
						.newInstance(RegistrationMainActivity.this);
			case 5:
				return RegistrationReviewFragment
						.newInstance(RegistrationMainActivity.this);
			default:
				return VerifyIdentityFragment
						.newInstance(RegistrationMainActivity.this);
			}
		}

		@Override
		public int getCount() {
			return 6;
		}
	}

	public void pushFragment(Fragment fragment) {
		String fragmentTag = fragment.getClass().toString();
		getSupportFragmentManager()
				.beginTransaction()
				.setCustomAnimations(R.anim.slide_in_right,
						R.anim.slide_out_left, R.anim.slide_in_left,
						R.anim.slide_out_right).addToBackStack(fragmentTag)
				.replace(R.id.registrationPager, fragment, fragmentTag)
				.commit();
	}

	public void switchToLogin(String extra) {
		Intent exitRegistrationIntent = new Intent(this, LoginActivity.class);
		if(extra.equalsIgnoreCase("INVALID_ATTEMPTS"))
			exitRegistrationIntent.putExtra("INVALID_ATTEMPTS", true);
		else if(extra.equalsIgnoreCase("ALREADY_ENROLLED"))
			exitRegistrationIntent.putExtra("ALREADY_ENROLLED", rDataManager.getFromRegistrationCache(RegistrationKey.USER_NAME));
		else if(extra.equalsIgnoreCase("REGISTRATION_ACC_CIF_LOCKED"))
			exitRegistrationIntent.putExtra("REGISTRATION_ACC_CIF_LOCKED", true);
		else if(extra.equalsIgnoreCase(LoginActivity.REGISTRATION_LOGIN_FAILURE))
			exitRegistrationIntent.putExtra(LoginActivity.REGISTRATION_LOGIN_FAILURE, true);
		exitRegistrationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(exitRegistrationIntent);
		finish();
		
	}

	public void popFragment() {
		getSupportFragmentManager().popBackStack();
	}
	
	public RegistrationDataManager getDataManager(){
		return rDataManager;
	}

	public RegistrationCustomViewPager getViewPager() {
		return viewPager;
	}

	public void setFragment(int position) {
		viewPager.setCurrentItem(position);
	}

	public void setIndicatorProp(int position) {
		cIndicator.setmLastPage(position);
	}

	public void setAllPagesFilled(Boolean isComplete) {
		cIndicator.setAreAllPagesFilled(isComplete);
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		RegistrationMainActivity.counter = counter;
	}

	public boolean isValidateIdentity() {
		return validateIdentity;
	}

	public void setValidateIdentity(boolean validateIdentity) {
		RegistrationMainActivity.validateIdentity = validateIdentity;
	}

}