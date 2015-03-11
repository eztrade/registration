package com.goldfish.registration;
 
import com.goldfish.BarclayCardApplication;
import com.goldfish.R;
import com.goldfish.utils.AppUtils;

import android.app.Activity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
 
public class ViewDisclosuresActivity extends Activity {
 
	private TextView disclosuretxt;
	private Button closeBtn;
	
	private final String REPLACED_NUMBER = "partnerNumber";
	private final String PHONE_NUMBER = "partnerPhoneNumber";
 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.disclosures_paperless);
		AppUtils.showScreenNameToast(this, "Paperless Statement Disclosures screen");
		closeBtn = (Button)findViewById(R.id.closeBtn);
		closeBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		disclosuretxt = (TextView) findViewById(R.id.disclosures_txt2);
		String disclosuresWithReplacedNumber = getString(R.string.disclosure_text2);
		
		if(null != getIntent().getExtras() && null != getIntent().getStringExtra(PHONE_NUMBER)){
			disclosuresWithReplacedNumber = disclosuresWithReplacedNumber.replace(REPLACED_NUMBER, getIntent().getStringExtra(PHONE_NUMBER));
		}else if(null != BarclayCardApplication.getApplication().getAuthResult()){
			disclosuresWithReplacedNumber = disclosuresWithReplacedNumber.replace(REPLACED_NUMBER, BarclayCardApplication.getApplication().getPartnerContactNumber());
		}
		
		disclosuretxt.setText(disclosuresWithReplacedNumber);
		disclosuretxt.setAutoLinkMask(Linkify.PHONE_NUMBERS);
		
 
	}
}