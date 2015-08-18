/**
 * Copyright 2012 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.feinno.beside.utils.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.ImageView;

import cn.com.fetion.R;

import com.feinno.beside.application.BesideInitUtil;
import com.feinno.beside.utils.UIUtils;
import com.feinno.beside.utils.log.LogSystem;
import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LRULimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecodingInfo;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A subclass of {@link ImageWorker} that fetches images from a URL. 
 * If you often got OutOfMemoryError in your app using Universal Image Loader then try
 * next (all of them or several):
 * 
 * Reduce thread pool size in configuration (.threadPoolSize(...)). 1 - 5 is recommended. 
 * Use bitmapConfig(Bitmap.Config.RGB_565) in display options.
 * Bitmaps in RGB_565 consume 2 times less memory than in ARGB_8888.
 * Use memoryCache(new WeakMemoryCache()) in configuration or disable caching in  memory at all in display options (don't call .cacheInMemory()).
 * Use imageScaleType(ImageScaleType.IN_SAMPLE_INT) in display options Or try.imageScaleType(ImageScaleType.EXACTLY). 
 * Avoid using RoundedBitmapDisplayer It creates new Bitmap object with ARGB_8888 config for displaying during work.
 */
public class ImageFetcher{
	
	private static ImageFetcher INSTANCE;
	private static Context context;
	private static ImageLoader imageLoader;
	private static DisplayImageOptions defaultOptions;
	private static ImageLoaderConfiguration configuration = null;
	private static FetionFileNameGenertaor fileNameGenertaor = null;
	private static ImageDownloader imageDownloader = null;;
	private static FetionLruMemoryCache cache = null;
	private static LocalFileImageLoader localFileImageLoader;
	public static final int MEMORY_CACHE_HEIGHT = 0;
	public static final int MEMORY_CACHE_WIDTH = 0;
	private static final int CACHE_SIZE = (int)Runtime.getRuntime().maxMemory() / 8; 
	private ImageFetcher() {
		createImageLoader();
	}
	
	private static void createImageLoader() {
		imageLoader = ImageLoader.getInstance();
		if (cache == null) {
			cache = new FetionLruMemoryCache();
		}
		if (imageLoader.isInited() && imageLoader.getMemoryCache() == cache ) {
			return;
		}
		LogSystem.d(TAG, "create imageloader ,init all config...");
		try {
			imageLoader.destroy();
		} catch (Exception e) {}
		fileNameGenertaor = new FetionFileNameGenertaor();
		imageDownloader = new FetionImageDownloader(context);
		defaultOptions = new DisplayImageOptions.Builder()
		.cacheInMemory(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.cacheOnDisc(true)
		.build();
		final String cachePath = getExternalCacheDir(context).getPath();
		File cacheDir = new File(cachePath);
		configuration = new ImageLoaderConfiguration.Builder(context)
		.discCacheFileNameGenerator(fileNameGenertaor)
		.memoryCache(cache)
		.memoryCacheExtraOptions(MEMORY_CACHE_WIDTH, MEMORY_CACHE_HEIGHT)
		.discCacheExtraOptions(480, 800, CompressFormat.JPEG, 70, null)
		.threadPoolSize(3)
		.threadPriority(Thread.MIN_PRIORITY)
		.writeDebugLogs()
		.imageDecoder(new BesideDecoder(LogSystem.DEBUG))
		.tasksProcessingOrder(QueueProcessingType.LIFO)
		.imageDownloader(imageDownloader)
		.discCache(new FetionDiscCache(cacheDir,60*60*24*7)).build();
		imageLoader.init(configuration);
	}
	
	/**
	 * 图片默认的的缓存配置:缓存到本地，缓存到内存,未设置默认图片
	 * @return
	 */
	public DisplayImageOptions getDefaultOptions() {
		return defaultOptions;
	}
	
    public void setDefaultOptioins(DisplayImageOptions defaultOptions) {
        ImageFetcher.defaultOptions = defaultOptions;
    }
	
	public static ImageLoaderConfiguration getConfiguration() {
		return configuration;
	}
	
	public static final ImageFetcher getFetcherInstance(Context cxt) {
		context = cxt;
		synchronized (ImageFetcher.class) {
			if (INSTANCE == null) {
				INSTANCE = new ImageFetcher();
			}
		}
		return INSTANCE;
	}

	/**
	 * 获取 圆形头像设置
	 * @param id   默认图id
	 * @return
	 */
	public DisplayImageOptions getRoundedOptions(int id) {
        return new DisplayImageOptions.Builder().cloneFrom(defaultOptions)
                .displayer(new RoundedBitmapDisplayer(80))
                .showImageForEmptyUri(id)
                .showImageOnFail(id)
                .showStubImage(id).build();
	}
	/**
	 * 获取 圆形头像设置
	 * @param id   默认图id
	 * @return
	 */
	public DisplayImageOptions getHelpRoundedOptions(int id) {
        return new DisplayImageOptions.Builder().cloneFrom(defaultOptions)
                .displayer(new RoundedBitmapDisplayer(90))
                .showImageForEmptyUri(id)
                .showImageOnFail(id)
                .showStubImage(id).build();
	}
	/**
	 * 设置获取图片圆角
	 * @param id
	 * @return
	 */
	public DisplayImageOptions getRoundIconOption(int id){
		return new DisplayImageOptions.Builder().cloneFrom(defaultOptions)
                .displayer(new RoundedBitmapDisplayer(10))
                .showImageForEmptyUri(id)
                .showImageOnFail(id)
                .showStubImage(id).build();
	}
	public static final int IO_BUFFER_SIZE_BYTES = 1 * 1024; // 1KB

	public void loadImage(String url,ImageView imageView,int resId) {
		loadImage(url, imageView, resId, null);
	}
	
	public void loadImage(String url,ImageView imageView,int resId,ImageLoadingListener listener) {
		if (imageLoader == null || !imageLoader.isInited() || imageLoader.getMemoryCache() != cache) {
			createImageLoader();
		}
		DisplayImageOptions options = new DisplayImageOptions.Builder().cloneFrom(defaultOptions)
				.showImageForEmptyUri(resId)
				.showImageOnFail(resId)
				.showStubImage(resId).build();
		loadImage(url, imageView, options, listener);
	}
	
	@SuppressWarnings("deprecation")
	public void loadImage(String uri, ImageView imageView, DisplayImageOptions options, ImageLoadingListener listener) {
		if (uri != null && uri.indexOf("{0}") != -1
				&& BesideInitUtil.getCKEY() != null) {
			uri = uri.replace("{0}",
					URLEncoder.encode(BesideInitUtil.getCKEY()));
		}
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			if (imageLoader == null || !imageLoader.isInited() || imageLoader.getMemoryCache() != cache) {
				createImageLoader();
			}
			imageLoader.displayImage(uri, imageView, options, listener);
		} else {
			if (options.shouldShowStubImage()) {
				imageView.setImageResource(options.getStubImage());
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public void loadImage(String uri,ImageLoadingListener listener) {
		if (uri != null && uri.indexOf("{0}") != -1
				&& BesideInitUtil.getCKEY() != null) {
			uri = uri.replace("{0}",
					URLEncoder.encode(BesideInitUtil.getCKEY()));
		}
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			if (imageLoader == null || !imageLoader.isInited() || imageLoader.getMemoryCache() != cache) {
				createImageLoader();
			}
			imageLoader.loadImage(uri, listener);
		}
	}
	
	public ImageLoader getImageLoader () {
		if (imageLoader == null || !imageLoader.isInited() ||  imageLoader.getMemoryCache() != cache) {
			createImageLoader();
		}
		return imageLoader;
	}
	
	public static File getExternalCacheDir(Context context) {

		if (hasExternalCacheDir()) {
			File cacheDir = context.getExternalCacheDir();
			if (cacheDir != null) {
				return cacheDir;
			}
		}
		final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
		return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
	}
	
	public static File getExternalGifCacheDir(Context context) {
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/gif/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }
	
	/**
	 * 获取缓存目录下单个附件的文件
	 */
	public static File getExternalCacheFile(Context context, String localName){
		File cacheDir = getExternalCacheDir(context);
		File localFile = new File(cacheDir, localName);
		return localFile;
	}
	
	/**
     * 获取GIF缓存目录下单个附件的文件
     */
    public static File getExternalGifCacheFile(Context context, String localName){
        File cacheDir = getExternalGifCacheDir(context);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        File localFile = new File(cacheDir, localName);
        return localFile;
    }
	
	public static boolean hasExternalCacheDir() {
		return UIUtils.hasFroyo();
	}
	
	private static class BesideDecoder extends BaseImageDecoder {

        public BesideDecoder(boolean loggingEnabled) {
            super(loggingEnabled);
        }
        
        @Override
        protected Options prepareDecodingOptions(ImageSize srcSize, ImageDecodingInfo decodingInfo) {
            Options ops = decodingInfo.getDecodingOptions();;
            ImageScaleType scaleType = decodingInfo.getImageScaleType();
            ImageSize targetSize = decodingInfo.getTargetSize();
            int srcWidth = srcSize.getWidth();
            int srcHeight = srcSize.getHeight();
            int scale = 1;
            Object tag = decodingInfo.getExtraForDownloader();
            if(Math.max(srcWidth, srcHeight) > Math.min(srcWidth, srcHeight) * 3 && tag != null) {
                scale = 1;
            } else {
                int targetWidth = targetSize.getWidth();
                int targetHeight = targetSize.getHeight();
                int widthScale = srcWidth / targetWidth;
                int heightScale = srcHeight / targetHeight;
                boolean powerOf2Scale = scaleType == ImageScaleType.IN_SAMPLE_POWER_OF_2;
                switch (decodingInfo.getViewScaleType()) {
                    case FIT_INSIDE:
                        if (powerOf2Scale) {
                            while (srcWidth / 2 >= targetWidth || srcHeight / 2 >= targetHeight) { // ||
                                srcWidth /= 2;
                                srcHeight /= 2;
                                scale *= 2;
                            }
                        } else {
                            scale = Math.max(widthScale, heightScale); // max
                        }
                        break;
                    case CROP:
                        if (powerOf2Scale) {
                            while (srcWidth / 2 >= targetWidth && srcHeight / 2 >= targetHeight) { // &&
                                srcWidth /= 2;
                                srcHeight /= 2;
                                scale *= 2;
                            }
                        } else {
                            scale = Math.min(widthScale, heightScale); // min
                        }
                        break;
                }
            }
            if (scale < 1) {
                scale = 1;
            }
            ops.inSampleSize = scale;
            return ops;
        }

	}
	private static class FetionLruMemoryCache extends LRULimitedMemoryCache {
		public FetionLruMemoryCache(int maxSize) {
			super(maxSize);
		}
		
		public FetionLruMemoryCache() {
			this(CACHE_SIZE);
			LogSystem.d("CACHE_SIZE", CACHE_SIZE/1024/1024+"MB");
		}
		
		@Override
		public Bitmap get(String key) {
		    LogSystem.d("CACHE_SIZE", "get,key = "+key);
			return super.get(filteKey(key));
		}
		
		@Override
		public boolean put(String key, Bitmap value) {
			String filterKey = filteKey(key);
			boolean res = super.put(filteKey(key), value);
			LogSystem.d("CACHE_SIZE", "put,res = " + res +" filterKey = " + filterKey);
			return res;
		}
		
		@Override
		public void remove(String key) {
			String filterKey = filteKey(key);
			LogSystem.d("CACHE_SIZE", "remove,filterKey = " + filterKey + "   key = " + key);
			super.remove(filterKey);
		}
	}
	
	private static class FetionImageDownloader extends BaseImageDownloader {

		public FetionImageDownloader(Context context) {
			super(context);
		}
		
		@Override
		protected InputStream getStreamFromNetwork(String imageUri, Object extra)
				throws IOException {
			HttpClient httpClient = new HttpClient();
			HttpRequest request = new HttpRequest(imageUri.toString(), HttpRequest.GET);
			request.addHeader("Accept", "*/*");
			request.setReadTimeout(20000);
			request.setNeedStream(true);
			HttpResponse response = null;
			try {
				response = httpClient.send(request);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (response != null && response.getInputStream() != null) {
				return response.getInputStream();
			} else {
				return super.getStreamFromNetwork(imageUri, extra);
			}
		}
		
		@Override
		protected InputStream getStreamFromFile(String imageUri, Object extra)
				throws IOException {
			if (localFileImageLoader != null) {
				String filePath = Scheme.FILE.crop(imageUri);
				filePath = localFileImageLoader.compressBeforeLoade(filePath, extra);
				imageUri = Scheme.FILE.wrap(filePath);
				return super.getStreamFromFile(imageUri, extra);
			}
			return super.getStreamFromFile(imageUri, extra);
		}
	}
	
	private static final String TAG = "FetionImageLoader";
	private static class FetionDiscCache extends LimitedAgeDiscCache {

		public FetionDiscCache(File cacheDir,int time) {
			super(cacheDir, time);
		}
		
		@Override
		public void put(String key, File file) {
			LogSystem.d(TAG, "put disc cache,key = " + key + "  file = " + file.getAbsolutePath());
			String filterKey = filteKey(key);
			super.put(filterKey, file);
		}
		
		@Override
		public File get(String key) {
			key = filteKey(key);
			File file = super.get(key);
			LogSystem.d(TAG, "get disc cache,key = " + key + "  file = " + (file==null?"":file.getAbsolutePath()));
			return file;
		}
		
	}

	private static class FetionFileNameGenertaor extends HashCodeFileNameGenerator {
		
		@Override
		public String generate(String imageUri) {
			return super.generate(filteKey(imageUri));
		}
	}
	
	private static String filteKey(String url) {
		if (TextUtils.isEmpty(url)) {
			return url;
		}
		if (!url.contains("&c=") && !url.contains("\\?c=")) {
			return url;
		}
		String[] params = null; 
		params = url.split("\\?");
		if (params == null || params.length < 2) {
			return url;
		}
		String result = params[0];
		String paramStr = params[1];
		params = paramStr.split("&");
		if (params == null || params.length < 2) {
			return url;
		}
		Map<String, String> paramMap = new HashMap<String, String>();
		for(String param:params) {
			if (!TextUtils.isEmpty(param)) {
				String[] parArray = param.split("=");
				if (parArray != null && parArray.length > 1 && !"c".equals(parArray[0])) {
					paramMap.put(parArray[0], parArray[1]);
				}
			}
		}
		if (paramMap.size() > 0) {
			result += "?";
		}
		for(Iterator<Entry<String, String>> it = paramMap.entrySet().iterator();it.hasNext();) {
			Entry<String, String> entry = it.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if (it.hasNext()) {
				result = result + key + "=" + value + "&";
			} else {
				result = result + key + "=" + value;
			}
			System.out.println(key + "=" + value);
		}
		return result;
	}
	
	/**
	 * 设置图片加载器本地压缩图片的回调监听<br/>
	 * 注意也不不需要此回调时一定要清空掉，调用clearLocalFileImageLoader
	 * @param loader
	 */
	public void setLocalFileImageLoader(LocalFileImageLoader loader) {
		localFileImageLoader = loader;
	}
	/**
	 * 清空回调监听
	 */
	public void clearLocalFileImageLoader() {
		setLocalFileImageLoader(null);
	}
	
	/**
	 * 图片下载器加载本地图片前的压缩回调
	 * @author yuanshuqi
	 *
	 */
	public interface LocalFileImageLoader {
		/**
		 * 下载前压缩
		 * @param imageUri 下载图片的地址
		 * @param extra  标记，可以忽略
		 * @return  压缩图片完毕的地址
		 */
		String compressBeforeLoade(String imageUri, Object extra);
	}
}
