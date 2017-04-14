package us.asimgasimzade.android.neatwallpapers.tasks;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

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
import java.util.Random;

import us.asimgasimzade.android.neatwallpapers.MainActivity;
import us.asimgasimzade.android.neatwallpapers.R;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * This task instantiates new notifications if user hasn't entered the app for a day
 */

public class AddNotificationTask extends AsyncTask<String, Void, Integer> {

    private static final String NOTIFICATION_ID_KEY = "Notification Key";
    private Context context;
    private URL feed_url;
    private HttpURLConnection urlConnection;
    private Bitmap notificationBitmap;
    private int notificationImageWidth;
    private int notificationImageHeight;
    private String notificationImageURL;

    public AddNotificationTask(Context context) {
        this.context = context;
    }

    @Override
    protected Integer doInBackground(String... params) {

        String[] urls = {
                "https://pixabay.com/api/?key=" + context.getString(R.string.pixabay_key) + "&image_type=photo&orientation=horizontal&safesearch=true&order=latest&per_page=3&q=nature%20landscape",
                "https://pixabay.com/api/?key=" + context.getString(R.string.pixabay_key) + "&image_type=photo&orientation=horizontal&safesearch=true&order=latest&per_page=3&q=building",
                "https://pixabay.com/api/?key=" + context.getString(R.string.pixabay_key) + "&image_type=photo&orientation=horizontal&safesearch=true&order=latest&per_page=3&q=monuments",
                "https://pixabay.com/api/?key=" + context.getString(R.string.pixabay_key) + "&image_type=photo&orientation=horizontal&safesearch=true&order=latest&per_page=3&q=space%20stars",
                "https://pixabay.com/api/?key=" + context.getString(R.string.pixabay_key) + "&image_type=photo&orientation=horizontal&safesearch=true&order=latest&per_page=3&q=user:unsplash",
        };

        Random random = new Random();
        int randomCategory = random.nextInt(urls.length);
        String url = urls[randomCategory];
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
                int randomImage = random.nextInt(3);
                String response = streamToString(urlConnection.getInputStream());
                try {
                    JSONObject rootJson = new JSONObject(response);
                    JSONArray hits = rootJson.optJSONArray("hits");
                    if (hits.length() > 0) {
                        JSONObject image = hits.getJSONObject(randomImage);
                        if (image != null) {
                            notificationImageURL = image.getString("webformatURL");
                            notificationImageWidth = image.getInt("webformatWidth");
                            notificationImageHeight = image.getInt("webformatHeight");
                            result = 1;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    result = 0;
                }
            } else {
                result = 0; //Failed
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = 0;
        } finally {
            urlConnection.disconnect();
        }

        //get the Bitmap to show in notification bar
        try {
            notificationBitmap = Glide.with(context).
                    load(notificationImageURL).
                    asBitmap().centerCrop().
                    into(notificationImageWidth, notificationImageHeight).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(Integer result) {
        Bitmap notificationLargIconBitmap;
        if (result == 1) {

            if (notificationBitmap != null) {
                //noinspection SuspiciousNameCombination
                notificationLargIconBitmap = Bitmap.createBitmap(notificationBitmap, 0, 0,
                        notificationImageHeight, notificationImageHeight);

                //Setting content of standard notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(notificationLargIconBitmap)
                        .setContentTitle(context.getResources().getString(R.string.app_name))
                        .setContentText("Check out new wallpapers!")
                        .setTicker("text")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                //Setting new style for expanded notification
                NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();

                //Set bitmap as big picture to BigPictureStyle
                bigPictureStyle.bigPicture(notificationBitmap);

                // Sets a title for the Inbox in expanded layout
                bigPictureStyle.setBigContentTitle(context.getResources().getString(R.string.app_name));
                //Set style to a Notification Builder
                builder.setStyle(bigPictureStyle);
                //Set intent to open the app when clicking on notification
                Intent notificationIntent = new Intent(context, MainActivity.class);
                //Put extra so that Main activity knows that app is opened from notification and opens
                //app on Recent tab
                notificationIntent.putExtra(NOTIFICATION_ID_KEY, 42);

                // This ensures that the back button follows the recommended convention for the back key.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                // Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(MainActivity.class);
                // Adds the Intent that starts the Activity to the top of the stack.
                stackBuilder.addNextIntent(notificationIntent);

                //Set PendingIntent so notification arrives even when app is not running
                PendingIntent contentIntent = PendingIntent.getActivity(context, 3, notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                //Set pending intent to notification builder
                builder.setContentIntent(contentIntent);

                //To close notification after clicking it
                builder.setAutoCancel(true);

                //Launch notification
                NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                manager.notify(0, builder.build());
            }
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