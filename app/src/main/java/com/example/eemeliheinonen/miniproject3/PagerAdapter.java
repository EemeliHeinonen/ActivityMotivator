package com.example.eemeliheinonen.miniproject3;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
/**
 * Created by eemeliheinonen on 30/08/16.
 */

public class PagerAdapter extends FragmentPagerAdapter {

    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] { "Main", "Settings" };
    public PagerAdapter(FragmentManager fm) { super(fm); }

    public int getCount() {
        return PAGE_COUNT;
    }

    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return MainFragment.newInstance("Eka fragmentti");
            case 1:
                return MainFragment.newInstance("Toka fragmentti");
        }
        return null;
    }

    public CharSequence getPageTitle(int position) { return tabTitles[position]; }
}
