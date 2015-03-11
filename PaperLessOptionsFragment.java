package com.goldfish.registration;

import com.goldfish.R;
import com.goldfish.analytics.AnalyticsManager;
import com.goldfish.utils.AppUtils;

import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class PaperLessOptionsFragment extends DialogFragment {
	private PaperLessOptionsListener mOptionsSelectionListener;
	private CheckBox cb_yes,cb_disclosures;
	private Button doneButton;
	private boolean chk1 = false;
	private boolean chk2 = false;
	private boolean isSelected = false;
	private String phoneNumber = "";
	private static String PHONE_NUMBER = "partnerPhoneNumber";
	
	public interface PaperLessOptionsListener {
		public void onPaperLessOptionsSelected(boolean noMail, boolean disclosures);
	}
	
	static PaperLessOptionsFragment newInstance(boolean selected, String partnerPhoneNumber) {
		PaperLessOptionsFragment f = new PaperLessOptionsFragment();
		Bundle args = new Bundle();
        args.putBoolean("IS_SELECTED", selected);
        args.putString(PHONE_NUMBER, partnerPhoneNumber);
        f.setArguments(args);
        
        return f;
    }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		isSelected = args.getBoolean("IS_SELECTED");
		phoneNumber = args.getString(PHONE_NUMBER);
		AppUtils.showScreenNameToast(getActivity(), getString(R.string.paperless_options_screen));
	}
	
	
	public void setPaperLessOptionsSelectionListener(PaperLessOptionsListener listener) {
		mOptionsSelectionListener = listener;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.registration_paperless_options_dialog, null);
		
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		getDialog().setCanceledOnTouchOutside(false);
		doneButton = (Button) view.findViewById(R.id.btn_Done);
		doneButton.setEnabled(false);
		
		TextView disclosurestv = (TextView) view.findViewById(R.id.disclosures);
		disclosurestv.setContentDescription("View paperless statements disclosures");
		disclosurestv.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), ViewDisclosuresActivity.class);
				intent.putExtra(PHONE_NUMBER, phoneNumber);
			    startActivity(intent);
				}
			});
		
		cb_yes =(CheckBox) view.findViewById(R.id.cb_yes);
		cb_disclosures =(CheckBox) view.findViewById(R.id.cb_disclosures);
		
		cb_yes.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				chk1 = isChecked;
				if(chk1 && chk2){
					doneButton.setEnabled(true);
				}else{
					doneButton.setEnabled(false);
				}
			}
		});
		
		cb_disclosures.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				chk2 = isChecked;
				if(chk1 && chk2){
					doneButton.setEnabled(true);
				}else {
					doneButton.setEnabled(false);
				}
			}
		});
		
		
		
		doneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null != mOptionsSelectionListener) {
					mOptionsSelectionListener.onPaperLessOptionsSelected(chk1, chk2);
				}
				dismiss();
			}
		});
		
		getDialog().setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if( keyCode == KeyEvent.KEYCODE_BACK ){
						if (null != mOptionsSelectionListener) {
							mOptionsSelectionListener.onPaperLessOptionsSelected(chk1, chk2);
					}
		            return false;
		        }
		        return false;
			}
		});
		
		AnalyticsManager.getInstance().trackRegPaperlessPopup();
		
		if(isSelected){
			cb_yes.setChecked(true);
			cb_disclosures.setChecked(true);
			doneButton.setEnabled(true);
		}
		
		return view;
	}
	
	
	
}
