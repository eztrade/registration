package com.goldfish.registration;

import java.net.SocketTimeoutException;

import com.goldfish.R;
import com.goldfish.services.BarclayServiceListener;
import com.goldfish.services.ServiceException;
import com.goldfish.ui.DialogManager;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;

public class BaseRegistrationFragment extends Fragment implements BarclayServiceListener{

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		hideKeyboard();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		hideKeyboard();
	}

	public boolean handleBackPressed() {
		return false;
	}
	
	public RegistrationMainActivity getMainActivity() {
		return (RegistrationMainActivity)getActivity();
	}

	public void hideKeyboard(){
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		try{
			if(imm.isActive()){
				imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
			}
		} catch(Exception e){
		}
	}

	protected String getProgressSpinnerMessage() {
		return null;
	}
	
	protected RegistrationDataManager getDataManager() {
		return getMainActivity().getDataManager();
	}
	
	@Override
	public void serviceRequestStarted() {
		DialogManager.getInstance().showProgressSpinner(getActivity());
	}

	@Override
	public void serviceRequestCompleted(Object obj) {
		DialogManager.getInstance().dismissProgressSpinner();
		
	}

	@Override
	public void serviceRequestFailed(ServiceException e) {
		DialogManager.getInstance().dismissProgressSpinner();
	}
	
	protected void defaultServiceExceptionHandler(ServiceException e) {
		if (e.getCause() != null && e.getCause() instanceof SocketTimeoutException) {
			DialogManager.getInstance().showRetryDialog(
				getActivity(),
				getResources().getString(R.string.error_title),
				getResources().getString(R.string.error_timeout_retry),
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						retryServiceCall();
						DialogManager.getInstance().closeDialog();
					}
				});
		} else{
			DialogManager.getInstance().setupSingleButtonDismissDialog(getResources().getString(R.string.error_title),
					getResources().getString(R.string.error_general), getMainActivity(), "Ok");
		}
		
	}

	protected void retryServiceCall() {
		// To be provided by subclass
	}
}