package com.mtk.firmware;

import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import com.mtk.firmware.util.BinUtil;
import com.mtk.firmware.util.ComUtil;
import com.mtk.firmware.util.GenericFileFilter;
import com.mtk.firmware.util.Log;
import com.mtk.firmware.util.PropManager;

public class MediaPanel extends JPanel{
	private static final long	serialVersionUID	= 1L;
	private static MediaPanel mPanel;
	public static Preferences pref;
	private String mRomPath;
	private boolean mIsKernelLogoInSystem;
	private boolean mIsHiddenLogoSupport;

	private MediaPanel()
	{
		setLayout(null);

		pref = Preferences.userRoot().node(MainView.class.getName());

		initMediaItemViews();
	}

	public synchronized static MediaPanel getInstance(){	
		if (mPanel == null){		
			mPanel = new MediaPanel();
		}
		return mPanel;
	}

	public void preAnimation(String path){	
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
	
	public void setEnabled(boolean enable){
		if(ComUtil.DEBUG_MODE){
			return;
		}
		Iterator<Map.Entry<String,MediaItemView>> iter = mMediaItemViews.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String,MediaItemView> entry = iter.next();
			MediaItemView item = (MediaItemView)entry.getValue();
			item.setEnabled(enable);
		}
	}

	public void onRomLoaded(){
		Iterator<Map.Entry<String,MediaItemView>> iter = mMediaItemViews.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String,MediaItemView> entry = iter.next();
			MediaItemView item = (MediaItemView)entry.getValue();
			item.reset();
		}

		mRomPath = FirmwarePanel.getInstance().getRomPath();
		mIsKernelLogoInSystem = !isAndroidVerKK();
		mIsHiddenLogoSupport = isHideLogoSupport();
	}

	private boolean isAndroidVerKK(){
		return PropManager.getInstance().getAndroidVersion().startsWith("4.4");
	}

	public boolean isModified(){	
		boolean modified = false;
		Iterator<Map.Entry<String,MediaItemView>> iter = mMediaItemViews.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String,MediaItemView> entry = iter.next();
			MediaItemView item = (MediaItemView)entry.getValue();
			modified = item.isModified();
			if(modified){
				break;
			}
		}
		return modified;
	}

	public boolean isSystemModified(){
		boolean modified = false;
		Iterator<Map.Entry<String,MediaItemView>> iter = mMediaItemViews.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String,MediaItemView> entry = iter.next();
			MediaItemView item = (MediaItemView)entry.getValue();
			modified = item.isSystemModifiedOk();
			if(modified){
				break;
			}
		}
		//Log.i("mediaPenel, isSystemModified="+modified);
		return modified;
	}

	public boolean isShutdownModified(){
		boolean modified = false;
		Iterator<Map.Entry<String,MediaItemView>> iter = mMediaItemViews.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String,MediaItemView> entry = iter.next();
			MediaItemView item = (MediaItemView)entry.getValue();
			modified = item.isShutdownModified();
			if(modified){
				break;
			}
		}
		return modified;
	}

	public void finishModify(){
		
	}

	private boolean isHideLogoSupport(){
		String hideAnimPath = ComUtil.pathConcat(ComUtil.SYSTEM_DIR,"media","bootanimation2.zip");
		return new File(hideAnimPath).exists();
	}

	private int getBootImgSizeAndRotate(String imgFile, BinUtil.Size size, boolean port){
		Log.i("getBootImgSizeAndRotate,imgFile="+imgFile);
		int degree = 0;
		try{
			Image pic = javax.imageio.ImageIO.read(new File(imgFile));
			int width = pic.getWidth(null);
			int height = pic.getHeight(null);
			//System.out.print("width="+size.width+",height="+size.height);
			if(width != -1 && height != -1){
				if(!port && (width < height)){
					degree = -90;
				}else if(port && (width > height)){
					degree = 90;
				}

				if(size != null){
					size.width = port ? Math.min(width,height) : Math.max(width,height);
					size.height = port ? Math.max(width,height) : Math.min(width,height);
				}
			}
		}catch (IOException e){
			Log.i(e.toString());
		}
		return degree;
	}

	private int getLogoSizeAndRotate(String imgFile, BinUtil.Size size){
		return getBootImgSizeAndRotate(imgFile,size,PropManager.getInstance().isLogoDispPort());
	}
	
	private int getAnimSizeAndRotate(String imgFile, BinUtil.Size size){
		return getBootImgSizeAndRotate(imgFile,size,PropManager.getInstance().isDispPort());
	}
	
	private void convertLogo(String srcFile, String dstFile, int degree){
		if(degree != 0){
			BinUtil.convertAndRotateImage(srcFile, dstFile, degree);
		}else{
			BinUtil.convertImage(srcFile, dstFile);
		}
	}
	
	private void convertLogo(String srcFile, String dstFile){
		convertLogo(srcFile, dstFile, getLogoSizeAndRotate(srcFile,null));
	}

	public void doModify(){
		Iterator<Map.Entry<String,MediaItemView>> iter = mMediaItemViews.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String,MediaItemView> entry = iter.next();
			MediaItemView item = (MediaItemView)entry.getValue();
			if(item.isModified()){
				item.doModify();
			}
		}
	}
	
	private LinkedHashMap<String,MediaItemView> mMediaItemViews = new LinkedHashMap<String,MediaItemView>(20);
	private static final String KEY_LOGOS = "logos";
	private static final String KEY_LOGO_UBOOT = "uboot";
	private static final String KEY_LOGO_KERNEL = "kernel";
	private static final String KEY_BOOT_ANIM = "bootanim";
	private static final String KEY_BOOT_AUDIO = "bootaudio";
	private static final String KEY_SHUT_ANIM = "shutanim";
	private static final String KEY_SHUT_AUDIO = "shutaudio";
	private static final String KEY_WALLPAPER = "wallpaper";
	private static final String KEY_CANDIDATE_WALLPAPER = "candidate_wallpaper";
	private static final String KEY_LOGO_UBOOT2 = "hide_uboot2";
	private static final String KEY_LOGO_KERNEL2 = "hide_kernel2";
	private static final String KEY_BOOT_ANIM2 = "hide_bootanim2";
	private static final String KEY_BOOT_AUDIO2 = "hide_bootaudio2";
	private static final String KEY_SHUT_ANIM2 = "hide_shutanim2";
	private static final String KEY_SHUT_AUDIO2 = "hide_shutaudio2";
	private static final String KEY_FAT_IMAGE = "fat";

	private static final int PROP_ITEM_COLS = 25;
	private static final int PROP_ITEM_LABEL_COLS = 6;
	private void initMediaItemViews(){
		FileFilter logoFileFilter = new GenericFileFilter(new String[]{ "jpg", "jpeg", "png", "bmp" }, "Images Files(*.jpg;*.jpeg;*.png;*.bmp)");
		FileFilter audioFileFilter = new GenericFileFilter(new String[]{ "mp3" }, "*.mp3");
		FileFilter imageFileFilter = new GenericFileFilter(new String[]{ "img" }, "*.img");
		mMediaItemViews.put(KEY_LOGOS,new LogoGroupView(new LogoItemView[]{
			new LogoItemView("第一屏LOGO",JFileChooser.FILES_ONLY, logoFileFilter, "选择图片",MainView.getBounds(0, 0, 1, PROP_ITEM_COLS),0,false,false),
			new LogoItemView("第二屏LOGO",JFileChooser.FILES_ONLY, logoFileFilter, "选择图片",MainView.getBounds(0, PROP_ITEM_COLS+1, 1, PROP_ITEM_COLS),38,true,false),
			new LogoItemView("隐藏第一屏LOGO",JFileChooser.FILES_ONLY, logoFileFilter, "选择图片",MainView.getBounds(6, 0, 1, PROP_ITEM_COLS),39,false,true),
			new LogoItemView("隐藏第二屏LOGO",JFileChooser.FILES_ONLY, logoFileFilter, "选择图片",MainView.getBounds(6, PROP_ITEM_COLS+1, 1, PROP_ITEM_COLS),40,true,true),
		}));
		mMediaItemViews.put(KEY_BOOT_ANIM,new AnimItemView("开机动画目录",JFileChooser.DIRECTORIES_ONLY, null, "选择动画目录",MainView.getBounds(2, 0, 1, PROP_ITEM_COLS*2+1),"bootanimation",false));
		mMediaItemViews.put(KEY_BOOT_AUDIO,new AudioItemView("开机铃声",JFileChooser.FILES_ONLY, audioFileFilter, "选择mp3文件",MainView.getBounds(1, 0, 1, PROP_ITEM_COLS), "bootaudio.mp3",false));
		mMediaItemViews.put(KEY_SHUT_ANIM,new AnimItemView("关机动画目录",JFileChooser.DIRECTORIES_ONLY, null, "选择动画目录",MainView.getBounds(3, 0, 1, PROP_ITEM_COLS*2+1),"shutanimation",false));
		mMediaItemViews.put(KEY_SHUT_AUDIO,new AudioItemView("关机铃声",JFileChooser.FILES_ONLY, audioFileFilter, "选择mp3文件",MainView.getBounds(1, PROP_ITEM_COLS+1, 1, PROP_ITEM_COLS), "shutaudio.mp3",false));
		mMediaItemViews.put(KEY_WALLPAPER,new WallpaperGroupView(
			new MediaItemView("默认壁纸",JFileChooser.FILES_ONLY, logoFileFilter, "选择图片",MainView.getBounds(4, 0, 1, PROP_ITEM_COLS*2+1)),
			new CandidateWallpaperItemView("候选壁纸",JFileChooser.DIRECTORIES_ONLY, null, "选择壁纸目录",MainView.getBounds(5, 0, 1, PROP_ITEM_COLS*2+1))
		));
		mMediaItemViews.put(KEY_BOOT_ANIM2,new AnimItemView("隐藏开机动画",JFileChooser.DIRECTORIES_ONLY, null, "选择动画目录",MainView.getBounds(8, 0, 1, PROP_ITEM_COLS*2+1),"bootanimation2",true));
		mMediaItemViews.put(KEY_BOOT_AUDIO2,new AudioItemView("隐藏开机铃声",JFileChooser.FILES_ONLY, audioFileFilter, "选择mp3文件",MainView.getBounds(7, 0, 1, PROP_ITEM_COLS),"bootaudio2.mp3",true));
		mMediaItemViews.put(KEY_SHUT_ANIM2,new AnimItemView("隐藏关机动画",JFileChooser.DIRECTORIES_ONLY, null, "选择动画目录",MainView.getBounds(9, 0, 1, PROP_ITEM_COLS*2+1),"shutanimation2",true));
		mMediaItemViews.put(KEY_SHUT_AUDIO2,new AudioItemView("隐藏关机铃声",JFileChooser.FILES_ONLY, audioFileFilter, "选择mp3文件",MainView.getBounds(7, PROP_ITEM_COLS+1, 1, PROP_ITEM_COLS),"shutaudio2.mp3",true));
		mMediaItemViews.put(KEY_FAT_IMAGE,new FatItemView("fat_sparse.img",JFileChooser.FILES_ONLY, imageFileFilter, "选择image文件",MainView.getBounds(10, 0, 1, PROP_ITEM_COLS*2+1),FirmwarePanel.getInstance().getRomPath()));

		Iterator<Map.Entry<String,MediaItemView>> iter = mMediaItemViews.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String,MediaItemView> entry = iter.next();
			MediaItemView item = (MediaItemView)entry.getValue();
			item.addView(this, 0, 0);
		}
	}
	
	private class MediaItemView implements ActionListener{
		protected String mLabel;
		private int mSelMode;
		private FileFilter mFileFilter;
		private String mTitle;
		protected JTextField mTextView;
		protected JLabel mJLabel;
		protected JButton mBrowse;
		protected Rectangle mRect;
		protected int mState;
		protected static final int STATE_IDLE = 0;
		protected static final int STATE_MODIFIED = 1;
		protected static final int STATE_SUCCESS = 2;
		protected static final int STATE_FAILED = 3;
		
		public MediaItemView(String label, int selMode, FileFilter filter, String title, Rectangle rect){
			mLabel = label;
			mSelMode = selMode;
			mFileFilter = filter;
			mTitle = title;
			mRect = rect;
			mState = STATE_IDLE;
		}

		public boolean isModified(){
			return mState == STATE_MODIFIED;
		}

		public boolean isSystemModifiedOk(){
			return (mState == STATE_SUCCESS) && isInSystemImg();
		}

		public boolean isInSystemImg(){
			return true;
		}

		public boolean isShutdownModified(){
			return false;
		}

		public String getMediaPath(){
			return (mTextView != null) ? mTextView.getText().toString().trim() : null;
		}

		public void addView(JPanel panel, int x, int y){
			int browseColSize = MainView.PANEL_MAIN_COL_SIZE*4;
			int labelColSize = MainView.PANEL_MAIN_COL_SIZE*PROP_ITEM_LABEL_COLS;
			mJLabel = new JLabel(mLabel);
			mJLabel.setBounds(mRect.x,mRect.y,labelColSize,mRect.height);
			add(mJLabel);
			mTextView = new JTextField();
			mTextView.setBounds(mRect.x+labelColSize,mRect.y,mRect.width-labelColSize-browseColSize,mRect.height);
			add(mTextView);
			mBrowse = new JButton("浏览");
			mBrowse.setBounds(mRect.x+mRect.width-browseColSize,mRect.y,browseColSize,mRect.height);
			add(mBrowse);
			mBrowse.addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent event){
			String lastPath = pref.get("lastPath", "");
			JFileChooser fd = null;
			if (!lastPath.equals("")){			
				fd = new JFileChooser(lastPath);
			}
			else{			
				fd = new JFileChooser();
			}
			fd.setFileSelectionMode(mSelMode);
			if(mFileFilter != null){
				fd.setFileFilter(mFileFilter);
			}
			int option = fd.showDialog(MainView.getInstance(), mTitle);
			File file = fd.getSelectedFile();
			if (option == JFileChooser.APPROVE_OPTION){			
				String path = file.getAbsolutePath();
				if(path != null && !path.isEmpty()){
					markState(STATE_MODIFIED);
					mTextView.setText(path);				
					pref.put("lastPath", path);
				}
			}
		}
		
		public void setEnabled(boolean enable){
			if(mJLabel != null){
				mJLabel.setEnabled(enable);
			}
			if(mTextView != null){
				mTextView.setEnabled(enable);
			}
			if(mBrowse != null){
				mBrowse.setEnabled(enable);
			}
		}
		
		public void doModify(){
			//mJLabel.setForeground(ComUtil.COLOR_MARK_UNMODIFIED);

			if(doRealModify()){
				markState(STATE_SUCCESS);
			}else{
				markState(STATE_FAILED);
			}
		}

		public boolean doRealModify(){
			return true;
		}

		protected void markState(int state){
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

		public void reset(){
			markState(STATE_IDLE);
			if(mTextView != null){
				mTextView.setText("");
			}
			if(mJLabel != null){
				mJLabel.setForeground(ComUtil.COLOR_MARK_UNMODIFIED);
			}
		}
	}

	private class MediaGroupView extends MediaItemView{
		MediaItemView[] mItems;
		public MediaGroupView(MediaItemView[] items){
			super(null,0,null,null,null);
			mItems = items;
		}

		public boolean isModified(){
			for(int i=0;i<mItems.length;i++){
				if(mItems[i].isModified()){
					return true;
				}
			}
			return false;
		}
		
		public void addView(JPanel panel, int x, int y){
			for(int i=0;i<mItems.length;i++){
				mItems[i].addView(panel,x,y);
			}
		}

		public void setEnabled(boolean enable){
			for(int i=0;i<mItems.length;i++){
				mItems[i].setEnabled(enable);
			}
		}

		public void doModify(){
			for(int i=0;i<mItems.length;i++){
				mItems[i].doModify();
			}
		}

		public void reset(){
			for(int i=0;i<mItems.length;i++){
				mItems[i].reset();
			}
		}

		public boolean isSystemModifiedOk(){
			for(int i=0;i<mItems.length;i++){
				if(mItems[i].isSystemModifiedOk()){
					return true;
				}
			}
			return false;
		}
	}

	private class LogoGroupView extends MediaGroupView{
		LogoItemView[] mLogoItems;
		public LogoGroupView(LogoItemView[] items){
			super(items);
			mLogoItems = items;
		}
		
		@Override
		public void doModify(){
			String tmpPath = ComUtil.pathConcat(ComUtil.OUT_DIR,"logo");
			String logoBinPath = FirmwarePanel.getInstance().getLogoBinPath();
			Logo bin = new Logo(new File(logoBinPath));
			BinUtil.rm(tmpPath);
			BinUtil.mkdir(tmpPath);
			bin.unpack(tmpPath);

			for(int i=0;i<mLogoItems.length;i++){
				if(!mLogoItems[i].isModified()){
					continue;
				}
				int logoIndex = mLogoItems[i].getIndex();
				String srcPath = mLogoItems[i].getMediaPath();
				String dstBmp = ComUtil.pathConcat(tmpPath,logoIndex+".bmp");
				int degree = getLogoSizeAndRotate(srcPath,null);
				convertLogo(srcPath,dstBmp,degree);
				//Log.i("modify logo, path="+path+",dstBmp="+dstBmp);
				if(mIsKernelLogoInSystem && mLogoItems[i].isKenelLogo()){
					String bootLogo = mLogoItems[i].isHidden() ? "boot_logo2" : "boot_logo";
					BinUtil.bmpToRaw(dstBmp,ComUtil.pathConcat(ComUtil.SYSTEM_DIR,"media","images",bootLogo));
				}
				String dstRaw = ComUtil.pathConcat(tmpPath,logoIndex+".raw");
				BinUtil.bmpToRaw(dstBmp,dstRaw);

				mLogoItems[i].markState(STATE_SUCCESS);
			}

			bin.repack(tmpPath);
		}
	}

	private class LogoItemView extends MediaItemView{
		private int mIndex;
		private boolean mIsKernel;
		private boolean mIsHidden;
		
		public LogoItemView(String label, int selMode, FileFilter filter, String title, Rectangle rect, int index, boolean isKernel, boolean isHidden) {
			super(label, selMode, filter, title, rect);
			mIndex = index;
			mIsKernel = isKernel;
			mIsHidden = isHidden;
		}

		public int getIndex(){
			return mIndex;
		}

		public boolean isKenelLogo(){
			return mIsKernel;	
		}

		public boolean isHidden(){
			return mIsHidden;
		}

		public boolean isInSystemImg(){
			return (mIsKernelLogoInSystem && mIsKernel) ? true : false;
		}

		public void setEnabled(boolean enable){
			super.setEnabled((mIsHidden && !mIsHiddenLogoSupport) ? false : enable);
		}
	}

	private class AudioItemView extends MediaItemView{
		private String mDstAudio;
		private boolean mIsHidden;
		
		public AudioItemView(String label, int selMode, FileFilter filter, String title, Rectangle rect, String dstAudio, boolean isHidden) {
			super(label, selMode, filter, title, rect);
			mDstAudio = dstAudio;
			mIsHidden = isHidden;
		}

		@Override
		public boolean doRealModify() {
			BinUtil.copy(getMediaPath(), ComUtil.pathConcat(ComUtil.pathConcat(ComUtil.SYSTEM_DIR,"media",mDstAudio)));
			return true;
		}

		public boolean isShutdownModified(){
			if((mState == STATE_SUCCESS) && mDstAudio.contains("shut")){
				return true;
			}
			return false;
		}

		public void setEnabled(boolean enable){
			super.setEnabled((mIsHidden && !mIsHiddenLogoSupport) ? false : enable);
		}
	}

	private class AnimItemView extends MediaItemView{
		private JCheckBox mLoop;
		private JTextField mFps;
		private JLabel mFpsLabel;
		private String mDstAnim;
		private boolean mIsHidden;
		private static final boolean DEFAULT_LOOP_ENABLE = false;
		private static final String DEFAULT_FPS_VALUE = "10";
		
		public AnimItemView(String label, int selMode, FileFilter filter, String title, Rectangle rect, String dstAnim, boolean isHidden) {
			super(label, selMode, filter, title, rect);
			mDstAnim = dstAnim;
			mIsHidden = isHidden;
		}
		
		@Override
		public void addView(JPanel panel, int x, int y){
			int browseColSize = MainView.PANEL_MAIN_COL_SIZE*4;
			int loopColSize = MainView.PANEL_MAIN_COL_SIZE*6;
			int fpsLabelColSize = MainView.PANEL_MAIN_COL_SIZE*4;
			int fpsColSize = MainView.PANEL_MAIN_COL_SIZE*2;
			int labelColSize = MainView.PANEL_MAIN_COL_SIZE*PROP_ITEM_LABEL_COLS;
			mJLabel = new JLabel(mLabel);
			mJLabel.setBounds(mRect.x,mRect.y,labelColSize,mRect.height);
			add(mJLabel);
			mTextView = new JTextField();
			mTextView.setBounds(mRect.x+labelColSize,mRect.y,mRect.width-labelColSize-browseColSize-fpsLabelColSize-fpsColSize-labelColSize,mRect.height);
			add(mTextView);
			mBrowse = new JButton("浏览");
			mBrowse.setBounds(mRect.x+mRect.width-browseColSize,mRect.y,browseColSize,mRect.height);
			add(mBrowse);
			mBrowse.addActionListener(this);			
			
			mLoop = new JCheckBox("循环播放");
			mLoop.setBounds(mRect.x+mRect.width-browseColSize-fpsColSize-fpsLabelColSize-loopColSize,mRect.y,loopColSize,mRect.height);
			add(mLoop);
			mFpsLabel = new JLabel("播放帧率");
			mFpsLabel.setBounds(mRect.x+mRect.width-browseColSize-fpsColSize-fpsLabelColSize,mRect.y,fpsLabelColSize,mRect.height);
			add(mFpsLabel);
			mFps = new JTextField(DEFAULT_FPS_VALUE);
			mFps.setBounds(mRect.x+mRect.width-browseColSize-fpsColSize,mRect.y,fpsColSize,mRect.height);
			add(mFps);
		}

		public boolean getLoop(){
			return mLoop.isSelected();
		}

		public String getFps(){
			return mFps.getText().toString().trim();
		}

		@Override
		public boolean doRealModify() {
			boolean loop = mLoop.isSelected();
			String fps = getFps();
			String tmpPath = ComUtil.pathConcat(ComUtil.OUT_DIR,"anim",mDstAnim);
			BinUtil.rm(tmpPath);
			BinUtil.mkdir(ComUtil.pathConcat(tmpPath,"part0"));
			if(!loop){
				BinUtil.mkdir(ComUtil.pathConcat(tmpPath,"part1"));
			}
			String animPath = getMediaPath();
			String[] srcFiles = new File(animPath).list();
			String tmpSrcPath = ComUtil.pathConcat(ComUtil.OUT_DIR,"anim","tmp_src");
			ComUtil.mkTempDir(tmpSrcPath);
			int index = 0;
			DecimalFormat dig = new DecimalFormat("000");
			for(String animFile : srcFiles){
				String suffix = ComUtil.getFileNameSuffix(animFile);
				if(!ComUtil.isValidPictureSuffix(suffix)){
					Log.i("invalid anim file "+animFile);
					continue;
				}
				BinUtil.copy(ComUtil.pathConcat(animPath,animFile), ComUtil.pathConcat(tmpSrcPath,ComUtil.strConcatWith(".",dig.format(index++),suffix)));
			}
			srcFiles = new File(tmpSrcPath).list();
			if(srcFiles.length == 0) return false;
			
			BinUtil.Size logoSize = new BinUtil.Size(-1,-1);
			int rotateDegree = getAnimSizeAndRotate(ComUtil.pathConcat(tmpSrcPath,srcFiles[0]), logoSize);
			index = 0;
			for(String srcFile : srcFiles){
				String srcPath = ComUtil.pathConcat(tmpSrcPath,srcFile);
				String dstFile = ComUtil.pathConcat(ComUtil.pathConcat(tmpPath,"part0"),ComUtil.strConcatWith(".",dig.format(index++),"png"));
				convertLogo(srcPath, dstFile, rotateDegree);
			}
			if(!loop){
				String srcPath = ComUtil.pathConcat(tmpSrcPath,srcFiles[srcFiles.length-1]);
				String dstFile = ComUtil.pathConcat(ComUtil.pathConcat(tmpPath,"part1"),ComUtil.strConcatWith(".",dig.format(index++),"png"));
				convertLogo(srcPath, dstFile, rotateDegree);
			}
			File descxt = new File(ComUtil.pathConcat(tmpPath,"desc.txt"));
			try{
				FileWriter writer = new FileWriter(descxt, true);
				writer.write(logoSize.width + " " + logoSize.height + " " + fps + "\n");
				if(loop){
					writer.write("p 0 0 part0" + "\n");
				}else{
					writer.write("p 1 0 part0" + "\n");
					writer.write("p 0 0 part1" + "\n");
				}
				writer.close();
			}catch (IOException e){
				Log.i(e.toString());
			}
			BinUtil.zipCompress(tmpPath, mDstAnim, "-r -0");
			String zipFile = ComUtil.strConcat(mDstAnim,".zip");
			BinUtil.copy(ComUtil.pathConcat(tmpPath, zipFile), ComUtil.pathConcat(ComUtil.SYSTEM_DIR,"media",zipFile));

			return true;
		}

		@Override
		public void setEnabled(boolean enable) {
			if(mIsHidden && !mIsHiddenLogoSupport){
				enable = false;
			}
			super.setEnabled(enable);
			if(mLoop != null){
				mLoop.setEnabled(enable);
			}
			if(mFpsLabel != null){
				mFpsLabel.setEnabled(enable);
			}
			if(mFps != null){
				mFps.setEnabled(enable);
			}
		}

		@Override
		public void reset(){
			super.reset();
			mLoop.setSelected(DEFAULT_LOOP_ENABLE);
			mFps.setText(DEFAULT_FPS_VALUE);
		}

		public boolean isShutdownModified(){
			if((mState == STATE_SUCCESS) && mDstAnim.contains("shut")){
				return true;
			}
			return false;
		}
	}

	private class CandidateWallpaperItemView extends MediaItemView{
		private JCheckBox mRmOthers;
		private static final boolean DEFAULT_RM_OTHER_ENABLE = false;
		
		public CandidateWallpaperItemView(String label, int selMode, FileFilter filter, String title, Rectangle rect) {
			super(label, selMode, filter, title, rect);
		}
		
		@Override
		public void addView(JPanel panel, int x, int y){
			int browseColSize = MainView.PANEL_MAIN_COL_SIZE*4;
			int loopColSize = MainView.PANEL_MAIN_COL_SIZE*8;
			int labelColSize = MainView.PANEL_MAIN_COL_SIZE*PROP_ITEM_LABEL_COLS;
			mJLabel = new JLabel(mLabel);
			mJLabel.setBounds(mRect.x,mRect.y,labelColSize,mRect.height);
			add(mJLabel);
			mTextView = new JTextField();
			mTextView.setBounds(mRect.x+labelColSize,mRect.y,mRect.width-labelColSize-browseColSize-loopColSize,mRect.height);
			add(mTextView);
			mBrowse = new JButton("浏览");
			mBrowse.setBounds(mRect.x+mRect.width-browseColSize,mRect.y,browseColSize,mRect.height);
			add(mBrowse);
			mBrowse.addActionListener(this);			
			
			mRmOthers = new JCheckBox("删除非候选壁纸");
			mRmOthers.setBounds(mRect.x+mRect.width-browseColSize-loopColSize,mRect.y,loopColSize,mRect.height);
			add(mRmOthers);
		}

		public boolean getRemoveOther(){
			return mRmOthers.isSelected();
		}

		@Override
		public boolean doRealModify() {
			return true;
		}

		@Override
		public void setEnabled(boolean enable) {
			super.setEnabled(enable);
			if(mRmOthers != null){
				mRmOthers.setEnabled(enable);
			}
		}

		@Override
		public void reset(){
			super.reset();
			mRmOthers.setSelected(DEFAULT_RM_OTHER_ENABLE);
		}
	}

	private class WallpaperGroupView extends MediaGroupView{
		private MediaItemView mDefaultItem;
		private CandidateWallpaperItemView mCandidateItem;
		private DecimalFormat mDF = new DecimalFormat("00");
		private static final int DEFAULT_WALLPAPER_IDX = 1;
		private static final int CANDIDATE_WALLPAPER_START_IDX = 2;
		private final String[] WALLPAPER_DRAWABLES = new String[]{"drawable-nodpi","drawable-sw600dp-nodpi"};
		private static final String SMALL_SIZE = "x189";
		private static final int MAX_WALLPAPERS = 20;
		
		public WallpaperGroupView(MediaItemView defItem, CandidateWallpaperItemView candidateItem){
			super(new MediaItemView[]{defItem,candidateItem});
			mDefaultItem = defItem;
			mCandidateItem = candidateItem;
		}

		public void doModify(){
			Log.i("<<<<<<<<<<<<<<<<<<<<<<<<<<modify wallpaper begin");
			String tmpDir = ComUtil.pathConcat(ComUtil.OUT_DIR,"wallpaper");
			ComUtil.mkTempDir(tmpDir);
			String srcTmpDir = ComUtil.pathConcat(tmpDir,"src_tmp");
			BinUtil.mkdir(srcTmpDir);
			String srcCpDir = ComUtil.pathConcat(tmpDir,"src_cp");
			BinUtil.mkdir(srcCpDir);
			String sw480Dir = ComUtil.pathConcat(tmpDir,"sw480");
			BinUtil.mkdir(sw480Dir);
			
			String srcDefPath = mDefaultItem.getMediaPath();
			String srcCandidatePath = mCandidateItem.getMediaPath();
			boolean isDefaultModified = mDefaultItem.isModified() && !ComUtil.strIsEmpty(srcDefPath);
			boolean isCandidateModified = mCandidateItem.isModified() && !ComUtil.strIsEmpty(srcCandidatePath);
			String srcTmpDefPath = null;
			int wallpaperCount = 0;
			if(isDefaultModified){
				String dstPath = ComUtil.pathConcat(srcTmpDir,getWallpaperName(DEFAULT_WALLPAPER_IDX,false));
				String srcCpPath = ComUtil.pathConcat(srcCpDir,ComUtil.strConcatWithDot(mDF.format(DEFAULT_WALLPAPER_IDX),ComUtil.getFileNameSuffix(srcDefPath)));
				BinUtil.copy(srcDefPath,srcCpPath);
				BinUtil.convertImage(srcCpPath, dstPath);
				BinUtil.resizeImage(dstPath,ComUtil.pathConcat(srcTmpDir,getWallpaperName(DEFAULT_WALLPAPER_IDX,true)), SMALL_SIZE);
				srcTmpDefPath = dstPath;
				wallpaperCount++;

				//update framework imediately
				StringBuilder sb = new StringBuilder();
				String defName = "default_wallpaper.jpg";
				String frameworkTmpDir = ComUtil.pathConcat(tmpDir,"framework");
				for(String drawable : WALLPAPER_DRAWABLES){
					String dstDir = ComUtil.pathConcat(frameworkTmpDir,"res",drawable);
					BinUtil.mkdir(dstDir);
					BinUtil.copy(srcTmpDefPath,ComUtil.pathConcat(dstDir,defName));
					sb.append(" ");
					sb.append(ComUtil.strWithQuotation(ComUtil.pathConcat("res",drawable,defName)));
				}
				BinUtil.zipUpdateApk(frameworkTmpDir, ComUtil.FRAMEWORK_RES, sb.toString());
			}

			String launcherPath = getLauncherApkPath();
			if(launcherPath == null){
				if(isDefaultModified){
					mDefaultItem.markState(STATE_SUCCESS);
				}
				return;
			}
			
			String launcherTmpDir = ComUtil.pathConcat(tmpDir,"launcher");
			String launcherNodpiDir = ComUtil.pathConcat(launcherTmpDir,"res","drawable-nodpi");
			boolean rmNonCandidateWallpapers = isCandidateModified && mCandidateItem.getRemoveOther();
			//handle old version problem begin, deprecate drawable-sw400dp-nodpi,drawable-sw480dp-nodpi
			String deprecatedDrawables = ComUtil.pathConcat("res","drawable-sw480dp-nodpi","*");
			BinUtil.unzipExtract(launcherPath, deprecatedDrawables, sw480Dir);
			String deprecatedDir = ComUtil.pathConcat(sw480Dir,"res","drawable-sw480dp-nodpi");
			if(new File(deprecatedDir).exists()){
				String delPaths = ComUtil.strConcatSpace(deprecatedDrawables,ComUtil.pathConcat("res","drawable-sw400dp-nodpi","*"));
				BinUtil.zipDelete(launcherPath,delPaths);
				
				BinUtil.mkdir(launcherNodpiDir);
				if(isCandidateModified && rmNonCandidateWallpapers){
					BinUtil.copy(ComUtil.pathConcat(deprecatedDir,"Wallpaper_01*"),launcherNodpiDir);
				}else{
					BinUtil.copy(ComUtil.pathConcat(deprecatedDir,"*"),launcherNodpiDir);
				}
				wallpaperCount++; //just indicate has wallpaper
			}
			//handle old version problem end
			
			if(isCandidateModified){
				String[] srcFiles = new File(srcCandidatePath).list();
				int maxCount = Math.min(srcFiles.length,MAX_WALLPAPERS);
				int index = CANDIDATE_WALLPAPER_START_IDX;
				for(int i=0;i<maxCount;i++){
					if(!ComUtil.isValidPictureFile(srcFiles[i])){
						Log.i("invalid wallpaper file "+srcFiles[i]);
						continue;
					}
					String dstPath = ComUtil.pathConcat(srcTmpDir,getWallpaperName(index,false));
					String srcCpPath = ComUtil.pathConcat(srcCpDir,ComUtil.strConcatWithDot(mDF.format(index),ComUtil.getFileNameSuffix(srcFiles[i])));
					BinUtil.copy(ComUtil.pathConcat(srcCandidatePath,srcFiles[i]),srcCpPath);
					BinUtil.convertImage(ComUtil.pathConcat(srcCandidatePath,srcFiles[i]), dstPath);
					BinUtil.resizeImage(dstPath, ComUtil.pathConcat(srcTmpDir,getWallpaperName(index,true)), SMALL_SIZE);
					wallpaperCount++;
					index++;
				}

				if(rmNonCandidateWallpapers){
					//backup default wallpaper
					if(!isDefaultModified){
						for(String drawable : WALLPAPER_DRAWABLES){
							String defaultWallpaepr = ComUtil.pathConcat("res",drawable,getWallpaperName(DEFAULT_WALLPAPER_IDX,false));
							BinUtil.unzipExtract(launcherPath, defaultWallpaepr, launcherTmpDir);
							defaultWallpaepr = ComUtil.pathConcat("res",drawable,getWallpaperName(DEFAULT_WALLPAPER_IDX,true));
							BinUtil.unzipExtract(launcherPath, defaultWallpaepr, launcherTmpDir);
							wallpaperCount++; //just indicate has wallpaper
						}
					}
					StringBuilder delPathBuilder = new StringBuilder();
					for(String drawable : WALLPAPER_DRAWABLES){
						delPathBuilder.append(" ");
						delPathBuilder.append(ComUtil.pathConcat("res",drawable,"wallpaper_*"));
					}
					BinUtil.zipDelete(launcherPath,delPathBuilder.toString());
				}
			}
			
			if(wallpaperCount == 0){
				return;
			}
			
			//BinUtil.mkdir(launcherTmpDir);
			int startIdx = isDefaultModified ? DEFAULT_WALLPAPER_IDX : CANDIDATE_WALLPAPER_START_IDX;
			StringBuilder sb = new StringBuilder();
			for(String drawable : WALLPAPER_DRAWABLES){
				String dstDir = ComUtil.pathConcat(launcherTmpDir,"res",drawable);
				BinUtil.mkdir(dstDir);
				BinUtil.copy(ComUtil.pathConcat(srcTmpDir,"*"),dstDir);

				sb.append(" ");
				sb.append(ComUtil.strWithQuotation(ComUtil.pathConcat("res",drawable,"*")));
			}
			BinUtil.zipUpdateApk(launcherTmpDir, launcherPath, sb.toString());
			
			if(isDefaultModified){
				mDefaultItem.markState(STATE_SUCCESS);
			}
			if(isCandidateModified){
				mCandidateItem.markState(STATE_SUCCESS);
			}
			Log.i(">>>>>>>>>>>>>>>>>>>>>>>>>>modify wallpaper end");
		}

		private String getLauncherApkPath(){
			String[] launcherPaths = new String[]{
				ComUtil.pathConcat(ComUtil.SYSTEM_DIR,"app","Launcher2.apk"),
				ComUtil.pathConcat(ComUtil.SYSTEM_DIR,"priv-app","Launcher2.apk")
			};
			for(String path : launcherPaths){
				File launcherApk = new File(path);
				if(launcherApk.exists()){
					return path;
				}
			}
			return null;
		}

		private String getWallpaperName(int index, boolean isSmall){
			String suffix = isSmall ? "_small.jpg" : ".jpg";
			return ComUtil.strConcat("wallpaper_",mDF.format(index),suffix);
		}
	}

	private class FatItemView extends MediaItemView{
		//private String mRomPath;
		private static final String FAT_IMG_NAME = "fat_sparse.img";
		
		public FatItemView(String label, int selMode, FileFilter filter, String title, Rectangle rect) {
			super(label, selMode, filter, title, rect);
		}

		public FatItemView(String label, int selMode, FileFilter filter, String title, Rectangle rect, String romPath) {
			super(label, selMode, filter, title, rect);
			mRomPath = romPath;
		}

		@Override
		public boolean doRealModify() {
			boolean success = false;
			BinUtil.copy(getMediaPath(), ComUtil.pathConcat(mRomPath,FAT_IMG_NAME));
			String tmpScatFilePath = ComUtil.pathConcat(ComUtil.OUT_DIR,"scatter_src.txt");
			String outScatFilePath = ComUtil.pathConcat(ComUtil.OUT_DIR,"scatter_out.txt");
			String scatterFilePath = getScatterPath(mRomPath);
			BinUtil.copy(scatterFilePath,tmpScatFilePath);
			try{
				File out = new File(outScatFilePath);
				BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(out));
				
				File in = new File(tmpScatFilePath);
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(in)));
				boolean fat_begin = false;
				String read;
				while ((read = br.readLine()) != null){
					if(!fat_begin){
						if ((read.indexOf("FAT")) != -1){					
							fat_begin = true;
						}
					}else{					
						if((read.indexOf("file_name")) != -1){
							read = read.replace("NONE","fat_sparse.img");
						}else if((read.indexOf("is_download")) != -1){
							read = read.replace("false","true");
							fat_begin = false;
						}
					}
					fos.write(read.getBytes());
					fos.write("\n".getBytes());
				}
				fos.flush();
				fos.close();
				br.close();

				BinUtil.copy(outScatFilePath,scatterFilePath);
				success = true;
			}catch (FileNotFoundException e){
				Log.i(e.toString());
			}catch (IOException e){
				Log.i(e.toString());
			}
			BinUtil.rm(tmpScatFilePath);
			BinUtil.rm(outScatFilePath);
			return success;
		}

		public String getScatterPath(String romPath){
			String scatterFilePath = null;
			
			File romPathFile = new File(romPath);
			String[] romFiles = romPathFile.list();
			for(String romFile : romFiles){
				if(romFile.endsWith("Android_scatter.txt")){
					scatterFilePath = romFile;
					break;
				}
			}
			return ComUtil.pathConcat(romPath,scatterFilePath);
		}

		public boolean isSystemModifiedOk(){
			return false;
		}
	}

	private class DummyItemView extends MediaItemView{
		public DummyItemView(String label, int selMode, FileFilter filter, String title, Rectangle rect) {
			super(label, selMode, filter, title, rect);
		}
		
		@Override
		public void addView(JPanel panel, int x, int y){
		}

		@Override
		public void doModify() {
		}
	}
}
