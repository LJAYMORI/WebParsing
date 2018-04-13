package com.sweetsound.ljs.webparsing.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.jakewharton.disklrucache.DiskLruCache;
import com.sweetsound.ljs.webparsing.common.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by ljeongseok on 2018. 4. 12..
 *
 * com.jakewharton.disklrucache.DiskLruCache 라이브러리 사용
 */

public class DiskCache {
    private static final String DISK_CACHE_SUBDIR = "thumbnails";

    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final int BUFFER_SIZE = 8 * 1024;

    private DiskLruCache mDiskCache;

    public DiskCache(Context context) {
        try {
            mDiskCache = DiskLruCache.open(Utils.getDiskCacheDir(context, DISK_CACHE_SUBDIR), 1, 1, DISK_CACHE_SIZE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void putBitmap(String key, Bitmap bitmap, Bitmap.CompressFormat compressFormat) {
        DiskLruCache.Editor editor = null;

        try {
            editor = mDiskCache.edit(key);
            if (editor == null) {
                return;
            }

            if (writeBitmapToFile(bitmap, editor, compressFormat)) {
                mDiskCache.flush();
                editor.commit();
            } else {
                editor.abort();
            }
        } catch (Exception e) {
            try {
                if (editor != null) {
                    editor.abort();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor, Bitmap.CompressFormat compressFormat)
            throws IOException, FileNotFoundException {
        OutputStream out = null;

        try {
            out = new BufferedOutputStream(editor.newOutputStream(0), BUFFER_SIZE);
            return bitmap.compress(compressFormat, 100, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public Bitmap getBitmap(String key) {
        Bitmap bitmap = null;
        DiskLruCache.Snapshot snapshot = null;

        try {
            snapshot = mDiskCache.get(key);

            if (snapshot == null) {
                return null;
            }

            InputStream in = snapshot.getInputStream(0);

            if (in != null) {
                BufferedInputStream buffIn =
                        new BufferedInputStream(in, BUFFER_SIZE);
                bitmap = BitmapFactory.decodeStream(buffIn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }

        return bitmap;
    }

    public void clearCache() {
        try {
            mDiskCache.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String generateKey(String source) {
        return String.valueOf(source.hashCode());
    }

    public static Bitmap.CompressFormat getCompressFormat(String imageFileName) {
        int dotIndex = imageFileName.lastIndexOf(".");
        String fileExt = imageFileName.substring(dotIndex + 1).toLowerCase();

        Bitmap.CompressFormat format = null;

        switch (fileExt) {
            case "png":
                format = Bitmap.CompressFormat.PNG;
                break;

            case "webp":
                format = Bitmap.CompressFormat.WEBP;
                break;

            case "jpg":
            case "jpeg":
            default:
                format = Bitmap.CompressFormat.JPEG;
                break;
        }

        return format;
    }
}
