package com.codepath.apps.restclienttemplate.models;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {
    EditText etCompose;
    TextView tvCounter;
    Button btnTweet;
    TwitterClient client;
    public static final String TAG = "ComposeActivity";
    public static final int MAX_TWEET_LENGTH = 140;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        etCompose = findViewById(R.id.etCompose);
        btnTweet = findViewById(R.id.btnTweet);
        tvCounter = findViewById(R.id.tvCounter);
        client = TwitterApp.getRestClient(this);
        // set click listener on button
        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // illegal tweets handling
                String tweetContent = etCompose.getText().toString();
                if (tweetContent.isEmpty()){
                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet cannot be empty", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (tweetContent.length() >MAX_TWEET_LENGTH ) {
                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet cannot be longer than 140 characters", Toast.LENGTH_LONG).show();
                    return;
                }
                // Make an API call to twitter to post tweet
                client.publishTweet(tweetContent, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess in publishTweet ");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Log.i(TAG, "Published tweet says: "+ tweet.body);
                            Intent intent = new Intent();
                            // pass the relavant data back as a result
                            intent.putExtra("tweet", Parcels.wrap(tweet));
                            setResult(RESULT_OK, intent);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "publishTweet onFailure", throwable);
                    }
                });
                Toast.makeText(ComposeActivity.this, tweetContent, Toast.LENGTH_LONG).show();
            }
        });

        etCompose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                tvCounter.setText(s.toString().length() + "/140");
                if (s.toString().length() > 140 ) {
                    // alert the user to delete the exceeded characters
                    tvCounter.setTextColor(Color.RED);
                }
                else if (s.toString().length() <= 140 ) {
                    // alert the user to delete the exceeded characters
                    tvCounter.setTextColor(Color.GRAY);
                }
            }
        });

    }


}
