package gmail.mchrisju.comiz;

import gmail.mchrisju.comiz.test.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public enum PageIndex {
		PAGE_FAV, PAGE_SEARCH, PAGE_NAVIGATE, PAGE_LOCAL, PAGE_TRANSMIT
	};

	static final int SETTING = 0x0100;
	static final int ADD_TO_FAV = 0x0101;
	static final int REMOVE_FAV = 0x0102;
	static final int DELETE_ALL = 0x0103;
	static final int REMOVE_LOCAL = 0x0111;
	static final int VIEW_COMIC_PAGE = 0x0121;
	static final int VIEW_LESSON_PAGE = 0x0122;
	static final int VIEW_COMIC_PAGE_FROM_FAV = 0x0123;
	static final int FAV_READED = 0x0124;
	static final int AUTO_DOWNLOAD = 0x0125;
	static final int FAV_ALLREADED = 0x0126;
	static final int FAV_CHECKUPDATE = 0x0127;
	static final int CANCEL_DOWNLOAD = 0x0131;
	static final int UPDATED_FAV = 0x01;
	static final int UPDATED_LOCAL = 0x02;

	public final int REQUEST_TASK = 0;

	MyApp app;
	private ViewPager myViewPager;
	MyViewPager mMyTab;
	Bitmap mLongsepbm;
	Bitmap mSepbm;
	private int mPageIndex = 0; // 当前标签页

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Log.e("zz", "MainActivity onCreate");
		app = (MyApp) getApplication();
		if (app.configs == null) {
			app.initApp();
		}

		if (!MyUtil.sdcardOk()) {
			Toast.makeText(this, R.string.invalid_sdcard, Toast.LENGTH_LONG)
					.show();
			finish();
			return;
		}

		initSolidUI();

		myViewPager = (ViewPager) findViewById(R.id.viewpagerLayout);
		mMyTab = new MyViewPager(this, myViewPager);

		restoreErewhileUI();

		int startpage = app.getDefaultSharedPreferencesInt("startpageindex", 1);
		setCurrentPage(startpage);

		onNewIntent(getIntent());

		app.setNextUpdate(System.currentTimeMillis() + 1000);
		
//		new Test(this).run();
	}

	// 恢复刚刚的UI, 防止不小心的关闭
	private void restoreErewhileUI() {
		PageSearch p = (PageSearch) (mMyTab.mPages[1]);
		File f = new File(getCacheDir(), "comiz_tempsave");// +System.currentTimeMillis());
		FileInputStream is;
		try {
			is = new FileInputStream(f);
			BufferedReader br = new BufferedReader(new InputStreamReader(is,
					"utf-8"));
			String t = br.readLine();
			if (t != null) {
				long old = Long.parseLong(t); // 第一行时间
				long now = System.currentTimeMillis();
				if (now - old < 1000 * 60) { // 60秒内则恢复
					t = br.readLine(); // 第二行滚动位置
					int pos = Integer.parseInt(t);
					while ((t = br.readLine()) != null) {
						p.adapter.arr.add(new PageSearch.itemData(t));
					}
					p.adapter.notifyDataSetChanged();
					p.mList.setSelection(pos);
				}
			}
			is.close();
			f.delete();
		} catch (FileNotFoundException e) {
		} catch (UnsupportedEncodingException e) {
			Log.e("zz", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("zz", e.getMessage());
			e.printStackTrace();
		}
	}

	void initSolidUI() {
		initBitmaps();

		ImageView longsep = (ImageView) findViewById(R.id.longsep);
		ImageView longsep2 = (ImageView) findViewById(R.id.longsep_a);
		longsep.setImageBitmap(mLongsepbm);
		longsep2.setImageBitmap(mLongsepbm);

		drawSep(mPageIndex);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_TASK) {
			if (resultCode == RESULT_OK) {
				String[] task = data.getStringArrayExtra("task");
				String name = data.getStringExtra("name");
				String site = data.getStringExtra("site");
				PageTransmit p = (PageTransmit) this.mMyTab.mPages[4];
				// 哪个网站哪个漫画哪一话(话名||地址)
				Log.e("zz", "p.AddTask(site, name, task)");
				p.AddTask(site, name, task);
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		int type = intent.getIntExtra("type", 0);
		if ((type & UPDATED_FAV) != 0) {
			PageFav pf = (PageFav) (mMyTab.mPages[0]);
			pf.adapter.notifyDataSetChanged();
			setCurrentPage(0);
		}
		if ((type & UPDATED_LOCAL) != 0) {
			PageFav pf = (PageFav) (mMyTab.mPages[0]);
			pf.adapter.notifyDataSetChanged();
			PageLocal pl = (PageLocal) (mMyTab.mPages[3]);
			pl.initAdapter();
			setCurrentPage(3);
		}
		super.onNewIntent(intent);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (mPageIndex == 0) {
			menu.add(0, MainActivity.FAV_CHECKUPDATE, 0, R.string.checkupdate);
			menu.add(0, MainActivity.FAV_ALLREADED, 0, R.string.setallreaded);
		}
		if (mPageIndex == 0 || mPageIndex == 3 || mPageIndex == 4) {
			menu.add(0, MainActivity.DELETE_ALL, 0, R.string.deleteall);
		}
		menu.add(0, MainActivity.SETTING, 0, R.string.menu_settings);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case SETTING: {
			Intent intent = new Intent();
			intent.setClass(this, SettingActivity.class);
			startActivity(intent);
		}
			break;
		case FAV_CHECKUPDATE: {
			Intent intent = new Intent();
			intent.putExtra("manual", true);
			intent.setClass(this, AlarmService.class);
			startService(intent);
		}
			break;
		case FAV_ALLREADED: {
			PageFav pf = (PageFav) (mMyTab.mPages[0]);
			SQLiteDatabase writer = new Db(this).getWritableDatabase();
			for (Fav m : pf.favs) {
				if (m.unreadnum != 0) {
					m.readednum += m.unreadnum;
					m.unreadnum = 0;
					m.add_or_update(writer);
				}
			}
			writer.close();
			pf.adapter.notifyDataSetChanged();
		}
			break;
		case DELETE_ALL: {
			ConfirmDialog dlg = new ConfirmDialog(this);
			dlg.show();
			if (dlg.ret == DialogInterface.BUTTON_POSITIVE) {
				switch (mPageIndex) {
				case 0: // DELETE_ALL_FAV
					SQLiteDatabase writer = new Db(this).getWritableDatabase();
					Fav.deleteall(writer);
					writer.close();
					PageFav pf = (PageFav) (mMyTab.mPages[0]);
					pf.adapter.notifyDataSetChanged();
					break;
				case 3: // DELETE_ALL_LOCAL
					PageLocal pl = (PageLocal) (mMyTab.mPages[3]);
					for (String s : pl.adapter.arr) {
						String[] ss = StringUtils.splitByWholeSeparatorPreserveAllTokens(s, PageTransmit.sep); // {name,lesson}
						MyUtil.DeleteLesson(this, ss[0], ss[1]);
					}
					pl.adapter.arr.clear();
					pl.adapter.notifyDataSetChanged();
					break;
				case 4: // DELETE_ALL_DOWNLOAD
					app.removeAllDownloadTask();
					PageTransmit pt = (PageTransmit) (mMyTab.mPages[4]);
					pt.adapter.notifyDataSetChanged();
					break;
				}
			}
		}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int pos = ((AdapterContextMenuInfo) item.getMenuInfo()).position;
		switch (item.getItemId()) {
		case ADD_TO_FAV: {
			PageSearch p = (PageSearch) (mMyTab.mPages[1]);
			PageSearch.itemData m = p.adapter.arr.get(pos);
			Fav fav = new Fav(m.site, m.name, m.url);
			SQLiteDatabase writer = new Db(this).getWritableDatabase();
			fav.add_or_update(writer);
			// app.checkUpdate(fav, writer); // 同步会卡UI
			writer.close();
			PageFav pf = (PageFav) (mMyTab.mPages[0]);
			pf.adapter.notifyDataSetChanged();
		}
			break;
		case REMOVE_FAV: {
			PageFav pf = (PageFav) (mMyTab.mPages[0]);
			SQLiteDatabase writer = new Db(this).getWritableDatabase();
			pf.favs.get(pos).delete(writer);
			writer.close();
			pf.adapter.notifyDataSetChanged();
		}
			break;
		case AUTO_DOWNLOAD: {
			PageFav pf = (PageFav) (mMyTab.mPages[0]);
			Fav m = pf.favs.get(pos);
			if (item.isChecked()) {
				item.setChecked(false);
				String msg = getString(R.string.notautodownloadtip, m.comic);
				Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
			} else {
				item.setChecked(true);
				String msg = getString(R.string.autodownloadtip, m.comic);
				// 如没打开自动下载 增加提示

				if (!PreferenceManager.getDefaultSharedPreferences(this)
						.getBoolean("allowautodownload", false)) {
					msg += getString(R.string.setautodownloadtip);
				}
				Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
			}
			m.auto_download = item.isChecked();
			SQLiteDatabase writer = new Db(this).getWritableDatabase();
			m.add_or_update(writer);
			writer.close();
			pf.adapter.notifyDataSetChanged();
		}
			break;
		case FAV_READED: {
			PageFav pf = (PageFav) (mMyTab.mPages[0]);
			Fav m = pf.favs.get(pos);
			if (m.unreadnum != 0) {
				m.readednum += m.unreadnum;
				m.unreadnum = 0;
				SQLiteDatabase writer = new Db(this).getWritableDatabase();
				m.add_or_update(writer);
				writer.close();
				pf.adapter.notifyDataSetChanged();
			}
		}
			break;
		case VIEW_COMIC_PAGE_FROM_FAV: {
			PageFav pf = (PageFav) (mMyTab.mPages[0]);
			Fav m = pf.favs.get(pos);
			Log.e("zz", m.url);
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(m.url));
			startActivity(intent);
		}
			break;
		case REMOVE_LOCAL: {
			PageLocal p = (PageLocal) (mMyTab.mPages[3]);
			String text = p.adapter.arr.get(pos);
			String[] ss = StringUtils.splitByWholeSeparatorPreserveAllTokens(text, PageTransmit.sep); // {name,lesson}
			MyUtil.DeleteLesson(this, ss[0], ss[1]);
			p.adapter.arr.remove(pos);
			p.adapter.notifyDataSetChanged();
		}
			break;
		case VIEW_COMIC_PAGE: {
			PageSearch p = (PageSearch) (mMyTab.mPages[1]);
			PageSearch.itemData m = p.adapter.arr.get(pos);
			Log.e("zz", m.url);
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(m.url));
			startActivity(intent);
		}
			break;
		case CANCEL_DOWNLOAD: {
			PageTransmit p = (PageTransmit) (mMyTab.mPages[4]);
			DownloadTask t = p.tasks.get(pos);
			app.removeDownloadTask(t.site.name, t.comic, t.lesson);
			// MyUtil.DeleteLesson(this, t.comic, t.lesson);
			p.adapter.notifyDataSetChanged();
		}
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		initSolidUI();
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onResume() {
		Log.e("zz", "MainActivity onResume");
		PageFav pf = (PageFav) (mMyTab.mPages[0]);
		pf.adapter.notifyDataSetChanged();

		super.onResume();
	}

	@Override
	protected void onStop() {
		Log.e("zz", "MainActivity onStop");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.e("zz", "MainActivity onDestroy");
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		MyApp app = (MyApp) getApplication();
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (app.tasks.size() > 0) {
				Intent it = new Intent(Intent.ACTION_MAIN);
				it.addCategory(Intent.CATEGORY_HOME);
				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(it);
				return true; // 下载时不退出
			}
			break;
		}

		// 保存搜索状态以供恢复
		PageSearch p = (PageSearch) (mMyTab.mPages[1]);
		if (p.adapter.arr.size() > 0) {
			File f = new File(getCacheDir(), "comiz_tempsave");// +System.currentTimeMillis());
			FileWriter fw;
			try {
				fw = new FileWriter(f);
				fw.write(System.currentTimeMillis() + "\n"); // 第一行写入时间
				fw.write(p.mList.getFirstVisiblePosition() + "\n"); // 第二行写入滚动位置
				for (PageSearch.itemData m : p.adapter.arr) {
					fw.write(m.toString() + "\n");
				}
				fw.close();
			} catch (IOException e) {
				Log.e("zz", e.getMessage());
				e.printStackTrace();
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	public void setCurrentPage(int index) {
		mMyTab.mTab.setCurrentItem(index);
		drawSep(index);
	}

	@SuppressWarnings("deprecation")
	private void initBitmaps() {
		// Point pt = new Point();
		// int w = getWindowManager().getDefaultDisplay().getSize(pt); //2.2不支持
		int w = getWindowManager().getDefaultDisplay().getWidth();
		Log.e("zz", String.valueOf(w));

		mLongsepbm = Bitmap.createBitmap(w, 2, Bitmap.Config.ARGB_8888);
		DoGraphic.drawSeperator(mLongsepbm);

		mSepbm = Bitmap.createBitmap(w / 5, 2, Bitmap.Config.ARGB_8888);
		DoGraphic.drawSeperator(mSepbm);
	}

	public void drawSep(int pos) {
		ImageView[] vs = new ImageView[5];
		vs[0] = (ImageView) findViewById(R.id.sep1);
		vs[1] = (ImageView) findViewById(R.id.sep2);
		vs[2] = (ImageView) findViewById(R.id.sep3);
		vs[3] = (ImageView) findViewById(R.id.sep4);
		vs[4] = (ImageView) findViewById(R.id.sep5);
		for (int i = 0; i < 5; i++) {
			vs[i].setImageBitmap(null);
		}
		vs[pos].setImageBitmap(mSepbm);

		mPageIndex = pos;
	}

	public void myClickHandler(View view) {
		PageSearch page = (PageSearch) (mMyTab.mPages[1]);
		Log.e("zz", "page:" + page.toString());
		page.onClickSearch();
	}

	public void onPageFav(View view) {
		setCurrentPage(0);
	}

	public void onPageSearch(View view) {
		setCurrentPage(1);
	}

	public void onPageNavigate(View view) {
		setCurrentPage(2);
	}

	public void onPageLocal(View view) {
		setCurrentPage(3);
	}

	public void onPageTransmit(View view) {
		setCurrentPage(4);
	}

	int getUpdatePos(String supdate, String[] ss) {
		for (int i = 0; i < ss.length; i++) {
			String s = ss[i];
			String[] ass = StringUtils.splitByWholeSeparatorPreserveAllTokens(s, "||"); // 话名||url
			if (supdate.equals(ass[0])) {
				return i;
			}
		}
		return -1;
	}

	// return DialogInterface.BUTTON_POSITIVE,DialogInterface.BUTTON_NEGATIVE
	private class ConfirmDialog extends AlertDialog.Builder {
		int ret = 0;

		public ConfirmDialog(Context context) {
			super(context);

			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					ret = which;
				}
			};

			this.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.app_name)
					.setMessage(R.string.deleteallconfirm)
					.setPositiveButton(android.R.string.ok, listener)
					.setNegativeButton(android.R.string.cancel, listener);
		}

	}

	// jni
	static {
		System.loadLibrary("luandroid");
	}

	public static native String[] luafunc2stringarray(String luastring,
			String luafunc);

	public static native String[] luafunc2stringarray1(String luastring,
			String luafunc, String strparam1);

	public static native String[] luafunc2stringarray1b(String luastring,
			String luafunc, byte[] b);

	public static native byte[] jnidecode(byte[] b);

}
