package gmail.mchrisju.comiz;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class PageTransmit implements OnItemClickListener {

	static final String sep = " -- ";

	MyApp app;
	ArrayList<DownloadTask> tasks = null;
	private DownloadReceiver mReciver = null;
	private View mView = null;
	private ListView mList = null;
	private TextView mText = null;
	MyAdapter adapter;

	@SuppressWarnings("unchecked")
	public PageTransmit(View v) {
		mView = v;
		app = (MyApp) v.getContext().getApplicationContext();
		tasks = (ArrayList<DownloadTask>) app.tasks.clone();
		mList = (ListView) v.findViewById(R.id.list);
		mText = (TextView) v.findViewById(R.id.text);
		mList.setEmptyView(mText);
		mList.setOnItemClickListener(this);
		mReciver = new DownloadReceiver(new Handler());
		BuildList();
	}

	public void AddTask(String site, String comic, String[] task) {

		// 创建漫画名目录
		// 不同网站同名漫画的覆盖问题?? - 不用考虑 覆盖就行
		File new_dir = new File(MyUtil.getMyExternalFilesDir(
				mView.getContext(), null), comic);
		if (!new_dir.exists()) {
			new_dir.mkdirs();
		}

		Log.e("zz", "AddTask task:\n" + task);
		for (String s : task) {
			// 格式: 话名||地址
			String[] ss = StringUtils.splitByWholeSeparatorPreserveAllTokens(s, "||");
			if(ss[0].isEmpty()){
				ss[0] = "no_name";
			}

			// 创建某一话的目录
			new_dir = new File(MyUtil.getMyExternalFilesDir(mView.getContext(),
					null), comic + "/" + ss[0]);
			if (!new_dir.exists()) {
				new_dir.mkdirs();
			}

			SiteConfig config = app.getSiteConfig(site);
			DownloadTask t = new DownloadTask(config, comic, ss[0], ss[1], 0,
					new_dir);
			t.receiver = mReciver;
			app.addDownloadTask(t);
			adapter.notifyDataSetChanged();
			Log.e("zz", "adapter.notifyDataSetChanged");

			if (MyUtil.networkOk(mView.getContext())) {

				new DownloadPicPage(t).execute(ss[1]);
				Toast.makeText(mView.getContext(), R.string.task_added,
						Toast.LENGTH_SHORT).show();

			} else {
				Toast.makeText(mView.getContext(),
						"No network connection available.", Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	class DownloadReceiver extends ResultReceiver {
		public DownloadReceiver(Handler handler) {
			super(handler);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			super.onReceiveResult(resultCode, resultData);
			if (resultCode == DownloadService.UPDATE_PROGRESS) {
				String op = resultData.getString("op");
				adapter.notifyDataSetChanged();
				if (op.equals("finished")) {
					String comic = resultData.getString("comic");
					String lesson = resultData.getString("lesson");
					PageLocal local = (PageLocal) ((MainActivity) mView
							.getContext()).mMyTab.mPages[3];
					local.insertItem(comic + sep + lesson);
				}
			}
		}
	}

	static void startDownloadService(Context context) {
		// service在第一次启动时创建
		Intent intent = new Intent(context, DownloadService.class);
		context.startService(intent);
	}

	private class DownloadPicPage extends AsyncTask<String, Integer, String> {
		DownloadTask task;

		public DownloadPicPage(DownloadTask t) {
			task = t;
		}

		@Override
		protected String doInBackground(String... urls) {
			try {
				task.getPicUrls(mView.getContext());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "";
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String err) {
			try {
				adapter.notifyDataSetChanged();

				startDownloadService(mView.getContext());

				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(mView.getContext());
				if (prefs.getBoolean("onlywifi", false)) {
					if (!MyUtil.wifiOk(mView.getContext())
							&& MyUtil.networkOk(mView.getContext())) {
						Toast.makeText(mView.getContext(),
								R.string.wifishuttip, Toast.LENGTH_LONG).show();
					}
				}

			} catch (Exception e) {
				Toast.makeText(mView.getContext(), e.getMessage(),
						Toast.LENGTH_LONG).show();
			}
		}
	}

	class MyAdapter extends BaseAdapter {

		private LayoutInflater inflater;

		public MyAdapter(Context context) {
			super();
			inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// Log.e("zz", "in getCount()");
			return tasks.size();
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
			// Log.e("zz", "in getView()");
			if (view == null) {
				view = inflater.inflate(R.layout.item2, null);
			}
			final ImageView imgv = (ImageView) view.findViewById(R.id.img);
			final TextView tv = (TextView) view.findViewById(R.id.ItemTitle);
			final TextView tv2 = (TextView) view.findViewById(R.id.perc);
			final ProgressBar p = (ProgressBar) view
					.findViewById(R.id.progress);
			DownloadTask t = tasks.get(position);
			int imgid = R.drawable.pause;
			if (t.state == 0) {
				imgid = R.drawable.start;
			}
			imgv.setImageResource(imgid);
			tv.setText(t.comic + sep + t.lesson);
			if (t.urls != null) {
				p.setMax(t.urls.length);
			}
			p.setProgress(t.progress);
			tv2.setText(String.format("%d/%d", p.getProgress(), p.getMax()));
			return view;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void notifyDataSetChanged() {
			// Log.e("zz", "in notifyDataSetChanged()");
			tasks = (ArrayList<DownloadTask>) app.tasks.clone();
			super.notifyDataSetChanged();
		}

	}

	private void BuildList() {
		adapter = new MyAdapter(mList.getContext());
		mList.setAdapter(adapter);

		mList.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				int selectedPosition = ((AdapterContextMenuInfo) menuInfo).position;
				DownloadTask t = tasks.get(selectedPosition);
				String title = new String(t.comic + sep + t.lesson);
				menu.setHeaderTitle(title);
				menu.add(0, MainActivity.CANCEL_DOWNLOAD, 0,
						R.string.cancel_download);
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		int state = tasks.get(arg2).state;
		if (state == 0) {
			state = 1;
		} else {
			if (state == 1) {
				startDownloadService(mView.getContext());
			}
			state = 0;
		}
		tasks.get(arg2).state = state;
		adapter.notifyDataSetChanged();
		app.setTaskState(tasks.get(arg2).url, state);
	}

}
