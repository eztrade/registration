package com.goldfish.registration;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.goldfish.R;
import com.goldfish.registration.ChooseSecurityQuestionsFragment.AnswerTextChangedListener;
import com.goldfish.registration.RegistrationDataManager.RegistrationKey;
import com.goldfish.services.model.RSAQuestion;
import com.goldfish.utils.AppUtils;

public class SecurityQuestionsAdapter extends BaseAdapter implements
		OnTouchListener {

	private final RegistrationMainActivity mContext;

	private final int QUESTION_VIEW = 0;
	private final int ANSWER_VIEW = 1;
	private final int FOOTER_VIEW = 2;
	private final int NUM_STATIC_FIELDS = 6;

	private final String QUESTION = "Question ";

	private List<RSAQuestion> chosenQuestions;
	private List<String> usersAnswers;
	private List<RSAQuestion> mListOfQuestions;
	private AnswerTextChangedListener mListener;
	private RegistrationDataManager rDataManager;

	public SecurityQuestionsAdapter(RegistrationMainActivity context,
			AnswerTextChangedListener listener) {
		super();
		mContext = context;
		rDataManager = new RegistrationDataManager();
		chosenQuestions = new ArrayList<RSAQuestion>();
		usersAnswers = new ArrayList<String>();
		mListener = listener;

		for (int i = 0; i < 5; i++) {
			chosenQuestions.add(null);
			usersAnswers.add(null);
		}
	}

	@Override
	public int getViewTypeCount() {
		return 3; // we have the footer layout, and the two view for question
					// and answer field
	}

	@Override
	public int getItemViewType(int position) {
		if (position == this.getCount() - 1)
			return FOOTER_VIEW;
		else
			return (position % 2 == 0) ? QUESTION_VIEW : ANSWER_VIEW;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getCount() {
		return chosenQuestions.size() + NUM_STATIC_FIELDS;
	}

	private int getQuestionPosition(int position) {
		return position / 2;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setSelectedQuestion(RSAQuestion question, int position) {
		chosenQuestions.set(getQuestionPosition(position), question);
		notifyDataSetChanged();
	}

	public void setQuestions(List<RSAQuestion> questions) {
		mListOfQuestions = questions;
	}

	public List<RSAQuestion> getAllChosenQuestions() {
		return chosenQuestions;
	}

	public List<String> getUsersAnswers() {
		return usersAnswers;
	}

	public void selectNewQuestion(final View view, final int position) {
		final RSAQuestionsAdapter adapter = new RSAQuestionsAdapter(mContext,
				mListOfQuestions, chosenQuestions);
		AlertDialog.Builder builderSingle = new AlertDialog.Builder(mContext);
		builderSingle.setTitle("Select a question");
		builderSingle.setAdapter(adapter,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						RSAQuestion question = adapter.getItem(which);
						((TextView) view.findViewById(R.id.selected_question))
								.setText(question.getText());
						((TextView) view.findViewById(R.id.answer_et))
								.setText("");
						
						((TextView) view.findViewById(R.id.answer_et)).setContentDescription("Double Tap to provide an answer");
						((TextView) view.findViewById(R.id.select_question)).setContentDescription(mContext.getString(R.string.empty_content_description));
						animateQuestionIn(question, position,view.findViewById(R.id.answerQuestionView));
					}
				});
		builderSingle.show();
	}

	@Override
	public View getView(final int position, View view, ViewGroup parent) {
		ViewHolder holder;
		int viewType = getItemViewType(position);

		if (view == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			holder = new ViewHolder();
			if (viewType == QUESTION_VIEW) {
				view = inflater.inflate(R.layout.question_item, null);
				holder.questionNumber = (TextView) view
						.findViewById(R.id.num_question);
			} else if (viewType == ANSWER_VIEW) {
				view = inflater.inflate(R.layout.rsa_question_item, null);
				holder.answerLayout = (LinearLayout) view
						.findViewById(R.id.answerQuestionView);
				holder.change = (Button) view.findViewById(R.id.change);
				holder.change.setContentDescription("change the security question, for item");
				holder.answer = (TextView) view.findViewById(R.id.answer_et);
				holder.questionTV = (TextView) view
						.findViewById(R.id.selected_question);
				holder.selectQuestion = (TextView) view
						.findViewById(R.id.select_question);
			} else {
				view = inflater.inflate(R.layout.footer_info, null);
			}
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		switch (viewType) {
		case QUESTION_VIEW:
			holder.questionNumber.setText(QUESTION
					+ (getQuestionPosition(position) + 1));
			view.setOnTouchListener(this);
			break;
		case ANSWER_VIEW:
			final RSAQuestion selectedQuestion = chosenQuestions
					.get(getQuestionPosition(position));
			final View convertView = view;
			if (selectedQuestion != null) {
				holder.answerLayout.setVisibility(View.VISIBLE);
				holder.change.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						selectNewQuestion(convertView, position);
					}

				});

				holder.answer.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						showPopoutEditor(selectedQuestion.getText(),
								getQuestionPosition(position));
					}

				});

				holder.questionTV.setText(selectedQuestion.getText());

				if (usersAnswers.get(getQuestionPosition(position)) != null) {
					String answer = usersAnswers
							.get(getQuestionPosition(position));
					holder.answer.setError(getErrorForAnswer(answer));
					holder.answer.setText(answer);
				} else {
					holder.answer.setText("");
					holder.answer.setError(null);
				}
			} else {
				holder.answerLayout.setVisibility(View.INVISIBLE);
			}
			break;
		case FOOTER_VIEW:
			view.setOnTouchListener(this);
			break;
		}

		return view;
	}

	static class ViewHolder {
		TextView questionTV;
		Button change;
		TextView answer;
		LinearLayout answerLayout;
		TextView selectQuestion;
		TextView questionNumber;
	}

	/**
	 * This will consume a touch event and get rid of highlights on fields we
	 * don't want it on
	 */
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		return true;
	}

	private String getErrorForAnswer(String answer) {
		if (answer.equals("")
				|| answer.equals(rDataManager
						.getFromRegistrationCache(RegistrationKey.PASSWORD)))
			return mContext.getString(R.string.error_rsa_answer);

		return null;
	}

	private void animateQuestionIn(final RSAQuestion question,
			final int position, View view) {
		if (view.getVisibility() == View.INVISIBLE) {
			Animation slideIn = AnimationUtils.loadAnimation(mContext,
					R.anim.slide_in_left);
			slideIn.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation arg0) {
					setSelectedQuestion(question, position);
				}

				@Override
				public void onAnimationRepeat(Animation arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationStart(Animation arg0) {
					// TODO Auto-generated method stub

				}

			});
			view.startAnimation(slideIn);

		} else
			setSelectedQuestion(question, position);
	}

	private void showPopoutEditor(String question, final int positionAnswer) {
		final Dialog popup = new Dialog(mContext, R.style.WhiteDialog);
		popup.requestWindowFeature(Window.FEATURE_NO_TITLE);
		popup.setContentView(R.layout.answer_rsa);
		popup.setCanceledOnTouchOutside(true);

		final EditText answer = (EditText) popup.findViewById(R.id.answer_et);
		answer.setContentDescription("Enter answer to security question,");
		final Button done = (Button) popup.findViewById(R.id.done);
		done.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				usersAnswers.set(positionAnswer, answer.getText().toString());
				notifyDataSetChanged();
				mListener.onAnswerTextChanged();

				AppUtils.hideKeyboard(answer, mContext);
				popup.dismiss();
			}

		});
		answer.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
						|| (actionId == EditorInfo.IME_ACTION_DONE)) {
					done.performClick();
				}
				return false;
			}
		});
		answer.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				if(null != answer.getError()){
					answer.setContentDescription(answer.getError());
				}
				
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				answer.setError(getErrorForAnswer(arg0.toString()));
			}
			
		});

		String userAnswer = usersAnswers.get(positionAnswer);
		if (userAnswer != null) {
			answer.setText(userAnswer);
			answer.setSelection(answer.getText().length());
			answer.setError(getErrorForAnswer(userAnswer));
		}

		((TextView) popup.findViewById(R.id.current_question))
				.setText(question);
		
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		Window window = popup.getWindow();
		lp.copyFrom(window.getAttributes());
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		window.setAttributes(lp);
		window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		popup.show();
	}
}