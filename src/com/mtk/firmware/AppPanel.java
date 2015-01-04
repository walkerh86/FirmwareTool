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
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

import com.mtk.firmware.util.FileUtil;
import com.mtk.firmware.util.GenericFileFilter;

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
			return 0;
		else if (cb_nrm.isSelected())
			return 1;
		else if (cb_nre.isSelected())
			return 2;
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

		if (size > FirmwarePanel.allowSize && (getApkItem() == 0 || getApkItem() == 1))
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

	public boolean hasModify()
	{
		return getApkList().length > 0;
	}

	public void setAppPanelEnable(boolean bool)
	{
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
