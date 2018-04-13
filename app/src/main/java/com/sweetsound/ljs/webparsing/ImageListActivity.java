package com.sweetsound.ljs.webparsing;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.sweetsound.ljs.webparsing.adapter.ImageListAdapter;
import com.sweetsound.ljs.webparsing.loader.ImageLoader;
import com.sweetsound.ljs.webparsing.parser.WebParser;

import java.util.ArrayList;

public class ImageListActivity extends AppCompatActivity {
    private static final int LOADING_SIZE = 20;

    private static final String PRE_WEB_PAGE = "http://www.gettyimagesgallery.com";
    private static final String POST_WEB_PAGE = "/collections/archive/slim-aarons.aspx";

    private ImageListAdapter mImageListAdapter;

    private RecyclerView mImageListView;

    private int mStartIndex = 0;
    private int mEndIndex = LOADING_SIZE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_list);

        mImageListView = (RecyclerView) findViewById(R.id.image_listview);
        mImageListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mImageListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        parsingWeb();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ImageLoader.getInstance(ImageListActivity.this).clear();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /** 이미지 Url을 가져오기 위해 web을 parsing한다.
     */
    private void parsingWeb() {
        AsyncTask<Object, Object, ArrayList<String>> parsingAsyncTask = new AsyncTask<Object, Object, ArrayList<String>>()
        {
            private ProgressDialog mProgressDialog;

            @Override
            protected void onPreExecute()
            {
                mProgressDialog = new ProgressDialog(ImageListActivity.this);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setMessage(getString(R.string.proc_web_parsing));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
            }

            @Override
            protected ArrayList<String> doInBackground(Object... params)
            {
                // 웹 페이지에서 필요한 이미지 정보를 가져온다.
                WebParser webParser = new WebParser();
                ArrayList<String> imageUrlList = webParser.getImageUrlList(PRE_WEB_PAGE, POST_WEB_PAGE);

                return imageUrlList;
            }

            @Override
            protected void onPostExecute(ArrayList<String> imageUrlList)
            {
                for (String url : imageUrlList) {
                    Log.e("TAG", "LJS== imageUrlList : " + url);
                }

                if(imageUrlList.size() < 1) {
                    Toast.makeText(getApplicationContext(), R.string.error_fail_web_parsing, Toast.LENGTH_SHORT).show();
                } else {
                    mImageListAdapter = new ImageListAdapter(imageUrlList);
                    mImageListView.setAdapter(mImageListAdapter);
                }

                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        };

        parsingAsyncTask.execute();
    }
}
