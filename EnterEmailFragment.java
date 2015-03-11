package com.goldfish.registration;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.goldfish.R;
import com.goldfish.analytics.AnalyticsManager;
import com.goldfish.registration.PaperLessOptionsFragment.PaperLessOptionsListener;
import com.goldfish.registration.RegistrationDataManager.RegistrationKey;
import com.goldfish.utils.AppUtils;
import com.goldfish.utils.StringUtils;
import com.goldfish.widgets.CustomFontButtonView;

public class EnterEmailFragment extends BaseRegistrationFragment implements IRegistrationFragments{
	private EditText et_email, et_confirmEmail;
	private CustomFontButtonView nextButton,backButton;
	private String title, emailMsg;
	private RadioButton rBtnYes, rBtnNo; 
	private static boolean paperlessOptionsSelected;
	private boolean emailvalidation = false;
	private LinearLayout paperlessOptions;
	private TextView cardnickNameTv;
	private boolean isSelected = false;
	private final static String OPT_IN = "OPT_IN";
	private final static String OPT_OUT = "OPT_OUT";
	private boolean hasPrefilled = false;
	
	private RegistrationDataManager rDataManager;
	
	public static EnterEmailFragment newInstance(Context cxt) {
		EnterEmailFragment f = new EnterEmailFragment();
		Bundle args = new Bundle();
		args.putString("title","Email");
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		title = getArguments().getString("title");
		rDataManager = getDataManager();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.registration_email, null);
		
		view.setOnTouchListener(new OnSwipeTouchListener(getMainActivity(),4,this){
		});

		paperlessOptions = (LinearLayout)view.findViewById(R.id.paperless_options);
		cardnickNameTv = (TextView)view.findViewById(R.id.accept_tnc_tv);
		
		et_email = (EditText) view.findViewById(R.id.et_email);
		et_confirmEmail = (EditText) view.findViewById(R.id.et_confirm_email);
		et_confirmEmail.setContentDescription("Re-Enter your primary email address.");
		if(null != rDataManager.getFromRegistrationCache(RegistrationKey.EMAIL) && !hasPrefilled){
			String email = rDataManager.getFromRegistrationCache(RegistrationKey.EMAIL);
			et_email.setText(email);
			et_email.setContentDescription("Your Primary email address on the account is, " + email);
			hasPrefilled = true;
			if(validateEmail(email, et_email, true)){
				emailvalidation = true;
			}
		}else{
			et_email.setContentDescription("Enter your primary email address");
		}
		
		if(null != rDataManager.getFromRegistrationCache(RegistrationKey.ISPAPERLESSELIGIBLE) 
				&& null != rDataManager.getFromRegistrationCache(RegistrationKey.ISUSERENROLLEDINPP)
				&& rDataManager.getFromRegistrationCache(RegistrationKey.ISPAPERLESSELIGIBLE).equalsIgnoreCase("true")
				&& (rDataManager.getFromRegistrationCache(RegistrationKey.ISUSERENROLLEDINPP).equalsIgnoreCase("false"))){
			paperlessOptions.setVisibility(View.VISIBLE);
		} else{
			paperlessOptionsSelected = true;
		}
		
		if(null != rDataManager.getFromRegistrationCache(RegistrationKey.ACCNUM) &&
				null != rDataManager.getFromRegistrationCache(RegistrationKey.PARTNERNAME) ){
			String accNum = rDataManager.getFromRegistrationCache(RegistrationKey.ACCNUM);
			String lastFourDigits = accNum.length() <= 4 ? accNum : accNum.substring(accNum.length() - 4);
			cardnickNameTv.setText(getString(R.string.paperless_yes_info) 
					+ " " + rDataManager.getFromRegistrationCache(RegistrationKey.PARTNERNAME)
					+ " " + "ending in"
					+ " " + lastFourDigits);
			cardnickNameTv.setContentDescription(getString(R.string.paperless_yes_info) 
					+ " " + rDataManager.getFromRegistrationCache(RegistrationKey.PARTNERNAME)
					+ " " + "ending in" + StringUtils.splitStringToChar(lastFourDigits));
		}
		
		emailMsg = getResources().getString(R.string.email_warning);

		et_email.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					if(!et_email.getText().toString().equalsIgnoreCase(et_confirmEmail.getText().toString()) && StringUtils.isValidEmail(et_confirmEmail.getText().toString())) {
						et_confirmEmail.setError("Email should match");
						emailvalidation = false;
					}else if(StringUtils.isValidEmail(et_email.getText().toString())){
						emailvalidation = true;
					}else{
						et_email.setError(emailMsg);
					}
				}
			}
		});

		et_email.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if(getMainActivity().getViewPager().getCurrentItem() == 4){
					if(et_confirmEmail.getText().toString().length() >=6 && StringUtils.isValidEmail(et_confirmEmail.getText().toString()) 
							|| et_email.getError() !=null || nextButton.isEnabled()){
							if(!et_email.getText().toString().equalsIgnoreCase(et_confirmEmail.getText().toString()) ){
								et_email.setError("Email should match");
								et_email.setContentDescription("Email should match");
								emailvalidation = false;
								if(nextButton.isEnabled())
									validateAllFields();
							}else{
								et_email.setError(null);
								emailvalidation = true;
								if(validateAllFields())
									et_confirmEmail.setError(null);
							}
					} 
					
				}
			}
		});
		
		et_confirmEmail.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					if(!et_email.getText().toString().equalsIgnoreCase(et_confirmEmail.getText().toString()) && StringUtils.isValidEmail(et_email.getText().toString())) {
						if(!et_email.getText().toString().equalsIgnoreCase(et_confirmEmail.getText().toString())) {
							et_confirmEmail.setError("Email should match");
							emailvalidation = false;
							if(nextButton.isEnabled())
								validateAllFields();
						}else{
							emailvalidation = true;
							validateAllFields();
						}
					}
				}
			}
		});
		
		et_confirmEmail.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if(getMainActivity().getViewPager().getCurrentItem() == 4){
					if(et_confirmEmail.getError() != null || nextButton.isEnabled() || et_email.getText().toString().length() >=6){
						if(!et_email.getText().toString().equalsIgnoreCase(et_confirmEmail.getText().toString())){
							et_confirmEmail.setError("Email should match");
							et_confirmEmail.setContentDescription("Email should match");
							emailvalidation = false;
							if(nextButton.isEnabled())
								validateAllFields();
							
						}else{
							et_confirmEmail.setError(null);
							emailvalidation = true;
							et_email.setError(null);
							validateAllFields();
						}
					}
				}
			}
		});
		
		rBtnYes = (RadioButton)view. findViewById(R.id.rb_paperless_yes);
		rBtnNo = (RadioButton) view.findViewById(R.id.rb_paperless_no);

		rBtnYes.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				rBtnYes.setChecked(true);
				rBtnNo.setChecked(false);
				gotoChoosePaperLessOptions(isSelected);
			}
		});

		rBtnNo.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				rBtnYes.setChecked(false);
				rBtnNo.setChecked(true);
				paperlessOptionsSelected = true;
				validateAllFields();
			}
		});

		return view;
	}

	public boolean validateEmail(String str, EditText et, boolean showError){
		if(str != null && !str.equals("")) {
			if (et.getText().toString().length() >= 6 && StringUtils.isValidEmail(str)) {
				return true;
			}else {
				if(showError){
					et.setError(emailMsg);
					et.setContentDescription(emailMsg);
				}
				return false;
			}
		}
		return true;
	}

	@Override
	public void onFragmentDisplay() {
		backButton= (CustomFontButtonView)getMainActivity().findViewById(R.id.cancelORback);
		backButton.setText("Back");
		backButton.setContentDescription(getString(R.string.talkback_return_choose_sec_q));
		nextButton = (CustomFontButtonView) getMainActivity().findViewById(R.id.next);
		nextButton.setText("Next");
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(validateAllFields()){
					getMainActivity().setFragment(6);
				}
			}
		});
		TextView headerText = (TextView) getMainActivity().findViewById(R.id.commonHeader);
		headerText.setText(title);
		AnalyticsManager.getInstance().trackRegEmailSetupLoad();
		getMainActivity().getViewPager().setSwipe_enabled(validateAllFields());
		AppUtils.showScreenNameToast(getActivity(), getString(R.string.enter_email_screen));
	}

	public void gotoChoosePaperLessOptions(boolean selected){
		PaperLessOptionsFragment paperLessOptionsFragment = PaperLessOptionsFragment.newInstance(selected, rDataManager.getFromRegistrationCache(RegistrationKey.PARTNERPHONENUM));
		paperLessOptionsFragment.setPaperLessOptionsSelectionListener(new PaperLessOptionsListener() {
			@Override
			public void onPaperLessOptionsSelected(boolean noMail, boolean disclosures) {
				// Add options to RegistrationDataManager
				if(noMail && disclosures){
					paperlessOptionsSelected = true;
					isSelected = true;
				}else {
					rBtnYes.setChecked(false);
					paperlessOptionsSelected = false;
				}
				validateAllFields();
			}
			
		});
		paperLessOptionsFragment.show(getFragmentManager(), "dialog");
	}

	public boolean validateAllFields() {
		boolean rbvalidation = false;
		boolean result = false;
		String paperless = "";
		String email = et_email.getText().toString();
		String confirm_email = et_confirmEmail.getText().toString();
		boolean emailValidated = validateEmail(email, et_email, true);
		boolean emailConfirmValidated = validateEmail(confirm_email, et_confirmEmail, true);
		if(!confirm_email.equalsIgnoreCase(email)){
			emailvalidation = false;
		}
		if(paperlessOptions.getVisibility() == View.VISIBLE){
			if(rBtnYes.isChecked() || rBtnNo.isChecked()){
				rbvalidation = true;
				paperlessOptionsSelected = true;
				if(isSelected && rBtnYes.isChecked()){
					paperless = OPT_IN;
				}else{
					paperless = OPT_OUT;
				}
				rDataManager.setInRegistrationCache(RegistrationKey.ENROLL_IN_PAPERLESS, paperless);
			}
		}else{
			paperlessOptionsSelected = true;
			rbvalidation = true;
		}
		
		if(emailvalidation && rbvalidation && paperlessOptionsSelected && emailValidated && emailConfirmValidated){
			nextButton.setEnabled(true);
			result = true;
			rDataManager.setInRegistrationCache(RegistrationKey.USER_EMAIL, email);
		}else{
			nextButton.setEnabled(false);
		}
		
		getMainActivity().getViewPager().setSwipe_enabled(result);
		if(result){
			if(getMainActivity().getCounter() < 5){
				getMainActivity().setIndicatorProp(5);
				getMainActivity().setCounter(5);
			}
		}
		return result;
	}

}
