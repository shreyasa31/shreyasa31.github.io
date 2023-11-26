package com.example.ebayshoppingapplictaion;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TabsViewAdapter extends FragmentStateAdapter {
    private  String itemId;
    private String keyword;
    public TabsViewAdapter(FragmentActivity fa,String keyword,String itemId) {
        super(fa);
        this.keyword = keyword;
        this.itemId=itemId;
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ProductFragment();
            case 1:
                return new ShippingFragment();
            case 2:
//                return new PhotosFragment();
                return PhotosFragment.newInstance(keyword);
            case 3:
//                return new SimilarFragment();
                return SimilarFragment.newInstance(itemId);
            default:
                return new ProductFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4; // Number of tabs
    }
}