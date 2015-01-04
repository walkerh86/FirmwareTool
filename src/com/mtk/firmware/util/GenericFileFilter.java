package com.mtk.firmware.util;

import java.io.File;

public class GenericFileFilter extends javax.swing.filechooser.FileFilter
{
	private static final boolean	ONE		= true;
	private String					fileExt;
	private String[]				fileExts;
	private boolean					type	= false;
	private String					description;
	private int						length;
	private String					extension;

	public GenericFileFilter(String[] filesExtsIn, String description)
	{
		if (filesExtsIn.length == 1)
		{
			type = ONE;
			fileExt = filesExtsIn[0];
		}
		else
		{
			fileExts = filesExtsIn;
			length = fileExts.length;
		}
		this.description = description;
	}

	public boolean accept(File f)
	{
		if (f.isDirectory())
		{
			return true;
		}
		extension = getExtension(f);
		if (extension != null)
		{
			if (type)
				return check(fileExt);
			else
			{
				for (int i = 0; i < length; i++)
				{
					if (check(fileExts[i]))
						return true;
				}
			}
		}
		return false;
	}

	private boolean check(String in)
	{
		return extension.equalsIgnoreCase(in);
	}

	public String getDescription()
	{
		return description;
	}

	private String getExtension(File file)
	{
		String filename = file.getName();
		int length = filename.length();
		int i = filename.lastIndexOf('.');
		if (i > 0 && i < length - 1)
			return filename.substring(i + 1).toLowerCase();
		return null;
	}
}
