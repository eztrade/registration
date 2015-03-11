package com.goldfish.registration;

import com.goldfish.R;
import com.goldfish.services.BarclayServiceListener;
import com.goldfish.services.ServiceException;
import com.goldfish.services.helpers.SaveMMNService;
import com.goldfish.ui.DialogManager;

import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SaveMMNDialogFragment extends DialogFragment implements BarclayServiceListener{
	private MMNEnteredListener mMMNEnteredListener;
	private EditText mmnEditText;
	private Button continueButton;
	private String accountNumber;
	private String customerId;
	
	public interface MMNEnteredListener {
		public void onMmnEntered(boolean mmnEntered);
	}
	
	static SaveMMNDialogFragment newInstance(String accNum, String custId) {
		SaveMMNDialogFragment f = new SaveMMNDialogFragment();
		Bundle args = new Bundle();
		args.putString("ACC_NUM", accNum);
		args.putString("CUST_ID", custId);
        f.setArguments(args);
        
        return f;
    }
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		accountNumber = args.getString("ACC_NUM");
		customerId = args.getString("CUST_ID");
		int style = DialogFragment.STYLE_NO_TITLE, theme = android.R.style.Theme_Black_NoTitleBar;
		setStyle(style, theme);
	}
	
	
	public void setMMNEnteredListener(MMNEnteredListener listener) {
		mMMNEnteredListener = listener;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.security_dialog, null);
		
		
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		getDialog().setCanceledOnTouchOutside(false);
		continueButton = (Button) view.findViewById(R.id.btn_continue);
		continueButton.setEnabled(false);
		
		mmnEditText =(EditText) view.findViewById(R.id.et_maidenname);
		
		mmnEditText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				continueButton.setEnabled(s.length() > 0);
			}
		});
		
		continueButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveMMN();
			}
		});
		
		getDialog().setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if( keyCode == KeyEvent.KEYCODE_BACK ){
		            return true;
		        }
		        return false;
			}
		});
		
		return view;
	}
	
	public void saveMMN(){
		SaveMMNService.saveMMN(SaveMMNService.postParameters(mmnEditText.getText().toString(), accountNumber, customerId), false, this);
	}

	@Override
	public void serviceRequestStarted() {
		DialogManager.getInstance().showProgressSpinner(getActivity());
	}

	@Override
	public void serviceRequestCompleted(Object obj) {
		DialogManager.getInstance().dismissProgressSpinner();
		if (null != mMMNEnteredListener) {
			mMMNEnteredListener.onMmnEntered(true);
		}
		dismiss();
		
	}

	@Override
	public void serviceRequestFailed(ServiceException e) {
		DialogManager.getInstance().dismissProgressSpinner();
	}
	
}
