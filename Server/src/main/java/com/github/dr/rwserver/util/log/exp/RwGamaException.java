package com.github.dr.rwserver.util.log.exp;

/**
 * @author Dr
 */
public class RwGamaException {
	public static class KickException extends Exception {
		public KickException(String type) {
	        super(com.github.dr.rwserver.util.log.ErrorCode.valueOf(type).getError());
	    }
	}

	public static class KickStartException extends KickException {
		public KickStartException(String type) {
	        super(type);
	    }
	} 

	public static class KickPullException extends KickException {
		public KickPullException(String type) {
	        super(type);
	    }
	}  

	public static class PasswdException extends Exception {
		public PasswdException(String type) {
	        super(com.github.dr.rwserver.util.log.ErrorCode.valueOf(type).getError());
	    }
	}  
}