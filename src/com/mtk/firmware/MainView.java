package com.mtk.firmware;

import java.awt.BorderLayout;
import java.awt.Image;
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

import com.mtk.firmware.util.FileUtil;
import com.mtk.firmware.util.VerUpdate;

public class MainView extends JFrame implements ActionListener
{
	private static final long		serialVersionUID	= 1L;

	private JButton					execButton;
	private JTabbedPane				tabbedPane;
	private JPanel					progressPanel;
	private JProgressBar			current;

	public final static String		USER_DIR			= System.getProperty("user.dir");
	public final static String		FILE_SEPARATOR		= System.getProperty("file.separator");

	public final static String		XBIN				= USER_DIR + FILE_SEPARATOR + "xbin";
	public final static String		HELPFILE			= " \"" + USER_DIR + FILE_SEPARATOR + "使用说明.pdf\" ";
	public final static String		TMP_DIR				= USER_DIR + FILE_SEPARATOR + "firmware";
	public final static String		LOGO_DIR			= TMP_DIR + FILE_SEPARATOR + "logo";
	public final static String		LOGO				= TMP_DIR + FILE_SEPARATOR + "logo" + FILE_SEPARATOR + "logo.bin";
	public final static String		SYSTEM_DIR			= TMP_DIR + FILE_SEPARATOR + "system";
	public final static String		USERDATA_DIR		= TMP_DIR + FILE_SEPARATOR + "data";
	public final static String		WALLPAPER_DIR		= TMP_DIR + FILE_SEPARATOR + "wallpaper" + FILE_SEPARATOR + "res" + FILE_SEPARATOR + "drawable-nodpi";
	public final static String		WALLPAPER_DEST		= "res" + FILE_SEPARATOR + "drawable-nodpi" + FILE_SEPARATOR + "default_wallpaper.jpg";
	public final static String		APP_DIR				= SYSTEM_DIR + FILE_SEPARATOR + "vendor" + FILE_SEPARATOR + "operator" + FILE_SEPARATOR + "app";

	public final static String		FRAMWORK_RES		= " \"" + SYSTEM_DIR + FILE_SEPARATOR + "framework" + FILE_SEPARATOR + "framework-res.apk\" ";
	public final static String		SYSTEMIMG			= " \"" + TMP_DIR + FILE_SEPARATOR + "system.img\" ";
	public final static String		USERDATAIMG			= " \"" + TMP_DIR + FILE_SEPARATOR + "userdata.img\" ";
	public final static String		SYSTEMIMGEXT		= " \"" + TMP_DIR + FILE_SEPARATOR + "system.img.ext4\" ";
	public final static String		USERDATAIMGEXT		= " \"" + TMP_DIR + FILE_SEPARATOR + "userdata.img.ext4\" ";
	public final static String		LOG					= USER_DIR + FILE_SEPARATOR + "log.txt";

	public final static String		BOOTLOGO_DIR		= TMP_DIR + FILE_SEPARATOR + "system" + FILE_SEPARATOR + "media" + FILE_SEPARATOR + "images";

	public final static String		LOCALPATH			= " \"" + USER_DIR + FILE_SEPARATOR + "checkUpdate.exe\" ";

	public final static String		CONVERT				= "\"" + XBIN + FILE_SEPARATOR + "convert.exe\" ";
	public final static String		BMP_TO_RAW			= "\"" + XBIN + FILE_SEPARATOR + "bmp_to_raw.exe\" ";
	public final static String		GREP				= "\"" + XBIN + FILE_SEPARATOR + "grep.exe\" ";
	public final static String		SED					= "\"" + XBIN + FILE_SEPARATOR + "sed.exe\" ";
	public final static String		COPY				= "\"" + XBIN + FILE_SEPARATOR + "cp.exe\" ";
	public final static String		RM					= "\"" + XBIN + FILE_SEPARATOR + "rm.exe\" ";
	public final static String		SIMG2IMG			= "\"" + XBIN + FILE_SEPARATOR + "simg2img.exe\" ";
	public final static String		MAKE_EXT4FS			= "\"" + XBIN + FILE_SEPARATOR + "make_ext4fs.exe\" ";
	public final static String		MKDIR				= "\"" + XBIN + FILE_SEPARATOR + "mkdir.exe\" ";
	public final static String		EXT4				= "\"" + XBIN + FILE_SEPARATOR + "ext4.exe\" ";
	public final static String		MV					= "\"" + XBIN + FILE_SEPARATOR + "mv.exe\" ";
	public final static String		UNZIP				= "\"" + XBIN + FILE_SEPARATOR + "unzip.exe\" ";
	public final static String		ZIP					= "\"" + XBIN + FILE_SEPARATOR + "zip.exe\" ";

	public static String			buildprop			= " \"" + TMP_DIR + FILE_SEPARATOR + "system" + FILE_SEPARATOR + "build.prop\" ";
	public static String			customconf			= " \"" + TMP_DIR + FILE_SEPARATOR + "system" + FILE_SEPARATOR + "etc" + FILE_SEPARATOR
																+ "custom.conf\" ";
	public final static String	LAUNCHER_RES		= " \"" + SYSTEM_DIR + FILE_SEPARATOR + "app" + FILE_SEPARATOR + "Launcher2.apk\" ";
	public final static String	LAUNCHER_WALLPAPER_DIR		= TMP_DIR + FILE_SEPARATOR + "launcher_wallpaper" + FILE_SEPARATOR + "res" + FILE_SEPARATOR + "drawable-nodpi";
	public final static String	LAUNCHER_WALLPAPER_DEST		= "res" + FILE_SEPARATOR + "drawable-nodpi" + FILE_SEPARATOR + "wallpaper_01.jpg";
	public final static String	LAUNCHER_WALLPAPER_SMALL_DEST		= "res" + FILE_SEPARATOR + "drawable-nodpi" + FILE_SEPARATOR + "wallpaper_01_small.jpg";
	
	public final static String TY_TEMP_DIR =TMP_DIR + FILE_SEPARATOR +"temp";
	public final static String TY_SYS_IMG_AUTHEN_FILE = "res"+FILE_SEPARATOR+"drawable"+FILE_SEPARATOR+"ltty_background_dark.xml";
	public final static String TY_SYS_IMG_AUTHEN_FILE_PATH = TY_TEMP_DIR+FILE_SEPARATOR+TY_SYS_IMG_AUTHEN_FILE;
	public final static String TY_SYS_IMG_AUTHEN_FILE2_PATH = SYSTEM_DIR+FILE_SEPARATOR+"etc"+FILE_SEPARATOR+"ty-conf.conf";

	public final static String	TYCONVERT = "\"" + XBIN + FILE_SEPARATOR + "TyConvert.exe\" ";

	public final static String	DATA_APP_DIR = USERDATA_DIR + FILE_SEPARATOR + "app";

	private static boolean			DEBUG				= false;
	public static FileWriter		logw;
	public static Date				date;
	private boolean					hasModify			= false;
	private boolean					cpSystem			= false;
	private boolean					cpUserdata			= false;
	private String					logo1_path, logo2_path;
	private String					bootanimationPath;

	private String					hide_logo1_path, hide_logo2_path;

	private VerUpdate				verUpdate;
	private static FirmwarePanel	firmwarePanel;
	private static InfoPanel		infoPanel;
	private static MediaPanel		mediaPanel;
	private static AppPanel			appPanel;
	private static MainView			mainPanel;

	private Thread					pthread;
	private File					logFile;
	private ProGressWork			exec				= null;

	private MainView()
	{
		try
		{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (UnsupportedLookAndFeelException e)
		{
			e.printStackTrace();
		}
		if (!isRightPath(TMP_DIR))
		{
			JOptionPane.showMessageDialog(null, "错误:当前工具存放路径为:" + USER_DIR + "\n不能放在文件夹有中文或者有空格的路径下面,建议修改目录名为英文同时去掉空格，建议放在C,D盘根目录下使用!!\n");
			System.exit(1);
		}
		initComponent();
	}

	public synchronized static MainView getInstance()
	{
		if (mainPanel == null)
		{
			mainPanel = new MainView();
		}
		return mainPanel;
	}

	private void initComponent()
	{
		try
		{
			if (DEBUG)
			{
				date = new Date();
				logFile = new File(LOG);
				if (logFile.exists())
				{
					logFile.delete();
				}
				logFile.createNewFile();
				logw = new FileWriter(logFile, true);
			}

			verUpdate = new VerUpdate();
			firmwarePanel = FirmwarePanel.getInstance();
			add(firmwarePanel, BorderLayout.NORTH);

			tabbedPane = new JTabbedPane();
			tabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
			infoPanel = InfoPanel.getInstance();
			mediaPanel = MediaPanel.getInstance();
			appPanel = AppPanel.getInstance();

			tabbedPane.addTab("基  本 信 息", infoPanel);
			tabbedPane.addTab("图  片 与  铃 声", mediaPanel);
			tabbedPane.addTab("内 置 APK", appPanel);

			add(tabbedPane, BorderLayout.CENTER);

			execButton = new JButton("执行");
			execButton.addActionListener(this);

			current = new JProgressBar(0, 100);
			progressPanel = new JPanel();
			progressPanel.add(execButton);
			progressPanel.add(current);
			add(progressPanel, BorderLayout.SOUTH);

			updateTabbedPane(false);
			setIconImage(Toolkit.getDefaultToolkit().createImage(MainView.class.getResource("/res/logo.png")));
			setTitle("Ty Firmware Tool Ver." + verUpdate.getLocalVer());
			setResizable(false);
			setSize(700, 560);
			setVisible(true);
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			setLocationRelativeTo(null);
			writeLog("UI init succeed!");
			// firmwarePanel.setFirmwarePath("C:\\Users\\ShiYouHui\\Desktop\\Test\\MT6572");

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private boolean isRightPath(String path)
	{
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

	public void updateTabbedPane(boolean bool)
	{
		tabbedPane.setEnabledAt(0, bool);
		tabbedPane.setEnabledAt(1, bool);
		tabbedPane.setEnabledAt(2, bool);
		infoPanel.setInfoPanelEnable(bool);
		mediaPanel.setMediaPanelEnable(bool);
		appPanel.setAppPanelEnable(bool);
		execButton.setEnabled(bool);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == execButton)
		{
			if (!AppPanel.setDynamicSize(AppPanel.getListSize()) && (AppPanel.getApkItem() == 0 || AppPanel.getApkItem() == 1))
			{
				JOptionPane.showMessageDialog(null, "APK容量超过允许大小，只允许添加" + FirmwarePanel.allowSize + "M,请删除部分APK,或者修改分区！！");
			}
			else
			{
				if (hasModify())
				{
					if (AppPanel.getApkItem() == 1)
					{
						if (!checkAppSize())
						{
							firmwarePanel.setFirmwarePanelEnable(true);
							return;
						}
					}
					exec = new ProGressWork();
					exec.execute();
				}
				else
				{
					JOptionPane.showMessageDialog(null, "你要修改什么呢？");
				}
			}
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
			if (hasModify)
			{
				updateProgress(85);
				updateTabbedPane(false);
				preEnv();
				execCommand();
				if (cpSystem)
				{
					cleanBuff(Runtime.getRuntime().exec(COPY + " " + SYSTEMIMG + " \"" + FirmwarePanel.rompath + "\""));
				}
				if (cpUserdata)
				{
					cleanBuff(Runtime.getRuntime().exec(COPY + " " + USERDATAIMG + " \"" + FirmwarePanel.rompath + "\""));
				}
				pthread.stop();
				for (int i = 85; i <= 100; i++)
				{
					current.setValue(i);
				}
			}
			else
			{
				JOptionPane.showMessageDialog(null, "请选择修改项目！！");
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
			if (hasModify)
			{
				JOptionPane.showMessageDialog(MainView.this, "固件修改完成！！");
				current.setValue(0);
				updateTabbedPane(true);
				firmwarePanel.setFirmwarePanelEnable(true);
			}
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

	public static void writeLog(String str)
	{
		try
		{
			if (DEBUG)
			{
				MainView.logw.write(MainView.date.toLocaleString() + "      " + str + "\n\n");
				MainView.logw.flush();
			}
			else
			{
				System.out.println(str);
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void preEnv()
	{
		hide_logo2_path = mediaPanel.getHideLogo2Path();
		if (!(logo2_path = mediaPanel.getLogo2Path()).equals("")
			|| !hide_logo2_path.equals("")
				|| infoPanel.hasModify()
				|| !mediaPanel.getBootanimationPath().equals("")
				|| (appPanel.getApkList().length > 0 && (AppPanel.getApkItem() == 1 || AppPanel.getApkItem() == 0) || !mediaPanel.getBaudio().equals("") || !mediaPanel
						.getWallpaper().equals("")))
		{
			cpSystem = true;
		}

		if ((appPanel.getApkList().length > 0) && (AppPanel.getApkItem() == 2))
		{
			cpUserdata = true;
			if (!new File(unwrapper(USERDATAIMG)).exists())
			{
				try
				{
					writeLog(COPY + " \"" + FirmwarePanel.rompath + "\\userdata.img\"" + wrapper(TMP_DIR));
					cleanBuff(Runtime.getRuntime().exec(COPY + " \"" + FirmwarePanel.rompath + "\\userdata.img\"" + wrapper(TMP_DIR)));
				}
				catch (IOException e)
				{
					JOptionPane.showMessageDialog(null, "拷贝userdata.img失败！！！");
					e.printStackTrace();
				}
			}
		}
	}

	private void makeLogoBin(int index)
	{
		Logo bin = new Logo(new File(LOGO));
		try
		{
			writeLog("logobin[1]: " + RM + "-rf" + wrapper(LOGO_DIR));
			cleanBuff(Runtime.getRuntime().exec(RM + "-rf" + wrapper(LOGO_DIR)));

			writeLog("logobin[2]: " + MKDIR + wrapper(LOGO_DIR));
			cleanBuff(Runtime.getRuntime().exec(MKDIR + wrapper(LOGO_DIR)));

			writeLog("logobin[3]: " + COPY + wrapper(FirmwarePanel.rompath + FILE_SEPARATOR + "logo.bin") + wrapper(LOGO_DIR));
			cleanBuff(Runtime.getRuntime().exec(COPY + wrapper(FirmwarePanel.rompath + FILE_SEPARATOR + "logo.bin") + wrapper(LOGO_DIR)));

			bin.unpack();

			if (index == 0)
			{
				writeLog("logobin[4]: " + CONVERT + wrapper(logo1_path) + wrapper(LOGO_DIR + FILE_SEPARATOR + "logo.bmp"));
				cleanBuff(Runtime.getRuntime().exec(CONVERT + wrapper(logo1_path) + wrapper(LOGO_DIR + FILE_SEPARATOR + "logo.bmp")));
				checkLogoImage(wrapper(LOGO_DIR + FILE_SEPARATOR + "logo.bmp"));
				writeLog("logobin[5]: " + BMP_TO_RAW + wrapper(LOGO_DIR + FILE_SEPARATOR + index + ".raw") + wrapper(LOGO_DIR + FILE_SEPARATOR + "logo.bmp"));
				cleanBuff(Runtime.getRuntime().exec(
						BMP_TO_RAW + wrapper(LOGO_DIR + FILE_SEPARATOR + index + ".raw") + wrapper(LOGO_DIR + FILE_SEPARATOR + "logo.bmp")));
			}
			else if (index == 39)
			{
				writeLog("logobin[4]: " + CONVERT + wrapper(hide_logo1_path) + wrapper(LOGO_DIR + FILE_SEPARATOR + "hide_logo1.bmp"));
				cleanBuff(Runtime.getRuntime().exec(CONVERT + wrapper(hide_logo1_path) + wrapper(LOGO_DIR + FILE_SEPARATOR + "hide_logo1.bmp")));
				checkLogoImage(wrapper(LOGO_DIR + FILE_SEPARATOR + "hide_logo1.bmp"));
				writeLog("logobin[5]: " + BMP_TO_RAW + wrapper(LOGO_DIR + FILE_SEPARATOR + index + ".raw") + wrapper(LOGO_DIR + FILE_SEPARATOR + "hide_logo1.bmp"));
				cleanBuff(Runtime.getRuntime().exec(
						BMP_TO_RAW + wrapper(LOGO_DIR + FILE_SEPARATOR + index + ".raw") + wrapper(LOGO_DIR + FILE_SEPARATOR + "hide_logo1.bmp")));
			}
			else if (index == 40)
			{
				writeLog("boot_logo[2]: " + BMP_TO_RAW + wrapper(LOGO_DIR + FILE_SEPARATOR + index + ".raw")
						+ wrapper(TMP_DIR + FILE_SEPARATOR + "boot_logo2.bmp"));
				cleanBuff(Runtime.getRuntime().exec(
						BMP_TO_RAW + wrapper(LOGO_DIR + FILE_SEPARATOR + index + ".raw") + wrapper(TMP_DIR + FILE_SEPARATOR + "boot_logo2.bmp")));
			}
			else
			{
				writeLog("boot_logo[2]: " + BMP_TO_RAW + wrapper(LOGO_DIR + FILE_SEPARATOR + index + ".raw")
						+ wrapper(TMP_DIR + FILE_SEPARATOR + "boot_logo.bmp"));
				cleanBuff(Runtime.getRuntime().exec(
						BMP_TO_RAW + wrapper(LOGO_DIR + FILE_SEPARATOR + index + ".raw") + wrapper(TMP_DIR + FILE_SEPARATOR + "boot_logo.bmp")));
			}

			bin.repack();

			writeLog("logobin[6]: " + COPY + wrapper(LOGO) + wrapper(FirmwarePanel.rompath));
			cleanBuff(Runtime.getRuntime().exec(COPY + wrapper(LOGO) + wrapper(FirmwarePanel.rompath)));

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void makeBootLogo()
	{
		try
		{
			writeLog("boot_logo[1]: " + CONVERT + wrapper(logo2_path) + wrapper(TMP_DIR + FILE_SEPARATOR + "boot_logo.bmp"));
			cleanBuff(Runtime.getRuntime().exec(CONVERT + wrapper(logo2_path) + wrapper(TMP_DIR + FILE_SEPARATOR + "boot_logo.bmp")));
			checkLogoImage(TMP_DIR + FILE_SEPARATOR + "boot_logo.bmp");
			writeLog("boot_logo[2]: " + BMP_TO_RAW + wrapper(BOOTLOGO_DIR + FILE_SEPARATOR + "boot_logo") + wrapper(TMP_DIR + FILE_SEPARATOR + "boot_logo.bmp"));
			cleanBuff(Runtime.getRuntime().exec(
					BMP_TO_RAW + wrapper(BOOTLOGO_DIR + FILE_SEPARATOR + "boot_logo") + wrapper(TMP_DIR + FILE_SEPARATOR + "boot_logo.bmp")));

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void makeBootLogo2()
	{
		try
		{
			writeLog("boot_logo[1]: " + CONVERT + wrapper(hide_logo2_path) + wrapper(TMP_DIR + FILE_SEPARATOR + "boot_logo2.bmp"));
			cleanBuff(Runtime.getRuntime().exec(CONVERT + wrapper(hide_logo2_path) + wrapper(TMP_DIR + FILE_SEPARATOR + "boot_logo2.bmp")));
			checkLogoImage(TMP_DIR + FILE_SEPARATOR + "boot_logo2.bmp");
			writeLog("boot_logo[2]: " + BMP_TO_RAW + wrapper(BOOTLOGO_DIR + FILE_SEPARATOR + "boot_logo2") + wrapper(TMP_DIR + FILE_SEPARATOR + "boot_logo2.bmp"));
			cleanBuff(Runtime.getRuntime().exec(
					BMP_TO_RAW + wrapper(BOOTLOGO_DIR + FILE_SEPARATOR + "boot_logo2") + wrapper(TMP_DIR + FILE_SEPARATOR + "boot_logo2.bmp")));

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void makeAnimation(int handle)
	{
		writeLog(">>>>>>>>>>>>>>>>Begin makeAnimation!!!");
		String destString = "";
		String srcString = "";
		int width = 0;
		int height = 0;
		boolean loop = false;
		String fps = "";
		if (handle == 0)
		{
			loop = mediaPanel.getBootLoopItem();
			fps = mediaPanel.getBootFps();
			destString = "bootanimation";
			srcString = bootanimationPath;
		}

		try
		{
			writeLog(RM + "-rf" + wrapper(TMP_DIR + FILE_SEPARATOR + destString));
			cleanBuff(Runtime.getRuntime().exec(RM + "-rf" + wrapper(TMP_DIR + FILE_SEPARATOR + destString)));

			writeLog("animation[1]: " + MKDIR + wrapper(TMP_DIR + FILE_SEPARATOR + destString));
			cleanBuff(Runtime.getRuntime().exec(MKDIR + wrapper(TMP_DIR + FILE_SEPARATOR + destString)));

			File[] src = new File(srcString).listFiles();
			String suffix = src[0].getName().substring(src[0].getName().lastIndexOf("."), src[0].getName().length());
			Image pic = javax.imageio.ImageIO.read(src[0]);
			width = pic.getWidth(null);
			height = pic.getHeight(null);
			Map<Double, String> srcmap = new HashMap<Double, String>();
			Map<Integer, String> destmap = new HashMap<Integer, String>();
			boolean delete = false;
			for (int i = 0; i < src.length; i++)
			{
				srcmap.put(Double.parseDouble(split(src[i])), srcString + FILE_SEPARATOR + split(src[i]) + suffix);
				if (!src[i].getName().equals(split(src[i]) + suffix))
				{
					delete = true;
					FileUtil.fileChannelCopy(src[i], new File(srcString + FILE_SEPARATOR + split(src[i]) + suffix));
				}
			}
			Object[] key = srcmap.keySet().toArray();
			Arrays.sort(key);

			for (int i = 0; i < src.length; i++)
			{
				DecimalFormat dig = new DecimalFormat("000");
				writeLog("animation[2]: " + CONVERT + wrapper(srcmap.get(key[i]))
						+ wrapper(TMP_DIR + FILE_SEPARATOR + destString + FILE_SEPARATOR + dig.format(i + 1) + ".png"));
				cleanBuff(Runtime.getRuntime().exec(
						CONVERT + wrapper(srcmap.get(key[i])) + wrapper(TMP_DIR + FILE_SEPARATOR + destString + FILE_SEPARATOR + dig.format(i + 1) + ".png")));
				checkLogoImage(wrapper(TMP_DIR + FILE_SEPARATOR + destString + FILE_SEPARATOR + dig.format(i + 1) + ".png"));
				if (delete)
				{
					new File(srcmap.get(key[i])).delete();
				}
				destmap.put(i, TMP_DIR + FILE_SEPARATOR + destString + FILE_SEPARATOR + dig.format(i + 1) + ".png");
			}

			writeLog("animation[3]: " + MKDIR + wrapper(TMP_DIR + FILE_SEPARATOR + destString + FILE_SEPARATOR + destString));
			cleanBuff(Runtime.getRuntime().exec(MKDIR + wrapper(TMP_DIR + FILE_SEPARATOR + destString + FILE_SEPARATOR + destString)));

			File descxt = new File(TMP_DIR + FILE_SEPARATOR + destString + FILE_SEPARATOR + destString + FILE_SEPARATOR + "desc.txt");
			if (descxt.exists())
			{
				descxt.delete();
			}
			FileWriter writer = new FileWriter(descxt, true);
			writer.write(width + " " + height + " " + fps + "\n");

			if (loop)
			{
				String part0 = TMP_DIR + FILE_SEPARATOR + destString + FILE_SEPARATOR + destString + FILE_SEPARATOR + "part0";
				String part1 = TMP_DIR + FILE_SEPARATOR + destString + FILE_SEPARATOR + destString + FILE_SEPARATOR + "part1";

				writeLog("animation[4]: " + MKDIR + wrapper(part0));
				cleanBuff(Runtime.getRuntime().exec(MKDIR + wrapper(part0)));

				int i = 0;
				for (; i < src.length; i++)
				{
					writeLog(MV + wrapper(destmap.get(i)) + wrapper(part0));
					cleanBuff(Runtime.getRuntime().exec(MV + wrapper(destmap.get(i)) + wrapper(part0)));
				}

				writeLog("animation[5]: " + MKDIR + wrapper(part1));
				cleanBuff(Runtime.getRuntime().exec(MKDIR + wrapper(part1)));

				writeLog(COPY + wrapper(part0 + FILE_SEPARATOR + getFileName(destmap.get(i - 1))) + wrapper(part1));
				cleanBuff(Runtime.getRuntime().exec(COPY + wrapper(part0 + FILE_SEPARATOR + getFileName(destmap.get(i - 1))) + wrapper(part1)));
			}
			else
			{
				int num = 0;
				int j = 0, k = 0, i = 0;
				for (; i < src.length; i++)
				{
					++num;
					j = (num - 1) % 20;
					k = (num - 1) / 20;
					if (j == 0)
					{
						writeLog(MKDIR + wrapper(TMP_DIR + FILE_SEPARATOR + destString + FILE_SEPARATOR + destString + FILE_SEPARATOR + "part" + k));
						cleanBuff(Runtime.getRuntime().exec(
								MKDIR + wrapper(TMP_DIR + FILE_SEPARATOR + destString + FILE_SEPARATOR + destString + FILE_SEPARATOR + "part" + k)));
					}

					writeLog(COPY + wrapper(destmap.get(i))
							+ wrapper(TMP_DIR + FILE_SEPARATOR + destString + FILE_SEPARATOR + destString + FILE_SEPARATOR + "part" + k));
					cleanBuff(Runtime.getRuntime().exec(
							COPY + wrapper(destmap.get(i))
									+ wrapper(TMP_DIR + FILE_SEPARATOR + destString + FILE_SEPARATOR + destString + FILE_SEPARATOR + "part" + k)));
				}

				writeLog(MKDIR + wrapper(TMP_DIR + FILE_SEPARATOR + destString + FILE_SEPARATOR + destString + FILE_SEPARATOR + "part" + (k + 1)));
				cleanBuff(Runtime.getRuntime().exec(
						MKDIR + wrapper(TMP_DIR + FILE_SEPARATOR + destString + FILE_SEPARATOR + destString + FILE_SEPARATOR + "part" + (k + 1))));

				writeLog(COPY + wrapper(destmap.get(i - 1))
						+ wrapper(TMP_DIR + FILE_SEPARATOR + destString + FILE_SEPARATOR + destString + FILE_SEPARATOR + "part" + (k + 1)));
				cleanBuff(Runtime.getRuntime().exec(
						COPY + wrapper(destmap.get(i - 1))
								+ wrapper(TMP_DIR + FILE_SEPARATOR + destString + FILE_SEPARATOR + destString + FILE_SEPARATOR + "part" + (k + 1))));

			}

			File[] floder = new File(TMP_DIR + FILE_SEPARATOR + destString + FILE_SEPARATOR + destString).listFiles();
			int i = 0;
			for (; i < floder.length; i++)
			{
				if (floder[i].isDirectory())
				{
					if (loop)
					{
						writer.write("p 0 0 " + floder[i].getName() + "\n");
					}
					else
					{
						if (floder[i].getName().contains(floder.length - 2 + ""))
						{
							writer.write("p 0 0 " + floder[i].getName() + "\n");
						}
						else
						{
							writer.write("p 1 0 " + floder[i].getName() + "\n");
						}
					}
				}
			}
			writer.close();

			writeLog("cmd /c cd /d" + wrapper(TMP_DIR + FILE_SEPARATOR + destString + FILE_SEPARATOR + destString) + "&" + ZIP + " -r -0" + wrapper(destString)
					+ "*");
			cleanBuff(Runtime.getRuntime().exec(
					"cmd /c cd /d" + wrapper(TMP_DIR + FILE_SEPARATOR + destString + FILE_SEPARATOR + destString) + "&" + ZIP + " -r -0" + wrapper(destString)
							+ "*"));

			writeLog(COPY + wrapper(TMP_DIR + FILE_SEPARATOR + destString + FILE_SEPARATOR + destString + FILE_SEPARATOR + destString + ".zip")
					+ wrapper(TMP_DIR + FILE_SEPARATOR + "system" + FILE_SEPARATOR + "media"));
			cleanBuff(Runtime.getRuntime().exec(
					COPY + wrapper(TMP_DIR + FILE_SEPARATOR + destString + FILE_SEPARATOR + destString + FILE_SEPARATOR + destString + ".zip")
							+ wrapper(TMP_DIR + FILE_SEPARATOR + "system" + FILE_SEPARATOR + "media")));

			writeLog(">>>>>>>>>>>>>>>>finish makeAnimation!!!");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private String getFileName(String path)
	{
		return path.substring(path.lastIndexOf("\\") + 1, path.length());
	}

	public static String wrapper(String string)
	{
		return " \"" + string + "\" ";
	}

	public static String unwrapper(String string)
	{
		return string.replace("\"", "").replace(" ", "");
	}

	protected String split(File f)
	{
		StringBuffer list = new StringBuffer();
		String s = f.getName().substring(0, f.getName().lastIndexOf("."));
		char[] cs = s.toCharArray();

		for (int i = 0; i < cs.length; i++)
		{
			char c = cs[i];
			if (Character.isDigit(c))
			{
				list.append(String.valueOf(c));
			}
		}

		return list.toString();
	}

	private boolean checkAppSize()
	{
		try
		{
			writeLog(RM + " -rf" + wrapper(TMP_DIR + FILE_SEPARATOR + "apktmp"));
			cleanBuff(Runtime.getRuntime().exec(RM + " -rf" + wrapper(TMP_DIR + FILE_SEPARATOR + "apktmp")));
			File f[] = appPanel.getApkList();

			writeLog(MKDIR + wrapper(TMP_DIR + FILE_SEPARATOR + "apktmp"));
			cleanBuff(Runtime.getRuntime().exec(MKDIR + wrapper(TMP_DIR + FILE_SEPARATOR + "apktmp")));

			int total = 0;
			File lib = null;
			for (int i = 0; i < f.length; i++)
			{
				cleanBuff(Runtime.getRuntime().exec(COPY + wrapper(f[i].getAbsolutePath()) + wrapper(TMP_DIR + FILE_SEPARATOR + "apktmp")));
				String apkname = f[i].getName().substring(0, f[i].getName().lastIndexOf("."));
				cleanBuff(Runtime.getRuntime().exec(
						UNZIP + " -on" + wrapper(f[i].getAbsolutePath()) + " -d " + wrapper(TMP_DIR + FILE_SEPARATOR + "apktmp" + FILE_SEPARATOR + apkname)));

				if (new File(TMP_DIR + FILE_SEPARATOR + "apktmp" + FILE_SEPARATOR + apkname + FILE_SEPARATOR + "lib").exists())
				{
					if ((lib = new File(TMP_DIR + FILE_SEPARATOR + "apktmp" + FILE_SEPARATOR + apkname + FILE_SEPARATOR + "lib" + FILE_SEPARATOR
							+ "armeabi-v7a")).exists())
					{
						writeLog(COPY
								+ wrapper(TMP_DIR + FILE_SEPARATOR + "apktmp" + FILE_SEPARATOR + apkname + FILE_SEPARATOR + "lib" + FILE_SEPARATOR
										+ "armeabi-v7a" + FILE_SEPARATOR + "*") + wrapper(TMP_DIR + FILE_SEPARATOR + "system" + FILE_SEPARATOR + "lib"));
					}
					else if ((lib = new File(TMP_DIR + FILE_SEPARATOR + "apktmp" + FILE_SEPARATOR + apkname + FILE_SEPARATOR + "lib" + FILE_SEPARATOR
							+ "armeabi")).exists())
					{
						writeLog(COPY
								+ wrapper(TMP_DIR + FILE_SEPARATOR + "apktmp" + FILE_SEPARATOR + apkname + FILE_SEPARATOR + "lib" + FILE_SEPARATOR + "armeabi"
										+ FILE_SEPARATOR + "*") + wrapper(TMP_DIR + FILE_SEPARATOR + "system" + FILE_SEPARATOR + "lib"));
					}
					System.out.print("apk size:" + AppPanel.getListSize() + "  lib size:" + FileUtil.FormatFileSize(FileUtil.getDirSize(lib)) + "\n");
					total += FileUtil.FormatFileSize(FileUtil.getDirSize(lib));
				}
			}
			if (AppPanel.getListSize() + total > FirmwarePanel.allowSize)
			{
				AppPanel.setDynamicSize(AppPanel.getListSize() + total);
				writeLog("选择不可卸载解压完毕后，APK容量超过允许的" + FirmwarePanel.allowSize + "M大小 ,请删除部分APK!!");
				JOptionPane.showMessageDialog(null, "选择不可卸载解压完毕后，APK容量超过允许的" + FirmwarePanel.allowSize + "M大小 ,请删除部分APK!!");
				return false;
			}
			else
			{
				writeLog(COPY + wrapper(lib.getAbsolutePath() + FILE_SEPARATOR + "*") + wrapper(TMP_DIR + FILE_SEPARATOR + "system" + FILE_SEPARATOR + "lib"));
				cleanBuff(Runtime.getRuntime().exec(
						COPY + wrapper(lib.getAbsolutePath() + FILE_SEPARATOR + "*") + wrapper(TMP_DIR + FILE_SEPARATOR + "system" + FILE_SEPARATOR + "lib")));
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}

	private void checkLogoImage(String imgFile){
		try{
			writeLog(TYCONVERT + " " + imgFile);
			cleanBuff(Runtime.getRuntime().exec(TYCONVERT + " " + imgFile));
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private void execCommand()
	{
		String model_name, btName, timezone, language, brand, device, manufacturer, density, version, cversion, ssid, ums, baudio, wallpaperpath;
		buildprop = buildprop.replace("\\", "/");
		customconf = customconf.replace("\\", "/");
		File userdataext4 = new File(unwrapper(USERDATAIMGEXT));
		File systemext4 = new File(unwrapper(SYSTEMIMGEXT));
		try
		{
			if (cpUserdata)
			{
				if (new File(TMP_DIR + "\\userdata.img").exists())
				{
					if (!userdataext4.exists())
					{
						writeLog(SIMG2IMG + " " + USERDATAIMG + " " + USERDATAIMGEXT);
						cleanBuff(Runtime.getRuntime().exec(SIMG2IMG + " " + USERDATAIMG + " " + USERDATAIMGEXT));
						writeLog(MKDIR + " " + wrapper(USERDATA_DIR));
						cleanBuff(Runtime.getRuntime().exec(MKDIR + " " + wrapper(USERDATA_DIR)));
						writeLog(EXT4 + " " + USERDATAIMGEXT + " " + wrapper(USERDATA_DIR));
						cleanBuff(Runtime.getRuntime().exec(EXT4 + " " + USERDATAIMGEXT + " " + wrapper(USERDATA_DIR)));
					}
				}
			}

			if (!(model_name = infoPanel.getModelName()).isEmpty())
			{
				writeLog("model_name: " + SED + " -i \"/^ro.product.model/s/=.*/=" + model_name + "/\" " + buildprop);
				cleanBuff(Runtime.getRuntime().exec(SED + " -i \"/^ro.product.model/s/=.*/=" + model_name + "/\" " + buildprop));
			}

			if (!(timezone = infoPanel.getTimezone()).equals("0"))
			{
				writeLog("timezone: " + SED + " -i \"/^persist.sys.timezone/s/=.*/=" + timezone.replace("/", "\\/") + "/\" " + buildprop);
				cleanBuff(Runtime.getRuntime().exec(SED + " -i \"/^persist.sys.timezone/s/=.*/=" + timezone.replace("/", "\\/") + "/\" " + buildprop));
			}

			if (!(language = infoPanel.getLanguage()).equals("0"))
			{
				writeLog("language[1]: " + SED + " -i \"/^ro.product.locale.language/s/=.*/=" + language.split("_")[0] + "/\" " + buildprop);
				cleanBuff(Runtime.getRuntime().exec(SED + " -i \"/^ro.product.locale.language/s/=.*/=" + language.split("_")[0] + "/\" " + buildprop));
				writeLog("language[2]: " + SED + " -i \"/^ro.product.locale.region/s/=.*/=" + language.split("_")[1] + "/\" " + buildprop);
				cleanBuff(Runtime.getRuntime().exec(SED + " -i \"/^ro.product.locale.region/s/=.*/=" + language.split("_")[1] + "/\" " + buildprop));
			}

			if (!(btName = infoPanel.getBtname()).isEmpty())
			{
				writeLog("btName: " + SED + " -i \"/^bluetooth/s/=.*/=" + btName + "/\" " + customconf);
				cleanBuff(Runtime.getRuntime().exec(SED + " -i \"/^bluetooth/s/=.*/=" + btName + "/\" " + customconf));
			}

			if (!(brand = infoPanel.getBrandname()).isEmpty())
			{
				writeLog("brand: " + SED + " -i \"/^ro.product.brand/s/=.*/=" + brand + "/\" " + buildprop);
				cleanBuff(Runtime.getRuntime().exec(SED + " -i \"/^ro.product.brand/s/=.*/=" + brand + "/\" " + buildprop));
			}

			if (!(device = infoPanel.getMTPname()).isEmpty())
			{
				writeLog("device: " + SED + " -i \"/^ro.product.device/s/=.*/=" + device + "/\" " + buildprop);
				cleanBuff(Runtime.getRuntime().exec(SED + " -i \"/^ro.product.device/s/=.*/=" + device + "/\" " + buildprop));

				writeLog("name: " + SED + " -i \"/^ro.product.name/s/=.*/=" + device + "/\" " + buildprop);
				cleanBuff(Runtime.getRuntime().exec(SED + " -i \"/^ro.product.name/s/=.*/=" + device + "/\" " + buildprop));
			}

			if (!(manufacturer = infoPanel.getMafname()).isEmpty())
			{
				writeLog("manufacturer: " + SED + " -i \"/^ro.product.manufacturer/s/=.*/=" + manufacturer + "/\" " + buildprop);
				cleanBuff(Runtime.getRuntime().exec(SED + " -i \"/^ro.product.manufacturer/s/=.*/=" + manufacturer + "/\" " + buildprop));
			}

			if (!(density = infoPanel.getDensity()).isEmpty())
			{
				writeLog("density: " + SED + " -i \"/^ro.sf.lcd_density/s/=.*/=" + density + "/\" " + buildprop);
				cleanBuff(Runtime.getRuntime().exec(SED + " -i \"/^ro.sf.lcd_density/s/=.*/=" + density + "/\" " + buildprop));
			}

			if (!(version = infoPanel.getVer()).isEmpty())
			{
				writeLog("version: " + SED + " -i \"/^ro.build.display.id/s/=.*/=" + version + "/\" " + buildprop);
				cleanBuff(Runtime.getRuntime().exec(SED + " -i \"/^ro.build.display.id/s/=.*/=" + version + "/\" " + buildprop));
			}

			if (!(cversion = infoPanel.getCver()).isEmpty())
			{
				writeLog("cversion: " + SED + " -i \"/^ro.custom.build.version/s/=.*/=" + version + "/\" " + buildprop);
				cleanBuff(Runtime.getRuntime().exec(SED + " -i \"/^ro.custom.build.version/s/=.*/=" + cversion + "/\" " + buildprop));
			}

			if (!(ssid = infoPanel.getSsid()).isEmpty())
			{
				writeLog("ssid: " + SED + " -i \"/^wlan.SSID/s/=.*/=" + ssid + "/\" " + customconf);
				cleanBuff(Runtime.getRuntime().exec(SED + " -i \"/^wlan.SSID/s/=.*/=" + ssid + "/\" " + customconf));
			}

			if (!(ums = infoPanel.getUms()).isEmpty())
			{
				writeLog("ums: " + SED + " -i \"/^ro.ty.ums.label/s/=.*/=" + ums + "/\" " + buildprop);
				cleanBuff(Runtime.getRuntime().exec(SED + " -i \"/^ro.ty.ums.label/s/=.*/=" + ums + "/\" " + buildprop));
			}

			String fakeSizeIn,fakeSizeSd,fakeSizeRam,brightness,homepage;
			if (!(fakeSizeIn = infoPanel.getFakeSizeIn()).isEmpty())
			{
				writeLog("ums: " + SED + " -i \"/^ro.ty.storage.fakein/s/=.*/=" + fakeSizeIn + "/\" " + buildprop);
				cleanBuff(Runtime.getRuntime().exec(SED + " -i \"/^ro.ty.storage.fakein/s/=.*/=" + fakeSizeIn + "/\" " + buildprop));
			}
			if (!(fakeSizeSd = infoPanel.getFakeSizeSd()).isEmpty())
			{
				writeLog("ums: " + SED + " -i \"/^ro.ty.storage.fakesd/s/=.*/=" + fakeSizeSd + "/\" " + buildprop);
				cleanBuff(Runtime.getRuntime().exec(SED + " -i \"/^ro.ty.storage.fakesd/s/=.*/=" + fakeSizeSd + "/\" " + buildprop));
			}
			if (!(fakeSizeRam = infoPanel.getFakeSizeRam()).isEmpty())
			{
				writeLog("ums: " + SED + " -i \"/^ro.ty.storage.fakeram/s/=.*/=" + fakeSizeRam + "/\" " + buildprop);
				cleanBuff(Runtime.getRuntime().exec(SED + " -i \"/^ro.ty.storage.fakeram/s/=.*/=" + fakeSizeRam + "/\" " + buildprop));
			}

			if (!(brightness = infoPanel.getBrightness()).isEmpty())
			{
				//int intBrightness = (int)(Integer.valueOf(brightness)*255F/100);
				String value = brightness;//String.valueOf(intBrightness);
				writeLog("ums: " + SED + " -i \"/^ro.ty.setting.brightness/s/=.*/=" + value + "/\" " + buildprop);
				cleanBuff(Runtime.getRuntime().exec(SED + " -i \"/^ro.ty.setting.brightness/s/=.*/=" + value + "/\" " + buildprop));
			}

			if (infoPanel.getBrowserHomePageSupport() && !(homepage = infoPanel.getBrowserHomePage()).isEmpty())
			{
				String convertUrl = homepage.replaceAll("//","\\\\/\\\\/");
				writeLog("ums: " + SED + " -i \"/^ro.ty.browser.homepage/s/=.*/=" + convertUrl + "/\" " + buildprop);
				cleanBuff(Runtime.getRuntime().exec(SED + " -i \"/^ro.ty.browser.homepage/s/=.*/=" + convertUrl + "/\" " + buildprop));
			}

			if(infoPanel.getDrawerBgTransSupport()){
				String value = infoPanel.getDrawerBgTrans() ? "1" : "0";
				writeLog("ums: " + SED + " -i \"/^ro.ty.launcher.bgtrans/s/=.*/=" + value + "/\" " + buildprop);
				cleanBuff(Runtime.getRuntime().exec(SED + " -i \"/^ro.ty.launcher.bgtrans/s/=.*/=" + value + "/\" " + buildprop));
			}

			if(infoPanel.getLangBySimSupport()){
				String value = infoPanel.getLangBySim() ? "1" : "0";
				writeLog("ums: " + SED + " -i \"/^ro.ty.lang.bysim/s/=.*/=" + value + "/\" " + buildprop);
				cleanBuff(Runtime.getRuntime().exec(SED + " -i \"/^ro.ty.lang.bysim/s/=.*/=" + value + "/\" " + buildprop));
			}

			if (!(logo1_path = mediaPanel.getLogo1Path()).isEmpty())
			{
				makeLogoBin(0);
			}

			if (!logo2_path.isEmpty())
			{
				makeBootLogo();
				makeLogoBin(38);
			}

			if (!((hide_logo1_path = mediaPanel.getHideLogo1Path()).isEmpty()))
			{
				makeLogoBin(39);
			}

			if (!(hide_logo2_path.isEmpty()))
			{
				makeBootLogo2();
				makeLogoBin(40);
			}

			if (!(bootanimationPath = mediaPanel.getBootanimationPath()).isEmpty())
			{
				makeAnimation(0);
			}

			if (!(wallpaperpath = mediaPanel.getWallpaperPath()).isEmpty())
			{
				writeLog("wallpaper: " + MKDIR + "-p" + wrapper(WALLPAPER_DIR));
				cleanBuff(Runtime.getRuntime().exec(MKDIR + "-p" + wrapper(WALLPAPER_DIR)));

				writeLog("wallpaper: " + CONVERT + wrapper(wallpaperpath) + wrapper(WALLPAPER_DIR + FILE_SEPARATOR + "default_wallpaper.jpg"));
				cleanBuff(Runtime.getRuntime().exec(CONVERT + wrapper(wallpaperpath) + wrapper(WALLPAPER_DIR + FILE_SEPARATOR + "default_wallpaper.jpg")));

				writeLog("wallpaper: cmd /c cd /d " + TMP_DIR + FILE_SEPARATOR + "wallpaper" + "&" + ZIP + " -m" + FRAMWORK_RES + wrapper(WALLPAPER_DEST));
				cleanBuff(Runtime.getRuntime().exec(
						"cmd /c cd /d " + TMP_DIR + FILE_SEPARATOR + "wallpaper" + "&" + ZIP + " -m" + FRAMWORK_RES + wrapper(WALLPAPER_DEST)));

				//modify launcher
				/*
				writeLog("wallpaper: " + MKDIR + "-p" + wrapper(LAUNCHER_WALLPAPER_DIR));
				cleanBuff(Runtime.getRuntime().exec(MKDIR + "-p" + wrapper(LAUNCHER_WALLPAPER_DIR)));
				
				writeLog("wallpaper: " + CONVERT + wrapper(wallpaperpath) + wrapper(LAUNCHER_WALLPAPER_DIR + FILE_SEPARATOR + "wallpaper_01.jpg"));
				cleanBuff(Runtime.getRuntime().exec(CONVERT + wrapper(wallpaperpath) + wrapper(LAUNCHER_WALLPAPER_DIR + FILE_SEPARATOR + "wallpaper_01.jpg")));

				writeLog("wallpaper: cmd /c cd /d " + TMP_DIR + FILE_SEPARATOR + "launcher_wallpaper" + "&" + ZIP + " -m" + LAUNCHER_RES + wrapper(LAUNCHER_WALLPAPER_DEST));
				cleanBuff(Runtime.getRuntime().exec(
						"cmd /c cd /d " + TMP_DIR + FILE_SEPARATOR + "launcher_wallpaper" + "&" + ZIP + " -m" + LAUNCHER_RES + wrapper(LAUNCHER_WALLPAPER_DEST)));

				writeLog("wallpaper: " + CONVERT + wrapper(wallpaperpath) +" -resize x189 "  + wrapper(LAUNCHER_WALLPAPER_DIR + FILE_SEPARATOR + "wallpaper_01_small.jpg"));
				cleanBuff(Runtime.getRuntime().exec(CONVERT + wrapper(wallpaperpath) +" -resize x189 " + wrapper(LAUNCHER_WALLPAPER_DIR + FILE_SEPARATOR + "wallpaper_01_small.jpg")));

				writeLog("wallpaper: cmd /c cd /d " + TMP_DIR + FILE_SEPARATOR + "launcher_wallpaper" + "&" + ZIP + " -m" + LAUNCHER_RES + wrapper(LAUNCHER_WALLPAPER_SMALL_DEST));
				cleanBuff(Runtime.getRuntime().exec(
						"cmd /c cd /d " + TMP_DIR + FILE_SEPARATOR + "launcher_wallpaper" + "&" + ZIP + " -m" + LAUNCHER_RES + wrapper(LAUNCHER_WALLPAPER_SMALL_DEST)));
				*/
			}

			if (!(baudio = mediaPanel.getBaudio()).isEmpty())
			{
				writeLog("bootaudio: " + COPY + wrapper(baudio)
						+ wrapper(TMP_DIR + FILE_SEPARATOR + "system" + FILE_SEPARATOR + "media" + FILE_SEPARATOR + "bootaudio.mp3"));
				cleanBuff(Runtime.getRuntime().exec(
						COPY + wrapper(baudio) + wrapper(TMP_DIR + FILE_SEPARATOR + "system" + FILE_SEPARATOR + "media" + FILE_SEPARATOR + "bootaudio.mp3")));
			}

			if (appPanel.getApkList().length > 0)
			{
				File f[] = appPanel.getApkList();
				for (int i = 0; i < f.length; i++)
				{
					UUID name = UUID.randomUUID();
					if (AppPanel.getApkItem() == 0)
					{
						File dest = new File(APP_DIR);
						if (!dest.exists())
						{
							writeLog(MKDIR + " -p" + wrapper(APP_DIR));
							cleanBuff(Runtime.getRuntime().exec(MKDIR + " -p" + wrapper(APP_DIR)));
						}
						writeLog(COPY + wrapper(f[i].getAbsolutePath()) + wrapper(APP_DIR + FILE_SEPARATOR + name + ".apk"));
						cleanBuff(Runtime.getRuntime().exec(COPY + wrapper(f[i].getAbsolutePath()) + wrapper(APP_DIR + FILE_SEPARATOR + name + ".apk")));

					}
					else if (AppPanel.getApkItem() == 1)
					{
						writeLog("apk[1]: /system/app/" + f[i].getName() + ".apk");
						cleanBuff(Runtime.getRuntime().exec(
								COPY + wrapper(f[i].getAbsolutePath())
										+ wrapper(TMP_DIR + FILE_SEPARATOR + "system" + FILE_SEPARATOR + "app" + FILE_SEPARATOR + name + ".apk")));

					}
					else if (AppPanel.getApkItem() == 2)
					{
						File dest = new File(DATA_APP_DIR);
						if (!dest.exists())
						{
							writeLog(MKDIR + " -p" + wrapper(DATA_APP_DIR));
							cleanBuff(Runtime.getRuntime().exec(MKDIR + " -p" + wrapper(DATA_APP_DIR)));
						}
						writeLog("apk[1]: /data/app/" + f[i].getName() + ".apk");
						cleanBuff(Runtime.getRuntime().exec(
								COPY + wrapper(f[i].getAbsolutePath())
										+ wrapper(TMP_DIR + FILE_SEPARATOR + "data" + FILE_SEPARATOR + "app" + FILE_SEPARATOR + name + ".apk")));
					}
				}
			}

			if (userdataext4.exists())
			{
				int size = (int) (FileUtil.getFileSizes(userdataext4) / 1024 / 1024);
				writeLog(MAKE_EXT4FS + " -s -l " + size + "M -a system  " + USERDATAIMG + wrapper(USERDATA_DIR));
				cleanBuff(Runtime.getRuntime().exec(MAKE_EXT4FS + " -s -l " + size + "M -a system  " + USERDATAIMG + wrapper(USERDATA_DIR)));
			}

			if (systemext4.exists())
			{
				int size = (int) (FileUtil.getFileSizes(systemext4) / 1024 / 1024);
				writeLog(MAKE_EXT4FS + " -s -l " + size + "M -a system  " + SYSTEMIMG + wrapper(SYSTEM_DIR));
				cleanBuff(Runtime.getRuntime().exec(MAKE_EXT4FS + " -s -l " + size + "M -a system  " + SYSTEMIMG + wrapper(SYSTEM_DIR)));
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void cleanBuff(Process p)
	{
		try
		{
			final InputStream is1 = p.getInputStream();
			new Thread(new Runnable()
			{
				public void run()
				{
					BufferedReader br = new BufferedReader(new InputStreamReader(is1));
					try
					{
						while (br.readLine() != null)
							;
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}).start();

			InputStream is2 = p.getErrorStream();
			BufferedReader br2 = new BufferedReader(new InputStreamReader(is2));
			StringBuilder buf = new StringBuilder();
			String line = null;
			while ((line = br2.readLine()) != null)
			{
				buf.append(line);
			}
			p.waitFor();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

	}

	private boolean hasModify()
	{
		hasModify = infoPanel.hasModify() || mediaPanel.hasModify() || appPanel.hasModify();
		return hasModify;
	}

	class BackupWork extends SwingWorker<List<Work>, Work>
	{
		List<Work>	list	= new ArrayList<Work>();

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
			JFileChooser fd = new JFileChooser();
			fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			FirmwarePanel.current.setValue(0);
			int option = fd.showDialog(MainView.getInstance(), "选择保存目录");
			if (option == JFileChooser.APPROVE_OPTION)
			{
				File file = fd.getSelectedFile();
				FirmwarePanel.progressFrame.setVisible(true);
				updateProgress(90);
				FirmwarePanel.current.setValue(0);
				FirmwarePanel.current.setIndeterminate(false);
				FirmwarePanel.progressFrame.setTitle("备份固件");
				MainView.writeLog("backup firmware : " + MainView.COPY + " -rf" + wrapper(FirmwarePanel.rompath + FILE_SEPARATOR + "*")
						+ wrapper(file.getAbsolutePath()));
				MainView.cleanBuff(Runtime.getRuntime().exec(
						MainView.COPY + " -rf" + wrapper(FirmwarePanel.rompath + FILE_SEPARATOR + "*") + wrapper(file.getAbsolutePath())));
				for (int i = 90; i <= 100; i++)
				{
					FirmwarePanel.current.setValue(i);
				}
			}
			return list;
		}

		@Override
		protected void process(List<Work> works)
		{
			for (Work work : works)
			{
				FirmwarePanel.current.setValue(work.getId());
			}
		}

		@Override
		protected void done()
		{
			MainView.writeLog("backup firmware finish!!");
			FirmwarePanel.progressFrame.setVisible(false);
			firmwarePanel.setFirmwarePanelEnable(false);
			if (hasModify())
			{
				if (AppPanel.getApkItem() == 1)
				{
					if (!checkAppSize())
					{
						firmwarePanel.setFirmwarePanelEnable(true);
						return;
					}
				}
				exec = new ProGressWork();
				exec.execute();
			}
			else
			{
				JOptionPane.showMessageDialog(null, "你要修改什么呢？");
			}
		}
	}

	public static void main(String[] args)
	{
		if (args.length == 1)
		{
			if (args[0].equals("tyd"))
				DEBUG = true;
		}
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				MainView.getInstance();
			}
		});
	}
}
