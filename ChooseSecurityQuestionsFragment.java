package com.goldfish.registration;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.goldfish.BarclayCardApplication;
import com.goldfish.R;
import com.goldfish.analytics.AnalyticsManager;
import com.goldfish.registration.RegistrationDataManager.QuestionKey;
import com.goldfish.registration.RegistrationDataManager.RegistrationKey;
import com.goldfish.services.BarclayServiceListener;
import com.goldfish.services.ServiceException;
import com.goldfish.services.helpers.GetRSAQuestionsService;
import com.goldfish.services.model.RSAQuestion;
import com.goldfish.services.model.RSAQuestionResponse;
import com.goldfish.ui.DialogManager;
import com.goldfish.utils.AppUtils;
import com.goldfish.widgets.CustomFontButtonView;

public class ChooseSecurityQuestionsFragment extends BaseRegistrationFragment
		implements IRegistrationFragments, OnItemClickListener,
		BarclayServiceListener {

	View mView;
	private SecurityQuestionsAdapter mQuestionsAdapter;
	CustomFontButtonView nextButton, cancelButton;
	private String title, next, back;
	private RegistrationDataManager rDataManager;

	final private AnswerTextChangedListener answerListener = new AnswerTextChangedListener() {

		@Override
		public void onAnswerTextChanged() {
			boolean enabled = validateQuestionsAnswers();
			nextButton.setEnabled(enabled);
			getMainActivity().getViewPager().setSwipe_enabled(enabled);
		}

	};

	public interface AnswerTextChangedListener {
		public void onAnswerTextChanged();
	}

	public static ChooseSecurityQuestionsFragment newInstance(Context cxt) {
		ChooseSecurityQuestionsFragment f = new ChooseSecurityQuestionsFragment();
		Bundle args = new Bundle();
		args.putString("title",
				cxt.getResources()
						.getString(R.string.security_questions_header));
		f.setArguments(args);

		return f;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
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
		mView = inflater.inflate(R.layout.question_selection_screen, null);
		next = getMainActivity().getResources().getString(R.string.next);
		back = getMainActivity().getResources().getString(R.string.back);
		nextButton = (CustomFontButtonView) getMainActivity().findViewById(
				R.id.next);
		cancelButton = (CustomFontButtonView) getMainActivity().findViewById(
				R.id.cancelORback);

		ListView questionsList = (ListView) mView
				.findViewById(R.id.security_questions_list);
		questionsList.setOnItemClickListener(this);

		if (mQuestionsAdapter == null) {
			mQuestionsAdapter = new SecurityQuestionsAdapter(getMainActivity(),
					answerListener);
		}

		questionsList.setAdapter(mQuestionsAdapter);

		questionsList.setOnTouchListener(new OnSwipeTouchListener(getMainActivity(), 3,
				this) {
		});

		return mView;
	}

	public boolean validateAllFields() {
		if (mQuestionsAdapter == null)
			return false;
		else
			return validateQuestionsAnswers();
	}

	private boolean validateQuestionsAnswers() {
		List<RSAQuestion> questions = mQuestionsAdapter.getAllChosenQuestions();
		List<String> answers = mQuestionsAdapter.getUsersAnswers();

		for (int i = 0; i < 5; i++) {
			String answer = answers.get(i);
			RSAQuestion question = questions.get(i);

			if (question == null)
				return false;

			if (answer == null)
				return false;

			if (answer.length() < 1 || answer.equals(rDataManager.getFromRegistrationCache(RegistrationKey.PASSWORD)))
				return false;

	
			switch (i) {
			case 0:
				rDataManager.setInRegistrationCache(
						RegistrationKey.ANSWER_1, answer);
				rDataManager.setInQuestionsCache(QuestionKey.QUESTION_1,
						question);
				break;
			case 1:
				rDataManager.setInRegistrationCache(
						RegistrationKey.ANSWER_2, answer);
				rDataManager.setInQuestionsCache(QuestionKey.QUESTION_2,
						question);
				break;
			case 2:
				rDataManager.setInRegistrationCache(
						RegistrationKey.ANSWER_3, answer);
				rDataManager.setInQuestionsCache(QuestionKey.QUESTION_3,
						question);
				break;
			case 3:
				rDataManager.setInRegistrationCache(
						RegistrationKey.ANSWER_4, answer);
				rDataManager.setInQuestionsCache(QuestionKey.QUESTION_4,
						question);
				break;
			case 4:
				rDataManager.setInRegistrationCache(
						RegistrationKey.ANSWER_5, answer);
				rDataManager.setInQuestionsCache(QuestionKey.QUESTION_5,
						question);
				break;
			}

		}
		if (getMainActivity().getCounter() < 4) {
			getMainActivity().setIndicatorProp(4);
			getMainActivity().setCounter(4);
		}
		return true;
	}

	private void dismissKeyboard() {
		InputMethodManager inputManager = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);

		// check if no view has focus:
		View v = getActivity().getCurrentFocus();
		if (v == null)
			return;

		inputManager.hideSoftInputFromWindow(v.getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@Override
	public void onFragmentDisplay() {
		cancelButton.setContentDescription(getString(R.string.talkback_return_choose_sec));
		AnalyticsManager.getInstance().trackRegSecQuestionsOnLoad();
		dismissKeyboard();
		TextView headerText = (TextView) getMainActivity().findViewById(
				R.id.commonHeader);
		headerText.setText(title);
		nextButton = (CustomFontButtonView) getMainActivity().findViewById(
				R.id.next);
		nextButton.setText(next);
		cancelButton = (CustomFontButtonView) getMainActivity().findViewById(
				R.id.cancelORback);
		cancelButton.setText(back);

		boolean fieldsValid = validateAllFields();
		nextButton.setEnabled(fieldsValid);
		getMainActivity().getViewPager().setSwipe_enabled(fieldsValid);

		CustomFontButtonView nextButton = (CustomFontButtonView) getMainActivity()
				.findViewById(R.id.next);
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (validateAllFields()) {
					getMainActivity().setFragment(4);
				}
			}
		});
		
		AppUtils.showScreenNameToast(getActivity(), getString(R.string.choose_sec_qtions_screen));

		if (BarclayCardApplication.getApplication().getRSAResponse() == null)
			requestQuestions();
	}
	
	private void requestQuestions(){
		GetRSAQuestionsService.getRSAQuestions(GetRSAQuestionsService.getParameters(rDataManager
				.getFromRegistrationCache(RegistrationKey.USER_NAME)), false, this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, final View view,
			final int pos, long arg3) {
		if(BarclayCardApplication.getApplication().getRSAResponse()==null){
			requestQuestions();
		}
		else{
			mQuestionsAdapter.setQuestions(BarclayCardApplication.getApplication().getRSAResponse().getchallengeQuestionsList());
			mQuestionsAdapter.selectNewQuestion(view, pos);
		}
	}

	@Override
	public void serviceRequestStarted() {
		DialogManager.getInstance().showProgressSpinner(getActivity());
	}

	@Override
	public void serviceRequestCompleted(Object obj) {
		DialogManager.getInstance().dismissProgressSpinner();
		if (obj instanceof RSAQuestionResponse) {
			RSAQuestionResponse rsaResponse = (RSAQuestionResponse) obj;
			if(rsaResponse.getchallengeQuestionsList() != null){
				BarclayCardApplication.getApplication().setRSAResponse(rsaResponse);
				mQuestionsAdapter.setQuestions(rsaResponse
					.getchallengeQuestionsList());
			}
		}
	}

	@Override
	public void serviceRequestFailed(ServiceException e) {
		DialogManager.getInstance().dismissProgressSpinner();
		defaultServiceExceptionHandler(e);
	}
	
	@Override
	protected void retryServiceCall() {
		requestQuestions();
	}
}
