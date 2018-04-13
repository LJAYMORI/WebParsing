package com.sweetsound.ljs.webparsing.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.sweetsound.ljs.webparsing.cache.DiskCache;
import com.sweetsound.ljs.webparsing.cache.MemoryCache;
import com.sweetsound.ljs.webparsing.common.TransactionUtils;
import com.sweetsound.ljs.webparsing.common.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ljeongseok on 2018. 4. 12..
 */

public class ImageLoader {
    private static final String TAG = ImageLoader.class.getSimpleName();

    private static ImageLoader mInstance;

    private MemoryCache mMemoryCache;
    private DiskCache mDiskCache;

    private ConcurrentHashMap<ImageView, String> mDisplayUrlMap = new ConcurrentHashMap<ImageView, String>();
    private ConcurrentHashMap<ImageView, AsyncTask> mUsingAsyncTasklMap = new ConcurrentHashMap<ImageView, AsyncTask>();
    private List<AsyncTask> mAsyncTaskCache = Collections.synchronizedList(new ArrayList<AsyncTask>());

    private ImageLoader(Context context) {
        mMemoryCache = new MemoryCache();
        mDiskCache = new DiskCache(context);
    }

    public static synchronized ImageLoader getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ImageLoader(context);
        }

        return mInstance;
    }

    /** 이미지를 불러와 Display한다.
     * @param imageView 이미지를 담을 ImageView
     * @param imageUrl 이미지 Url
     */
    public void setImage(final ImageView imageView, final ImageView loadingImageView, final String imageUrl) {

        // imageview가 재사용 되기 전의 AsyncTask는 종료시킨다.(Thread pool max가 128)
        String compareUrl = mDisplayUrlMap.get(imageView);

        if (compareUrl != null && compareUrl.equals(imageUrl) == false) {
            AsyncTask asyncTask = mUsingAsyncTasklMap.get(imageView);

            if (asyncTask != null) {
                asyncTask.cancel(true);
                mUsingAsyncTasklMap.remove(imageView);
            }
        }

        mDisplayUrlMap.put(imageView, imageUrl);
        Bitmap memoryBitmap = mMemoryCache.getBitmap(imageUrl);

        if (memoryBitmap != null) {
            loadingImageView.setVisibility(View.GONE);
            imageView.setImageBitmap(memoryBitmap);
        } else {
            AsyncTask<String, Object, ReturnResult> loadBitmapAsyncTask = new AsyncTask<String, Object, ReturnResult>() {
                @Override
                protected ReturnResult doInBackground(String[] params) {
                    String url = params[0];

                    // 디스크 케쉬에서 꺼내고
                    Bitmap bitmap = mDiskCache.getBitmap(DiskCache.generateKey(url));

                    if (bitmap == null) {
                        // 서버에서 받아와야 함.
                        try {
                            TransactionUtils transactionUtils = new TransactionUtils();
                            HttpURLConnection conn = transactionUtils.getConnection(url);

                            bitmap = decodeFile(conn.getInputStream(), url);

                            if (bitmap == null) {
                                Log.e("TAG", "LJS== bitmap : " + bitmap);
                                return null;
                            }
                        } catch (Exception ex) {
                            Log.e(TAG, "LJS== " + url + "Exception : " + Utils.getExceptionString(ex));
                            Log.e("TAG", "LJS== bitmap : " + bitmap);

                            return null;
                        }

                        mDiskCache.putBitmap(DiskCache.generateKey(imageUrl), bitmap, DiskCache.getCompressFormat(imageUrl));
                    }

                    mMemoryCache.addBitmap(url, bitmap);

                    ReturnResult returnResult = new ReturnResult(bitmap, url);
                    return returnResult;
                }

                @Override
                protected void onPostExecute(ReturnResult returnResult) {
                    if (isCancelled() == false) {
                        if (returnResult != null && returnResult.bitmap != null && mDisplayUrlMap.get(imageView).equals(returnResult.imageUrl)) {
                            loadingImageView.setVisibility(View.GONE);
                            imageView.setImageBitmap(returnResult.bitmap);
                        }
                    }

                    mAsyncTaskCache.remove(this);
                }
            };

            mUsingAsyncTasklMap.put(imageView, loadBitmapAsyncTask);
            mAsyncTaskCache.add(loadBitmapAsyncTask);

            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
                loadBitmapAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imageUrl);
            } else {
                loadBitmapAsyncTask.execute(imageUrl);
            }
        }
    }

    private Bitmap decodeFile(InputStream is, String url) {
        Bitmap bitmap = null;

        ByteArrayOutputStream bos = null;
        InputStream bitmapIs = null;

        boolean copyResult = false;

        try {
            // 크기 조절을 위해 inputstream을 복사 한다.
            bos = new ByteArrayOutputStream();
            copyResult = Utils.copyStream(is, bos, url);

            // 이전 AsyncTask를 cancel하면 Stream을 copy하다 오류가 발생해서 이미지가 깨지게 된다. 깨진 이미지는 버림.
            if (copyResult == false) {
                return null;
            }

            bitmapIs = new ByteArrayInputStream(bos.toByteArray());

            BitmapFactory.Options sizeOptions = new BitmapFactory.Options();
            sizeOptions.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(is, null, sizeOptions);

            // 적당한 크기로 크기를 줄인다.
            final int REQUIRED_SIZE = 80;
            int width_tmp = sizeOptions.outWidth, height_tmp = sizeOptions.outHeight;
            int scale = 1;

            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
                    break;
                }

                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inSampleSize = scale;
            bitmap = BitmapFactory.decodeStream(bitmapIs, null, bitmapOptions);
        } catch (Exception ex) {
            Log.e(TAG, "LJS== " + url + " decodeFile Exception : " + Utils.getExceptionString(ex));
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception ex) {}

            try {
                if (bitmapIs != null) {
                    bitmapIs.close();
                }
            } catch (Exception ex) {}

            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception ex) {}
        }

        return bitmap;
    }

    public void clear() {
        mDiskCache.clearCache();

        for (AsyncTask asyncTask : mAsyncTaskCache) {
            asyncTask.cancel(true);
        }

        mAsyncTaskCache.clear();
    }



    private class ReturnResult {
        public Bitmap bitmap;
        public String imageUrl;

        public ReturnResult(Bitmap bitmap, String imageUrl) {
            this.bitmap = bitmap;
            this.imageUrl = imageUrl;
        }

        private boolean isSame(String targetUrl) {
            boolean result = false;

            if (imageUrl != null) {
                result = imageUrl.equals(targetUrl);
            }

            return result;
        }
    }
}
