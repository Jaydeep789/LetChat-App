package com.example.letschat.Adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.letschat.Fragments.ChatFragment;
import com.example.letschat.Fragments.SettingsFragment;
import com.example.letschat.Fragments.UserFragment;

public class TabAccessorAdapter extends FragmentPagerAdapter {
    public TabAccessorAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {

        switch (i) {
            case 0:
                return new ChatFragment();
            case 1:
                return new UserFragment();
            case 2 :
                return new SettingsFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position) {
            case 0:
                return "Chats";
            case 1:
                return "Contacts";
            case 2:
                return "Settings";
        }
        return null;
    }
}
