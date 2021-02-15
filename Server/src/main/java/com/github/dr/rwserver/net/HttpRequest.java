package com.github.dr.rwserver.net;

import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.util.log.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;

import static com.github.dr.rwserver.util.IsUtil.isBlank;


/**
 * @author Dr
 */
public class HttpRequest {

	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36";
	private static final HttpClient CLIENT = HttpClient.newBuilder()
											.version(HttpClient.Version.HTTP_1_1)
											.connectTimeout(Duration.ofSeconds(10))
											.build();

	public static String doGet(String url) {
		java.net.http.HttpResponse<String> response = null;
		java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
											.uri(URI.create(url))
											.setHeader("User-Agent", USER_AGENT)
											.build();
		try {
			response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (Exception e) {
			Log.error("HTTP GET Error",e);
		}
		return (String) response.body();
	}

	public static String doPost(String url, String param) {
		return doPost(url,param,USER_AGENT);
	}

	public static String doPost(String url, String param,String usAg) {
		java.net.http.HttpResponse<String> response = null;
		java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
				.uri(URI.create(url))
				.setHeader("User-Agent", usAg)
				.POST(java.net.http.HttpRequest.BodyPublishers.ofString(param))
				.build();
		try {
			response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (Exception e) {
			Log.error("HTTP Post Error",e);
		}
		return (String) response.body();
	}

	public static String doPostRw(String url, String param) {
		/*
		java.net.http.HttpResponse<String> response = null;
		java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
				.uri(URI.create(url))
				.setHeader("User-Agent", "rw android 151 zh")
				.setHeader("Language", "zh")
				.POST(java.net.http.HttpRequest.BodyPublishers.ofString(param))
				.build();
		try {
			response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (Exception e) {
			Log.error("HTTP Post Error",e);
		}
		Log.info("POST CODE",response.statusCode());
		return (String) response.body();
		 */
		final StringBuilder result = new StringBuilder();
		PrintWriter out = null;
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			URLConnection conn = realUrl.openConnection();
			conn.setRequestProperty("User-Agent", "rw android 151 zh");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Language", "zh");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			out = new PrintWriter(conn.getOutputStream());
			out.print(param);
			out.flush();
			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), Data.UTF_8));
			while (in.readLine() != null) {
				result.append(Data.LINE_SEPARATOR + Data.LINE_SEPARATOR);
			}
		} catch (IOException e) {
			Log.error("doPost!", e);
		} finally {
			if (out != null) {
				out.close();
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					in = null;
				}
			}
		}
		return result.toString();
	}

	public static boolean downUrl(final String url,final File file) {
		try{
			File filepath=file.getParentFile();
			if(!filepath.exists()) {
				filepath.mkdirs();
			}
			URL httpUrl=new URL(url);
			HttpURLConnection conn;
			while (true) {
				conn = (HttpURLConnection) httpUrl.openConnection();
				conn.setRequestProperty("User-Agent",USER_AGENT);
				if (conn.getResponseCode() == 301 || conn.getResponseCode() == 302) {
					final String newUrl = conn.getHeaderField("Location");
					if (isBlank(newUrl)) {
						Log.error("Download Fail: Empty Redirect");
						return false;
					}
					httpUrl = new URL(newUrl);
				} else {
					break;
				}
			}
			try (BufferedInputStream bis = new BufferedInputStream(conn.getInputStream())) {
				try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
					byte[] buf = new byte[4096];
					int length = bis.read(buf);
					while (length != -1) {
						bos.write(buf, 0, length);
						length = bis.read(buf);
					}
				}
			}
			return true;
		} catch (Exception e) {
			Log.error("downUrl",e);
		}
		return false;
	}
}