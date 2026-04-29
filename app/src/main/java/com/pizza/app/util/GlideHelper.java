package com.pizza.app.util;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.pizza.app.R;

/**
 * Wrapper tiện ích load ảnh với Glide — thống nhất placeholder và cache strategy
 */
public final class GlideHelper {

    private GlideHelper() {}

    public static void loadProduct(ImageView view, String url) {
        Glide.with(view.getContext())
             .load(url)
             .apply(new RequestOptions()
                     .placeholder(R.drawable.placeholder_food)
                     .error(R.drawable.placeholder_food)
                     .diskCacheStrategy(DiskCacheStrategy.ALL))
             .into(view);
    }

    public static void loadAvatar(ImageView view, String url) {
        Glide.with(view.getContext())
             .load(url)
             .apply(new RequestOptions()
                     .placeholder(R.drawable.ic_avatar_default)
                     .error(R.drawable.ic_avatar_default)
                     .circleCrop()
                     .diskCacheStrategy(DiskCacheStrategy.ALL))
             .into(view);
    }

    public static void loadBanner(ImageView view, String url) {
        Glide.with(view.getContext())
             .load(url)
             .apply(new RequestOptions()
                     .placeholder(R.drawable.placeholder_banner)
                     .error(R.drawable.placeholder_banner)
                     .diskCacheStrategy(DiskCacheStrategy.ALL)
                     .centerCrop())
             .into(view);
    }
}
