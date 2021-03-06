package com.mtk.firmware;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;

import com.mtk.firmware.util.BinUtil;
import com.mtk.firmware.util.ComUtil;
import com.mtk.firmware.util.FileUtil;
import com.mtk.firmware.util.Log;
import com.mtk.firmware.util.PropManager;

public class FirmwarePanel extends JPanel implements ActionListener
{
	private static final long		serialVersionUID	= 1L;
	private static JTextField		firmwarePath;
	private JButton					selectButton;
	public static int				allowSize;
	private static int				systemSize;
	private static FirmwarePanel	fPanel;
	public static JProgressBar		current;
	public static JFrame			progressFrame;
	public static String			rompath;

	public static final String	SYSTEM_IMGEXT = ComUtil.pathConcat(ComUtil.OUT_DIR,"system.img.ext4");
	public static final String SYSTEM_DIR = ComUtil.pathConcat(ComUtil.OUT_DIR,"system");
	public static final String FRAMEWORK_RES_APK = ComUtil.pathConcat(SYSTEM_DIR,"framework","framework-res.apk");
	public static final String	USERDATA_IMGEXT = ComUtil.pathConcat(ComUtil.OUT_DIR,"userdata.img.ext4");
	public static final String USERDATA_DIR = ComUtil.pathConcat(ComUtil.OUT_DIR,"data");

	private String mSysImgPath;
	private String mDataImgPath;
	private int mSysImgSizeMb;
	private int mDataImgSizeMb;

	private FirmwarePanel()
	{
		mSysImgSizeMb = 0;
		
		progressFrame = new JFrame();
		progressFrame.setTitle("正在回读升级包");
		progressFrame.setSize(600, 70);
		progressFrame.setResizable(false);
		progressFrame.setVisible(false);
		progressFrame.setLocationRelativeTo(null);
		progressFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		progressFrame.setIconImage(Toolkit.getDefaultToolkit().createImage(MainView.class.getResource("/res/logo.png")));
		progressFrame.setAlwaysOnTop(true);
		current = new JProgressBar(0, 100);
		current.setIndeterminate(false);
		progressFrame.add(current);
		firmwarePath = new JTextField(72);
		firmwarePath.setFocusable(false);
		selectButton = new JButton("浏览..");
		selectButton.addActionListener(this);
		JLabel jl = new JLabel("固件目录");
		jl.setToolTipText("请选择要修改的固件目录，此处只能选择目录");
		firmwarePath.setToolTipText("请选择要修改的固件目录，此处只能选择目录");
		add(jl);
		add(firmwarePath);
		add(selectButton);
	}

	public synchronized static FirmwarePanel getInstance()
	{
		if (fPanel == null){		
			fPanel = new FirmwarePanel();
			if(ComUtil.DEBUG_MODE){
				fPanel.setFirmwarePath("E:\\release\\1TWD_NEUTRAL\\10.1\\K0708_V1.0.3_170220_JD9366_512M");
				fPanel.setAndroidSize();
			}
		}
		return fPanel;
	}

	public String getFirmwarePath()
	{
		return firmwarePath.getText().toString().trim();
	}

	public void setFirmwarePath(String path)
	{
		rompath = path;
		firmwarePath.setText(path);

		if(ComUtil.FAST_MODE){
			mSysImgPath = ComUtil.pathConcat(rompath,"system.img");
		}else{
			mSysImgPath = ComUtil.pathConcat(ComUtil.OUT_DIR,"system.img");
		}

		if(ComUtil.FAST_MODE){
			mDataImgPath = ComUtil.pathConcat(rompath,"userdata.img");
		}else{
			mDataImgPath = ComUtil.pathConcat(ComUtil.OUT_DIR,"userdata.img");
		}
	}

	public void setAndroidSize()
	{
		BufferedReader br = null;
		String read;
		File f = new File(getFirmwarePath());
		File scatterFile = null;
		long size = 0;
		for (File i : f.listFiles())
		{
			if (i.getName().indexOf("scatter") != -1)
			{
				scatterFile = i;
			}
		}

		try
		{
			br = new BufferedReader(new InputStreamReader(new FileInputStream(scatterFile)));
			boolean s = false;
			while ((read = br.readLine()) != null)
			{
				if ((read.indexOf("ANDROID")) != -1)
				{
					s = true;
				}
				if ((read.indexOf("partition_size") != -1) && s)
				{
					size = Long.valueOf(read.split("0x")[1].replace(" ", ""), 16);
					break;
				}
			}
			br.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		systemSize = (int)(size / 1024L / 1024L);
		mSysImgSizeMb = systemSize;
	}

	private boolean isFirmwareDir(File file)
	{
		for (String s : file.list())
		{
			if (s.equals("system.img") || s.equals("userdata.img") || s.equals("logo.bin"))
			{
				return true;
			}
		}

		return false;
	}

	public void setFirmwarePanelEnable(boolean bool)
	{
		firmwarePath.setEditable(bool);
		selectButton.setEnabled(bool);
		setEnabled(bool);
	}

	private boolean romCheck(String sysDir){
		if(ComUtil.ROM_AUTH_DISABLE){ 
			PropManager mPropManager = PropManager.getInstance();
			mPropManager.loadProps();
			return true;
		}
		String tmpDir = ComUtil.pathConcat(ComUtil.OUT_DIR,"temp");
		BinUtil.rm(tmpDir);
		
		String checkFilePath = ComUtil.pathConcat("res","drawable","ltty_background_dark.xml");
		BinUtil.unzipExtractNoLog(FRAMEWORK_RES_APK, checkFilePath, tmpDir);
		File checkFile = new File(ComUtil.pathConcat(tmpDir,checkFilePath));
		if(!checkFile.exists()){
			System.out.print("romCheck fail1\n");
			BinUtil.rm(tmpDir);
			return false;
		}
		BinUtil.rm(tmpDir);
		
		checkFilePath = ComUtil.pathConcat(SYSTEM_DIR,"etc","ty-conf.conf");
		checkFile = new File(checkFilePath);
		if(!checkFile.exists()){
			System.out.print("romCheck fail2\n");
			return false;
		}
		
		PropManager mPropManager = PropManager.getInstance();
		mPropManager.loadProps();
		if(!mPropManager.isPorpSupport("ro.ty.auth")){
			System.out.print("romCheck fail3\n");
			return false;
		}

		return true;
	}

	class ProGressWork extends SwingWorker<List<Work>, Work>
	{
		List<Work>	list	= new ArrayList<Work>();
		boolean mRomValid = false;

		private void updateProgress(final int value)
		{
			new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					for (int i = current.getValue(); i <= value; i++)
					{
						try
						{
							Thread.sleep(new Random().nextInt(1000));
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
			}).start();
		}

		@Override
		protected List<Work> doInBackground() throws Exception
		{
			Log.i("====================================Begin readback system.img====================================");
			MainView.getInstance().updateTabbedPane(false);
			current.setValue(0);
			updateProgress(90);
			
			ComUtil.mkTempDir(ComUtil.OUT_DIR);
			unpackSystem();

			mRomValid = romCheck(ComUtil.SYSTEM_DIR);
			if(!mRomValid){
				BinUtil.rm(ComUtil.SYSTEM_DIR);
			}
			
			for (int i = 90; i <= 100; i++){			
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
		protected void done()
		{
			Log.i("====================================Readback system.img finish====================================");
			progressFrame.setVisible(false);
			if(!mRomValid){
				JOptionPane.showMessageDialog(null,"ROM非法");
				return;
			}
			InfoPanel.getInstance().onRomLoaded();
			MediaPanel.getInstance().onRomLoaded();
			AppPanel.getInstance().onRomLoaded();
			MainView.getInstance().updateTabbedPane(true);
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

	@Override
	public void actionPerformed(ActionEvent e)
	{
		JFileChooser fd = new JFileChooser();
		fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (e.getSource() == selectButton)
		{
			int option = fd.showDialog(MainView.getInstance(), "选择固件目录");
			File file = fd.getSelectedFile();
			if (option == JFileChooser.APPROVE_OPTION)
			{
				String path = file.getAbsolutePath();
				if(path.contains(" ")){
					JOptionPane.showMessageDialog(null, "固件目录路径中请不要带空格！");
					return;
				}
				if (isFirmwareDir(file))
				{
					try
					{
						setFirmwarePath(file.getAbsolutePath());
						setAndroidSize();
						Log.i("Firmware path:" + file.getAbsolutePath());
						Log.i("Android parttion size:" + systemSize + "M");
						int realsize = FileUtil.FormatFileSize(FileUtil.getFileSizes(new File(file.getAbsolutePath() + "\\system.img")));
						Log.i("systemimg size:" + realsize + "M");
						if (realsize <= 10)
						{
							JOptionPane.showMessageDialog(null, "固件目录下的system.img损坏,请查看大小是否为0KB！！！");
							Log.i("system.img损坏");
							return;
						}
						allowSize = systemSize - realsize;
						Log.i("Allow add apk size:" + allowSize + "M");
						new ProGressWork().execute();
						progressFrame.setVisible(true);
						MediaPanel.pref.put("last", file.getAbsolutePath());
						MediaPanel.pref.put("allowSize", allowSize + "");
					}
					catch (Exception e1)
					{
						Log.i(e1.toString());
					}
				}
				else
				{
					JOptionPane.showMessageDialog(null, "该目录不是固件目录！！！");
					Log.i("该目录不是固件目录！！！");
				}
			}
		}
	}

	public int getAvailSizeMb(){
		return allowSize;
	}

	public String getSystemImgPath(){
		return mSysImgPath;
	}

	public String getUserdataImgPath(){
		return mDataImgPath;
	}

	public String getLogoBinPath(){
		return ComUtil.pathConcat(rompath,"logo.bin");
	}

	public String getRomPath(){
		return rompath;
	}

	public void repackSystem(){
		BinUtil.rm(mSysImgPath);
		BinUtil.makeExt4Fs(mSysImgPath, SYSTEM_DIR, mSysImgSizeMb);
		if(!ComUtil.FAST_MODE){
			BinUtil.copy(mSysImgPath,ComUtil.pathConcat(rompath,"system.img"));
		}
	}

	public void repackUserdata(){
		BinUtil.rm(mDataImgPath);
		BinUtil.makeExt4Fs(mDataImgPath, USERDATA_DIR, mDataImgSizeMb);
		if(!ComUtil.FAST_MODE){
			BinUtil.copy(mDataImgPath,ComUtil.pathConcat(rompath,"userdata.img"));
		}
	}

	public void unpackUserData(){
		if(!ComUtil.FAST_MODE){
			BinUtil.copy(ComUtil.pathConcat(rompath,"userdata.img"),mDataImgPath);
		}
		BinUtil.simgToImg(getUserdataImgPath(), USERDATA_IMGEXT);
		BinUtil.mkdir(ComUtil.USERDATA_DIR);
		BinUtil.ext4(USERDATA_IMGEXT, ComUtil.USERDATA_DIR);
		mDataImgSizeMb = (int)FileUtil.getFileSizes(USERDATA_IMGEXT) / 1024 / 1024;
		BinUtil.rm(USERDATA_IMGEXT);
	}

	public void unpackSystem(){
		if(!ComUtil.FAST_MODE){
			BinUtil.copy(ComUtil.pathConcat(rompath,"system.img"),mSysImgPath);
		}
		BinUtil.simgToImg(mSysImgPath, SYSTEM_IMGEXT);
		BinUtil.mkdir(ComUtil.SYSTEM_DIR);
		BinUtil.ext4(SYSTEM_IMGEXT,ComUtil.SYSTEM_DIR);
		if(mSysImgSizeMb == 0){
			mSysImgSizeMb = (int)FileUtil.getFileSizes(SYSTEM_IMGEXT) / 1024 / 1024;
		}
		int realsize = FileUtil.FormatFileSize(FileUtil.getFileSizes(mSysImgPath));
		allowSize = mSysImgSizeMb - realsize;
		Log.i("Allow add apk size:" + allowSize + "M");
		BinUtil.rm(SYSTEM_IMGEXT);
	}

	public void checksumGen(){
		BinUtil.checksumGen(rompath);
	}
}
