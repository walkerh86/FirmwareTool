package com.mtk.firmware.util;

import java.awt.Color;
import java.io.File;

public class ComUtil {
	public static final boolean DEBUG_MODE = false; //no need to load system.img in this mode
	public static final boolean FAST_MODE = false; //no need copy system.img
	public static final boolean ROM_AUTH_DISABLE = false; //no need copy system.img
	
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public static final String USER_DIR = System.getProperty("user.dir");
	public static final String OUT_DIR = pathConcat(USER_DIR,"firmware");
	public static final String SYSTEM_DIR = pathConcat(OUT_DIR,"system");
	public static final String SYSTEM_ETC_DIR = pathConcat(SYSTEM_DIR,"etc");
	public static final String FRAMEWORK_RES = pathConcat(SYSTEM_DIR,"framework","framework-res.apk");
	public static final String USERDATA_DIR = pathConcat(OUT_DIR,"data");

	public static final String XBIN = pathConcat(USER_DIR,"xbin");
	public static final String BIN_IDENTIFY = pathConcat(XBIN,"identify.exe");
	public static final String BIN_CONVERT = pathConcat(XBIN,"convert.exe");
	public static final String BIN_MKDIR =pathConcat(XBIN,"mkdir.exe");
	public static final String BIN_ZIP = pathConcat(XBIN,"zip.exe");
	public static final String BIN_UNZIP = pathConcat(XBIN,"unzip.exe");
	public static final String BIN_CP = pathConcat(XBIN,"cp.exe");
	public static final String BIN_SED = pathConcat(XBIN,"sed.exe");
	public static final String BIN_RM = pathConcat(XBIN,"rm.exe");
	public static final String BIN_BMP2RAW = pathConcat(XBIN,"bmp_to_raw.exe");
	public static final String BIN_SIMG2IMG = pathConcat(XBIN,"simg2img.exe");
	public static final String BIN_EXT4 = pathConcat(XBIN,"ext4.exe");
	public static final String BIN_MAKE_EXT4FS = pathConcat(XBIN,"make_ext4fs.exe");
	public static final String BIN_CYGPATH = pathConcat(XBIN,"cygpath","cygpath.exe");
	public static final String BIN_CHECKSUM = pathConcat(XBIN,"CheckSum_Gen.exe");

	public static final Color COLOR_MARK_MODIFIED = new Color(0,201,87);
	public static final Color COLOR_MARK_UNMODIFIED = Color.BLACK;
	public static final Color COLOR_MARK_FAILED = Color.RED;

	public static String strConcatWith(String... strs){
		if(strs == null || strs.length < 2){
			return null;
		}else if(strs.length == 2){
			return strs[1];
		}
		StringBuffer sb = new StringBuffer();
		String catStr = strs[0];
		for(int i=1;i<strs.length-1;i++){
			sb.append(strs[i]);
			sb.append(catStr);
		}
		sb.append(strs[strs.length-1]);
		return sb.toString();
	}

	public static String strConcat(String... strs){
		String[] tmpStrs = new String[strs.length+1];
		System.arraycopy(strs, 0, tmpStrs, 1, strs.length);
		tmpStrs[0] = "";
		return strConcatWith(tmpStrs);
	}

	public static String strConcatSpace(String... strs){
		String[] tmpStrs = new String[strs.length+1];
		System.arraycopy(strs, 0, tmpStrs, 1, strs.length);
		tmpStrs[0] = " ";
		return strConcatWith(tmpStrs);
	}

	public static String cmdConcat(String... strs){
		String[] tmpStrs = new String[strs.length+1];
		System.arraycopy(strs, 0, tmpStrs, 1, strs.length);
		tmpStrs[0] = " & ";
		return strConcatWith(tmpStrs);
	}

	public static String pathConcat(String... strs){
		String[] tmpStrs = new String[strs.length+1];
		System.arraycopy(strs, 0, tmpStrs, 1, strs.length);
		tmpStrs[0] = FILE_SEPARATOR;
		return strConcatWith(tmpStrs);
	}

	public static String strConcatWithDot(String... strs){
		String[] tmpStrs = new String[strs.length+1];
		System.arraycopy(strs, 0, tmpStrs, 1, strs.length);
		tmpStrs[0] = ".";
		return strConcatWith(tmpStrs);
	}
	
	public static String strWithQuotation(String str){
		return new StringBuffer().append("\"").append(str).append("\"").toString();
	}

	public static void mkTempDir(String path){
		if(new File(path).exists()){
			BinUtil.rm(path);
		}
		BinUtil.mkdir(path);
	}

	public static boolean strIsEmpty(String str){
		return str == null || str.length() == 0;
	}

	public static String getFileNameSuffix(String name){
		return name.substring(name.lastIndexOf(".")+1, name.length());
	}

	public static boolean isValidPictureFile(String name){
		return isValidPictureSuffix(getFileNameSuffix(name));
	}

	public static boolean isValidPictureSuffix(String suffix){
		return suffix.equalsIgnoreCase("jpg") || suffix.equalsIgnoreCase("jpeg") 
			|| suffix.equalsIgnoreCase("bmp") || suffix.equalsIgnoreCase("png");
	}
}
