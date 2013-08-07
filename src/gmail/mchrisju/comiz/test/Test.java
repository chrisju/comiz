package gmail.mchrisju.comiz.test;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import gmail.mchrisju.comiz.DownloadService;
import gmail.mchrisju.comiz.DownloadTask;
import gmail.mchrisju.comiz.MainActivity;
import gmail.mchrisju.comiz.MyApp;
import gmail.mchrisju.comiz.MyUtil;
import gmail.mchrisju.comiz.R;
import gmail.mchrisju.comiz.SiteConfig;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class Test {

	Context context;
	MyApp app;

	String keyword;

	public Test(Context context) {
		this.context = context;
		app = (MyApp) context.getApplicationContext();
	}

	public void setTest1() {
		keyword = "爸";
	}

	public void run() {
		setTest1();
		// search
		for (SiteConfig site : app.configs) {
			new SearchThread(site, keyword).start();
		}

	}

	class SearchThread extends Thread {
		SiteConfig site;
		String keyword;

		SearchThread(SiteConfig site, String keyword) {
			this.site = site;
			this.keyword = keyword;
		}

		@Override
		public void run() {
			try {
				byte[] keyword;
				if (site.encoding_search.equalsIgnoreCase("unicode-escape")) {
					keyword = MyUtil.escapeUnicode(this.keyword).getBytes();
				} else {
					keyword = this.keyword.getBytes(site.encoding_search);
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
				String[] comics;
				File file = new File(context.getCacheDir(), "datafile"
						+ "search" + site.name + SystemClock.uptimeMillis());
				MyUtil.WriteFile(file, result);
				comics = MainActivity.luafunc2stringarray1(site.luastring,
						"getcomics", file.getPath());
				file.delete();

				// 搜索结果
				for (String comic : comics) {
					Log.e("zz", site.name + ":" + comic);
				}

				if (comics.length > 0) {
					String[] p = StringUtils
							.splitByWholeSeparatorPreserveAllTokens(comics[0],
									"||");
					new GetPartThread(site, p[3], p[0]).start();
				}

			} catch (Exception e) {
				String err = context.getString(R.string.failed_pre)
						+ e.getMessage();
				Log.e("zz", err);
			}
		}
	}

	class GetPartThread extends Thread {
		SiteConfig site;
		String comic;
		String name;

		GetPartThread(SiteConfig site, String comic,String name) {
			this.site = site;
			this.comic = comic;
			this.name = name;
		}

		@Override
		public void run() {
			ArrayList<String> lessons;
			lessons = new ArrayList<String>();
			String err = "";
			File file = new File(context.getCacheDir(), "datafile" + "parts"
					+ SystemClock.uptimeMillis());
			err = DownloadTask.getLessons(comic, site, file, lessons);
			if(!err.isEmpty()) Log.e("zz", err);
			file.delete();

			// 每话
			String[] mResult = lessons.toArray(new String[lessons.size()]);
			for (String s : mResult) {
				Log.e("zz", site.name + ":" + s);
			}

			if (mResult.length > 0) {
				File new_dir = new File(MyUtil.getMyExternalFilesDir(
						context, null), name);
				if (!new_dir.exists()) {
					new_dir.mkdirs();
				}
				String[] ss = StringUtils.splitByWholeSeparatorPreserveAllTokens(mResult[0], "||");
				if(ss[0].isEmpty()){
					ss[0] = Long.toHexString(System.currentTimeMillis());
				}
				new_dir = new File(MyUtil.getMyExternalFilesDir(context,
						null), name + "/" + ss[0]);
				if (!new_dir.exists()) {
					new_dir.mkdirs();
				}
				DownloadTask t = new DownloadTask(site, comic, ss[0], ss[1], 0,
						new_dir);
				app.addDownloadTask(t);
				
				// 图片下载地址
				t.getPicUrls(context);
				Intent intent = new Intent(context, DownloadService.class);
				context.startService(intent);
			}
		}
	}


}
