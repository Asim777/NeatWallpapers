package us.asimgasimzade.android.neatwallpapers.tasks;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import us.asimgasimzade.android.neatwallpapers.R;
import us.asimgasimzade.android.neatwallpapers.data.GridItem;
import us.asimgasimzade.android.neatwallpapers.data.ImagesDataClass;
import us.asimgasimzade.android.neatwallpapers.utils.Utils;

/**
 * This Fragment manages LoadFavoritesTask background task and retains
 * itself across configuration changes.
 */

public class LoadFavoritesTaskFragment extends Fragment {

    private TaskCallbacks mCallbacks;
    String updatedFavoriteUrlString;


    /**
     * Callback interface through which the fragment will report the
     * task's progress and results back to the FavoritesFragment.
     */
    public interface TaskCallbacks {
        void onCancelled();

        void onPostExecute(ArrayList<GridItem> updatedGridData);

        DatabaseReference getFavoritesReference();
    }


    //Required empty default constructor
    public LoadFavoritesTaskFragment() {
    }

    /**
     * Hold a reference to the parent Fragment so we can report the
     * task's current progress and results. FavoritesFragment will
     * pass us a reference to the newly created parent Fragment after
     * each configuration change.
     */


    public void attachNewFragment(Fragment callerFragment) {
        if (callerFragment instanceof TaskCallbacks) {
            mCallbacks = (TaskCallbacks) callerFragment;
            LoadFavoritesTask loadFavoritesTask = new LoadFavoritesTask();
            loadFavoritesTask.execute();
        }
    }

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);
        updatedFavoriteUrlString = getString(R.string.url_part_one) +
                getString(R.string.pixabay_key) + "&response_group=high_resolution&id=";
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Fragment instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }


    /**
     * Loads list of Favorite images from FireBase database and populates GridView with them
     * Also takes care of updating expired image url's in database.
     * It proxies progress, updates and results back to the FavoritesFragment.
     * Note that we need to check if the callbacks are null in each
     * method in case they are invoked after the Activity's and
     * Fragment's onDestroy() method have been called.
     */
    private class LoadFavoritesTask extends AsyncTask<String, Void, ArrayList<GridItem>> {

        String mImageName;
        DataSnapshot mDataSnapshot;
        ArrayList<GridItem> mGridData;
        GridItem mCurrentItem;
        URL mUrl;
        SimpleDateFormat simpleDateFormat;
        Date mImageTimestamp;
        Date nowDate;
        HttpURLConnection mUrlConnection;
        String updatedImage;
        String updatedThumbnail;
        DatabaseReference favoritesReference;

        LoadFavoritesTask() {
            mDataSnapshot = ImagesDataClass.favoritesDataSnapshot;
        }

        @Override
        protected void onPreExecute() {
            mGridData = new ArrayList<>();
            simpleDateFormat = new SimpleDateFormat("ddMMyyyy-HHmmss", Locale.US);
            nowDate = new Date();
        }

        @Override
        protected ArrayList<GridItem> doInBackground(String... params) {
            int i = ((int) mDataSnapshot.getChildrenCount()) - 1;
            //Loop through all favorite images and assign their values to gridItem
            for (DataSnapshot child : mDataSnapshot.getChildren()) {
                if (!isCancelled()) {
                    mCurrentItem = child.getValue(GridItem.class);
                    //Getting timestamp (when image was added to favorite or last time updated
                    // after being expired)
                    String timestamp = child.child("timestamp").getValue().toString();

                    try {
                        mImageTimestamp = simpleDateFormat.parse(timestamp);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    //After 2 days Pixabay updates image url's so in images favorites lose all their urls
                    //That's why we are checking if 2 days has pass since image added to database and if yes,
                    //we are getting new urls from Pixabay API using image's id_hash
                    if (Utils.checkNetworkConnection(
                            LoadFavoritesTaskFragment.this.getActivity().getApplicationContext(),
                            false) && imageHasExpired()) {
                        mImageName = mCurrentItem.getName();
                        try {
                            mUrl = new URL(updatedFavoriteUrlString + mImageName);
                            //Create Apache HttpClient
                            mUrlConnection = (HttpURLConnection) mUrl.openConnection();
                            int statusCode = mUrlConnection.getResponseCode();
                            //200 represent status is OK
                            if (statusCode == 200) {
                                String response = streamToString(mUrlConnection.getInputStream());
                                parseResult(response);
                                //Update timestamp
                                mCurrentItem.setTimestamp(simpleDateFormat.format(new Date()));
                            } /*else {
                                //Url request Failed
                            }*/
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            mUrlConnection.disconnect();
                        }

                        //Updating GridItem's image and thumbnail url and adding it to FireBase database
                        //instead of expired ones
                        if (updatedImage != null && updatedThumbnail != null) {
                            mCurrentItem.setImage(updatedImage);
                            mCurrentItem.setThumbnail(updatedThumbnail);
                            favoritesReference = mCallbacks.getFavoritesReference();
                            if (favoritesReference != null) {
                                favoritesReference.child(mCurrentItem.getName()).setValue(mCurrentItem);
                            }
                        }
                    }
                    mCurrentItem.setNumber(i);
                    mGridData.add(mCurrentItem);

                    i--;
                }
            }

            return mGridData;
        }

        @Override
        protected void onCancelled() {
            if (mCallbacks != null) {
                mCallbacks.onCancelled();
            }
        }

        @Override
        protected void onPostExecute(ArrayList<GridItem> updatedGridData) {

            if (mCallbacks != null) {
                mCallbacks.onPostExecute(updatedGridData);
            }
        }

        /**
         * Getting String from Input stream
         *
         * @param stream - InputStream to convert to String
         * @return result - result String
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
         * Parse response String with JSON, get updated Image url and Thumbnail for each image
         *
         * @param response - JSON response as String
         */
        private void parseResult(String response) {
            try {
                JSONObject rootJson = new JSONObject(response);
                JSONArray hits = rootJson.optJSONArray("hits");
                if (hits.length() > 0) {
                    JSONObject image = hits.getJSONObject(0);
                    if (image != null) {
                        updatedImage = image.getString("largeImageURL");
                        updatedThumbnail = image.getString("webformatURL");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        /**
         * If image has been added more than 1 day ago, it's expired and we need to update it's
         * url so that user can see it in Favorites page
         *
         * @return long differenceInDays - how many complete days ago image url has been updated last time
         */
        private boolean imageHasExpired() {
            long difference = nowDate.getTime() - mImageTimestamp.getTime();
            long differenceInDays = difference / (1000 * 60 * 60 * 24);
            //If image has been added more than 4 days ago, it's expired and we need to update it's
            //url so that user can see it in Favorites page
            return differenceInDays >= 1;
        }
    }

}
