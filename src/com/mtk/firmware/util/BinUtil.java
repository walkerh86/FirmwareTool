package com.mtk.firmware.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Console;  
import java.io.PrintWriter;  
import java.io.OutputStream;

public class BinUtil {
	public static void zipUpdateApk(String cdDir, String apkPath, String updatePath){
		String cmdCdDir = ComUtil.strConcatSpace("cmd /c cd /d ",ComUtil.strWithQuotation(cdDir));
		String cmdZipUpdate = ComUtil.strConcatSpace(ComUtil.BIN_ZIP,"-m",ComUtil.strWithQuotation(apkPath),updatePath);
		String cmd = ComUtil.cmdConcat(cmdCdDir,cmdZipUpdate);
		doCmd(cmd);
	}
	
	public static void zipCompress(String cdDir, String dstFile, String zipParam){
		String cmdCdDir = ComUtil.strConcatSpace("cmd /c cd /d ",ComUtil.strWithQuotation(cdDir));
		String cmdCompress = ComUtil.strConcatSpace(ComUtil.BIN_ZIP,zipParam,dstFile,"*");
		String cmd = ComUtil.cmdConcat(cmdCdDir,cmdCompress);
		doCmd(cmd);
	}

	public static void zipDelete(String zipPath, String delPaths){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_ZIP,"-d",ComUtil.strWithQuotation(zipPath),delPaths);
		doCmd(cmd);
	}

	public static void unzipExtract(String zipFile, String extractPath, String dstDir){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_UNZIP,"-o",ComUtil.strWithQuotation(zipFile),ComUtil.strWithQuotation(extractPath),"-d",ComUtil.strWithQuotation(dstDir));
		doCmd(cmd);
	}

	public static void unzipExtractNoLog(String zipFile, String extractPath, String dstDir){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_UNZIP,"-o",ComUtil.strWithQuotation(zipFile),ComUtil.strWithQuotation(extractPath),"-d",ComUtil.strWithQuotation(dstDir));
		doCmd(cmd,false,false);
	}

	public static void convertImage(String src, String dst){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_CONVERT,ComUtil.strWithQuotation(src),ComUtil.strWithQuotation(dst));
		doCmd(cmd);
	}

	public static void rotateImage(int degree, String imgPath){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_CONVERT,ComUtil.strWithQuotation(imgPath),"-rotate",String.valueOf(degree),ComUtil.strWithQuotation(imgPath));
		doCmd(cmd);
	}

	public static void resizeImage(String imgPath, String dstPath, String size){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_CONVERT,ComUtil.strWithQuotation(imgPath),"-resize",size,ComUtil.strWithQuotation(dstPath));
		doCmd(cmd);
	}

	public static void convertAndRotateImage(String srcPath, String dstPath, int degree){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_CONVERT,ComUtil.strWithQuotation(srcPath),"-rotate",String.valueOf(degree),ComUtil.strWithQuotation(dstPath));
		doCmd(cmd);
	}
	
	public static Size IdentifyImageSize(String imgPath){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_IDENTIFY,"-format %wx%h",ComUtil.strWithQuotation(imgPath));
		Size size = new Size(-1,-1);
		String sizeStr = doCmd(cmd, true, true);
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
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_SED,"-i",ComUtil.strWithQuotation(ComUtil.strConcat("/^",prop,"=/s/=.*/=",value,"/")),filePath);
		doCmd(cmd);
	}

	public static void sedInsert(String filePath, String prop, String value){
		String cmd = ComUtil.strConcatSpace(ComUtil.BIN_SED,"-i",ComUtil.strConcat("/^ro.ty.auth=1/i",prop,"=",value),filePath);
		doCmd(cmd);
	}

	public static void checksumGen(String dstPath){
		final String DST_BIN = ComUtil.strWithQuotation(ComUtil.pathConcat(dstPath,"CheckSum_Gen.exe"));
		copy(ComUtil.BIN_CHECKSUM,DST_BIN);
		String cmd = ComUtil.strConcatSpace(DST_BIN,ComUtil.strWithQuotation(dstPath));

		Log.i(ComUtil.strConcatSpace("doCmd,cmd =",cmd));

		try {
			Runtime r=Runtime.getRuntime();
			final Process p=r.exec(cmd); 
		
			new Thread(new Runnable(){
				@Override
				public void run(){
					try{
						BufferedReader inBr=new BufferedReader(new InputStreamReader(p.getInputStream()));
						StringBuffer sbResult = new StringBuffer();
						String inline;
						while(null!=(inline=inBr.readLine())){ 
							sbResult.append(inline);
							if(inline.contains("Press any key to continue")){
								p.destroy();
							}
							//Log.i(inline);
						}
						inBr.close();
					}catch (Exception e) {
						
					}
				}
			}).start();
			
			BufferedReader errBr=new BufferedReader(new InputStreamReader(p.getErrorStream()));
			StringBuffer errSb=new StringBuffer();
			String inline;
			while(null!=(inline=errBr.readLine())){ 
				errSb.append(inline);
				Log.i("err line:"+inline);
			}
			errBr.close();

			p.waitFor();

			//BinUtil.rm(DST_BIN);
		}catch (Exception e) {
			Log.i("doCmd, e="+e.toString()); 
		}
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

	public static String doCmd(String cmd, boolean logEnable, boolean getOut){
		if(logEnable){
			Log.i(ComUtil.strConcatSpace("doCmd,cmd =",cmd));
		}
		StringBuffer sbResult = null;
		try {
			Runtime r=Runtime.getRuntime();
			final Process p=r.exec(cmd); 

			new Thread(new Runnable(){
				@Override
				public void run(){
					try{
						BufferedReader inBr=new BufferedReader(new InputStreamReader(p.getInputStream()));
						StringBuffer sbResult = new StringBuffer();
						String inline;
						while(null!=(inline=inBr.readLine())){ 
							sbResult.append(inline);
						}
						inBr.close();
					}catch (Exception e) {
						
					}
				}
			}).start();

			BufferedReader errBr=new BufferedReader(new InputStreamReader(p.getErrorStream()));
			StringBuffer errSb=new StringBuffer();
			String inline;
			while(null!=(inline=errBr.readLine())){ 
				errSb.append(inline);
			}
			errBr.close();
			
			int result = p.waitFor();
			if(logEnable && result != 0){
				Log.i("doCmd,result="+errSb.toString());
			}
		}catch (Exception e) {
			Log.i("doCmd, e="+e.toString()); 
		}

		return (sbResult != null) ? sbResult.toString() : null;
	}

	public static void doCmd(String cmd){
		doCmd(cmd,true,false);
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
