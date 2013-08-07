package gmail.mchrisju.comiz;

import gmail.mchrisju.comiz.PageTransmit.DownloadReceiver;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

public class DownloadTask {

	SiteConfig site;
	String comic;
	String lesson;
	String url; // 网址
	int type; // 0:activity下载 1:service下载
	File dir; // 文件夹
	String[] urls = null; // 图片网址
	int state = 0; // 状态 0:ok 1:pause 2:deleted
	int progress = 0; // 进度 >=0
	DownloadReceiver receiver = null;

	public DownloadTask(SiteConfig site, String comic, String lesson, String url, int type,
			File dir) {
		this.site = site;
		this.comic = comic;
		this.lesson = lesson;
		this.url = url;
		this.type = type;
		this.dir = dir;
	}

	public String[] getPicUrls(Context context) {
		ArrayList<String> urls = new ArrayList<String>();

		File cachefile = new File(context.getCacheDir(), "comizdatafile"
				+ "pics" + site.name + SystemClock.uptimeMillis());
		getPicUrls(url, cachefile);
		cachefile.delete();

		return (String[]) urls.toArray(new String[urls.size()]);
	}

	String[] getPicUrls(String url, File cachefile) {
		try {
			// if two step, first will set referer
			if (site.referer.equals("")) {
				site.referer = url;
			}
			site.cookie = MyUtil.downloadPageAndCookie(url, site.encoding);
			String result = site.cookie.get(0);
			site.cookie.remove(0);

			MyUtil.WriteFile(cachefile, result);

//			 Log.e("zz", "lua:\n" + site.luastring);
			urls = MainActivity.luafunc2stringarray1(site.luastring, "getpics",
					cachefile.getPath());
			 Log.e("zz", "DownloadPicPage:\n"+urls[0]);
			if (urls.length == 1) {
				// 需要再下载picpage并分析 getpics函数需要在匹配不到图片时匹配到一个下载地址
				String[] ss = StringUtils.splitByWholeSeparatorPreserveAllTokens(url, "//");
				String[] ss2 = StringUtils.splitByWholeSeparatorPreserveAllTokens(ss[1], "/");
				Log.e("zz", ss[0] + "//" + ss2[0] + urls[0]);
				url = ss[0] + "//" + ss2[0] + urls[0];
				getPicUrls(url, cachefile);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return urls;
	}

	public static String getLessons(String url, SiteConfig site, File cachefile,
			ArrayList<String> lessons) {
		String err = "";
		try {
			String result = MyUtil.downloadString(url, site.encoding);
			MyUtil.WriteFile(cachefile, result);
			String[] results = MainActivity.luafunc2stringarray1(
					site.luastring, "getparts", cachefile.getPath());
			lessons.addAll(Arrays.asList(results));
		} catch (Exception e) {
			err = e.getMessage();
			e.printStackTrace();
		}
		return err;
	}

}
