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
			if(ComUtil.DEBUG_MODE){
				mPropManager.loadProps();
			}
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

	public void clearModified(){
		for(PropUtil propUtil : mPropUtils){
			if(propUtil != null && propUtil.isModified()){
				propUtil.clearModified();
			}
		}
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

	public void insertBuildProp(String prop, String value){
		BinUtil.sedInsert(ComUtil.pathConcat(ComUtil.SYSTEM_DIR,"build.prop"),prop,value);
		mPropUtils[0].addProp(prop,value);
	}

	public static String getAndroidVersion(){
		PropManager propManager = PropManager.getInstance();
		return propManager.getValue("ro.build.version.release");
	}

	public static boolean isDispPort(){
		PropManager propManager = PropManager.getInstance();
		String value = propManager.getValue("ro.ty.flag");
		boolean port = (value == null) ? false : value.contains("_PORT");
		String board = propManager.getValue("ro.product.board");
		if("joya82_wet_kk".equals(board)){
			port = false;
		}
		return port;
	}

	public static boolean isLogoDispPort(){
		PropManager propManager = PropManager.getInstance();
		String value = propManager.getValue("ro.ty.logo.disp.port");
		boolean port = (value == null) ? isDispPort() : value.equals("1");
		return port;
	}

	public static int getDensity(){
		int density = 0;
		PropManager propManager = PropManager.getInstance();
		String value = propManager.getValue("ro.sf.lcd_density");
		if(value != null){
			density = Integer.valueOf(value);
		}
		return density;
	}

	public static int getLcmDp(){
		PropManager propManager = PropManager.getInstance();
		String value = propManager.getValue("ro.ty.flag");
		if(value == null){
			value = propManager.getValue("ro.build.display.id");
		}
		int sw = 0;
		if(value.contains("-HD-")){
			sw = 600;
		}else if(value.contains("-LD-")){
			sw = 480;
		}else{
			return sw;
		}
		int density = getDensity();
		if(density == 0){
			return sw;
		}
		return sw*160/density;
	}
}
