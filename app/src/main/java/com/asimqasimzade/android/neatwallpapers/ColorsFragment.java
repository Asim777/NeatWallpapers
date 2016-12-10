package com.asimqasimzade.android.neatwallpapers;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.asimqasimzade.android.neatwallpapers.Adapters.ColorsListViewAdapter;
import com.asimqasimzade.android.neatwallpapers.Data.Color;

import java.util.ArrayList;


public class ColorsFragment extends Fragment{

    private ListView mColorsListView;
    private View rootView;
    private ArrayList<Color> mColorData = new ArrayList<>();
    private ColorsListViewAdapter mColorAdapter;

    //String array for holding list of categories
    public String[] colorNames = new String[]{
            "Black", "White", "Red", "Blue", "Green", "Yellow", "Purple",
            "Grey", "Orange", "Brown"
    };

    public int[] colorThumbnails = new int[]{
            R.drawable.black,
            R.drawable.white,
            R.drawable.red,
            R.drawable.blue,
            R.drawable.green,
            R.drawable.yellow,
            R.drawable.purple,
            R.drawable.grey,
            R.drawable.orange,
            R.drawable.brown
    };


    public ColorsFragment() {
        // We need to set mColorData only once when Fragment is started so ListView doesn't get
        //populated with the same data again when Fragment is relaunched
        for (int i = 0; i < colorNames.length; i++) {
            Color color = new Color();
            color.setColorName(colorNames[i]);
            color.setColorThumbnail(colorThumbnails[i]);
            mColorData.add(color);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_colors, container, false);
        mColorsListView = (ListView) rootView.findViewById(R.id.colors_listView);

        // Create new ArrayAdapter - giving it arguments - context, single row xml, which is
        // color_list_item_layout.xml and array to take data from
        mColorAdapter = new ColorsListViewAdapter(getActivity(), R.layout.color_list_item_layout, mColorData);
        // If ListView is not null, set ArrayAdapter to this ListView
        if (mColorsListView != null) {
            mColorsListView.setAdapter(mColorAdapter);
        }

        //Set OnItemClickListener, so when user clicks on a color, according color is opened
        mColorsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Get an item position
                Color color = (Color) parent.getItemAtPosition(position);

                Intent openColorIntent = new Intent(getActivity(), SingleColorActivity.class);
                openColorIntent.putExtra("colorName", color.getColorName());
                startActivity(openColorIntent);
            }
        });


        return rootView;
    }


}