package com.asimqasimzade.android.neatwallpapers;

/**
 * Created by Asim on 12/1/2016.
 */

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

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


public class PopularFragment extends Fragment{

    private static final String TAG = PopularFragment.class.getSimpleName();
    private GridView mGridView;
    private ProgressBar mProgressBar;
    private GridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;
    private URL feed_url;

    public PopularFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_popular, container, false);

        mGridView = (GridView) rootView.findViewById(R.id.gridView);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        try{
            feed_url =
                    new URL("https://pixabay.com/api/?key=3898774-ad29861c5699760086a93892b&image_type=photo&order=popular&per_page=100");
        } catch (MalformedURLException e){
            e.printStackTrace();
        }

        //Initialize with empty data
        mGridData = new ArrayList<>();
        mGridAdapter = new GridViewAdapter(getActivity(), R.layout.grid_item_layout, mGridData);
        mGridView.setAdapter(mGridAdapter);

        //Start Download
        new AsyncHttpTask().execute();
        mProgressBar.setVisibility(View.VISIBLE);

        return rootView;

    }

    //Downloading data asynchronously
    class AsyncHttpTask extends AsyncTask<String, Void, Integer> {
        HttpURLConnection urlConnection;
        @Override
        protected Integer doInBackground(String... params) {
            Integer result = 0;

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
                Toast.makeText(getActivity(), "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
            mProgressBar.setVisibility(View.GONE);
        }

        String streamToString(InputStream stream) throws IOException {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
            String line;
            String result = "";
            while((line = bufferedReader.readLine()) != null){
                result += line;
            }

            //Close stream
            if(null != stream){
                stream.close();
            }
            return result;
        }

        /**
         * Parsing the feed results and get the list
         * @param result
         */
        private void parseResult(String result){
            try{
                JSONObject rootJson = new JSONObject(result);
                JSONArray hits = rootJson.optJSONArray("hits");
                GridItem item;
                for(int i = 0; i<hits.length();i++){

                    item = new GridItem();

                    if(null != hits && hits.length() > 0){
                        JSONObject image = hits.getJSONObject(i);
                        if(image != null){
                            item.setImage(image.getString("webformatURL"));
                        }
                    }
                    mGridData.add(item);
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

}