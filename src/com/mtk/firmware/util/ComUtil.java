package com.mtk.firmware.util;

public class ComUtil {
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public static final String USER_DIR = System.getProperty("user.dir");

	public static final String XBIN = pathConcat(USER_DIR,"xbin");
	public static final String BIN_IDENTIFY = pathConcat(XBIN,"identify.exe");
	public static final String BIN_CONVERT = pathConcat(XBIN,"convert.exe");
	public static final String BIN_MKDIR =pathConcat(XBIN,"mkdir.exe");
	public static final String BIN_ZIP = pathConcat(XBIN,"zip.exe");
	public static final String BIN_CP = pathConcat(XBIN,"cp.exe");

	public static String pathConcat(String... strs){
		StringBuffer sb = new StringBuffer();
		int index = 0;
		for(String str : strs){
			index++;
			
			sb.append(str);
			if(index < strs.length){
				sb.append(FILE_SEPARATOR);
			}
		}
		return sb.toString();
	}

	public static String strConcat(String... strs){
		StringBuffer sb = new StringBuffer();
		for(String str : strs){
			sb.append(str);
			sb.append(" ");
		}
		return sb.toString();
	}

	public static String cmdConcat(String... strs){
		StringBuffer sb = new StringBuffer();
		int index = 0;
		for(String str : strs){
			index++;
			
			sb.append(str);
			if(index < strs.length){
				sb.append(" & ");
			}
		}
		return sb.toString();
	}

	public static String strWithQuotation(String str){
		return new StringBuffer().append("\"").append(str).append("\"").toString();
	}
}
