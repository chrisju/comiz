package gmail.mchrisju.comiz;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class SiteConfig implements Parcelable {
	public String name;
	public String encoding;
	public int hide; // 1:默认隐藏 0:默认显示
	public int human; // 仿人行为 0:默认行为
	public String encoding_search;

	public String luastring;
	public ArrayList<String> cookie;

	public String referer = "";

	SiteConfig(String[] config, String s) {
		name = config[0];
		encoding = config[1];
		hide = Integer.parseInt(config[2]);
		human = Integer.parseInt(config[3]);
		encoding_search = config[4];

		luastring = s;
		cookie = new ArrayList<String>();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		arg0.writeString(name);
		arg0.writeString(encoding);
		arg0.writeInt(hide);
		arg0.writeInt(human);
		arg0.writeString(encoding_search);

		arg0.writeString(luastring);
		arg0.writeStringList(cookie);
	}

	public static final Parcelable.Creator<SiteConfig> CREATOR = new Parcelable.Creator<SiteConfig>() {
		public SiteConfig createFromParcel(Parcel in) {
			return new SiteConfig(in);
		}

		public SiteConfig[] newArray(int size) {
			return new SiteConfig[size];
		}
	};

	private SiteConfig(Parcel in) {
		name = in.readString();
		encoding = in.readString();
		hide = in.readInt();
		human = in.readInt();
		encoding_search = in.readString();

		luastring = in.readString();
		cookie = new ArrayList<String>();
		in.readStringList(cookie);
	}

}
