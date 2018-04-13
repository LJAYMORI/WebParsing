package com.sweetsound.ljs.webparsing.common;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by ljeongseok on 2018. 4. 13..
 */

public class TransactionUtils {
    private static final int MAX_RETRY_COUNT  = 1;
    private static final int CONNECTION_TIMEOUT = 30000;

    private int mRetryCount;

    public TransactionUtils() {
        mRetryCount = 0;
    }

    public HttpURLConnection getConnection(String url) throws MalformedURLException, IOException {
        HttpURLConnection conn = null;

        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(CONNECTION_TIMEOUT);
            conn.setInstanceFollowRedirects(true);

            mRetryCount = 0;
        } catch (SocketTimeoutException ex) {
            if(mRetryCount < MAX_RETRY_COUNT) {
                // 연결 오류는 다시 시도한다.
                getConnection(url);
                mRetryCount++;
            }
        }

        return conn;
    }
}
