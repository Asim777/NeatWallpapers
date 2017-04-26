package us.asimgasimzade.android.neatwallpapers;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import us.asimgasimzade.android.neatwallpapers.adapters.ImagesGridViewAdapter;
import us.asimgasimzade.android.neatwallpapers.data.GridItem;
import us.asimgasimzade.android.neatwallpapers.data.ImagesDataClass;
import us.asimgasimzade.android.neatwallpapers.utils.Utils;

/**
 * This holds Favorite images gridView and populated from FavoritesDB
 */

public class FavoritesFragment extends Fragment {

    GridView mGridView;
    View rootView;
    ProgressBar mProgressBar;

    private DatabaseReference favoritesReference;
    private ValueEventListener eventListener;
    FirebaseAuth auth;
    FirebaseUser authUser;
    FirebaseDatabase database;

    public FavoritesFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_favorites, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.gridView);

        //Get reference to auth instance and FireBase user
        auth = FirebaseAuth.getInstance();
        authUser = auth.getCurrentUser();
        //Get user id
        String userId = authUser != null ? authUser.getUid() : null;
        //Get instance of FireBase database
        database = FirebaseDatabase.getInstance();
        //Get reference to current user's favorites node
        if (userId != null) {
            favoritesReference = database.getReference("users").child(userId).child("favorites");
        }

        //Get reference to a gridView and progressBar and show the ProgressBar
        GridView mGridView = (GridView) rootView.findViewById(R.id.gridView);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        //Setting onItemClickListener to GridView which starts intent and goes to SingleImageActivity
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Get an item position
                GridItem item = (GridItem) parent.getItemAtPosition(position);

                //Pass image url and imageSource to SingleImageActivity
                Intent intent = new Intent(getActivity(), SingleImageActivity.class);
                intent.putExtra("number", item.getNumber());
                intent.putExtra("imageSource", "favorites");

                //Start SingleImageActivity
                startActivity(intent);
            }
        });
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        // This listener gets triggered each time there's change in user's favorites node nad
        // updates Favorites Fragment's gridView with new images
        Query favoritesQuery = favoritesReference.orderByChild("timestamp");
        eventListener = favoritesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Load favorites from Firebase database
                new LoadFavoritesTask(dataSnapshot).execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Utils.showToast(getActivity().getApplicationContext(),
                        getActivity().getString(R.string.downloading_favorites_fail_message),
                        Toast.LENGTH_LONG);
            }
        });
    }

    private class LoadFavoritesTask extends AsyncTask<String, Void, ArrayList<GridItem>> {

        String mImageName;
        DataSnapshot mDataSnapshot;
        ArrayList<GridItem> mGridData;
        GridItem mCurrentItem;
        ImagesGridViewAdapter mGridAdapter;
        String updatedFavoriteUrlString = "https://pixabay.com/api/?key=" + getString(R.string.pixabay_key) + "&response_group=high_resolution&id=";
        URL mUrl;
        SimpleDateFormat simpleDateFormat;
        Date mImageTimestamp;
        Date nowDate;
        HttpURLConnection mUrlConnection;
        String updatedImage;
        String updatedThumbnail;

        LoadFavoritesTask(DataSnapshot dataSnapshot) {
            mDataSnapshot = dataSnapshot;
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
                mCurrentItem = child.getValue(GridItem.class);
                //Getting timestamp (when image was added to favorite or last time updated after being expired)
                String timestamp = child.child("timestamp").getValue().toString();

                try {
                    mImageTimestamp = simpleDateFormat.parse(timestamp);
                } catch (ParseException e){
                    e.printStackTrace();
                }

                //After 2 days pixabay updates image url's so in images favorites lose all their urls
                //That's why we are cheking if 2 days has pass since image added to database and if yes,
                //we are getting new urls from pixabay API using image's id_hash
                if (Utils.checkNetworkConnection(FavoritesFragment.this.getActivity().getApplicationContext(),
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

                    //Updating GridItem's image and thumbnail url and timestamp adding it to FireBase database
                    //instead of expired ones
                    if (updatedImage != null && updatedThumbnail != null) {
                        mCurrentItem.setImage(updatedImage);
                        mCurrentItem.setThumbnail(updatedThumbnail);
                        favoritesReference.child(mCurrentItem.getName()).setValue(mCurrentItem);
                    }
                }
                mCurrentItem.setNumber(i);
                mGridData.add(mCurrentItem);
                i--;
            }

            return mGridData;
        }

        @Override
        protected void onPostExecute(ArrayList<GridItem> updatedGridData) {
            //Reversing values in mGridData to show descending list of items in Favorites page
            Collections.reverse(updatedGridData);
            ImagesDataClass.favoriteImagesList = updatedGridData;
            mGridAdapter = new ImagesGridViewAdapter(FavoritesFragment.this.getActivity(), updatedGridData);
            mGridView.setAdapter(mGridAdapter);
            mProgressBar.setVisibility(View.INVISIBLE);
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
         * If image has been added more than 4 days ago, it's expired and we need to update it's
         * url so that user can see it in Favorites page
         *
         * @return long differenceInDays - how many complete days ago image url has been updated last time
         */
        private boolean imageHasExpired() {
            long difference = nowDate.getTime() - mImageTimestamp.getTime();
            long differenceInDays = difference / (1000 * 60 * 60 * 24);
            //If image has been added more than 4 days ago, it's expired and we need to update it's
            //url so that user can see it in Favorites page
            return differenceInDays >= 4;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (eventListener != null) {
            favoritesReference.removeEventListener(eventListener);
        }
    }
}