package com.mtk.firmware.util;

public class PropManager {
	private PropUtil[] mPropUtils;
	private static PropManager mPropManager;
	private static final String[] mPropPaths = new String[]{
		ComUtil.pathConcat(ComUtil.SYSTEM_DIR,"build.prop"),
		ComUtil.pathConcat(ComUtil.SYSTEM_ETC_DIR,"custom.conf")
	};
	
	public PropManager(){
	}

	public static PropManager getInstance(){
		if(mPropManager == null){
			mPropManager = new PropManager();
		}

		return mPropManager;
	}

	public void loadProps(){
		mPropUtils = new PropUtil[mPropPaths.length];
		int i = 0;
		for(String propPath : mPropPaths){
			mPropUtils[i++] = new PropUtil(ComUtil.pathConcat(propPath));
		}
	}
	
	public String getValue(String prop){
		String value = null;
		for(PropUtil propUtil : mPropUtils){
			if(propUtil != null && propUtil.containsProp(prop)){
				value = propUtil.getValue(prop);
				break;
			}
		}
		return value;
	}
	
	public void setValue(String prop, String value){
		for(PropUtil propUtil : mPropUtils){
			if(propUtil != null && propUtil.containsProp(prop)){
				propUtil.setValue(prop,value);
				break;
			}
		}
	}

	public boolean isPorpExists(String prop){
		boolean exists = false;
		for(PropUtil propUtil : mPropUtils){
			if(propUtil != null && propUtil.containsProp(prop)){
				exists = true;
				break;
			}
		}
		return exists;
	}
	
	public boolean isPorpSupport(String prop){
		boolean support = false;
		for(PropUtil propUtil : mPropUtils){
			if(propUtil != null && propUtil.containsProp(prop)){
				support = true;
				break;
			}
		}
		return support;
	}
	
	public void save(){
		for(PropUtil propUtil : mPropUtils){
			if(propUtil != null && propUtil.isModified()){
				propUtil.save();
			}
		}
	}

	public boolean isModified(){
		boolean modified = false;
		for(PropUtil propUtil : mPropUtils){
			if(propUtil != null && propUtil.isModified()){
				modified = propUtil.isModified();
				if(modified){
					break;
				}
			}
		}
		return modified;
	}

	public static void insertBuildProp(String prop, String value){
		BinUtil.sedInsert(ComUtil.pathConcat(ComUtil.SYSTEM_DIR,"build.prop"),prop,value);
	}

	public static String getAndroidVersion(){
		PropManager propManager = PropManager.getInstance();
		return propManager.getValue("ro.build.version.release");
	}
}
