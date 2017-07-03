package com.mtk.firmware;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.mtk.firmware.util.ComUtil;
import com.mtk.firmware.util.Item;
import com.mtk.firmware.util.Language;
import com.mtk.firmware.util.Log;
import com.mtk.firmware.util.PropManager;
import com.mtk.firmware.util.PropUtil;
import com.mtk.firmware.util.Timezones;
import com.mtk.firmware.util.PropXmlParser;

import javax.swing.JCheckBox;

public class InfoPanel extends JPanel{
	private static final long	serialVersionUID	= 1L;
	private static InfoPanel	iPanel;

	private PropManager mPropManager;

	private static final LinkedHashSet<PropItemView> mPropModifiedSets = new LinkedHashSet<PropItemView>();
	private static final LinkedHashSet<PropItemView> mPropSets = new LinkedHashSet<PropItemView>();
	
	private static final int PROP_ITEM_COLS = 25;
	private static final int PROP_ITEM_LABEL_COLS = 6;

	public final static String	CFG_XML_PATH_TIMEZONE = System.getProperty("user.dir") + "\\etc\\timezones.xml";
	public final static String	CFG_XML_PATH_LANGUAGE = System.getProperty("user.dir") + "\\etc\\language.xml";
	public final static String	CFG_XML_PATH_PICSIZE = System.getProperty("user.dir") + "\\etc\\picturesize.xml";

	public final static String	CFG_XML_PATH_FONTSIZE = System.getProperty("user.dir") + "\\etc\\fontsize.xml";
	public final static String	CFG_XML_PATH_CAMPREVMODE = System.getProperty("user.dir") + "\\etc\\previewmode.xml";
	public final static String	CFG_XML_PATH_SIGNALFAKEMODE = System.getProperty("user.dir") + "\\etc\\signal_fakemode.xml";
	
	private InfoPanel(){	
		setLayout(null);
		initProps();
	}

	public synchronized static InfoPanel getInstance(){
		if (iPanel == null){
			iPanel = new InfoPanel();
			if(ComUtil.DEBUG_MODE){
				iPanel.onRomLoaded();
			}
		}
		return iPanel;
	}

	public boolean isModified(){
		mPropModifiedSets.clear();
		
		Iterator<PropItemView> iter = mPropSets.iterator();
		while(iter.hasNext()){
			PropItemView item = iter.next();
			if(item.isModified()){
				mPropModifiedSets.add(item);
			}
		}
		//Log.i("infoPenel, isSystemModified="+modified);
		return mPropModifiedSets.size() > 0;
	}

	public void doModify(){
		if(mPropManager == null || mPropModifiedSets.size() == 0){
			return;
		}
		mPropManager.clearModified();
		Iterator<PropItemView> iter = mPropModifiedSets.iterator();
		while(iter.hasNext()){
			PropItemView item = iter.next();
			String key = item.getKey();
			if(key.equals("ro.product.locale.language")){
				String value = item.getValue();
				String[] splitValue = value.split("_");
				if(splitValue.length == 2){
					mPropManager.setValue(key,splitValue[0]);
					mPropManager.setValue("ro.product.locale.region",splitValue[1]);
					item.setModified(true);
				}
			}else{
				mPropManager.setValue(key,item.getValue());
				item.setModified(true);

				if("ro.ty.ums.label".equals(key)){
					mPropManager.setValue("ro.product.name",item.getValue());
				}
			}
		}
		mPropManager.save();

		checkForGooglePlay();
	}

	public boolean isSystemModified(){
		boolean modified = false;
		Iterator<PropItemView> iter = mPropModifiedSets.iterator();
		while(iter.hasNext()){
			PropItemView item = iter.next();
			modified = item.isModifySuccessed();
			if(modified){
				break;
			}
		}
		//Log.i("infoPenel, isSystemModified="+modified);
		return modified;
	}

	public void finishModify(){
		mPropModifiedSets.clear();
	}

	public boolean isModifiedValid(){
		return true;
	}

	public void setEnabled(boolean bool){
		if(ComUtil.DEBUG_MODE){
			return;
		}		
		if(mPropManager == null){
			mPropManager = PropManager.getInstance();
		}
		
		Iterator<PropItemView> iter = mPropSets.iterator();
		while(iter.hasNext()){
			PropItemView item = iter.next();
			if(bool && !mPropManager.isPorpSupport(item.getKey())){
				item.setEnable(false);
			}else{
				item.setEnable(bool);
			}
		}
	}

	public void onRomLoaded(){
		if(mPropManager == null){
			mPropManager = PropManager.getInstance();
		}
				
		Iterator<PropItemView> iter1 = mPropSets.iterator();
		while(iter1.hasNext()){
			PropItemView item = iter1.next();
			String key = item.getKey();
			String value = mPropManager.getValue(key);
			if(item.isDualKey()){
				String subValue = mPropManager.getValue(item.getSubKey());
				value += "_"+subValue;
			}
			item.setValue(value);
			//item.setModified(false);
		}
		checkForGooglePlay();
	}

	private boolean checkContainsInvalid(String value){
		Pattern p = Pattern.compile("([^-\\w])");
		Matcher m = p.matcher(value);
		return m.find();
	}

	private void checkForGooglePlay(){
		//check ro.product.model && ro.product.device
		if(checkContainsInvalid(mPropManager.getValue("ro.product.model")) || checkContainsInvalid(mPropManager.getValue("ro.product.device"))){
			JOptionPane.showMessageDialog(null, "机型名称和设备名称包含非字母数字以外的特殊字符或者空格很可能导致谷歌商店用不了，请务必验证谷歌商店！！！");
		}
	}

	private void initProps(){		
		mPropSets.add(new ListPropItemView("persist.sys.timezone","默认时区", MainView.getBounds(0, 0, 1, PROP_ITEM_COLS),CFG_XML_PATH_TIMEZONE));
		ListPropItemView item = new ListPropItemView("ro.product.locale.language","默认语言", MainView.getBounds(0, PROP_ITEM_COLS+1, 1, PROP_ITEM_COLS),CFG_XML_PATH_LANGUAGE);
		item.setSubKey("ro.product.locale.region");
		mPropSets.add(item);
		
		mPropSets.add(new TextPropItemView("ro.product.model","机型名称", MainView.getBounds(1, 0, 1, PROP_ITEM_COLS/2)));
		mPropSets.add(new TextPropItemView("ro.product.brand","品牌名称", MainView.getBounds(1, PROP_ITEM_COLS/2+1, 1, PROP_ITEM_COLS/2)));
		mPropSets.add(new TextPropItemView("ro.product.manufacturer","制造商", MainView.getBounds(2, 0, 1, PROP_ITEM_COLS/2)));
		mPropSets.add(new TextPropItemView("bluetooth.HostName","蓝牙名称", MainView.getBounds(2, PROP_ITEM_COLS/2+1, 1, PROP_ITEM_COLS/2)));
		mPropSets.add(new TextPropItemView("ro.product.device","设备名称", MainView.getBounds(3, 0, 1, PROP_ITEM_COLS/2)));
		mPropSets.add(new TextPropItemView("ro.build.display.id","版本号", MainView.getBounds(1, PROP_ITEM_COLS+1, 1, PROP_ITEM_COLS)));
		mPropSets.add(new TextPropItemView("ro.custom.build.version","自定版本", MainView.getBounds(2, PROP_ITEM_COLS+1, 1, PROP_ITEM_COLS)));
		mPropSets.add(new TextPropItemView("ro.ty.ums.label","磁盘名称", MainView.getBounds(3, PROP_ITEM_COLS/2+1, 1, PROP_ITEM_COLS/2)));
		mPropSets.add(new TextPropItemView("wlan.SSID","共享SSID", MainView.getBounds(4, 0, 1, PROP_ITEM_COLS/2)));
		mPropSets.add(new TextPropItemView("ro.ty.setting.brightness","默认亮度(%)", MainView.getBounds(4, PROP_ITEM_COLS/2+1, 1, PROP_ITEM_COLS/2)));
		mPropSets.add(new TextPropItemView("ro.sf.lcd_density","屏幕密度", MainView.getBounds(5, 0, 1, PROP_ITEM_COLS/2)));

		mPropSets.add(new TextPropItemView("ro.ty.storage.fakein","假内部存储(Gb)", MainView.getBounds(6, 0, 1, PROP_ITEM_COLS/2)));
		mPropSets.add(new TextPropItemView("ro.ty.storage.fakesd","假手机存储(Gb)", MainView.getBounds(6, PROP_ITEM_COLS/2+1, 1, PROP_ITEM_COLS/2)));
		mPropSets.add(new TextPropItemView("ro.ty.storage.fakeram","假正在运行(Gb)", MainView.getBounds(7, 0, 1, PROP_ITEM_COLS/2)));
		mPropSets.add(new TextPropItemView("ro.ty.setting.fake.androidver","假Android版本", MainView.getBounds(7, PROP_ITEM_COLS/2+1, 1, PROP_ITEM_COLS/2)));

		mPropSets.add(new TextPropItemView("ro.ty.browser.homepage","默认主页", MainView.getBounds(3, PROP_ITEM_COLS+1, 1, PROP_ITEM_COLS)));
		mPropSets.add(new TextPropItemView("ro.ty.default.ime","默认输入法", MainView.getBounds(4, PROP_ITEM_COLS+1, 1, PROP_ITEM_COLS)));
		mPropSets.add(new TextPropItemView("ro.ty.default.wallpaper","默认APK壁纸", MainView.getBounds(5, PROP_ITEM_COLS+1, 1, PROP_ITEM_COLS)));
		mPropSets.add(new TextPropItemView("ro.ty.code.sw_logo","LOGO切换指令", MainView.getBounds(6, PROP_ITEM_COLS+1, 1, PROP_ITEM_COLS)));

		mPropSets.add(new CheckPropItemView("ro.ty.launcher.bgtrans","主菜单背景透明", MainView.getBounds(7, PROP_ITEM_COLS+1, 1, PROP_ITEM_COLS/2)));
		mPropSets.add(new CheckPropItemView("ro.ty.lang.bysim","语言随SIM卡变化", MainView.getBounds(7, PROP_ITEM_COLS+1+PROP_ITEM_COLS/2+1, 1, PROP_ITEM_COLS/2)));
		mPropSets.add(new CheckPropItemView("ro.ty.pwrmenu.reboot.enable","关机菜单显示重启", MainView.getBounds(8, PROP_ITEM_COLS+1, 1, PROP_ITEM_COLS/2)));
		mPropSets.add(new CheckPropItemView("ro.ty.code.sw_logo.support","打开隐藏LOGO功能", MainView.getBounds(8, PROP_ITEM_COLS+1+PROP_ITEM_COLS/2+1, 1, PROP_ITEM_COLS/2)));

		mPropSets.add(new TextPropItemView("ro.ty.fake.cpu_model","假CPU型号", MainView.getBounds(8, 0, 1, PROP_ITEM_COLS/2)));
		mPropSets.add(new TextPropItemView("ro.ty.fake.cpu_cores","假CPU核数", MainView.getBounds(8, PROP_ITEM_COLS/2+1, 1, PROP_ITEM_COLS/2)));
		mPropSets.add(new TextPropItemView("ro.ty.fake.cpu_freq","假CPU频率", MainView.getBounds(9, 0, 1, PROP_ITEM_COLS/2)));
		mPropSets.add(new TextPropItemView("ro.ty.fake.resolution","假分辨率", MainView.getBounds(9, PROP_ITEM_COLS/2+1, 1, PROP_ITEM_COLS/2)));
		mPropSets.add(new CheckPropItemView("ro.ty.fake.4g","假4G信号", MainView.getBounds(9, PROP_ITEM_COLS+1, 1, PROP_ITEM_COLS/2)));

		mPropSets.add(new ListPropItemView("ro.ty.camera.picture_size.front","前摄像素", MainView.getBounds(10, 0, 1, PROP_ITEM_COLS/2),CFG_XML_PATH_PICSIZE));
		mPropSets.add(new ListPropItemView("ro.ty.camera.picture_size.back","后摄像素", MainView.getBounds(10, PROP_ITEM_COLS/2+1, 1, PROP_ITEM_COLS/2),CFG_XML_PATH_PICSIZE));

		mPropSets.add(new TextPropItemView("ro.ty.setting.vol_percent","默认音量(%)", MainView.getBounds(5, PROP_ITEM_COLS/2+1, 1, PROP_ITEM_COLS/2)));

		mPropSets.add(new ListPropItemView("ro.ty.fontscale","字体大小", MainView.getBounds(10, PROP_ITEM_COLS+1, 1, PROP_ITEM_COLS/2),CFG_XML_PATH_FONTSIZE));
		mPropSets.add(new ListPropItemView("ro.ty.camera.preview_mode","照相预览", MainView.getBounds(11, 0, 1, PROP_ITEM_COLS/2),CFG_XML_PATH_CAMPREVMODE));
		mPropSets.add(new ListPropItemView("ro.ty.signal.fake_mode","假信号模式", MainView.getBounds(11, PROP_ITEM_COLS/2+1, 1, PROP_ITEM_COLS/2),CFG_XML_PATH_SIGNALFAKEMODE));

		mPropSets.add(new CheckPropItemView("ro.ty.feature.rm_vibrator","去掉马达功能", MainView.getBounds(9, PROP_ITEM_COLS+1+PROP_ITEM_COLS/2+1, 1, PROP_ITEM_COLS/2)));
		
		initPropViews(this);
	}
	
	private void initPropViews(JPanel panel){
		Iterator<PropItemView> iter = mPropSets.iterator();
		while(iter.hasNext()){
			PropItemView item = iter.next();
			item.addView(panel, 0, 0);
		}
	}
	
	private abstract class PropItemView{
		protected String mKey;
		protected String mSubKey;
		protected String mValue;
		protected String mLabel;
		protected Rectangle mRect;
		private boolean mIsDualKey;
		protected JComponent mJLabel;
		private int mState;
		private static final int STATE_IDLE = 0;
		private static final int STATE_MODIFIED = 1;
		private static final int STATE_SUCCESS = 2;
		private static final int STATE_FAILED = 3;
		
		public PropItemView(String key, String label, Rectangle rect){
			mKey = key;
			mLabel = label;
			mRect = rect;
			mState = STATE_IDLE;
		}
		
		public void setSubKey(String subKey){
			mSubKey = subKey;
			mIsDualKey = (mSubKey != null) && (mSubKey.length() > 0);
		}

		public String getKey(){
			return mKey;
		}

		public String getSubKey(){
			return mSubKey;
		}
		
		public void setValue(String value){
			mValue = value;
			markState(STATE_IDLE);
		}

		public void setEnable(boolean enable){
			JComponent componet = getEditComponet();
			if(componet != null){
				componet.setEnabled(enable);
			}
		}

		public boolean isDualKey(){
			return mIsDualKey;
		}

		private void markState(int state){
			if(mState == state){
				return;
			}
			mState = state;
			if(mJLabel == null){
				return;
			}
			if(mState == STATE_SUCCESS){
				mJLabel.setForeground(ComUtil.COLOR_MARK_MODIFIED);
			}else if(mState == STATE_FAILED){
				mJLabel.setForeground(ComUtil.COLOR_MARK_FAILED);
			}else{
				mJLabel.setForeground(ComUtil.COLOR_MARK_UNMODIFIED);
			}
		}

		public void setModified(boolean modified){
			mValue = getValue();
			markState(modified ? STATE_SUCCESS : STATE_IDLE );
		}

		public boolean isModified(){
			boolean modified = isRealModified();
			if(modified){
				markState(STATE_MODIFIED);
			}
			return modified;
		}

		public boolean isModifySuccessed(){
			return mState == STATE_SUCCESS;
		}		
		public abstract String getValue();
		public abstract void addView(JPanel panel, int x, int y);
		public abstract JComponent getEditComponet();
		public abstract boolean isRealModified();
	}
	
	private class TextPropItemView extends PropItemView{
		private JTextField mTextView;
		public TextPropItemView(String key, String label, Rectangle rect) {
			super(key, label, rect);
		}

		@Override
		public void addView(JPanel panel, int x, int y) {
			int labelColSize = MainView.PANEL_MAIN_COL_SIZE*PROP_ITEM_LABEL_COLS;
			mJLabel = new JLabel(mLabel);
			mJLabel.setBounds(mRect.x, mRect.y, labelColSize , mRect.height);
			panel.add(mJLabel);
			mTextView = new JTextField();
			mTextView.setBounds(mRect.x+labelColSize, mRect.y, mRect.width-labelColSize, mRect.height);
			panel.add(mTextView);
		}

		@Override
		public String getValue() {
			return mTextView.getText().toString().trim();
		}
		
		@Override
		public void setValue(String value){
			if(value == null || value.equals("null") 
				|| (value.equals("0") && (mKey.equals("ro.ty.storage.fakein") || mKey.equals("ro.ty.storage.fakesd")  || mKey.equals("ro.ty.storage.fakeram")))
				|| (value.equals("-1") && (mKey.equals("ro.ty.setting.vol_percent")))){
				value = "";
			}
			super.setValue(value);
			mTextView.setText(value);
		}

		@Override
		public JComponent getEditComponet() {
			return mTextView;
		}

		@Override
		public boolean isRealModified() {
			//Log.i("PropItemView, key="+mKey+",old="+mValue+",new="+mTextView.getText());
			return mValue != null ? !mValue.equals(mTextView.getText()) : false;
		}
	}
	
	private class CheckPropItemView extends PropItemView{
		private JCheckBox mJCheckBox;
		private boolean mIsSelected;
		public CheckPropItemView(String key, String label, Rectangle rect) {
			super(key, label, rect);
		}

		@Override
		public void addView(JPanel panel, int x, int y) {
			mJCheckBox = new JCheckBox(mLabel);
			mJCheckBox.setBounds(mRect.x, mRect.y, mRect.width, mRect.height);
			panel.add(mJCheckBox);
			mJLabel = mJCheckBox;
		}

		@Override
		public String getValue() {
			return mJCheckBox.isSelected() ? "1" : "0";
		}

		@Override
		public void setValue(String value){
			super.setValue(value);
			if(value == null){
				return;
			}
			if("false".equals(value)){
				mIsSelected = false;
			}else{
				mIsSelected = Integer.valueOf(value) != 0;
			}
			mJCheckBox.setSelected(mIsSelected);
		}

		@Override
		public JComponent getEditComponet() {
			return mJCheckBox;
		}

		@Override
		public boolean isRealModified() {
			//Log.i("PropItemView, key="+mKey+",old="+mIsSelected+",new="+mJCheckBox.isSelected());
			return mIsSelected^mJCheckBox.isSelected();
		}
		
		@Override
		public void setModified(boolean modified){
			super.setModified(modified);
			mIsSelected = mJCheckBox.isSelected();
		}
	}
	
	private class ListPropItemView extends PropItemView implements ActionListener{
		private Item mItem;
		private Vector<Item> mItems;
		private JComboBox mJComboBox;
		
		public ListPropItemView(String key, String label, Rectangle rect, String cfgXmlPath) {
			super(key, label, rect);
			mItem = new Item(null, null, null);
			PropXmlParser xmlParser = new PropXmlParser(cfgXmlPath);
			setItems(xmlParser.getItems());
		}

		public void setItems(Vector<Item> items){
			mItems = items;
		}

		@Override
		public void addView(JPanel panel, int x, int y) {
			int labelColSize = MainView.PANEL_MAIN_COL_SIZE*PROP_ITEM_LABEL_COLS;
			mJLabel = new JLabel(mLabel);
			mJLabel.setBounds(mRect.x, mRect.y, labelColSize , mRect.height);
			panel.add(mJLabel);
			mJComboBox = new JComboBox(mItems);
			mJComboBox.setBounds(mRect.x+labelColSize, mRect.y, mRect.width-labelColSize, mRect.height);
			panel.add(mJComboBox);

			mJComboBox.addActionListener(this);
		}

		@Override
		public String getValue() {
			return mItem.getId();
		}

		@Override
		public void setValue(String value){
			super.setValue(value);
			if(value == null){
				return;
			}
			int index = -1;
			Iterator<Item> iter = mItems.iterator();
			while(iter.hasNext()){
				Item item = iter.next();
				if(item.getId().equals(value)){
					index = Integer.valueOf(item.getIndex());
					break;
				}
			}
			if(index != -1){
				mJComboBox.setSelectedIndex(index);
				mItem = (Item)mJComboBox.getSelectedItem();
			}else{
				mItem.setId(value);//for language not in launguage.xml
			}
		}

		@Override
		public JComponent getEditComponet() {
			return mJComboBox;
		}

		@Override
		public void actionPerformed(ActionEvent e){
			mItem = (Item) ((JComboBox) e.getSource()).getSelectedItem();
		}

		@Override
		public boolean isRealModified() {
			//Log.i("PropItemView, key="+mKey+",old="+mValue+",new="+mItem.getId());
			return mValue != null ? !mValue.equals(mItem.getId()) : false;
		}
	}
}
