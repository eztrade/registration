package com.goldfish.registration;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.goldfish.R;
import com.goldfish.services.UrlManager;
import com.goldfish.services.model.Category;
import com.squareup.picasso.Picasso;

public class SecurityImageAdapter extends BaseAdapter {
	private Context mContext;
	private List<Category> mCategories;
	private int mCategoryPosition = 0;

	public SecurityImageAdapter(Context c, List<Category> categories) {
		mContext = c;
		mCategories = categories;
	}

	public int getCount() {
		return mCategories.get(mCategoryPosition).getPath().size();
	}
	
	public void refreshWithNewCategory(int categoryPosition){
		mCategoryPosition = categoryPosition;
		notifyDataSetChanged();
	}

	// create a new ImageView for each item referenced by the Adapter
	public View getView(int position, View convertView, ViewGroup parent) {
		SquaredImageView securityImageView = (SquaredImageView) convertView;

	    if (securityImageView == null) {
	    	securityImageView = new SquaredImageView(mContext);
	    	securityImageView.setPadding(10, 10, 10, 10);
	    	securityImageView.setScaleType(CENTER_CROP);
	    }
		String imageUrl = mCategories.get(mCategoryPosition).getPath().get(position);
		String imageAltText = mCategories.get(mCategoryPosition).getCategoryName() + position;
		
		Picasso.with(mContext).load(UrlManager.getInstance().getImageUrlString(imageUrl))
				.placeholder(R.drawable.placeholder).into(securityImageView);
		securityImageView.setContentDescription(imageAltText);

		return securityImageView;
	}

	@Override
	public String getItem(int position) {
		// TODO Auto-generated method stub
		return mCategories.get(mCategoryPosition).getPath().get(position);
	}
	
	public String getAltText(int position) {
		return mCategories.get(mCategoryPosition).getCategoryName() + " "+position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
}
