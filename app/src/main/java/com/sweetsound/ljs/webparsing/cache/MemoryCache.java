package com.sweetsound.ljs.webparsing.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * Created by ljeongseok on 2018. 4. 12..
 *
 * https://developer.android.com/topic/performance/graphics/cache-bitmap.html 참고 했음.
 */

public class MemoryCache {
    private LruCache<String, Bitmap> mCache;

    public MemoryCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // 어플이 사용할 수 있는 메모리(KB)의 1/8을 메모리 캐쉬로 설정
        final int cacheSize = maxMemory / 8;

        mCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void addBitmap(String imageUrl, Bitmap bitmap) {
        if (getBitmap(imageUrl) == null) {
            mCache.put(imageUrl, bitmap);
        }
    }

    public void clear() {
        mCache.evictAll();
    }

    public Bitmap getBitmap(String imageUrl) {
        return mCache.get(imageUrl);
    }
}
