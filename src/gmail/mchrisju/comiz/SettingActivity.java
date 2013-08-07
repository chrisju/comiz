package gmail.mchrisju.comiz;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingActivity extends PreferenceActivity {
	@SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);
    }

//	@SuppressWarnings("deprecation")
//	@Override
//	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
//			Preference preference) {
//		String key = preference.getKey();
//		if(key.equals("comicupdatenotify")){
//			CheckBoxPreference pref = (CheckBoxPreference) preference;
//			boolean checked = pref.isChecked();
//			Log.e("zz", key +"="+checked);
//		}
//		else if(key.equals("allowautodownload")){
//		}
//		else if(key.equals("read_orientation")){
//		}
//		
//		return super.onPreferenceTreeClick(preferenceScreen, preference);
//	}

}
