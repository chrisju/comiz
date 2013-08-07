package gmail.mchrisju.comiz;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

public class DownloadService extends Service {

	public static final int UPDATE_PROGRESS = 8344;

	MyApp app;
	private ArrayList<MyThread> mHandlers = null;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		mHandlers = new ArrayList<MyThread>();
		app = (MyApp) getApplication();
		if (app.configs == null) {
			stopSelf();
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		handleCommand(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("zz", "onStartCommand:" + intent);
		handleCommand(intent);
		// not restart
		return START_NOT_STICKY;// START_REDELIVER_INTENT
	}

	void handleCommand(Intent intent) {
		// Toast.makeText(this, "onStartCommand", Toast.LENGTH_SHORT).show();
		Log.e("zz", "handleCommand:" + intent);

		if (!app.hasAliveTask()) {
			stopSelf();
			return;
		}

		synchronized (app.tasks) {
			for (DownloadTask t : app.tasks) {
				MyThread h = getThreadBySite(t.site);
				if (h == null) {
					h = createThread(t.site);
					mHandlers.add(h);
				}
			}
		}

	}

	private class MyThread extends Thread {
		public SiteConfig site;

		MyThread(SiteConfig site) {
			this.site = site;
		}

		@Override
		public void run() {
			while (app.hasTask(site)) {
				DownloadTask t = app.getNextTask(site);
				// Log.e("zz", "t=" + t);
				if (t == null || !netOK(t.type)) {
					if (!app.hasAliveTask()) {
						stopSelf();
						return;
					}
					break;
				}

				// Log.e("zz", "MyThread:" + t.url);
				while (t.state == 0 && t.progress < t.urls.length
						&& netOK(t.type)) {
					// while (t.state == 0) {
					String picurl = t.urls[t.progress];
					String name = String.format("%03d", t.progress)+MyUtil.getExt(picurl, "jpg");
					File afile = new File(t.dir, name);
					// Log.e("zz", "MyThread:downloading" + picurl + " to "
					// + afile.getPath());
					try {
						MyUtil.downloadFile(picurl, afile, site.referer,
								site.cookie);
					} catch (IOException e) {
						Log.e("zz", "MyThread.download "+picurl+" fail: " + e.toString());
						e.printStackTrace();
						if (!netOK(t.type)) {
							t.progress -= 1;
						}
					}
					t.progress += 1;

					if (t.receiver != null) {
						Bundle resultData = new Bundle();
						resultData.putString("op", "progress");
						resultData.putString("comic", t.comic);
						resultData.putString("lesson", t.lesson);
						t.receiver.send(UPDATE_PROGRESS, resultData);
					}

					if (t.progress % 20 == 19) {
						SystemClock.sleep(896);
					}
					if (site.human != 0) {
						SystemClock.sleep((int) (Math.random() * 1000) + 500);
					}

				}

				if (t.progress == t.urls.length) {
					// Log.e("zz", t.comic + t.lesson + " download finished");
					app.removeDownloadTask(t.site.name, t.comic, t.lesson);

					if (t.receiver != null) {
						Bundle resultData = new Bundle();
						resultData.putString("op", "finished");
						resultData.putString("comic", t.comic);
						resultData.putString("lesson", t.lesson);
						t.receiver.send(UPDATE_PROGRESS, resultData);
					}

					if (t.type == 1) {// 自动下载完成的要通知并更新数据库
						boolean allownotify = PreferenceManager
								.getDefaultSharedPreferences(
										DownloadService.this).getBoolean(
										"comicupdatenotify", true);
						if (allownotify) {
							// 同时有UPDATED_LOCAL和UPDATED_FAV会是UPDATED_FAV..可能是前一个为主吧
							app.makeNotify(
									MainActivity.UPDATED_LOCAL,
									getString(R.string.autodownloadfinish,
											t.comic, t.lesson));
						}

						if (!app.hasSameComicTask(t)) {
							SQLiteDatabase writer = new Db(DownloadService.this)
									.getWritableDatabase();
							Fav f = Fav.get_fav(writer, t.site.name, t.comic);
							ContentValues values = new ContentValues();
							values.put("readednum", f.readednum + f.unreadnum);
							values.put("unreadnum", 0);
							String where = String.format(
									"site='%s' and comic='%s'", t.site.name,
									t.comic);
							writer.update("fav", values, where, null);
							writer.close();
						}
					}

				}

				// 每话下完sleep约1秒
				SystemClock.sleep(896);
			}

			// 线程结束时从列表中移除
			Log.e("zz", "removeThread:" + site.name + site);
			mHandlers.remove(this);
			if (mHandlers.size() == 0) {
				stopSelf();
			}
		}
	}

	private MyThread getThreadBySite(SiteConfig site) {
		for (MyThread h : mHandlers) {
			if (site == h.site) {
				return h;
			}
		}
		return null;
	}

	private MyThread createThread(SiteConfig site) {
		MyThread thread = new MyThread(site);
		Log.e("zz", "createThread:" + site.name + site);
		thread.start();
		return thread;
	}

	boolean netOK(int type) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (type == 0) {
			if (prefs.getBoolean("onlywifi", false)) {
				return MyUtil.wifiOk(this);
			} else {
				return MyUtil.networkOk(this);
			}
		} else if (type == 1) {
			if (prefs.getBoolean("onlywifi_auto", false)) {
				return MyUtil.wifiOk(this);
			} else {
				return MyUtil.networkOk(this);
			}
		}
		return false;
	}

}
