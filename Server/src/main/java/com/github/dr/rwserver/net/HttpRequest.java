package com.github.dr.rwserver.net;

import com.github.dr.rwserver.util.log.Log;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * @author Dr
 */
public class HttpRequest {

	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36";
	private static final OkHttpClient client = new OkHttpClient();

	public static String doGet(String url) {
		Request request = new Request.Builder()
				.url(url)
				.addHeader("User-Agent",USER_AGENT)
				.build();
		try (Response response = client.newCall(request).execute()) {
			return response.body().string();
		} catch (IOException e) {
			Log.error(e);
		}
		return null;
	}

	public static String doPost(String url, String param) {
		return doPost(url,param,USER_AGENT);
	}

	public static String doPost(String url, String param,String usAg) {
		FormBody.Builder formBody = new FormBody.Builder();
		final String[] paramArray = param.split("&");
		for (String pam : paramArray) {
			final String[] keyValue = pam.split("=");
			formBody.add(keyValue[0],keyValue[1]);
		}
		Request request = new Request.Builder()
				.url(url)
				.addHeader("User-Agent",USER_AGENT)
				.post(formBody.build())
				.build();
		try {
			Response response = client.newCall(request).execute();
			if (!response.isSuccessful()) {
				Log.error("Unexpected code",new IOException());
			}
			return response.body().string();
		} catch (IOException e) {
			Log.error(e);
		}
		return "";
	}

	public static String doPostRw(String url, String param) {
		FormBody.Builder formBody = new FormBody.Builder();
		final String[] paramArray = param.split("&");
		for (String pam : paramArray) {
			final String[] keyValue = pam.split("=");
			formBody.add(keyValue[0],keyValue[1]);
		}
		Request request = new Request.Builder()
				.url(url)
				.addHeader("User-Agent","rw android 151 zh")
				.addHeader("Language","zh")
				.post(formBody.build())
				.build();
		try {
			Response response = client.newCall(request).execute();
			if (!response.isSuccessful()) {
				Log.error("Unexpected code",new IOException());
			}
			return response.body().string();
		} catch (IOException e) {
			Log.error(e);
		}
		return "";
	}

	public static boolean downUrl(final String url,final File file) {
		FileOutputStream output = null;
		try {
			Request request   = new Request.Builder()
					.url(url)
					.addHeader("User-Agent",USER_AGENT)
					.build();
			Response response = client.newCall(request).execute();
			if ( !response.isSuccessful() ) {
				throw new FileNotFoundException();
			}
			output  = new FileOutputStream(file);
			output.write(response.body().bytes());
			output.flush();
			return true;
		} catch (Exception e) {
			Log.error(e);
		} finally {
			if ( output != null ) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
}