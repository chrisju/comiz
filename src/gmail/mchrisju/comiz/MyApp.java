package gmail.mchrisju.comiz;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

public class MyApp extends Application {

	public ArrayList<SiteConfig> configs = null;
	ArrayList<DownloadTask> tasks = null;
	int notifyID = 1;

	void initApp() {
		configs = new ArrayList<SiteConfig>();
		tasks = new ArrayList<DownloadTask>();
		loadSiteConfig();

		File new_dir = MyUtil.getMyExternalFilesDir(this, null);
		if (!new_dir.exists()) {
			new_dir.mkdirs();
		}
	}

	void loadSiteConfig() {
		Log.e("zz", "start load configs...");
		try {
			String root = "site";
			String[] sitefiles = getAssets().list(root);
			Arrays.sort(sitefiles);
			for (String sitefile : sitefiles) {
				String s = MyUtil.getLuaScript2(this, root + "/" + sitefile);
				String[] config = MainActivity.luafunc2stringarray(s,
						"getconfig");
				configs.add(new SiteConfig(config, s));
			}
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("IOException", e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("Exception", e.getMessage());
		}
	}

	SiteConfig getSiteConfig(String site) {
		for (SiteConfig c : configs) {
			if (c.name.equals(site)) {
				return c;
			}
		}
		return null;
	}

	boolean checkUpdate(Fav fav, SQLiteDatabase writer, StringBuffer msg) {
		String[] lessons; // 话名||url
		boolean tobenotify = false;
		SiteConfig config = getSiteConfig(fav.site);
		Log.e("zz", "checking:" + fav.toString());
		// Log.e("zz", "site:"+fav.site+"\t"+config);
		File file = new File(getCacheDir(), "update" + fav.site + fav.comic
				+ SystemClock.uptimeMillis());

		try {
			String result = MyUtil.downloadString(fav.url, config.encoding);
			MyUtil.WriteFile(file, result);
			lessons = MainActivity.luafunc2stringarray1(config.luastring,
					"getparts", file.getPath());
			if (lessons.length > 0) {
				String last = lessons[0];
				String[] ss = StringUtils.splitByWholeSeparatorPreserveAllTokens(last, "||"); // 话名||url
				boolean tobeupdate = !fav.supdate.equals(ss[0]);
				fav.supdate = ss[0];
				Log.e("zz", "lessons.length:" + lessons.length);
				if (fav.readednum + fav.unreadnum > lessons.length) { // 异常
					fav.readednum = 0;
					fav.unreadnum = lessons.length;
				} else if (fav.readednum + fav.unreadnum < lessons.length) { // 正常
					fav.unreadnum = lessons.length - fav.readednum;
					tobenotify = true;
				}
				if (tobeupdate || tobenotify) { // 按需更新
					fav.add_or_update(writer);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			msg.append(e.toString());
		} catch (Exception e) {
			e.printStackTrace();
			msg.append(e.toString());
		}

		file.delete();
		return tobenotify;
	}

	void setNextUpdate(long time) {
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		PendingIntent pi = PendingIntent.getService(this, 0, new Intent(this,
				AlarmService.class), PendingIntent.FLAG_UPDATE_CURRENT);
		// 启动AlarmManager
		am.set(AlarmManager.RTC, time, pi);
		// am.cancel(pi);
	}

	public void addDownloadTask(DownloadTask task) {
		synchronized (tasks) {
			DownloadTask exist = null;
			for (DownloadTask t : tasks) {
				if (t.url.equals(task.url)) {
					exist = t;
					break;
				}
			}
			if (exist == null) {
				Log.e("zz", "add download task:" + task.comic + task.lesson);
				tasks.add(task);
			} else if (task.type == 0) { // 自动下载时手动下载将获得手动下载效果
				exist.receiver = task.receiver;
			}
		}
	}

	void removeDownloadTask(String site, String comic, String lesson) {
		synchronized (tasks) {
			for (DownloadTask t : tasks) {
				if (t.site.name.equals(site) && t.comic.equals(comic)
						&& t.lesson.equals(lesson)) {
					t.state = 2;
					tasks.remove(t);
					return;
				}
			}
		}
	}

	void removeAllDownloadTask() {
		synchronized (tasks) {
			for (DownloadTask t : tasks) {
				t.state = 2;
			}
			tasks.clear();
		}
	}

	boolean hasTask(SiteConfig site) {
		synchronized (tasks) {
			for (DownloadTask t : tasks) {
				if (t.site == site) {
					return true;
				}
			}
		}
		return false;
	}

	boolean hasSameComicTask(DownloadTask task) {
		synchronized (tasks) {
			for (DownloadTask t : tasks) {
				if (t.site == task.site && t.comic.equals(task.comic)) {
					return true;
				}
			}
		}
		return false;
	}

	boolean hasAliveTask() {
		synchronized (tasks) {
			for (DownloadTask t : tasks) {
				if (t.state == 0) {
					return true;
				}
			}
		}
		return false;
	}

	DownloadTask getNextTask(SiteConfig site) {
		synchronized (tasks) {
			for (DownloadTask t : tasks) {
				if (t.site == site && t.state == 0 && t.urls != null) {
					return t;
				}
			}
		}
		return null;
	}

	void setTaskState(String url, int state) {
		synchronized (tasks) {
			DownloadTask t = getDownloadTask(url);
			t.state = state;
		}
	}

	void setTaskProgress(String url, int progress) {
		synchronized (tasks) {
			DownloadTask t = getDownloadTask(url);
			t.progress = progress;
		}
	}

	DownloadTask getDownloadTask(String url) {
		for (DownloadTask t : tasks) {
			if (t.url.equals(url)) {
				return t;
			}
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	void makeNotify(int type, String msg) {
		Intent notifyIntent = new Intent(this, MainActivity.class);
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		notifyIntent.putExtra("type", type);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notifyIntent, 0);

		final int HELLO_ID = notifyID++;
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification notification = new Notification();
		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = msg;
		notification.defaults = Notification.DEFAULT_LIGHTS;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(this, getString(R.string.app_name),
				msg, contentIntent);
		notificationManager.notify(HELLO_ID, notification);
	}

	int getDefaultSharedPreferencesInt(String key, int defValue) {
		SharedPreferences pf = PreferenceManager
				.getDefaultSharedPreferences(this);
		try {
			String s = pf.getString(key, String.valueOf(defValue));
			return Integer.parseInt(s);
		} catch (Exception e) {
			Log.e("zz", "getDefaultSharedPreferencesInt: " + e.getMessage());
			return defValue;
		}
	}
	
}
