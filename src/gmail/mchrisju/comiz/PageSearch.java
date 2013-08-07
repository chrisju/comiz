package gmail.mchrisju.comiz;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class PageSearch {

	private final String datafile = "datafile";

	MyApp app;
	private Context context;
	private View mView;
	ListView mList;
	private EditText mEdit;
	private ProgressBar mProgress;
	private int mFinished; // 搜索完成的个数
	private ArrayList<DownloadSearchResult> mDownloaders;
	MyAdapter adapter;

	public PageSearch(View v) {
		context = v.getContext();
		app = (MyApp) context.getApplicationContext();
		mView = v;
		mDownloaders = new ArrayList<DownloadSearchResult>();

		mEdit = (EditText) v.findViewById(R.id.edit);
		mList = (ListView) v.findViewById(R.id.result);
		mProgress = ((ProgressBar) v.findViewById(R.id.progress));

		BuildList();

		mEdit.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					onClickSearch();
					handled = true;
				}
				return handled;
			}
		});

	}

	public void onClickSearch() {
		String keyword = mEdit.getText().toString().trim();
		if (keyword.equals("")) {
			keyword = context.getString(R.string.search_hint);
		}

		MyUtil.HideSoftInput(mView);

		// 初始化
		for (DownloadSearchResult downer : mDownloaders) {
			downer.cancel(true);
		}
		mDownloaders.clear();
		adapter.arr.clear();
		adapter.notifyDataSetChanged();
		mFinished = 0;

		if (MyUtil.networkOk(mView.getContext())) {
			mProgress.setVisibility(View.VISIBLE);
			for (SiteConfig site : app.configs) {
				if (site.hide == 0) {
					// String s = String.format(site.encoding, URLEncoder
					// .encode(mEdit.getText().toString(), "utf-8"));
					mDownloaders
							.add((DownloadSearchResult) new DownloadSearchResult(
									site).execute(keyword));
				}
			}
		} else {
			Toast.makeText(context, "No network connection available.",
					Toast.LENGTH_LONG).show();
		}
	}

	private class DownloadSearchResult extends
			AsyncTask<String, Integer, String> {
		SiteConfig site;
		String[] comics;

		DownloadSearchResult(SiteConfig _site) {
			site = _site;
		}

		@Override
		protected String doInBackground(String... params) {
			Log.e("zz", "downloading " + site.name + ":\t" + params[0]);
			try {
				byte[] keyword;
				if (site.encoding_search.equalsIgnoreCase("unicode-escape")) {
					Log.e("zz", "keyword0: " + MyUtil.escapeUnicode(params[0]));
					keyword = MyUtil.escapeUnicode(params[0]).getBytes();
				} else {
					keyword = params[0].getBytes(site.encoding_search);
				}
				Log.e("zz", site.name + "\tkeyword:\t" + keyword);

				String[] ss = MainActivity.luafunc2stringarray1b(
						site.luastring, "getsearchparam", keyword);
				for (int i = 0; i < ss.length; i++) {
					Log.e("zz", site.name + "\tsearch param:\t" + ss[i]);
				}

				String result;
				if (ss[2].equalsIgnoreCase("GET")) {
					result = MyUtil.downloadString(ss[0] + "?" + ss[1],
							site.encoding);
				} else {
					// List<NameValuePair> params1 = new
					// ArrayList<NameValuePair>();
					// params1.add(new BasicNameValuePair("show",
					// "title,btitle"));
					// params1.add(new BasicNameValuePair("keyboard",
					// params[0]));
					// result = MyUtil.doPost2(ss[0], params1);
					result = MyUtil.doPost(ss[0], ss[1], site.encoding);
					// File file = new File(
					// Environment.getExternalStorageDirectory(),
					// "search.txt");
					// MyUtil.WriteFile(file, result);
					// Log.e("zz", result);
				}

				// 处理结果
				String err = "";
				File file = new File(mView.getContext().getCacheDir(), datafile
						+ "search" + site.name + SystemClock.uptimeMillis());
				MyUtil.WriteFile(file, result);
				comics = MainActivity.luafunc2stringarray1(site.luastring,
						"getcomics", file.getPath());
				file.delete();
				return err;
			} catch (Exception e) {
				String err = mView.getContext().getString(R.string.failed_pre)
						+ e.getMessage();
				Log.e("zz", err);
				return err;
			}
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String err) {
			if (err.equals("")) {
				// Log.e("zz", "comics:");
				for (String as : comics) {
					// Log.e("zz", as);
					adapter.arr.add(new itemData(StringUtils.splitByWholeSeparatorPreserveAllTokens(as, "||"),
							site.name));
				}
				adapter.notifyDataSetChanged();
			}

			if (++mFinished == mDownloaders.size()) {
				mProgress.setVisibility(View.GONE);
			}
		}
	}

	private void BuildList() {
		adapter = new MyAdapter(mList.getContext());
		mList.setAdapter(adapter);

		mList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.e("zz", "on click search list");
				// Toast.makeText(context,
				// "你点击了第" + arg2 + "行:\n" + adapter.arr.get(arg2).url,
				// Toast.LENGTH_SHORT).show();

				Intent intent = new Intent();
				intent.putExtra("name", adapter.arr.get(arg2).name);
				intent.putExtra("url", adapter.arr.get(arg2).url);
				intent.putExtra("site",
						app.getSiteConfig(adapter.arr.get(arg2).site));
				intent.setClass(context.getApplicationContext(),
						PartsActivity.class);
				((Activity) context).startActivityForResult(intent,
						((MainActivity) context).REQUEST_TASK);
			}
		});

		// 添加长按点击
		mList.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				int selectedPosition = ((AdapterContextMenuInfo) menuInfo).position;
				itemData data = adapter.arr.get(selectedPosition);
				String title = new String(data.name);
				if (!data.author.equals("")) {
					title += "   (" + data.author + ")";
				}
				Log.e("zz", "header:" + title);
				menu.setHeaderTitle(title);
				menu.add(0, MainActivity.ADD_TO_FAV, 0, R.string.add_to_fav);
				menu.add(0, MainActivity.VIEW_COMIC_PAGE, 0,
						R.string.open_in_browser);
			}
		});

	}

	static class itemData {
		static final String sep = "||";
		static final String format = "%s||%s||%s||%s||%s";
		public String site;
		public String name;
		public String author;
		public String update;
		public String url;

		// comic: name||author||update||url
		itemData(String[] comic, String _site) {
			site = _site;
			name = comic[0].replace("/", "_");
			author = comic[1];
			update = comic[2];
			url = comic[3];
		}

		itemData(String s) {
			String[] ss = StringUtils.splitByWholeSeparatorPreserveAllTokens(s, "||");
			site = ss[0];
			name = ss[1];
			author = ss[2];
			update = ss[3];
			url = ss[4];
		}

		@Override
		public String toString() {
			return String.format(format, site, name, author, update, url);
		}

	}

	class MyAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		public ArrayList<itemData> arr;
		public ArrayList<View> views;

		public MyAdapter(Context context) {
			super();
			inflater = LayoutInflater.from(context);
			arr = new ArrayList<itemData>();
			views = new ArrayList<View>();
		}

		@Override
		public int getCount() {
			return arr.size();
		}

		@Override
		public Object getItem(int arg0) {
			return arg0;
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(final int position, View view, ViewGroup arg2) {
			return views.get(position);
		}

		@Override
		public void notifyDataSetChanged() {
			int oldnum = views.size();
			int newnum = arr.size();
			if (oldnum < newnum) { // 不够就新建
				for (int i = oldnum; i < newnum; i++) {
					views.add(inflater.inflate(R.layout.item, null));
				}
			} else if (oldnum > newnum) { // 多余则删除
				for (int i = newnum; i < oldnum; i++) {
					views.remove(0);
				}
			}

			// 设置view内容
			for (int position = 0; position < arr.size(); position++) {
				View view = views.get(position);
				final TextView tv = (TextView) view
						.findViewById(R.id.ItemTitle);
				final TextView tv2 = (TextView) view.findViewById(R.id.update);
				final TextView tv3 = (TextView) view.findViewById(R.id.site);

				String title = new String(arr.get(position).name);
				if (!arr.get(position).author.equals("")) {
					title += "   (" + arr.get(position).author + ")";
				}
				tv.setText(title);
				tv2.setText(arr.get(position).update);
				tv3.setText(arr.get(position).site);
			}
			super.notifyDataSetChanged();
		}

	}

}
