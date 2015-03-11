package com.goldfish.registration;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.goldfish.R;
import com.goldfish.services.model.RSAQuestion;

public class RSAQuestionsAdapter extends BaseAdapter {

	private ArrayList<RSAQuestion> mQuestions;
	private Context mContext;

	public RSAQuestionsAdapter(Context context, List<RSAQuestion> questions, List<RSAQuestion> chosenQuestions) {
		mContext = context;
		mQuestions = new ArrayList<RSAQuestion>(questions);
		for(RSAQuestion rsaQuestion : chosenQuestions){
			if(questions.contains(rsaQuestion))
				mQuestions.remove(rsaQuestion);
		}
	}

	@Override
	public int getCount() {
		return mQuestions.size();
	}

	@Override
	public RSAQuestion getItem(int arg0) {
		return mQuestions.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {

		TextView question = (TextView) convertView;
		int paddingHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, mContext.getResources().getDisplayMetrics());
		int paddingLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, mContext.getResources().getDisplayMetrics());
		if (question == null) {
			question = new TextView(mContext);
			question.setPadding(paddingLeft, paddingHeight, 0, paddingHeight);
		}

		question.setText(mQuestions.get(position).getText());
		question.setTextColor(mContext.getResources().getColor(android.R.color.black));
		question.setBackgroundColor((position % 2 == 0) ? mContext.getResources()
				.getColor(R.color.white) : mContext.getResources().getColor(
				R.color.list_gray));

		return question;
	}

}
