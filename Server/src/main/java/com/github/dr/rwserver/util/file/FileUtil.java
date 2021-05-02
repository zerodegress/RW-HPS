package com.github.dr.rwserver.util.file;

import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.log.Log;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.github.dr.rwserver.util.IsUtil.isBlank;
//Java

/**
 * @author Dr
 */
public class FileUtil {

	private File file;

	public static String path = null;

	private String filepath;

	public FileUtil(File file){
		this.file = file;
		this.filepath = file.getPath();
		if(!file.exists()) {
			file.mkdirs();
		}
	}

	public FileUtil(String filepath){
		this.filepath = filepath;
		file = new File(filepath);
		if(!file.exists()) {
			file.mkdirs();
		}
	}

	private FileUtil(File file, String filepath){
		this.file = file;
		this.filepath = filepath;
		if(!file.exists()) {
			file.mkdirs();
		}
	}

	public static FileUtil File() {
		return File(null);
	}

	public static FileUtil File(String tofile) {
		File file;
		String filepath = null;
		String to = tofile;
		if (null!=tofile) {
			final String pth = "/";
            if(!pth.equals(String.valueOf(tofile.charAt(0)))) {
                to = "/" + tofile;
            }
        }
		try {
			File directory = new File("");
			if (null==tofile) {
                file = (isBlank(path)) ? new File(directory.getCanonicalPath()) : new File(path);
            } else {
				filepath=(isBlank(path)) ? directory.getCanonicalPath()+to : path + to;
				file = new File(filepath);
			}
		} catch (Exception e) {

			if (null==tofile) {
                file = (isBlank(path)) ? new File(System.getProperty("user.dir")) : new File(path);
            } else {
				filepath= (isBlank(path)) ? System.getProperty("user.dir")+to : path;
				file = new File(filepath);
			}
		}
		return new FileUtil(file,filepath);
	}

	public boolean exists() {
		return (file.exists());
	}

	public File getFile() {
		return file;
	}

	public String getPath() {
		return filepath;
	}

	public FileUtil toPath(String filename) {
		String path = filepath+"/"+filename;
		filepath = path;
		file = new File(path);
		if(!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		return this;
	}

	public List<File> getFileList() {
		File[] array = file.listFiles();
		List<File> fileList = new ArrayList<>();
		for (File value : array) {
			if (!value.isDirectory()) {
				if (value.isFile()) {
					fileList.add(value);
				}
			}
		}
		return fileList;
	}
/*
	public List<File> getFileList() {
		String[] list = file.list();
		List<File> fileList = new ArrayList<File>();
		if (list == null) {
			return fileList;
		}
		File file;
		for (String path : list) {
			file = new File(new String(path.getBytes(),Data.UTF_8));
			if(!file.isDirectory() && file.isFile()) {
				fileList.add(file);
			} else {
				try {
					file = new File(new String(path.getBytes(),"gbk"));
					if(!file.isDirectory() && file.isFile()) {
						fileList.add(file);
					}
				} catch (UnsupportedEncodingException e) {
					Log.error("[FILE GBK]",e);
				}
			}
		}
		return fileList;
	}
	*/
	/**
	 *
	 * @param log
	 * @param cover 是否尾部写入
	 */
	public void writeFile(Object log, boolean cover) {
		File parent = file.getParentFile();
		if(!parent.exists()) {
			parent.mkdirs();
		}
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				Log.error("Mk file",e);
			}
		}
		try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file,cover), StandardCharsets.UTF_8)) {
			osw.write(log.toString());
			osw.flush();
		} catch (Exception e) {
			Log.error("writeFile",e);
		}
	}

	public void writeFileByte(byte[] bytes, boolean cover) {
		File parent = file.getParentFile();
		if(!parent.exists()) {
			parent.mkdirs();
		}
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				Log.error("Mk file",e);
			}
		}

		try (BufferedOutputStream osw = new BufferedOutputStream(new FileOutputStream(file,cover))) {
			osw.write(bytes);
			osw.flush();
		} catch (Exception e) {
			Log.error("writeByteFile",e);
		}
	}

	public OutputStream writeByteOutputStream(boolean cover) throws Exception {
			File parent = file.getParentFile();
			if(!parent.exists()) {
				parent.mkdirs();
			}
			if(!file.exists()) {
				file.createNewFile();
			}
			return new FileOutputStream(file,cover);
	}

	public FileInputStream getInputsStream() {
		File parent = file.getParentFile();
		if(!parent.exists()) {
			parent.mkdirs();
		}
		try {
			if(!file.exists()) {
				file.createNewFile();
			}
			return new FileInputStream(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public InputStreamReader readInputsStream() {
		try {
			return new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
		} catch (IOException e) { 
			e.printStackTrace(); 
		}
		return null;
	}

	public byte[] readFileByte() throws Exception {
		ByteBuffer byteBuffer;
		try (FileChannel channel = new FileInputStream(file).getChannel()) {
			byteBuffer = ByteBuffer.allocate((int) channel.size());
			while ((channel.read(byteBuffer)) > 0) {
			}
		}
		return byteBuffer.array();
	}

	public Object readFileData(boolean list) {
		try(InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
			return readFileData(list,inputStreamReader);
		} catch (FileNotFoundException fileNotFoundException) {
			Log.error("FileNotFoundException");
		} catch (IOException ioException) {
			Log.error("Read IO Error",ioException);
		}
		return null;
	}

	public static Object readFileData(boolean list, InputStreamReader isr) {
		try (BufferedReader br = new BufferedReader(isr)) {
			String line;
			if(list){
				Seq<String> FileContent = new Seq<>();
				while ((line = br.readLine()) != null) { 
					FileContent.add(line);
				} 
				return FileContent;
			} else {
				StringBuilder FileContent = new StringBuilder();
				while ((line = br.readLine()) != null) { 
					FileContent.append(line);
					FileContent.append("\r\n");
				}
				return FileContent.toString();
			}
		} catch (IOException e) { 
			e.printStackTrace(); 
		}
		return null;
	}

}