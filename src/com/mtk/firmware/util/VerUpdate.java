package com.mtk.firmware.util;

import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class VerUpdate
{
	String		verLocalPath	= "/values/version.xml";
	SAXReader	saxReader;

	public String getLocalVer()
	{
		Element root1Elm = null;
		try
		{
			saxReader = new SAXReader();
			InputStream verStream = Timezones.class.getResourceAsStream(verLocalPath);
			Document document = saxReader.read(verStream);
			root1Elm = document.getRootElement();
		}
		catch (DocumentException e)
		{
			e.printStackTrace();
		}
		return root1Elm.getStringValue();
	}

}
