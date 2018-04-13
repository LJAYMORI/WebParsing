package com.sweetsound.ljs.webparsing.adapter;

import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sweetsound.ljs.webparsing.R;
import com.sweetsound.ljs.webparsing.loader.ImageLoader;

import java.util.ArrayList;

/**
 * Created by ljeongseok on 2018. 4. 12..
 */

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ItemViewHolder>{

    private ArrayList<String> mImageUrlList;

    public ImageListAdapter(ArrayList<String> imageUrlList) {
        mImageUrlList = imageUrlList;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listitem = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item_layout, parent, false);
        return new ItemViewHolder(listitem);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.setData(mImageUrlList.get(position));
    }

    @Override
    public int getItemCount() {
        if (mImageUrlList == null) {
            return 0;
        } else {
            return mImageUrlList.size();
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private ImageView mItemImageView;
        private ImageView mLoadingImageView;

        private ImageLoader mImageLoader;

        public ItemViewHolder(View itemView) {
            super(itemView);

            mItemImageView = (ImageView) itemView.findViewById(R.id.item_imageview);
            mLoadingImageView = (ImageView) itemView.findViewById(R.id.loading_imageview);
            AnimationDrawable loadingAnimDrawable = (AnimationDrawable)mLoadingImageView.getBackground();
            loadingAnimDrawable.start();

            mImageLoader = ImageLoader.getInstance(itemView.getContext());
        }

        public void setData(String imageUrl) {
            // 캐쉬에서 꺼내와서 보여준다.
            mItemImageView.setImageBitmap(null);
            mLoadingImageView.setVisibility(View.VISIBLE);
            mImageLoader.setImage(mItemImageView, mLoadingImageView, imageUrl);
        }
    }
}
