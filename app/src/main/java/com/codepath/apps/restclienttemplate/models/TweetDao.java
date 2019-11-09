package com.codepath.apps.restclienttemplate.models;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TweetDao {
    @Query("SELECT * FROM Tweet ORDER BY createdAt DESC")
    List<Tweet>  getTweets();

    // We used tweet_body and tweet_createdAt prefixes to resolve ambiguity
    // between user.createdAt and tweet.createdAt fields.
    @Query("SELECT Tweet.body AS tweet_body, Tweet.createdAt as tweet_createdAt, " +
            "User.* FROM Tweet INNER JOIN User ON Tweet.userId = User.id " +
            "ORDER BY Tweet.id DESC LIMIT 5")
    List<TweetWithUser> recentItems();


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertModel(Tweet... tweets);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertModel(User... users);


}
