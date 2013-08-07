package gmail.mchrisju.comiz;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class ImageViewActivity extends Activity implements
		View.OnTouchListener, View.OnFocusChangeListener,
		OnSeekBarChangeListener {

	private enum Orientation {
		LEFT, RIGHT
	};

	static final int GO_TO_PAGE = 0x0101;
	static final int FIT_WIDTH = 0x0111;
	static final int FIT_HEIGHT = 0x0112;
	static final int FIT_ORIGIN = 0x0113;
	static final int SCREEN_ORIENTATION = 0x0121;
	static final int REDOWNLOAD = 0x0122;
	static final int PRE_LESSON = 0x0131;
	static final int NEXT_LESSON = 0x0132;

	MyApp app;
	private int mCurrPage = 0;
	private String mComic;
	private String mLesson;
	private List<File> mFiles;
	private ImageView mImageView;
	private RelativeLayout mTipLayout;
	private LinearLayout mSeekLayout;
	private SeekBar mSeekBar;
	private TextView mSeekHint;
	private Bitmap[] mBitmaps;
	private TextView mText; // 页数提示
	private TextView mText2; // 缩放大小提示
	private Matrix mMatrix;
	private Matrix mMatrixTemp;
	private int mScaleType = 0; // [0, FIT_WIDTH, FIT_HEIGHT, FIT_ORIGIN]
	private MyTouchListener mListener;
	double mradius = 0.1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image);

		app = (MyApp) getApplication();
		mImageView = (ImageView) findViewById(R.id.img);
		mText = (TextView) findViewById(R.id.pagehint);
		mTipLayout = (RelativeLayout) findViewById(R.id.relative);
		mSeekLayout = (LinearLayout) findViewById(R.id.seekframe);
		mSeekBar = (SeekBar) findViewById(R.id.seek);
		mSeekHint = (TextView) findViewById(R.id.hint);

		mSeekBar.setOnSeekBarChangeListener(this);
		mSeekLayout.setVisibility(View.GONE);

		mMatrix = new Matrix();
		mMatrixTemp = new Matrix();
		mListener = new MyTouchListener();
		mImageView.setOnTouchListener(mListener);
		initButtons();

		// mText = new TextView(this);
		// mText.setTextSize(20);
		// mText.getPaint().setFakeBoldText(true);
		// mTipLayout.addView(mText, creatButtonParams(0.5, 1));

		mText2 = new TextView(this);
		mText2.setTextSize(20);
		mText2.getPaint().setFakeBoldText(true);
		mText2.setTextColor(Color.GREEN);
		mTipLayout.addView(mText2, creatButtonParams(0.5, 0.5));
		mText2.setVisibility(View.INVISIBLE);

		Intent intent = getIntent();
		mComic = intent.getStringExtra("comic");
		mLesson = intent.getStringExtra("lesson");

		initFiles();

		SetCurrPage(mCurrPage);

		// 继承上个activity的屏幕方向
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_BEHIND);
	}

	private void initFiles() {
		Log.e("zz", mComic + "/" + mLesson);
		File d = MyUtil.getMyExternalFilesDir(this, mComic + "/" + mLesson);
		File[] files = d.listFiles();
		if (files == null || files.length == 0) {
			finish();
		} else {
			mFiles = Arrays.asList(files);
			sortFiles(mFiles);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// android:targetSdkVersion 设为低值4.1才有菜单
		menu.add(0, ImageViewActivity.PRE_LESSON, 0, R.string.pre_lesson);
		menu.add(0, ImageViewActivity.NEXT_LESSON, 0, R.string.next_lesson);
		menu.add(0, ImageViewActivity.FIT_WIDTH, 0, R.string.fit_width);
		menu.add(0, ImageViewActivity.FIT_HEIGHT, 0, R.string.fit_height);
		menu.add(0, ImageViewActivity.FIT_ORIGIN, 0, R.string.fit_origin);
		menu.add(0, ImageViewActivity.SCREEN_ORIENTATION, 0,
				R.string.screeen_orientation);
		menu.add(0, ImageViewActivity.REDOWNLOAD, 0, R.string.redownloadpage);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ImageViewActivity.PRE_LESSON:
			gotoPreLesson();
			break;
		case ImageViewActivity.NEXT_LESSON:
			gotoNextLesson();
			break;
		case ImageViewActivity.FIT_WIDTH:
			mScaleType = FIT_WIDTH;
			SetCurrPage(mCurrPage);
			break;
		case ImageViewActivity.FIT_HEIGHT:
			mScaleType = FIT_HEIGHT;
			SetCurrPage(mCurrPage);
			break;
		case ImageViewActivity.FIT_ORIGIN:
			mScaleType = FIT_ORIGIN;
			SetCurrPage(mCurrPage);
			break;
		case ImageViewActivity.SCREEN_ORIENTATION: {
			new MyAlertDialogBuilder(this).show();
		}
			break;
		case ImageViewActivity.REDOWNLOAD: {
			// TODO 重新下载本页 需在收藏中的漫画
			new AlertDialog.Builder(this).setMessage(R.string.todo).show();
		}
			break;
		}
		return false;
	}

	private class MyAlertDialogBuilder extends AlertDialog.Builder {
		int type;
		int index = -1;

		public MyAlertDialogBuilder(Context arg0) {
			super(arg0);
			type = getRequestedOrientation();
			switch (type) {
			case ActivityInfo.SCREEN_ORIENTATION_SENSOR:
				index = 0;
				break;
			case ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE:
				index = 1;
				break;
			case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
				index = 2;
				break;
			}

			this.setTitle(R.string.screeen_orientation)
					.setSingleChoiceItems(R.array.screenorientation, index,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									switch (whichButton) {
									case 0:
										type = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
										break;
									case 1:
										type = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
										break;
									case 2:
										type = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
										break;

									default:
										break;
									}
									/*
									 * User clicked on a radio button do some
									 * stuff
									 */
								}
							})
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* User clicked Yes so do some stuff */
									ImageViewActivity.this
											.setRequestedOrientation(type);
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

									/* User clicked No so do some stuff */
								}
							});
		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private boolean gotoPreLesson() {
		File d = MyUtil.getMyExternalFilesDir(ImageViewActivity.this, mComic);
		ArrayList<File> dirs = new ArrayList<File>();
		for (File f : d.listFiles()) {
			if (f.isDirectory() && f.listFiles().length > 0) {
				dirs.add(f);
			}
		}
		if (dirs.size() > 0) {
			sortFiles(dirs);
			for (File f : dirs) {
				Log.e("zz", f.getName());
				if (f.getName().equals(mLesson)) {
					int index = dirs.indexOf(f);
					if (index > 0) {
						mLesson = dirs.get(index - 1).getName();
						initFiles();
						SetCurrPage(0);
						return true;
					}
				}
			}
		}
		Toast.makeText(ImageViewActivity.this, R.string.nomore,
				Toast.LENGTH_SHORT).show();
		return false;
	}

	private boolean gotoNextLesson() {
		File d = MyUtil.getMyExternalFilesDir(ImageViewActivity.this, mComic);
		ArrayList<File> dirs = new ArrayList<File>();
		for (File f : d.listFiles()) {
			if (f.isDirectory() && f.listFiles().length > 0) {
				dirs.add(f);
			}
		}
		if (dirs.size() > 0) {
			sortFiles(dirs);
			for (File f : dirs) {
				// Log.e("zz", f.getName());
				if (f.getName().equals(mLesson)) {
					int index = dirs.indexOf(f);
					if (index < dirs.size() - 1) {
						mLesson = dirs.get(index + 1).getName();
						initFiles();
						SetCurrPage(0);
						return true;
					}
				}
			}
		}
		Toast.makeText(ImageViewActivity.this, R.string.nomore,
				Toast.LENGTH_SHORT).show();
		return false;
	}

	private void gotoPrePage() {
		if (mCurrPage > 0) {
			SetCurrPage(mCurrPage - 1);
		} else {
			if (gotoPreLesson()) {
				SetCurrPage(mFiles.size() - 1);
			}
		}
	}

	private void gotoNextPage() {
		if (mCurrPage < mFiles.size() - 1) {
			SetCurrPage(mCurrPage + 1);
		} else {
			gotoNextLesson();
		}
	}

	private class leftClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (mSeekLayout.getVisibility() == View.VISIBLE) {
				return;
			}

			int orie = app
					.getDefaultSharedPreferencesInt("read_orientation", 1);
			if (orie == 0) {
				gotoPrePage();
			} else {
				gotoNextPage();
			}
		}
	}

	private class rightClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (mSeekLayout.getVisibility() == View.VISIBLE) {
				return;
			}

			int orie = app
					.getDefaultSharedPreferencesInt("read_orientation", 1);
			if (orie == 0) {
				gotoNextPage();
			} else {
				gotoPrePage();
			}
		}
	}

	private ImageButton createButton(Orientation o) {
		int offset = 10;
		final ImageButton btn = new ImageButton(this);
		btn.setImageBitmap(mBitmaps[0]);
		btn.setPadding(offset, offset, offset, offset);
		btn.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		btn.setOnTouchListener(this);
		// btn.setOnFocusChangeListener(this);

		if (o == Orientation.LEFT) {
			btn.setOnClickListener(new leftClickListener());
		} else if (o == Orientation.RIGHT) {
			btn.setOnClickListener(new rightClickListener());
		}
		return btn;
	}

	private RelativeLayout.LayoutParams creatButtonParams(double x, double y) {
		RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		if (x == 0) {
			lp1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		} else if (x == 0.5) {
			lp1.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		} else if (x == 1) {
			lp1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		}
		if (y == 0) {
			lp1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		} else if (y == 0.5) {
			lp1.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		} else if (y == 1) {
			lp1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		}
		return lp1;
	}

	private void initButtons() {
		int w = mImageView.getWidth();
		int h = mImageView.getHeight();
		int x = w < h ? w : h;
		int d = x / 20 < 40 ? 40 : x / 20;
		mBitmaps = new Bitmap[2];
		mBitmaps[0] = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);
		DoGraphic.drawTip(mBitmaps[0], Color.argb(100, 218, 218, 218));
		mBitmaps[1] = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);
		DoGraphic.drawTip(mBitmaps[1], Color.YELLOW);

		mTipLayout.addView(createButton(Orientation.LEFT),
				creatButtonParams(0, 0));
		mTipLayout.addView(createButton(Orientation.LEFT),
				creatButtonParams(0, 0.5));
		mTipLayout.addView(createButton(Orientation.LEFT),
				creatButtonParams(0, 1));
		mTipLayout.addView(createButton(Orientation.RIGHT),
				creatButtonParams(1, 0));
		mTipLayout.addView(createButton(Orientation.RIGHT),
				creatButtonParams(1, 0.5));
		mTipLayout.addView(createButton(Orientation.RIGHT),
				creatButtonParams(1, 1));

		// mImageView.setVisibility(View.INVISIBLE);
	}

	@SuppressWarnings("deprecation")
	void SetCurrPage(int page) {
		mCurrPage = page;

		Bitmap bm = BitmapFactory.decodeFile(mFiles.get(mCurrPage).getPath());
		Bitmap bm2;

		if (bm == null) {
			// Toast.makeText(this,
			// "invalid pic: " + mFiles.get(mCurrPage).getName(),
			// Toast.LENGTH_SHORT).show();
			bm = BitmapFactory.decodeResource(getResources(),
					R.drawable.invalid_img);
		}

		if (bm.getWidth() >= 2048 || bm.getHeight() >= 2048) {
			double w = bm.getWidth(), h = bm.getHeight();
			double d = w / h;
			if (d > 1) {
				w = 2000;
				h = (int) (w / d);
			} else {
				h = 2000;
				w = (int) (h * d);
			}
			bm2 = Bitmap.createScaledBitmap(bm, (int) w, (int) h, false);
		} else {
			bm2 = bm;
		}

		mImageView.setImageBitmap(bm2);

		float sw = mImageView.getWidth(), sh = mImageView.getHeight();
		// Log.e("zz", "sw,sh=" + sw + "," + sh);
		if (sw < 1) { // view还没初始化时sw为0
			sw = getWindowManager().getDefaultDisplay().getWidth();
			sh = getWindowManager().getDefaultDisplay().getHeight();
		}
		mListener.setSize(bm2.getWidth(), bm2.getHeight());
		float[] values = new float[9];
		mMatrix.getValues(values);
		if (mScaleType == FIT_WIDTH) {
			float scale = sw / bm2.getWidth();
			values[Matrix.MSCALE_X] = values[Matrix.MSCALE_Y] = scale;
		} else if (mScaleType == FIT_HEIGHT) {
			float scale = sh / bm2.getHeight();
			values[Matrix.MSCALE_X] = values[Matrix.MSCALE_Y] = scale;
		} else if (mScaleType == FIT_ORIGIN) {
			float scale = 1.0f;
			values[Matrix.MSCALE_X] = values[Matrix.MSCALE_Y] = scale;
			mScaleType = 0; // 原始大小情况下翻页不调整缩放比率
		}

		// 过宽置右(或左),过高则置顶,否则居中
		if (bm2.getWidth() * values[Matrix.MSCALE_X] > sw) {
			int orie = app
					.getDefaultSharedPreferencesInt("read_orientation", 1);
			if (orie == 0) {
				values[Matrix.MTRANS_X] = 0;
			} else {
				values[Matrix.MTRANS_X] = sw - bm2.getWidth()
						* values[Matrix.MSCALE_X];
			}
		} else {
			values[Matrix.MTRANS_X] = (sw - bm2.getWidth()
					* values[Matrix.MSCALE_X]) / 2;
		}
		if (bm2.getHeight() * values[Matrix.MSCALE_Y] > sh) {
			values[Matrix.MTRANS_Y] = 0;
		} else {
			values[Matrix.MTRANS_Y] = (sh - bm2.getHeight()
					* values[Matrix.MSCALE_Y]) / 2;
		}
		mMatrix.setValues(values);
		mImageView.setImageMatrix(mMatrix);

		mText.setText(String.format("%s[%d/%d]", mLesson, 1 + mCurrPage,
				mFiles.size()));
		if (bm != bm2) {
			bm.recycle();
		}

	}

	void sortFiles(List<File> l) {
		if (!l.isEmpty()) {
			Collections.sort(l, new Comparator<File>() {
				@Override
				public int compare(File object1, File object2) {
					// 根据文本排序
					return object1.getName().compareToIgnoreCase(
							object2.getName());
				}
			});
		}
	}

	private class MyTouchListener implements OnTouchListener {
		float baseValue, x, y, last_x, last_y;
		int bm_w, bm_h;
		boolean doimage = false;
		boolean moved = false;

		void setSize(int w, int h) {
			bm_w = w;
			bm_h = h;
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (mSeekLayout.getVisibility() == View.VISIBLE) {
				return false;
			}

			boolean dealed = false;
			if (event.getPointerCount() != 2) {
				mText2.setVisibility(View.INVISIBLE);
			}

			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				doimage = false;
				dealed = true;
				moved = false;
				baseValue = 0;
				last_x = event.getRawX();
				last_y = event.getRawY();
			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				if (event.getPointerCount() == 2) {
					dealed = true;
					moved = true;
					float x = event.getX(0) - event.getX(1);
					float y = event.getY(0) - event.getY(1);
					float value = (float) Math.sqrt(x * x + y * y);// 计算两点的距离
					if (baseValue == 0) {
						baseValue = value;
						this.x = (event.getX(0) + event.getX(1)) / 2;
						this.y = (event.getY(0) + event.getY(1)) / 2;
						mMatrixTemp.set(mMatrix);
						mText2.setVisibility(View.VISIBLE);
					} else {
						if (value - baseValue >= 10 || value - baseValue <= -10) {
							float scale = value / baseValue;
							mMatrix.set(mMatrixTemp);
							float[] values = new float[9];
							mMatrix.getValues(values);
							// 限制缩放范围
							if (scale * values[Matrix.MSCALE_X] > 4.0)
								scale = (float) (4.0 / values[Matrix.MSCALE_X]);
							if (scale * values[Matrix.MSCALE_X] < 0.1)
								scale = (float) (0.1 / values[Matrix.MSCALE_X]);
							mMatrix.postScale(scale, scale, this.x, this.y);
						}
					}
				} else if (event.getPointerCount() == 1) {
					dealed = true;
					float x = event.getRawX();
					float y = event.getRawY();
					x -= last_x;
					y -= last_y;
					if (Math.abs(x) > 2 || Math.abs(y) > 2) {
						moved = true;
					}
					if (baseValue == 0) {
						// 限制移动范围
						int ww = mImageView.getWidth();
						int wh = mImageView.getHeight();
						float[] values = new float[9];
						mMatrix.getValues(values);
						x = MyUtil.limitedValue(x, -values[Matrix.MTRANS_X], ww
								- bm_w * values[Matrix.MSCALE_X]
								- values[Matrix.MTRANS_X]);
						y = MyUtil.limitedValue(y, -values[Matrix.MTRANS_Y], wh
								- bm_h * values[Matrix.MSCALE_Y]
								- values[Matrix.MTRANS_Y]);

						mMatrix.postTranslate(x, y);
					}
					last_x = event.getRawX();
					last_y = event.getRawY();
				}
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				if (doimage || moved) {
					dealed = true;
				}
			}

			float[] values = new float[9];
			mMatrix.getValues(values);
			mText2.setText(String.format("%d%%",
					(int) (values[Matrix.MSCALE_X] * 100)));
			mImageView.setImageMatrix(mMatrix);

			return dealed;
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			((ImageView) v).setImageBitmap(mBitmaps[1]);
		} else {
			((ImageView) v).setImageBitmap(mBitmaps[0]);// 还原图片
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			((ImageView) v).setImageBitmap(mBitmaps[1]);
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			((ImageView) v).setImageBitmap(mBitmaps[0]);// 还原图片
		}
		return false;
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			// Log.e("zz", "MotionEvent.ACTION_UP");
			float ww = mImageView.getWidth();
			float wh = mImageView.getHeight();
			if (MyUtil.inValueSection(event.getX(), ww / 8, ww * 7 / 8)
					&& MyUtil.inValueSection(event.getY(), wh / 8, wh * 7 / 8)) {
				if (mSeekLayout.getVisibility() == View.VISIBLE) {
					mSeekLayout.setVisibility(View.GONE);
				} else {
					int orie = app.getDefaultSharedPreferencesInt(
							"read_orientation", 1);
					if (orie == 1 && android.os.Build.VERSION.SDK_INT >= 11) {
						mSeekBar.setRotation(180);
					}
					mSeekBar.setProgress(mCurrPage);
					mSeekBar.setMax(mFiles.size() - 1);
					mSeekHint.setText(pagehint());
					mSeekLayout.setVisibility(View.VISIBLE);
				}
			}
			return true;
		default:
			return super.onTouchEvent(event);
		}
	}

	String pagehint() {
		String s = String.format("%d/%d", mCurrPage + 1, mFiles.size());
		return String.format("%s\n%s\n%s\n%s", mComic, mLesson, s,
				mFiles.get(mCurrPage).getPath());
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		SetCurrPage(progress);
		mSeekHint.setText(pagehint());
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// Log.e("zz", "onStartTrackingTouch");
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// Log.e("zz", "onStopTrackingTouch");
	}

}
