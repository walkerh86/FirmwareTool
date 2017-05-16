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

public class PropXmlParser
{
	Vector<Item>				model;
	SAXReader					saxReader;
	InputStream					propFile;
	Document					document;
	Element						root1Elm;

	public PropXmlParser(String filePath)
	{
		try
		{
			model = new Vector<Item>();
			saxReader = new SAXReader();
			propFile = new FileInputStream(filePath);
			document = saxReader.read(propFile);
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

	public int getIndex(String value)
	{
		List nodes = root1Elm.elements("item");
		Iterator<Element> it = nodes.iterator();
		for (int i = 0; it.hasNext(); i++)
		{
			Element elm = (Element) it.next();
			if (elm.attributeValue("id").toString().equals(value))
			{
				return i + 1;
			}
		}
		return 0;
	}

	public Vector<Item> getItems()
	{

		List nodes = root1Elm.elements("item");
		model.addElement(new Item("", "请选择","-1"));
		int index = 1;
		for (Iterator<Element> it = nodes.iterator(); it.hasNext();)
		{
			Element elm = (Element) it.next();
			model.addElement(new Item(elm.attributeValue("id"), elm.attributeValue("value"), String.valueOf(index++)));
		}
		return model;
	}

}
