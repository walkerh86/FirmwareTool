package com.mtk.firmware.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.io.FileInputStream;
import java.nio.channels.FileChannel;

public class FileUtil
{

	public static long getFileSizes(File f) throws Exception // 获取文件大小
	{
		long s = 0;
		if (f.exists())
		{
			FileInputStream fis = null;
			fis = new FileInputStream(f);
			s = fis.available();
			fis.close();
		}
		else
		{
			f.createNewFile();
			System.out.println("文件不存在");
		}

		return s;
	}

	public static long getFileSizes(String filePath){
		long s = 0;
		
		try{
			File f = new File(filePath);
			if (f.exists()){			
				FileInputStream fis = null;
				fis = new FileInputStream(f);
				s = fis.available();
				fis.close();
			}else{
				f.createNewFile();
				System.out.println("文件不存在");
			}
		}catch(IOException e){
		}

		return s;
	}

	public static long getDirSize(File f) throws Exception // 获取目录内文件总大小
	{
		long size = 0;
		File flist[] = f.listFiles();
		for (int i = 0; i < flist.length; i++)
		{
			if (flist[i].isDirectory())
			{
				size = size + getDirSize(flist[i]);
			}
			else
			{
				size = size + flist[i].length();
			}
		}
		return size;
	}

	public static int FormatFileSize(long fileS) // 格式化文件大小
	{
		DecimalFormat df = new DecimalFormat("#.00");
		return (int) Double.parseDouble(df.format((double) fileS / 1048576));
	}

	public static long getlist(File f) // 递归求取目录文件个数
	{
		long size = 0;
		File flist[] = f.listFiles();
		size = flist.length;
		for (int i = 0; i < flist.length; i++)
		{
			if (flist[i].isDirectory())
			{
				size = size + getlist(flist[i]);
				size--;
			}
		}
		return size;
	}

	public static void fileChannelCopy(File s, File t) // 文件另存拷贝
	{
		FileInputStream fi = null;
		FileOutputStream fo = null;
		FileChannel in = null;
		FileChannel out = null;

		try
		{
			fi = new FileInputStream(s);
			fo = new FileOutputStream(t);
			in = fi.getChannel();
			out = fo.getChannel();
			in.transferTo(0, in.size(), out);
		}
		catch (IOException e)
		{
		}
		finally
		{
			try
			{
				fi.close();
				in.close();
				fo.close();
				out.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
