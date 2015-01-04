package com.mtk.firmware.util;

import java.io.*;
import java.util.zip.*;

public class ZLib
{
	public static byte[] Inflate(byte[] source) throws DataFormatException, IOException
	{
		Inflater inflater = new Inflater();
		ByteArrayOutputStream stream = null;
		byte[] result = null;
		try
		{
			inflater.setInput(source);
			stream = new ByteArrayOutputStream(source.length);
			byte[] buffer = new byte[1024];
			while (!inflater.finished())
			{
				int decompressed = inflater.inflate(buffer);
				stream.write(buffer, 0, decompressed);
			}
			stream.close();
			result = stream.toByteArray();
			stream = null;
		}
		finally
		{
			inflater.end();
			if (stream != null)
			{
				stream.close();
			}
		}
		return result;
	}

	public static byte[] Deflate(byte[] source) throws IOException
	{
		Deflater deflater = new Deflater(9);
		ByteArrayOutputStream stream = null;
		byte[] result;
		try
		{
			deflater.setInput(source);
			deflater.finish();
			stream = new ByteArrayOutputStream(source.length);
			byte[] buffer = new byte[1024];
			while (!deflater.finished())
			{
				int compressed = deflater.deflate(buffer);
				stream.write(buffer, 0, compressed);
			}
			stream.close();
			result = stream.toByteArray();
			stream = null;
		}
		finally
		{
			deflater.end();
			if (stream != null)
			{
				stream.close();
			}
		}
		return result;
	}

	private static final java.util.Random	random	= new java.util.Random();

	public static void CreateRandomDataFile(String filename, int size) throws FileNotFoundException, IOException
	{
		byte[] data = new byte[size];
		random.nextBytes(data);
		SaveToFile(filename, data);
	}

	public static void SaveToFile(String filename, byte[] data) throws FileNotFoundException, IOException
	{
		FileOutputStream output = null;
		try
		{
			output = new FileOutputStream(filename);
			output.write(data);
		}
		finally
		{
			if (output != null)
			{
				output.close();
				output = null;
			}
		}
	}

	public static byte[] ReadFromFile(String filename) throws FileNotFoundException, IOException
	{
		FileInputStream input = null;
		byte[] buffer = new byte[1024];
		byte[] result = null;
		try
		{
			input = new FileInputStream(filename);
			while (true)
			{
				int size = input.read(buffer);
				if (size < 0)
				{
					break;
				}
				if (result == null)
				{
					result = new byte[size];
					System.arraycopy(buffer, 0, result, 0, size);
				}
				else
				{
					byte[] temp = new byte[result.length + size];
					System.arraycopy(result, 0, temp, 0, result.length);
					System.arraycopy(buffer, 0, temp, result.length, size);
					result = temp;
				}
			}
		}
		finally
		{
			if (input != null)
			{
				input.close();
				input = null;
			}
		}
		return result;
	}

	public static void main(String[] args)
	{
		if (args.length < 2)
		{
			System.out.format("javazlibtest n|c|d|t filename%nn:%s%nc:%s%nd:%s%nt:%s%n", "new", "compress", "decompress", "test");
			return;
		}
		byte[] data;
		try
		{
			switch (args[0].charAt(0))
			{
				case 'n':
					CreateRandomDataFile(args[1] + ".raw", 2048);
					break;
				case 'c':
					data = ReadFromFile(args[1] + ".raw");
					data = Deflate(data);
					SaveToFile(args[1] + ".zip", data);
					break;
				case 'd':
					data = ReadFromFile(args[1] + ".zip");
					data = Inflate(data);
					SaveToFile(args[1] + ".dat", data);
					break;
				case 't':
				{
					byte[] src = ReadFromFile(args[1] + ".raw");
					byte[] dest = ReadFromFile(args[1] + ".dat");
					if (src.length != dest.length)
					{
						System.out.println("length are different%n");
						return;
					}
					for (int i = 0; i < src.length; i++)
					{
						if (src[i] != dest[i])
						{
							System.out.format("data different. Position:%d%n", i);
							break;
						}
					}
				}
					break;
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace(System.err);
		}
	}
}
