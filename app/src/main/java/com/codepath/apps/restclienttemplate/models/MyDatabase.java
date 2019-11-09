package com.codepath.apps.restclienttemplate.models;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Tweet.class, User.class}, version = 1)
public abstract class MyDatabase  extends RoomDatabase {
    public abstract SampleModelDao sampleModelDao();

    public abstract TweetDao tweetDao();

    // Database name to be used
    public static final String NAME = "MyDataBase";
}
