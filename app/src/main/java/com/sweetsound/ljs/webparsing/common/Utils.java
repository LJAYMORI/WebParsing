package com.sweetsound.ljs.webparsing.common;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by ljeongseok on 2018. 4. 12..
 */

public class Utils {
    private Utils() {};

    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath =
        Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
        !isExternalStorageRemovable() ? getExternalCacheDir(context).getPath() : context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }


    private static boolean isExternalStorageRemovable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return Environment.isExternalStorageRemovable();
        }

        return true;
    }

    private static File getExternalCacheDir(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            return context.getExternalCacheDir();
        }

        return new File(Environment.getExternalStorageDirectory().getPath()
                + "/Android/data/" + context.getPackageName() + "/cache/");
    }

    public static boolean copyStream(InputStream is, OutputStream os, String url) {
        boolean result = false;

        final int BUFFER_SIZE = 1024;

        try {
            byte[] bytes = new byte[BUFFER_SIZE];

            while (true) {
                int count = is.read(bytes, 0, BUFFER_SIZE);

                if (count == -1) {
                    break;
                }

                os.write(bytes, 0, count);
            }

            result = true;
        } catch (Exception ex) {
            Log.e("TAG", "LJS== " + url + " copyStream Exception : " + Utils.getExceptionString(ex));
        }

        return result;
    }

    public static String getExceptionString(Exception exception)
    {
        String exceptionString = null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        exception.printStackTrace(ps);

        exceptionString = baos.toString();

        try
        {
            if(baos != null)
                baos.close();

            if(ps != null)
                ps.close();
        }
        catch (IOException ex){}

        return exceptionString;
    }
}
