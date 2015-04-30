package com.mtk.firmware;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.mtk.firmware.util.BinUtil;
import com.mtk.firmware.util.ComUtil;
import com.mtk.firmware.util.FileUtil;
import com.mtk.firmware.util.Log;
import com.mtk.firmware.util.PropManager;
import com.mtk.firmware.util.PropUtil;
import com.mtk.firmware.util.VerUpdate;

public class MainView extends JFrame implements ActionListener
{
	private static final long		serialVersionUID	= 1L;

	private JButton					execButton;
	private JTabbedPane				tabbedPane;
	private JPanel					progressPanel;
	private JProgressBar			current;

	private VerUpdate				verUpdate;
	private static FirmwarePanel	firmwarePanel;
	private static InfoPanel		infoPanel;
	private static MediaPanel		mediaPanel;
	private static AppPanel			appPanel;
	private static MainView			mainPanel;

	private Thread					pthread;
	private ProGressWork			exec				= null;

	public static final int PANEL_MAIN_WIDTH = 800;
	public static final int PANEL_MAIN_HEIGHT = 560;
	public static final int PANEL_MAIN_ROW_SIZE = 20;
	public static final int PANEL_MAIN_COL_SIZE = 15;
	public static final int PANEL_MAIN_PADDING = 10;
	public static final int PANEL_MAIN_ROW_PADDING = 20;

	private boolean mForceRepack;
	
	private MainView(){
		try{		
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		}
		catch (ClassNotFoundException e){		
			e.printStackTrace();
		}
		catch (InstantiationException e){		
			e.printStackTrace();
		}
		catch (IllegalAccessException e){		
			e.printStackTrace();
		}
		catch (UnsupportedLookAndFeelException e){		
			e.printStackTrace();
		}
		if (!isRightPath(ComUtil.OUT_DIR)){		
			JOptionPane.showMessageDialog(null, "错误:当前工具存放路径为:" + ComUtil.USER_DIR + "\n不能放在文件夹有中文或者有空格的路径下面,建议修改目录名为英文同时去掉空格，建议放在C,D盘根目录下使用!!\n");
			System.exit(1);
		}
		initComponent();
	}

	public synchronized static MainView getInstance(){	
		if (mainPanel == null){		
			mainPanel = new MainView();
		}
		return mainPanel;
	}

	private void initComponent(){
		verUpdate = new VerUpdate();
		firmwarePanel = FirmwarePanel.getInstance();
		add(firmwarePanel, BorderLayout.NORTH);

		tabbedPane = new JTabbedPane();
		tabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		infoPanel = InfoPanel.getInstance();
		mediaPanel = MediaPanel.getInstance();
		appPanel = AppPanel.getInstance();

		tabbedPane.addTab("基  本 信 息", infoPanel);
		tabbedPane.addTab("图片铃声视频", mediaPanel);
		tabbedPane.addTab("内 置 APK", appPanel);

		add(tabbedPane, BorderLayout.CENTER);

		execButton = new JButton("执行(请先备份！)");
		execButton.addActionListener(this);

		current = new JProgressBar(0, 100);
		progressPanel = new JPanel();
		progressPanel.add(execButton);
		progressPanel.add(current);
		add(progressPanel, BorderLayout.SOUTH);

		updateTabbedPane(false);
		setIconImage(Toolkit.getDefaultToolkit().createImage(MainView.class.getResource("/res/logo.png")));
		String verStr = "Ty Firmware Tool Ver." + verUpdate.getLocalVer();
		if(ComUtil.FAST_MODE){
			verStr += " fast";
		}
		setTitle(verStr);
		setResizable(false);
		setSize(PANEL_MAIN_WIDTH, PANEL_MAIN_HEIGHT);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		Log.i("UI init succeed!");
	}

	private boolean isRightPath(String path){	
		String regEx = "[\\u4e00-\\u9fa5]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(path);
		int count = 0;
		while (m.find())
		{
			for (int i = 0; i <= m.groupCount(); i++)
			{
				count = count + 1;
			}
		}
		if (count != 0)
		{
			return false;
		}
		else if (path.contains(" "))
		{
			return false;
		}
		return true;
	}

	public void updateTabbedPane(boolean bool){
		if(ComUtil.DEBUG_MODE){
			return;
		}
	
		//tabbedPane.setEnabledAt(0, bool);
		//tabbedPane.setEnabledAt(1, bool);
		//tabbedPane.setEnabledAt(2, bool);
		infoPanel.setEnabled(bool);
		mediaPanel.setEnabled(bool);
		appPanel.setEnabled(bool);
		execButton.setEnabled(bool);
	}

	public static Rectangle getBounds(int row, int col, int rows , int cols){
		Rectangle rect = new Rectangle(); 
		rect.x = PANEL_MAIN_PADDING + col*PANEL_MAIN_COL_SIZE;
		rect.y = PANEL_MAIN_PADDING + row*(PANEL_MAIN_ROW_SIZE+PANEL_MAIN_ROW_PADDING);
		rect.width = cols*PANEL_MAIN_COL_SIZE;
		rect.height = rows*PANEL_MAIN_ROW_SIZE;
		return rect;
	}


	@Override
	public void actionPerformed(ActionEvent e){	
		if (e.getSource() == execButton){
			mForceRepack = false;
			if(!isModified()){
				int option = JOptionPane.showConfirmDialog(null, "没啥好修改的，继续打包？", "提示", JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.NO_OPTION){
					return;
				}else{
					mForceRepack = true;
				}
			}
			if(!isModifiedValid() && !mForceRepack){
				return;
			}
			execButton.setEnabled(false);
			exec = new ProGressWork();
			exec.execute();
		}
	}

	class ProGressWork extends SwingWorker<List<Work>, Work>
	{
		List<Work>	list	= new ArrayList<Work>();

		private void updateProgress(final int value)
		{
			pthread = new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					for (int i = current.getValue(); i <= value; i++)
					{
						try
						{
							Thread.sleep(new Random().nextInt(600));
							Work w = new Work(i);
							list.add(w);
							publish(w);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
				}
			});
			pthread.start();
		}

		@Override
		protected List<Work> doInBackground() throws Exception
		{
			current.setValue(0);

			updateProgress(85);
			updateTabbedPane(false);
			execCommand();
			pthread.stop();
			for (int i = 85; i <= 100; i++)
			{
				current.setValue(i);
			}

			return list;
		}

		@Override
		protected void process(List<Work> works)
		{
			for (Work work : works)
			{
				current.setValue(work.getId());
			}
		}

		@Override
		protected void done(){		
			JOptionPane.showMessageDialog(MainView.this, "固件修改完成！！");
			current.setValue(0);
			updateTabbedPane(true);
			firmwarePanel.setFirmwarePanelEnable(true);
		}
	}

	class Work
	{
		private int	id;

		public Work(int id)
		{
			this.id = id;
		}

		public int getId()
		{
			return id;
		}
	}
	
	private void checkShutdownProp(){
		if(mediaPanel.isShutdownModified()){
			if(!PropManager.getInstance().isPorpExists("ro.operator.optr")){
				PropManager.insertBuildProp("ro.operator.optr", "CUST");
			}
		}
	}

	private boolean isSystemModified(){
		return infoPanel.isSystemModified() || mediaPanel.isSystemModified() || appPanel.isSystemModified();
	}

	private boolean isUserdataModified(){
		return appPanel.isUserdataModified();
	}

	private void finishModify(){
		infoPanel.finishModify();
		mediaPanel.finishModify();
		appPanel.finishModify();

		execButton.setEnabled(true);
	}

	private void execCommand(){	
		try{	
			Log.i("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<rom modify begin");
			infoPanel.doModify();
			mediaPanel.doModify();
			checkShutdownProp();
			appPanel.doModify();

			if(isUserdataModified()){
				firmwarePanel.repackUserdata();
			}
			
			if (mForceRepack || isSystemModified()){			
				firmwarePanel.repackSystem();
			}

			firmwarePanel.checksumGen();

			finishModify();
			Log.i(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>rom modify finished");
		}catch (Exception e){				
			e.printStackTrace();
		}
	}

	private boolean isModified(){
		return infoPanel.isModified() || mediaPanel.isModified() || appPanel.isModified();
	}

	private boolean isModifiedValid(){
		return infoPanel.isModifiedValid() && appPanel.isModifiedValid();
	}

	public static void main(String[] args){
		if (args.length == 1){		
			if (args[0].equals("tyd")){
				Log.setEnabled(true);
			}
		}
		if(!ComUtil.DEBUG_MODE){
			Log.setEnabled(true);
		}
		SwingUtilities.invokeLater(new Runnable(){		
			public void run(){			
				MainView.getInstance();
			}
		});
	}
}
