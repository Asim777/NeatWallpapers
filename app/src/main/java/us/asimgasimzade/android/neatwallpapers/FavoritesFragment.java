package us.asimgasimzade.android.neatwallpapers;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

import java.util.ArrayList;
import java.util.Collections;

import us.asimgasimzade.android.neatwallpapers.adapters.ImagesGridViewAdapter;
import us.asimgasimzade.android.neatwallpapers.data.GridItem;
import us.asimgasimzade.android.neatwallpapers.data.ImagesDataClass;
import us.asimgasimzade.android.neatwallpapers.tasks.LoadFavoritesTaskFragment;
import us.asimgasimzade.android.neatwallpapers.utils.Utils;

/**
 * This holds Favorite images gridView and populated from FireBase database
 */

public class FavoritesFragment extends Fragment implements LoadFavoritesTaskFragment.TaskCallbacks {

    private static final String TAG_TASK_FRAGMENT = "task_fragment";

    GridView mGridView;
    View rootView;
    ProgressBar mProgressBar;
    private DatabaseReference favoritesReference;
    private ValueEventListener eventListener;
    FirebaseAuth auth;
    FirebaseUser authUser;
    FirebaseDatabase database;
    private boolean firstTimeLoadHappened;
    LoadFavoritesTaskFragment mLoadFavoritesTaskFragment;
    FragmentManager fm;
    ImagesGridViewAdapter mGridAdapter;

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
        Query favoritesQuery = favoritesReference.orderByChild("firstAddedTime");
        eventListener = favoritesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // We are checking this because inside LoadFavoritesTask we are updating expired items
                // and changing database which initiates onDataChange call and it creates a loop.
                // We want LoadFavoritesTask to be run only once per lifetime of Fragment. To make
                // any change to database user has to leave the Fragment (Go to SingleImageFragment
                // and add new image to Favorites), that's why on onPause we are resetting firstTimeLoadHappened
                // boolean to false so that when user is back to FavoritesFragment it updates the GridView
                if(!firstTimeLoadHappened){
                    //Save DataSnapshot to ImagesDataClass static variable
                    ImagesDataClass.favoritesDataSnapshot = dataSnapshot;
                    //Load favorites from FireBase database and update them if they expired
                    fm = getFragmentManager();
                    mLoadFavoritesTaskFragment = (LoadFavoritesTaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);
                    // If the Fragment is non-null, then it is currently being
                    // retained across a configuration change.
                    if (mLoadFavoritesTaskFragment == null) {
                        mLoadFavoritesTaskFragment = new LoadFavoritesTaskFragment();

                        fm.beginTransaction().add(mLoadFavoritesTaskFragment, TAG_TASK_FRAGMENT).commit();
                    }
                    //Passing current instance of FavoritesFragment to FavoritesTaskFragment
                    mLoadFavoritesTaskFragment.attachNewFragment(FavoritesFragment.this);
                    firstTimeLoadHappened = true;
                }
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

    @Override
    public void onCancelled() {
        //LoadFavoritesTask is cancelled
        Utils.showToast(getActivity().getApplicationContext(),
                getActivity().getString(R.string.downloading_favorites_fail_message),
                Toast.LENGTH_LONG);
    }

    @Override
    public void onPostExecute(ArrayList<GridItem> updatedGridData) {
        //Reversing values in mGridData to show descending list of items in Favorites page
        Collections.reverse(updatedGridData);
        ImagesDataClass.favoriteImagesList = updatedGridData;
        mGridAdapter = new ImagesGridViewAdapter(FavoritesFragment.this.getActivity(), updatedGridData);
        mGridView.setAdapter(mGridAdapter);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public DatabaseReference getFavoritesReference() {
        return favoritesReference;
    }


    @Override
    public void onPause() {
        super.onPause();
        if (eventListener != null) {
            favoritesReference.removeEventListener(eventListener);
        }
        firstTimeLoadHappened = false;
    }
}