package sj.android.com.lapit;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SectionPageAdapter extends FragmentPagerAdapter {
    public SectionPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position)
    {
        switch(position){
            case 0:
                RequestFragment requestFragment=new RequestFragment();
                return requestFragment;
            case 1:
                ChatsFragment chatsFragment=new ChatsFragment();
                return chatsFragment;
            case 2:
                FriendsFragment friendsFragment= new FriendsFragment();
                return friendsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        switch(position){
            case 0:
                return "Requests";
            case 1:
                return "Chats";
            case 2:
                return "Friends";
            default:
                return null;
        }
    }
}
