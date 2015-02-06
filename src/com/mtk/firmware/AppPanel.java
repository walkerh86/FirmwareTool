package com.mtk.firmware;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
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
	private JPanel				jp_app, jp_appbtn, jp_appHandle;
	private JButton				jb_add, jb_del;
	private JFileChooser		fd;
	private JScrollPane			panel;
	private static JList		jlist;
	private DefaultListModel	dlm;
	private String				lastPath;
	private static JLabel		jl_total;

	private static final int APK_ITEM_VENDOR = 0; //can remove & recovery
	private static final int APK_ITEM_SYSTEM = 1; //cannot remove
	private static final int APK_ITEM_DATA = 2; //cannot recovery

	private AppPanel()
	{
		jlist = new JList();
		jlist.setModel(new DefaultListModel());
		dlm = (DefaultListModel) jlist.getModel();
		jlist.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		panel = new JScrollPane(jlist);
		panel.setPreferredSize(new Dimension(300, 150));
		panel.setBorder(BorderFactory.createMatteBorder(2, 2, 5, 2, Color.white));

		setLayout(null);
		jp_app = new JPanel();
		jp_app.add(new JLabel("客制APK目录"));
		jp_app.add(panel);
		jp_app.setBounds(50, 5, 400, 150);
		add(jp_app);

		jp_appbtn = new JPanel(new GridLayout(3, 1));
		jb_add = new JButton("增加");
		jb_add.addActionListener(this);
		jb_del = new JButton("删除");
		jb_del.addActionListener(this);
		jp_appbtn.add(jb_add);
		jp_appbtn.add(new JLabel(""));
		jp_appbtn.add(jb_del);
		jp_appbtn.setBounds(480, 40, 70, 80);
		add(jp_appbtn);

		jp_appHandle = new JPanel();
		appHandle = new ButtonGroup();
		cb_both = new JRadioButton("", true);
		cb_both.addItemListener(this);
		cb_nrm = new JRadioButton("", false);
		cb_nrm.addItemListener(this);
		cb_nre = new JRadioButton("", false);
		cb_nre.addItemListener(this);
		appHandle.add(cb_both);
		appHandle.add(cb_nrm);
		appHandle.add(cb_nre);
		jl_total = new JLabel("      ");
		JLabel jl1 = new JLabel("总大小:");
		jl1.setToolTipText("添加APk的总大小，注意有大小限制，红色为超过容量大小，绿色为正常");
		jp_appHandle.add(jl1);
		jp_appHandle.add(jl_total);
		jp_appHandle.add(cb_both);
		JLabel jl2 = new JLabel("可恢复可卸载");
		jl2.setToolTipText("该选项为终端用户可以对APK进行卸载，且进行恢复出厂设置后这些卸载的APK会恢复");
		jp_appHandle.add(jl2);
		jp_appHandle.add(cb_nrm);
		JLabel jl3 = new JLabel("不可卸载");
		jl3.setToolTipText("该选项为终端用户不可对APK进行卸载");
		jp_appHandle.add(jl3);
		jp_appHandle.add(cb_nre);
		JLabel jl4 = new JLabel("可卸载不可恢复");
		jl4.setToolTipText("该选项为终端用户可以对APK进行卸载，进行恢复出厂设置后这些卸载的APK不会恢复");
		jp_appHandle.add(jl4);
		jp_appHandle.setBounds(0, 160, 500, 30);
		add(jp_appHandle);
		int size = getListSize();
		setDynamicSize(size);
	}

	public synchronized static AppPanel getInstance()
	{
		if (aPanel == null)
		{
			aPanel = new AppPanel();
		}
		return aPanel;
	}

	public static int getApkItem()
	{
		if (cb_both.isSelected())
			return APK_ITEM_VENDOR;
		else if (cb_nrm.isSelected())
			return APK_ITEM_SYSTEM;
		else if (cb_nre.isSelected())
			return APK_ITEM_DATA;
		return -1;
	}

	public static int getListSize()
	{
		int filecount = jlist.getModel().getSize();
		long total = 0;
		for (int i = 0; i < filecount; i++)
		{
			try
			{
				File f = (File) jlist.getModel().getElementAt(i);
				total += FileUtil.getFileSizes(f);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return FileUtil.FormatFileSize(total);
	}

	public File[] getApkList()
	{

		int filecount = jlist.getModel().getSize();
		File[] files = new File[filecount];
		for (int i = 0; i < filecount; i++)
		{
			try
			{
				File f = (File) jlist.getModel().getElementAt(i);
				files[i] = f;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return files;
	}

	public static boolean setDynamicSize(int size)
	{

		if (size > FirmwarePanel.allowSize)
		{
			jl_total.setForeground(Color.RED);
			jl_total.setText(size + "M  ");
			return false;
		}
		else
		{
			jl_total.setForeground(Color.GREEN);
			jl_total.setText(size + "M  ");
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
		int apksSize = getListSize();
		int addSize = apksSize+FileUtil.FormatFileSize(getLibsSize());
				
		if(!setDynamicSize(addSize)){
			if(apksSize > FirmwarePanel.allowSize){
				JOptionPane.showMessageDialog(null, "APK容量超过允许大小，只允许添加" + FirmwarePanel.allowSize + "M,请删除部分APK,或者修改分区！！");
			}else{
				JOptionPane.showMessageDialog(null, "不可卸载模式导致APK容量超过允许大小" + FirmwarePanel.allowSize + "M,请删除部分APK,或者修改模式！！");
			}
			return false;
		}
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

	public void setAppPanelEnable(boolean bool){
		cb_both.setEnabled(bool);
		cb_nre.setEnabled(bool);
		cb_nrm.setEnabled(bool);
		jb_add.setEnabled(bool);
		jb_del.setEnabled(bool);
		jlist.setEnabled(bool);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == jb_add)
		{
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
			fd.addChoosableFileFilter(filter);
			fd.setMultiSelectionEnabled(true);
			int option = fd.showDialog(MainView.getInstance(), "选择APK");
			File[] files = fd.getSelectedFiles();
			if (option == JFileChooser.APPROVE_OPTION)
			{
				for (File f : files)
				{
					dlm.addElement(f);
				}
				int size = getListSize();
				setDynamicSize(size);
				MediaPanel.pref.put("lastPath", files[0].getPath());
			}

		}
		if (e.getSource() == jb_del)
		{
			dlm.removeElement(jlist.getSelectedValue());
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
