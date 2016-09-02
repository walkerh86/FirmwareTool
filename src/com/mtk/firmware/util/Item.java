package com.mtk.firmware.util;

public class Item
{
	private String	id;
	private String	description;
	private String mIndex;

	public Item(String id, String description, String index)
	{
		this.id = id;
		this.description = description;
		this.mIndex = index;
	}

	public String getIndex(){
		return mIndex;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String str){
		id = str;
	}

	public String getDescription()
	{
		return description;
	}

	public String toString()
	{
		return description;
	}
}
