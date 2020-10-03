package com.example.tutkdemo.viewmodel;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {
    private List<View> ViewList;

    public ViewPagerAdapter(List<View> viewList) {
        ViewList = viewList;
    }

    @Override
    public int getCount() {
        return ViewList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        View view = ViewList.get(position);
        if (view != null) {
            container.removeView(view);
        }

    }
    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(ViewList.get(position));
        //每次滑动的时候把视图添加到viewpager
        return ViewList.get(position);
    }
}
