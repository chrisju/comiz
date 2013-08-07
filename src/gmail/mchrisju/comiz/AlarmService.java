package gmail.mchrisju.comiz;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class AlarmService extends IntentService {

	// 检查漫画更新
	// 自动下载漫画更新

	MyApp app;
	boolean running = false;
	boolean manual = false;

	/**
	 * A constructor is required, and must call the super IntentService(String)
	 * constructor with a name for the worker thread.
	 */
	public AlarmService() {
		super("AlarmService");
		Log.e("zz", "AlarmService");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if (running) {
			if (!manual) {
				manual = intent.getBooleanExtra("manual", false);
			}
			return;
		}
		super.onStart(intent, startId);
	}

	@SuppressLint("NewApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (running) {
			if (!manual) {
				manual = intent.getBooleanExtra("manual", false);
			}
			return START_NOT_STICKY;
		}
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * The IntentService calls this method from the default worker thread with
	 * the intent that started the service. When this method returns,
	 * IntentService stops the service, as appropriate.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		running = true;
		manual = intent.getBooleanExtra("manual", false);
		app = (MyApp) getApplication();
		if (app.configs == null) {
			app.initApp();
		}
		if (!MyUtil.networkOk(this)) {
			stopSelf();
			return;
		}

		// Normally we would do some work here, like download a file.
		// For our sample, we just sleep for 5 seconds.
		synchronized (this) {
			SharedPreferences prefs = getSharedPreferences("alarm",
					MODE_PRIVATE);
			if (manual
					|| System.currentTimeMillis() > prefs.getLong("nextcheck",
							0)) {

				boolean success = checkUpdates(manual);

				if (success) {
					// 限制4小时内不复查
					prefs.edit()
							.putLong(
									"nextcheck",
									System.currentTimeMillis()
											+ AlarmManager.INTERVAL_HOUR * 4)
							.commit();
					prefs.edit().putInt("failtimes", 0).commit();
					app.setNextUpdate(System.currentTimeMillis()
							+ AlarmManager.INTERVAL_HOUR * 4 + 1000 * 10);
				} else {
					int failtimes = prefs.getInt("failtimes", 0);
					prefs.edit().putInt("failtimes", failtimes + 1).commit();
					long nexttime;
					if (failtimes == 0) {
						nexttime = System.currentTimeMillis() + 1000 * 60 * 5;
					} else {
						nexttime = System.currentTimeMillis()
								+ AlarmManager.INTERVAL_HOUR;
					}
					app.setNextUpdate(nexttime);
				}
			}
		}
		running = false;
	}

	// 更新有出错返回false
	private boolean checkUpdates(boolean tip) {
		// 检查更新
		StringBuffer failmsgs = new StringBuffer();
		SQLiteDatabase writer = new Db(this).getWritableDatabase();
		ArrayList<Fav> favs = Fav.get_fav_list(writer);
		ArrayList<Fav> upfavs = new ArrayList<Fav>();
		Log.e("zz", "start checkUpdates");
		for (Fav fav : favs) {
			String s = getString(R.string.checking, fav.comic);
			if (app.checkUpdate(fav, writer, failmsgs)) {
				upfavs.add(fav);
				s += getString(R.string.updatedto, fav.supdate);
			} else {
				if (failmsgs.length() == 0) {
					s += getString(R.string.alreadynewest);
				} else {
					s += getString(R.string.failed_pre) + failmsgs.toString();
				}
			}
			if (tip) {
				Log.e("zz", s);
				final String text = s;
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					public void run() {
						Toast.makeText(getApplicationContext(), text,
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		}
		writer.close();
		Log.e("zz", "end checkUpdates");

		// 自动下载
		for (Fav fav : favs) {
			if (fav.unreadnum > 0 && fav.auto_download
					&& MyUtil.networkOk(this)) {
				ArrayList<String> lessons = new ArrayList<String>();
				SiteConfig config = app.getSiteConfig(fav.site);

				// 获取每话信息
				File file = new File(getCacheDir(), "datafile" + "parts"
						+ SystemClock.uptimeMillis());
				DownloadTask.getLessons(fav.url, config, file, lessons);
				file.delete();

				if (lessons.size() > 0) {
					File new_dir = new File(MyUtil.getMyExternalFilesDir(this,
							null), fav.comic);
					if (!new_dir.exists()) {
						new_dir.mkdirs();
					}

					// 创建下载任务
					for (int i = 0; i < lessons.size() - fav.readednum; i++) {
						String lesson = lessons.get(i);
						String[] ss = StringUtils.splitByWholeSeparatorPreserveAllTokens(lesson,
								"||");

						// 创建某一话的目录
						new_dir = new File(MyUtil.getMyExternalFilesDir(this,
								null), fav.comic + "/" + ss[0]);
						if (!new_dir.exists()) {
							new_dir.mkdirs();
						}

						DownloadTask t = new DownloadTask(config, fav.comic,
								ss[0], ss[1], 1, new_dir);
						app.addDownloadTask(t);
						t.getPicUrls(this);
					}
					upfavs.remove(fav);

					PageTransmit.startDownloadService(this);
				}
			}
		}

		// 通知
		boolean allownotify = PreferenceManager.getDefaultSharedPreferences(
				this).getBoolean("comicupdatenotify", true);
		if (allownotify && upfavs.size() > 0) {
			String msg = "";
			if (upfavs.size() > 1) {
				ArrayList<String> ss = new ArrayList<String>();
				for (Fav f : upfavs) {
					ss.add(f.comic);
				}
				String[] sa = (String[]) ss.toArray(new String[ss.size()]);
				msg = getString(R.string.multiupdatenotify,
						MyUtil.join(sa, ","));
			} else if (upfavs.size() == 1) {
				Fav fav = upfavs.get(0);
				msg = getString(R.string.updatenotify, fav.comic, fav.supdate);
			}
			app.makeNotify(MainActivity.UPDATED_FAV, msg);
		}

		return failmsgs.length() == 0;
	}
}
