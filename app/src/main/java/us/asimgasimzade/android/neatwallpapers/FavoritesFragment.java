package us.asimgasimzade.android.neatwallpapers;


import android.content.Intent;
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

import java.util.ArrayList;
import java.util.Collections;

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
    /*private MultiSwipeRefreshLayout swipeContainer;*/
    private ArrayList<GridItem> mGridData;
    private ImagesGridViewAdapter mGridAdapter;
    private DatabaseReference favoritesReference;
    private GridItem currentItem;
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

                //Pass image url and source to SingleImageActivity
                Intent intent = new Intent(getActivity(), SingleImageActivity.class);
                intent.putExtra("number", item.getNumber());
                intent.putExtra("source", "favorites");

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
        Query favoritesQuery = favoritesReference.orderByPriority();
        eventListener = favoritesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mGridData = new ArrayList<>();
                int i = 0;
                //Loop through all favorite images and assign their values to gridItem
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    currentItem = child.getValue(GridItem.class);
                    currentItem.setNumber(i);
                    mGridData.add(currentItem);
                    i++;
                }

                //Reversing values in mGridData to show descending list of items in Favorites page
                Collections.reverse(mGridData);
                ImagesDataClass.favoriteImagesList = mGridData;
                mGridAdapter = new ImagesGridViewAdapter(getActivity(), R.layout.image_grid_item_layout, mGridData);
                mGridView.setAdapter(mGridAdapter);
                mProgressBar.setVisibility(View.INVISIBLE);

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
    public void onPause() {
        super.onPause();
        if (eventListener != null) {
            favoritesReference.removeEventListener(eventListener);
        }
    }
}