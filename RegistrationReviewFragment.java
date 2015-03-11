package com.goldfish.registration;

import java.net.SocketTimeoutException;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.goldfish.R;
import com.goldfish.analytics.AnalyticsManager;
import com.goldfish.login.LoginActivity;
import com.goldfish.login.LoginViewHandler;
import com.goldfish.login.LoginViewHandler.LoginListener;
import com.goldfish.registration.RegistrationDataManager.QuestionKey;
import com.goldfish.registration.RegistrationDataManager.RegistrationKey;
import com.goldfish.services.BarclayServiceListener;
import com.goldfish.services.ServiceException;
import com.goldfish.services.UrlManager;
import com.goldfish.services.helpers.EnrollInPassmarkService;
import com.goldfish.services.model.EnrollInPassmarkResponse;
import com.goldfish.services.model.RSAQuestion;
import com.goldfish.services.tasks.BarclayServiceTask;
import com.goldfish.ui.DialogManager;
import com.goldfish.utils.AppUtils;
import com.goldfish.utils.StringUtils;
import com.goldfish.widgets.CustomFontButtonView;
import com.goldfish.widgets.CustomFontTextView;
import com.squareup.picasso.Picasso;

public class RegistrationReviewFragment extends BaseRegistrationFragment
		implements IRegistrationFragments, OnClickListener,
		BarclayServiceListener, LoginListener {

	public static int securityImageId;
	private RadioButton rb_yes, rb_no;
	private CheckBox cb_yes, cb_disclosures;
	public static String email = "";
	private String title, next, back;
	private CustomFontButtonView nextButton, cancelButton;
	private final static String OPT_IN = "OPT_IN";
	private final static String OPT_OUT = "OPT_OUT";

	private LoginViewHandler loginService;
	private View mView;
	private TextView paperlessOption;
	private LinearLayout reviewPaperless;

	private RegistrationDataManager rDataManager;

	public static RegistrationReviewFragment newInstance(Context cxt) {
		RegistrationReviewFragment f = new RegistrationReviewFragment();
		Bundle args = new Bundle();
		args.putString("title",
				cxt.getResources().getString(R.string.review_heading));
		f.setArguments(args);

		return f;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onPause() {
		super.onPause();
		loginService.removeLoginListener();
	}

	@Override
	public void onResume() {
		super.onResume();
		loginService.registerLoginListener(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		title = getArguments().getString("title");
		loginService = new LoginViewHandler(getActivity());
		rDataManager = getDataManager();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.registration_review, null);

		next = getMainActivity().getResources().getString(
				R.string.review_enroll);
		back = getMainActivity().getResources().getString(R.string.back);

		reviewPaperless = (LinearLayout)mView.findViewById(R.id.reviewpaperlessLyt);

		return mView;
	}

	private void populateAllViews() {
		populateQuestionValueForKey(R.id.tv_q1, QuestionKey.QUESTION_1);
		populateQuestionValueForKey(R.id.tv_q2, QuestionKey.QUESTION_2);
		populateQuestionValueForKey(R.id.tv_q3, QuestionKey.QUESTION_3);
		populateQuestionValueForKey(R.id.tv_q4, QuestionKey.QUESTION_4);
		populateQuestionValueForKey(R.id.tv_q5, QuestionKey.QUESTION_5);

		populateAnswerValueForKey(R.id.tv_a1, RegistrationKey.ANSWER_1);
		populateAnswerValueForKey(R.id.tv_a2, RegistrationKey.ANSWER_2);
		populateAnswerValueForKey(R.id.tv_a3, RegistrationKey.ANSWER_3);
		populateAnswerValueForKey(R.id.tv_a4, RegistrationKey.ANSWER_4);
		populateAnswerValueForKey(R.id.tv_a5, RegistrationKey.ANSWER_5);

		populateAnswerValueForKey(R.id.tv_entered_phrase,
				RegistrationKey.SECURITY_PHRASE);
		populateAnswerValueForKey(R.id.tv_email, RegistrationKey.USER_EMAIL);

		mView.findViewById(R.id.edit_securityPhrase).setOnClickListener(this);
		mView.findViewById(R.id.edit_securityPhrase).setContentDescription("edit your security image and security phrase");
		mView.findViewById(R.id.edit_securityQuestions)
				.setOnClickListener(this);
		mView.findViewById(R.id.edit_securityQuestions).setContentDescription("edit your security questions and answers.");
		mView.findViewById(R.id.edit_email).setOnClickListener(this);
		mView.findViewById(R.id.edit_email).setContentDescription("edit your primary email address.");
		mView.findViewById(R.id.edit_paperlessOption).setOnClickListener(this);
		mView.findViewById(R.id.edit_paperlessOption).setContentDescription("edit your statement delivery preference.");

		SquaredImageView securityImage = (SquaredImageView) mView
				.findViewById(R.id.security_image);
		Picasso.with(getActivity())
				.load(UrlManager.getInstance().getImageUrlString(
						rDataManager.getFromRegistrationCache(
								RegistrationKey.SECURITY_IMAGE_PATH)))
				.placeholder(R.drawable.placeholder).into(securityImage);
		securityImage.setContentDescription(rDataManager.getFromRegistrationCache(RegistrationKey.SECURITY_IMAGE_CATEGORY) +"Security Image");
	}

	private void populateAnswerValueForKey(int textViewID, RegistrationKey key) {
		String uniqueValue = rDataManager.getFromRegistrationCache(key);
		((CustomFontTextView) mView.findViewById(textViewID))
				.setText(uniqueValue);
	}

	private void populateQuestionValueForKey(int textViewID, QuestionKey key) {
		RSAQuestion question = rDataManager.getFromQuestionsCache(key);
		((CustomFontTextView) mView.findViewById(textViewID)).setText(question
				.getText());
	}

	public boolean validateAllFields() {
		boolean validationResult = false;

		Boolean isStatementpref = false;
		Boolean isPaperless = rb_yes.isChecked();
		Boolean isNotPaperless = rb_no.isChecked();

		if (isPaperless || isNotPaperless) {
			if (isPaperless) {
				if (cb_yes.isChecked() && cb_disclosures.isChecked()) {
					isStatementpref = true;
				} else {
					if (!cb_yes.isChecked())
						cb_yes.setError("Plesae check this I agree checkbox");
					if (!cb_disclosures.isChecked())
						cb_yes.setError("Plesae check this disclosures checkbox");

				}
			} else {
				isStatementpref = true;
			}

		} else {
			rb_yes.setError("Please select a statement Preference");
			rb_no.setError("Please select a statement Preference");
		}

		if (isStatementpref) {
			validationResult = true;
		}
		getMainActivity().getViewPager().setSwipe_enabled(validationResult);

		return validationResult;
	}

	@Override
	public void onFragmentDisplay() {
		populateAllViews();
		TextView headerText = (TextView) getMainActivity().findViewById(
				R.id.commonHeader);
		headerText.setText(title);
		nextButton = (CustomFontButtonView) getMainActivity().findViewById(
				R.id.next);
		nextButton.setText(next);
		nextButton.setEnabled(true);
		cancelButton = (CustomFontButtonView) getMainActivity().findViewById(
				R.id.cancelORback);
		cancelButton.setText(back);
		cancelButton.setContentDescription(getString(R.string.talkback_return_to_enter_email));
		// setViews();
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				enrollInPassMark();
			}
		});
		reviewPaperlessOptions();
		AnalyticsManager.getInstance().trackRegReviewScreenLoad();
		
		AppUtils.showScreenNameToast(getActivity(), getString(R.string.review_registration_screen));
	}

	public void enrollInPassMark(){
		EnrollInPassmarkService.enrollInPassMark(EnrollInPassmarkService.getParameters(rDataManager), false, this);
	}

	public void reviewPaperlessOptions(){
		if(null != rDataManager.getFromRegistrationCache(RegistrationKey.ISPAPERLESSELIGIBLE)
				&& null != rDataManager.getFromRegistrationCache(RegistrationKey.ISUSERENROLLEDINPP)
				&& rDataManager.getFromRegistrationCache(RegistrationKey.ISPAPERLESSELIGIBLE).equalsIgnoreCase("true")
				&& (rDataManager.getFromRegistrationCache(RegistrationKey.ISUSERENROLLEDINPP).equalsIgnoreCase("false"))){
			if(null != rDataManager.getFromRegistrationCache(RegistrationKey.ENROLL_IN_PAPERLESS) &&
					null != rDataManager.getFromRegistrationCache(RegistrationKey.ACCNUM) &&
					null != rDataManager.getFromRegistrationCache(RegistrationKey.PARTNERNAME)){
				String accNum = rDataManager.getFromRegistrationCache(RegistrationKey.ACCNUM);
				String lastFourDigits = accNum.length() <= 4 ? accNum : accNum.substring(accNum.length() - 4);
				paperlessOption = (TextView)mView.findViewById(R.id.tv_paperless_option);
				if(rDataManager.getFromRegistrationCache(RegistrationKey.ENROLL_IN_PAPERLESS).equalsIgnoreCase("OPT_IN")){
					paperlessOption.setText(getString(R.string.paperless_yes_info)
							+ " " + rDataManager.getFromRegistrationCache(RegistrationKey.PARTNERNAME)
							+ " " + "ending in"
							+ " " + lastFourDigits);
					paperlessOption.setContentDescription(getString(R.string.paperless_yes_info)
							+ " " + rDataManager.getFromRegistrationCache(RegistrationKey.PARTNERNAME)
							+ " " + "ending in" + StringUtils.splitStringToChar(lastFourDigits));
				}else{
					paperlessOption.setText(getString(R.string.mail_statement));
				}
			}
		}else{
			reviewPaperless.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.edit_securityPhrase:
			getMainActivity().setFragment(2);
			break;
		case R.id.edit_securityQuestions:
			getMainActivity().setFragment(3);
			break;
		case R.id.edit_email:
			getMainActivity().setFragment(4);
			break;
		case R.id.edit_paperlessOption:
			getMainActivity().setFragment(4);
			break;
		}

	}

	@Override
	public void serviceRequestStarted() {
		DialogManager.getInstance().showProgressSpinner(getActivity());
	}

	@Override
	public void serviceRequestCompleted(Object obj) {
		DialogManager.getInstance().dismissProgressSpinner();

		if (obj instanceof EnrollInPassmarkResponse) {
			EnrollInPassmarkResponse response = (EnrollInPassmarkResponse) obj;
			if (response.getStatusInfo().getStatusCode().equals(BarclayServiceTask.SUCCESS_STATUS_CODE)) {
				getMainActivity().setValidateIdentity(false);
				String paperlessOption = rDataManager.getFromRegistrationCache(RegistrationKey.ENROLL_IN_PAPERLESS);
				if(paperlessOption.equalsIgnoreCase(OPT_IN))
					AnalyticsManager.getInstance().trackRegEmailPaperlessOptIn();
				else if(paperlessOption.equalsIgnoreCase(OPT_OUT))
					AnalyticsManager.getInstance().trackRegEmailUsMailOptIn();
				AnalyticsManager.getInstance().trackRegEnrollComplete();
				loginService.login(
						rDataManager.getFromRegistrationCache(
								RegistrationKey.USER_NAME),
								rDataManager.getFromRegistrationCache(
								RegistrationKey.PASSWORD), false);
			}else
				getMainActivity().switchToLogin(LoginActivity.REGISTRATION_LOGIN_FAILURE);
		}
	}

	@Override
	public void serviceRequestFailed(ServiceException e) {
		if(e.getCause() != null && e.getCause() instanceof SocketTimeoutException)
			defaultServiceExceptionHandler(e);
		else
			getMainActivity().switchToLogin(LoginActivity.REGISTRATION_LOGIN_FAILURE);
	}

	@Override
	public void loginStarted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loginSuccess() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loginFailure(ServiceException e, boolean logout) {
		getMainActivity().switchToLogin(LoginActivity.REGISTRATION_LOGIN_FAILURE);
	}
}
