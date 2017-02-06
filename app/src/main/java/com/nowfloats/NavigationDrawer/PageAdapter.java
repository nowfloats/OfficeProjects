package com.nowfloats.NavigationDrawer;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.nowfloats.Login.Model.FloatsMessageModel;
import com.nowfloats.test.com.nowfloatsui.buisness.util.Util;
import com.nowfloats.util.Methods;
import com.nowfloats.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dell on 24-01-2015.
 */
public class PageAdapter extends FragmentPagerAdapter {
    private Context appContext ;

    ArrayList<String> images = new ArrayList<>();
    ArrayList<String> mainText = new ArrayList<>();
    ArrayList<String> dateText = new ArrayList<>();
    ArrayList<String> messageIds = new ArrayList<>();
    final List<FloatsMessageModel> messages;

    public PageAdapter(FragmentManager fm, List<FloatsMessageModel> messages,  Context context) {
        super(fm);
        //Log.d("Page Adapter","Page Adapter");
        this.messages = messages;
        FloatsMessageModel data;
        if(messages != null)
        {
            for(int i = 0 ; i < messages.size(); i++)
            {
                data = messages.get(i);

                try {
                    if (data != null) {
                        mainText.add(i, data.message);
                        dateText.add(i, Methods.getFormattedDate(data.createdOn));
                        images.add(i, data.tileImageUri);
                        messageIds.add(i,HomeActivity.StorebizFloats.get(i)._id);
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putString(Card_Full_View_Fragment.MainTextKey,mainText.get(position));
        bundle.putString(Card_Full_View_Fragment.DateTextKey,dateText.get(position));
        bundle.putString(Card_Full_View_Fragment.ImageKey,images.get(position));
        bundle.putString(Card_Full_View_Fragment.MessageIdKey,messageIds.get(position));

        Card_Full_View_Fragment cardFragment = new Card_Full_View_Fragment();
        cardFragment.setArguments(bundle);

        return cardFragment;
    }
}