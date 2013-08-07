package gmail.mchrisju.comiz;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;

public class MyViewPager {

	private Context context = null;
	ViewPager mTab = null;

	private MyPagerAdapter myAdapter;
	private LayoutInflater mInflater;

	public List<View> mLayouts;
	public Object[] mPages = new Object[5];

	public MyViewPager(Context ctx, ViewPager pages) {

		this.context = ctx;
		this.mTab = pages;

		myAdapter = new MyPagerAdapter();
		pages.setAdapter(myAdapter);

		initViewList();

		setOnPageChangeListener(pages);
	}

	public void initViewList() {

		mLayouts = new ArrayList<View>();
		mInflater = ((Activity) context).getLayoutInflater();

		mLayouts.add(mInflater.inflate(R.layout.layout1, null));
		mLayouts.add(mInflater.inflate(R.layout.layout2, null));
		mLayouts.add(mInflater.inflate(R.layout.layout3, null));
		mLayouts.add(mInflater.inflate(R.layout.layout4, null));
		mLayouts.add(mInflater.inflate(R.layout.layout5, null));

		mPages[0] = new PageFav(mLayouts.get(0));
		mPages[1] = new PageSearch(mLayouts.get(1));
		mPages[3] = new PageLocal(mLayouts.get(3));
		mPages[4] = new PageTransmit(mLayouts.get(4));
	}

	public void setOnPageChangeListener(ViewPager pages) {

		pages.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				// Log.d("k", "onPageSelected : " + arg0);
				((MainActivity) context).drawSep(arg0);

				// activity从1到2滑动，2被加载后掉用此方法
				// View v = mListViews.get(arg0);
				// EditText editText = (EditText)v.findViewById(R.id.editText1);
				// editText.setText("动态设置#"+arg0+"edittext控件的值");

				// ImageView myImageView =
				// (ImageView)findViewById(R.id.imageview);
				// myImageView.setImageResource(R.drawable.androidbiancheng);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// Log.d("k", "onPageScrolled - " + arg0);
				// 从1到2滑动，在1滑动前调用
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// Log.d("k", "onPageScrollStateChanged - " + arg0);
				// 状态有三个0空闲，1是增在滑行中，2目标加载完毕
				/**
				 * Indicates that the pager is in an idle, settled state. The
				 * current page is fully in view and no animation is in
				 * progress.
				 */
				// public static final int SCROLL_STATE_IDLE = 0;
				/**
				 * Indicates that the pager is currently being dragged by the
				 * user.
				 */
				// public static final int SCROLL_STATE_DRAGGING = 1;
				/**
				 * Indicates that the pager is in the process of settling to a
				 * final position.
				 */
				// public static final int SCROLL_STATE_SETTLING = 2;

			}
		});

	}

	private class MyPagerAdapter extends PagerAdapter {

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			// Log.d("k", "destroyItem");
			((ViewPager) arg0).removeView(mLayouts.get(arg1));
		}

		@Override
		public void finishUpdate(View arg0) {
			// Log.d("k", "finishUpdate");
		}

		@Override
		public int getCount() {
			// Log.d("k", "getCount");
			return mLayouts.size();
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			// Log.d("k", "instantiateItem");
			((ViewPager) arg0).addView(mLayouts.get(arg1), 0);
			return mLayouts.get(arg1);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// Log.d("k", "isViewFromObject");
			return arg0 == (arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
			// Log.d("k", "restoreState");
		}

		@Override
		public Parcelable saveState() {
			// Log.d("k", "saveState");
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
			// Log.d("k", "startUpdate");
		}
	}
}
