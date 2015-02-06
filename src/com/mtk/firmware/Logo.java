package com.mtk.firmware;

import com.mtk.firmware.util.ComUtil;
import com.mtk.firmware.util.Log;
import com.mtk.firmware.util.ZLib;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;

public class Logo
{
	int		header_sig		= 0x58881688;
	int		header_length	= 512;
	int		name_length		= 32;
	int		fileCount		= 0;
	byte[]	name;
	File	logoFile;

	Logo(File f)
	{
		logoFile = f;
	}

	public byte[] toBH(int i)
	{
		byte[] result = new byte[4];
		result[0] = (byte) ((i >> 24) & 0xFF);
		result[1] = (byte) ((i >> 16) & 0xFF);
		result[2] = (byte) ((i >> 8) & 0xFF);
		result[3] = (byte) (i & 0xFF);
		return result;
	}

	public byte[] toLH(int n)
	{
		byte[] b = new byte[4];
		b[0] = (byte) (n & 0xff);
		b[1] = (byte) (n >> 8 & 0xff);
		b[2] = (byte) (n >> 16 & 0xff);
		b[3] = (byte) (n >> 24 & 0xff);
		return b;
	}

	public String toHex(byte[] b)
	{
		StringBuffer s = new StringBuffer();
		s.append("0x");
		for (int i = 0; i < b.length; i++)
		{
			String tmp = Integer.toHexString(b[i] & 0xFF);
			if (tmp.length() == 1)
				tmp = "0" + tmp;
			s.append(tmp);
		}
		return s.toString();
	}

	public byte[] getBytes(String filePath)
	{
		byte[] buffer = null;
		try
		{
			File file = new File(filePath);
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1000];
			int n;
			while ((n = fis.read(b)) != -1)
			{
				bos.write(b, 0, n);
			}
			fis.close();
			bos.close();
			buffer = bos.toByteArray();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return buffer;
	}

	public void unpack(String dstPath)
	{
		Log.i("====================================Begin to unpack logo.bin====================================");
		byte[] buf = new byte[4096];
		DataInputStream in = null;
		DataOutputStream out = null;
		try
		{
			System.out.println("logoFile length=" + logoFile.length());
			in = new DataInputStream(new BufferedInputStream(new FileInputStream(logoFile)));

			header_sig = in.readInt();
			Log.i("header_sig=" + toHex(toLH((header_sig))));

			byte[] logo_length = toLH(in.readInt());
			Log.i("logo_length=" + Integer.parseInt(toHex(logo_length).split("0x")[1], 16));

			name = new byte[name_length];
			in.read(name, 0, name.length);
			for (int k = 0; k < name.length; k++)
			{
				if (name[k] != 0)
					System.out.print((char) name[k]);
			}

			byte[] logo_addr = toLH(in.readInt());
			Log.i("logo_addr=" + toHex(logo_addr));

			byte[] header = new byte[header_length];
			in.read(header, 0, header_length - 4 - 4 - 32 - 4);
			fileCount = in.readByte();
			Log.i("num=" + fileCount);

			in.read(buf, 0, 7);
			int[] addrs = new int[fileCount];
			for (int i = 0; i < fileCount; i++)
			{
				addrs[i] = Integer.parseInt(toHex(toLH(in.readInt())).split("0x")[1], 16);
				Log.i("addr[" + i + "]" + toHex(toLH(addrs[i])));
			}
			for (int i = 0; i < fileCount; i++)
			{
				int size = 0;
				if (i == (fileCount - 1))
				{
					System.out.println(" " + toHex(logo_length));
					size = Integer.parseInt(toHex(logo_length).split("0x")[1], 16) - addrs[i];
				}
				else
				{
					size = addrs[i + 1] - addrs[i];
				}

				Log.i("size[" + i + "]" + size);
				byte[] temfile = new byte[size];
				in.read(temfile, 0, size);

				File raw = new File(ComUtil.pathConcat(dstPath, i + ".raw"));
				out = new DataOutputStream(new FileOutputStream(raw));
				raw.createNewFile();
				out.write(ZLib.Inflate(temfile));
				out.close();
			}
			in.close();
			Log.i("====================================Finish make logo.bin====================================");
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (DataFormatException e)
		{
			e.printStackTrace();
		}
	}

	public void repack(String dstPath)
	{
		try
		{
			int total = 4 + 4 + fileCount * 4;
			int[] addrs = new int[fileCount];
			addrs[0] = total;
			for (int i = 0; i < fileCount; i++)
			{
				File f = new File(ComUtil.pathConcat(dstPath, i + ".raw"));
				byte[] end = ZLib.Deflate(getBytes(f.getAbsolutePath()));
				Log.i("File[" + i + "]" + toHex(toLH(addrs[i])));
				if (i < fileCount - 1)
				{
					addrs[i + 1] = addrs[i] + end.length;
				}
				total += end.length;
			}
			Log.i("total=" + total);

			DataOutputStream out = new DataOutputStream(new FileOutputStream(logoFile));
			logoFile.delete();
			logoFile.createNewFile();
			out.write(toBH(header_sig));
			out.write(toLH(total));
			out.write(name);
			out.writeInt(~0);
			for (int i = 512 - 44; i > 0; i--)
			{
				out.write((char) 0xff);
			}
			out.write(toLH(fileCount));
			out.write(toLH(total));
			for (int i = 0; i < addrs.length; i++)
			{
				out.write(toLH(addrs[i]));
			}
			for (int i = 0; i < fileCount; i++)
			{
				File f = new File(ComUtil.pathConcat(dstPath, i + ".raw"));
				byte[] end = ZLib.Deflate(getBytes(f.getAbsolutePath()));
				out.write(end);
			}
			out.close();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}

	}

}
