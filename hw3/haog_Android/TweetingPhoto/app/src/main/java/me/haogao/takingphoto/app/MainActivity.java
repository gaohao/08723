package me.haogao.takingphoto.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;


public class MainActivity extends ActionBarActivity {
    static final String andrewID = "haog";
    String curPhotoPath;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView imageView;
    String PhoneModel;
    String AndroidVersion;
    static final String consumerKey = "ZYGKs6UbOS5GlLOBwKTBdaNGT";
    static final String consumerSecret = "ttrnhMOJWxZr0nLQNSNaJ9r0BiTSQ1XqdUXvzLMYEU29oNe4y0";
    static final String accessToken = "476706716-YkMoEKKpZpDyALbpjSXRdoIA3BAQxXMlXJKgs76v";
    static final String accessSecret = "saFy9hUEDdgN0KEGBkbSObJQO1DJRrRrfMX8JnyOSpt7a";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
        PhoneModel = android.os.Build.MODEL;
        AndroidVersion = android.os.Build.VERSION.RELEASE;
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
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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

            String deviceInfo = getDeviceInfo();
            Log.i("Device Info", deviceInfo);
        }
    }

    private String getDeviceInfo() {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        df.setTimeZone(TimeZone.getTimeZone("GMT-4"));
        return andrewID + " : " + PhoneModel + " " + AndroidVersion + " : " + df.format(date) + " PST";
    }

    public void takePic(View view) {
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

    public void tweeting(View view) {
        if (curPhotoPath == null) {
            EditText statusTxt = (EditText) findViewById(R.id.statusTxt);
            statusTxt.setText("take a photo first!!");
        } else {
            new sendTweetTask().execute(curPhotoPath);
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

    class sendTweetTask extends AsyncTask<String, Void, Boolean> {

        private Exception exception;

        protected Boolean doInBackground(String... params) {
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessSecret);
            try
            {
                TwitterFactory factory = new TwitterFactory(cb.build());
                Twitter twitter = factory.getInstance();

                twitter.getScreenName();
                StatusUpdate status = new StatusUpdate("@MobileApp4 " + getDeviceInfo());
                status.setMedia(new File(params[0]));
                twitter.updateStatus(status);
                return true;

            } catch (TwitterException ex) {
                Log.d("Exception", ex.getMessage());
                return false;
            } catch (Exception ex) {
                Log.d("Exception", ex.getMessage());
                return false;
            }
        }

        protected void onPostExecute(Boolean result) {
            EditText statusTxt = (EditText) findViewById(R.id.statusTxt);
            if (result) {
                statusTxt.setText("tweet posted!");
            } else {
                statusTxt.setText("please resend!");
            }
        }
    }
}
