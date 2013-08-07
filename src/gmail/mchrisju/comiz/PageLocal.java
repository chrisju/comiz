package gmail.mchrisju.comiz;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class PageLocal implements OnItemClickListener {
	private View mView = null;
	private ListView mList = null;
	private TextView mText = null;
	MyAdapter adapter;

	public PageLocal(View v) {
		mView = v;
		mList = (ListView) v.findViewById(R.id.list);
		mText = (TextView) v.findViewById(R.id.text);
		mList.setEmptyView(mText);
		mList.setOnItemClickListener(this);
		BuildList();
		initAdapter();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (android.os.Build.VERSION.SDK_INT < 7) {
			new AlertDialog.Builder(mView.getContext())
					.setMessage(
							mView.getContext().getString(
									R.string.low_sdk1,
									mView.getContext().getString(
											R.string.app_dir)))
					.setPositiveButton(android.R.string.ok, null).show();
			return;
		}

		if (arg1 != null) {
			TextView tv = (TextView) arg1;
			String text = tv.getText().toString();
			String[] ss = StringUtils.splitByWholeSeparatorPreserveAllTokens(text, PageTransmit.sep); // {name,lesson}

			Intent intent = new Intent();
			intent.putExtra("comic", ss[0]);
			intent.putExtra("lesson", ss[1]);
			intent.setClass(mView.getContext().getApplicationContext(),
					ImageViewActivity.class);
			((Activity) mView.getContext()).startActivity(intent);
		}
	}

	class MyAdapter extends BaseAdapter {

		private Context context;
		public ArrayList<String> arr;

		public MyAdapter(Context context) {
			super();
			this.context = context;
			arr = new ArrayList<String>();
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
			TextView tv;
			if (view == null) {
				tv = new TextView(context);
			} else {
				tv = (TextView) view;
			}
			tv.setTextSize(20);
			tv.setText(arr.get(position));

			return tv;
		}
	}

	void initAdapter() {
		adapter.arr.clear();
		File d = MyUtil.getMyExternalFilesDir(mView.getContext(), null);
		for (File fcomic : d.listFiles()) {
			if (fcomic.isDirectory()) {
				String comic = fcomic.getName();
				for (File flesson : fcomic.listFiles()) {
					if (flesson.isDirectory()) {
						String lesson = flesson.getName();
						if (flesson.listFiles().length > 0) {
							adapter.arr.add(0, comic + PageTransmit.sep
									+ lesson);
						}
					}
				}
			}
		}
		adapter.notifyDataSetChanged();
	}

	private void BuildList() {
		adapter = new MyAdapter(mList.getContext());
		mList.setAdapter(adapter);

		mList.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				int selectedPosition = ((AdapterContextMenuInfo) menuInfo).position;
				String text = adapter.arr.get(selectedPosition);
				menu.setHeaderTitle(text);
				menu.add(0, MainActivity.REMOVE_LOCAL, 0, R.string.delete);
			}
		});
	}

	void insertItem(String text) {
		adapter.arr.add(0, text);
		adapter.notifyDataSetChanged();
	}

}
