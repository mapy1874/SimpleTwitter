package com.codepath.apps.restclienttemplate.models;

import androidx.room.Embedded;

import com.codepath.apps.restclienttemplate.TwitterApp;

import java.util.ArrayList;
import java.util.List;

public class TweetWithUser {
    // @Embedded notation flattens the properties of the User object into the object, preserving encapsulation.
    @Embedded
    User user;

    // Prefix is needed to resolve ambiguity between fields: user.id and tweet.id, user.createdAt and tweet.createdAt
    @Embedded(prefix = "tweet_")
    Tweet tweet;

    public static List<Tweet> getTweetList(List<TweetWithUser> tweetWithUserList){
        List<Tweet> tweets = new ArrayList<>();
        for(int i =0; i < tweetWithUserList.size(); i++){
            TweetWithUser tweetWithUser = tweetWithUserList.get(i);
            Tweet tweet =tweetWithUser.tweet;
            tweet.user = tweetWithUser.user;
            tweets.add(tweet);
        }
        return tweets;
    }

}
