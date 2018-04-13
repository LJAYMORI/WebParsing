package com.sweetsound.ljs.webparsing.parser;

import com.sweetsound.ljs.webparsing.common.TransactionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;

/**
 * Created by ljeongseok on 2018. 4. 12..
 */

public class WebParser {
    private static final String TAG = WebParser.class.getSimpleName();
    // image tag를 찾기 위한 값
    private static final String FIND_START_TAG = "g src=\"";
    private static final String FIND_END_TAG = "\"";

    /** 이미지 Url을 가져온다.
     * @param preUrl 웹 페이지의 공통된 앞쪽 주소
     * @param postUrl 웹 페이지의 뒷쪽 주소
     * @return 이미지 Url이 있는 List
     */
    public ArrayList<String> getImageUrlList(String preUrl, String postUrl) {
        ArrayList<String> imageUrlList = new ArrayList<String>();

        String url = preUrl + postUrl;
        String readData = null;
        String imageUrl = null;

        BufferedReader br = null;

        try {
            TransactionUtils transactionUtils = new TransactionUtils();
            HttpURLConnection conn = transactionUtils.getConnection(url);

            // 한줄씩 읽기 위해 BufferedReader를 사용
            br =  new BufferedReader(new InputStreamReader(conn.getInputStream()));

            // 해당 페이지를 모두 읽어 필요한 정보를 가져온다.
            while(true) {
                readData = br.readLine();

                if(readData == null)
                    break;

                imageUrl = findString(readData);

                if(imageUrl != null)
                    imageUrlList.add(preUrl + imageUrl);
            }

            br.close();
            br = null;
        } catch(Exception ex) {
            imageUrlList.clear();
        } finally {
            try {
                if(br != null) {
                    br.close();
                }
            } catch (IOException e) {}
        }

        return imageUrlList;
    }

    /** 이미지 Url의 뒷쪽 주소를 찾아 반환 한다.
     * @param readData 찾기 위해 읽어 들인 Data
     * @return 이미지 Url 뒤쪽 주소
     */
    private String findString(String readData) {
        String result = null;

        int startIndex = readData.indexOf(FIND_START_TAG);
        int endIndex = 0;

        if(startIndex > 0) {
            startIndex += FIND_START_TAG.length();
            endIndex = readData.indexOf(FIND_END_TAG, startIndex);
            result = readData.substring(startIndex, endIndex);

            // 앞쪽에 File Separator가 없는 것이 있어 확인 후 붙여 준다.
            if(result.indexOf("..") == 0)
                result = "/" + result;
        }

        return result;
    }
}
