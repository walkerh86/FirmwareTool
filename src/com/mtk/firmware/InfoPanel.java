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

import javax.swing.JCheckBox;

public class InfoPanel extends JPanel{
	private static final long	serialVersionUID	= 1L;
	private static InfoPanel	iPanel;

	private PropManager mPropManager;

	private static final LinkedHashSet<PropItemView> mPropModifiedSets = new LinkedHashSet<PropItemView>();
	private static final LinkedHashSet<PropItemView> mPropSets = new LinkedHashSet<PropItemView>();
	
	private static final int PROP_ITEM_COLS = 25;
	private static final int PROP_ITEM_LABEL_COLS = 6;

	private InfoPanel(){	
		setLayout(null);
		initProps();
	}

	public synchronized static InfoPanel getInstance(){
		if (iPanel == null){
			iPanel = new InfoPanel();
			if(ComUtil.DEBUG_MODE){
				iPanel.preload();
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
			}
		}
		mPropManager.save();
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

	public void preload(){
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
	}

	private void initProps(){		
		mPropSets.add(new TimeZoneListPropItemView("persist.sys.timezone","默认时区", MainView.getBounds(0, 0, 1, PROP_ITEM_COLS)));
		LanguageListPropItemView item = new LanguageListPropItemView("ro.product.locale.language","默认语言", MainView.getBounds(0, PROP_ITEM_COLS+1, 1, PROP_ITEM_COLS));
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
		mPropSets.add(new TextPropItemView("ro.ty.storage.fakein","假内部存储(Gb)", MainView.getBounds(5, 0, 1, PROP_ITEM_COLS/2)));
		mPropSets.add(new TextPropItemView("ro.ty.storage.fakesd","假手机存储(Gb)", MainView.getBounds(5, PROP_ITEM_COLS/2+1, 1, PROP_ITEM_COLS/2)));
		mPropSets.add(new TextPropItemView("ro.ty.storage.fakeram","假正在运行(Gb)", MainView.getBounds(6, 0, 1, PROP_ITEM_COLS/2)));
		mPropSets.add(new TextPropItemView("ro.ty.setting.fake.androidver","假Android版本", MainView.getBounds(6, PROP_ITEM_COLS/2+1, 1, PROP_ITEM_COLS/2)));
		mPropSets.add(new TextPropItemView("ro.ty.browser.homepage","默认主页", MainView.getBounds(3, PROP_ITEM_COLS+1, 1, PROP_ITEM_COLS)));
		mPropSets.add(new TextPropItemView("ro.ty.default.ime","默认输入法", MainView.getBounds(4, PROP_ITEM_COLS+1, 1, PROP_ITEM_COLS)));
		mPropSets.add(new TextPropItemView("ro.ty.default.wallpaper","默认APK壁纸", MainView.getBounds(5, PROP_ITEM_COLS+1, 1, PROP_ITEM_COLS)));

		mPropSets.add(new CheckPropItemView("ro.ty.launcher.bgtrans","主菜单背景透明", MainView.getBounds(6, PROP_ITEM_COLS+1, 1, PROP_ITEM_COLS/2)));
		mPropSets.add(new CheckPropItemView("ro.ty.lang.bysim","语言随SIM卡变化", MainView.getBounds(6, PROP_ITEM_COLS+1+PROP_ITEM_COLS/2+1, 1, PROP_ITEM_COLS/2)));
				
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
				|| (value.equals("0") && (mKey.equals("ro.ty.storage.fakein") || mKey.equals("ro.ty.storage.fakesd")  || mKey.equals("ro.ty.storage.fakeram")))){
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
			mIsSelected = Integer.valueOf(value) != 0;
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
		
		public ListPropItemView(String key, String label, Rectangle rect) {
			super(key, label, rect);
			mItem = new Item(null, null, null);
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

	private class TimeZoneListPropItemView extends ListPropItemView{
		private Timezones mTimezones;
		public TimeZoneListPropItemView(String key, String label, Rectangle rect) {
			super(key, label, rect);
			mTimezones = new Timezones();
			setItems(mTimezones.getTimezones());			
		}
	}

	private class LanguageListPropItemView extends ListPropItemView{
		private Language mLanguages;
		public LanguageListPropItemView(String key, String label, Rectangle rect) {
			super(key, label, rect);
			mLanguages = new Language();
			setItems(mLanguages.getLanguage());			
		}
	}
}
