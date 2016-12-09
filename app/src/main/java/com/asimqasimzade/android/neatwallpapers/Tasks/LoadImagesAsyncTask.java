package com.asimqasimzade.android.neatwallpapers.Tasks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.asimqasimzade.android.neatwallpapers.Adapters.ImagesGridViewAdapter;
import com.asimqasimzade.android.neatwallpapers.Data.GridItem;
import com.asimqasimzade.android.neatwallpapers.PopularFragment;
import com.asimqasimzade.android.neatwallpapers.R;

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


//Downloading data asynchronously
public class LoadImagesAsyncTask extends AsyncTask<String, Void, Integer> {

    private URL feed_url;
    private HttpURLConnection urlConnection;
    private static final String TAG = PopularFragment.class.getSimpleName();
    private ProgressBar mProgressBar;
    private ImagesGridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;
    private Context mContext;
    private View mRootView;
    private String mUrl;


    public LoadImagesAsyncTask(Context context, View rootView, String url){
        mContext = context;
        mRootView = rootView;
        mUrl = url;

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

        try{
            feed_url = new URL(mUrl);
        } catch (MalformedURLException e){
            e.printStackTrace();
        }

        try {
            //Create Apache HttpClient
            urlConnection = (HttpURLConnection) feed_url.openConnection();
            int statusCode =  urlConnection.getResponseCode();

            //200 represent status is OK
            if(statusCode == 200){
                String response = streamToString(urlConnection.getInputStream());
                parseResult(response);
                result = 1; //Successful
            } else {
                result = 0; //Failed
            }
        } catch(Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
        } finally {
            urlConnection.disconnect();
        }
        return result;
    }

    @Override
    protected void onPostExecute(Integer result) {
        //Download complete, let's update UI
        if(result == 1){
            mGridAdapter.setGridData(mGridData);
        } else {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if(activeNetworkInfo == null){
                Toast.makeText(mContext, "Please check the status of the network.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
        }
        mProgressBar.setVisibility(View.GONE);
    }

    private String streamToString(InputStream stream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        String line;
        String result = "";
        while((line = bufferedReader.readLine()) != null){
            result += line;
        }

        //Close stream
        stream.close();
        return result;
    }

    /**
     * Parsing the feed results and get the list
     * @param result is result String we got from InputStreamReader
     */
    private void parseResult(String result){
        try{
            JSONObject rootJson = new JSONObject(result);
            JSONArray hits = rootJson.optJSONArray("hits");
            GridItem item;
            for(int i = 0; i<hits.length();i++){

                item = new GridItem();

                if(hits.length() > 0){
                    JSONObject image = hits.getJSONObject(i);
                    if(image != null){
                        item.setImage(image.getString("webformatURL"));
                        item.setName(image.getString("id"));
                    }
                }
                mGridData.add(item);
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }
}