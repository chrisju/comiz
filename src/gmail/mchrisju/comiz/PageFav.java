package gmail.mchrisju.comiz;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class PageFav implements OnItemClickListener {
	private View mView = null;
	private ListView mList = null;
	private TextView mText = null;
	MyAdapter adapter;
	ArrayList<Fav> favs = null;

	public PageFav(View v) {
		mView = v;
		mList = (ListView) v.findViewById(R.id.list);
		mText = (TextView) v.findViewById(R.id.text);
		mList.setEmptyView(mText);
		mList.setOnItemClickListener(this);

		SQLiteDatabase reader = new Db(mView.getContext())
				.getReadableDatabase();
		favs = Fav.get_fav_list(reader);
		reader.close();

		BuildList();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		MyApp app = (MyApp) mView.getContext().getApplicationContext();
		Intent intent = new Intent();
		intent.putExtra("name", favs.get(arg2).comic);
		intent.putExtra("url", favs.get(arg2).url);
		intent.putExtra("site", app.getSiteConfig(favs.get(arg2).site));
		intent.setClass(mView.getContext().getApplicationContext(),
				PartsActivity.class);
		((Activity) mView.getContext()).startActivityForResult(intent,
				((MainActivity) mView.getContext()).REQUEST_TASK);
	}

	class MyAdapter extends BaseAdapter {

		private LayoutInflater inflater;

		public MyAdapter(Context context) {
			super();
			inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return favs.size();
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
			if (view == null) {
				view = inflater.inflate(R.layout.item, null);
			}
			final TextView tv = (TextView) view.findViewById(R.id.ItemTitle);
			final TextView tv2 = (TextView) view.findViewById(R.id.update);
			final TextView tv3 = (TextView) view.findViewById(R.id.site);
			final TextView tv4 = (TextView) view.findViewById(R.id.upnum);

			tv.setText(favs.get(position).comic);
			tv2.setText(favs.get(position).supdate);
			tv3.setText(favs.get(position).site);
			if (favs.get(position).unreadnum != 0) {
				tv4.setText(String.valueOf(favs.get(position).unreadnum));
			} else {
				tv4.setText("");
			}
			return view;
		}

		@Override
		public void notifyDataSetChanged() {
			SQLiteDatabase reader = new Db(mView.getContext())
					.getReadableDatabase();
			favs = Fav.get_fav_list(reader);
			reader.close();
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
				Fav fav = favs.get(selectedPosition);
				String title = new String(fav.comic);
				menu.setHeaderTitle(title);
				MenuItem m;
				m = menu.add(0, MainActivity.FAV_READED, 0, R.string.setreaded);
				m = menu.add(0, MainActivity.AUTO_DOWNLOAD, 0,
						R.string.autodownload);
				m.setCheckable(true);
				m.setChecked(fav.auto_download);
				m = menu.add(0, MainActivity.VIEW_COMIC_PAGE_FROM_FAV, 0,
						R.string.open_in_browser);
				m = menu.add(0, MainActivity.REMOVE_FAV, 0, R.string.remove_fav);
			}
		});

	}

}
