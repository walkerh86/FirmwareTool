package com.mtk.firmware;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.mtk.firmware.util.Item;
import com.mtk.firmware.util.Language;
import com.mtk.firmware.util.MyTextField;
import com.mtk.firmware.util.Timezones;

public class InfoPanel extends JPanel implements ActionListener, KeyListener
{
	private static final long	serialVersionUID	= 1L;
	private static InfoPanel	iPanel;
	private MyTextField			jtf_model, jtf_btname, jtf_mtp, jtf_maf, jtf_brand, jtf_ver, jtf_cver, jtf_ssid, jtf_ums;
	private JTextField			jtf_density;
	private JComboBox			jcb_timezone, jcb_language;
	private Timezones			timezones;
	private Language			language;
	private Item				timezoneItem, languageItem;
	private String				tmpModel, tmpTz, tmpLan, tmpBT, tmpBrand, tmpMTP, tmpMaf, tmpDen, tmpVer, tmpCver, tmpSSID, tmpUMS;
	private boolean				hasUms				= true;

	private InfoPanel()
	{
		setLayout(null);

		timezones = new Timezones();
		timezoneItem = new Item("0", "0");
		jcb_timezone = new JComboBox(timezones.getTimezones());
		jcb_timezone.addActionListener(this);

		language = new Language();
		languageItem = new Item("0", "0");
		jcb_language = new JComboBox(language.getLanguage());
		jcb_language.addActionListener(this);

		jtf_model = new MyTextField(65, "jtf_model");
		jtf_btname = new MyTextField(65, "jtf_btname");
		jtf_brand = new MyTextField(65, "jtf_brand");
		jtf_mtp = new MyTextField(65, "jtf_mtp");
		jtf_maf = new MyTextField(65, "jtf_maf");
		jtf_density = new JTextField(65);
		jtf_density.addKeyListener(this);
		jtf_ver = new MyTextField(65, "jtf_ver");
		jtf_cver = new MyTextField(65, "jtf_cver");
		jtf_ssid = new MyTextField(65, "jtf_ssid");
		jtf_ums = new MyTextField(65, "jtf_ums");

		JLabel jl1 = new JLabel("机型名称");
		jl1.setToolTipText("影响 设置→关于平板→机型名 ");
		jtf_model.setToolTipText("影响 设置→关于平板→机型名 ");
		jl1.setBounds(10, 10, 60, 20);
		add(jl1);
		jtf_model.setBounds(65, 10, 220, 20);
		add(jtf_model);

		JLabel jl2 = new JLabel("默认时区");
		jl2.setToolTipText("影响系统默认时间 ");
		jcb_timezone.setToolTipText("影响系统默认时间 ");
		jl2.setBounds(380, 10, 60, 20);
		add(jl2);
		jcb_timezone.setBounds(435, 10, 220, 20);
		add(jcb_timezone);

		JLabel jl3 = new JLabel("默认语言");
		jl3.setToolTipText("影响系统默认语言 ");
		jcb_language.setToolTipText("影响系统默认语言 ");
		jl3.setBounds(10, 50, 60, 20);
		add(jl3);
		jcb_language.setBounds(65, 50, 220, 20);
		add(jcb_language);

		JLabel jl4 = new JLabel("蓝牙名称");
		jl4.setToolTipText("影响本机打开蓝牙时被搜索出来的显示名称");
		jtf_btname.setToolTipText("影响本机打开蓝牙时被搜索出来的显示名称");
		jl4.setBounds(380, 50, 60, 20);
		add(jl4);
		jtf_btname.setBounds(435, 50, 220, 20);
		add(jtf_btname);

		JLabel jl5 = new JLabel("品牌名称");
		jl5.setToolTipText("影响安兔兔检测基本信息时显示的品牌名称");
		jtf_brand.setToolTipText("影响安兔兔检测基本信息时显示的品牌名称");
		jl5.setBounds(10, 90, 60, 20);
		add(jl5);
		jtf_brand.setBounds(65, 90, 220, 20);
		add(jtf_brand);

		JLabel jl6 = new JLabel("设备名称");
		jl6.setToolTipText("影响MTP、PTP链接PC时显示的名称,最长11个字符，不允许有空格");
		jtf_mtp.setToolTipText("影响MTP、PTP链接PC时显示的名称,最长11个字符，不允许有空格");
		jl6.setBounds(380, 90, 60, 20);
		add(jl6);
		jtf_mtp.setBounds(435, 90, 220, 20);
		add(jtf_mtp);

		JLabel jl7 = new JLabel("制 造 商 ");
		jl7.setToolTipText("影响安兔兔检测基本信息时显示的制造商名称");
		jtf_maf.setToolTipText("影响安兔兔检测基本信息时显示的制造商名称");
		jl7.setBounds(10, 130, 60, 20);
		add(jl7);
		jtf_maf.setBounds(65, 130, 220, 20);
		add(jtf_maf);

		JLabel jl8 = new JLabel("像素密度");
		jl8.setToolTipText("※影响整体UI的布局，请慎重修改，此处只能填数字");
		jtf_density.setToolTipText("※影响整体UI的布局，请慎重修改，此处只能填数字");
		jl8.setBounds(380, 130, 60, 20);
		add(jl8);
		jtf_density.setBounds(435, 130, 220, 20);
		add(jtf_density);

		JLabel jl9 = new JLabel("版 本 号");
		jl9.setToolTipText("影响 设置→关于平板→版本号");
		jtf_ver.setToolTipText("影响 设置→关于平板→版本号");
		jl9.setBounds(10, 170, 60, 20);
		add(jl9);
		jtf_ver.setBounds(65, 170, 220, 20);
		add(jtf_ver);

		JLabel jl10 = new JLabel("自定版本");
		jl10.setToolTipText("影响 设置→关于平板→自定义版本号");
		jtf_cver.setToolTipText("影响 设置→关于平板→自定义版本号");
		jl10.setBounds(380, 170, 60, 20);
		add(jl10);
		jtf_cver.setBounds(435, 170, 220, 20);
		add(jtf_cver);

		JLabel jl11 = new JLabel("共享SSID");
		jl11.setToolTipText("影响分享网络热点 的显示名称");
		jtf_ssid.setToolTipText("影响分享网络热点 的显示名称");
		jl11.setBounds(10, 210, 60, 20);
		add(jl11);
		jtf_ssid.setBounds(65, 210, 220, 20);
		add(jtf_ssid);

		JLabel jl12 = new JLabel("磁盘名称");
		jl12.setToolTipText("影响 USB大容量存储方式连接PC显示的磁盘名,最长11个字符，不允许有空格");
		jtf_ums.setToolTipText("影响 USB大容量存储方式连接PC显示的磁盘名,最长11个字符，不允许有空格");
		jl12.setBounds(380, 210, 60, 20);
		add(jl12);
		jtf_ums.setBounds(435, 210, 220, 20);
		add(jtf_ums);
	}

	public synchronized static InfoPanel getInstance()
	{
		if (iPanel == null)
		{
			iPanel = new InfoPanel();
		}
		return iPanel;
	}

	public String getModelName()
	{
		return jtf_model.getText().toString().trim().replace(" ", "\\ ");
	}

	public void setModelName(String modelName)
	{
		jtf_model.setText(modelName);
	}

	public String getTimezone()
	{
		return timezoneItem.getId();
	}

	public void setTimezone(String timezone)
	{
		jcb_timezone.setSelectedIndex(timezones.getIndex(timezone));
	}

	public String getLanguage()
	{
		return languageItem.getId();
	}

	public void setLanguage(String lan)
	{
		jcb_language.setSelectedIndex(language.getIndex(lan));
	}

	public String getBtname()
	{
		return jtf_btname.getText().toString().trim().replace(" ", "\\ ");
	}

	public void setBtname(String bn)
	{
		jtf_btname.setText(bn);
	}

	public String getMTPname()
	{
		return jtf_mtp.getText().toString().trim().replace(" ", "\\ ");
	}

	public void setMTPname(String mtn)
	{
		jtf_mtp.setText(mtn);
	}

	public String getMafname()
	{
		return jtf_maf.getText().toString().trim().replace(" ", "\\ ");
	}

	public void setMafname(String maf)
	{
		jtf_maf.setText(maf);
	}

	public String getBrandname()
	{
		return jtf_brand.getText().toString().trim().replace(" ", "\\ ");
	}

	public void setBrandname(String bra)
	{
		jtf_brand.setText(bra);
	}

	public String getDensity()
	{
		return jtf_density.getText().toString().trim();
	}

	public void setDensity(String den)
	{
		jtf_density.setText(den);
	}

	public String getVer()
	{
		return jtf_ver.getText().toString().trim().replace(" ", "\\ ");
	}

	public void setVer(String v)
	{
		jtf_ver.setText(v);
	}

	public String getCver()
	{
		return jtf_cver.getText().toString().trim().replace(" ", "\\ ");
	}

	public void setCver(String v)
	{
		jtf_cver.setText(v);
	}

	public String getSsid()
	{
		return jtf_ssid.getText().toString().trim().replace(" ", "\\ ");
	}

	public void setSsid(String ss)
	{
		jtf_ssid.setText(ss);
	}

	public String getUms()
	{
		return jtf_ums.getText().toString().trim().replace(" ", "\\ ");
	}

	public void setUms(String ums)
	{
		jtf_ums.setText(ums);
	}

	public boolean hasModify()
	{
		boolean ums = true;
		if (hasUms)
		{
			if (getUms().equals(tmpUMS))
			{
				ums = false;
			}
		}
		return !(getModelName().equals(tmpModel) && getLanguage().equals(tmpLan) && getTimezone().equals(tmpTz) && getBrandname().equals(tmpBrand)
				&& getMafname().equals(tmpMaf) && getMTPname().equals(tmpMTP) && getBrandname().equals(tmpBrand) && getDensity().equals(tmpDen)
				&& getVer().equals(tmpVer) && getCver().equals(tmpCver) && getSsid().equals(tmpSSID) && getBtname().equals(tmpBT.replace(" ", "\\ ")) && ums);

	}

	public void setInfoPanelEnable(boolean bool)
	{
		jtf_model.setEditable(bool);
		jcb_language.setEnabled(bool);
		jcb_timezone.setEnabled(bool);
		jtf_btname.setEditable(bool);
		jtf_mtp.setEditable(bool);
		jtf_maf.setEditable(bool);
		jtf_brand.setEditable(bool);
		jtf_density.setEditable(bool);
		jtf_ver.setEditable(bool);
		jtf_cver.setEditable(bool);
		jtf_ssid.setEditable(bool);
		if (hasUms)
		{
			jtf_ums.setEditable(bool);
		}
	}

	public void preload()
	{
		String read;
		StringBuffer lan = new StringBuffer();
		File f1 = null;
		File f2 = null;
		BufferedReader br = null;
		BufferedReader br2 = null;
		try
		{
			f1 = new File(MainView.unwrapper(MainView.buildprop));
			f2 = new File(MainView.unwrapper(MainView.customconf));
			br = new BufferedReader(new InputStreamReader(new FileInputStream(f1)));
			br2 = new BufferedReader(new InputStreamReader(new FileInputStream(f2)));
			while ((read = br.readLine()) != null)
			{
				if (read.startsWith("ro.product.model"))
				{
					if (read.split("=").length > 1)
					{
						setModelName(tmpModel = (read.split("=")[1]));
					}
				}
				if (read.startsWith("ro.product.locale.language"))
				{
					if (read.split("=").length > 1)
					{
						lan.append(read.split("=")[1]);
					}
				}
				if (read.startsWith("ro.product.locale.region"))
				{
					if (read.split("=").length > 1)
					{
						lan.append("_" + read.split("=")[1]);
						setLanguage(tmpLan = lan.toString().replace(" ", ""));
					}
				}
				if (read.startsWith("persist.sys.timezone"))
				{
					if (read.split("=").length > 1)
					{
						setTimezone(tmpTz = (read.split("=")[1].replace(" ", "")));
					}
				}
				if (read.startsWith("ro.product.brand"))
				{
					if (read.split("=").length > 1)
					{
						setBrandname(tmpBrand = (read.split("=")[1]));
					}
				}
				if (read.startsWith("ro.product.device="))
				{
					if (read.split("=").length > 1)
					{
						setMTPname(tmpMTP = (read.split("=")[1]));
					}
				}
				if (read.startsWith("ro.product.manufacturer"))
				{
					if (read.split("=").length > 1)
					{
						setMafname(tmpMaf = (read.split("=")[1]));
					}
				}
				if (read.startsWith("ro.sf.lcd_density"))
				{
					if (read.split("=").length > 1)
					{
						setDensity(tmpDen = (read.split("=")[1].replace(" ", "")));
					}
				}
				if (read.startsWith("ro.build.display.id"))
				{
					if (read.split("=").length > 1)
					{
						setVer(tmpVer = (read.split("=")[1]));
					}
				}
				if (read.startsWith("ro.custom.build.version"))
				{
					if (read.split("=").length > 1)
					{
						setCver(tmpCver = (read.split("=")[1]));
					}
				}

				if (read.startsWith("ro.usb.storage.name"))
				{
					if (read.split("=").length > 1)
					{
						setUms(tmpUMS = (read.split("=")[1]));
					}
				}
			}
			if (jtf_ums.getText().equals(""))
			{
				jtf_ums.setEditable(false);
				hasUms = false;
			}
			while ((read = br2.readLine()) != null)
			{
				if ((read.indexOf("bluetooth.HostName") != -1) && read.startsWith("bluetooth"))
				{
					if (read.split("=").length > 1)
					{
						setBtname(tmpBT = (read.split("=")[1]));
					}
				}

				if (read.indexOf("wlan.SSID") != -1)
				{
					if (read.split("=").length > 1)
					{
						setSsid(tmpSSID = (read.split("=")[1]));
					}
				}
			}
			br.close();
			br2.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == jcb_timezone)
		{
			timezoneItem = (Item) ((JComboBox) e.getSource()).getSelectedItem();
		}
		if (e.getSource() == jcb_language)
		{
			languageItem = (Item) ((JComboBox) e.getSource()).getSelectedItem();
		}
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		if (e.getSource() == jtf_density)
		{
			Pattern p = Pattern.compile("[0-9]");
			Matcher m = p.matcher(String.valueOf(e.getKeyChar()));
			boolean b = m.matches();
			if (!b)
			{
				e.consume();
			}
		}
	}

	private boolean isChinese(String path)
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
}
