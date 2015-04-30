package com.mtk.firmware;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

import com.mtk.firmware.util.BinUtil;
import com.mtk.firmware.util.ComUtil;
import com.mtk.firmware.util.FileUtil;
import com.mtk.firmware.util.GenericFileFilter;
import com.mtk.firmware.util.Log;

public class AppPanel extends JPanel implements ActionListener, ItemListener
{
	private static final long	serialVersionUID	= 1L;
	private static AppPanel		aPanel;
	private static JRadioButton	cb_both;
	private static JRadioButton	cb_nre;
	private static JRadioButton	cb_nrm;
	private ButtonGroup			appHandle;
	private JButton				jb_add, jb_del;
	
	private static JList	mAddList;
	private DefaultListModel	 mAddModel;
	private String				lastPath;
	private JLabel mAddedSizeLabel;

	private JList mPackedList;
	private DefaultListModel mPackedModel;
	private JButton mPackedDel;
	private JLabel mAvailSizeLabel;

	private int mAvailSizeMb;
	private int mPackedSizeMb;

	private static final int LIST_ROW_NUM = 12;
	private static final int LIST_COL_NUM = 14;
	
	private static final int ADDPANEL_ROW_IDX = 2;
	private static final int ADDPANEL_COL_IDX = 1;
	
	private static final int PACKEDPANEL_ROW_IDX = ADDPANEL_ROW_IDX;
	private static final int PACKEDPANEL_COL_IDX = ADDPANEL_COL_IDX+LIST_COL_NUM+6;

	private static final int ACTION_BTN_ROW_IDX = ADDPANEL_ROW_IDX+LIST_ROW_NUM;
	private static final int ACTION_BTN_ROW_NUM = 1;
	private static final int ACTION_BTN_COL_NUM = 3;

	private static final int MODE_SEL_ROW_IDX = ACTION_BTN_ROW_IDX+ACTION_BTN_ROW_NUM;
	private static final int MODE_SEL_COL_IDX = ADDPANEL_COL_IDX;
	private static final int MODE_SEL_ROW_NUM = 2;
	private static final int MODE_SEL_COL_NUM = 15;

	private static final int SIZE_HINT_ROW_IDX = MODE_SEL_ROW_IDX+MODE_SEL_ROW_NUM;
	private static final int SIZE_HINT_COL_IDX = ADDPANEL_COL_IDX;

	private static final int APK_ITEM_VENDOR = 0; //can remove & recovery
	private static final int APK_ITEM_SYSTEM = 1; //cannot remove
	private static final int APK_ITEM_DATA = 2; //cannot recovery

	private class GridLayout{
		private int mRowSize;
		private int mColSize;
		
		public GridLayout(int rowSize, int colSize){
			mRowSize = rowSize;
			mColSize = colSize;
		}

		public Rectangle getBounds(int row, int col, int rows , int cols){
			Rectangle rect = new Rectangle(); 
			rect.x = 10 + col*mColSize;
			rect.y = 10 + row*mRowSize;
			rect.width = cols*mColSize;
			rect.height = rows*mRowSize;
			return rect;
		}
	}

	private AppPanel(){
		setLayout(null);

		GridLayout grid = new GridLayout(20,20);

		//JLabel addLabel = new JLabel("未添加");
		JLabel addLabel = new JLabel("预置APK");
		addLabel.setBounds(grid.getBounds(0, ADDPANEL_COL_IDX, 2, 10));
		add(addLabel);
		mAddList = new JList();
		mAddList.setModel(new DefaultListModel());
		mAddModel = (DefaultListModel) mAddList.getModel();
		mAddList.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		JScrollPane addPanel = new JScrollPane(mAddList);
		addPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.white));
		addPanel.setBounds(grid.getBounds(ADDPANEL_ROW_IDX, ADDPANEL_COL_IDX, LIST_ROW_NUM, LIST_COL_NUM));
		add(addPanel);
		jb_add = new JButton("增加");
		jb_add.addActionListener(this);
		jb_add.setBounds(grid.getBounds(ACTION_BTN_ROW_IDX, ADDPANEL_COL_IDX, ACTION_BTN_ROW_NUM, ACTION_BTN_COL_NUM));
		add(jb_add);
		jb_del = new JButton("删除");
		jb_del.addActionListener(this);
		jb_del.setBounds(grid.getBounds(ACTION_BTN_ROW_IDX, ADDPANEL_COL_IDX+ACTION_BTN_COL_NUM+2, ACTION_BTN_ROW_NUM, ACTION_BTN_COL_NUM));
		add(jb_del);

		JLabel packedLabel = new JLabel("已添加");
		packedLabel.setBounds(grid.getBounds(0, PACKEDPANEL_COL_IDX, 2, 10));
		add(packedLabel);
		mPackedList = new JList();
		mPackedList.setModel(new DefaultListModel());
		mPackedModel = (DefaultListModel) mPackedList.getModel();
		JScrollPane packedPanel = new JScrollPane(mPackedList);
		packedPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));
		packedPanel.setBounds(grid.getBounds(PACKEDPANEL_ROW_IDX, PACKEDPANEL_COL_IDX, LIST_ROW_NUM, LIST_COL_NUM));
		add(packedPanel);
		mPackedDel = new JButton("删除");
		mPackedDel.addActionListener(this);
		mPackedDel.setBounds(grid.getBounds(ACTION_BTN_ROW_IDX, PACKEDPANEL_COL_IDX, ACTION_BTN_ROW_NUM, ACTION_BTN_COL_NUM));
		//add(mPackedDel);

		appHandle = new ButtonGroup();
		cb_both = new JRadioButton("可恢复可卸载", true);
		cb_both.addItemListener(this);
		cb_nrm = new JRadioButton("不可卸载", false);
		cb_nrm.addItemListener(this);
		cb_nre = new JRadioButton("可卸载不可恢复", false);
		cb_nre.addItemListener(this);
		appHandle.add(cb_both);
		appHandle.add(cb_nrm);
		appHandle.add(cb_nre);
		JPanel btnsPanel = new JPanel();
		btnsPanel.add(cb_both);
		btnsPanel.add(cb_nrm);
		btnsPanel.add(cb_nre);
		btnsPanel.setBounds(grid.getBounds(MODE_SEL_ROW_IDX, MODE_SEL_COL_IDX, MODE_SEL_ROW_NUM, MODE_SEL_COL_NUM));
		add(btnsPanel);

		JLabel mAvailLabel = new JLabel("可添加大小：");
		mAvailLabel.setBounds(grid.getBounds(SIZE_HINT_ROW_IDX, SIZE_HINT_COL_IDX, 2, 4));
		add(mAvailLabel);
		mAvailSizeLabel = new JLabel("      ");
		mAvailSizeLabel.setBounds(grid.getBounds(SIZE_HINT_ROW_IDX, SIZE_HINT_COL_IDX+4, 2, 4));
		add(mAvailSizeLabel);
		JLabel mAddedLabel = new JLabel("已添加大小：");
		mAddedLabel.setBounds(grid.getBounds(SIZE_HINT_ROW_IDX, SIZE_HINT_COL_IDX+8, 2, 4));
		add(mAddedLabel);
		mAddedSizeLabel = new JLabel("      ");
		mAddedSizeLabel.setBounds(grid.getBounds(SIZE_HINT_ROW_IDX, SIZE_HINT_COL_IDX+12, 2, 4));
		add(mAddedSizeLabel);

		int size = getListSize();
		setDynamicSize(size);
	}

	public synchronized static AppPanel getInstance(){	
		if (aPanel == null){		
			aPanel = new AppPanel();
		}
		return aPanel;
	}

	public void onRomLoaded(){
		if(mAddModel != null){
			mAddModel.removeAllElements();
		}
		if(mPackedModel != null){
			mPackedModel.removeAllElements();
		}
		if(cb_both != null){
			cb_both.setSelected(true);
		}

		mAvailSizeMb = FirmwarePanel.getInstance().getAvailSizeMb();
		mAvailSizeLabel.setText(mAvailSizeMb+"M");
		setDynamicSize(0);
	}

	public static int getApkItem(){	
		if (cb_both.isSelected())
			return APK_ITEM_VENDOR;
		else if (cb_nrm.isSelected())
			return APK_ITEM_SYSTEM;
		else if (cb_nre.isSelected())
			return APK_ITEM_DATA;
		return -1;
	}

	public static int getListSize(){	
		int filecount = mAddList.getModel().getSize();
		long total = 0;
		for (int i = 0; i < filecount; i++){
			try{
			
				File f = (File) mAddList.getModel().getElementAt(i);
				total += FileUtil.getFileSizes(f);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		return FileUtil.FormatFileSize(total);
	}

	public File[] getApkList(){
		int filecount = mAddList.getModel().getSize();
		File[] files = new File[filecount];
		for (int i = 0; i < filecount; i++){		
			try{
				File f = (File) mAddList.getModel().getElementAt(i);
				files[i] = f;
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		return files;
	}

	public boolean setDynamicSize(int size){
		if (size > mAvailSizeMb){		
			mAddedSizeLabel.setForeground(Color.RED);
			mAddedSizeLabel.setText(size + "M  ");
			return false;
		}else{
			mAddedSizeLabel.setForeground(Color.GREEN);
			mAddedSizeLabel.setText(size + "M  ");
			return true;
		}
	}

	private HashMap<String, String> mWaitCopyLibs = new HashMap<String, String>();
	public long getLibsSize(){
		mWaitCopyLibs.clear();
		
		int apkItem = getApkItem();
		if(apkItem != APK_ITEM_SYSTEM){
			return 0;
		}
		
		long libsSize = 0;
		String tmpApkLibDir = ComUtil.pathConcat(ComUtil.OUT_DIR,"apklib");
		ComUtil.mkTempDir(tmpApkLibDir);
		
		String armeabiDir = ComUtil.pathConcat("lib","armeabi");
		String armeabiV7Dir = ComUtil.pathConcat("lib","armeabi-v7a");
		String armeabiPath = ComUtil.pathConcat(tmpApkLibDir,armeabiDir);
		String armeabiV7Path = ComUtil.pathConcat(tmpApkLibDir,armeabiV7Dir);
		File[] apkFileList = getApkList();
		for(File apkFile : apkFileList){
			String srcPath = apkFile.getAbsolutePath();
			BinUtil.unzipExtract(srcPath, ComUtil.pathConcat(armeabiV7Dir,"*"), tmpApkLibDir);
			if(! new File(armeabiV7Path).exists()){
				BinUtil.unzipExtract(srcPath, ComUtil.pathConcat(armeabiDir,"*"), tmpApkLibDir);
			}
		}
		
		String[] libPaths = new String[]{armeabiV7Path,armeabiPath};
		for(String libPath : libPaths){
			String[] libList = new File(libPath).list();
			if(libList != null){
				for(String lib : libList){
					if(mWaitCopyLibs.containsKey(lib)){
						continue;
					}
					String libFilePath = ComUtil.pathConcat(libPath,lib);
					File libFile = new File(libFilePath);
					if(libFile.isDirectory()){
						continue;
					}
					libsSize += libFile.length();
					mWaitCopyLibs.put(lib,libFilePath);
				}
			}
		}
		return libsSize;
	}

	public boolean isModifiedValid(){
		if(ComUtil.DEBUG_MODE){
			return true;
		}
		int apksSize = getListSize();
		int addSize = apksSize+FileUtil.FormatFileSize(getLibsSize());
				
		if(!setDynamicSize(addSize)){
			if(apksSize > mAvailSizeMb){
				JOptionPane.showMessageDialog(null, "APK容量超过允许大小，只允许添加" + mAvailSizeMb + "M,请删除部分APK,或者修改分区！！");
			}else{
				JOptionPane.showMessageDialog(null, "不可卸载模式导致APK容量超过允许大小" + mAvailSizeMb + "M,请删除部分APK,或者修改模式！！");
			}
			return false;
		}
		mPackedSizeMb = addSize;
		return true;
	}

	public boolean isModified(){	
		return getApkList().length > 0;
	}

	public boolean isSystemModified(){
		boolean modified = getApkList().length > 0 && (getApkItem() == 0 || getApkItem() == 1);
		//Log.i("appPenel, isSystemModified="+modified);
		return modified;
	}

	public boolean isUserdataModified(){
		return getApkList().length > 0 && getApkItem() == 2;
	}

	public void doModify(){
		if(getApkList().length == 0){
			return;
		}

		String dstApkDir = null;
		int apkItem = getApkItem();
		if (apkItem == 0){
			dstApkDir = ComUtil.pathConcat(ComUtil.SYSTEM_DIR,"vendor","operator","app");
		}else if (apkItem == 1){
			dstApkDir = ComUtil.pathConcat(ComUtil.SYSTEM_DIR,"app");
		}else if (apkItem == 2){
			dstApkDir = ComUtil.pathConcat(ComUtil.USERDATA_DIR,"app");
			FirmwarePanel.getInstance().unpackUserData();
		}else{
			return;
		}

		if(!new File(dstApkDir).exists()){
			BinUtil.mkdir(dstApkDir);
		}
		File[] apkFileList = getApkList();
		for(File apkFile : apkFileList){
			UUID name = UUID.randomUUID();
			String srcPath = apkFile.getAbsolutePath();
			String dstPath = ComUtil.pathConcat(dstApkDir,name+".apk");
			BinUtil.copy(srcPath, dstPath);
		}
		
		if (apkItem == 1 && mWaitCopyLibs.size() > 0){
			Iterator<Map.Entry<String, String>> iter = mWaitCopyLibs.entrySet().iterator();
			while(iter.hasNext()){
				Map.Entry<String, String> entry = iter.next();
				BinUtil.copy(entry.getValue(),ComUtil.pathConcat(ComUtil.SYSTEM_DIR,"lib",entry.getKey()));
			}
		}
	}

	public void finishModify(){
		for(int i=0;i<mAddModel.size();i++){
			mPackedModel.addElement(mAddModel.getElementAt(i));
		}
		mAddModel.removeAllElements();
		
		mAvailSizeMb -= mPackedSizeMb;
		mAvailSizeLabel.setText(mAvailSizeMb+"M");
		setDynamicSize(0);
	}

	public void setEnabled(boolean bool){
		if(ComUtil.DEBUG_MODE){
			return;
		}				
		cb_both.setEnabled(bool);
		cb_nre.setEnabled(bool);
		cb_nrm.setEnabled(bool);
		jb_add.setEnabled(bool);
		jb_del.setEnabled(bool);
		mAddList.setEnabled(bool);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == jb_add)
		{
			JFileChooser fd;
			lastPath = MediaPanel.pref.get("lastPath", "");
			System.out.println("lastPath: " + lastPath);
			if (!lastPath.isEmpty())
			{
				fd = new JFileChooser(lastPath);
			}
			else
			{
				fd = new JFileChooser();
			}

			fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
			String imageExts[] = { "apk", "APK" };
			FileFilter filter = new GenericFileFilter(imageExts, "APK Files(*.apk;*.APK)");
			fd.setFileFilter(filter);
			fd.setMultiSelectionEnabled(true);
			int option = fd.showDialog(MainView.getInstance(), "选择APK");
			File[] files = fd.getSelectedFiles();
			if (option == JFileChooser.APPROVE_OPTION)
			{
				for (File f : files)
				{
					mAddModel.addElement(f);
				}
				int size = getListSize();
				setDynamicSize(size);
				MediaPanel.pref.put("lastPath", files[0].getPath());
			}

		}
		if (e.getSource() == jb_del)
		{
			mAddModel.removeElement(mAddList.getSelectedValue());
			int size = getListSize();
			setDynamicSize(size);
		}

	}

	@Override
	public void itemStateChanged(ItemEvent e)
	{
		int size = getListSize();
		setDynamicSize(size);
	}

}
