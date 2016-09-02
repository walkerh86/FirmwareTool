package com.mtk.firmware.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class PropUtil {
	private String mPropFilePath;
	private HashMap<String,String> mPropMaps = new HashMap<String,String>();
	private HashMap<String,String> mPropMapsModified = new HashMap<String,String>();
	
	public PropUtil(String filePath){
		mPropFilePath = filePath;
		initProps();
		/*
		Iterator<Entry<String, String>> iter = mPropMaps.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, String> prop= iter.next();
			System.out.print("key="+prop.getKey()+",value="+prop.getValue()+"\n");
		}
		*/
	}
	
	public boolean containsProp(String prop){
		return mPropMaps.containsKey(prop);
	}
	
	public boolean isModified(){
		return mPropMapsModified.size() > 0;
	}
	
	public boolean isPorpSupport(String prop){
		return mPropMaps.containsKey(prop);
	}
	
	public String getValue(String prop){
		return mPropMaps.get(prop);
	}

	public void setValue(String prop, String value){
		String oldValue = getValue(prop);
		if(!oldValue.equals(value)){
			mPropMapsModified.put(prop,value);
			mPropMaps.put(prop,value);
		}
	}

	public void clearModified(){
		mPropMapsModified.clear();
	}

	public void save(){
		Iterator<Entry<String, String>> iter = mPropMapsModified.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, String> prop= iter.next();
			String convertValue = prop.getValue().replace("/","\\\\/");
			BinUtil.sedReplace(mPropFilePath, prop.getKey(), convertValue);
		}
	}

	private void initProps(){
		try{
			String lineStr;
			File file = new File(mPropFilePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			while ((lineStr = br.readLine()) != null){
				String trimStr = lineStr.trim();
				if(trimStr.length() < 1 || trimStr.startsWith("#") || !trimStr.contains("=")){
					continue;
				}
				String[] splitStr = trimStr.split("=");
				if(splitStr.length == 2){
					mPropMaps.put(splitStr[0].trim(), splitStr[1].trim());
				}else{
					mPropMaps.put(splitStr[0].trim(), "");
				}
			}
			br.close();
		}catch (FileNotFoundException e){		
			e.printStackTrace();
		}catch (IOException e){			
			e.printStackTrace();
		}
	}

	public void addProp(String prop, String value){
		mPropMaps.put(prop,value);
	}
}
