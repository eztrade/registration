package com.goldfish.registration;

import java.util.Random;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.goldfish.BarclayCardApplication;
import com.goldfish.R;
import com.goldfish.analytics.AnalyticsManager;
import com.goldfish.registration.RegistrationDataManager.RegistrationKey;
import com.goldfish.services.BarclayServiceListener;
import com.goldfish.services.ServiceException;
import com.goldfish.services.UrlManager;
import com.goldfish.services.helpers.GetSecurityImageService;
import com.goldfish.services.model.Category;
import com.goldfish.services.model.SecurityImagesResponse;
import com.goldfish.ui.DialogManager;
import com.goldfish.utils.AppUtils;
import com.goldfish.widgets.CustomFontButtonView;
import com.squareup.picasso.Picasso;

public class ChooseSecurityOptionsFragment extends BaseRegistrationFragment
implements IRegistrationFragments, SecurityImageListener,
BarclayServiceListener {

	private ImageView securityImageView;
	private EditText et_phrase;
	private String phrase_msg;
	private String title, next, back;
	CustomFontButtonView nextButton, cancelButton;

	private Button changeImageBtn;

	private RegistrationDataManager rDataManager;
	
	private final String DIALOG_TAG = "image_dialog";

	public static ChooseSecurityOptionsFragment newInstance(Context cxt) {
		ChooseSecurityOptionsFragment f = new ChooseSecurityOptionsFragment();
		Bundle args = new Bundle();
		args.putString("title",
				cxt.getResources().getString(R.string.choose_security_header));
		f.setArguments(args);

		return f;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		title = getArguments().getString("title");
		rDataManager = getDataManager();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.registration_choose_security,
				null);

		next = getMainActivity().getResources().getString(R.string.next);
		back = getMainActivity().getResources().getString(R.string.back);

		view.setOnTouchListener(new OnSwipeTouchListener(getMainActivity(), 2,
				this) {
		});

		nextButton = (CustomFontButtonView) getMainActivity().findViewById(
				R.id.next);
		et_phrase = (EditText) view.findViewById(R.id.editText_phrase);
		phrase_msg = getResources().getString(R.string.security_phrase_warning);

		CustomFontButtonView backButton = (CustomFontButtonView) getMainActivity()
				.findViewById(R.id.cancelORback);

		backButton.setText(R.string.back);

		securityImageView = (ImageView) view
				.findViewById(R.id.imageView_default);
		loadImageFromCache();

		changeImageBtn = (Button) view.findViewById(R.id.btnChoose_image);
		changeImageBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(BarclayCardApplication.getApplication().getSecurityImagesResponse() == null)
					fetchSecurityImages();
				else
					showDialog();
			}
		});

		et_phrase.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					if (getTextSize(et_phrase) < 6) {
						et_phrase.setError(phrase_msg);
					}
				}
			}
		});
		et_phrase.addTextChangedListener(new TextWatcher() {
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
				if (et_phrase.getError() != null)
					et_phrase.setError(null);

				if(et_phrase.getText().toString().equals(rDataManager.getFromRegistrationCache(RegistrationKey.PASSWORD))){
					et_phrase.setError(phrase_msg);
				}
				validateAllFields();
			}
		});

		return view;

	}

	public void showDialog() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();

		// Remove a previous instance of the dialog if it exists.
		Fragment prev = getFragmentManager().findFragmentByTag(DIALOG_TAG);
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.

		SecurityImageFragment securityFragment = SecurityImageFragment
				.newInstance();
		securityFragment.setOnImageSelectedListener(this);
		securityFragment.show(ft, DIALOG_TAG);
	}

	public boolean validateAllFields() {
		boolean isValid = false;
		String phrase = et_phrase.getText().toString();
		if (phrase != null) {
			if (getTextSize(et_phrase) >= 6
					&& !phrase.equals(rDataManager.getFromRegistrationCache(RegistrationKey.PASSWORD))
					&& rDataManager.getFromRegistrationCache(
							RegistrationKey.SECURITY_IMAGE_PATH) != null) {
				isValid = true;
			}
		}
		if (isValid) {
			nextButton.setEnabled(true);
		} else {
			nextButton.setEnabled(false);
		}

		getMainActivity().getViewPager().setSwipe_enabled(isValid);
		if (isValid) {
			rDataManager.setInRegistrationCache(
					RegistrationKey.SECURITY_PHRASE, phrase);
			if(getMainActivity().getCounter() < 3){
				getMainActivity().setIndicatorProp(3);
				getMainActivity().setCounter(3);
			}
		}

		return isValid;

	}

	private int getTextSize(EditText field) {
		return field.getText().toString().trim().length();
	}

	private void selectRandomImage(SecurityImagesResponse response) {
		// -- The largest list will be all images
		int largestList = 0;
		Category allImages = null;
		for (Category category : response.getCategory()) {
			if (category.getPath().size() > largestList) {
				largestList = category.getPath().size();
				allImages = category;
			}
		}

		// Returns a pseudo-random uniformly distributed int in the half-open
		// range [0, n)
		Random rnd = new Random();
		int position = rnd.nextInt(allImages.getPath().size());

		rDataManager.setInRegistrationCache(
				RegistrationKey.SECURITY_IMAGE_PATH,
				allImages.getPath().get(position));
		securityImageView.setContentDescription(allImages.getCategoryName()+" "+position);
		rDataManager.setInRegistrationCache(RegistrationKey.SECURITY_IMAGE_CATEGORY, allImages.getCategoryName()+" "+position);
		loadImageFromCache();
	}

	private void loadImageFromCache() {
		String path = rDataManager.getFromRegistrationCache(
				RegistrationKey.SECURITY_IMAGE_PATH);
		if (path != null) {
			Picasso.with(getActivity())
			.load(UrlManager.getInstance().getImageUrlString(path))
			.placeholder(R.drawable.placeholder)
			.into(securityImageView);
		}
	}

	private void fetchSecurityImages(){
		if (BarclayCardApplication.getApplication().getSecurityImagesResponse() == null) {
			GetSecurityImageService.getSecurityImages(GetSecurityImageService.getParameters(rDataManager
							.getFromRegistrationCache(RegistrationKey.USER_NAME)), false, this);
		} else {
			changeImageBtn.setEnabled(true);
			if(null == rDataManager.getFromRegistrationCache(RegistrationKey.SECURITY_IMAGE_PATH)){
				selectRandomImage(BarclayCardApplication.getApplication()
						.getSecurityImagesResponse());
			}
		}

	}

	@Override
	public void onFragmentDisplay() {
		fetchSecurityImages();
		TextView headerText = (TextView) getMainActivity().findViewById(
				R.id.commonHeader);
		headerText.setText(title);
		nextButton = (CustomFontButtonView) getMainActivity().findViewById(
				R.id.next);
		nextButton.setText(next);
		nextButton.setEnabled(validateAllFields());

		cancelButton = (CustomFontButtonView) getMainActivity().findViewById(
				R.id.cancelORback);
		cancelButton.setText(back);
		cancelButton.setContentDescription(getString(R.string.talkback_return_to_cc));

		CustomFontButtonView nextButton = (CustomFontButtonView) getMainActivity()
				.findViewById(R.id.next);

		AnalyticsManager.getInstance().trackRegScurityOptionsOnLoad();

		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (validateAllFields()) {
					if (getMainActivity().getCounter() < 3) {
						getMainActivity().setIndicatorProp(3);
						getMainActivity().setCounter(3);
					}
					getMainActivity().setFragment(3);
				}
			}
		});
	}
	
	@Override
	public void onImageSelected(String path, String altText) {
		Picasso.with(getActivity())
		.load(UrlManager.getInstance().getImageUrlString(path))
		.placeholder(R.drawable.placeholder).into(securityImageView);
		securityImageView.setContentDescription(altText);
		rDataManager.setInRegistrationCache(
				RegistrationKey.SECURITY_IMAGE_PATH, path);
	}

	@Override
	public void serviceRequestStarted() {
		DialogManager.getInstance().showProgressSpinner(getActivity());
	}

	@Override
	public void serviceRequestCompleted(Object obj) {
		DialogManager.getInstance().dismissProgressSpinner();
		if (obj instanceof SecurityImagesResponse) {
			SecurityImagesResponse imageResponse = (SecurityImagesResponse) obj;
			BarclayCardApplication.getApplication().setSecurityImagesResponse(
					imageResponse);
			changeImageBtn.setEnabled(true);
			selectRandomImage(imageResponse);
		}
		AppUtils.showScreenNameToast(getActivity(), "Choose security options screen");
	}

	@Override
	public void serviceRequestFailed(ServiceException e) {
		DialogManager.getInstance().dismissProgressSpinner();
		defaultServiceExceptionHandler(e);
	}
	
	@Override
	protected void retryServiceCall() {
		fetchSecurityImages();
	}

}
