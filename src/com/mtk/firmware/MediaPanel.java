package com.mtk.firmware;

import java.awt.Checkbox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import com.mtk.firmware.util.GenericFileFilter;

public class MediaPanel extends JPanel implements ActionListener
{
	private static final long	serialVersionUID	= 1L;
	private static MediaPanel	mPanel;
	private JTextField			jtf_logo1, jtf_logo2, jtf_banimation, jtf_fpsboot, jtf_baudio, jtf_wallpaper;
	private JButton				jb_logo1, jb_logo2, jb_banimation, jb_baudio, jb_wallpaper;
	private JFileChooser		fd;
	private Checkbox			cb_boot;
	private JPanel				jp_logo1, jp_logo2, jp_boot, jp_bootitem, jp_baudio, jp_wallpaper;
	public static String		lastPath			= "";
	public static Preferences	pref;

	private MediaPanel()
	{
		setLayout(null);

		pref = Preferences.userRoot().node(MainView.class.getName());
		jp_logo1 = new JPanel();
		jtf_logo1 = new JTextField(60);
		jb_logo1 = new JButton("浏览..");
		jb_logo1.addActionListener(this);
		jp_logo1.setToolTipText("选择第一屏图片,只能选择图片,不论格式,请选择对应分辨率的图片");
		jtf_logo1.setToolTipText("选择第一屏图片,只能选择图片,不论格式,请选择对应分辨率的图片");
		jp_logo1.add(new JLabel("开机第一屏图片"));
		jp_logo1.add(jtf_logo1);
		jp_logo1.add(jb_logo1);
		jp_logo1.setBounds(0, 5, 600, 30);
		add(jp_logo1);

		jp_logo2 = new JPanel();
		jtf_logo2 = new JTextField(60);
		jb_logo2 = new JButton("浏览..");
		jb_logo2.addActionListener(this);
		jp_logo2.add(new JLabel("开机第二屏图片"));
		jp_logo2.setToolTipText("选择第二屏图片,只能选择图片,不论格式,请选择对应分辨率的图片");
		jtf_logo2.setToolTipText("选择第二屏图片,只能选择图片,不论格式,请选择对应分辨率的图片");
		jp_logo2.add(jtf_logo2);
		jp_logo2.add(jb_logo2);
		jp_logo2.setBounds(0, 45, 600, 30);
		add(jp_logo2);

		jp_boot = new JPanel();
		jtf_banimation = new JTextField(60);
		jb_banimation = new JButton("浏览..");
		jb_banimation.addActionListener(this);
		jp_boot.add(new JLabel("开机动画目录 "));
		jp_boot.setToolTipText("选择动画目录，只能选择目录，目录中只能有图片且图片名称有序");
		jtf_banimation.setToolTipText("选择动画目录，只能选择目录，目录中只能有图片且图片名称有序");
		jp_boot.add(jtf_banimation);
		jp_boot.add(jb_banimation);
		jp_boot.setBounds(0, 85, 600, 30);
		add(jp_boot);

		jp_bootitem = new JPanel();
		cb_boot = new Checkbox();
		cb_boot.setEnabled(false);
		JLabel jl1 = new JLabel("无限循环");
		jl1.setToolTipText("无限循环为循环播放");
		jp_bootitem.add(jl1);
		jp_bootitem.add(cb_boot);

		jtf_fpsboot = new JTextField(3);
		jtf_fpsboot.setText("30");
		jtf_fpsboot.setEditable(false);
		JLabel jl2 = new JLabel("每秒播放帧数");
		jl2.setToolTipText("帧数为每秒播放张数，影响播放速度");
		jp_bootitem.add(jl2);
		jp_bootitem.add(jtf_fpsboot);
		jp_bootitem.setBounds(20, 110, 600, 30);
		add(jp_bootitem);

		jp_baudio = new JPanel();
		jtf_baudio = new JTextField(60);
		jb_baudio = new JButton("浏览..");
		jb_baudio.addActionListener(this);
		jp_baudio.add(new JLabel("开 机 音 乐 "));
		jp_baudio.setToolTipText("选择开机音乐文件，只能选择MP3文件");
		jtf_baudio.setToolTipText("选择开机音乐文件，只能选择MP3文件");
		jp_baudio.add(jtf_baudio);
		jp_baudio.add(jb_baudio);
		jp_baudio.setBounds(0, 135, 600, 30);
		add(jp_baudio);

		jp_wallpaper = new JPanel();
		jtf_wallpaper = new JTextField(60);
		jb_wallpaper = new JButton("浏览..");
		jb_wallpaper.addActionListener(this);
		jp_wallpaper.add(new JLabel("默 认 壁 纸 "));
		jb_wallpaper.setToolTipText("选择关机音乐文件，只能选择MP3文件");
		jtf_wallpaper.setToolTipText("选择关机音乐文件，只能选择MP3文件");
		jp_wallpaper.add(jtf_wallpaper);
		jp_wallpaper.add(jb_wallpaper);
		jp_wallpaper.setBounds(0, 185, 600, 30);
		add(jp_wallpaper);
	}

	public synchronized static MediaPanel getInstance()
	{
		if (mPanel == null)
		{
			mPanel = new MediaPanel();
		}
		return mPanel;
	}

	public String getLogo1Path()
	{
		return jtf_logo1.getText().toString().trim();
	}

	public String getLogo2Path()
	{
		return jtf_logo2.getText().toString().trim();
	}

	public String getWallpaperPath()
	{
		return jtf_wallpaper.getText().toString().trim();
	}

	public boolean getBootLoopItem()
	{
		return cb_boot.getState();
	}

	public String getBootanimationPath()
	{
		return jtf_banimation.getText().toString().trim();
	}

	public String getBootFps()
	{
		return jtf_fpsboot.getText().toString().trim();
	}

	public String getWallpaper()
	{
		return jtf_wallpaper.getText().toString().trim();
	}

	public String getBaudio()
	{
		return jtf_baudio.getText().toString().trim();
	}

	public void preAnimation(String path)
	{
		BufferedImage bi = null;

		File adir = new File(path);
		for (File f : adir.listFiles())
		{
			try
			{
				bi = ImageIO.read(f);
				if (f.isDirectory() || bi == null)
				{
					f.delete();
				}
			}
			catch (IOException e)
			{
				f.delete();
			}
		}
	}

	public boolean hasModify()
	{
		return !(isNull(jtf_logo1) && isNull(jtf_logo2) && isNull(jtf_banimation) && isNull(jtf_baudio) && isNull(jtf_wallpaper));
	}

	private boolean isNull(JTextField textField)
	{
		return textField.getText().toString().trim().equals("");
	}

	public void setMediaPanelEnable(boolean bool)
	{
		jtf_logo1.setEnabled(bool);
		jtf_logo2.setEnabled(bool);
		jtf_banimation.setEnabled(bool);
		jtf_fpsboot.setEnabled(bool);
		jtf_baudio.setEnabled(bool);
		jtf_wallpaper.setEnabled(bool);
		jb_logo1.setEnabled(bool);
		jb_logo2.setEnabled(bool);
		jb_banimation.setEnabled(bool);
		jb_baudio.setEnabled(bool);
		jb_wallpaper.setEnabled(bool);
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		if ((event.getSource() == jb_logo1) || (event.getSource() == jb_logo2) || (event.getSource() == jb_wallpaper))
		{
			lastPath = pref.get("lastPath", "");
			if (!lastPath.equals(""))
			{
				fd = new JFileChooser(lastPath);
			}
			else
			{
				fd = new JFileChooser();
			}
			fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
			String imageExts[] = { "jpg", "jpeg", "png", "gif", "bmp" };
			FileFilter filter = new GenericFileFilter(imageExts, "Images Files(*.jpg;*.jpeg;*.png;*.gif;*.bmp)");
			fd.addChoosableFileFilter(filter);
			int option = fd.showDialog(MainView.getInstance(), "选择图片");
			File file = fd.getSelectedFile();
			if (option == JFileChooser.APPROVE_OPTION)
			{
				if (event.getSource() == jb_logo1)
					jtf_logo1.setText(file.getAbsolutePath());
				if (event.getSource() == jb_logo2)
					jtf_logo2.setText(file.getAbsolutePath());
				if (event.getSource() == jb_wallpaper)
					jtf_wallpaper.setText(file.getAbsolutePath());
				pref.put("lastPath", file.getPath());
			}
		}
		if ((event.getSource() == jb_banimation))
		{
			lastPath = pref.get("lastPath", "");
			if (!lastPath.equals(""))
			{
				fd = new JFileChooser(lastPath);
			}
			else
			{
				fd = new JFileChooser();
			}
			fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int option = fd.showDialog(MainView.getInstance(), "选择动画目录");
			File file = fd.getSelectedFile();
			if (option == JFileChooser.APPROVE_OPTION)
			{
				preAnimation(file.getAbsolutePath());
				if (event.getSource() == jb_banimation)
				{
					jtf_banimation.setText(file.getAbsolutePath());
					cb_boot.setEnabled(true);
					jtf_fpsboot.setEditable(true);
				}
				pref.put("lastPath", file.getPath());
			}
		}
		if ((event.getSource() == jb_baudio))
		{
			lastPath = pref.get("lastPath", "");
			if (!lastPath.equals(""))
			{
				fd = new JFileChooser(lastPath);
			}
			else
			{
				fd = new JFileChooser();
			}
			fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
			String imageExts[] = { "mp3" };
			FileFilter filter = new GenericFileFilter(imageExts, "*.mp3");
			fd.addChoosableFileFilter(filter);
			int option = fd.showDialog(MainView.getInstance(), "选择mp3文件");
			File file = fd.getSelectedFile();
			if (option == JFileChooser.APPROVE_OPTION)
			{
				if (event.getSource() == jb_baudio)
					jtf_baudio.setText(file.getAbsolutePath());
				pref.put("lastPath", file.getPath());
			}
		}
	}
}
