package com.mtk.firmware.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class Language
{
	Vector<Item>				model;
	SAXReader					saxReader;
	InputStream					timezoneFile;
	Document					document;
	Element						root1Elm;
	public final static String	languagefile	= System.getProperty("user.dir") + "\\etc\\language.xml";

	public Language()
	{
		try
		{
			model = new Vector<Item>();
			saxReader = new SAXReader();
			timezoneFile = new FileInputStream(languagefile);
			;
			document = saxReader.read(timezoneFile);
			root1Elm = document.getRootElement();
		}
		catch (DocumentException e)
		{
			e.printStackTrace();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	public int getIndex(String lan)
	{
		List nodes = root1Elm.elements("item");
		Iterator<Element> it = nodes.iterator();
		for (int i = 0; it.hasNext(); i++)
		{
			Element elm = (Element) it.next();
			if (elm.attributeValue("id").toString().equals(lan))
			{
				return i + 1;
			}
		}
		return 0;
	}

	public Vector<Item> getLanguage()
	{

		List nodes = root1Elm.elements("item");
		model.addElement(new Item("", "-------------请选择-------------"));
		for (Iterator<Element> it = nodes.iterator(); it.hasNext();)
		{
			Element elm = (Element) it.next();
			model.addElement(new Item(elm.attributeValue("id"), elm.attributeValue("value")));
		}
		return model;
	}
}
