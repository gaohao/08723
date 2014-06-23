package me.haogao.linkapp.app;

import io.oauth.OAuth;
import io.oauth.OAuthCallback;
import io.oauth.OAuthData;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.restfb.BinaryAttachment;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity implements OAuthCallback {
    // Constants
    /**
     * Register your here app https://dev.twitter.com/apps/new and get your
     * consumer key and secret
     * */
    static String TWITTER_CONSUMER_KEY = "JUVjwDpSE4FCrPP6fwxcbLBji";
    static String TWITTER_CONSUMER_SECRET = "M9aR2w1eq94vRCPYCdUSDZe1LAAoCHYbtKpBY5HYIKGhat7cr1";

    // Preference Constants
    static final String PREF_KEY_TWITTER_OAUTH_TOKEN = "twitter_oauth_token";
    static final String PREF_KEY_TWITTER_OAUTH_SECRET = "twitter_oauth_token_secret";
    static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";

    static final String PREF_KEY_FACEBOOK_OAUTH_TOKEN = "facebook_oauth_token";
    static final String PREF_KEY_FACEBOOK_OAUTH_SECRET = "facebook_oauth_token_secret";
    static final String PREF_KEY_FACEBOOK_LOGIN = "isFacebookLogedIn";

    String curPhotoPath;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView imageView;

    // Twitter Login button
    Button btnLoginTwitter;

    // Twitter Login button
    Button btnLoginFacebook;

    // Update status button
    ImageButton btnUpdateStatus;

    // Logout button
    Button btnLogoutTwitter;

    Button btnLogoutFacebook;

    // EditText for update
    EditText txtUpdate;
    // lbl update
    TextView lblUpdate;
    TextView lblUserName;

    ImageButton btnTakePicture;

    ImageButton btnNewsFeed;

    // Progress dialog
    ProgressDialog pDialog;

    // Twitter
    private static Twitter twitter;
    private static RequestToken requestToken;

    // Shared Preferences
    private static SharedPreferences mSharedPreferences;

    // Internet Connection detector
    private ConnectionDetector cd;

    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        final OAuth o = new OAuth(this);
        o.initialize("OquecX1y_ZzeNzN4PxoDTxs8klc"); // Initialize the oauth key

        cd = new ConnectionDetector(getApplicationContext());

        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialog(MainActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }

        // Check if twitter keys are set
        if(TWITTER_CONSUMER_KEY.trim().length() == 0 || TWITTER_CONSUMER_SECRET.trim().length() == 0){
            // Internet Connection is not present
            alert.showAlertDialog(MainActivity.this, "Twitter oAuth tokens", "Please set your twitter oauth tokens first!", false);
            // stop executing code by return
            return;
        }

        // All UI elements
        btnLoginTwitter = (Button) findViewById(R.id.btnLoginTwitter);
        btnLoginFacebook = (Button) findViewById(R.id.btnLoginFacebook);
        btnUpdateStatus = (ImageButton) findViewById(R.id.btnUpdateStatus);
        btnLogoutTwitter = (Button) findViewById(R.id.btnLogoutTwitter);
        btnLogoutFacebook = (Button) findViewById(R.id.btnLogoutFacebook);
        txtUpdate = (EditText) findViewById(R.id.txtUpdateStatus);
        lblUpdate = (TextView) findViewById(R.id.lblUpdate);
        lblUserName = (TextView) findViewById(R.id.lblUserName);
        btnTakePicture = (ImageButton) findViewById(R.id.btnTakePicture);
        imageView = (ImageView) findViewById(R.id.imageView);
        btnNewsFeed = (ImageButton) findViewById(R.id.btnNavigateTimeline);
        // Shared Preferences
        mSharedPreferences = getApplicationContext().getSharedPreferences(
                "MyPref", 0);


        /**
         * Twitter login button click event
         * */
        btnLoginTwitter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Call login twitter function
                o.popup("twitter", MainActivity.this); // Launch the pop up with the right provider & callback
            }
        });

        /**
         * Facebook login button click event
         * */
        btnLoginFacebook.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Call login twitter function
                o.popup("facebook", MainActivity.this); // Launch the pop up with the right provider & callback
            }
        });

        /**
         * Button click event to Update Status, will call updateStatus()
         * function
         * */
        btnUpdateStatus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Call update status function
                // Get the status from EditText
                String status = txtUpdate.getText().toString();

                // Check for blank text
                if (status.trim().length() > 0) {
                    // update status
                    new updateStatus().execute(status, curPhotoPath);
                } else {
                    // EditText is empty
                    Toast.makeText(getApplicationContext(),
                            "Please enter status message", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        btnNewsFeed.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TimelineActivity.class));
            }
        });

        /**
         * Button click event for logout from facebook
         * */
        btnLogoutFacebook.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Call logout twitter function
                logoutFromFacebook();
            }
        });

        /**
         * Button click event for logout from twitter
         * */
        btnLogoutTwitter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Call logout twitter function
                logoutFromTwitter();
            }
        });

        btnTakePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Call logout twitter function
                takePic();
            }
        });

        /** This if conditions is tested once is
         * redirected from twitter page. Parse the uri to get oAuth
         * Verifier
         * */
        if (!isTwitterLoggedInAlready()) {
            btnLogoutTwitter.setVisibility(View.GONE);
            btnUpdateStatus.setVisibility(View.GONE);
            txtUpdate.setVisibility(View.GONE);
            lblUpdate.setVisibility(View.GONE);
            lblUserName.setText("");
            lblUserName.setVisibility(View.GONE);

            //btnLoginTwitter.setVisibility(View.VISIBLE);

        } else {
            // Hide login button
            btnLoginTwitter.setVisibility(View.GONE);

            // Show Update Twitter
            lblUpdate.setVisibility(View.VISIBLE);
            txtUpdate.setVisibility(View.VISIBLE);
            btnUpdateStatus.setVisibility(View.VISIBLE);
            //btnLogoutTwitter.setVisibility(View.VISIBLE);
        }

        if (!isFacebookLoggedInAlready()) {
            btnLogoutFacebook.setVisibility(View.GONE);
            btnUpdateStatus.setVisibility(View.GONE);
            txtUpdate.setVisibility(View.GONE);
            lblUpdate.setVisibility(View.GONE);
            lblUserName.setText("");
            lblUserName.setVisibility(View.GONE);

            //btnLoginFacebook.setVisibility(View.VISIBLE);

        } else {
            // Hide login button
            btnLoginFacebook.setVisibility(View.GONE);

            // Show Update Twitter
            lblUpdate.setVisibility(View.VISIBLE);
            txtUpdate.setVisibility(View.VISIBLE);
            btnUpdateStatus.setVisibility(View.VISIBLE);
            //btnLogoutFacebook.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE
                && resultCode == RESULT_OK
                && curPhotoPath != null) {

            // scale the bitmap
            double targetW = imageView.getWidth();
            double targetH = imageView.getHeight();
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(curPhotoPath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;
            int scaleFactor = 1;
            if ((targetW > 0) && (targetH > 0)) {
                scaleFactor = (int)Math.min(Math.ceil(photoW/targetW), Math.ceil(photoH/targetH));
            }
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;
            Bitmap bitmap = BitmapFactory.decodeFile(curPhotoPath, bmOptions);
            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);

            // add to gallery
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(curPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
        }
    }

    @Override
    public void onFinished(OAuthData data) {
        if (data.status.equals("success")) {
            if (data.provider.equals("twitter")) {
                // Shared Preferences
                Editor e = mSharedPreferences.edit();

                // After getting access token, access token secret
                // store them in application preferences
                e.putString(PREF_KEY_TWITTER_OAUTH_TOKEN, data.token);
                e.putString(PREF_KEY_TWITTER_OAUTH_SECRET, data.secret);
                // Store login status - true
                e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
                e.commit(); // save changes

                Log.e("Twitter OAuth Token", "> " + data.token);

                // Hide login button
                btnLoginTwitter.setVisibility(View.GONE);

                // Show Update Twitter
                lblUpdate.setVisibility(View.VISIBLE);
                txtUpdate.setVisibility(View.VISIBLE);
                btnUpdateStatus.setVisibility(View.VISIBLE);
                btnLogoutTwitter.setVisibility(View.VISIBLE);

            } else if (data.provider.equals("facebook")) {
                // Shared Preferences
                Editor e = mSharedPreferences.edit();

                // After getting access token, access token secret
                // store them in application preferences
                e.putString(PREF_KEY_FACEBOOK_OAUTH_TOKEN, data.token);
                e.putString(PREF_KEY_FACEBOOK_OAUTH_SECRET, data.secret);
                // Store login status - true
                e.putBoolean(PREF_KEY_FACEBOOK_LOGIN, true);
                e.commit(); // save changes

                Log.e("Facebook OAuth Token", "> " + data.token);

                // Hide login button
                btnLoginFacebook.setVisibility(View.GONE);

                // Show Update Twitter
                lblUpdate.setVisibility(View.VISIBLE);
                txtUpdate.setVisibility(View.VISIBLE);
                btnUpdateStatus.setVisibility(View.VISIBLE);
                //btnLogoutTwitter.setVisibility(View.VISIBLE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        curPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void takePic() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.d("IO Exception", ex.getMessage());
                photoFile = null;
                curPhotoPath = null;
            } catch (Exception ex) {
                Log.d("Exception", ex.getMessage());
                photoFile = null;
                curPhotoPath = null;
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    /**
     * Function to update status
     * */
    class updateStatus extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Sending ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting Places JSON
         * */
        protected String doInBackground(String... args) {
            Log.d("Text", "> " + args[0]);
            String msg = args[0];
            String picPath = args[1];
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

                // Update status
                StatusUpdate status = new StatusUpdate(msg);
                if (picPath != null) {
                    status.setMedia(new File(picPath));
                }
                twitter4j.Status trResponse = twitter.updateStatus(status);

                Log.d("Status", "> " + trResponse.getText());

            } catch (TwitterException e) {
                // Error in updating status
                Log.d("Twitter Update Error", e.getMessage());
            }

            try {
                // Access Token
                access_token = mSharedPreferences.getString(PREF_KEY_FACEBOOK_OAUTH_TOKEN, "");
                // Access Token Secret
                access_token_secret = mSharedPreferences.getString(PREF_KEY_FACEBOOK_OAUTH_SECRET, "");
                FacebookClient facebookClient = new DefaultFacebookClient(access_token, access_token_secret);
                if (picPath == null) {
                    FacebookType fbResponse =
                            facebookClient.publish("me/feed", FacebookType.class,
                                    Parameter.with("message", msg));
                } else {
                    InputStream is = new FileInputStream(new File(picPath));
                    FacebookType fbResponse =
                            facebookClient.publish("me/photos", FacebookType.class,
                                    BinaryAttachment.with("pic", is),
                                    Parameter.with("message", msg));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog and show
         * the data in UI Always use runOnUiThread(new Runnable()) to update UI
         * from background thread, otherwise you will get error
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "Status tweeted successfully", Toast.LENGTH_SHORT)
                            .show();
                    // Clearing EditText field
                    txtUpdate.setText("");
                }
            });
            imageView.setImageBitmap(null);
        }
    }

    /**
     * Function to logout from twitter
     * It will just clear the application shared preferences
     * */
    private void logoutFromTwitter() {
        // Clear the shared preferences
        Editor e = mSharedPreferences.edit();
        e.remove(PREF_KEY_TWITTER_OAUTH_TOKEN);
        e.remove(PREF_KEY_TWITTER_OAUTH_SECRET);
        e.remove(PREF_KEY_TWITTER_LOGIN);
        e.commit();

        // After this take the appropriate action
        // I am showing the hiding/showing buttons again
        // You might not needed this code
        btnLogoutTwitter.setVisibility(View.GONE);
        btnUpdateStatus.setVisibility(View.GONE);
        txtUpdate.setVisibility(View.GONE);
        lblUpdate.setVisibility(View.GONE);
        lblUserName.setText("");
        lblUserName.setVisibility(View.GONE);

        btnLoginTwitter.setVisibility(View.VISIBLE);
    }

    /**
     * Function to logout from twitter
     * It will just clear the application shared preferences
     * */
    private void logoutFromFacebook() {
        // Clear the shared preferences
        Editor e = mSharedPreferences.edit();
        e.remove(PREF_KEY_FACEBOOK_OAUTH_TOKEN);
        e.remove(PREF_KEY_FACEBOOK_OAUTH_SECRET);
        e.remove(PREF_KEY_FACEBOOK_LOGIN);
        e.commit();

        // After this take the appropriate action
        // I am showing the hiding/showing buttons again
        // You might not needed this code
        btnLogoutFacebook.setVisibility(View.GONE);
        btnUpdateStatus.setVisibility(View.GONE);
        txtUpdate.setVisibility(View.GONE);
        lblUpdate.setVisibility(View.GONE);
        lblUserName.setText("");
        lblUserName.setVisibility(View.GONE);

        btnLoginFacebook.setVisibility(View.VISIBLE);
    }

    /**
     * Check user already logged in your application using twitter Login flag is
     * fetched from Shared Preferences
     * */
    private boolean isTwitterLoggedInAlready() {
        // return twitter login status from Shared Preferences
        return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
    }

    /**
     * Check user already logged in your application using facebook Login flag is
     * fetched from Shared Preferences
     * */
    private boolean isFacebookLoggedInAlready() {
        // return twitter login status from Shared Preferences
        return mSharedPreferences.getBoolean(PREF_KEY_FACEBOOK_LOGIN, false);
    }

    protected void onResume() {
        super.onResume();
    }

}