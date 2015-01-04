package com.mtk.firmware.util;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.text.Document;

public class MyTextField extends JTextField implements KeyListener
{
	String	jname;

	public MyTextField()
	{

	}

	public MyTextField(String text)
	{
		super(text);
	}

	public MyTextField(int columns, String name)
	{
		super(columns);
		addKeyListener(this);
		jname = name;
	}

	public MyTextField(String text, int columns)
	{
		super(text, columns);
	}

	public MyTextField(Document doc, String text, int columns)
	{
		super(doc, text, columns);
	}

	public String getJname()
	{
		return jname;
	}

	public void setJname(String name)
	{
		jname = name;
	}

	@Override
	public void keyPressed(KeyEvent e)
	{

	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		String temp = getJname();
		if (e.getKeyChar() == ' ' && (temp.equals("jtf_ums") || temp.equals("jtf_mtp")))
		{
			JOptionPane.showMessageDialog(null, "不允许有空格!!");
			setText("");
			System.out.print("has blank");
		}

		if (!isChinese(getText()))
		{
			JOptionPane.showMessageDialog(null, "不允许有中文!!");
			setText("");
		}

	}

	private boolean isChinese(String path)
	{
		String regEx = "[\\u4e00-\\u9fa5]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(path);
		int count = 0;
		while (m.find())
		{
			for (int i = 0; i <= m.groupCount(); i++)
			{
				count = count + 1;
			}
		}
		if (count != 0)
		{
			return false;
		}
		return true;
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		String temp = getJname();
		if (temp.equals("jtf_ums") || temp.equals("jtf_mtp"))
		{
			if (getText().length() >= 11)
			{
				setText(getText().substring(0, 11));
			}
		}
	}

}
