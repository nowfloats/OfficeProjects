package com.nowfloats.twitter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.nowfloats.util.Constants;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

/**
 * Created by Admin on 31-05-2017.
 */

public class TwitterConnection {
    public final static String PREF_NAME = "NFBoostTwitterPref";
    public static final String PREF_KEY_TWITTER_LOGIN = "is_twitter_loggedin";
    public static final int WEBVIEW_REQUEST_CODE = 100;
    public static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    public static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    public static final String PREF_USER_NAME = "twitter_user_name";
    private Context mContext;


    TwitterConfig config;
    private TwitterAuthClient client;

    public TwitterConnection(Context context){
        config = new TwitterConfig.Builder(context)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET))
                .debug(true)
                .build();
        Twitter.initialize(config);
        mContext = context;
    }
    
    public void authorize(){
        client = new TwitterAuthClient();
        client.authorize((Activity)mContext, new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                ((TwitterResult)mContext).onTwitterConnected(result);
                TwitterSession session = result.data;
                Log.v("ggg",session.getAuthToken()+" "+session.getUserId()+" "+session.toString());
            }

            @Override
            public void failure(TwitterException e) {
                ((TwitterResult)mContext).onTwitterConnected(null);
            }
        });

    }

    public interface TwitterResult{
        void onTwitterConnected(Result<TwitterSession> result);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        client.onActivityResult(requestCode,resultCode,data);
    }
}
