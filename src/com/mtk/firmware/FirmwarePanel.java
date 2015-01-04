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

import com.mtk.firmware.util.FileUtil;

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
	public static String			src_systemimg;
	public static String			rompath;

	public final static String		FILE_SEPARATOR		= System.getProperty("file.separator");

	private FirmwarePanel()
	{
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
		JLabel jl = new JLabel("请选择固件目录");
		jl.setToolTipText("请选择要修改的固件目录，此处只能选择目录");
		firmwarePath.setToolTipText("请选择要修改的固件目录，此处只能选择目录");
		add(jl);
		add(firmwarePath);
		add(selectButton);
	}

	public synchronized static FirmwarePanel getInstance()
	{
		if (fPanel == null)
		{
			fPanel = new FirmwarePanel();
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
	}

	public void setAndroidSize()
	{
		BufferedReader br = null;
		String read;
		File f = new File(getFirmwarePath());
		File scatterFile = null;
		int size = 0;
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
					size = Integer.valueOf(read.split("0x")[1].replace(" ", ""), 16);
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

		systemSize = size / 1024 / 1024;
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

	class ProGressWork extends SwingWorker<List<Work>, Work>
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
			MainView.writeLog("====================================Begin readback system.img====================================");
			String tmpdir = MainView.wrapper(MainView.TMP_DIR);
			String sytemdir = MainView.wrapper(MainView.SYSTEM_DIR);
			MainView.getInstance().updateTabbedPane(false);
			current.setValue(0);
			updateProgress(90);
			if (new File(MainView.TMP_DIR).exists())
			{
				MainView.writeLog(MainView.RM + tmpdir + "-rf");
				MainView.cleanBuff(Runtime.getRuntime().exec(MainView.RM + tmpdir + "-rf"));
			}
			MainView.writeLog(MainView.MKDIR + tmpdir);
			MainView.cleanBuff(Runtime.getRuntime().exec(MainView.MKDIR + tmpdir));

			MainView.writeLog(MainView.COPY + src_systemimg + tmpdir);
			MainView.cleanBuff(Runtime.getRuntime().exec(MainView.COPY + src_systemimg + tmpdir));

			MainView.writeLog(MainView.SIMG2IMG + MainView.SYSTEMIMG + MainView.SYSTEMIMGEXT);
			MainView.cleanBuff(Runtime.getRuntime().exec(MainView.SIMG2IMG + MainView.SYSTEMIMG + MainView.SYSTEMIMGEXT));

			MainView.writeLog(MainView.MKDIR + sytemdir);
			MainView.cleanBuff(Runtime.getRuntime().exec(MainView.MKDIR + sytemdir));

			MainView.writeLog(MainView.EXT4 + MainView.SYSTEMIMGEXT + sytemdir);
			MainView.cleanBuff(Runtime.getRuntime().exec(MainView.EXT4 + MainView.SYSTEMIMGEXT + sytemdir));

			for (int i = 90; i <= 100; i++)
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
		protected void done()
		{
			MainView.writeLog("====================================Readback system.img finish====================================");
			progressFrame.setVisible(false);
			MainView.getInstance().updateTabbedPane(true);
			InfoPanel.getInstance().preload();
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
				if (isFirmwareDir(file))
				{
					try
					{
						src_systemimg = MainView.wrapper(file.getAbsolutePath() + "\\system.img");
						setFirmwarePath(file.getAbsolutePath());
						setAndroidSize();
						MainView.writeLog("Firmware path:" + file.getAbsolutePath());
						MainView.writeLog("Android parttion size:" + systemSize + "M");
						int realsize = FileUtil.FormatFileSize(FileUtil.getFileSizes(new File(file.getAbsolutePath() + "\\system.img")));
						MainView.writeLog("systemimg size:" + realsize + "M");
						if (realsize <= 10)
						{
							JOptionPane.showMessageDialog(null, "固件目录下的system.img损坏,请查看大小是否为0KB！！！");
							MainView.writeLog("system.img损坏");
							return;
						}
						allowSize = systemSize - realsize;
						MainView.writeLog("Allow add apk size:" + allowSize + "M");
						new ProGressWork().execute();
						progressFrame.setVisible(true);
						MediaPanel.pref.put("last", file.getAbsolutePath());
						MediaPanel.pref.put("allowSize", allowSize + "");
					}
					catch (Exception e1)
					{
						MainView.writeLog(e1.toString());
					}
				}
				else
				{
					JOptionPane.showMessageDialog(null, "该目录不是固件目录！！！");
					MainView.writeLog("该目录不是固件目录！！！");
				}
			}
		}
	}
}
