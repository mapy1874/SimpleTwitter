package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codepath.apps.restclienttemplate.models.ComposeActivity;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.TweetDao;
import com.codepath.apps.restclienttemplate.models.TweetWithUser;
import com.codepath.apps.restclienttemplate.models.MyDatabase;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {
    static final String TAG = "TimelineActivity";
    static final int COMPOSE_ACTIVITY_REQUEST_CODE = 100;
    TwitterClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    SwipeRefreshLayout swipeContainer;
    EndlessRecyclerViewScrollListener scrollListener;
    TweetDao tweetDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        client = TwitterApp.getRestClient(this);

        swipeContainer = findViewById(R.id.swipeContainer);
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                           android.R.color.holo_green_light,
                           android.R.color.holo_orange_light,
                           android.R.color.holo_red_light);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG,"fetching new data!");
                populateHomeTimeLine();
            }
        });

        // Find the recyclerview
        rvTweets = findViewById(R.id.rvTweets);
        // Initialize the list of tweets and adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        // Recycler view setup: layout manager and the adapter
        rvTweets.setLayoutManager(linearLayoutManager);
        rvTweets.setAdapter(adapter);
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG,"onLoadMore: "+page);
                loadMoreData();
            }
        };
        tweetDao = ((TwitterApp) getApplicationContext()).getMyDatabase().tweetDao();
        // Adds the scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener);
        // Remember to always move DB queries off of the Main thread
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Request list of tweets with Users using DAO
                List<TweetWithUser> tweetFromDatabase = tweetDao.recentItems();
                adapter.clear();
                Log.i(TAG,"Showing data from database");

                // TweetWithUser has to be converted Tweet objects with nested User objects (see next snippet)
                List<Tweet> tweetList = TweetWithUser.getTweetList(tweetFromDatabase);
                adapter.addAll(tweetList);
            }
        });
        populateHomeTimeLine();
    }

    private void populateHomeTimeLine() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess"+json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    Log.i("Peiyuan","populateHomeTimeLine onSuccess");
                    adapter.clear();
                    final List<Tweet> freshTweets = Tweet.fromJsonArray(json.jsonArray);
                    final List<User> freshUsers = User.fromJsonTweetArray(freshTweets);
                    adapter.addAll(freshTweets);

                    // Saving data into our database
                    // Interaction with database cannot happen on the Main Thread
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            // runInTransaction() allows to perform multiple db actions in a batch thus preserving consistency
                            ((TwitterApp) getApplicationContext()).getMyDatabase().runInTransaction(new Runnable() {
                                @Override
                                public void run() {
                                    // Inserting both Tweets and Users to their respective tables
                                    // insert user first to make ForeignKey work
                                    tweetDao.insertModel(freshUsers.toArray(new User[0]));
                                    tweetDao.insertModel(freshTweets.toArray(new Tweet[0]));
                                }
                            });
                        }
                    });
                    // Now we call setRefreshing(false) to signal refresh has finished
                    swipeContainer.setRefreshing(false);
                } catch (JSONException e) {
                    Log.e(TAG, "populateHomeTimeLine");
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i(TAG, "onFailure"+response, throwable);

            }
        });
    }

    // this is where we will make another API call to get the next page of tweets and add the objects to our current list of tweets
    public void loadMoreData() {
        // 1. Send an API request to retrieve appropriate paginated data
        client.getNextPageOfTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG,"loadMoreData onSuccess");
                // 2. Deserialize and construct new model objects from the API response
                JSONArray jsonArray = json.jsonArray;
                try {
                    List<Tweet> tweets = Tweet.fromJsonArray(jsonArray);
                    adapter.addAll(tweets);
                } catch (JSONException e) {
                    Log.e(TAG, "loadMoreData");
                }

                // 3. Append the new data objects to the existing set of items inside the array of items
                // 4. Notify the adapter of the new items made with `notifyItemRangeInserted()`

            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i("Peiyuan","loadMoreData onFailure");
            }
        }, tweets.get(tweets.size()-1).id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.miCompose){
            // Compose item has been selected
            // Navigate to the ComposeActivity
            Intent intent = new Intent(this, ComposeActivity.class);
            startActivityForResult(intent, COMPOSE_ACTIVITY_REQUEST_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK&&requestCode==COMPOSE_ACTIVITY_REQUEST_CODE){
            // Get data from the intent (tweet)
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            // update the RV with the tweet
            // Modify data source of tweets
            tweets.add(0,tweet);
            adapter.notifyItemInserted(0);
            rvTweets.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode,resultCode,data);
    }
}
