package gmail.mchrisju.comiz;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Db extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "comiz";
	private static final int DATABASE_VERSION = 1;
	static final String FAV_TABLE_NAME = "fav";

	Db(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String DICTIONARY_TABLE_CREATE = String.format(
				"CREATE TABLE %s  (" +
				"%s   TEXT,  " +
				"%s   TEXT,  " +
				"%s   TEXT,  " +
				"%s   TEXT,  " +
				"%s   INTEGER,  " +
				"%s   INTEGER,  " +
				"%s   INTEGER );",
				FAV_TABLE_NAME,
				"site",
				"comic",
				"url",
				"supdate",
				"readednum",
				"unreadnum",
				"auto_download");
		db.execSQL(DICTIONARY_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch (newVersion) {
		case 1:
			db.execSQL("DROP TABLE IF EXISTS " + FAV_TABLE_NAME);
			onCreate(db);
			break;

		default:
			break;
		}
	}
}
