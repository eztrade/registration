package com.goldfish.registration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.goldfish.R;
import com.goldfish.analytics.AnalyticsManager;
import com.goldfish.registration.RegistrationDataManager.RegistrationKey;
import com.goldfish.services.ServiceException;
import com.goldfish.services.helpers.GetCountryListRegistrationService;
import com.goldfish.services.model.Country;
import com.goldfish.services.model.GetCountryListResponse;
import com.goldfish.ui.DialogManager;
import com.goldfish.utils.AppUtils;
import com.goldfish.utils.StringUtils;
import com.goldfish.widgets.CustomFontButtonView;


public class VerifyIdentityFragment extends BaseRegistrationFragment implements IRegistrationFragments{
	private Spinner countryList;
	private EditText et_AccNum, et_ssn1, et_ssn2, et_ssn3, et_dob_mm, et_dob_dt, et_dob_year;
	private String acc_msg,dob_msg,ssn_msg,cob_msg;
	private RadioGroup rGroup;
	private RadioButton rb_yes,rb_no;
	private String title,cancel;
	private CustomFontButtonView nextButton,backButton;
	private List<Country> countrylist;
	private HashMap<String, String> countryMap;
	ArrayList<String> countrySpinnerList;
	ArrayAdapter<String> dataAdapter;
	private RegistrationDataManager rDataManager;
	private View view;

	public static VerifyIdentityFragment newInstance(Context cxt) {
		VerifyIdentityFragment f = new VerifyIdentityFragment();
		Bundle args = new Bundle();
		args.putString("title",cxt.getResources().getString(R.string.verify_header));
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		title = getArguments().getString("title");
		AnalyticsManager.getInstance().trackRegIdentityScreenLoad();
		AnalyticsManager.getInstance().trackRegistrationStart();
		rDataManager = getDataManager();
	}

	public void disableDataEntry(){
		et_AccNum.setEnabled(false);
		et_ssn1.setEnabled(false);
		et_ssn2.setEnabled(false);
		et_ssn3.setEnabled(false);
		et_dob_mm.setEnabled(false);
		et_dob_dt.setEnabled(false);
		et_dob_year.setEnabled(false);
		countryList.setEnabled(false);
		nextButton.setEnabled(true);
		rGroup.setEnabled(false);
		rb_yes.setEnabled(false);
		rb_no.setEnabled(false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.registration_verify_identity, null);
		setMessages();

		GetCountryListRegistrationService.getCountryList(false, this);
		view.setOnTouchListener(new OnSwipeTouchListener(getMainActivity(),0,this){
		});
		setupUI();

		return view;
	}

	public void initUIWidgetsonScreen(){
		title =getResources().getString(R.string.verify_header);
		cancel =getMainActivity().getResources().getString(R.string.cancel);
		nextButton = (CustomFontButtonView) getMainActivity().findViewById(R.id.next);
		nextButton.setEnabled(false);
		backButton = (CustomFontButtonView) getMainActivity().findViewById(R.id.cancelORback);
		backButton.setContentDescription(getString(R.string.talkback_cancel_button));
		et_ssn1 = (EditText) view.findViewById(R.id.et_ssn1);
		et_ssn2 = (EditText) view.findViewById(R.id.et_ssn2);
		et_ssn3 = (EditText) view.findViewById(R.id.et_ssn3);
		et_ssn1.setTransformationMethod(PasswordTransformationMethod.getInstance());
		et_ssn2.setTransformationMethod(PasswordTransformationMethod.getInstance());
		et_ssn3.setTransformationMethod(PasswordTransformationMethod.getInstance());
		et_dob_mm = (EditText) view.findViewById(R.id.dob_month);
		et_dob_dt = (EditText) view.findViewById(R.id.dob_date);
		et_dob_year = (EditText) view.findViewById(R.id.dob_year);
		rb_yes =(RadioButton) view.findViewById(R.id.radioButton_yes);
		rb_no =(RadioButton) view.findViewById(R.id.radioButton_no);
		rGroup=(RadioGroup) view.findViewById(R.id.radioButton_group);
		countryList = (Spinner) view.findViewById(R.id.countryList);
		et_AccNum = (EditText) view.findViewById(R.id.et_AccNum);
	}

	private OnClickListener nextButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(validateAllFields()) {
				if(getMainActivity().getCounter() < 1){
					getMainActivity().setIndicatorProp(1);
					getMainActivity().setCounter(1);
				}
				getMainActivity().setFragment(1);
			}
		}
	};

	private OnClickListener backButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			getMainActivity().onBackPressed();
			AnalyticsManager.getInstance().trackRegIdentityScreenAbandonment();
		}
	};

	private TextWatcher commonTxtWatcher = new TextWatcher() {
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
			areAllFieldsValid();
		}
	};

	private TextWatcher dobMonthTxtWatcher = new TextWatcher() {
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
			if (et_dob_year.getError() != null)
				et_dob_year.setError(null);
			if (text.length() == 2)
				et_dob_dt.requestFocus();

			areAllFieldsValid();
		}
	};

	private TextWatcher dobDateTxtWatcher = new TextWatcher() {
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
			if (et_dob_year.getError() != null)
				et_dob_year.setError(null);
			if (text.length() == 2)
				et_dob_year.requestFocus();
			areAllFieldsValid();
		}
	};

	private TextWatcher dobYearTxtWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			String dob = et_dob_mm.getText().toString() + "/"
					+ et_dob_dt.getText().toString() + "/"
					+ et_dob_year.getText().toString();
			if ((et_dob_mm.getText().toString().length() == 2)
					&& (et_dob_dt.getText().toString().length() == 2)
					&& (et_dob_year.getText().toString().length() == 4)
					&& dobValidated(dob, et_dob_year,
							dob_msg,true)) {
			}
		}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}
		@Override
		public void afterTextChanged(Editable text) {
			if (et_dob_year.getError() != null)
				et_dob_year.setError(null);
			areAllFieldsValid();
		}
	};

	private TextWatcher ssn1TextWatcher = new TextWatcher() {
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
			if (et_ssn3.getError() != null)
				et_ssn3.setError(null);

			if (text.length() == 3)
				et_ssn2.requestFocus();
			areAllFieldsValid();
		}
	};

	private TextWatcher ssn2TextWatcher = new TextWatcher() {
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
			if (et_ssn3.getError() != null)
				et_ssn3.setError(null);

			if (text.length() == 2)
				et_ssn3.requestFocus();
			areAllFieldsValid();
		}
	};

	private OnFocusChangeListener accNumFocusChangeListener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(!hasFocus){
				if(validateEditText(et_AccNum, 15, 16,acc_msg,true)){
					areAllFieldsValid();
				}
			}
		}
	};
	
	private OnFocusChangeListener dobMonthFocusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				if (getTextSize(et_dob_mm) < 1) {
					et_dob_year.setError(dob_msg);
				}
				else if(getTextSize(et_dob_mm) ==1){
					String formatted = String.format(Locale.ENGLISH,
							"%02d", Integer.parseInt(et_dob_mm.getText()
									.toString()));
					et_dob_mm.setText(formatted);
				}
				areAllFieldsValid();
			}
		}
	};
	
	private OnFocusChangeListener dobDateFocusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				if (getTextSize(et_dob_dt) < 1) {
					et_dob_year.setError(dob_msg);
				}
				else if(getTextSize(et_dob_dt) ==1){
					String formatted = String.format(Locale.ENGLISH,
							"%02d", Integer.parseInt(et_dob_dt.getText()
									.toString()));
					et_dob_dt.setText(formatted);
				}
				areAllFieldsValid();
			}
		}
	};
	
	private OnFocusChangeListener dobYearFocusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			String dob = et_dob_mm.getText().toString() + "/"
					+ et_dob_dt.getText().toString() + "/"
					+ et_dob_year.getText().toString();
			if (!hasFocus) {
				if (getTextSize(et_dob_year) < 4
						|| getTextSize(et_dob_dt) < 2
						|| getTextSize(et_dob_mm) < 2) {
					et_dob_year.setError(dob_msg);
				} else if (getTextSize(et_dob_year) == 4
						&& getTextSize(et_dob_dt) == 2
						&& getTextSize(et_dob_mm) == 2
						&& dobValidated(dob, et_dob_year,
								dob_msg,true)) {
				}
			} else if ((et_dob_mm.getText().toString().length() < 2)
					|| (et_dob_dt.getText().toString().length() < 2)
					|| (et_dob_year.getText().toString().length() < 4)) {
			}
		}
	};
	
	private OnFocusChangeListener ssn3FocusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			String ssn = et_ssn1.getText().toString()
					+ et_ssn2.getText().toString()
					+ et_ssn3.getText().toString();
			if (!hasFocus) {
				if (!StringUtils.isSSN(ssn)) {
					et_ssn3.setError(ssn_msg);
				}
			}
		}
	};
	
	private OnEditorActionListener dobYearEditorActionListener = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId,
				KeyEvent event) {
			if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
					|| (actionId == EditorInfo.IME_ACTION_DONE)) {
				et_ssn1.requestFocus();
			}
			return false;
		}
	};
	
	private OnEditorActionListener accNumEditorActionListener = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId,
				KeyEvent event) {
			if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
					|| (actionId == EditorInfo.IME_ACTION_DONE)) {
				et_dob_mm.requestFocus();
			}
			return false;
		}
	};


	public void setupUI(){
		initUIWidgetsonScreen();
		backButton.setOnClickListener(backButtonListener);
		nextButton.setOnClickListener(nextButtonListener);
		et_AccNum.setOnFocusChangeListener(accNumFocusChangeListener);
		et_AccNum.addTextChangedListener(commonTxtWatcher);
		et_ssn3.addTextChangedListener(commonTxtWatcher);
		et_ssn1.addTextChangedListener(ssn1TextWatcher);
		et_ssn2.addTextChangedListener(ssn2TextWatcher);
		et_ssn3.setOnFocusChangeListener(ssn3FocusChangeListener);
		rGroup.setOnCheckedChangeListener(rGroupOnCheckedChangeListener);
		countryList.setOnItemSelectedListener(countrylistItemSelectedListener);
		et_dob_mm.addTextChangedListener(dobMonthTxtWatcher);
		et_dob_dt.addTextChangedListener(dobDateTxtWatcher);
		et_dob_year.addTextChangedListener(dobYearTxtWatcher);
		et_dob_mm.setOnFocusChangeListener(dobMonthFocusChangeListener);
		et_dob_dt.setOnFocusChangeListener(dobDateFocusChangeListener);
		et_dob_year.setOnFocusChangeListener(dobYearFocusChangeListener);
		et_dob_year.setOnEditorActionListener(dobYearEditorActionListener);
		et_AccNum.setOnEditorActionListener(accNumEditorActionListener);
		checkforAccessibility();
	}

	private OnCheckedChangeListener rGroupOnCheckedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch(checkedId) {
			case R.id.radioButton_yes:
				countryList.setVisibility(View.GONE);
				areAllFieldsValid();
				break;
			case R.id.radioButton_no:
				countryList.setVisibility(View.VISIBLE);
				break;
			default:
				break;
			}
			if(rb_no.getError()!=null){
				rb_no.setError(null);
			}
		}
	};

	private OnItemSelectedListener countrylistItemSelectedListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			if (countryList.getSelectedItemPosition() != 0) {
				validateCountrySpinnerField(countryList, true);
				areAllFieldsValid();
			}
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	public void checkforAccessibility(){
		if (AppUtils.isAccessibilityOn(getActivity())) {
			et_dob_mm.setHint(null);
			et_dob_dt.setHint(null);
			et_dob_year.setHint(null);
			et_dob_mm.setContentDescription(getString(R.string.talkback_mm));
			et_dob_dt.setContentDescription(getString(R.string.talkback_dd));
			et_dob_year.setContentDescription(getString(R.string.talkback_yyyy));
		}
	}

	private boolean dobValidated(String dob, EditText et, String error,Boolean showError) {
		if(StringUtils.isDOBValid(dob))
			return true;
		else{
			if(showError)
				et.setError(error);
		}
		return false;
	}

	private int getTextSize(EditText field) {
		return field.getText().toString().trim().length();
	}

	private boolean validateCountrySpinnerField(Spinner spinner_in, boolean showError) {
		if(rGroup.getCheckedRadioButtonId()==-1){
			if(showError){
				rb_no.setError(cob_msg);
			}
			return false;
		} else if(countryList.getVisibility()== View.VISIBLE){
			if (spinner_in.getSelectedItemPosition() != 0) {
				rDataManager.setInRegistrationCache(RegistrationKey.COUNTRYNAME, countryList.getSelectedItem().toString());
				return true;
			} else {
				DialogManager.getInstance().setupSingleButtonDismissDialog("Error", cob_msg, getActivity(), "Ok");
				return false;
			}
		}else{
			return true;
		}
	}

	private void setMessages(){
		acc_msg =StringUtils.getStringResource(getActivity(), R.string.account_no_warning);
		ssn_msg =StringUtils.getStringResource(getActivity(), R.string.ssn_warning);
		dob_msg =StringUtils.getStringResource(getActivity(), R.string.dob_warning);
		cob_msg =StringUtils.getStringResource(getActivity(), R.string.citizenship_warning);
	}

	public boolean validateEditText(EditText editText, int minChar, int maxChar, String errorMessage,Boolean showError) {
		int editTextCharLength = editText.getText().length();
		if (editTextCharLength < minChar || editTextCharLength > maxChar
				|| editText.getText().toString().substring(0, 1).equals(" ")) {
			if(showError)
				editText.setError(errorMessage);
			return false;
		} else {
			return true;
		}
	}

	public boolean validateAllFields() {
		String dob = et_dob_mm.getText().toString() + "/"+ et_dob_dt.getText().toString() + "/"+ et_dob_year.getText().toString();
		String ssn = et_ssn1.getText().toString()+ et_ssn2.getText().toString() + et_ssn3.getText().toString();
		boolean validationResult = false;
		StringBuilder trackFormErrors = new StringBuilder("");
		boolean accNumValidated = validateEditText(et_AccNum, 15,16, acc_msg,true);
		boolean dobValidated = dobValidated(dob, et_dob_year, dob_msg,true);
		boolean ssnValidated = StringUtils.isSSN(ssn);
		boolean countryValidated = validateCountrySpinnerField(countryList,true);
		if (!accNumValidated) {
			trackFormErrors.append("1:ACCN|");
			et_AccNum.requestFocus();
		} else if (!countryValidated) {
			trackFormErrors.append("4:USC");
			countryList.requestFocus();
		} else if (!ssnValidated) {
			trackFormErrors.append("3:SSN|");
			et_ssn1.requestFocus();
			et_ssn3.setError(ssn_msg);
		} else if (!dobValidated) {
			trackFormErrors.append("2:DOB|");
			et_dob_mm.requestFocus();
		}
		if (accNumValidated && countryValidated && ssnValidated && dobValidated ) {
			setInRegistrationCache(dob, ssn);
			validationResult = true;
		}else
			AnalyticsManager.getInstance().trackRegIdentityScreenFormErrors(trackFormErrors.toString());
		getMainActivity().getViewPager().setSwipe_enabled(validationResult);
		return validationResult;
	}

	private void setInRegistrationCache(String dob, String ssn) {
		rDataManager.setInRegistrationCache(RegistrationKey.ACCOUNT_NUMBER, et_AccNum.getText().toString());
		rDataManager.setInRegistrationCache(RegistrationKey.DOB, dob);
		rDataManager.setInRegistrationCache(RegistrationKey.SSN, ssn);
		if(rb_yes.isChecked())
			rDataManager.setInRegistrationCache(RegistrationKey.COUNTRY_CITIZENSHIP, "US");
		else
			rDataManager.setInRegistrationCache(RegistrationKey.COUNTRY_CITIZENSHIP, countryMap.get(countryList.getSelectedItem().toString()));
	}

	public Boolean areAllFieldsValid() {
		boolean areValid = false;
		String dob = et_dob_mm.getText().toString() + "/"+ et_dob_dt.getText().toString() + "/"+ et_dob_year.getText().toString();
		String ssn = et_ssn1.getText().toString()+ et_ssn2.getText().toString() + et_ssn3.getText().toString();
		StringBuilder trackFormErrors = new StringBuilder("");
		boolean accNumValidated = validateEditText(et_AccNum, 15,16, acc_msg,false);
		if(!accNumValidated && et_AccNum.getText().toString().length() >= 15)
			trackFormErrors.append("1:ACCN|");
		boolean dobValidated = dobValidated(dob, et_dob_year, dob_msg,false);
		if(!dobValidated && dob.length() == 8)
			trackFormErrors.append("2:DOB|");
		boolean ssnValidated = StringUtils.isSSN(ssn);
		if(!ssnValidated && ssn.length() == 9)
			trackFormErrors.append("3:SSN|");
		boolean countryValidated = validateCountrySpinnerField(countryList,false) ;
		if(!countryValidated)
			trackFormErrors.append("4:USC");
		if (accNumValidated && countryValidated && ssnValidated && dobValidated ) {
			setInRegistrationCache(dob, ssn);
			areValid = true;
		}
		nextButton.setEnabled(areValid);
		return areValid;
	}

	@Override
	public void serviceRequestStarted() {
		DialogManager.getInstance().showProgressSpinner(getActivity());
	}

	@Override
	public void serviceRequestCompleted(Object obj) {
		DialogManager.getInstance().dismissProgressSpinner();
		if(obj instanceof GetCountryListResponse){
			GetCountryListResponse result = (GetCountryListResponse)obj;
			countrylist = result.getCountryList();
			countrySpinnerList = new ArrayList<String>();
			countryMap = new HashMap<String, String>(countrylist.size());
			for (int i =0; i < countrylist.size(); i++) {
				String countryCode = countrylist.get(i).getCountryCode();
				String countryName = countrylist.get(i).getCountryName();
				if(i == 0){
					countrySpinnerList.add("Select a country of citizenship");
					countrySpinnerList.add(countryName);
				}else{
					countrySpinnerList.add(countryName);
				}
				countryMap.put(countryName, countryCode);
			}
			setDataAdapter(countryList, countrySpinnerList);
		}
		AppUtils.showScreenNameToast(getActivity(), "Verify your identity screen");
	}

	@Override
	public void serviceRequestFailed(ServiceException e) {
		DialogManager.getInstance().dismissProgressSpinner();
	}

	private void setDataAdapter(Spinner spinner, List<String> list_items) {
		dataAdapter = new ArrayAdapter<String>(getMainActivity(),
				android.R.layout.simple_spinner_item, list_items) {
			@Override
			public View getDropDownView(int position, View convertView,
					ViewGroup parent) {
				View v = null;
				if (position == 0) {
					TextView tv = new TextView(getContext());
					tv.setHeight(0);
					tv.setVisibility(View.GONE);
					v = tv;
				} else {
					v = super.getDropDownView(position, null, parent);
				}
				parent.setVerticalScrollBarEnabled(false);
				return v;
			}};
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(dataAdapter);
	}

	@Override
	public void onFragmentDisplay() {
		if(getMainActivity().isValidateIdentity())
			disableDataEntry();
		areAllFieldsValid();
		AnalyticsManager.getInstance().trackRegIdentityScreenLoad();
		TextView headerText = (TextView) getMainActivity().findViewById(R.id.commonHeader);
		headerText.setText(title);
		backButton = (CustomFontButtonView) getMainActivity().findViewById(R.id.cancelORback);
		backButton.setText(cancel);
		backButton.setContentDescription(getString(R.string.talkback_cancel_button));
		if(null != countrySpinnerList){
			setDataAdapter(countryList, countrySpinnerList);
			if(null != rDataManager.getFromRegistrationCache(RegistrationKey.COUNTRYNAME))
				countryList.setSelection(dataAdapter.getPosition(rDataManager.getFromRegistrationCache(RegistrationKey.COUNTRYNAME)));
		}
		nextButton.setOnClickListener(nextButtonListener);

	}

}