package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

import static com.codepath.apps.restclienttemplate.models.Tweet.fromJsonArray;

public class TimelineActivity extends AppCompatActivity {

    public static final String TAG = "TimelineActivity";
    TwitterClient client;
    RecyclerView rvTweet;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    SwipeRefreshLayout swipeContainer;
    EndlessRecyclerViewScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        client = TwitterApp.getRestClient(this);
        swipeContainer = findViewById(R.id.swipeContainer);

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "fetching new data.");
                //populateHomeTimeline();
            }
        });

        rvTweet = findViewById(R.id.rvTweet);
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvTweet.setLayoutManager(new LinearLayoutManager(this));
        rvTweet.setAdapter(adapter);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG, "onLoadMore" + page);
                loadMoreData();

            }

            private void loadMoreData() {
                client.getNextPageOfTweets(new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess for loadMoreData" + json.toString());
                        JSONArray jsonArray = json.jsonArray;
                        try {
                            List<Tweet> tweets = Tweet.fromJsonArray(jsonArray);
                            adapter.addAll(tweets);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure for loadMoreData", throwable);
                    }
                }, tweets.get(tweets.size() - 1).id);
            }

            ;
            //rvTweet.addOnScrollListener(scrollListener);
            //populateHomeTimeline();
            //}

            private void populateHomeTimeline() {
                client.getHomeTimeline(new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess!" + json.toString());
                        JSONArray jsonArray = json.jsonArray;
                        try {
                            adapter.clear();
                            adapter.addAll(fromJsonArray(jsonArray));
                            //tweets.addAll(Tweet.fromJsonArray(jsonArray));
                            //adapter.notifyDataSetChanged();
                            swipeContainer.setRefreshing(false);
                            //List<Tweet> tweets = Tweet.fromJsonArray(jsonArray);
                        } catch (JSONException e) {
                            Log.e(TAG, "Json exception", e);
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure!" + response, throwable);

                    }
                });
            }
        };
    }
}