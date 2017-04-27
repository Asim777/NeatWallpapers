package us.asimgasimzade.android.neatwallpapers.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import us.asimgasimzade.android.neatwallpapers.R;
import us.asimgasimzade.android.neatwallpapers.adapters.ImagesGridViewAdapter;
import us.asimgasimzade.android.neatwallpapers.data.GridItem;
import us.asimgasimzade.android.neatwallpapers.data.ImagesDataClass;
import us.asimgasimzade.android.neatwallpapers.utils.NoResultsCallbackInterface;

import static us.asimgasimzade.android.neatwallpapers.utils.Utils.checkNetworkConnection;

/**
 * This task is called from MainActivity(PopularFragment and RecentFragment), SingleCategoryActivity
 * and SearchResultActivity to load images from online database and pass them to adapter
 */

//Downloading data asynchronously
public class LoadImagesAsyncTask extends AsyncTask<String, Void, Integer> {

    private int offset;
    private HttpURLConnection urlConnection;
    private ProgressBar mProgressBar;
    private ImagesGridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;
    private WeakReference<Activity> mActivityReference;
    private View mRootView;
    private String mUrl;
    private String mSource;
    private SwipeRefreshLayout mSwipeContainer;
    private int numberOfPages = 1;
    private WeakReference<NoResultsCallbackInterface> noResultsCallbackReference;


    public LoadImagesAsyncTask(Activity activity, View rootView, String url, SwipeRefreshLayout swipeContainer, String source) {
        mActivityReference = new WeakReference<>(activity);
        mRootView = rootView;
        mUrl = url;
        mSwipeContainer = swipeContainer;
        mSource = source;
    }

    //This constructor is to be called from SearchResultsActivity, where we don't have SwipeRefreshLayout
    public LoadImagesAsyncTask(Activity activity, View rootView, String url, String source) {
        mActivityReference = new WeakReference<>(activity);
        mRootView = rootView;
        mUrl = url;
        mSource = source;
        if (mSource.equals("search")) {
            noResultsCallbackReference = new WeakReference<>((NoResultsCallbackInterface) activity);
        }
    }

    @Override
    protected void onPreExecute() {
        GridView mGridView;
        mProgressBar = (ProgressBar) mRootView.findViewById(R.id.progressBar);
        mGridView = (GridView) mRootView.findViewById(R.id.gridView);

        //Initialize mGridData with empty data and set it to mGridAdapter
        mGridData = new ArrayList<>();
        mGridAdapter = new ImagesGridViewAdapter(mActivityReference.get(), mGridData);
        mGridView.setAdapter(mGridAdapter);
    }

    @Override
    protected Integer doInBackground(String... params) {
        Integer result = 0;
        URL feed_url;

        //Loop through pages
        for (int i = 0; i < numberOfPages; i++) {
            try {
                feed_url = new URL(mUrl + "&page=" + (i + 1));
                //Create Apache HttpClient
                urlConnection = (HttpURLConnection) feed_url.openConnection();

                int statusCode = urlConnection.getResponseCode();

                //200 represent status is OK
                if (statusCode == 200) {
                    String response = streamToString(urlConnection.getInputStream());
                    if (i == 0) {
                        numberOfPages = getNumberOfPages(response);
                    }
                    parseResult(response, i);
                    result = 1; //Successful
                } else {
                    result = 0; //Failed
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(urlConnection != null)
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
            checkNetworkConnection(mActivityReference.get().getApplicationContext(), true);
        }
        //Save mGridData in separate class ImagesDataClass to use later when user scrolls to
        // other images from SingleImageActivity
        switch (mSource) {
            case "popular": {
                ImagesDataClass.popularImagesList = mGridData;
                ImagesDataClass.defaultImagesList = mGridData;
            }
            break;
            case "recent": {
                ImagesDataClass.recentImagesList = mGridData;
            }
            break;
            case "search": {
                ImagesDataClass.searchResultImagesList = mGridData;
                NoResultsCallbackInterface ref = noResultsCallbackReference.get();
                if (ImagesDataClass.searchResultImagesList.isEmpty() && ref != null) {
                    ref.noResults();
                }
            }
            break;
            case "default": {
                ImagesDataClass.defaultImagesList = mGridData;
            }
            break;
        }
        mProgressBar.setVisibility(View.GONE);
        if (mSwipeContainer != null) {
            mSwipeContainer.setRefreshing(false);
        }
    }

    /**
     * Creates a string out of InputStream and returns it
     *
     * @param stream - InputStream to turn into String
     *
     * @return result - String made of InputStream
     *
     * @throws IOException if InputStream is null
     */

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
     * Parses the feed results and get the list
     *
     * @param result - String we got from InputStreamReader
     * @param pageNumber - Number of pages in current query
     */
    private void parseResult(String result, int pageNumber) {

        try {
            JSONObject rootJson = new JSONObject(result);
            JSONArray hits = rootJson.optJSONArray("hits");

            GridItem item;
            if (hits.length() > 0) {
                for (int i = 0; i < 200; i++) {

                    int width;
                    int height;

                    JSONObject image = hits.getJSONObject(i);
                    if (image != null) {
                        width = image.getInt("imageWidth");
                        height = image.getInt("imageHeight");

                        if (imageMeetsRequirements(width, height)) {
                            item = new GridItem(image.getString("largeImageURL"),
                                    image.getString("id_hash"),
                                    image.getString("user"),
                                    image.getString("fullHDURL"),
                                    image.getString("webformatURL"));

                            switch (pageNumber) {
                                case 0:
                                    item.setNumber(i - offset);
                                    break;
                                case 1:
                                    item.setNumber(i + 200 - offset);
                                    break;
                                case 2:
                                    item.setNumber(i + 400 - offset);
                                    break;
                                case 3:
                                    item.setNumber(i + 600 - offset);
                                    break;
                                case 4:
                                    item.setNumber(i + 800 - offset);
                                    break;
                            }
                            mGridData.add(item);
                        } else {
                            offset++;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return true if aspect ratio meets requirements (width / height is less or equal to 1.7)
     *
     * @param width  - width of image
     * @param height - height of image
     *
     * @return boolean - true if image meets requirements
     */
    private boolean imageMeetsRequirements(int width, int height) {
        double mWidth = (double) width;
        double mHeight = (double) height;
        return (mWidth / mHeight <= 1.7);
    }

    /**
     * Returns number of pages to loop through
     *
     * @param result - String we got from InputStreamReader
     *
     * @return int - number of pages
     */
    private int getNumberOfPages(String result) {
        int pages = 0;

        try {
            //Finding out number of pages
            JSONObject rootJson = new JSONObject(result);

            if (rootJson.getInt("totalHits") % 200 != 0) {
                pages = (rootJson.getInt("totalHits") / 200) + 1;
            } else {
                pages = rootJson.getInt("totalHits") / 200;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return pages;
    }
}