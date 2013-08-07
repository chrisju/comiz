package gmail.mchrisju.comiz;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class MyUtil {
	static final String sep = "||";

	public static File getMyExternalFilesDir(Context context, String relativePath) {
		File root = Environment.getExternalStorageDirectory();
		String rdir = context.getString(R.string.app_dir);
		if (relativePath != null) {
			rdir += "/" + relativePath;
		}
		File d = new File(root, rdir);
		return d;
	}

	static void DeleteLesson(Context context, String comic, String lesson) {
		File d = getMyExternalFilesDir(context, comic + "/" + lesson);
		MyUtil.deleteFolder(d);
		d = MyUtil.getMyExternalFilesDir(context, comic);
		d.delete(); // 删除空的漫画文件夹

	}

	// 将value限制在one和another之间
	public static float limitedValue(float value, float one, float another) {
		if (one <= another) {
			if (value < one)
				value = one;
			if (value > another)
				value = another;
		} else {
			if (value > one)
				value = one;
			if (value < another)
				value = another;
		}
		return value;
	}

	public static boolean inValueSection(float value, float one, float another) {
		if (one <= another) {
			if (one <= value && value <= another)
				return true;
			else
				return false;
		} else {
			if (one >= value && value >= another)
				return true;
			else
				return false;
		}
	}

	public static boolean deleteFolder(File f) {
		if (f.isFile()) {
			return f.delete();
		} else if (f.isDirectory()) {
			for (File g : f.listFiles()) {
				if (g.isFile()) {
					g.delete();
				} else if (g.isDirectory()) {
					deleteFolder(g);
				}
			}
			return f.delete();
		}
		return false;
	}

	public static String getLuaScript(Context ctx, String localpath)
			throws IOException {
		InputStream inputStream = ctx.getAssets().open("common.lua");
		InputStream inputStream2 = ctx.getAssets().open(localpath);

		StringBuffer sb = new StringBuffer("");
		char[] buf = new char[0x1FFF];// 8K
		int n;

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream, "utf-8"));
		while ((n = reader.read(buf)) > 0) {
			sb.append(buf, 0, n);
		}

		BufferedReader reader2 = new BufferedReader(new InputStreamReader(
				inputStream2, "utf-8"));
		while ((n = reader2.read(buf)) > 0) {
			sb.append(buf, 0, n);
		}

		return sb.toString();
	}

	public static byte[] readBytes(InputStream fin) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(fin);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int c;
		while ((c = bis.read()) != -1) {
			baos.write(c);
		}
		bis.close();
		return baos.toByteArray();
	}

	public static String getLuaScript2(Context ctx, String localpath)
			throws IOException {
		InputStream inputStream = ctx.getAssets().open("common.lua");
		InputStream inputStream2 = ctx.getAssets().open(localpath);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(readBytes(inputStream));
		baos.write(readBytes(inputStream2));
		inputStream.close();
		inputStream2.close();
		String s = new String(baos.toByteArray());
		return s;
	}

	public static String getAssetsFile(Context ctx, String localpath)
			throws IOException {
		InputStream inputStream = ctx.getAssets().open(localpath);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream, "utf-8"));

		StringBuffer sb = new StringBuffer("");
		char[] buf = new char[0xFFF];// 4K
		int n;
		while ((n = reader.read(buf)) > 0) {
			sb.append(buf, 0, n);
		}

		return sb.toString();
	}

	public static String getString(InputStream inputStream) {
		InputStreamReader inputStreamReader = null;
		try {
			inputStreamReader = new InputStreamReader(inputStream, "utf-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		BufferedReader reader = new BufferedReader(inputStreamReader);
		StringBuffer sb = new StringBuffer("");
		char[] buf = new char[0xFFF];// 4K
		int n;
		try {
			while ((n = reader.read(buf)) > 0) {
				sb.append(buf, 0, n);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public static boolean networkOk(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public static boolean wifiOk(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			String typeName = networkInfo.getTypeName().toLowerCase(); // WIFI/MOBILE
			if (typeName.equals("wifi")) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unused")
	public static boolean sdcardOk() {
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}

		return mExternalStorageWriteable;
	}

	public static void HideSoftInput(View v) {
		InputMethodManager inputMethodManager = (InputMethodManager) v
				.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	private static final char[] hexChar = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static String escapeUnicode(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if ((c >> 7) > 0) {
				sb.append("\\u");
				sb.append(hexChar[(c >> 12) & 0xF]); // append the hex character
														// for the left-most
														// 4-bits
				sb.append(hexChar[(c >> 8) & 0xF]); // hex for the second group
													// of 4-bits from the left
				sb.append(hexChar[(c >> 4) & 0xF]); // hex for the third group
				sb.append(hexChar[c & 0xF]); // hex for the last group, e.g.,
												// the right most 4-bits
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static String unescapeUnicode(final String dataStr) {
		int start = 0;
		int end = dataStr.length();
		final StringBuffer buffer = new StringBuffer();
		while (start < end) {
			if (dataStr.startsWith("\\u", start)) {
				String uni = dataStr.substring(start + 2, start + 6);
				char letter = (char) Integer.parseInt(uni, 16);
				buffer.append(new Character(letter).toString());
				start += 6;
			} else {
				buffer.append(dataStr.charAt(start));
				start++;
			}
		}

		return buffer.toString();
	}

	public static void WriteFile(File file, String s) throws IOException {
		BufferedWriter output = new BufferedWriter(new FileWriter(file));
		output.write(s);
		output.close();
	}

	public static byte[] gbk2byte(String chenese) {
		char c[] = chenese.toCharArray();
		byte[] fullByte = new byte[3 * c.length];
		for (int i = 0; i < c.length; i++) {
			int m = (int) c[i];
			String word = Integer.toBinaryString(m);

			StringBuffer sb = new StringBuffer();
			int len = 16 - word.length();
			for (int j = 0; j < len; j++) {
				sb.append("0");
			}
			sb.append(word);
			sb.insert(0, "1110");
			sb.insert(8, "10");
			sb.insert(16, "10");

			String s1 = sb.substring(0, 8);
			String s2 = sb.substring(8, 16);
			String s3 = sb.substring(16);

			byte b0 = Integer.valueOf(s1, 2).byteValue();
			byte b1 = Integer.valueOf(s2, 2).byteValue();
			byte b2 = Integer.valueOf(s3, 2).byteValue();
			byte[] bf = new byte[3];
			bf[0] = b0;
			fullByte[i * 3] = bf[0];
			bf[1] = b1;
			fullByte[i * 3 + 1] = bf[1];
			bf[2] = b2;
			fullByte[i * 3 + 2] = bf[2];

		}
		return fullByte;
	}

	public static String doPost2(String surl, List<NameValuePair> param) {
		String result = "";
		HttpParams httpParams = new BasicHttpParams();
		// 设置连接超时和 Socket 超时，以及 Socket 缓存大小

		HttpConnectionParams.setConnectionTimeout(httpParams, 20 * 1000);

		HttpConnectionParams.setSoTimeout(httpParams, 20 * 1000);

		HttpConnectionParams.setSocketBufferSize(httpParams, 8192);

		// 设置重定向，缺省为 true

		HttpClientParams.setRedirecting(httpParams, true);

		// 设置 user agent

		String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2) Gecko/20100115 Firefox/3.6";
		HttpProtocolParams.setUserAgent(httpParams, userAgent);

		// 创建一个 HttpClient 实例

		// 注意 HttpClient httpClient = new HttpClient(); 是Commons HttpClient

		// 中的用法，在 Android 1.5 中我们需要使用 Apache 的缺省实现 DefaultHttpClient

		HttpClient httpClient = new DefaultHttpClient(httpParams);
		/* 建立HTTPPost对象 */
		HttpPost httpRequest = new HttpPost(surl);

		try {
			/* 添加请求参数到请求对象 */
			httpRequest.setEntity(new UrlEncodedFormEntity(param, "gbk"));
			/* 发送请求并等待响应 */
			HttpResponse httpResponse = httpClient.execute(httpRequest);
			/* 若状态码为200 ok */
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				/* 读返回数据 */
				result = EntityUtils.toString(httpResponse.getEntity(), "gbk");

			} else {
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static String doPost(String surl, String param, String encoding) {
		URL url;
		String result = "";
		try {
			url = new URL(surl);
			URLConnection connection = url.openConnection();
			connection
					.setRequestProperty(
							"User-Agent",
							"Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.81 Safari/537.1");
			connection.setDoOutput(true);
			OutputStreamWriter out = new OutputStreamWriter(
					connection.getOutputStream(), "8859_1");
			out.write(param);
			// remember to clean up
			out.flush();
			out.close();

			StringBuffer sb = new StringBuffer();
			InputStream is = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is,
					encoding));
			String data = "";
			while ((data = br.readLine()) != null) {
				sb.append(data + "\n");
			}
			result = sb.toString();
			// result = readIt(connection.getInputStream());
		} catch (MalformedURLException e) {
			Log.e("zz", e.getMessage());
		} catch (IOException e) {
			Log.e("zz", e.getMessage());
		}
		return result;
	}

	public static String sendPost(String url, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			HttpURLConnection conn = (HttpURLConnection) realUrl
					.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
			conn.setInstanceFollowRedirects(true);
			// conn.setRequestProperty("Accept-Charset","utf-8;q=0.7,*;q=0.7");

			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			result = readIt(conn.getInputStream());
		} catch (Exception e) {
			System.out.println("发送POST请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	// Given a URL, establishes an HttpUrlConnection and retrieves
	// the web page content as a InputStream, which it returns as
	// a string.
	public static String downloadString(String myurl, String encoding)
			throws IOException {
		InputStream is = null;
		// Only display the first 500 characters of the retrieved
		// web page content.
		try {
			String contentAsString = "";
			URL url = new URL(myurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.81 Safari/537.1");
			conn.setReadTimeout(15000 /* milliseconds */);
			conn.setConnectTimeout(20000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);

			conn.connect();
			int response = conn.getResponseCode();
			if (response != 200)
				Log.e("zz", myurl + "\tThe response is: " + response);
			// Starts the query
			is = conn.getInputStream();

			StringBuffer sb = new StringBuffer();
			BufferedReader br = new BufferedReader(new InputStreamReader(is,
					encoding));
			String data = "";
			while ((data = br.readLine()) != null) {
				sb.append(data + "\n");
			}
			contentAsString = sb.toString();
			return contentAsString;

			// Makes sure that the InputStream is closed after the app is
			// finished using it.
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	// Given a URL, establishes an HttpUrlConnection and retrieves
	// the web page content as a InputStream, which it returns as
	// a string.
	public static ArrayList<String> downloadPageAndCookie(String myurl,
			String encoding) throws IOException {
		InputStream is = null;
		ArrayList<String> results = new ArrayList<String>();
		// Only display the first 500 characters of the retrieved
		// web page content.
		try {
			URL url = new URL(myurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.81 Safari/537.1");
			conn.setRequestProperty("Referer", "http://www.xindm.cn/");
			conn.setReadTimeout(15000 /* milliseconds */);
			conn.setConnectTimeout(20000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);

			conn.connect();
			int response = conn.getResponseCode();
			if (response != 200)
				Log.e("zz", myurl + "\tThe response is: " + response);
			// Starts the query
			is = conn.getInputStream();

			StringBuffer sb = new StringBuffer();
			BufferedReader br = new BufferedReader(new InputStreamReader(is,
					encoding));
			String data = "";
			while ((data = br.readLine()) != null) {
				sb.append(data + "\n");
			}
			results.add(sb.toString());

			List<String> cookies = conn.getHeaderFields().get("Set-Cookie");
			if (cookies != null) {
				results.addAll(cookies);
			}

			return results;
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	// Given a URL, establishes an HttpUrlConnection and retrieves
	// the web page content as a InputStream, which it returns as
	// a string.
	public static void downloadFile(String surl, File file, String referer,
			ArrayList<String> cookie) throws IOException {
		InputStream is = null;
		// Only display the first 500 characters of the retrieved
		// web page content.
		try {
			URL url = new URL(surl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.81 Safari/537.1");
			conn.setRequestProperty("Referer", referer);
			if (cookie.size() > 0) {
				String acookie = StringUtils.splitByWholeSeparatorPreserveAllTokens(cookie.get(0), ";")[0];
				// Log.e("zz", "Cookie:" + acookie);
				conn.setRequestProperty("Cookie", acookie);
			}
			conn.setReadTimeout(15000 /* milliseconds */);
			conn.setConnectTimeout(20000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);

			conn.connect();
			int response = conn.getResponseCode();
			if (response != 200)
				Log.e("zz", surl + "\tThe response is: " + response);
			// Starts the query
			is = conn.getInputStream();

			OutputStream output = new FileOutputStream(file);

			byte data[] = new byte[1024];
			int count;
			while ((count = is.read(data)) != -1) {
				output.write(data, 0, count);
			}

			output.flush();
			output.close();

			// Makes sure that the InputStream is closed after the app is
			// finished using it.
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	public static void downloadFile2(String surl, File file, String referer,
			ArrayList<String> cookie) throws IOException {
		URL url = new URL(surl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("Referer", referer);
		String acookie = StringUtils.splitByWholeSeparatorPreserveAllTokens(cookie.get(0), ";")[0];
		Log.e("zz", "Cookie:" + acookie);
		connection.setRequestProperty("Cookie", acookie);
		connection
				.setRequestProperty(
						"User-Agent",
						"Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.81 Safari/537.1");
		connection.setRequestMethod("GET");
		connection.setDoInput(true);

		// connection.setRequestProperty("Cookie", cookie);
		connection.connect();

		// download the file
		InputStream input = new BufferedInputStream(url.openStream());
		OutputStream output = new FileOutputStream(file);

		byte data[] = new byte[1024];
		int count;
		while ((count = input.read(data)) != -1) {
			output.write(data, 0, count);
		}

		output.flush();
		output.close();
		input.close();
	}

	// Reads an InputStream and converts it to a String.
	public static String readIt(InputStream stream) throws IOException,
			UnsupportedEncodingException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[128];
		int ch = -1;
		while ((ch = stream.read(buf)) != -1) {
			baos.write(buf, 0, ch);
		}
		return new String(baos.toByteArray());
	}

	public static String join(String[] ss, String sep) {
		String s = "";
		if (ss.length > 0) {
			for (String as : ss) {
				s += as + sep;
			}
			s = s.substring(0, s.length() - sep.length());
		}
		return s;
	}
	
	// return ".png" etc.
	public static String getExt(String s, String defValue){
		int index = s.lastIndexOf(".");
		if(index==-1){
			return defValue;
		}
		else{
			return s.substring(index);
		}
	}

}
