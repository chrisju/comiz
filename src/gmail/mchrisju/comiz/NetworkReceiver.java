package gmail.mchrisju.comiz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.util.Log;

public class NetworkReceiver extends BroadcastReceiver {

	MyApp app;

	@Override
	public void onReceive(Context context, Intent intent) {
		app = (MyApp) context.getApplicationContext();
		if (app == null) {
			context.startService(new Intent(context, AlarmService.class));
			app.setNextUpdate(System.currentTimeMillis() + 15 * 1000);
			return;
		}
		if (app.configs == null) {
			app.initApp();
		}

		String action = intent.getAction();
		if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			Log.e("zz", "!!! GOT Broadcast of network changing !!!");
			if (MyUtil.networkOk(context)) {
				// 启动下载服务
				if (app != null && app.hasAliveTask()) {
					PageTransmit.startDownloadService(context);
				}

				// 过了间隔则启动更新服务
				SharedPreferences prefs = context.getSharedPreferences("alarm",
						Context.MODE_PRIVATE);
				// prefs.edit().putLong("nextcheck", 0);
				if (System.currentTimeMillis() > prefs.getLong("nextcheck", 0)) {
					context.startService(new Intent(context, AlarmService.class));
				}
			}
		}
	}

}
