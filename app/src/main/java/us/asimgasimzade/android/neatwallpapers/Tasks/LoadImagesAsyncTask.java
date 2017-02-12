package us.asimgasimzade.android.neatwallpapers.Tasks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;
import us.asimgasimzade.android.neatwallpapers.R;

import us.asimgasimzade.android.neatwallpapers.Adapters.ImagesGridViewAdapter;
import us.asimgasimzade.android.neatwallpapers.Data.GridItem;
import us.asimgasimzade.android.neatwallpapers.Data.ImagesDataClass;
import us.asimgasimzade.android.neatwallpapers.PopularFragment;

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
import java.util.ArrayList;

/**
 * This task is called from MainActivity, SingleCategoryActivity and SearchResultActivity to load
 * images from online database and pass them to adapter
 */

//Downloading data asynchronously
public class LoadImagesAsyncTask extends AsyncTask<String, Void, Integer> {

    private static final String LOG_TAG = "LoadImagesAsyncTask";
    private URL feed_url;
    private HttpURLConnection urlConnection;
    private static final String TAG = PopularFragment.class.getSimpleName();
    private ProgressBar mProgressBar;
    private ImagesGridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;
    private Context mContext;
    private View mRootView;
    private String mUrl;
    private String mSource;
    private int numberOfPages = 1;

    public LoadImagesAsyncTask(Context context, View rootView, String url, String source) {
        mContext = context;
        mRootView = rootView;
        mUrl = url;
        mSource = source;
    }

    @Override
    protected void onPreExecute() {
        mProgressBar = (ProgressBar) mRootView.findViewById(R.id.progressBar);
        GridView mGridView = (GridView) mRootView.findViewById(R.id.gridView);
        //Initialize with empty data
        mGridData = new ArrayList<>();
        mGridAdapter = new ImagesGridViewAdapter(mContext, R.layout.image_grid_item_layout, mGridData);
        mGridView.setAdapter(mGridAdapter);
    }

    @Override
    protected Integer doInBackground(String... params) {
        Integer result = 0;

        //Loop through pages
        for (int i = 0; i < numberOfPages; i++) {
            try {
                mUrl = mUrl + "&page=" + (i + 1);
                feed_url = new URL(mUrl);
                //Create Apache HttpClient
                urlConnection = (HttpURLConnection) feed_url.openConnection();
                int statusCode = urlConnection.getResponseCode();

                //200 represent status is OK
                if (statusCode == 200) {
                    String response = streamToString(urlConnection.getInputStream());
                    if(i == 0) {
                        numberOfPages = getNumberOfPages(response);
                    }
                    parseResult(response, i);
                    result = 1; //Successful
                } else {
                    result = 0; //Failed
                }
            } catch (MalformedURLException e) {
                Log.d(TAG, "Problem creating URL");
                e.printStackTrace();
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(Integer result) {
        //Download complete, let's update UI
        if (result == 1) {
            mGridAdapter.setGridData(mGridData);
        } else {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            try {
                if (activeNetworkInfo == null || !activeNetworkInfo.isConnectedOrConnecting()) {
                    Toast.makeText(mContext, "Please check the status of the network.", Toast.LENGTH_SHORT).show();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Can't check if network info isConnectedOrConnecting");
            }
        }
        //Save mGridData in separate class ImagesDataClass to use later when user scrolls to
        // other images from SingleImageActivity
        switch (mSource){
            case "popular" : {
                ImagesDataClass.popularImagesList = mGridData;
                ImagesDataClass.imageslist = mGridData;
            }
                break;
            case "recent" : {
                ImagesDataClass.recentImagesList = mGridData;
            }
                break;
            case "default" : {
                ImagesDataClass.imageslist = mGridData;
            }

        }
        mProgressBar.setVisibility(View.GONE);
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

    /**
     * Parsing the feed results and get the list
     *
     * @param result is result String we got from InputStreamReader
     */
    private void parseResult(String result, int pageNumber) {
        try {
            JSONObject rootJson = new JSONObject(result);
            JSONArray hits = rootJson.optJSONArray("hits");

            GridItem item;
            for (int i = 0; i < 200; i++) {

                item = new GridItem();

                if (hits.length() > 0) {
                    JSONObject image = hits.getJSONObject(i);
                    if (image != null) {
                        item.setImage(image.getString("webformatURL"));
                        item.setName(image.getString("id"));
                        item.setAuthor(image.getString("user"));
                        item.setLink(image.getString("pageURL"));
                        switch (pageNumber) {
                            case 0:
                                item.setNumber(i);
                                break;
                            case 1:
                                item.setNumber(i + 200);
                                break;
                            case 2:
                                item.setNumber(i + 400);
                                break;
                            case 3:
                                item.setNumber(i + 600);
                                break;
                            case 4:
                                item.setNumber(i + 800);
                                break;
                        }
                    }
                }
                mGridData.add(item);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returning number of pages to loop through
     *
     * @param result is result String we got from InputStreamReader
     */
    private int getNumberOfPages(String result) {
        int pages = 0;

        try{
        //Finding out number of pages
        JSONObject rootJson = new JSONObject(result);

        if(rootJson.getInt("totalHits")%200 != 0) {
            pages = (rootJson.getInt("totalHits") / 200) + 1;
        } else {
            pages =  rootJson.getInt("totalHits") / 200;
        }} catch (JSONException e){
            e.printStackTrace();
        }

        return pages;
    }
}