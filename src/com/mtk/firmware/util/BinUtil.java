package com.mtk.firmware.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BinUtil {
	public static void zipUpdateApk(String cdDir, String apkPath, String updatePath){
		String cmdCdDir = ComUtil.strConcatSpace("cmd /c cd /d ",ComUtil.strWithQuotation(cdDir));
		String cmdZipUpdate = ComUtil.strConcatSpace(ComUtil.BIN_ZIP,"-m",apkPath,ComUtil.strWithQuotation(updatePath));
		String cmd = ComUtil.cmdConcat(cmdCdDir,cmdZipUpdate);
		doCmd(cmd);
	}

	public static void zipCompress(String cdDir, String dstFile, String zipParam){
		String cmdCdDir = ComUtil.strConcatSpace("cmd /c cd /d ",ComUtil.strWithQuotation(cdDir));
		String cmdCompress = ComUtil.strConcatSpace(ComUtil.BIN_ZIP,zipParam,dstFile,"*");
		String cmd = ComUtil.cmdConcat(cmdCdDir,cmdCompress);
		doCmd(cmd);
	}

	public static void unzipExtract(String zipFile, String extractPath, String dstDir){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_UNZIP,"-o",ComUtil.strWithQuotation(zipFile),ComUtil.strWithQuotation(extractPath),"-d",ComUtil.strWithQuotation(dstDir));
		doCmd(cmd);
	}

	public static void unzipExtractNoLog(String zipFile, String extractPath, String dstDir){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_UNZIP,"-o",ComUtil.strWithQuotation(zipFile),ComUtil.strWithQuotation(extractPath),"-d",ComUtil.strWithQuotation(dstDir));
		doCmdNoLog(cmd);
	}

	public static void convertImage(String src, String dst){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_CONVERT,ComUtil.strWithQuotation(src),ComUtil.strWithQuotation(dst));
		doCmd(cmd);
	}

	public static void rotateImage(int degree, String imgPath){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_CONVERT,ComUtil.strWithQuotation(imgPath),"-rotate",String.valueOf(degree),ComUtil.strWithQuotation(imgPath));
		doCmd(cmd);
	}

	public static void resizeImage(String imgPath, String size){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_CONVERT,ComUtil.strWithQuotation(imgPath),"-resize",size,ComUtil.strWithQuotation(imgPath));
		doCmd(cmd);
	}

	public static void convertAndRotateImage(String srcPath, String dstPath, int degree){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_CONVERT,ComUtil.strWithQuotation(srcPath),"-rotate",String.valueOf(degree),ComUtil.strWithQuotation(dstPath));
		doCmd(cmd);
	}
	
	public static Size IdentifyImageSize(String imgPath){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_IDENTIFY,"-format %wx%h",ComUtil.strWithQuotation(imgPath));
		Size size = new Size(-1,-1);
		String sizeStr = doCmd(cmd);
		if(sizeStr != null) {
			String[] sizeStrs = sizeStr.split("x");
			
			if(sizeStrs.length == 2){
				size.width = Integer.valueOf(sizeStrs[0]);
				size.height = Integer.valueOf(sizeStrs[1]);
			}
		}
		return size;
	}

	public static void bmpToRaw(String srcPath, String dstPath){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_BMP2RAW,ComUtil.strWithQuotation(dstPath),ComUtil.strWithQuotation(srcPath));
		doCmd(cmd);
	}

	public static void simgToImg(String img, String imgext){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_SIMG2IMG,ComUtil.strWithQuotation(img),ComUtil.strWithQuotation(imgext));
		doCmd(cmd);
	}

	public static void ext4(String imgext, String dstDir){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_EXT4,ComUtil.strWithQuotation(imgext),ComUtil.strWithQuotation(dstDir));
		doCmd(cmd);
	}

	public static void makeExt4Fs(String dstImg, String srcDir, int size){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_MAKE_EXT4FS,"-s -l",ComUtil.strConcat(String.valueOf(size),"M -a system"),ComUtil.strWithQuotation(dstImg),ComUtil.strWithQuotation(srcDir));
		doCmd(cmd);
	}

	public static void sedReplace(String filePath, String prop, String value){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_SED,"-i",ComUtil.strConcat("/^",prop,"/s/=.*/=",value,"/"),filePath);
		doCmd(cmd);
	}

	public static void sedInsert(String filePath, String prop, String value){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_SED,"-i",ComUtil.strConcat("/^ro.ty.auth=1/i",prop,"=",value),filePath);
		doCmd(cmd);
	}

	public static void mkdir(String path){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_MKDIR,"-p",ComUtil.strWithQuotation(path));
		doCmd(cmd);
	}

	public static void copy(String srcPath, String dstPaht){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_CP,ComUtil.strWithQuotation(srcPath),ComUtil.strWithQuotation(dstPaht));
		doCmd(cmd);
	}

	public static void rm(String path){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_RM,"-rf",ComUtil.strWithQuotation(path));
		doCmd(cmd);
	}

	private static String doCmdNoLog(String cmd){
		StringBuffer sb = null;
		try {
			Runtime r=Runtime.getRuntime();
			Process p=r.exec(cmd); 
			BufferedReader br=new BufferedReader(new InputStreamReader(p.getInputStream()));
			sb=new StringBuffer();
			String inline;
			while(null!=(inline=br.readLine())){ 
				sb.append(inline);
			}
			p.waitFor();
			br.close();
		}catch (Exception e) {
			e.printStackTrace(); 
		}

		return (sb != null) ? sb.toString() : null;
	}

	private static String doCmd(String cmd){
		Log.i(ComUtil.strConcatSpace("doCmd,cmd =",cmd));
		return doCmdNoLog(cmd);
	}

	public static class Size{
		public int width;
		public int height;

		public Size(int w, int h){
			width = w;
			height = h;
		}

		public String toString(){
			return width+"x"+height;
		}
	}
}
