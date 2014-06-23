package me.haogao.linkapp.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.restfb.BinaryAttachment;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;
import com.restfb.types.Post;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;


public class TimelineActivity extends ActionBarActivity {

    static String TWITTER_CONSUMER_KEY = "JUVjwDpSE4FCrPP6fwxcbLBji";
    static String TWITTER_CONSUMER_SECRET = "M9aR2w1eq94vRCPYCdUSDZe1LAAoCHYbtKpBY5HYIKGhat7cr1";

    // Preference Constants
    static final String PREF_KEY_TWITTER_OAUTH_TOKEN = "twitter_oauth_token";
    static final String PREF_KEY_TWITTER_OAUTH_SECRET = "twitter_oauth_token_secret";
    static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";

    static final String PREF_KEY_FACEBOOK_OAUTH_TOKEN = "facebook_oauth_token";
    static final String PREF_KEY_FACEBOOK_OAUTH_SECRET = "facebook_oauth_token_secret";
    static final String PREF_KEY_FACEBOOK_LOGIN = "isFacebookLogedIn";

    // Shared Preferences
    private static SharedPreferences mSharedPreferences;

    TextView statusTextView;
    ImageButton btnNavigateToHome;

    String twitterFeed;
    String facebookFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        statusTextView = (TextView) findViewById(R.id.statusTextView);

        // Shared Preferences
        mSharedPreferences = getApplicationContext().getSharedPreferences(
                "MyPref", 0);
        new getTimelineTask().execute();
        btnNavigateToHome = (ImageButton) findViewById(R.id.btnNavigateToHome);

        btnNavigateToHome.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                startActivity(new Intent(TimelineActivity.this, MainActivity.class));
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        switch (item.getItemId()) {
            case R.id.selectFB:
                statusTextView.setText(facebookFeed);
                return true;
            case R.id.selectTR:
                statusTextView.setText(twitterFeed);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class getTimelineTask extends AsyncTask<String, String, String> {

        protected String doInBackground(String... params) {
            // Access Token
            String access_token;
            String access_token_secret;
            try {
                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
                builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);

                // Access Token
                access_token = mSharedPreferences.getString(PREF_KEY_TWITTER_OAUTH_TOKEN, "");
                // Access Token Secret
                access_token_secret = mSharedPreferences.getString(PREF_KEY_TWITTER_OAUTH_SECRET, "");

                AccessToken accessToken = new AccessToken(access_token, access_token_secret);
                Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

                List<twitter4j.Status> statuses = twitter.getHomeTimeline();
                twitterFeed = "";
                for (int i = 0; i < statuses.size(); i++) {
                    twitter4j.Status status = statuses.get(i);
                    twitterFeed += "Tweet #" + i + " \n";
                    twitterFeed += "Date:" + status.getCreatedAt() + "\n";
                    twitterFeed += "Post:" + status.getText() + "\n\n";
                }
            } catch (TwitterException e) {
                // Error in updating status
                Log.d("Twitter Error", e.getMessage());
            }

            try {
                // Access Token
                access_token = mSharedPreferences.getString(PREF_KEY_FACEBOOK_OAUTH_TOKEN, "");
                // Access Token Secret
                access_token_secret = mSharedPreferences.getString(PREF_KEY_FACEBOOK_OAUTH_SECRET, "");
                FacebookClient facebookClient = new DefaultFacebookClient(access_token, access_token_secret);
                Connection<Post> myFeed = facebookClient.fetchConnection("me/home", Post.class);
                int i = 0;
                facebookFeed = "";
                for (List<Post> posts : myFeed) {
                    for (Post post : posts) {
                        if (post.getMessage() != null) {
                            facebookFeed += "Facebook #" + i + " \n";
                            facebookFeed += "Date:" + post.getCreatedTime() + "\n";
                            facebookFeed += "Post:" + post.getMessage() + "\n\n";
                            i++;
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return twitterFeed + facebookFeed;
        }

        protected void onPostExecute(String status) {
            statusTextView.setText(status);
        }
    }
}
