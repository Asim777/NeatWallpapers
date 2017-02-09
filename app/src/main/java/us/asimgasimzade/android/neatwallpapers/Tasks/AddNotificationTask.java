package us.asimgasimzade.android.neatwallpapers.Tasks;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import us.asimgasimzade.android.neatwallpapers.MainActivity;
import us.asimgasimzade.android.neatwallpapers.R;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * This task instantiates new notifications if user hasn't entered the app for on week
 */

public class AddNotificationTask extends AsyncTask<String, Void, Integer> {

    private static final String LOG_TAG = "AddNotificationTask";
    private Context context;
    private String url = "https://pixabay.com/api/?key=3898774-ad29861c5699760086a93892b&image_type=photo&orientation=vertical&safesearch=true&order=latest&per_page=3";
    private URL feed_url;
    private HttpURLConnection urlConnection;
    private Bitmap notificationBitmap;
    private Bitmap notificationLargIconBitmap;
    private int notificationImageWidth;
    private int notificationImageHeight;
    private String notificationImageURL;

    public AddNotificationTask(Context context) {
        this.context = context;
    }

    @Override
    protected Integer doInBackground(String... params) {
        Integer result = 0;

        try {
            feed_url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            //Create Apache HttpClient
            urlConnection = (HttpURLConnection) feed_url.openConnection();
            int statusCode = urlConnection.getResponseCode();

            //200 represent status is OK
            if (statusCode == 200) {
                String response = streamToString(urlConnection.getInputStream());
                try {
                    JSONObject rootJson = new JSONObject(response);
                    JSONArray hits = rootJson.optJSONArray("hits");
                    if (hits.length() > 0) {
                        JSONObject image = hits.getJSONObject(0);
                        if (image != null) {
                            notificationImageURL = image.getString("webformatURL");
                            notificationImageWidth = image.getInt("webformatWidth");
                            notificationImageHeight = image.getInt("webformatHeight");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                result = 1; //Successful
            } else {
                result = 0; //Failed
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getLocalizedMessage());
        } finally {
            urlConnection.disconnect();
        }


        //get the bitmap to show in notification bar
        try {
            notificationBitmap = Glide.with(context).
                    load(notificationImageURL).
                    asBitmap().centerCrop().
                    into(notificationImageWidth,notificationImageHeight).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(Integer result) {

        if(result == 1) {
            int notificationLargIconBitmapSize = Double.valueOf(notificationImageWidth * 0.7).intValue();

            notificationLargIconBitmap = Bitmap.createBitmap(notificationBitmap, 0, 0, notificationLargIconBitmapSize, notificationLargIconBitmapSize);
            //Setting content of standard notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(notificationLargIconBitmap)
                    .setContentTitle("NeatWallpapers")
                    .setContentText("Check out new wallpapers!")
                    .setTicker("text")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            //Setting new style for expanded notification
            NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();

            //Set bitmap as big picture to BigPictureStyle
            bigPictureStyle.bigPicture(notificationBitmap);

            // Sets a title for the Inbox in expanded layout
            bigPictureStyle.setBigContentTitle("NeatWallpapers");
            //Set style to a Notification Builder
            builder.setStyle(bigPictureStyle);
            //Set intent to open the app when clicking on notification
            Intent notificationIntent = new Intent(context, MainActivity.class);

            // This ensures that the back button follows the recommended convention for the back key.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MainActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack.
            stackBuilder.addNextIntent(notificationIntent);

            //Set PendingIntent so notification arrives even when app is not running
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            //Set pending intent to notification builder
            builder.setContentIntent(contentIntent);

            //Launch notification
            NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            manager.notify(0, builder.build());
        } else {
            Log.e(LOG_TAG, "Wasn't able to load notification, network error");
        }
    }

    private String streamToString(InputStream stream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

        //Close stream
        stream.close();
        return result;
    }

}