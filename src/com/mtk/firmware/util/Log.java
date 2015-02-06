package com.mtk.firmware.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class Log {
	private static boolean mEnabled = false;
	private static final String mLogPath = ComUtil.pathConcat(ComUtil.USER_DIR,"log.txt");
	private static FileWriter mLogWriter;
	private static Date mDate;

	public static void setEnabled(boolean enable){
		mEnabled = enable;
		if(mEnabled){
			try{
				File logFile = new File(mLogPath);
				if (logFile.exists()){				
					logFile.delete();
				}
				logFile.createNewFile();
				mLogWriter = new FileWriter(logFile, true);
			}catch (IOException e){
				e.printStackTrace();
			}
			
			mDate = new Date();
		}
	}
	
	public static void i(String log){
		if(mEnabled){
			try{
				mLogWriter.write(mDate.toLocaleString() + " " + log);
				mLogWriter.flush();
			}catch (IOException e){
				e.printStackTrace();
			}
		}else{
			System.out.print(log);
			System.out.print("\n");
		}
	}
}
