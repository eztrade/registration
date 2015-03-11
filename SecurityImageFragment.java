package com.goldfish.registration;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;

import com.goldfish.BarclayCardApplication;
import com.goldfish.R;
import com.goldfish.analytics.AnalyticsManager;
import com.goldfish.services.model.Category;

public class SecurityImageFragment extends DialogFragment implements
		OnItemClickListener, OnItemSelectedListener {

	private SecurityImageListener mListener;

	private GridView gridSecurityImages;
	private SecurityImageAdapter mAdapter;
	private Spinner categorySpinner;

	/**
	 * Create a new instance of MyDialogFragment, providing "num" as an
	 * argument.
	 */
	public static SecurityImageFragment newInstance() {
		SecurityImageFragment securityFragment = new SecurityImageFragment();

		return securityFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Pick a style based on the num.
		int style = DialogFragment.STYLE_NO_TITLE, theme = android.R.style.Theme_Black_NoTitleBar;
		setStyle(style, theme);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mView = inflater.inflate(R.layout.v4_fragment_security_image,
				container, false);

		gridSecurityImages = (GridView) mView
				.findViewById(R.id.secuirtyImageGrid);
		categorySpinner = (Spinner) mView.findViewById(R.id.category_spinner);
		gridSecurityImages.setOnItemClickListener(this);
		categorySpinner.setOnItemSelectedListener(this);

		loadSpinnerData();

		return mView;
	}

	@Override
	public void onResume() {
		super.onResume();
		AnalyticsManager.getInstance().trackRegImgSelectOnLoad();
		// GetSecurityImagesService.getImages("appt3st", false, this);

	}

	private void loadSpinnerData() {
		List<Category> categories = BarclayCardApplication.getApplication()
				.getSecurityImagesResponse().getCategory();
		List<String> categoryNames = new ArrayList<String>();
		for (Category category : categories) {
			categoryNames.add(category.getCategoryName());
		}

		ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(
				getActivity(), android.R.layout.simple_spinner_item,
				categoryNames);
		categoryAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAdapter = new SecurityImageAdapter(getActivity(), categories);

		categorySpinner.setAdapter(categoryAdapter);
		gridSecurityImages.setAdapter(mAdapter);
	}

	public void setOnImageSelectedListener(SecurityImageListener listener) {
		mListener = listener;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View image, int position, long arg3) {
		mListener.onImageSelected(mAdapter.getItem(position), mAdapter.getAltText(position));
		dismiss();
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
			long arg3) {
		mAdapter.refreshWithNewCategory(pos);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

}
