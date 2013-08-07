package gmail.mchrisju.comiz;

import gmail.mchrisju.comiz.Fav;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class PartsActivity extends Activity {

	public String mName = null;
	public String mUrl = null;
	public SiteConfig mSite = null;
	private TextView mTitle = null;
	private ListView mList = null;
	private ProgressBar mProgress = null;
	private String[] mResult = null;
	private String datafile = "datafile";
	AsyncTask<String, Integer, String> asynctask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.parts);
		mList = (ListView) findViewById(R.id.result);
		mTitle = (TextView) findViewById(R.id.name);
		mProgress = ((ProgressBar) findViewById(R.id.progress));

		Intent intent = getIntent();
		mUrl = intent.getStringExtra("url");
		mSite = intent.getParcelableExtra("site");
		mName = intent.getStringExtra("name");
		mTitle.setText(mName);

		if (MyUtil.networkOk(this)) {
			mProgress.setVisibility(View.VISIBLE);
			asynctask = new DownloadParts().execute(mUrl);
		} else {
			Toast.makeText(this, "No network connection available.",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// android:targetSdkVersion 设为低值4.1才有菜单
		menu.add(0, MainActivity.ADD_TO_FAV, 0, R.string.add_to_fav);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MainActivity.ADD_TO_FAV:
			Fav fav = new Fav(mSite.name, mName, mUrl);
			if (mResult != null && mResult.length > 0) {
				fav.readednum = mResult.length;
				fav.unreadnum = 0;
				fav.supdate = StringUtils.splitByWholeSeparatorPreserveAllTokens(mResult[0], "||")[0];
			}
			SQLiteDatabase writer = new Db(this).getWritableDatabase();
			fav.add_or_update(writer);
			writer.close();
			Toast.makeText(this,
					mName + "\n" + getString(R.string.added_to_fav),
					Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
		return false;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int pos = ((AdapterContextMenuInfo) item.getMenuInfo()).position;
		switch (item.getItemId()) {
		case MainActivity.VIEW_LESSON_PAGE: {
			String[] ss = StringUtils.splitByWholeSeparatorPreserveAllTokens(mResult[pos], "||");
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ss[1]));
			startActivity(intent);
		}
			break;
		}
		return super.onContextItemSelected(item);
	}

	void releaseFavUpdate() {
		SQLiteDatabase writer = new Db(this).getWritableDatabase();
		String supdate = "";
		if (mResult.length > 0) {
			String[] ss = StringUtils.splitByWholeSeparatorPreserveAllTokens(mResult[0], "||");
			supdate = ss[0];
		}
		ContentValues values = new ContentValues();
		values.put("url", mUrl);
		values.put("supdate", supdate);
		values.put("readednum", mResult.length);
		values.put("unreadnum", 0);
		String where = String.format("site='%s' and comic='%s'", mSite.name,
				mName);
		writer.update("fav", values, where, null);
		writer.close();
	}

	private void BuildList() {
		String[] ss = getData();
		mList.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_checked, ss));
		mList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		// 添加长按点击
		mList.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				int selectedPosition = ((AdapterContextMenuInfo) menuInfo).position;
				String[] ss = StringUtils
						.split(mResult[selectedPosition], "||");
				Log.e("zz", "header:" + ss[0]);
				menu.setHeaderTitle(ss[0]);
				menu.add(0, MainActivity.VIEW_LESSON_PAGE, 0,
						R.string.open_in_browser);
			}
		});
	}

	private String[] getData() {

		List<String> r = new ArrayList<String>();
		/* 在数组中存放数据 */
		if (mResult != null) {
			for (int i = 0; i < mResult.length; i++) {
				String[] ss = StringUtils.splitByWholeSeparatorPreserveAllTokens(mResult[i], "||"); // 话名||url
				r.add(ss[0]);
			}
		}

		return r.toArray(new String[r.size()]);
	}

	public void myClickHandler(View view) {

		SparseBooleanArray checkedItems = mList.getCheckedItemPositions();
		if (checkedItems != null) {

			List<String> r = new ArrayList<String>();

			for (int i = 0; i < checkedItems.size(); i++) {
				if (checkedItems.valueAt(i)) {
					r.add(0, mResult[checkedItems.keyAt(i)]);
				}
			}

			Log.e("zz", "r.size()=" + r.size());

			if (r.size() > 0) {
				MyApp app = (MyApp) getApplication();
				if (r.size() + app.tasks.size() > 20) {
					new AlertDialog.Builder(this)
							.setMessage(R.string.too_many_tasks)
							.setPositiveButton(android.R.string.ok, null)
							.show();
					return;
				}

				Intent intent = new Intent();
				intent.putExtra("site", mSite.name);
				intent.putExtra("name", mName);
				intent.putExtra("task", r.toArray(new String[r.size()])); // 话名||url
				intent.setClass(PartsActivity.this, MainActivity.class);
				PartsActivity.this.setResult(RESULT_OK, intent);
				PartsActivity.this.finish();
			} else {
				Toast.makeText(PartsActivity.this, R.string.not_selected,
						Toast.LENGTH_SHORT).show();
			}
		}
		// new AlertDialog.Builder(this).setMessage(s).show();

	}

	private class DownloadParts extends AsyncTask<String, Integer, String> {
		ArrayList<String> lessons;

		@Override
		protected String doInBackground(String... urls) {
			lessons = new ArrayList<String>();
			String err = "";
			File file = new File(getCacheDir(), datafile + "parts"
					+ SystemClock.uptimeMillis());
			err = DownloadTask.getLessons(urls[0], mSite, file, lessons);
			file.delete();
			return err;
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String err) {
			if (err.equals("")) {
				mResult = lessons.toArray(new String[lessons.size()]);
				// Log.e("zz", "parts");
				for (int i = 0; i < mResult.length; i++) {
					// Log.e("zz", mResult[i]);
				}
				BuildList();
				releaseFavUpdate();
			} else {
				Toast.makeText(PartsActivity.this, err, Toast.LENGTH_LONG)
						.show();
			}
			mProgress.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onDestroy() {
		if (asynctask != null) {
			asynctask.cancel(true);
		}
		super.onDestroy();
	}

}
