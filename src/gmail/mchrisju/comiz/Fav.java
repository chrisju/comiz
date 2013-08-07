package gmail.mchrisju.comiz;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Fav implements Comparable<Fav> {
	// 注意第一个和最后一个不能为"",否则可能出错
	static final String format = "%s||%s||%s||%s||%d||%d||%b";
	String site;
	String comic;
	String url;
	String supdate = ""; // 最后一话名
	int readednum = 0; // 已读条数,以打开parts和自动下载更改
	int unreadnum = 0; // 未读条数
	boolean auto_download = false;

	Fav(String _site, String _comic, String _url) {
		site = _site;
		comic = _comic;
		url = _url;
	}

	@SuppressLint("DefaultLocale")
	@Override
	public String toString() {
		return String.format(format, site, comic, url, supdate, readednum,
				unreadnum, auto_download);
	}

	Fav(String s) {
		String[] ss = StringUtils.splitByWholeSeparatorPreserveAllTokens(s,"||");
		site = ss[0];
		comic = ss[1];
		url = ss[2];
		supdate = ss[3];
		try {
			readednum = Integer.parseInt(ss[4]);
		} catch (NumberFormatException e) {
			readednum = 0;
		}
		try {
			unreadnum = Integer.parseInt(ss[5]);
		} catch (NumberFormatException e) {
			unreadnum = 0;
		}
		auto_download = Boolean.parseBoolean(ss[6]);
	}

	boolean sameWith(Fav fav) {
		if (site.equals(fav.site) && comic.equals(fav.comic)) {
			return true;
		} else {
			return false;
		}
	}

	boolean sameWith(String s) {
		if (s.equals(String.format(format, site, comic, url, supdate))) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(Fav another) {
		int c1 = comic.compareTo(another.comic);
		if (c1 == 0) {
			return site.compareTo(another.site);
		} else {
			return c1;
		}
	}

	boolean add_or_update(SQLiteDatabase writer) {
		try {
			String query = String.format(
					"SELECT * FROM fav WHERE site='%s' and comic='%s'", site,
					comic);
			Cursor c = writer.rawQuery(query, null);
			if (c.getCount() > 0) { // update
				ContentValues values = new ContentValues();
				values.put("url", url);
				values.put("supdate", supdate);
				values.put("readednum", readednum);
				values.put("unreadnum", unreadnum);
				values.put("auto_download", auto_download ? 1 : 0);
				String where = String.format("site='%s' and comic='%s'", site,
						comic);
				writer.update("fav", values, where, null);
			} else { // insert
				ContentValues values = new ContentValues();
				values.put("site", site);
				values.put("comic", comic);
				values.put("url", url);
				values.put("supdate", supdate);
				values.put("readednum", readednum);
				values.put("unreadnum", unreadnum);
				int autodownload = auto_download ? 1 : 0;
				values.put("auto_download", autodownload);
				writer.insert(Db.FAV_TABLE_NAME, null, values);
			}
			c.close();
			return true;
		} catch (Exception e) {
			Log.e("zz", e.getMessage());
			return false;
		}
	}

	boolean delete(SQLiteDatabase writer) {
		try {
			String sql = String
					.format("DELETE FROM fav WHERE  site='%s' and comic='%s' and url='%s'",
							site, comic, url);
			writer.execSQL(sql);
			return true;
		} catch (Exception e) {
			Log.e("zz", e.getMessage());
			return false;
		}
	}

	static boolean deleteall(SQLiteDatabase writer) {
		try {
			String sql = "DELETE FROM fav";
			writer.execSQL(sql);
			return true;
		} catch (Exception e) {
			Log.e("zz", e.getMessage());
			return false;
		}
	}

	static ArrayList<Fav> get_fav_list(SQLiteDatabase reader) {
		ArrayList<Fav> favs = new ArrayList<Fav>();

		try {
			String query = "SELECT * FROM fav ORDER BY comic";
			Cursor c = reader.rawQuery(query, null);
			c.moveToFirst();
			for (int i = 0; i < c.getCount(); i++) {
				Fav f = new Fav(c.getString(0), c.getString(1), c.getString(2));
				f.supdate = c.getString(3);
				f.readednum = c.getInt(4);
				f.unreadnum = c.getInt(5);
				f.auto_download = c.getInt(6) == 0 ? false : true;
				// Log.e("zz", f.toString());
				favs.add(f);
				c.moveToNext();
			}
			c.close();
		} catch (Exception e) {
			Log.e("zz", e.getMessage());
		}

		return favs;
	}

	static Fav get_fav(SQLiteDatabase reader, String site, String comic) {
		Fav f = null;

		try {
			String query = String.format(
					"SELECT * FROM fav where site='%s' and comic='%s'", site,
					comic);
			Cursor c = reader.rawQuery(query, null);
			c.moveToFirst();
			if (c.getCount() > 0) {
				f = new Fav(c.getString(0), c.getString(1), c.getString(2));
				f.supdate = c.getString(3);
				f.readednum = c.getInt(4);
				f.unreadnum = c.getInt(5);
				f.auto_download = c.getInt(6) == 0 ? false : true;
			}
			c.close();
		} catch (Exception e) {
			Log.e("zz", e.getMessage());
		}

		return f;
	}

	static boolean execSQL(String sql, SQLiteDatabase writer) {
		try {
			writer.execSQL(sql);
			return true;
		} catch (Exception e) {
			Log.e("zz", e.getMessage());
			return false;
		}
	}
}
