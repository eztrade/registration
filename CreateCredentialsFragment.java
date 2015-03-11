package com.goldfish.registration;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goldfish.R;
import com.goldfish.analytics.AnalyticsManager;
import com.goldfish.registration.RegistrationDataManager.RegistrationKey;
import com.goldfish.registration.SaveMMNDialogFragment.MMNEnteredListener;
import com.goldfish.services.ServiceException;
import com.goldfish.services.helpers.ReRegisterCustomerService;
import com.goldfish.services.helpers.RegisterCustomerService;
import com.goldfish.services.model.VerifyIdentityResult;
import com.goldfish.ui.DialogManager;
import com.goldfish.utils.AppUtils;
import com.goldfish.utils.PasswordValidator;
import com.goldfish.utils.StringUtils;
import com.goldfish.widgets.CustomFontButtonView;

public class CreateCredentialsFragment extends BaseRegistrationFragment implements IRegistrationFragments {

	private EditText et_uname, et_pwd, et_cpwd, et_cvv;
	private static String pwd;
	private String title;
	private String invalidUserName,missingField,invalidPwd,password_match,invalidCVV;
	CustomFontButtonView nextButton;
	private Boolean isNextEnabled = false;
	private StringBuilder formAbandonmentValues = null;
	private View cvvView;
	private TextView tv_tooltip;
	private RelativeLayout cvvLayout;
	private boolean retry = true;
	private RegistrationDataManager rDataManager;

	public static CreateCredentialsFragment newInstance(Context cxt) {
		CreateCredentialsFragment f = new CreateCredentialsFragment();
		Bundle args = new Bundle();
		args.putString("title",cxt.getResources().getString(R.string.create_credentials_header));
		f.setArguments(args);
		return f;
	}

	@Override
	public void onResume(){		
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		AnalyticsManager.getInstance().trackRegCredentialsScreenAbandonment(formAbandonmentValues.toString());
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		AnalyticsManager.getInstance().trackRegCredentialsScreenAbandonment(formAbandonmentValues.toString());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		title = getArguments().getString("title");
		rDataManager = getDataManager();
		formAbandonmentValues = new StringBuilder("");
		nextButton = (CustomFontButtonView) getMainActivity().findViewById(R.id.next);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.registration_create_credentials, null);
		setMessages();
		view.setOnTouchListener(new OnSwipeTouchListener(getMainActivity(),1,this){
		});
		et_pwd = (EditText) view.findViewById(R.id.editText_pwd);
		et_uname = (EditText) view.findViewById(R.id.editText_username);
		et_cpwd = (EditText) view.findViewById(R.id.editText_confirm_pwd);
		cvvLayout = (RelativeLayout) view.findViewById(R.id.cvv_linearLyt);
		et_cvv = (EditText) view.findViewById(R.id.editText_cvv);
		tv_tooltip = (TextView) view.findViewById(R.id.cvv_tooltip);

		initializeUNameField(view);
		initializeCPwd(view);
		initializePwd(view);
		initializeCVVField();
		initializeToolTip();

		return view;
	}

	private void initializeToolTip() {
		tv_tooltip.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				showCVVExplanation();
				return false;
			}
		});
	}

	private void initializeCVVField() {
		et_cvv.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void afterTextChanged(Editable text) {
				if (et_cvv.getError() != null)
					et_cvv.setError(null);
				areAllFieldsValid();
			}
		});
	}

	private void initializePwd(View view) {
		
		et_pwd.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void afterTextChanged(Editable text) {
			if(et_pwd.getText().toString().length() >= 8 && new PasswordValidator().validatePwd(et_pwd.getText().toString()) && et_cpwd.getText().toString().length() == 0){
				formAbandonmentValues.append("6:PASS1>");
				if (et_pwd.getError() != null)
					et_pwd.setError(null);
			}else if(et_cpwd.getText().toString().length() >= 8 && new PasswordValidator().validatePwd(et_cpwd.getText().toString()) 
						&& et_pwd.getText().toString().length() == et_cpwd.getText().toString().length()){
					if(et_pwd.getText().toString().equals(et_cpwd.getText().toString()))
						resetPwdError();
					else
						et_pwd.setError(getString(R.string.password_invalid));
					areAllFieldsValid();
			}else if(et_cpwd.getText().toString().length() >= 8 && new PasswordValidator().validatePwd(et_cpwd.getText().toString()) && et_pwd.getText().toString().length() >= 8
						&& new PasswordValidator().validatePwd(et_pwd.getText().toString())) {
					if(!et_pwd.getText().toString().equals(et_cpwd.getText().toString()))
						et_cpwd.setError(getString(R.string.password_invalid));
					else 
						resetPwdError();
					areAllFieldsValid();
					
			}else{
				isNextEnabled = false;
				areAllFieldsValid();
			}
		}
		});		
	}
	
	private void resetPwdError() {
		if (et_pwd.getError() != null)
			et_pwd.setError(null);
		if (et_cpwd.getError() != null)
			et_cpwd.setError(null);
	}

	private void initializeCPwd(View view) {
		et_cpwd.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void afterTextChanged(Editable text) {
				if(et_pwd.getText().toString().length() >= 8 && new PasswordValidator().validatePwd(et_pwd.getText().toString()) 
						&& et_cpwd.getText().toString().length() == et_pwd.getText().toString().length()){
					formAbandonmentValues.append("7:PASS2>");
					if(et_cpwd.getText().toString().equals(et_pwd.getText().toString())){
						if (et_pwd.getError() != null)
							et_pwd.setError(null);
					}else{
						et_cpwd.setError("Confirm password should match password");
					}
				}else if(et_cpwd.getText().toString().length() > et_pwd.getText().toString().length()){
					et_cpwd.setError("Confirm password should match password");
				}else{
					if (et_pwd.getError() != null)
						et_pwd.setError(null);
				}
				areAllFieldsValid();
			}
		});
	}

	private void initializeUNameField(View view) {
		et_uname.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					if (getTextSize(et_uname) < 6) 
						et_uname.setError(invalidUserName);
					else
						formAbandonmentValues.append("5:USN>");
				}
			}
		});
		et_uname.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void afterTextChanged(Editable text) {
				textChangedUname(text);
			}
		});
	}
	private void textChangedUname(Editable text) {
		if (et_uname.getError() != null)
			et_uname.setError(null);
		if (text.length() == 2)
			et_uname.requestFocus();
		areAllFieldsValid();
	}

	public void openSaveMMNDialog(){
		SaveMMNDialogFragment saveMMNDialogFragment = SaveMMNDialogFragment.newInstance(rDataManager.getFromRegistrationCache(RegistrationKey.ACCOUNT_NUMBER),
				rDataManager.getFromRegistrationCache(RegistrationKey.CUSTOMER_ID));
		saveMMNDialogFragment.setMMNEnteredListener( new MMNEnteredListener() {
			@Override
			public void onMmnEntered(boolean saveMMNDone) {
				if(saveMMNDone){
					getMainActivity().setFragment(2);
				}
			}
		});
		saveMMNDialogFragment.show(getFragmentManager(), "dialog");
	}

	public boolean validateAllFields() {
		boolean validationResult = false;
		String userName = et_uname.getText().toString();
		String pwd = et_pwd.getText().toString();
		String confirmPwd = et_cpwd.getText().toString();
		String cvv_entered = et_cvv.getText().toString();
		boolean usernameValidate = isUsernameValid(userName, et_uname, invalidUserName,false);
		boolean pwdValidate = isPwdValid(pwd, et_pwd, invalidUserName,false,false);
		boolean confirmpwd = isPwdValid(confirmPwd, et_cpwd, invalidPwd,false,false);
		String trackFormErrors = trackFormErrors(userName, pwd, confirmPwd, pwdValidate, confirmpwd, usernameValidate);
		boolean confirmCVV = isValidCVV(cvv_entered, et_cvv, invalidCVV,true);
		if (usernameValidate && pwdValidate && confirmpwd && 
				((cvvLayout.getVisibility() == View.VISIBLE) ? confirmCVV : true)) {
			if(pwd.equals(confirmPwd)){
				if(pwd.equals(userName))
					et_pwd.setError("user name and password cannot be same");
				else
					validationResult = true;
			}
			else
				et_cpwd.setError("Confirm password should match password");
		}
		setFieldsFocus(validationResult, usernameValidate, pwdValidate,
				confirmpwd);
		if(validationResult){
			setRegistrationCache(userName, pwd, cvv_entered);
			if(retry){
				retry();
				return false;
			} 
		}else{
			AnalyticsManager.getInstance().trackRegCreateCredentialsScreenFormErrors(trackFormErrors);
		}
		getMainActivity().getViewPager().setSwipe_enabled(validationResult);
		return validationResult;
	}

	private void setRegistrationCache(String userName, String pwd,
			String cvv_entered) {
		rDataManager.setInRegistrationCache(RegistrationKey.USER_NAME, userName);
		rDataManager.setInRegistrationCache(RegistrationKey.PASSWORD, pwd);
		rDataManager.setInRegistrationCache(RegistrationKey.CVV, cvv_entered);
		if(getMainActivity().getCounter() < 2){
			getMainActivity().setIndicatorProp(2);
			getMainActivity().setCounter(2);
		}
	}

	private void retry() {
		if((cvvLayout.getVisibility() == View.VISIBLE)){
			rDataManager.setInRegistrationCache(RegistrationKey.ACCOUNT_LOCKED_FLAG, String.valueOf(true));
			ReRegisterCustomerService.reRegisterCustomer(ReRegisterCustomerService.postParameters(rDataManager), false, this);
		} else {
			RegisterCustomerService.registerCustomer(RegisterCustomerService.postParameters(rDataManager), false, this);
		}
	}

	private void setFieldsFocus(boolean validationResult,
			boolean usernameValidate, boolean pwdValidate, boolean confirmpwd) {
		if(usernameValidate){
			et_uname.requestFocus();
		}
		if(pwdValidate){
			et_pwd.requestFocus();
		}
		if(confirmpwd){
			et_cpwd.requestFocus();
		}

		if(validationResult){
			setPwd(et_pwd.getText().toString());
		}
	}


	public boolean isUsernameValid(String userName, EditText editText, String error,Boolean showError) {
		boolean result = false;
		if(userName.length()==0 && showError){
			editText.setError(missingField);
		}
		else if (userName.length() >= 6 && userName.length() <=30) {
			result = true;
		}else{
			if(showError)
				editText.setError(invalidUserName);
		}

		return result;
	}

	public boolean isValidCVV(String cvv, EditText editText, String error,Boolean showError) {
		boolean result = false;
		if(cvv.length()==0 && showError){
			editText.setError(missingField);
		}
		else if (cvv.length() >= 3) {
			result = true;
		}else{
			if(showError)
				editText.setError(invalidCVV);
		}

		return result;
	}

	public boolean isPwdValid(String pwd, EditText editText, String error, Boolean showError, Boolean isConfirmPwd) {
		boolean result = false;
		if(pwd.length()==0 && showError){
			editText.setError(missingField);
		}
		else if (pwd.length() >= 8 && pwd.length() <=30) {
			if( new PasswordValidator().validatePwd(pwd))
				result = true;
			else{
				if(showError){
					if(isConfirmPwd){
						editText.setError(password_match);
					}else{
						editText.setError(invalidPwd);
					}
				}
			}
		}else if(showError){
			if(isConfirmPwd){
				editText.setError(password_match);
			}else{
				editText.setError(invalidPwd);
			}
		}

		return result;
	}

	private int getTextSize(EditText field) {
		return field.getText().toString().trim().length();
	}

	public static String getPwd() {
		return pwd;
	}


	public static void setPwd(String pwd) {
		CreateCredentialsFragment.pwd =  pwd;
	}

	private void setMessages(){
		invalidUserName =StringUtils.getStringResource(getActivity(), R.string.username_warning);
		missingField =StringUtils.getStringResource(getActivity(), R.string.missing_field);
		invalidPwd =StringUtils.getStringResource(getActivity(), R.string.password_invalid);
		password_match =StringUtils.getStringResource(getActivity(), R.string.password_match);
		invalidCVV =StringUtils.getStringResource(getActivity(), R.string.cvv);
	}

	@Override
	public boolean handleBackPressed(){
		getFragmentManager().popBackStack();
		return false;
	}

	public Boolean areAllFieldsValid(){
		Boolean areValid = false;
		String userName = et_uname.getText().toString();
		String pwd = et_pwd.getText().toString();
		String confirmPwd = et_cpwd.getText().toString();
		String cvv_entered = et_cvv.getText().toString();
		boolean confirmCVV = isValidCVV(cvv_entered, et_cvv, invalidCVV,false);
		boolean pwdValidate = isPwdValid(pwd, et_pwd, invalidUserName,false,false);
		boolean confirmpwd = isPwdValid(confirmPwd, et_cpwd, invalidPwd,false,false);
		boolean usernameValidate = isUsernameValid(userName, et_uname, invalidUserName,false);
		trackFormErrors(userName, pwd, confirmPwd,pwdValidate, confirmpwd, usernameValidate);
		if (usernameValidate && pwdValidate && confirmpwd && ((cvvLayout.getVisibility() == View.VISIBLE) ? confirmCVV : true)) {
			if(!pwd.equals(userName)){
				if(pwd.equals(confirmPwd)){
					areValid = true;
					rDataManager.setInRegistrationCache(RegistrationKey.USER_NAME, userName);
					rDataManager.setInRegistrationCache(RegistrationKey.PASSWORD, pwd);
				}
			}
		}else {
			isNextEnabled = false;
		}
		nextButton.setEnabled(areValid);
		isNextEnabled = areValid;
		return areValid;
	}

	private String trackFormErrors(String userName, String pwd,
			String confirmPwd,
			boolean pwdValidate, boolean confirmpwd, boolean usernameValidate) {
		StringBuilder trackFormErrors = new StringBuilder("");
		if(!usernameValidate && userName.length() >= 6){
			trackFormErrors.append("5:USN>");
		}
		
		if(!pwdValidate && pwd.length() >= 8){
			trackFormErrors.append("6:PASS1>");
		}
		
		if(!confirmpwd && confirmPwd.length() >= 8){
			trackFormErrors.append("7:PASS2>");
		}
		return trackFormErrors.toString();
	}

	private void showCVVExplanation() {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setCancelable(true);
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int height = metrics.heightPixels - 10;
		int width = metrics.widthPixels;
		LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		cvvView = inflater.inflate(R.layout.cvv_layout, null, true);
		cvvView.setMinimumWidth(width);
		cvvView.setMinimumHeight(height);

		DialogInterface.OnClickListener okay = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		};
		//builder.setTitle(getResources().getString(R.string.cvv_title));
		builder.setMessage(getResources().getString(R.string.cvv_msg));
		builder.setView(cvvView)
		.setPositiveButton(getResources().getString(R.string.OK), okay)				
		.show();

	}


	public void disableDataEntry(){
		et_uname.setEnabled(false);
		et_uname.setFocusable(false);
		et_pwd.setEnabled(false);
		et_pwd.setFocusable(false);
		et_cpwd.setEnabled(false);
		et_cpwd.setFocusable(false);
		if(et_cvv.getVisibility() == View.VISIBLE){
			et_cvv.setEnabled(false);
			et_cvv.setFocusable(false);
		}
		nextButton.setEnabled(true);
	}


	@Override
	public void onFragmentDisplay() {
		AppUtils.showScreenNameToast(getActivity(), "Create credentials screen");
		getMainActivity().getViewPager().setSwipe_enabled(false);
		CustomFontButtonView backButton = (CustomFontButtonView) getMainActivity().findViewById(R.id.cancelORback);
		backButton.setText("Back");
		backButton.setContentDescription(getString(R.string.talkback_return_to_verify));
		validateForm();
		initializeNextBtn();
		TextView headerText = (TextView) getMainActivity().findViewById(R.id.commonHeader);
		headerText.setText(title);
		AnalyticsManager.getInstance().trackRegCreateCredentialLoad();
	}

	private void validateForm() {
		if(areAllFieldsValid()){
			nextButton.setEnabled(true);
		}
		else{
			nextButton.setEnabled(false);
		}
		if(getMainActivity().isValidateIdentity()){
			disableDataEntry();
		}
	}

	private void initializeNextBtn() {
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(validateAllFields()){
					setPwd(et_pwd.getText().toString());
					if(getMainActivity().getCounter() < 2){
						getMainActivity().setIndicatorProp(2);
						getMainActivity().setCounter(2);
					}
					getMainActivity().setFragment(2);
				}
			}
		});
	}

	@Override
	public void serviceRequestStarted() {
		DialogManager.getInstance().showProgressSpinner(getActivity());
	}

	@Override
	public void serviceRequestCompleted(Object obj) {
		super.serviceRequestCompleted(obj);
		if (obj instanceof VerifyIdentityResult) {
			VerifyIdentityResult result = (VerifyIdentityResult) obj;
			setRegistrationCachePartnerPhoneNumber(result);
			rDataManager.setInRegistrationCache(RegistrationKey.ACCOUNT_LOCKED_FLAG, String.valueOf(result.isRSALocked()));
			if (result.isRegistrationInfoInvalid() || result.isInvalidParamValue()) {
				switchToLoginInvalidAttempts(result.invalidAttemptsCount());
			} else if (result.isAlreadyRegRsaVerified() || result.isAlreadyRegRsaUnlocked()) {
				rDataManager.setInRegistrationCache(RegistrationKey.USER_NAME, result.getUserId());
				getMainActivity().switchToLogin("ALREADY_ENROLLED");
			} else if (result.isAlreadyRegRsaLocked()) {
				cvvLayout.setVisibility(View.VISIBLE);
				nextButton.setEnabled(false);
			}else if (result.isAccountLocked()) {
				getMainActivity().switchToLogin("REGISTRATION_ACC_CIF_LOCKED");				
			}else if(result.isAlreadyRegRsaDeleted()) {
				ReRegisterCustomerService.reRegisterCustomer(ReRegisterCustomerService.postParameters(rDataManager), false, this);			
				rDataManager.setInRegistrationCache( 
						RegistrationKey.CUSTOMER_ID, result.getCustomerId());
			} else if (result.isUsernameAlreadyTaken()) { 
				showUserNameAlreadyTakenError();
			} else if (result.isRegistrationPwdReused()) {
				showUsingOldPasswordError();
			}else if (result.isAlreadyRegRsaUnverified() || result.isAlreadyRegRsaNotEnrolled() || result.isRegistrationValid()){
				verifyRSAReg(result);
			}
		}
	}

	private void verifyRSAReg(VerifyIdentityResult result) {
		if(result.getUserId().equalsIgnoreCase(rDataManager.getFromRegistrationCache(RegistrationKey.USER_NAME))){
			getMainActivity().setValidateIdentity(true);
			setRegistrationCacheValidIdentity(result);
			retry = false;
			if(!result.isMMNPresent()) {
				openSaveMMNDialog();
			}else{
				getMainActivity().setFragment(2);
			}
		}else {
			ReRegisterCustomerService.reRegisterCustomer(ReRegisterCustomerService.postParameters(rDataManager), false, this);
		}
	}

	private void showUserNameAlreadyTakenError() {
		DialogManager.getInstance().setupSingleButtonDismissDialog(
				getResources().getString(R.string.error_title), getString(R.string.username_taken),
				getMainActivity(), "Ok");
	}

	private void showUsingOldPasswordError() {
		DialogManager.getInstance().setupSingleButtonDismissDialog(
				getResources().getString(R.string.error_title), getString(R.string.can_not_use_last_5_pwd),
				getMainActivity(), "Ok");
	}

	private void setRegistrationCachePartnerPhoneNumber(
			VerifyIdentityResult result) {

		if(null != result.getPartnerContactNumber() && !result.getPartnerContactNumber().equals("")){
			rDataManager.setInRegistrationCache(RegistrationKey.PARTNERPHONENUM, result.getPartnerContactNumber());
		}else{
			rDataManager.setInRegistrationCache(RegistrationKey.PARTNERPHONENUM, getString(R.string.contact_number));
		}
		
		
	}

	private void setRegistrationCacheValidIdentity(VerifyIdentityResult result) {
		if(null != result) {
			
			rDataManager.setInRegistrationCache(
					RegistrationKey.CUSTOMER_ID, result.getCustomerId());
			rDataManager.setInRegistrationCache(RegistrationKey.USER_NAME, result.getUserId());
			
			if(!StringUtils.isNullOrEmpty(result.getPrimaryEmailAddr())  ){
				rDataManager.setInRegistrationCache(RegistrationKey.EMAIL,  result.getPrimaryEmailAddr());
			}
			if(!StringUtils.isNullOrEmpty(result.getAccountNumber()) ){
				rDataManager.setInRegistrationCache(RegistrationKey.ACCNUM,  result.getAccountNumber());
			}
			if(!StringUtils.isNullOrEmpty(result.getPartnerName())){
				rDataManager.setInRegistrationCache(RegistrationKey.PARTNERNAME,  result.getPartnerName());
			}
			if(result.isPartnerEligibleForPaperless()){
				rDataManager.setInRegistrationCache(RegistrationKey.ISPAPERLESSELIGIBLE, "true");
			}else{
				rDataManager.setInRegistrationCache(RegistrationKey.ISPAPERLESSELIGIBLE, "NOT_ELIGIBLE");
				rDataManager.setInRegistrationCache(RegistrationKey.ENROLL_IN_PAPERLESS, "NOT_ELIGIBLE");
			}
			if(result.isUserEnrolledInPaperless()){
				rDataManager.setInRegistrationCache(RegistrationKey.ISUSERENROLLEDINPP, "ALREADY_ENROLLED");
				rDataManager.setInRegistrationCache(RegistrationKey.ENROLL_IN_PAPERLESS, "ALREADY_ENROLLED");
			}else{
				rDataManager.setInRegistrationCache(RegistrationKey.ISUSERENROLLEDINPP, "false");
			}
		}
	}

	private void switchToLoginInvalidAttempts(int invalidAttemptsCount) {
		switch (invalidAttemptsCount) {
		case 1:
			DialogManager.getInstance().setupSingleButtonDismissDialog(
					getResources().getString(R.string.error_title), getString(R.string.invalid_info),
					getMainActivity(), "Ok");
			getMainActivity().setFragment(0);
			break;
		case 2:
			DialogManager.getInstance().setupSingleButtonDismissDialog(
					getResources().getString(R.string.error_title), getString(R.string.one_attempt_left),
					getMainActivity(), "Ok");
			getMainActivity().setFragment(0);
			break;
		case 3:
			getMainActivity().switchToLogin("INVALID_ATTEMPTS");
			break;
		default:
			getMainActivity().switchToLogin("INVALID_ATTEMPTS");
			break;
		}
	}

	@Override
	public void serviceRequestFailed(ServiceException e) {
		DialogManager.getInstance().dismissProgressSpinner();
		DialogManager.getInstance().setupSingleButtonDismissDialog(getResources().getString(R.string.error_title),
				getResources().getString(R.string.error_general), getMainActivity(), "Ok");
	}

}
