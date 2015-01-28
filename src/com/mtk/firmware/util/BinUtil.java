package com.mtk.firmware.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BinUtil {
	public static void zipUpdateApk(String cdDir, String apkPath, String updatePath){
		String cmdCdDir = ComUtil.strConcat("cmd /c cd /d ",ComUtil.strWithQuotation(cdDir));
		String cmdZipUpdate = ComUtil.strConcat(ComUtil.BIN_ZIP,"-m",apkPath,ComUtil.strWithQuotation(updatePath));
		String cmd = ComUtil.cmdConcat(cmdCdDir,cmdZipUpdate);
		doCmd(cmd);
	}

	public static void convertImage(String src, String dst){
		String cmd = ComUtil.strConcat(ComUtil.BIN_CONVERT,ComUtil.strWithQuotation(src),ComUtil.strWithQuotation(dst));
		doCmd(cmd);
	}

	public static void rotateImage(int degree, String imgPath){
		String cmd = ComUtil.strConcat(ComUtil.BIN_CONVERT,ComUtil.strWithQuotation(imgPath),"-rotate",String.valueOf(degree),ComUtil.strWithQuotation(imgPath));
		doCmd(cmd);
	}

	public static void resizeImage(String imgPath, String size){
		String cmd = ComUtil.strConcat(ComUtil.BIN_CONVERT,ComUtil.strWithQuotation(imgPath),"-resize",size,ComUtil.strWithQuotation(imgPath));
		doCmd(cmd);
	}
	
	public static Size IdentifyImageSize(String imgPath){
		String cmd = ComUtil.strConcat(ComUtil.BIN_IDENTIFY,"-format %wx%h",ComUtil.strWithQuotation(imgPath));
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

	public static void makeDir(String path){
		String cmd = ComUtil.strConcat(ComUtil.BIN_MKDIR,"-p",ComUtil.strWithQuotation(path));
		doCmd(cmd);
	}

	public static void copyFile(String srcPath, String dstPaht){
		String cmd = ComUtil.strConcat(ComUtil.BIN_CP,srcPath,dstPaht);
		doCmd(cmd);
	}

	private static String doCmd(String cmd){
		System.out.print(ComUtil.strConcat("doCmd,cmd =",cmd,"\n"));
		
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
