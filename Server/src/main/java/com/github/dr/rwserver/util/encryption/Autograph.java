package com.github.dr.rwserver.util.encryption;

import com.github.dr.rwserver.util.log.Log;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.jar.JarInputStream;

/**
 * @author Dr
 */
public final class Autograph {
	public boolean verify(URL url) {
		try(JarInputStream jarIn = new JarInputStream(new FileInputStream(new File(url.toURI())),true)) {
	        while(jarIn.getNextJarEntry() != null){
	            continue;
	        }
	        return true;
		} catch (Exception e) {
			Log.fatal(e);
		}
		return false;
	}
}